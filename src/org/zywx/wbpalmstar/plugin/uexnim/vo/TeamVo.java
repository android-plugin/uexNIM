package org.zywx.wbpalmstar.plugin.uexnim.vo;

import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;

/**
 * Created by Fred on 2016/3/22.
 */
public class TeamVo {
    private String teamId;
    private String teamName;
    private TeamTypeEnum type;
    private String owner;
    private String intro;
    private String announcement;
    private int memberNumber; //群成员人数，对应api里面的是memberCount.
    private int level; //群等级
    private long createTime; //创建时间
    private int joinMode;
    private String serverCustomInfo;
    private String clientCustomInfo;
    private boolean notifyForNewMsg; //是否需要通知

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public TeamTypeEnum getType() {
        return type;
    }

    public void setType(TeamTypeEnum type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public int getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(int memberNumber) {
        this.memberNumber = memberNumber;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getJoinMode() {
        return joinMode;
    }

    public void setJoinMode(int joinMode) {
        this.joinMode = joinMode;
    }

    public String getServerCustomInfo() {
        return serverCustomInfo;
    }

    public void setServerCustomInfo(String serverCustomInfo) {
        this.serverCustomInfo = serverCustomInfo;
    }

    public String getClientCustomInfo() {
        return clientCustomInfo;
    }

    public void setClientCustomInfo(String clientCustomInfo) {
        this.clientCustomInfo = clientCustomInfo;
    }

    public boolean isNotifyForNewMsg() {
        return notifyForNewMsg;
    }

    public void setNotifyForNewMsg(boolean notifyForNewMsg) {
        this.notifyForNewMsg = notifyForNewMsg;
    }
}
