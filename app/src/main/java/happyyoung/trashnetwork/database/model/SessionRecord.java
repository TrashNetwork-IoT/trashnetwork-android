package happyyoung.trashnetwork.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Collections;
import java.util.List;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */

@Table(name = "SessionRecord")
public class SessionRecord extends Model implements Comparable<SessionRecord> {
    public static final char SESSION_TYPE_UNKNOWN = 'U';
    public static final char SESSION_TYPE_GROUP = 'A';
    public static final char SESSION_TYPE_USER = 'B';

    @Column(name = "OwnerUserId", notNull = true)
    private long ownerUserId;

    @Column(name = "SessionId", notNull = true, uniqueGroups = {"OwnerUserId", "SessionId"},
            indexGroups = {"OwnerUserId", "SessionId"})
    private String originSessionId;

    @Column(name = "UnreadMessageCount")
    private int unreadMessageCount;

    public SessionRecord(){
        super();
    }

    public SessionRecord(int ownerUserId, char sessionType, long sessionId, int unreadMessageCount) {
        super();
        this.ownerUserId = ownerUserId;
        this.originSessionId = "" + sessionType + sessionId;
        this.unreadMessageCount = unreadMessageCount;
    }

    public SessionRecord(int ownerUserId, char sessionType, long sessionId) {
        this(ownerUserId, sessionType, sessionId, 0);
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public char getSessionType(){
        if(originSessionId == null)
            return SESSION_TYPE_UNKNOWN;
        if(originSessionId.startsWith("" + SESSION_TYPE_GROUP))
            return SESSION_TYPE_GROUP;
        else if(originSessionId.startsWith("" + SESSION_TYPE_USER))
            return SESSION_TYPE_USER;
        else
            return SESSION_TYPE_UNKNOWN;
    }

    public long getSessionId(){                 //Session ID represents group ID or user ID
        if(originSessionId == null)
            return -1;
        return Long.valueOf(originSessionId.substring(1));
    }

    public Integer getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(Integer unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public List<ChatMessageRecord> getAllMessages(){
        return getMany(ChatMessageRecord.class, "Session");
    }

    public ChatMessageRecord getLatestMessage(){
        return new Select().from(ChatMessageRecord.class).where("Session=", this)
                .orderBy("MessageType DESC").limit(1).executeSingle();
    }

    @Override
    public int compareTo(SessionRecord o) {
        return getLatestMessage().compareTo(o.getLatestMessage());
    }
}
