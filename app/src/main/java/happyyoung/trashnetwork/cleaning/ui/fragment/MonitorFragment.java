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

import com.android.volley.Request;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
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

import java.util.HashMap;
import java.util.Map;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.WorkRecordAdapter;
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.model.UserLocation;
import happyyoung.trashnetwork.cleaning.model.WorkRecord;
import happyyoung.trashnetwork.cleaning.net.PublicResultCode;
import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonListener;
import happyyoung.trashnetwork.cleaning.net.http.HttpApiJsonRequest;
import happyyoung.trashnetwork.cleaning.net.model.result.Result;
import happyyoung.trashnetwork.cleaning.net.model.result.WorkRecordListResult;
import happyyoung.trashnetwork.cleaning.service.MqttService;
import happyyoung.trashnetwork.cleaning.ui.activity.TrashInfoActivity;
import happyyoung.trashnetwork.cleaning.ui.activity.UserInfoActivity;
import happyyoung.trashnetwork.cleaning.util.DateTimeUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.GsonUtil;
import happyyoung.trashnetwork.cleaning.util.ImageUtil;

public class MonitorFragment extends Fragment {
    private static final String LOG_TAG_GEO_CODER = "GeoCoder";
    private static final String BUNDLE_KEY_MARKER_TYPE = "MarkerType";
    private static final String BUNDLE_KEY_USER_ID = "UserID";
    private static final String BUNDLE_KEY_TRASH_ID = "TrashID";
    private static final int MARKER_TYPE_USER = 1;
    private static final int MARKER_TYPE_CLEANER = 2;
    private static final int MARKER_TYPE_TRASH = 3;

    private BaiduMap baiduMap;
    private boolean mapCenterFlag = false;

    @BindDimen(R.dimen.normal_icon_size) int DIMEN_NORMAL_ICON;

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

    private Marker userMarker;
    private Map<Long, Marker> cleanerMarkerMap = new HashMap<>();
    private Map<Long, UserLocation> cleanerLocationMap = new HashMap<>();
    private long currentShowCleanerId = -1;
    private GeoCoder userLocationGeoCoder;
    private GeoCoder cleanerLocationGeoCoder;
    private LocationReceiver locationReceiver;

    private Map<Long, WorkRecord> trashWorkRecordMap = new HashMap<>();
    private long currentShowTrashId = -1;

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
                        MqttService.TOPIC_TYPE_PUBLIC, null, 0, Application.ACTION_CLEANER_LOCATION));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        context.bindService(new Intent(context, MqttService.class), fragment.mqttConn, Context.BIND_AUTO_CREATE);
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
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_CLEANER) {
                    showUserLocationInfo(true);
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle bundle = marker.getExtraInfo();
                switch (bundle.getInt(BUNDLE_KEY_MARKER_TYPE)){
                    case MARKER_TYPE_USER:
                        showUserLocationInfo(true);
                        break;
                    case MARKER_TYPE_CLEANER:
                        showCleanerLocation(bundle.getLong(BUNDLE_KEY_USER_ID), true);
                        break;
                    case MARKER_TYPE_TRASH:
                        showTrashInfo(bundle.getLong(BUNDLE_KEY_TRASH_ID));
                        break;
                }
                return true;
            }
        });
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
        cleanerLocationGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if(reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                    showGeoCoderError(reverseGeoCodeResult);
                    txtCleanerLocation.setText(R.string.unknown_location);
                    return;
                }
                txtCleanerLocation.setText(reverseGeoCodeResult.getAddress());
            }
        });

        MarkerOptions trashMarkerOpts = new MarkerOptions()
                .alpha(0.9f)
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(getContext(), R.drawable.ic_delete_green_32dp)));
        for(Trash t : GlobalInfo.trashList){
            trashMarkerOpts.position(new LatLng(t.getLatitude(), t.getLongitude()));
            Marker trashMarker = (Marker) baiduMap.addOverlay(trashMarkerOpts);
            Bundle extraInfo = new Bundle();
            extraInfo.putInt(BUNDLE_KEY_MARKER_TYPE, MARKER_TYPE_TRASH);
            extraInfo.putLong(BUNDLE_KEY_TRASH_ID, t.getTrashId());
            trashMarker.setExtraInfo(extraInfo);
        }

        locationReceiver = new LocationReceiver();
        IntentFilter filter = new IntentFilter(Application.ACTION_SELF_LOCATION);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(locationReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CLEANER_LOCATION);
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

    @OnClick(R.id.cleaner_view_area)
    void onCleanerLocationViewClick(View v){
        enterUserInfoActivity(currentShowCleanerId);
    }

    @OnClick(R.id.trash_view_area)
    void onTrashMonitorViewClick(View v){
        enterTrashInfoActivity(currentShowTrashId);
    }

    private void updateUserLocation(){
        LatLng pos = new LatLng(GlobalInfo.currentLocation.getLatitude(), GlobalInfo.currentLocation.getLongitude());
        if(userMarker == null){
            MarkerOptions userMarkerOptions = new MarkerOptions()
                    .draggable(false)
                    .alpha((float) 0.9)
                    .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(getContext(), R.drawable.ic_location_red)))
                    .position(pos);
            userMarker = (Marker) baiduMap.addOverlay(userMarkerOptions);
            Bundle extraInfo = new Bundle();
            extraInfo.putInt(BUNDLE_KEY_MARKER_TYPE, MARKER_TYPE_USER);
            userMarker.setExtraInfo(extraInfo);
        }else{
            userMarker.setPosition(pos);
        }
        if(!mapCenterFlag){
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(pos));
            mapCenterFlag = true;
        }
        if(cleanerLocationView.getVisibility() == View.VISIBLE || trashMonitorView.getVisibility() == View.VISIBLE)
            return;
        showUserLocationInfo(false);
    }

    private void showUserLocationInfo(boolean fromUser){
        if(GlobalInfo.currentLocation == null)
            return;
        cleanerLocationView.setVisibility(View.GONE);
        trashMonitorView.setVisibility(View.GONE);
        if(userLocationView.getVisibility() != View.VISIBLE || !fromUser){
            LatLng pos = new LatLng(GlobalInfo.currentLocation.getLatitude(), GlobalInfo.currentLocation.getLongitude());
            userLocationView.setVisibility(View.VISIBLE);
            txtUserUpdateTime.setText(DateTimeUtil.convertTimestamp(getContext(), GlobalInfo.currentLocation.getUpdateTime(), true, true, true));
            userLocationGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(pos));
        }
    }

    private void updateCleanerLocation(UserLocation newLoc){
        LatLng pos = new LatLng(newLoc.getLatitude(), newLoc.getLongitude());
        cleanerLocationMap.put(newLoc.getUserId(), newLoc);
        Marker marker = cleanerMarkerMap.get(newLoc.getUserId());
        if(marker == null){
            User u = GlobalInfo.findUserById(newLoc.getUserId());
            if(u == null)
                return;
            MarkerOptions markerOptions = new MarkerOptions()
                    .draggable(false)
                    .alpha((float) 0.9)
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            ImageUtil.getCroppedBitmap(u.getPortrait(), DIMEN_NORMAL_ICON)
                    ))
                    .position(pos);
            marker = (Marker) baiduMap.addOverlay(markerOptions);
            Bundle extraInfo = new Bundle();
            extraInfo.putInt(BUNDLE_KEY_MARKER_TYPE, MARKER_TYPE_CLEANER);
            extraInfo.putLong(BUNDLE_KEY_USER_ID, u.getUserId());
            marker.setExtraInfo(extraInfo);
            cleanerMarkerMap.put(u.getUserId(), marker);
        }
        if(!mapCenterFlag){
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(pos));
            mapCenterFlag = true;
        }
        if(cleanerLocationView.getVisibility() == View.VISIBLE && newLoc.getUserId() == currentShowCleanerId)
            showCleanerLocation(newLoc.getUserId(), false);
    }

    private void showCleanerLocation(long cleanerId, boolean fromUser){
        User u = GlobalInfo.findUserById(cleanerId);
        if(u == null)
            return;
        UserLocation loc = cleanerLocationMap.get(cleanerId);
        currentShowCleanerId = cleanerId;
        userLocationView.setVisibility(View.GONE);
        trashMonitorView.setVisibility(View.GONE);
        if(cleanerLocationView.getVisibility() != View.VISIBLE || !fromUser){
            cleanerLocationView.setVisibility(View.VISIBLE);
            cleanerPortrait.setImageBitmap(u.getPortrait());
            txtCleanerName.setText(u.getName());
            txtCleanerUpdateTime.setText(DateTimeUtil.convertTimestamp(getContext(), loc.getUpdateTime(), true, true, true));
            LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
            cleanerLocationGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(pos));
        }
    }

    private void showTrashInfo(long trashId){
        Trash t = GlobalInfo.findTrashById(trashId);
        if(t == null)
            return;
        currentShowTrashId = trashId;
        userLocationView.setVisibility(View.GONE);
        cleanerLocationView.setVisibility(View.GONE);
        trashMonitorView.setVisibility(View.VISIBLE);
        txtTrashName.setText(t.getTrashName(getContext()));
        txtTrashDesc.setText(t.getDescription());
        WorkRecord wr = trashWorkRecordMap.get(trashId);
        if(wr != null){
            final User u = GlobalInfo.findUserById(wr.getCleanerId());
            if(u == null)
                return;
            txtTrashCleanedTime.setText(DateTimeUtil.convertTimestamp(getContext(), wr.getRecordTime(), true, true, false));
            trashCleanerPortrait.setImageBitmap(u.getPortrait());
            trashCleanerPortrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterUserInfoActivity(u.getUserId());
                }
            });
        }else{
            getLatestWorkRecord(trashId);
        }

    }

    private void enterUserInfoActivity(long userId){
        Intent intent = new Intent(getContext(), UserInfoActivity.class);
        intent.putExtra(UserInfoActivity.BUNDLE_KEY_SHOW_CHATTING, true);
        intent.putExtra(UserInfoActivity.BUNDLE_KEY_USER_ID, userId);
        startActivity(intent);
    }

    private void enterTrashInfoActivity(long trashId){
        Intent intent = new Intent(getContext(), TrashInfoActivity.class);
        intent.putExtra(TrashInfoActivity.BUNDLE_KEY_TRASH_ID, trashId);
        startActivity(intent);
    }

    private void getLatestWorkRecord(final long trashId){
        String url = HttpApi.getApiUrl(HttpApi.WorkRecordApi.QUERY_RECORD_BY_TRASH, Long.toString(trashId), "" + 1);
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), url, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<WorkRecordListResult>(WorkRecordListResult.class) {
                    @Override
                    public void onResponse(WorkRecordListResult data) {
                        WorkRecord wr = trashWorkRecordMap.get(trashId);
                        if(wr != null && wr.getRecordTime().after(data.getWorkRecordList().get(0).getRecordTime()))
                            return;
                        trashWorkRecordMap.put(trashId, data.getWorkRecordList().get(0));
                        if(trashMonitorView.getVisibility() == View.VISIBLE && currentShowTrashId == trashId)
                            showTrashInfo(trashId);
                    }
                }));
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
            if(intent.getAction().equals(Application.ACTION_SELF_LOCATION))
                updateUserLocation();
            else if(intent.getAction().equals(Application.ACTION_CLEANER_LOCATION)){
                UserLocation newLoc = GsonUtil.getGson().fromJson(intent.getStringExtra(MqttService.BUNDLE_KEY_MESSAGE),
                        UserLocation.class);
                if(GlobalInfo.user.getUserId().equals(newLoc.getUserId()))
                    return;
                updateCleanerLocation(newLoc);
            }
        }
    }
}
