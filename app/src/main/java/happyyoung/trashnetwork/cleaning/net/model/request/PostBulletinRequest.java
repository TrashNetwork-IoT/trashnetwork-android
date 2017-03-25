package happyyoung.trashnetwork.cleaning.net.model.request;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-25
 */
public class PostBulletinRequest {
    private long groupId;
    private String title;
    private String textContent;

    public PostBulletinRequest(long groupId, String title, String textContent) {
        this.groupId = groupId;
        this.title = title;
        this.textContent = textContent;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}
