package happyyoung.trashnetwork.cleaning.ui.fragment.workgroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import happyyoung.trashnetwork.cleaning.R;
import happyyoung.trashnetwork.cleaning.adapter.ContactListContactHolder;
import happyyoung.trashnetwork.cleaning.adapter.ContactListRootHolder;
import happyyoung.trashnetwork.cleaning.database.model.SessionRecord;
import happyyoung.trashnetwork.cleaning.listener.ContactListener;
import happyyoung.trashnetwork.cleaning.model.Group;
import happyyoung.trashnetwork.cleaning.model.User;
import happyyoung.trashnetwork.cleaning.ui.activity.ChatActivity;
import happyyoung.trashnetwork.cleaning.util.GlobalInfo;

public class ContactFragment extends Fragment implements ContactListener {
    private View rootView;

    private TreeNode treeRoot;
    private TreeNode cleanerListRoot;
    private TreeNode managerListRoot;
    private TreeNode groupListRoot;

    private Map<Long, Boolean> userIdUsedMap = new HashMap<>();
    private Map<Long, Boolean> groupIdUsedMap = new HashMap<>();

    public ContactFragment() {
        // Required empty public constructor
    }

    public static ContactFragment newInstance(Context context) {
        ContactFragment fragment = new ContactFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.bind(this, rootView);
        treeRoot = TreeNode.root();
        AndroidTreeView atv = new AndroidTreeView(getActivity(), treeRoot);
        atv.setDefaultAnimation(true);
        groupListRoot = new TreeNode(new ContactListRootHolder.IconTextItem(R.drawable.ic_chat, getString(R.string.groups), 0))
                            .setViewHolder(new ContactListRootHolder(getContext(), atv));
        cleanerListRoot = new TreeNode(new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts, getString(R.string.cleaners), 0))
                            .setViewHolder(new ContactListRootHolder(getContext(), atv));
        managerListRoot = new TreeNode(new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts, getString(R.string.managers), 0))
                            .setViewHolder(new ContactListRootHolder(getContext(), atv));
        treeRoot.addChildren(groupListRoot, managerListRoot, cleanerListRoot);
        ((ViewGroup)rootView.findViewById(R.id.contacts_list_container)).addView(atv.getView());

        for(User u : GlobalInfo.groupWorkers)
            onAddContact(u);
        for(Group g : GlobalInfo.groupList)
            onAddGroup(g);
        return rootView;
    }

    @Override
    public void onAddContact(final User newContact) {
        if(rootView == null)
            return;
        if(userIdUsedMap.containsKey(newContact.getUserId()))
            return;
        userIdUsedMap.put(newContact.getUserId(), true);
        if(newContact.getAccountType() == User.ACCOUNT_TYPE_CLEANER) {
            cleanerListRoot.addChild(new TreeNode(new ContactListContactHolder.IconTextItem(newContact.getPortrait(), newContact.getName()))
                    .setViewHolder(new ContactListContactHolder(getContext(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enterChatting(SessionRecord.SESSION_TYPE_USER, newContact.getUserId());
                        }
                    })));
            ((ContactListRootHolder)cleanerListRoot.getViewHolder()).updateNodeView(
                    new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts, getString(R.string.cleaners), cleanerListRoot.getChildren().size()));
        }else if(newContact.getAccountType() == User.ACCOUNT_TYPE_MANAGER){
            managerListRoot.addChild(new TreeNode(new ContactListContactHolder.IconTextItem(newContact.getPortrait(), newContact.getName()))
                    .setViewHolder(new ContactListContactHolder(getContext(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enterChatting(SessionRecord.SESSION_TYPE_USER, newContact.getUserId());
                        }
                    })));
            ((ContactListRootHolder)managerListRoot.getViewHolder()).updateNodeView(
                    new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts, getString(R.string.managers), managerListRoot.getChildren().size()));
        }
    }

    @Override
    public void onAddGroup(final Group newGroup) {
        if(rootView == null)
            return;
        if(groupIdUsedMap.containsKey(newGroup.getGroupId()))
            return;
        userIdUsedMap.put(newGroup.getGroupId(), true);
        groupListRoot.addChild(new TreeNode(new TreeNode(new ContactListContactHolder.IconTextItem(newGroup.getPortrait(), newGroup.getName()))
                .setViewHolder(new ContactListContactHolder(getContext(), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enterChatting(SessionRecord.SESSION_TYPE_GROUP, newGroup.getGroupId());
                    }
                }))));
    }

    private void enterChatting(char sessionType, long sessionId){
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_TYPE, sessionType);
        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_ID, sessionId);
        startActivity(intent);
    }
}
