package happyyoung.trashnetwork.util;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import happyyoung.trashnetwork.database.model.SessionRecord;
import happyyoung.trashnetwork.model.Trash;
import happyyoung.trashnetwork.model.User;
import happyyoung.trashnetwork.model.UserLocation;
import happyyoung.trashnetwork.service.LocationService;
import happyyoung.trashnetwork.service.MqttService;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class GlobalInfo {
    public static String token;
    public static User user;
    public static List<User> groupWorkers = new ArrayList<>();
    public static List<Trash> trashList = new ArrayList<>();
    public static SessionRecord currentSession;
    public static UserLocation currentLocation;

    public static void logout(Context context){
        Intent mqttIntent = new Intent(context, MqttService.class);
        context.stopService(mqttIntent);
        if(user.getAccountType() == User.ACCOUNT_TYPE_CLEANER){
            Intent locationIntent = new Intent(context, LocationService.class);
            context.stopService(locationIntent);
        }
        token = null;
        user = null;
        currentLocation = null;
        groupWorkers.clear();
    }

    public static User findUserById(long userId){
        if(userId == user.getUserId())
            return user;
        for(User u : groupWorkers){
            if(u.getUserId() == userId)
                return u;
        }
        return null;
    }

    public static Trash findTrashById(long trashId){
        for(Trash t : trashList){
            if(t.getTrashId().equals(trashId))
                return t;
        }
        return null;
    }
}
