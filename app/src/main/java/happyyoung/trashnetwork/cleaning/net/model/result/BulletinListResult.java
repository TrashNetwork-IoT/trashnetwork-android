package happyyoung.trashnetwork.cleaning.net.model.result;

import java.util.List;

import happyyoung.trashnetwork.cleaning.model.Bulletin;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-25
 */
public class BulletinListResult extends Result {
    private List<Bulletin> bulletinList;

    public BulletinListResult(int resultCode, String message, List<Bulletin> bulletinList) {
        super(resultCode, message);
        this.bulletinList = bulletinList;
    }

    public List<Bulletin> getBulletinList() {
        return bulletinList;
    }

    public void setBulletinList(List<Bulletin> bulletinList) {
        this.bulletinList = bulletinList;
    }
}
