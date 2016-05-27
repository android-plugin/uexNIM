package org.zywx.wbpalmstar.plugin.uexnim.vo;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Fred on 2016/5/24.
 */
public class ChatRoomMemberVo implements Serializable {
    private String userId;
    private String avatar;
    private long enterTime;
    private Map<String, Object> extension;
    private String nick;
    private String roomId;
    private long updateTime;
    private boolean isInBlackList;
    private boolean isMuted;
    private boolean isOnline;
    private boolean isValid;
    private int memberType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(long enterTime) {
        this.enterTime = enterTime;
    }

    public Map<String, Object> getExtension() {
        return extension;
    }

    public void setExtension(Map<String, Object> extension) {
        this.extension = extension;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isInBlackList() {
        return isInBlackList;
    }

    public void setIsInBlackList(boolean isInBlackList) {
        this.isInBlackList = isInBlackList;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public int getMemberType() {
        return memberType;
    }

    public void setMemberType(int memberType) {
        this.memberType = memberType;
    }
}

