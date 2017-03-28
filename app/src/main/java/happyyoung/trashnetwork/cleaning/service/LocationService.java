package happyyoung.trashnetwork.cleaning.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.Calendar;

import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.model.UserLocation;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.GsonUtil;

public class LocationService extends Service implements BDLocationListener {
    private final String TAG = "LocationService";
    private final int LOCATE_INTERVAL = 5000;
    private final int PUBLISH_INTERVAL = LOCATE_INTERVAL * 2;

    private LocationClient locationClient;
    private MqttService mqttService;
    private ServiceConnection mqttConn;
    private Calendar publishTime;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(this);
        publishTime = Calendar.getInstance();
        publishTime.setTimeInMillis(System.currentTimeMillis() - PUBLISH_INTERVAL);

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

        mqttConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mqttService = ((MqttService.Binder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mqttService = null;
            }
        };
        bindService(new Intent(this, MqttService.class), mqttConn, BIND_AUTO_CREATE);
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
        GlobalInfo.currentLocation = newLocation;
        Intent intent = new Intent(Application.ACTION_SELF_LOCATION);
        intent.addCategory(getPackageName());
        sendBroadcast(intent);

        if(mqttService == null || System.currentTimeMillis() - publishTime.getTimeInMillis() < PUBLISH_INTERVAL)
            return;
        mqttService.addMQTTAction(new MqttService.MqttPublishAction(Application.MQTT_TOPIC_CLEANER_LOCATION,
                MqttService.TOPIC_TYPE_PUBLIC, null, 0, GsonUtil.getGson().toJson(newLocation), null));
        publishTime.setTimeInMillis(System.currentTimeMillis());
    }

    @Override
    public void onDestroy() {
        locationClient.stop();
        unbindService(mqttConn);
        super.onDestroy();
    }
}
