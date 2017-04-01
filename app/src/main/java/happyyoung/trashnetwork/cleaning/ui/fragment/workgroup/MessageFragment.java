package happyyoung.trashnetwork.cleaning.ui.fragment.workgroup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.Application;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.SessionMessageAdapter;
import happyyoung.trashnetwork.cleaning.database.model.ChatMessageRecord;
import happyyoung.trashnetwork.cleaning.database.model.SessionRecord;
import happyyoung.trashnetwork.cleaning.model.Group;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.ui.activity.ChatActivity;
import happyyoung.trashnetwork.cleaning.util.DatabaseUtil;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class MessageFragment extends Fragment {
    private View viewRoot;
    @BindView(R.id.session_msg_list) RecyclerView sessionListView;

    private LinkedList<SessionMessageAdapter.MessageItem> sessionList = new LinkedList<>();
    private SessionMessageAdapter adapter;

    private MessageReceiver msgReceiver;
    private UpdateSessionReceiver sessionReceiver;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(Context context) {
        MessageFragment fragment = new MessageFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_message, container, false);
        ButterKnife.bind(this, viewRoot);

        sessionListView.setNestedScrollingEnabled(false);
        sessionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<SessionRecord> records = DatabaseUtil.findAllSessionRecords(GlobalInfo.user.getUserId());
        Collections.sort(records);
        Collections.reverse(records);
        adapter = new SessionMessageAdapter(getContext(), sessionList, new SessionMessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SessionMessageAdapter.MessageItem item) {
                switch (item.getSession().getSessionType()){
                    case SessionRecord.SESSION_TYPE_GROUP:
                    case SessionRecord.SESSION_TYPE_USER:
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_TYPE, item.getSession().getSessionType());
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_ID, item.getSession().getSessionId());
                        startActivity(intent);
                        break;
                }
            }
        });
        sessionListView.setAdapter(adapter);
        for(SessionRecord sr : records)
            addNewSession(sr);

        //Register receiver
        msgReceiver = new MessageReceiver();
        sessionReceiver = new UpdateSessionReceiver();
        IntentFilter filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_RECEIVED_SAVED);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(msgReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SEND_START);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(msgReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SENT_SAVED);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(msgReceiver, filter);
        filter = new IntentFilter(Application.ACTION_SESSION_UPDATE);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(sessionReceiver, filter);

        return viewRoot;
    }

    private void addNewSession(SessionRecord session){
        SessionMessageAdapter.MessageItem item = new SessionMessageAdapter.MessageItem();
        updateSessionInfo(item, session, true);
        if(item.getSession() == null)
            return;
        sessionList.addFirst(item);
        adapter.notifyItemInserted(0);
        ButterKnife.findById(viewRoot, R.id.txt_no_message).setVisibility(View.GONE);
    }

    private void updateSessionInfo(SessionMessageAdapter.MessageItem item, SessionRecord newSession, boolean fullUpdate){
        if(fullUpdate) {
            switch (newSession.getSessionType()) {
                case SessionRecord.SESSION_TYPE_USER:
                    User u = GlobalInfo.findUserById(newSession.getSessionId());
                    if (u == null)
                        return;
                    item.setPortrait(u.getPortrait());
                    item.setDisplayName(u.getName());
                    break;
                case SessionRecord.SESSION_TYPE_GROUP:
                    Group g = GlobalInfo.findGroupById(newSession.getSessionId());
                    if(g == null)
                        return;
                    item.setPortrait(g.getPortrait());
                    item.setDisplayName(g.getName());
                    break;
            }
        }
        item.setSession(newSession);
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(msgReceiver);
        getContext().unregisterReceiver(sessionReceiver);
        super.onDestroy();
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatMessageRecord cmr = DatabaseUtil.findChatMessageByDbId(intent.getLongExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, -1));
            if(cmr == null)
                return;
            for(SessionMessageAdapter.MessageItem mi : sessionList){
                if(mi.getSession().equals(cmr.getSession())){
                    updateSessionInfo(mi, cmr.getSession(), false);
                    Collections.sort(sessionList);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
            addNewSession(cmr.getSession());
        }
    }

    private class UpdateSessionReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            long dbId = intent.getLongExtra(Application.BUNDLE_KEY_SESSION_DB_ID, -1);
            SessionRecord session = DatabaseUtil.findSessionRecordByDbId(dbId);
            for(SessionMessageAdapter.MessageItem mi : sessionList){
                if(mi.getSession().getId() == dbId){
                    if(session == null){
                        int index = sessionList.indexOf(mi);
                        sessionList.remove(index);
                        adapter.notifyItemRemoved(index);
                        return;
                    }else{
                        updateSessionInfo(mi, session, true);
                        Collections.sort(sessionList);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
        }
    }
}
