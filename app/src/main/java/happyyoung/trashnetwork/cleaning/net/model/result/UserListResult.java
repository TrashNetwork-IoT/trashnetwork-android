package happyyoung.trashnetwork.cleaning.net.model.result;


import java.util.List;

import happyyoung.trashnetwork.cleaning.model.User;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-19
 */
public class UserListResult extends Result {
    private List<User> userList;

    public UserListResult(int resultCode, String message, List<User> userList) {
        super(resultCode, message);
        this.userList = userList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}
