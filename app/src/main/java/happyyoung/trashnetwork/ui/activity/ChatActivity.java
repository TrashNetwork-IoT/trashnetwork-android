package happyyoung.trashnetwork.ui.activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.adapter.ChatMessageAdapter;
import happyyoung.trashnetwork.database.model.ChatMessageRecord;
import happyyoung.trashnetwork.database.model.SessionRecord;
import happyyoung.trashnetwork.util.DatabaseUtil;
import happyyoung.trashnetwork.util.GlobalInfo;

public class ChatActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_SESSION_ID = "SessionId";
    public static final String BUNDLE_KEY_SESSION_TYPE = "SessionType";

    private SwipeRefreshLayout refresh;
    private RecyclerView chatListView;
    private EditText editChatMsg;
    private LinkedList<Object> messageList = new LinkedList<>();
    private ChatMessageAdapter adapter;
    private Calendar endTime;

    private SessionRecord session;
    private boolean newSessionFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endTime = Calendar.getInstance();

        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editChatMsg = (EditText) findViewById(R.id.chat_msg_edit);
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateChatHistory();
            }
        });

        chatListView = (RecyclerView) findViewById(R.id.chat_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatListView.setLayoutManager(layoutManager);
        chatListView.setNestedScrollingEnabled(false);
        ImageButton btnSendMessage = (ImageButton) findViewById(R.id.btn_send_msg);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });

        char sessionType = getIntent().getCharExtra(BUNDLE_KEY_SESSION_TYPE, SessionRecord.SESSION_TYPE_UNKNOWN);
        long sessionId = getIntent().getLongExtra(BUNDLE_KEY_SESSION_ID, -1);
        session = DatabaseUtil.findSessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId);
        if(session == null) {
            newSessionFlag = true;
            session = new SessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId);
        }

        if(sessionType == SessionRecord.SESSION_TYPE_GROUP) {
            adapter = new ChatMessageAdapter(this, messageList, true);
        }else {
            adapter = new ChatMessageAdapter(this, messageList, false);
            setTitle(GlobalInfo.findUserById(sessionId).getName());
        }
        chatListView.setAdapter(adapter);
        updateChatHistory();
    }

    private void updateChatHistory(){
        List<ChatMessageRecord> records = session.getMessages(endTime.getTime(), 20);
        if(records != null){
            for(ChatMessageRecord cmr : records){
                endTime.setTimeInMillis(cmr.getMessageTime().getTime() - 1);
                if(GlobalInfo.user.getUserId() == cmr.getSenderId()){
                    messageList.addFirst(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_END,
                            GlobalInfo.user, cmr));
                }else{
                    messageList.addFirst(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_START,
                            GlobalInfo.findUserById(cmr.getSenderId()), cmr));
                }
                adapter.notifyItemInserted(0);
            }
            if(!records.isEmpty())
                chatListView.scrollToPosition(records.size() - 1);
        }
        refresh.setRefreshing(false);
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
        messageList.addLast(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_END, GlobalInfo.user, newMessage, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        chatListView.smoothScrollToPosition(messageList.size() - 1);

        //TODO
    }
}
