package happyyoung.trashnetwork.cleaning.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.model.UserLocation;
import happyyoung.trashnetwork.cleaning.service.MqttService;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.GsonUtil;
import happyyoung.trashnetwork.cleaning.util.ImageUtil;

public class MonitorFragment extends Fragment {
    private static final String LOG_TAG_GEO_CODER = "GeoCoder";

    private BaiduMap baiduMap;
    private boolean mapCenterFlag = false;

    private View rootView;
    @BindView(R.id.bmap_view) MapView mMapView;
    @BindView(R.id.user_location_area) View userLocationView;
    @BindView(R.id.txt_user_location) TextView txtUserLocation;
    @BindView(R.id.txt_user_update_time) TextView txtUserUpdateTime;

    @BindView(R.id.cleaner_view_area) View cleanerLocationView;
    @BindView(R.id.cleaner_portrait) ImageView cleanerPortrait;
    @BindView(R.id.txt_cleaner_name) TextView txtCleanerName;
    @BindView(R.id.txt_cleaner_location) TextView txtCleanerLocation;
    @BindView(R.id.txt_cleaner_update_time) TextView txtCleanerUpdateTime;

    @BindView(R.id.trash_view_area) View trashMonitorView;
    @BindView(R.id.icon_trash) ImageView iconTrash;
    @BindView(R.id.txt_trash_name) TextView txtTrashName;
    @BindView(R.id.txt_trash_desc) TextView txtTrashDesc;
    @BindView(R.id.txt_trash_cleaned_time) TextView txtTrashCleanedTime;
    @BindView(R.id.trash_cleaner_portrait) ImageView trashCleanerPortrait;

    private MarkerOptions userMarkerOptions;
    private Marker userMarker;
    private GeoCoder userLocationGeoCoder;
    private GeoCoder cleanerLocationGeoCoder;
    private LocationReceiver locationReceiver;

    private ServiceConnection mqttConn;

    public MonitorFragment() {
        // Required empty public constructor
    }

    public static MonitorFragment newInstance(Context context) {
        MonitorFragment fragment = new MonitorFragment();
        fragment.mqttConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MqttService mqttService = ((MqttService.Binder)service).getService();
                mqttService.addMQTTAction(new MqttService.MqttSubscriptionAction(Application.MQTT_TOPIC_CLEANER_LOCATION,
                        MqttService.TOPIC_TYPE_PUBLIC, null, 0, Application.ACTION_LOCATION));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_monitor, container, false);
        ButterKnife.bind(this, rootView);

        baiduMap = mMapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
        userMarkerOptions = new MarkerOptions()
                            .draggable(false)
                            .alpha((float) 0.9)
                            .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(getContext(), R.drawable.ic_location_red)));
        userLocationGeoCoder = GeoCoder.newInstance();
        userLocationGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if(reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                    showGeoCoderError(reverseGeoCodeResult);
                    txtUserLocation.setText(R.string.unknown_location);
                    return;
                }
                txtUserLocation.setText(reverseGeoCodeResult.getAddress());
            }
        });
        cleanerLocationGeoCoder = GeoCoder.newInstance();

        locationReceiver = new LocationReceiver();
        IntentFilter filter = new IntentFilter(Application.ACTION_LOCATION);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(locationReceiver, filter);

        return rootView;
    }

    @OnClick(R.id.user_location_area)
    void onUserLocationViewClick(View v){
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(
                new LatLng(GlobalInfo.currentLocation.getLatitude(),
                        GlobalInfo.currentLocation.getLongitude())
        ));
    }

    private void showUserLocation(){
        if(GlobalInfo.currentLocation == null || rootView == null)
            return;
        if(userMarker != null)
            userMarker.remove();
        LatLng pos = new LatLng(GlobalInfo.currentLocation.getLatitude(), GlobalInfo.currentLocation.getLongitude());
        userMarkerOptions.position(pos);
        userMarker = (Marker) baiduMap.addOverlay(userMarkerOptions);
        if(!mapCenterFlag){
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(pos));
            mapCenterFlag = true;
        }
        if(cleanerLocationView.getVisibility() == View.VISIBLE || trashMonitorView.getVisibility() == View.VISIBLE)
            return;
        userLocationView.setVisibility(View.VISIBLE);
        txtUserUpdateTime.setText(DateTimeUtil.convertTimestamp(getContext(), GlobalInfo.currentLocation.getUpdateTime(), true, true, true));
        userLocationGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(pos));
    }

    private void showGeoCoderError(ReverseGeoCodeResult reverseGeoCodeResult){
        if(reverseGeoCodeResult == null){
            Log.e(LOG_TAG_GEO_CODER, "Geo coder error");
        }else if(reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
            Log.e(LOG_TAG_GEO_CODER, "Geo coder error code: " + reverseGeoCodeResult.error);
        }
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        getContext().unregisterReceiver(locationReceiver);
        rootView = null;
        userLocationGeoCoder.destroy();
        cleanerLocationGeoCoder.destroy();
        if(mqttConn != null)
            getContext().unbindService(mqttConn);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    private class LocationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(GlobalInfo.user == null)
                return;
            UserLocation newLoc = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_USER_LOCATION_DATA),
                    UserLocation.class);
            if(GlobalInfo.user.getUserId().equals(newLoc.getUserId())){
                showUserLocation();
            }
        }
    }
}
