package happyyoung.trashnetwork;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushSettings;

import happyyoung.trashnetwork.net.http.HttpApi;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class Application extends com.activeandroid.app.Application {
    public static String AZURE_NOTIFICATION_HUB_CONN_NAME;
    public static String AZURE_NOTIFICATION_HUB_NAME;
    public static String BAIDU_PUSH_API_KEY;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            HttpApi.BASE_URL_V1 = appInfo.metaData.getString("TN_HTTP_API_BASE_URL_V1");
            BAIDU_PUSH_API_KEY = appInfo.metaData.getString("BAIDU_PUSH_API_KEY");
            AZURE_NOTIFICATION_HUB_NAME = appInfo.metaData.getString("AZURE_NOTIFICATION_HUB_NAME");
            AZURE_NOTIFICATION_HUB_CONN_NAME = appInfo.metaData.getString("AZURE_NOTIFICATION_HUB_CONN_NAME");

            PushSettings.enableDebugMode(getApplicationContext(), true);
            PushManager.startWork(getApplicationContext(),
                    PushConstants.LOGIN_TYPE_API_KEY, BAIDU_PUSH_API_KEY);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTerminate() {
        PushManager.stopWork(getApplicationContext());
    }
}
