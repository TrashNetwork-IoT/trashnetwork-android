package happyyoung.trashnetwork.net.model.result;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-12
 */

public class Result {
    private Integer resultCode;
    private String message;

    public Result(int resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
