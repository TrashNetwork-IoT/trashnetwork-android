package happyyoung.trashnetwork.cleaning.util;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import happyyoung.trashnetwork.cleaning.database.model.SessionRecord;
import happyyoung.trashnetwork.cleaning.model.Group;
import happyyoung.trashnetwork.cleaning.model.Trash;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.model.UserLocation;
import happyyoung.trashnetwork.cleaning.service.LocationService;
import happyyoung.trashnetwork.cleaning.service.MqttService;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class GlobalInfo {
    public static String token;
    public static User user;
    public static List<User> groupWorkers = new ArrayList<>();
    public static List<Trash> trashList = new ArrayList<>();
    public static List<Group> groupList = new ArrayList<>();
    public static SessionRecord currentSession;
    public static SessionRecord notificationSession;
    public static UserLocation currentLocation;

    public static void logout(Context context){
        context.stopService(new Intent(context, MqttService.class));
        if(user.getAccountType() == User.ACCOUNT_TYPE_CLEANER){
            context.stopService(new Intent(context, LocationService.class));
        }
        token = null;
        user = null;
        currentLocation = null;
        currentSession = null;
        notificationSession = null;
        groupWorkers.clear();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
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

    public static Group findGroupById(long groupId){
        for(Group g : groupList){
            if(g.getGroupId() == groupId)
                return g;
        }
        return null;
    }
}
