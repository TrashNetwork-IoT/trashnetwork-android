package happyyoung.trashnetwork.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.Date;

import happyyoung.trashnetwork.Application;
import happyyoung.trashnetwork.model.UserLocation;
import happyyoung.trashnetwork.util.GlobalInfo;
import happyyoung.trashnetwork.util.GsonUtil;

public class LocationService extends Service implements BDLocationListener {
    private final String TAG = "LocationService";
    private final int LOCATE_INTERVAL = 5000;

    public LocationClient locationClient = null;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(this);

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(LOCATE_INTERVAL);
        option.setOpenGps(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(true);
        option.setEnableSimulateGps(false);
        locationClient.setLocOption(option);
        locationClient.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if(bdLocation.getLocType() == BDLocation.TypeServerError) {
            Log.e(TAG, "Locate error due to server error");
        }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkException){
            Log.e(TAG, "Locate error due to network exception");
        }else if(bdLocation.getLocType() == BDLocation.TypeCriteriaException){
            Log.e(TAG, "Locate error due to criteria exception");
        }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation ||
                 bdLocation.getLocType() == BDLocation.TypeGpsLocation){
            sendLocation(bdLocation.getLongitude(), bdLocation.getLatitude());
        }
    }

    @Override
    public void onConnectHotSpotMessage(String s, int i) {}

    private void sendLocation(double longitude, double latitude) {
        if(GlobalInfo.user == null)
            return;
        UserLocation newLocation = new UserLocation(GlobalInfo.user.getUserId(), longitude, latitude);
        Intent intent = new Intent(Application.ACTION_LOCATION);
        intent.addCategory(getPackageName());
        intent.putExtra(Application.BUNDLE_KEY_USER_LOCATION_DATA, GsonUtil.getGson().toJson(newLocation));
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        locationClient.stop();
        super.onDestroy();
    }
}
