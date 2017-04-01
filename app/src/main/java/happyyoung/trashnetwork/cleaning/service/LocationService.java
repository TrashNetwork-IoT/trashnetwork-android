package happyyoung.trashnetwork.cleaning.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.Calendar;
import java.util.Date;

import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.model.UserLocation;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.GsonUtil;

public class LocationService extends Service implements AMapLocationListener {
    private final String TAG = "LocationService";
    private final int LOCATE_INTERVAL = 5000;
    private final int PUBLISH_INTERVAL = LOCATE_INTERVAL * 2;

    private AMapLocationClient locationClient;
    private MqttService mqttService;
    private ServiceConnection mqttConn;
    private Calendar publishTime;

    private long lastLocTime = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new AMapLocationClient(getApplicationContext());
        locationClient.setLocationListener(this);
        publishTime = Calendar.getInstance();
        publishTime.setTimeInMillis(System.currentTimeMillis() - PUBLISH_INTERVAL);

        AMapLocationClientOption opt = new AMapLocationClientOption()
                .setInterval(LOCATE_INTERVAL)
                .setKillProcess(true)
                .setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
                .setNeedAddress(true);
        opt.setWifiActiveScan(true);
        opt.setMockEnable(false);
        opt.setHttpTimeOut(15000);
        opt.setLocationCacheEnable(true);
        locationClient.setLocationOption(opt);
        locationClient.startLocation();

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
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(aMapLocation == null)
            return;
        if (aMapLocation.getErrorCode() == 0) {
            if(GlobalInfo.user == null)
                return;
            if(aMapLocation.getTime() < lastLocTime)
                return;
            UserLocation newLoc = new UserLocation(GlobalInfo.user.getUserId(), aMapLocation.getLongitude(),
                    aMapLocation.getLatitude(), new Date(), aMapLocation.getAddress());
            sendLocation(newLoc);
        }else {
            Log.e(TAG, "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:"
                    + aMapLocation.getErrorInfo());
        }
    }

    private void sendLocation(UserLocation newLocation) {
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
        locationClient.stopLocation();
        locationClient.onDestroy();
        unbindService(mqttConn);
        super.onDestroy();
    }
}
