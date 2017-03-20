package happyyoung.trashnetwork.cleaning.net.model.result;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class LoginResult extends Result {
    private String token;

    public LoginResult(int resultCode, String message, String token) {
        super(resultCode, message);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
