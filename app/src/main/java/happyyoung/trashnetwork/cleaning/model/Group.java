package happyyoung.trashnetwork.cleaning.model;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-24
 */
public class Group {
    private Long groupId;
    private String name;
    private Bitmap portrait;
    private List<Long> memberList;

    public Group(Long groupId, String name, Bitmap portrait, List<Long> memberList) {
        this.groupId = groupId;
        this.name = name;
        this.portrait = portrait;
        this.memberList = memberList;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getPortrait() {
        return portrait;
    }

    public void setPortrait(Bitmap portrait) {
        this.portrait = portrait;
    }

    public List<Long> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<Long> memberList) {
        this.memberList = memberList;
    }
}
