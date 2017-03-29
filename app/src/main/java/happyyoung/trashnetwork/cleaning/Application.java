package happyyoung.trashnetwork.cleaning;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.baidu.mapapi.SDKInitializer;

import java.util.Random;

import happyyoung.trashnetwork.cleaning.net.http.HttpApi;
import happyyoung.trashnetwork.cleaning.service.MqttService;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class Application extends com.activeandroid.app.Application {
    public static final String BUNDLE_KEY_CHAT_MSG_DB_ID = "MessageDBID";
    public static final String BUNDLE_KEY_SESSION_DB_ID = "SessionDBID";

    public static String ACTION_CHAT_MESSAGE_SENT;
    public static String ACTION_CHAT_MESSAGE_SENT_SAVED;
    public static String ACTION_CHAT_MESSAGE_RECEIVED;
    public static String ACTION_CHAT_MESSAGE_SEND_START;
    public static String ACTION_CHAT_MESSAGE_RECEIVED_SAVED;
    public static String ACTION_SESSION_UPDATE;
    public static String ACTION_SELF_LOCATION;
    public static String ACTION_CLEANER_LOCATION;
    public static String ACTION_CLEAN_REMINDER;
    public static String ACTION_LATEST_WORK_RECORD;

    public static String MQTT_TOPIC_CHATTING = "Chatting";
    public static String MQTT_TOPIC_CLEANER_LOCATION = "CleanerLocation";
    public static String MQTT_TOPIC_LATEST_WORK_RECORD = "LatestWorkRecord";
    public static String MQTT_TOPIC_CLEAN_REMINDER = "CleanReminder";

    public static int RANDOM_COLOR[];
    private static Random colorRandom;

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
        ACTION_SELF_LOCATION = getPackageName() + ".action.SELF_LOCATION";
        ACTION_CLEANER_LOCATION = getPackageName() + ".action.CLEANER_LOCATION";
        ACTION_CLEAN_REMINDER = getPackageName() + ".action.CLEAN_REMINDER";
        ACTION_LATEST_WORK_RECORD = getPackageName() + ".action.LATEST_WORK_RECORD";

        RANDOM_COLOR = new int[]{
                getResources().getColor(R.color.red_500),
                getResources().getColor(R.color.grey_500),
                getResources().getColor(R.color.green_500),
                getResources().getColor(R.color.teal_500),
                getResources().getColor(R.color.cyan_500),
                getResources().getColor(R.color.orange_500),
                getResources().getColor(R.color.light_blue_500),
                getResources().getColor(R.color.blue_500),
                getResources().getColor(R.color.light_green_500),
                getResources().getColor(R.color.amber_500),
                getResources().getColor(R.color.indigo_500),
                getResources().getColor(R.color.pink_500),
        };
        colorRandom = new Random(System.currentTimeMillis());
    }

    public static int getRandomColor(){
        return RANDOM_COLOR[colorRandom.nextInt(RANDOM_COLOR.length)];
    }

    public static int generateColorFromStr(String str){
        int sum = 0;
        if(str != null && !str.isEmpty()){
            for(int i = 0; i < str.length(); i++)
                sum += (int)str.charAt(i);
        }
        return RANDOM_COLOR[sum % RANDOM_COLOR.length];
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
