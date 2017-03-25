package happyyoung.trashnetwork.cleaning.net.model.request;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-24
 */
public class PostWorkRecordRequest {
    private long trashId;
    private double longitude;
    private double latitude;

    public PostWorkRecordRequest(long trashId, double longitude, double latitude) {
        this.trashId = trashId;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getTrashId() {
        return trashId;
    }

    public void setTrashId(long trashId) {
        this.trashId = trashId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
