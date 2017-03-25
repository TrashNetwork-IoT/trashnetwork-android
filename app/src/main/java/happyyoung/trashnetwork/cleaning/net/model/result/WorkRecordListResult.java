package happyyoung.trashnetwork.cleaning.net.model.result;

import java.util.List;

import happyyoung.trashnetwork.cleaning.model.WorkRecord;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-24
 */
public class WorkRecordListResult extends Result {
    private List<WorkRecord> workRecordList;

    public WorkRecordListResult(int resultCode, String message, List<WorkRecord> workRecordList) {
        super(resultCode, message);
        this.workRecordList = workRecordList;
    }
    
    public List<WorkRecord> getWorkRecordList() {
        return workRecordList;
    }

    public void setWorkRecordList(List<WorkRecord> workRecordList) {
        this.workRecordList = workRecordList;
    }
}
