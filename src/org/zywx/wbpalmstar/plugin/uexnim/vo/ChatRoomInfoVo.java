package org.zywx.wbpalmstar.plugin.uexnim.vo;


import java.io.Serializable;
import java.util.Map;

public class ChatRoomInfoVo implements Serializable{
    private String roomId;
    private String name;
    private String creator;
    private String announcement;
    private int onLineUserCount;
    private String broadcastUrl;
    private Map<String, Object> extention;


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public int getOnLineUserCount() {
        return onLineUserCount;
    }

    public void setOnLineUserCount(int onLineUserCount) {
        this.onLineUserCount = onLineUserCount;
    }

    public String getBroadcastUrl() {
        return broadcastUrl;
    }

    public void setBroadcastUrl(String broadcastUrl) {
        this.broadcastUrl = broadcastUrl;
    }

    public Map<String, Object> getExtention() {
        return extention;
    }
    public void setExtention(Map<String, Object> extention) {
        this.extention = extention;
    }

}
