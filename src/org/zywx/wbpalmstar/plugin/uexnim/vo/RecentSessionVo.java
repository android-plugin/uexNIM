package org.zywx.wbpalmstar.plugin.uexnim.vo;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;


public class RecentSessionVo {
    private SessionTypeEnum sessionTypeEnum;
    private int unreadCount;
    private MessageVo lastMessage;

    public SessionTypeEnum getSessionTypeEnum() {
        return sessionTypeEnum;
    }

    public void setSessionTypeEnum(SessionTypeEnum sessionTypeEnum) {
        this.sessionTypeEnum = sessionTypeEnum;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public MessageVo getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageVo lastMessage) {
        this.lastMessage = lastMessage;
    }
}
