package happyyoung.trashnetwork;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import happyyoung.trashnetwork.net.http.HttpApi;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class Application extends com.activeandroid.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            HttpApi.BASE_URL_V1 = appInfo.metaData.getString("TN_HTTP_API_BASE_URL_V1");
        }catch (PackageManager.NameNotFoundException nnfe){
            nnfe.printStackTrace();
        }
    }
}
