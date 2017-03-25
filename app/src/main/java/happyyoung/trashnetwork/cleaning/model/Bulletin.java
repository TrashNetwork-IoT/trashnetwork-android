package happyyoung.trashnetwork.cleaning.model;

import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-25
 */
public class Bulletin {
    private Long posterId;
    private Date postTime;
    private String title;
    private String textContent;

    public Bulletin(Long posterId, Date postTime, String title, String textContent) {
        this.posterId = posterId;
        this.postTime = postTime;
        this.title = title;
        this.textContent = textContent;
    }

    public Long getPosterId() {
        return posterId;
    }

    public void setPosterId(Long posterId) {
        this.posterId = posterId;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
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
