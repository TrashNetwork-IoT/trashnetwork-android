package happyyoung.trashnetwork.util;

import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

import happyyoung.trashnetwork.database.model.ChatMessageRecord;
import happyyoung.trashnetwork.database.model.LoginUserRecord;
import happyyoung.trashnetwork.database.model.SessionRecord;
import happyyoung.trashnetwork.model.User;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class DatabaseUtil {
    public static void updateLoginRecord(User user, String token){
        LoginUserRecord loginRecord = findLoginUserRecord(user.getUserId());
        if(loginRecord == null){
            loginRecord = new LoginUserRecord(user.getUserId(), token, user.getPortrait());
            loginRecord.save();
        }else{
            loginRecord.setLastLoginTime(new Date());
            loginRecord.setPortrait(user.getPortrait());
            loginRecord.setToken(token);
            loginRecord.save();
        }
    }

    public static List<LoginUserRecord> findAllLoginUserRecords(int limit){
        return new Select().from(LoginUserRecord.class)
                .orderBy("LastLoginTime DESC").limit(limit).execute();
    }

    public static LoginUserRecord findLoginUserRecord(long userId){
        return new Select().from(LoginUserRecord.class).where("UserId=?", userId)
                .executeSingle();
    }

    public static List<SessionRecord> findAllSessionRecords(long ownerUserId){
        return new Select().from(SessionRecord.class).where("OwnerUserId=?", ownerUserId)
                .execute();
    }

    public static SessionRecord findSessionRecord(long ownerUserId, char sessionType, long sessionId){
        return new Select().from(SessionRecord.class).where("OwnerUserId=?", ownerUserId)
                .where("SessionId=?", SessionRecord.getOriginalSessionId(sessionType, sessionId))
                .executeSingle();
    }

    public static SessionRecord findSessionRecordByDbId(long dbId){
        return new Select().from(SessionRecord.class).where("Id=?", dbId)
                .executeSingle();
    }

    public static ChatMessageRecord findChatMessageByDbId(long dbId){
        return new Select().from(ChatMessageRecord.class).where("Id=?", dbId)
                .executeSingle();
    }
}
