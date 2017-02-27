package happyyoung.trashnetwork.ui.fragment.workgroup;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import happyyoung.trashnetwork.R;
import happyyoung.trashnetwork.adapter.ContactListContactHolder;
import happyyoung.trashnetwork.adapter.ContactListRootHolder;
import happyyoung.trashnetwork.listener.ContactListener;
import happyyoung.trashnetwork.model.User;
import happyyoung.trashnetwork.util.GlobalInfo;

public class ContactFragment extends Fragment implements ContactListener {
    private View rootView;

    private TreeNode treeRoot;
    private TreeNode cleanerListRoot;
    private TreeNode managerListRoot;
    private TreeNode groupListRoot;

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
        return rootView;
    }

    @Override
    public void onAddContact(User newContact) {
        if(rootView == null)
            return;
        if(newContact.getAccountType() == User.ACCOUNT_TYPE_CLEANER) {
            cleanerListRoot.addChild(new TreeNode(new ContactListContactHolder.IconTextItem(newContact.getPortrait(), newContact.getName()))
                    .setViewHolder(new ContactListContactHolder(getContext(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO
                        }
                    })));
            ((ContactListRootHolder)cleanerListRoot.getViewHolder()).updateNodeView(
                    new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts, getString(R.string.cleaners), cleanerListRoot.getChildren().size()));
        }else if(newContact.getAccountType() == User.ACCOUNT_TYPE_MANAGER){
            managerListRoot.addChild(new TreeNode(new ContactListContactHolder.IconTextItem(newContact.getPortrait(), newContact.getName()))
                    .setViewHolder(new ContactListContactHolder(getContext(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO
                        }
                    })));
            ((ContactListRootHolder)managerListRoot.getViewHolder()).updateNodeView(
                    new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts, getString(R.string.managers), managerListRoot.getChildren().size()));
        }
    }
}
