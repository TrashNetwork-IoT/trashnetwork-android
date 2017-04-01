package happyyoung.trashnetwork.cleaning.model;

import android.content.Context;

import happyyoung.trashnetwork.cleaning.R;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-17
 */
public class Trash {
    private Long trashId;
    private String description;
    private Double longitude;
    private Double latitude;
    private Boolean bottleRecycle;

    public Trash(Long trashId, String description, Double longitude, Double latitude, Boolean bottleRecycle) {
        this.trashId = trashId;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
        this.bottleRecycle = bottleRecycle;
    }

    public Long getTrashId() {
        return trashId;
    }

    public void setTrashId(Long trashId) {
        this.trashId = trashId;
    }

    public String getTrashName(Context context){
        return context.getString(R.string.trash) + " #" + trashId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Boolean isBottleRecycle() {
        return bottleRecycle;
    }

    public void setBottleRecycle(Boolean bottleRecycle) {
        this.bottleRecycle = bottleRecycle;
    }
}
