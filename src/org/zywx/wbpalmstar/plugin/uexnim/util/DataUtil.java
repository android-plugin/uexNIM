package org.zywx.wbpalmstar.plugin.uexnim.util;

import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.zywx.wbpalmstar.plugin.uexnim.vo.MessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.OnlineClientVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.SystemMessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamMemberVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamVo;

/**
 * Created by Fred on 2016/3/22.
 */
public class DataUtil {
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
        vo.setMessageType(msg.getMsgType());
        vo.setFrom(msg.getFromAccount());
        vo.setTimestamp(msg.getTime());
        vo.setSessionId(msg.getSessionId());
        vo.setSessionType(msg.getSessionType());
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
        }
        return vo;
    }

    public static OnlineClientVo trans2OnlineClientVo(OnlineClient client) {
        OnlineClientVo vo = new OnlineClientVo();
        vo.setOs(client.getOs());
        vo.setTimestamp(client.getLoginTime());
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
}
