package happyyoung.trashnetwork.cleaning.net.model.result;

import java.util.List;

import happyyoung.trashnetwork.cleaning.model.Group;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-24
 */
public class GroupListResult extends Result {
    private List<Group> groupList;

    public GroupListResult(int resultCode, String message, List<Group> groupList) {
        super(resultCode, message);
        this.groupList = groupList;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }
}
