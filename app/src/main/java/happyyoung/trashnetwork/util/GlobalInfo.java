package happyyoung.trashnetwork.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import happyyoung.trashnetwork.model.User;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class GlobalInfo {
    public static String token;
    public static User user;
    public static List<User> groupWorkers = new ArrayList<>();

    public static void logout(Context context){
        token = null;
        user = null;
        groupWorkers.clear();
    }
}
