package happyyoung.trashnetwork.cleaning.model;

import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-04-05
 */
public class CleaningReminder {
    private Long trashId;
    private Date remindTime;

    public CleaningReminder(Long trashId, Date remindTime) {
        this.trashId = trashId;
        this.remindTime = remindTime;
    }

    public Long getTrashId() {
        return trashId;
    }

    public void setTrashId(Long trashId) {
        this.trashId = trashId;
    }

    public Date getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(Date remindTime) {
        this.remindTime = remindTime;
    }
}
