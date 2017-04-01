package happyyoung.trashnetwork.cleaning.model;

import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-17
 */
public class WorkRecord {
    private Long userId;
    private Long trashId;
    private Date recordTime;

    public WorkRecord(Long userId, Long trashId, Date recordTime) {
        this.userId = userId;
        this.trashId = trashId;
        this.recordTime = recordTime;
    }

    public WorkRecord(Long userId, Long trashId) {
        this(userId, trashId, new Date());
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTrashId() {
        return trashId;
    }

    public void setTrashId(Long trashId) {
        this.trashId = trashId;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }
}
