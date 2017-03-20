package happyyoung.trashnetwork.cleaning.net.model.request;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class LoginRequest {
    private long userId;
    private String password;

    public LoginRequest(long userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
