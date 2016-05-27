package org.zywx.wbpalmstar.plugin.uexnim;

import android.util.Log;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamServiceObserver;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.zywx.wbpalmstar.plugin.uexnim.util.NIMConstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ListenerRegister {
    private ListenersCallback callback;

    public void registerListeners(boolean register){
        registerLoginListener(register); //注册用户登录相关的监听
        registerMsgObserver(register);
        registerTeamObservers(register);
        registerRecentContactListener(register); //注册最近联系人相关的监听
        registerSystemMsgListener(register);
        registerCustomNotification(register);
        //监听用户资料变更
        registerUserServiceListener(register);
        //聊天室消息的监听
        registerChatRoomListener(register);
    }


    private void registerMsgObserver(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeAttachmentProgress(attachmentProgressObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingMessageObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(messageStatusObserver, register);
    }

    //监听接收的消息
    private Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
            callback.onReceivedMessages(messages);

        }
    };

    // 监听消息状态变化
    private Observer<IMMessage> messageStatusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            callback.onMessageStatusChange(msg);
        }
    };

    // 如果发送的多媒体文件消息，还需要监听文件的上传进度。
    private Observer <AttachmentProgress> attachmentProgressObserver = new Observer<AttachmentProgress>() {
        @Override
        public void onEvent(AttachmentProgress attachmentProgress) {
            //如果是接收对方发来的图片，音频，视频，也会触发这个回调。只是getTransferred 为 0
            //针对上传
            if (attachmentProgress.getTransferred() != 0) {
                Log.i("Listener", "upload progress:" + attachmentProgress.getTransferred() + " /" + attachmentProgress.getTotal());
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("result", true);
                result.put(NIMConstant.TEXT_PROGRESS, attachmentProgress.getTransferred() / attachmentProgress.getTotal());
                result.put(NIMConstant.TEXT_MESSAGE_ID, attachmentProgress.getUuid());
                callback.onSendMessageWithProgress(result);
            }
        }
    };

    private void registerLoginListener(boolean register) {
        //登录监听
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(loginStatusObserver, register);
        //多端登录
        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(multiLoginClientObserver, register);
    }

    private Observer<StatusCode> loginStatusObserver = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (StatusCode.KICK_BY_OTHER_CLIENT == statusCode) {
                callback.onKick(3);
            } else if (StatusCode.KICKOUT == statusCode) {
                callback.onKick(1);
            }
        }
    };
    private Observer<List<OnlineClient>> multiLoginClientObserver = new Observer<List<OnlineClient>>() {
        @Override
        public void onEvent(List<OnlineClient> onlineClients) {
            callback.onMultiLoginClientsChanged(onlineClients);
        }
    };
    public void registerTeamObservers(boolean register) {
        NIMClient.getService(TeamServiceObserver.class).observeTeamUpdate(teamUpdateObserver, register);
        NIMClient.getService(TeamServiceObserver.class).observeTeamRemove(teamRemoveObserver, register);
        NIMClient.getService(TeamServiceObserver.class).observeMemberUpdate(memberUpdateObserver, register);
        NIMClient.getService(TeamServiceObserver.class).observeMemberRemove(memberRemoveObserver, register);
    }

    // 群资料变动观察者通知。新建群和群更新的通知都通过该接口传递
    private Observer<List<Team>> teamUpdateObserver = new Observer<List<Team>>() {
        //返回的是有更新的群资料
        @Override
        public void onEvent(List<Team> teams) {
            callback.onTeamUpdated(teams);
        }
    };

    // 移除群的观察者通知。自己退群，群被解散，自己被踢出群时，会收到该通知
    private Observer<Team> teamRemoveObserver = new Observer<Team>() {
        @Override
        public void onEvent(Team team) {
            // team的flag被更新，isMyTeam为false
            Log.i("onEvent", "--teamRemoveObserver:" + new Gson().toJson(team));
            callback.onTeamRemoved(team);
        }
    };

    // 群成员资料变化观察者通知。可通过此接口更新缓存。
    private Observer<List<TeamMember>> memberUpdateObserver = new Observer<List<TeamMember>>() {
        //返回的参数为有更新的群成员资料列表。
        @Override
        public void onEvent(List<TeamMember> members) {
            Log.i("onEvent", "--memberUpdateObserver:" + new Gson().toJson(members));
            callback.onTeamMemberChanged(members);
        }
    };

    // 移除群成员的观察者通知。
    private Observer<TeamMember> memberRemoveObserver = new Observer<TeamMember>() {
        @Override
        public void onEvent(TeamMember member) {
            // member的validFlag被更新，isInTeam为false
            Log.i("onEvent", "--memberRemoveObserver:" + new Gson().toJson(member));
        }
    };
    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            // 1、根据sessionId判断是否是自己的消息
            // 2、更改内存中消息的状态
            // 3、刷新界面
        }
    };
    private void registerSystemMsgListener(boolean register) {
        NIMClient.getService(SystemMessageObserver.class)
                .observeReceiveSystemMsg(systemMessageObserver, true);
    }
    private Observer<SystemMessage> systemMessageObserver = new Observer<SystemMessage>() {
        @Override
        public void onEvent(SystemMessage message) {
            callback.onSystemMessageRecieved(message);
        }
    };


    private void registerRecentContactListener(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeRecentContact(recentContactObserver, true);
    }
    //监听最近会话变更
   private Observer<List<RecentContact>> recentContactObserver = new Observer<List<RecentContact>>() {
        @Override
        public void onEvent(List<RecentContact> messages) {
            callback.onUpdateRecentSession(messages);
        }
    };

    private void registerCustomNotification(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customNotificationObserver, register);
    }

    //自定义通知的接收观察者
    private Observer<CustomNotification> customNotificationObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            callback.onReceivedCustomNotification(customNotification);
        }
    };



    private void registerUserServiceListener(boolean register) {
        NIMClient.getService(UserServiceObserve.class).observeUserInfoUpdate(userInfoUpdateObserver, register);
    }
    // 用户资料变更观察者
    private Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> users) {
            callback.onUserInfoUpdate(users);
        }
    };

    //聊天室消息的监听
    private void registerChatRoomListener(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeReceiveMessage(incomingChatRoomMsg, register);
        //聊天室状态的监听
        NIMClient.getService(ChatRoomServiceObserver.class).observeOnlineStatus(chatRoomOnlineStatus, register);
        NIMClient.getService(ChatRoomServiceObserver.class)
                .observeKickOutEvent(kickOutObserver, register);
    }
    private Observer<List<ChatRoomMessage>> incomingChatRoomMsg = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> messages) {
            callback.onReceivedChatRoomMessage(messages);
        }
    };
    private Observer<ChatRoomStatusChangeData> chatRoomOnlineStatus = new Observer<ChatRoomStatusChangeData>() {
        @Override
        public void onEvent(ChatRoomStatusChangeData data) {
            // 如果遇到错误码13001，13002，13003，403，404，414，表示无法进入聊天室，此时应该调用离开聊天室接口。
            callback.onChatRoomStatusChanged(data);
        }
    };
    private Observer<ChatRoomKickOutEvent> kickOutObserver = new Observer<ChatRoomKickOutEvent>() {
        @Override
        public void onEvent(ChatRoomKickOutEvent chatRoomKickOutEvent) {
            callback.onChatRoomKickOutEvent(chatRoomKickOutEvent);
        }
    };


    public void setCallback(ListenersCallback callback) {
        this.callback = callback;
    }

    public interface ListenersCallback{
        void onSendMessageWithProgress(Map<String, Object> result);
        void onReceivedMessages(List<IMMessage> messages);
        void onMessageStatusChange(IMMessage msg);
        void onUpdateRecentSession(List<RecentContact> messages);

        void onKick(int code);
        void onMultiLoginClientsChanged(List<OnlineClient> onlineClients);

        void onTeamRemoved(Team team);
        void onTeamUpdated(List<Team> teams);
        void onTeamMemberChanged(List<TeamMember> members);

        void onSystemMessageRecieved(SystemMessage message);

        void onReceivedCustomNotification(CustomNotification customNotification);

        void onUserInfoUpdate(List<NimUserInfo> users);
        void onReceivedChatRoomMessage(List<ChatRoomMessage> messages);

        void onChatRoomStatusChanged(ChatRoomStatusChangeData data);

        void onChatRoomKickOutEvent(ChatRoomKickOutEvent chatRoomKickOutEvent);
    }
}
