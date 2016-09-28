package org.zywx.wbpalmstar.plugin.uexnim;

/**
 * Created by Fred on 2016/2/19.
 */
public class JsConst {
    public static final String CALLBACK_REGISTER_APP = "uexNIM.cbRegisterApp";
    public static final String CALLBACK_LOGIN = "uexNIM.cbLogin";
    public static final String CALLBACK_LOGOUT = "uexNIM.cbLogout";
    public static final String ON_KICK = "uexNIM.onKick";
    public static final String ON_MULTI_LOGIN_CLIENTS_CHANGED = "uexNIM.onMultiLoginClientsChanged";
    public static final String CALLBACK_REGISTER_USER = "uexNIM.cbRegisterUser";


    public static final String CALLBACK_DID_SEND_MESSAGE = "uexNIM.onMessageSend";
    public static final String ON_SEND_MESSAGE_WITH_PROGRESS = "uexNIM.onSendMessageWithProgress";
    public static final String WILL_SEND_MESSAGE = "uexNIM.willSendMessage";
    public static final String ON_RECIEVED_MESSAGE = "uexNIM.onRecvMessages"; //接收到消息时的监听
    public static final String CALLBACK_ALL_RECENT_SESSION = "uexNIM.cbAllRecentSession"; //接收到消息时的监听
    public static final String CALLBACK_FETCH_MESSAGE_HISTORY = "uexNIM.cbFetchMessageHistory"; //云端记录回调
    public static final String CALLBACK_MESSAGE_IN_SESSION = "uexNIM.cbMessageInSession"; //本地记录回调
    public static final String CALLBACK_SEARCH_MESSAGE = "uexNIM.cbSearchMessages"; //本地消息搜索
    public static final String CALLBACK_SWITCH_AUTIO_OUTPUT_DEVICE = "uexNIM.cbSwitchAudioOutputDevice"; //本地消息搜索
    public static final String CALLBACK_CANCEL_RECORD_AUTIO = "uexNIM.cbRecordAudioDidCancelled"; //取消录音


    public static final String CALLBACK_IS_PLAYING = "uexNIM.cbIsPlaying";
    public static final String CALLBACK_BEGAN_PLAY_AUDIO ="uexNIM.cbBeganPlayAudio";
    public static final String ON_BEGAN_PLAY_AUDIO ="uexNIM.onBeganPlayAudio";

    public static final String CALLBACK_COMPLETED_PLAY_AUDIO ="uexNIM.cbCompletedPlayAudio";
    public static final String ON_COMPLETED_PLAY_AUDIO ="uexNIM.onCompletedPlayAudio";
    public static final String CALLBACK_IS_RECORDING = "uexNIM.cbIsRecording";

    public static final String CALLBACK_ALL_MY_TEAMS = "uexNIM.cbAllMyTeams";
    public static final String CALLBACK_TEAM_BY_ID = "uexNIM.cbTeamById";
    public static final String CALLBACK_FETCH_TEAM_INFO = "uexNIM.cbFetchTeamInfo";
    public static final String CALLBACK_CREATE_TEAM = "uexNIM.cbCreateTeam";
    public static final String CALLBACK_ADD_USERS = "uexNIM.cbAddUsers";
    public static final String CALLBACK_ACCEPT_INVITE_WITH_TEAM = "uexNIM.cbAcceptInviteWithTeam";
    public static final String CALLBACK_REJECT_INVITE_WITH_TEAM = "uexNIM.cbRejectInviteWithTeam";
    public static final String CALLBACK_APPLY_JOIN_TEAM = "uexNIM.cbApplyToTeam";
    public static final String CALLBACK_PASS_APPLY_JOIN_TO_TEAM = "uexNIM.cbPassApplyToTeam";
    public static final String CALLBACK_REJECT_APPLY_JOIN_TO_TEAM = "uexNIM.cbRejectApplyToTeam";
    public static final String CALLBACK_UPDATE_TEAM_NAME = "uexNIM.cbUpdateTeamName";
    public static final String CALLBACK_UPDATE_TEAM_INTRO = "uexNIM.cbUpdateTeamIntro";
    public static final String CALLBACK_UPDATE_TEAM_ANNOUNCEMENT = "uexNIM.cbUpdateTeamAnnouncement";
    public static final String CALLBACK_UPDATE_TEAM_JOIN_MODE = "uexNIM.cbUpdateTeamJoinMode";
    public static final String CALLBACK_ADD_MANAGER_TO_TEAM = "uexNIM.cbAddManagersToTeam";
    public static final String CALLBACK_REMOVE_MANAGER_FROM_TEAM = "uexNIM.cbRemoveManagersFromTeam";
    public static final String CALLBACK_TRANSFER_MANAGER_WITH_TEAM = "uexNIM.cbTransferManagerWithTeam";
    public static final String CALLBACK_FETCH_TEAM_MEMBERS = "uexNIM.cbFetchTeamMembers";
    public static final String CALLBACK_QUIT_TEAM = "uexNIM.cbQuitTeam";
    public static final String CALLBACK_KICK_USERS = "uexNIM.cbKickUsers";
    public static final String CALLBACK_DISMISS_TEAM = "uexNIM.cbDismissTeam" ;
    public static final String CALLBACK_UPDATE_NOTIFY_STATE_FOR_TEAM = "uexNIM.cbUpdateNotifyStateForTeam"; //修改群消息通知状态回调
    public static final String CALLBACK_BEGAN_RECORD_AUDIO = "uexNIM.cbBeganRecordAudio" ;
    public static final String CALLBACK_COMPLETED_RECORD_AUDIO = "uexNIM.cbCompletedRecordAudio" ;

    public static final String ON_TEAM_REMOVED = "uexNIM.onTeamRemoved";
    public static final String ON_TEAM_UPDATED = "uexNIM.onTeamUpdated";
    public static final String ON_TEAM_MEMBER_CHANGED = "uexNIM.onTeamMemberChanged";
    public static final String ON_RECIEVED_SYSTEM_NOTIFICATION = "uexNIM.onReceiveSystemNotification";
    public static final String CALLBACK_FETCH_SYSTEM_NOTIFICATION = "uexNIM.cbFetchSystemNotifications";
    public static final String CALLBACK_ALL_NOTIFICATION_UNREAD_COUNT = "uexNIM.cbAllNotificationsUnreadCount";
    public static final String CALLBACK_MARK_ALL_NOTIFICATIONS_AS_READ = "uexNIM.cbMarkAllNotificationsAsRead";
    public static final String ON_RECIEVED_CUSTOM_SYSTEM_NOTIFICATION = "uexNIM.onReceiveCustomSystemNotification";
    public static final String CALLBACK_USER_INFO = "uexNIM.cbUserInfo" ;
    public static final String ON_USER_INFO_UPDATE = "uexNIM.onUserInfoChanged";
    public static final String CALLBACK_FETCH_USER_INFOS = "uexNIM.cbFetchUserInfos";
    public static final String CALLBACK_UPDATE_MY_USER_INFO = "uexNIM.cbUpdateMyUserInfo" ;
    public static final String CALLBACK_MY_FRIENDS = "uexNIM.cbMyFriends";
    public static final String CALLback_REQUEST_FRIEND = "uexNIM.cbRequestFriend";
    public static final String CALLback_DELETE_FRIEND = "uexNIM.cbDeleteFriend";
    public static final String CALLBACK_MY_BLACK_LIST = "uexNIM.cbMyBlackList";
    public static final String CALLback_ADD_TO_BLACK_LIST = "uexNIM.cbAddToBlackList";
    public static final String CALLback_REMOVE_FROM_BLACK_LIST = "uexNIM.cbRemoveFromBlackBlackList";
    public static final String CALLback_IS_USER_IN_BLACK_LIST = "uexNIM.cbIsUserInBlackList";
    public static final String CALLBACK_MY_MUTE_USER_LIST = "uexNIM.cbMyMuteUserList";
    public static final String CALLBACK_UPDATE_NOTIFY_STATE_FOR_USER = "uexNIM.cbUpdateNotifyStateForUser";
    public static final String CALLBACK_NOTIFY_FOR_NEW_MSG_FOR_USER = "uexNIM.cbNotifyForNewMsgForUser";

    //聊天室
    public static final String CALLBACK_ENTER_CHATROOM = "uexNIM.cbEnterChatRoom";
    public static final String CALLBACK_EXIT_CHATROOM ="uexNIM.exitChatRoom" ;
    public static final String ON_RECIEVED_CHATROOM_MSG ="uexNIM.onReceivedChatRoomMsg" ;
    public static final String CALLBACK_GET_CHATROOM_HISTORY_MSG ="uexNIM.cbGetChatRoomHistoryMsg" ;
    public static final String CALLBACK_GET_CHATROOM_INFO = "uexNIM.cbGetChatRoomInfo";
    public static final String CALLBACK_GET_CHATROOM_MEMBERS = "uexNIM.cbGetChatRoomMembers";
    public static final String CALLBACK_GET_CHATROOM_MEMBERS_BY_IDS = "uexNIM.cbGetChatRoomMembersByIds" ;
    public static final String ON_CHATROOM_STATUS_CHANGE = "uexNIM.onChatRoomStatusChanged";
    public static final String CALLBACK_ADD_USER_TO_BLACK_LIST = "uexNIM.cbAddUserToBlackList";
    public static final String CALLBACK_MUTE_USER = "uexNIM.cbMuteUser";
    public static final String CALLBACK_SET_ADMIN = "uexNIM.cbSetAdmin";
    public static final String CALLBACK_SET_NORMAL = "uexNIM.cbSetNormal";
    public static final String CALLBACK_KICK_MEMBER_FROM_CHAT_ROOM = "uexNIM.cbkKickMemberFromChatRoom";
    public static final String ON_CHAT_ROOM_KICK_OUT_EVENT = "uexNIM.onChatRoomKickOutEvent";
    public static final String ON_BLACK_LIST_CHANGED = "uexNIM.onBlackListChanged";
}
