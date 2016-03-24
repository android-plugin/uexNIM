package org.zywx.wbpalmstar.plugin.uexnim.vo;

public class TeamMemberVo {
    private String teamId;
    private String userId;
    private String invitor;
    private int type;
    private String nickname;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInvitor() {
        return invitor;
    }

    public void setInvitor(String invitor) {
        this.invitor = invitor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
