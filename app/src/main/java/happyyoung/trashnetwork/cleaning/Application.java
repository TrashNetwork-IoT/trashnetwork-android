package happyyoung.trashnetwork.cleaning;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.baidu.mapapi.SDKInitializer;

import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.service.MqttService;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class Application extends com.activeandroid.app.Application {
    public static final String BUNDLE_KEY_CHAT_MSG_DB_ID = "MessageDBID";
    public static final String BUNDLE_KEY_SESSION_DB_ID = "SessionDBID";
    public static final String BUNDLE_KEY_USER_LOCATION_DATA = "UserLocationData";

    public static String ACTION_CHAT_MESSAGE_SENT;
    public static String ACTION_CHAT_MESSAGE_SENT_SAVED;
    public static String ACTION_CHAT_MESSAGE_RECEIVED;
    public static String ACTION_CHAT_MESSAGE_SEND_START;
    public static String ACTION_CHAT_MESSAGE_RECEIVED_SAVED;
    public static String ACTION_SESSION_UPDATE;
    public static String ACTION_LOCATION;

    public static String MQTT_TOPIC_CHATTING = "Chatting";

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            HttpApi.BASE_URL_V1 = appInfo.metaData.getString("TN_HTTP_API_BASE_URL_V1");
            MqttService.BROKER_URL = appInfo.metaData.getString("TN_MQTT_BROKER_URL");
        }catch (Exception e){
            e.printStackTrace();
        }

        ACTION_CHAT_MESSAGE_SENT = getPackageName() + ".action.CHAT_MESSAGE_SENT";
        ACTION_CHAT_MESSAGE_SENT_SAVED = getPackageName() + ".action.CHAT_MESSAGE_SENT_SAVED";
        ACTION_CHAT_MESSAGE_RECEIVED = getPackageName() + ".action.CHAT_MESSAGE_RECEIVED";
        ACTION_CHAT_MESSAGE_SEND_START = getPackageName() + ".action.CHAT_MESSAGE_SEND_START";
        ACTION_CHAT_MESSAGE_RECEIVED_SAVED = getPackageName() + ".action.CHAT_MESSAGE_RECEIVED_SAVED";
        ACTION_SESSION_UPDATE = getPackageName() + ".action.SESSION_UPDATE";
        ACTION_LOCATION = getPackageName() + ".action.LOCATION_DETECTED";
    }

    public static void checkPermission(Activity activity, String permission){
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return;
            }
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }
}
