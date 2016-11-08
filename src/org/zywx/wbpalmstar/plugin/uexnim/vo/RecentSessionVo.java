package org.zywx.wbpalmstar.plugin.uexnim.vo;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;


public class RecentSessionVo {
    private SessionTypeEnum sessionType;
    private int unreadCount;
    private MessageVo lastMessage;


    public void setSessionTypeEnum(SessionTypeEnum sessionType) {
        this.sessionType = sessionType;
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
