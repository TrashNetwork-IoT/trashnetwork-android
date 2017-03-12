package happyyoung.trashnetwork.model;

import android.graphics.Bitmap;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-19
 */
public class User {
    public static final char ACCOUNT_TYPE_CLEANER = 'C';
    public static final char ACCOUNT_TYPE_MANAGER = 'M';
    public static final char GENDER_MALE = 'M';
    public static final char GENDER_FEMALE = 'F';

    private Long userId;
    private String phoneNumber;
    private String name;
    private Character gender;
    private Character accountType;
    private Bitmap portrait;

    public User(Long userId, String phoneNumber, String name, Character gender, Character accountType, Bitmap portrait) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.gender = gender;
        this.accountType = accountType;
        this.portrait = portrait;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Character getAccountType() {
        return accountType;
    }

    public String getName() {
        return name;
    }

    public Character getGender() {
        return gender;
    }

    public Bitmap getPortrait() {
        return portrait;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof User){
            return userId.equals(((User) obj).userId);
        }
        return false;
    }
}
