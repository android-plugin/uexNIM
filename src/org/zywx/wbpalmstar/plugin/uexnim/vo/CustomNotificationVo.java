package org.zywx.wbpalmstar.plugin.uexnim.vo;

import java.io.Serializable;

/**
 * Created by Fred on 2016/3/30.
 */
public class CustomNotificationVo implements Serializable{
    private long timestamp;
    private String sender;
    private String receiver;
    private int receiverType;
    private String content;
    private String apnsContent;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(int receiverType) {
        this.receiverType = receiverType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getApnsContent() {
        return apnsContent;
    }

    public void setApnsContent(String apnsContent) {
        this.apnsContent = apnsContent;
    }
}
