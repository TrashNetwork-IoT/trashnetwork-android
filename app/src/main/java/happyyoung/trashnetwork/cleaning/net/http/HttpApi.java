package happyyoung.trashnetwork.cleaning.net.http;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */
public class HttpApi {
    public static String BASE_URL_V1;
    private static RequestQueue requestQueue = null;

    public static String getApiUrl(String... urlParam){
        String url = BASE_URL_V1;
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
        public static final String LOGIN = "cleaning/account/login";
        public static final String LOGOUT = "cleaning/account/logout";
        public static final String CHECK_LOGIN = "cleaning/account/check_login";
        public static final String USER_INFO_BY_ID = "cleaning/account/user_info/by_id";
        public static final String ALL_GROUP_USERS = "cleaning/account/all_group_users";
    }

    public static class GroupApi{
        public static final String ALL_GROUPS = "cleaning/group/all_groups";
        public static final String QUERY_BULLETIN = "cleaning/group/bulletin";
        public static final String POST_BULLETIN = "cleaning/group/new_bulletin";
    }

    public static class WorkRecordApi{
        public static final String QUERY_RECORD = "cleaning/work/record";
        public static final String QUERY_RECORD_BY_USER = "cleaning/work/record/by_user";
        public static final String QUERY_RECORD_BY_TRASH = "cleaning/work/record/by_trash";
        public static final String POST_RECORD = "cleaning/work/new_record";
    }

    public static class FeedbackApi{
        public static final String QUERY_FEEDBACK = "public/feedback/feedbacks";
    }
}
