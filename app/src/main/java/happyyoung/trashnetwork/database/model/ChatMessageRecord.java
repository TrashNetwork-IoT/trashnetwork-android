package happyyoung.trashnetwork.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */

@Table(name = "ChatMessageRecord")
public class ChatMessageRecord extends Model implements Serializable, Comparable<ChatMessageRecord> {
    public static final char MESSAGE_TYPE_TEXT = 'T';

    @Column(name = "Session", notNull = true)
    private SessionRecord session;

    @Column(name = "SenderId", notNull = true)
    private long senderId;

    @Column(name = "MessageTime", notNull = true, uniqueGroups = {"Session", "SenderId", "MessageTime"},
            indexGroups = {"Session", "SenderId", "MessageTime"})
    @Expose
    private Date messageTime;

    @Column(name = "MessageType", notNull = true, length = 10)
    @Expose
    private char messageType;

    @Column(name = "Content", notNull = true)
    @Expose
    private String content;

    public ChatMessageRecord(){
        super();
    }

    public ChatMessageRecord(SessionRecord session, long senderId, char messageType, String content) {
        this(session, senderId, new Date(), messageType, content);
    }

    public ChatMessageRecord(SessionRecord session, long senderId, Date messageTime, char messageType, String content) {
        this.session = session;
        this.senderId = senderId;
        this.messageTime = messageTime;
        this.messageType = messageType;
        this.content = content;
    }

    public char getMessageType() {
        return messageType;
    }

    public void setMessageType(char messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SessionRecord getSession() {
        return session;
    }

    public void setSession(SessionRecord session) {
        this.session = session;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public Date getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(Date messageTime) {
        this.messageTime = messageTime;
    }

    public String getLiteralContent(){
        if(messageType == MESSAGE_TYPE_TEXT)
            return getContent();
        return null;
    }

    @Override
    public int compareTo(ChatMessageRecord o) {
        if(messageTime.before(o.messageTime))
            return 1;
        else if(messageTime.after(o.messageTime))
            return -1;
        return 0;
    }
}
