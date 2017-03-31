package happyyoung.trashnetwork.cleaning.model;

import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-07
 */
public class UserLocation {
    private Long userId;
    private Double longitude;
    private Double latitude;
    private String address;
    private Date updateTime;

    public UserLocation(Long userId, Double longitude, Double latitude, Date updateTime, String address) {
        this.userId = userId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.updateTime = updateTime;
        this.address = address;
    }

    public UserLocation(Long userId, Double longitude, Double latitude, String address) {
        this(userId, longitude, latitude, new Date(), address);
    }

    public Long getUserId() {
        return userId;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}