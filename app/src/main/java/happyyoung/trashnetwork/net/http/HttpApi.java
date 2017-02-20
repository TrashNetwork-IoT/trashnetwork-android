package happyyoung.trashnetwork.net.http;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class HttpApi {
    private static final String BASE_URL = "http://happyyoung-pc.chinacloudapp.cn:23000/trashnetwork/v1/mobile";
    private static RequestQueue requestQueue = null;

    public static String getApiUrl(String... urlParam){
        String url = BASE_URL;
        for(String s : urlParam){
            if(s != null && !s.isEmpty())
                url += '/' + s;
        }
        return url;
    }

    public static void startRequest(HttpApiRequest req){
        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(req.getContext());
        requestQueue.add(req);
    }

    public static class AccountApi{
        public static final String LOGIN = "account/login";
        public static final String LOGOUT = "account/logout";
        public static final String CHECK_LOGIN = "account/check_login";
        public static final String USER_INFO_BY_ID = "account/user_info/by_id";
    }
}
