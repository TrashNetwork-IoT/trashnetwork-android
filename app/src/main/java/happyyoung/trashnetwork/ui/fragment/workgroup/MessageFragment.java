package happyyoung.trashnetwork.ui.fragment.workgroup;

import android.content.Context;
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

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.adapter.SessionMessageAdapter;
import happyyoung.trashnetwork.database.model.SessionRecord;
import happyyoung.trashnetwork.model.User;
import happyyoung.trashnetwork.util.DatabaseUtil;
import happyyoung.trashnetwork.util.GlobalInfo;

public class MessageFragment extends Fragment {
    private View viewRoot;
    private RecyclerView sessionListView;
    private LinkedList<SessionMessageAdapter.MessageItem> sessionList = new LinkedList<>();
    private SessionMessageAdapter adapter;

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
        sessionListView = (RecyclerView) viewRoot.findViewById(R.id.session_msg_list);
        sessionListView.setNestedScrollingEnabled(false);
        sessionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<SessionRecord> records = DatabaseUtil.findAllSessionRecords(GlobalInfo.user.getUserId());
        Collections.sort(records);
        for(SessionRecord sr : records) {
            if(sr.getSessionType() == SessionRecord.SESSION_TYPE_USER) {
                User u = GlobalInfo.findUserById(sr.getSessionId());
                if (u == null)
                    continue;
                sessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getName(), sr));
            }
        }
        adapter = new SessionMessageAdapter(getContext(), sessionList, new SessionMessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SessionMessageAdapter.MessageItem item) {

            }
        });
        sessionListView.setAdapter(adapter);
        if(!sessionList.isEmpty())
            viewRoot.findViewById(R.id.txt_no_message).setVisibility(View.GONE);
        return viewRoot;
    }

}
