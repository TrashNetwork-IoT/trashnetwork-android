package happyyoung.trashnetwork.cleaning.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.ChatMessageAdapter;
import happyyoung.trashnetwork.cleaning.database.model.ChatMessageRecord;
import happyyoung.trashnetwork.cleaning.database.model.SessionRecord;
import happyyoung.trashnetwork.cleaning.model.Group;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.receiver.ChatMessageReceiver;
import happyyoung.trashnetwork.cleaning.service.MqttService;
import happyyoung.trashnetwork.cleaning.util.DatabaseUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;
import happyyoung.trashnetwork.cleaning.util.GsonUtil;

public class ChatActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_SESSION_ID = "SessionId";
    public static final String BUNDLE_KEY_SESSION_TYPE = "SessionType";

    @BindView(R.id.toolbar_portrait) ImageView toolbarPortrait;
    @BindView(R.id.txt_toolbar_contact_name) TextView txtToolbarContactName;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refresh;
    @BindView(R.id.chat_list) RecyclerView chatListView;
    @BindView(R.id.chat_msg_edit) EditText editChatMsg;
    @BindView(R.id.btn_send_msg) ImageButton btnSendMessage;

    private LinkedList<Object> messageList = new LinkedList<>();
    private ChatMessageAdapter adapter;
    private ChatMessageAdapter.MessageItem.OnSenderClickListener onSenderClickListener;
    private Calendar endTime;

    private SessionRecord session;
    private boolean newSessionFlag = false;

    private Gson gson = GsonUtil.getDefaultGsonBuilder()
            .excludeFieldsWithoutExposeAnnotation().create();
    private MqttService mqttService;
    private ServiceConnection mqttServConn;

    private SentMessageReceiver sentMessageReceiver;
    private ReceivedMessageReceiver receivedMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endTime = Calendar.getInstance();

        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        setSupportActionBar((Toolbar) ButterKnife.findById(this, R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateChatHistory();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatListView.setLayoutManager(layoutManager);
        chatListView.setNestedScrollingEnabled(false);
        btnSendMessage.setEnabled(false);

        char sessionType = getIntent().getCharExtra(BUNDLE_KEY_SESSION_TYPE, SessionRecord.SESSION_TYPE_UNKNOWN);
        long sessionId = getIntent().getLongExtra(BUNDLE_KEY_SESSION_ID, -1);
        session = DatabaseUtil.findSessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId);
        if(session == null) {
            newSessionFlag = true;
            session = new SessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId);
        }

        if(sessionType == SessionRecord.SESSION_TYPE_GROUP) {
            adapter = new ChatMessageAdapter(this, messageList, true);
            Group g = GlobalInfo.findGroupById(sessionId);
            toolbarPortrait.setImageBitmap(g.getPortrait());
            txtToolbarContactName.setText(g.getName());
        }else {
            adapter = new ChatMessageAdapter(this, messageList, false);
            User u = GlobalInfo.findUserById(sessionId);
            toolbarPortrait.setImageBitmap(u.getPortrait());
            txtToolbarContactName.setText(u.getName());
        }
        onSenderClickListener = new ChatMessageAdapter.MessageItem.OnSenderClickListener() {
            @Override
            public void onClick(User sender) {
                Intent intent = new Intent(ChatActivity.this, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_USER_ID, sender.getUserId());
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_SHOW_CHATTING, session.getSessionType() == SessionRecord.SESSION_TYPE_GROUP);
                startActivity(intent);
            }
        };
        chatListView.setAdapter(adapter);
        updateChatHistory();

        if(session.equals(GlobalInfo.notificationSession)){
            NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(ChatMessageReceiver.CHAT_MSG_NOTIFICATION_ID);
            GlobalInfo.notificationSession = null;
        }

        mqttServConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mqttService = ((MqttService.Binder) service).getService();
                btnSendMessage.setEnabled(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        Intent mqttIntent = new Intent(this, MqttService.class);
        bindService(mqttIntent, mqttServConn, BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SENT_SAVED);
        filter.addCategory(getPackageName());
        sentMessageReceiver = new SentMessageReceiver();
        registerReceiver(sentMessageReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_RECEIVED_SAVED);
        filter.addCategory(getPackageName());
        receivedMessageReceiver = new ReceivedMessageReceiver();
        registerReceiver(receivedMessageReceiver, filter);
    }

    @OnClick(R.id.btn_send_msg)
    void onBtnSendMsgClick(View v){
        sendTextMessage();
    }

    @OnClick(R.id.toolbar_contact_view)
    void onToolbarContactViewClick(View v){
        Intent intent;
        switch (session.getSessionType()){
            case SessionRecord.SESSION_TYPE_USER:
                intent = new Intent(this, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_USER_ID, session.getSessionId());
                intent.putExtra(UserInfoActivity.BUNDLE_KEY_SHOW_CHATTING, false);
                startActivity(intent);
                break;
            case SessionRecord.SESSION_TYPE_GROUP:
                intent = new Intent(this, GroupInfoActivity.class);
                intent.putExtra(GroupInfoActivity.BUNDLE_KEY_GROUP_ID, session.getSessionId());
                startActivity(intent);
                break;
        }
    }

    private void updateChatHistory(){
        if(newSessionFlag)
            return;
        List<ChatMessageRecord> records = session.getMessages(endTime.getTime(), 20);
        if(records != null){
            for(ChatMessageRecord cmr : records){
                endTime.setTimeInMillis(cmr.getMessageTime().getTime() - 1);
                if(GlobalInfo.user.getUserId() == cmr.getSenderId()){
                    messageList.addFirst(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_END,
                            GlobalInfo.user, cmr, null));
                }else{
                    messageList.addFirst(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_START,
                            GlobalInfo.findUserById(cmr.getSenderId()), cmr, onSenderClickListener));
                }
                adapter.notifyItemInserted(0);
            }
            if(!records.isEmpty())
                chatListView.scrollToPosition(records.size() - 1);
        }
        refresh.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalInfo.currentSession = session;
    }

    @Override
    protected void onPause() {
        GlobalInfo.currentSession = null;
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendTextMessage(){
        String message = editChatMsg.getText().toString();
        if(message.isEmpty())
            return;
        ChatMessageRecord newMessage = new ChatMessageRecord(session, GlobalInfo.user.getUserId(), ChatMessageRecord.MESSAGE_TYPE_TEXT, message);
        sendMessage(newMessage);
        editChatMsg.setText("");
    }

    private void sendMessage(ChatMessageRecord newMessage){
        messageList.addLast(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_END, GlobalInfo.user, newMessage, null));
        adapter.notifyItemInserted(messageList.size() - 1);
        chatListView.smoothScrollToPosition(messageList.size() - 1);

        if(newSessionFlag) {
            updateSession();
            newMessage.setSession(session);
        }
        newMessage.save();
        Intent sentIntent = new Intent(Application.ACTION_CHAT_MESSAGE_SENT);
        sentIntent.addCategory(getPackageName());
        sentIntent.putExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, newMessage.getId());
        switch (session.getSessionType()){
            case SessionRecord.SESSION_TYPE_USER:
                mqttService.addMQTTAction(new MqttService.MqttPublishAction(Application.MQTT_TOPIC_CHATTING_PRIVATE, session.getSessionId(),
                        1, gson.toJson(newMessage), sentIntent));
                break;
            case SessionRecord.SESSION_TYPE_GROUP:
                mqttService.addMQTTAction(new MqttService.MqttPublishAction(Application.MQTT_TOPIC_CHATTING_GROUP, session.getSessionId(),
                        1, gson.toJson(newMessage), sentIntent));
                break;
        }

        Intent sendStartIntent = new Intent(Application.ACTION_CHAT_MESSAGE_SEND_START);
        sendStartIntent.addCategory(getPackageName());
        sendStartIntent.putExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, newMessage.getId());
        sendBroadcast(sendStartIntent);
    }

    private void updateSession(){
        SessionRecord tempSession = DatabaseUtil.findSessionRecord(session.getOwnerUserId(), session.getSessionType(),
                session.getSessionId());
        if (tempSession != null)
            session = tempSession;
        else
            session.save();
        newSessionFlag = false;
    }

    @Override
    protected void onDestroy() {
        unbindService(mqttServConn);
        mqttService = null;
        unregisterReceiver(sentMessageReceiver);
        unregisterReceiver(receivedMessageReceiver);
        if(!newSessionFlag){
            session.setUnreadMessageCount(0);
            Intent intent = new Intent(Application.ACTION_SESSION_UPDATE);
            intent.addCategory(getPackageName());
            intent.putExtra(Application.BUNDLE_KEY_SESSION_DB_ID, session.save());
            sendBroadcast(intent);
        }
        super.onDestroy();
    }

    private class SentMessageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            long msgDbId = intent.getLongExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, -1);
            for(Object o : messageList){
                if(o instanceof ChatMessageAdapter.MessageItem){
                    if(msgDbId == ((ChatMessageAdapter.MessageItem) o).getMessage().getId()){
                        ((ChatMessageAdapter.MessageItem) o).getMessage().setStatus(ChatMessageRecord.MESSAGE_STATUS_NORMAL);
                        adapter.notifyItemChanged(messageList.indexOf(o));
                    }
                }
            }
        }
    }

    private class ReceivedMessageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatMessageRecord cmr = DatabaseUtil.findChatMessageByDbId(intent.getLongExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, -1));
            if(cmr == null)
                return;
            if(!cmr.getSession().equals(session))
                return;
            if(newSessionFlag)
                updateSession();
            boolean scrollFlag = ((LinearLayoutManager)chatListView.getLayoutManager()).findLastVisibleItemPosition() > messageList.size() - 3;
            messageList.addLast(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_START, GlobalInfo.findUserById(cmr.getSenderId()), cmr, onSenderClickListener));
            adapter.notifyItemInserted(messageList.size() - 1);
            if(scrollFlag)
                chatListView.smoothScrollToPosition(messageList.size() - 1);
        }
    }

}
