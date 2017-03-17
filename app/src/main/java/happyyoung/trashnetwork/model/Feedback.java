package happyyoung.trashnetwork.model;

import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-16
 */
public class Feedback {
    @Nullable
    private String userName;
    private Date feedbackTime;
    private String title;
    private String textContent;

    public Feedback(@Nullable String userName, Date feedbackTime, String title, String textContent) {
        this.userName = userName;
        this.feedbackTime = feedbackTime;
        this.title = title;
        this.textContent = textContent;
    }

    public Feedback(@Nullable String userName, String title, String textContent) {
        this(userName, new Date(), title, textContent);
    }

    public Feedback(String title, String textContent) {
        this(null, new Date(), title, textContent);
    }

    @Nullable
    public String getUserName() {
        return userName;
    }

    public void setUserName(@Nullable String userName) {
        this.userName = userName;
    }

    public Date getFeedbackTime() {
        return feedbackTime;
    }

    public void setFeedbackTime(Date feedbackTime) {
        this.feedbackTime = feedbackTime;
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
