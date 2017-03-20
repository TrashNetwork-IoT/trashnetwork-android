package happyyoung.trashnetwork.cleaning.model;

import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-17
 */
public class WorkRecord {
    private Long cleanerId;
    private Long trashId;
    private Date recordTime;



    public WorkRecord(Long cleanerId, Long trashId, Date recordTime) {
        this.cleanerId = cleanerId;
        this.trashId = trashId;
        this.recordTime = recordTime;
    }

    public WorkRecord(Long cleanerId, Long trashId) {
        this(cleanerId, trashId, new Date());
    }

    public Long getCleanerId() {
        return cleanerId;
    }

    public void setCleanerId(Long cleanerId) {
        this.cleanerId = cleanerId;
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
