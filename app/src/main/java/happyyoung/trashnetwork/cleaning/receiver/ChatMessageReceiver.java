package happyyoung.trashnetwork.cleaning.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.database.model.ChatMessageRecord;
import happyyoung.trashnetwork.cleaning.database.model.SessionRecord;
import happyyoung.trashnetwork.cleaning.service.MqttService;
import happyyoung.trashnetwork.cleaning.util.DatabaseUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.GsonUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-03
 */
public class ChatMessageReceiver extends BroadcastReceiver {

    private Gson gson = GsonUtil.getDefaultGsonBuilder()
            .excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public void onReceive(Context context, Intent mqttIntent) {
        if(mqttIntent.getAction().equals(Application.ACTION_CHAT_MESSAGE_RECEIVED)) {
            ChatMessageRecord cmr = gson.fromJson(mqttIntent.getStringExtra(MqttService.BUNDLE_KEY_MESSAGE),
                    ChatMessageRecord.class);
            if (!checkReceivedChatMessage(mqttIntent, cmr))
                return;

            long sessionId = -1;
            char sessionType = cmr.getSession().getSessionType();
            switch (sessionType){
                case SessionRecord.SESSION_TYPE_USER:
                    sessionId = cmr.getSenderId();
                    break;
                case SessionRecord.SESSION_TYPE_GROUP:
                    sessionId = cmr.getSession().getSessionId();
                    break;
            }

            SessionRecord session = DatabaseUtil.findSessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId);
            if (session == null)
                session = new SessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId, 0);
            session.setUnreadMessageCount(session.getUnreadMessageCount() + 1);
            ChatMessageRecord newMessage = new ChatMessageRecord(session, cmr.getSenderId(), cmr.getMessageTime(), cmr.getMessageType(),
                    cmr.getStrContent(), ChatMessageRecord.MESSAGE_STATUS_NORMAL);
            Intent intent = new Intent(Application.ACTION_CHAT_MESSAGE_RECEIVED_SAVED);
            intent.addCategory(context.getPackageName());
            try {
                session.save();
                intent.putExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, newMessage.save());
                context.sendBroadcast(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(mqttIntent.getAction().equals(Application.ACTION_CHAT_MESSAGE_SENT)){
            ChatMessageRecord cmr = DatabaseUtil.findChatMessageByDbId(mqttIntent.getLongExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, -1));
            if(cmr != null){
                cmr.setStatus(ChatMessageRecord.MESSAGE_STATUS_NORMAL);
                cmr.save();
                Intent intent = new Intent(Application.ACTION_CHAT_MESSAGE_SENT_SAVED);
                intent.addCategory(context.getPackageName());
                intent.putExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, cmr.getId());
                context.sendBroadcast(intent);
            }
        }
    }

    private static boolean checkReceivedChatMessage(Intent mqttIntent, ChatMessageRecord cmr){
        if(GlobalInfo.user == null)
            return false;
        if(cmr.getSenderId() == GlobalInfo.user.getUserId())
            return false;
        switch (cmr.getSession().getSessionType()){
            case SessionRecord.SESSION_TYPE_USER:
                if(GlobalInfo.findUserById(cmr.getSenderId()) == null)
                    return false;
                if(mqttIntent.getIntExtra(MqttService.BUNDLE_KEY_TOPIC_TYPE, -1) != MqttService.TOPIC_TYPE_PRIVATE)
                    return false;
                if(mqttIntent.getLongExtra(MqttService.BUNDLE_KEY_TOPIC_OWNER_ID, -1) != GlobalInfo.user.getUserId())
                    return false;
                break;
        }
        return true;
    }
}
