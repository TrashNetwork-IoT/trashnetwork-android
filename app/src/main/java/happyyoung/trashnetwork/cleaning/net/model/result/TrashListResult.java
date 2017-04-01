package happyyoung.trashnetwork.cleaning.net.model.result;

import java.util.List;

import happyyoung.trashnetwork.cleaning.model.Trash;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-29
 */
public class TrashListResult extends Result {
    private List<Trash> trashList;

    public TrashListResult(int resultCode, String message, List<Trash> trashList) {
        super(resultCode, message);
        this.trashList = trashList;
    }

    public List<Trash> getTrashList() {
        return trashList;
    }

    public void setTrashList(List<Trash> trashList) {
        this.trashList = trashList;
    }
}
