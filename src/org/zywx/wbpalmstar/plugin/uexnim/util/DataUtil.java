package org.zywx.wbpalmstar.plugin.uexnim.util;

import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.model.DismissAttachment;
import com.netease.nimlib.sdk.team.model.LeaveTeamAttachment;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.plugin.uexnim.vo.ChatRoomInfoVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.ChatRoomMemberVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.ChatRoomMessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.CustomNotificationVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.MessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.OnlineClientVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.SystemMessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamMemberVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.UserInfoVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Fred on 2016/3/22.
 */
public class DataUtil<V> {
    public static TeamVo trans2TeamVo(Team team) {
        TeamVo vo = new TeamVo();
        vo.setTeamId(team.getId());
        vo.setTeamName(team.getName());
        vo.setType(team.getType());
        vo.setOwner(team.getCreator());
        vo.setIntro(team.getIntroduce());
        vo.setAnnouncement(team.getAnnouncement());
        vo.setMemberNumber(team.getMemberCount());//当前群成员人数
        vo.setCreateTime(team.getCreateTime());
        vo.setJoinMode(team.getVerifyType().getValue());
        return vo;
    }

    public static TeamMemberVo trans2TeamMemberVo(TeamMember teamMember) {
        TeamMemberVo vo = new TeamMemberVo();
        vo.setTeamId(teamMember.getTid());
        vo.setUserId(teamMember.getAccount());
        vo.setNickname(teamMember.getTeamNick());
        vo.setType(teamMember.getType().getValue());
        return vo;
    }

    public static MessageVo trans2MessageVo(IMMessage msg) {
        MessageVo vo = new MessageVo();
        vo.setMessageId(msg.getUuid());
        vo.setMessageType(msg.getMsgType().getValue());
        vo.setFrom(msg.getFromAccount());
        vo.setTimestamp(msg.getTime());
        vo.setSessionId(msg.getSessionId());
        vo.senderName=msg.getFromNick();
        vo.setSessionType(msg.getSessionType().getValue());
        vo.setExt(msg.getRemoteExtension());
        MsgTypeEnum msgTypeEnum = msg.getMsgType();
        if (msgTypeEnum == MsgTypeEnum.text) {
            vo.setText(msg.getContent());
        } else if (msgTypeEnum == MsgTypeEnum.audio) {
            AudioAttachment attach = (AudioAttachment) msg.getAttachment();
            vo.setFileLength(attach.getSize());
            vo.setPath(attach.getPath());
            vo.setUrl(attach.getUrl());
            vo.setFileLength(attach.getSize());
            vo.setDuration(attach.getDuration());

        } else if (msgTypeEnum == MsgTypeEnum.image) {
            ImageAttachment attach = (ImageAttachment) msg.getAttachment();
            vo.setFileLength(attach.getSize());
            vo.setPath(attach.getPathForSave());
            vo.setUrl(attach.getUrl());
            vo.setThumbPath(attach.getThumbPathForSave());
            vo.setThumbUrl(attach.getThumbPath());

        } else if (msgTypeEnum == MsgTypeEnum.video) {
            VideoAttachment attach = (VideoAttachment) msg.getAttachment();
            vo.setFileLength(attach.getSize());
            vo.setPath(attach.getPathForSave());
            vo.setUrl(attach.getUrl());
            //vo.setCoverPath(attach.getUrl());
            vo.setCoverUrl(attach.getThumbPathForSave() );
        } else if (msgTypeEnum == MsgTypeEnum.location) {
            LocationAttachment attach = (LocationAttachment) msg.getAttachment();
            vo.setTitle(attach.getAddress());
            vo.setLatitude(attach.getLatitude());
            vo.setLongitude(attach.getLongitude());
        } else if (msgTypeEnum == MsgTypeEnum.notification) {
            MsgAttachment attachment = msg.getAttachment();
            if (attachment instanceof AudioAttachment) {
                vo.setNotificationType(2);
            } else if (attachment instanceof ChatRoomNotificationAttachment) {
                vo.setNotificationType(3);
            } else if (attachment instanceof DismissAttachment || attachment instanceof LeaveTeamAttachment
                    || attachment instanceof UpdateTeamAttachment || attachment instanceof MemberChangeAttachment ) {
                vo.setNotificationType(1);
            } else {
                vo.setNotificationType(0);
            }
        }
        return vo;
    }
    public static ChatRoomMessageVo trans2ChatRoomMessageVo(ChatRoomMessage msg) {
        ChatRoomMessageVo vo = new ChatRoomMessageVo();
        vo.setMessageId(msg.getUuid());
        vo.setMessageType(msg.getMsgType().getValue());
        vo.setFrom(msg.getFromAccount());
        vo.setTimestamp(msg.getTime());
        vo.setSessionId(msg.getSessionId());
        vo.setSessionType(msg.getSessionType().getValue());
        vo.setExt(msg.getRemoteExtension());

        MsgTypeEnum msgTypeEnum = msg.getMsgType();
        if (msgTypeEnum == MsgTypeEnum.text) {
            vo.setText(msg.getContent());
        } else if (msgTypeEnum == MsgTypeEnum.audio) {
            AudioAttachment attach = (AudioAttachment) msg.getAttachment();
            vo.setFileLength(attach.getSize());
            vo.setPath(attach.getPath());
            vo.setUrl(attach.getUrl());
            vo.setFileLength(attach.getSize());
            vo.setDuration(attach.getDuration());

        } else if (msgTypeEnum == MsgTypeEnum.image) {
            ImageAttachment attach = (ImageAttachment) msg.getAttachment();
            vo.setFileLength(attach.getSize());
            vo.setPath(attach.getPathForSave());
            vo.setUrl(attach.getUrl());
            vo.setThumbPath(attach.getThumbPathForSave());
            vo.setThumbUrl(attach.getThumbPath());

        } else if (msgTypeEnum == MsgTypeEnum.video) {
            VideoAttachment attach = (VideoAttachment) msg.getAttachment();
            vo.setFileLength(attach.getSize());
            vo.setPath(attach.getPathForSave());
            vo.setUrl(attach.getUrl());
            vo.setCoverUrl(attach.getThumbPathForSave() );
        } else if (msgTypeEnum == MsgTypeEnum.location) {
            LocationAttachment attach = (LocationAttachment) msg.getAttachment();
            vo.setTitle(attach.getAddress());
            vo.setLatitude(attach.getLatitude());
            vo.setLongitude(attach.getLongitude());
        } else if (msgTypeEnum == MsgTypeEnum.notification) {
            ChatRoomNotificationAttachment attachment = (ChatRoomNotificationAttachment)msg.getAttachment();
            vo.setEventType(attachment.getType().getValue());
            vo.setNotificationType(3); //聊天室的通知类型是 3 ,和iOS保持一致
            vo.setOperator(attachment.getOperator());
            vo.setTargets(attachment.getTargets());
        }
        return vo;
    }

    public static OnlineClientVo trans2OnlineClientVo(OnlineClient client) {
        OnlineClientVo vo = new OnlineClientVo();
        vo.setOs(client.getOs());
        vo.timestamp=client.getLoginTime();
        vo.setType(client.getClientType());
        return vo;
    }

    public static SystemMessageVo trans2SystemMsgVo(SystemMessage message) {
        SystemMessageVo vo = new SystemMessageVo();
        vo.setType(message.getType().getValue());
        vo.setRead(message.isUnread());
        vo.setSourceID(message.getFromAccount());
        vo.setTargetID(message.getTargetId());
        vo.setTimestamp(message.getTime());
        return vo;
    }

    public static CustomNotificationVo trans2CustomNotificationVo(CustomNotification value) {
        CustomNotificationVo vo = new CustomNotificationVo();
        vo.setTimestamp(value.getTime());
        vo.setSender(value.getFromAccount());
        vo.setReceiverType(value.getSessionType().getValue());
        vo.setContent(value.getContent());
        vo.setApnsContent(value.getApnsText());
        return vo;
    }

    public static UserInfoVo trans2UserInfoVo(NimUserInfo user) {
        UserInfoVo vo = new UserInfoVo();
        vo.setNickName(user.getName());
        vo.setAvatarUrl(user.getAvatar());
        vo.setSign(user.getSignature());
        vo.setGender(user.getGenderEnum().getValue());
        vo.setEmail(user.getEmail());
        vo.setBirth(user.getBirthday());
        vo.setMobile(user.getMobile());
        vo.setExt(user.getExtension());
        return vo;
    }

    public static List<UserInfoVo> trans2UserInfoVoList(List<NimUserInfo> userInfos) {
        List<UserInfoVo> list = new ArrayList<UserInfoVo>();
        for (NimUserInfo userInfo : userInfos) {
            list.add(trans2UserInfoVo(userInfo));
        }
        return list;
    }
    public static ChatRoomInfoVo trans2ChatRoomInfoVo(ChatRoomInfo info) {
        ChatRoomInfoVo vo = new ChatRoomInfoVo();
        vo.setRoomId(info.getRoomId());
        vo.setName(info.getName());
        vo.setAnnouncement(info.getAnnouncement());
        vo.setBroadcastUrl(info.getBroadcastUrl());
        vo.setCreator(info.getCreator());
        vo.setOnLineUserCount(info.getOnlineUserCount());
        vo.setExtention(info.getExtension());
        return vo;
    }

    public static List<ChatRoomMemberVo> trans2ChatRoomMembers(List<ChatRoomMember> members ) {
        List<ChatRoomMemberVo> list = new ArrayList<ChatRoomMemberVo>();
        for (ChatRoomMember member: members) {
            list.add(trans2ChatRoomMember(member));
        }
        return list;
    }
    public static ChatRoomMemberVo trans2ChatRoomMember(ChatRoomMember member) {
        ChatRoomMemberVo vo = new ChatRoomMemberVo();
        if (member.getExtension() != null) {
            vo.setExtension(member.getExtension());
        }
        vo.setUserId(member.getAccount());
        vo.setAvatar(member.getAvatar());
        vo.setEnterTime(member.getEnterTime());
        vo.setIsInBlackList(member.isInBlackList());
        vo.setIsMuted(member.isMuted());
        vo.setIsOnline(member.isOnline());
        vo.setMemberType(member.getMemberType().getValue());
        vo.setRoomId(member.getRoomId());
        vo.setNick(member.getNick());
        vo.setUpdateTime(member.getUpdateTime());
        vo.setIsValid(member.isValid());
        return vo;
    }
    public static String getJSONFromMap(Map<String, Object> result) {
        if (result == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            for (Map.Entry entry : result.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                jsonObject.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


}
