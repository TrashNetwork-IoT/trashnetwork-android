package happyyoung.trashnetwork.cleaning.listener;

import happyyoung.trashnetwork.cleaning.model.Group;
import happyyoung.trashnetwork.cleaning.model.User;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */
public interface ContactListener {
    void onAddContact(User newContact);
    void onAddGroup(Group newGroup);
}
