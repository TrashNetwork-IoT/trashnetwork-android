package happyyoung.trashnetwork.net.model.result;

import happyyoung.trashnetwork.model.User;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-19
 */
public class UserResult extends Result {
    private User user;

    public UserResult(int resultCode, String message, User user) {
        super(resultCode, message);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
