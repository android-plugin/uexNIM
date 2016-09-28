package org.zywx.wbpalmstar.plugin.uexnim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimStrings;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.constant.MemberQueryType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.chatroom.model.MemberOption;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.friend.model.BlackListChangedNotify;
import com.netease.nimlib.sdk.media.player.AudioPlayer;
import com.netease.nimlib.sdk.media.player.OnPlayListener;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexnim.util.CommonUtil;
import org.zywx.wbpalmstar.plugin.uexnim.util.DataUtil;
import org.zywx.wbpalmstar.plugin.uexnim.util.NIMConstant;
import org.zywx.wbpalmstar.plugin.uexnim.util.SharedPreferencesHelper;
import org.zywx.wbpalmstar.plugin.uexnim.vo.ChatRoomInfoVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.ChatRoomMemberVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.ChatRoomMessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.CustomNotificationVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.MessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.OnlineClientVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.RecentSessionVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.SystemMessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamMemberVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.UserInfoVo;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EUExNIM extends EUExBase implements ListenerRegister.ListenersCallback {
    private static final String TAG = "EUExNIM";
    private AbortableFuture<LoginInfo> loginRequest;

    private final String MSG_INVALID_SESSION_TYPE = "Invalid sessionType !";
    private final String MSG_EMPTY_SESSION_ID_FILE_PATH = "sessionId or filePath is empty !";
    private final String MSG_TEAM_ID_EMPTY = "teamId can not be null!";
    private final String MSG_INVALID_PARAMS = "invalid params!";
    private final String MSG_ROOM_ID_EMPTY = "roomId can not be null!";
    private final String MSG_ROOM_ID_OR_USER_ID_EMPTY = "roomId or userId is empty!";

    private ResoureFinder finder;
    private AudioPlayer player;
    private AudioRecorder audioRecorder;
    private String TEMP_PATH = "nim_temp";
    private static EBrowserView rootEBrowserView;

    public EUExNIM(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
        finder = ResoureFinder.getInstance(context);
        if ("root".equalsIgnoreCase(eBrowserView.getWindowName())) {
            registerCallback();
        }

    }
    public void registerCallback() {
        rootEBrowserView = mBrwView;
    }
    public void registerApp(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        int callbackId = -1;
        if (params.length == 2 && BUtility.isNumeric(params[1])) {
            callbackId = Integer.parseInt(params[1]);
        }

        JSONObject jsonObject;
        String appKey = null;
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            if (TextUtils.isEmpty(jsonObject.optString("appKey"))) {
                result.put("result", false);
                if (callbackId != -1) {
                    callbackToJs(callbackId, false, EUExCallback.F_C_FAILED);
                } else {
                    result.put("error", 1);
                    evaluateRootWindowScript(JsConst.CALLBACK_REGISTER_APP, getJSONFromMap(result).toString());
                }
                return;
            }
            appKey = jsonObject.getString("appKey");
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        SDKOptions options = getOptions(mContext);
        options.appKey = appKey;

        try {
            NIMClient.init(mContext, getLoginInfo(mContext), options);
            result.put("result", true);
            if (callbackId != -1) {
                callbackToJs(callbackId, false, EUExCallback.F_C_SUCCESS);
            } else {
                evaluateRootWindowScript(JsConst.CALLBACK_REGISTER_APP, getJSONFromMap(result).toString());
            }
        } catch (Exception e) {
            Toast.makeText(mContext,"注册appKey出错!", Toast.LENGTH_SHORT).show();
        }
        ListenerRegister register = new ListenerRegister();
        register.setCallback(this);
        register.registerListeners(true);
    }

    private void registerLocaleReceiver(boolean register) {
        if (register) {
            updateLocale();
            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            mContext.registerReceiver(localeReceiver, filter);
        } else {
            mContext.unregisterReceiver(localeReceiver);
        }
    }

    private BroadcastReceiver localeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                updateLocale();
            }
        }
    };

    //拦截application的onCreate方法
    public static void onApplicationCreate(Context context) {
        if (context instanceof WidgetOneApplication) {
            SDKOptions options = getOptions(context);
            NIMClient.init(context, getLoginInfo(context), options);
        }
    }


    private void updateLocale() {
        NimStrings strings = new NimStrings();
        strings.status_bar_multi_messages_incoming = finder.getString("nim_status_bar_multi_messages_incoming");
        strings.status_bar_image_message = finder.getString("nim_status_bar_image_message");
        strings.status_bar_audio_message = finder.getString("nim_status_bar_audio_message");
        strings.status_bar_custom_message = finder.getString("nim_status_bar_custom_message");
        strings.status_bar_file_message = finder.getString("nim_status_bar_file_message");
        strings.status_bar_location_message = finder.getString("nim_status_bar_location_message");
        strings.status_bar_notification_message = finder.getString("nim_status_bar_notification_message");
        strings.status_bar_ticker_text = finder.getString("nim_status_bar_ticker_text");
        strings.status_bar_unsupported_message = finder.getString("nim_status_bar_unsupported_message");
        strings.status_bar_video_message = finder.getString("nim_status_bar_video_message");
        strings.status_bar_hidden_message_content = finder.getString("nim_status_bar_hidden_msg_content");
        NIMClient.updateStrings(strings);
    }

    //用户登录
    public void login(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        int funcId = -1;
        if (params.length == 2 && BUtility.isNumeric(params[1])) {
            funcId = Integer.parseInt(params[1]);
        }
        final int loginCallbackId = funcId;
        JSONObject jsonObject;
        String account = null;
        String password = null;
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            account = jsonObject.optString("userId");
            password = jsonObject.optString("password");
            if (TextUtils.isEmpty(account)
                    || TextUtils.isEmpty(password)) {
                result.put("result", false);
                String msg = "userId or password is empty !";
                result.put("error", msg);
                if (loginCallbackId != -1) {
                    callbackToJs(loginCallbackId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result).toString());
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            return;
        }

        //将用户的password作为网易云信的token使用
        loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(account, password));
        final String accountTemp = account;
        final String passwordTemp = password;
        loginRequest.setCallback(new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {
                result.put("userId", loginInfo.getAccount());
                //监听状态变化
                NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
                        new Observer<StatusCode>() {
                            public void onEvent(StatusCode status) {
                                Log.i("tag", "User status changed to: " + status);
                            }
                        }, true);
                //保存用户的信息，方便下次的自动登录
                SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(mContext, "uexNIM");
                sharedPreferencesHelper.setUserAccount(accountTemp);
                sharedPreferencesHelper.setUserToken(passwordTemp);
                if (loginCallbackId != -1) {
                    callbackToJs(loginCallbackId, false, EUExCallback.F_C_SUCCESS, getJSONFromMap(result));
                } else {
                    result.put("result", true);
                    evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result).toString());
                }
            }

            @Override
            public void onFailed(int code) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("result", false);
                result.put("error", code);
                result.put("userId", "");
                if (loginCallbackId != -1) {
                    callbackToJs(loginCallbackId, false, code);
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result).toString());
                }
            }

            @Override
            public void onException(Throwable throwable) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("result", false);
                result.put("error", -1); //system error
                if (loginCallbackId != -1) {
                    callbackToJs(loginCallbackId, false, -1);
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result).toString());
                }
            }
        });
    }



    /**
     * 被踢的监听
     */
    @Override
    public void onKick(int code) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("code", code);
        evaluateRootWindowScript(JsConst.ON_KICK, getJSONFromMap(result).toString());
    }

    /**
     * 多端登录监听
     * @param onlineClients
     */
    @Override
    public void onMultiLoginClientsChanged(List<OnlineClient> onlineClients) {
        if (onlineClients != null && onlineClients.size() >0) {
            List<OnlineClientVo> list = new ArrayList<OnlineClientVo>();
            for (OnlineClient client : onlineClients) {
                OnlineClientVo vo = DataUtil.trans2OnlineClientVo(client);
                list.add(vo);
            }
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("clients", list);
            evaluateRootWindowScript(JsConst.ON_MULTI_LOGIN_CLIENTS_CHANGED, new Gson().toJson(result));
        }
    }

    //退出操作
    public void logout(String params[]) {
        int logoutCallbackId = -1;
        if (params.length == 1 && BUtility.isNumeric(params[0])) {
            logoutCallbackId = Integer.parseInt(params[0]);

        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            NIMClient.getService(AuthService.class).logout();
            result.put("result", true);
            if (logoutCallbackId != -1) {
                callbackToJs(logoutCallbackId, false, EUExCallback.F_C_SUCCESS);
            } else {
                evaluateRootWindowScript(JsConst.CALLBACK_LOGOUT, getJSONFromMap(result).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("result", false);
            result.put("error", e.toString());
            if (logoutCallbackId != -1) {
                callbackToJs(logoutCallbackId ,false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
            } else {
                evaluateRootWindowScript(JsConst.CALLBACK_LOGOUT, getJSONFromMap(result).toString());
            }
        }
    }

    //-----------------------------------基础消息功能--------------------------------------

    //发送文本消息及表情
    public void sendText(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String sessionId = null;
        String content = null;
        int sessionType = 0;
        Map<String, Object> ext;

        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            content = jsonObject.optString("content");
            HashMap<String, Object> result = new HashMap<String, Object>();
            if (TextUtils.isEmpty(sessionId)) {
                if (funcId != -1) {
                    result.put("error", "sessionId is empty !");
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            if (sessionType != 0 && sessionType != 1 && sessionType != 2) {
                if (funcId != -1) {
                    result.put("error", MSG_INVALID_SESSION_TYPE);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            JSONObject extObj = jsonObject.optJSONObject("ext"); //扩展字段
            ext = getMapFromJSON(extObj);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        //如果是聊天室消息
        if (sessionType == 2) {
            final ChatRoomMessage  message = ChatRoomMessageBuilder.createChatRoomTextMessage(sessionId, content);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, content, NIMConstant.MESSAGE_TYPE_TEXT, sessionType, message);
            NIMClient.getService(ChatRoomService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendText", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        } else {
            final IMMessage  message = MessageBuilder.createTextMessage(sessionId, SessionTypeEnum.typeOfValue(sessionType), content);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, content, NIMConstant.MESSAGE_TYPE_TEXT, sessionType, message);
            NIMClient.getService(MsgService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendText", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            MessageVo vo = DataUtil.trans2MessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        }
    }

    private void willSendMsgCallback(String sessionId, String content, int messageType, int sessionType, IMMessage message) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(NIMConstant.TEXT_MESSAGE_ID, message.getUuid());
        map.put(NIMConstant.TEXT_MESSAGE_TYPE, messageType);
        map.put(NIMConstant.TEXT_SESSION_ID, sessionId);
        map.put(NIMConstant.TEXT_SESSION_TYPE, sessionType);
        if (content != null) {
            map.put(NIMConstant.TEXT_CONTENT, content);
        }
        evaluateRootWindowScript(JsConst.WILL_SEND_MESSAGE, getJSONFromMap(map).toString());
    }

    public void sendImage(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String sessionId = null;
        String filePath = null;
        int sessionType = 0;
        String displayName;
        Map<String, Object> ext;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            displayName = jsonObject.optString("displayName");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                if (funcId != -1) {
                    result.put("error", MSG_EMPTY_SESSION_ID_FILE_PATH);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            if (sessionType != 0 && sessionType != 1 && sessionType != 2) {
                if (funcId != -1) {
                    result.put("error", MSG_INVALID_SESSION_TYPE);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            JSONObject extObj = jsonObject.optJSONObject("ext"); //扩展字段
            ext = getMapFromJSON(extObj);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sessionType == 2) {
            // 创建图片消息
            final ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomImageMessage(sessionId, new File(getRealPath(filePath)), displayName);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_IMAGE, sessionType, message);
            NIMClient.getService(ChatRoomService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendImage", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));

        } else {
            // 创建图片消息
            final IMMessage message = MessageBuilder.createImageMessage(
                     sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                     SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                     new File(getRealPath(filePath)), // 图片文件对象
                     null // 文件显示名字，如果第三方 APP 不关注，可以为 null
            );
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_IMAGE, sessionType, message);
            NIMClient.getService(MsgService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendImage", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            MessageVo vo = DataUtil.trans2MessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        }


    }


    private RequestCallback sendMsgCallback = new RequestCallback<Void>() {
        @Override
        public void onSuccess(Void avoid) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("result", true);
            evaluateRootWindowScript(JsConst.CALLBACK_DID_SEND_MESSAGE, getJSONFromMap(result).toString());
        }

        @Override
        public void onFailed(int code) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("result", false);
            result.put("error", code);
            evaluateRootWindowScript(JsConst.CALLBACK_DID_SEND_MESSAGE, getJSONFromMap(result).toString());
        }

        @Override
        public void onException(Throwable throwable) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("result", false);
            result.put("error", throwable.getMessage());
            evaluateRootWindowScript(JsConst.CALLBACK_DID_SEND_MESSAGE, getJSONFromMap(result).toString());

        }
    };

    public void sendLocationMsg(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String sessionId = null;
        double latitude = 0.0;
        double longitude = 0.0;
        int sessionType = 0;
        String title = "";
        Map<String, Object> ext;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            latitude = jsonObject.optDouble("latitude");
            longitude = jsonObject.optDouble("longitude");
            title = jsonObject.optString("title");
            if (TextUtils.isEmpty(sessionId)) {
                if (funcId != -1) {
                    result.put("error", "sessionId is empty !");
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            if (sessionType != 0 && sessionType != 1 && sessionType != 2) {
                if (funcId != -1) {
                    result.put("error", MSG_INVALID_SESSION_TYPE);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            JSONObject extObj = jsonObject.optJSONObject("ext"); //扩展字段
            ext = getMapFromJSON(extObj);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sessionType == 2) {
            // 创建地理位置消息
            final ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomLocationMessage(sessionId, latitude, longitude, title);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_IMAGE, sessionType, message);
            NIMClient.getService(ChatRoomService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendLocationMsg", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        } else {
            // 发送其他类型的消息代码类似，演示如下
            // 创建地理位置消息
            final IMMessage message = MessageBuilder.createLocationMessage(
                    sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                    SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                    latitude, // 纬度
                    longitude, // 经度
                    title // 地址信息描述
            );
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_LOCATION, sessionType, message);
            NIMClient.getService(MsgService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendLocationMsg", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            MessageVo vo = DataUtil.trans2MessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        }
    }


    //发送音频
    public void sendAudio(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String sessionId = null;
        String filePath = null;
        int sessionType = 0;
        Map<String, Object> ext;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                if (funcId != -1) {
                    result.put("error", MSG_EMPTY_SESSION_ID_FILE_PATH);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            if (sessionType != 0 && sessionType != 1 && sessionType != 2) {
                if (funcId != -1) {
                    result.put("error", MSG_INVALID_SESSION_TYPE);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            JSONObject extObj = jsonObject.optJSONObject("ext"); //扩展字段
            ext = getMapFromJSON(extObj);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        String realPath = getRealPath(filePath);
        File file = new File(realPath);
        MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(realPath));
        int duration = mp.getDuration();
        if (sessionType == 2) {
            // 创建音频消息
            final ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomAudioMessage(sessionId, file, duration);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_AUDIO, sessionType, message);
            NIMClient.getService(ChatRoomService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendAudio", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        } else {
            final IMMessage  message = MessageBuilder.createAudioMessage(
                    sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                    SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                    file, // 音频文件
                    duration // 音频持续时间，单位是ms
            );
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_AUDIO, sessionType, message);
            NIMClient.getService(MsgService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendAudio", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            MessageVo vo = DataUtil.trans2MessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        }
    }

    //发送视频
    public void sendVideo(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String sessionId = null;
        String filePath = null;
        int sessionType = 0;
        String displayName;
        Map<String, Object> ext;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            displayName = jsonObject.optString("displayName");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                if (funcId != -1) {
                    result.put("error", MSG_EMPTY_SESSION_ID_FILE_PATH);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            if (sessionType != 0 && sessionType != 1 && sessionType != 2) {
                if (funcId != -1) {
                    result.put("error", MSG_INVALID_SESSION_TYPE);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            JSONObject extObj = jsonObject.optJSONObject("ext"); //扩展字段
            ext = getMapFromJSON(extObj);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        String realPath = getRealPath(filePath);
        File file = new File(realPath);
        MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(realPath));
        int duration = mp.getDuration();
        int width = mp.getVideoWidth();
        int height = mp.getVideoHeight();
        if (sessionType == 2) {
            final ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomVideoMessage(sessionId, file, duration, width, height, displayName);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_VIDEO, sessionType, message);
            NIMClient.getService(ChatRoomService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendVideo", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        } else {
            // 创建视频消息
            final IMMessage message = MessageBuilder.createVideoMessage(
                    sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                    SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                    file, // 视频文件
                    duration, // 视频持续时间
                    width, // 视频宽度
                    height, // 视频高度
                    displayName // 视频显示名，可为空
            );
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_VIDEO, sessionType, message);
            NIMClient.getService(MsgService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendVideo", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE,  new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            MessageVo vo = DataUtil.trans2MessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        }
    }

    //发送文件
    public void sendFile(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String sessionId = null;
        String filePath = null;
        int sessionType = 0;
        String displayName;
        Map<String, Object> ext;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            displayName = jsonObject.optString("displayName");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                if (funcId != -1) {
                    result.put("error", MSG_EMPTY_SESSION_ID_FILE_PATH );
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            if (sessionType != 0 && sessionType != 1 && sessionType != 2) {
                if (funcId != -1) {
                    result.put("error", MSG_INVALID_SESSION_TYPE);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
            JSONObject extObj = jsonObject.optJSONObject("ext"); //扩展字段
            ext = getMapFromJSON(extObj);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(getRealPath(filePath));
        if (sessionType == 2) {
            final ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomFileMessage(sessionId, file, displayName);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_FILE, sessionType, message);
            NIMClient.getService(ChatRoomService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendFile", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        } else {
            final IMMessage message = MessageBuilder.createFileMessage(
                    sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                    SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                    file,
                    displayName);
            if (ext != null) {
                message.setRemoteExtension(ext);
            }
            willSendMsgCallback(sessionId, null, NIMConstant.MESSAGE_TYPE_FILE, sessionType, message);
            NIMClient.getService(MsgService.class).sendMessage(message, true).setCallback(
                    new RequestCallbackTemplate<Void>("sendFile", funcId, JsConst.CALLBACK_DID_SEND_MESSAGE, new CustomDataUtil<Void>() {
                        @Override
                        public String getDataStr(Void avoid) {
                            MessageVo vo = DataUtil.trans2MessageVo(message);
                            return new Gson().toJson(vo);
                        }
                    }));
        }
    }

    //获取最近会话
    public void allRecentSession(String params[]) {
        int callbackId = -1;
        if (params.length == 1 && BUtility.isNumeric(params[0])) {
            callbackId = Integer.parseInt(params[0]);
        }
        final int funcCallbackId = callbackId;
        NIMClient.getService(MsgService.class).queryRecentContacts().setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
            @Override
            public void onResult(int code, List<RecentContact> recents, Throwable e) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                if (code != ResponseCode.RES_SUCCESS || recents == null || recents.size() == 0) {
                    map.put(NIMConstant.TEXT_RESULT, false);
                    String msg = "无最近会话消息";
                    map.put(NIMConstant.TEXT_ERROR, msg);
                    if (funcCallbackId != -1) {
                        callbackToJs(funcCallbackId, false, EUExCallback.F_C_FAILED, getJSONFromMap(map));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_ALL_RECENT_SESSION, getJSONFromMap(map).toString());
                    }
                    return;
                }
                List<RecentSessionVo> list = new ArrayList<RecentSessionVo>();
                for (RecentContact recent : recents) {
                    RecentSessionVo recentVo = new RecentSessionVo();
                    recentVo.setSessionTypeEnum(recent.getSessionType());
                    recentVo.setUnreadCount(recent.getUnreadCount());
                    MsgTypeEnum msgTypeEnum = recent.getMsgType();
                    MessageVo vo = new MessageVo();
                    vo.setMessageId(recent.getRecentMessageId());
                    vo.setFrom(recent.getFromAccount());
                    if (msgTypeEnum == MsgTypeEnum.text) {
                        vo.setText(recent.getContent());
                    } else if (msgTypeEnum == MsgTypeEnum.audio) {
                        AudioAttachment attach = (AudioAttachment) recent.getAttachment();
                        vo.setFileLength(attach.getSize());
                        vo.setPath(attach.getPath());
                        vo.setUrl(attach.getUrl());
                        vo.setFileLength(attach.getSize());
                        vo.setDuration(attach.getDuration());

                    } else if (msgTypeEnum == MsgTypeEnum.image) {
                        ImageAttachment attach = (ImageAttachment) recent.getAttachment();
                        vo.setFileLength(attach.getSize());
                        vo.setPath(attach.getPathForSave());
                        vo.setUrl(attach.getUrl());
                        vo.setThumbPath(attach.getThumbPathForSave());
                        vo.setThumbUrl(attach.getThumbPath());

                    } else if (msgTypeEnum == MsgTypeEnum.video) {
                        VideoAttachment attach = (VideoAttachment) recent.getAttachment();
                        vo.setFileLength(attach.getSize());
                        vo.setPath(attach.getPathForSave());
                        vo.setUrl(attach.getUrl());
                        vo.setCoverUrl(attach.getThumbPathForSave());
                    } else if (msgTypeEnum == MsgTypeEnum.location) {
                        LocationAttachment attach = (LocationAttachment) recent.getAttachment();
                        vo.setTitle(attach.getAddress());
                        vo.setLatitude(attach.getLatitude());
                        vo.setLongitude(attach.getLongitude());
                    }
                    recentVo.setLastMessage(vo);
                    list.add(recentVo);
                }
                map.put("sessions", list);
                if (funcCallbackId != -1) {
                    callbackToJs(funcCallbackId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(map));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_ALL_RECENT_SESSION, new Gson().toJson(map));
                }
            }
        });
    }

    /**
     * 获取云端消息记录
     * @param params
     */
    public void fetchMessageHistory(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String sessionId = null;
        int sessionType = 0;
        int limit = 100;
        int order = 0;
        boolean sync = false;
        long startTime = 0l;
        int id = -1;
        if (params.length == 2 && BUtility.isNumeric(params[1])) {
            id = Integer.parseInt(params[1]);
        }
        final int callbackId = id;
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            limit = jsonObject.optInt("limit");  //检索条数, 最大限制100条
//            order = jsonObject.optInt("order");  //检索顺序,0:从新消息往旧消息查询,1:从旧消息往新消息查询
            sync = jsonObject.optBoolean("sync");  //同步数据: 是否在远程获取消息成功之后同步到本地数据库，如果选择同步，则同步之后不会触发消息添加的回调。默认不同步(false),true为同步。
            startTime = jsonObject.optLong("startTime"); //起始时间点
            if (TextUtils.isEmpty(sessionId)) {
                Toast.makeText(mContext, "sessionId is empty !", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        IMMessage message = MessageBuilder.createEmptyMessage(sessionId, SessionTypeEnum.typeOfValue(sessionType), startTime);
        NIMClient.getService(MsgService.class).pullMessageHistory(message, limit, sync).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> imMessages) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                List<MessageVo> list = new ArrayList<MessageVo>();
                for (IMMessage msg : imMessages) {
                    list.add(DataUtil.trans2MessageVo(msg));
                }
                result.put("messages", list);
                if (callbackId != -1) {
                    callbackToJs(callbackId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_MESSAGE_HISTORY, new Gson().toJson(list));
                }
            }

            @Override
            public void onFailed(int code) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", code);
                if (callbackId != -1) {
                    callbackToJs(callbackId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_MESSAGE_HISTORY, getJSONFromMap(result).toString());
                }

            }

            @Override
            public void onException(Throwable throwable) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", throwable.getMessage());
                if (callbackId != -1) {
                    callbackToJs(callbackId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_MESSAGE_HISTORY, getJSONFromMap(result).toString());
                }
            }
        });
    }

     /**
     *切换音频的输出设备
     */
    public boolean switchAudioOutputDevice(String params []) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        String json = params[0];
        JSONObject jsonObject;
        int outputDevice = 0;
        try {
            jsonObject = new JSONObject(json);
            outputDevice = jsonObject.optInt("outputDevice");
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
        }
        if (player == null) {
            player = new AudioPlayer(mContext);
        }
        if(outputDevice == 0) {
            player.start(AudioManager.STREAM_VOICE_CALL);
        } else {
            player.start(AudioManager.STREAM_MUSIC);
        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("result", true);
        evaluateRootWindowScript(JsConst.CALLBACK_SWITCH_AUTIO_OUTPUT_DEVICE, getJSONFromMap(result).toString());
        return true;
    }

    public boolean isPlaying(String params[]) {
        if (player == null) {
            player = new AudioPlayer(mContext);
        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("result", player.isPlaying());
        evaluateRootWindowScript(JsConst.CALLBACK_IS_PLAYING, getJSONFromMap(result).toString());
        return player.isPlaying();
    }

    public void playAudio(String params[]) {
        String json = params[0];
        JSONObject jsonObject;
        String filePath = "";
        try {
            jsonObject = new JSONObject(json);
            filePath = jsonObject.optString("filePath");
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
        }

        final String realPath = getRealPath(filePath);
        final String tempPath = filePath ;
        if (player == null) {
            player = new AudioPlayer(mContext);
        }
        player.setDataSource(realPath);
        player.setOnPlayListener(new OnPlayListener() {
            @Override
            public void onPrepared() {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("filePath", tempPath);
                //4.0改成on
                evaluateRootWindowScript(JsConst.ON_BEGAN_PLAY_AUDIO, getJSONFromMap(map).toString());
                evaluateRootWindowScript(JsConst.CALLBACK_BEGAN_PLAY_AUDIO, getJSONFromMap(map).toString());
            }

            @Override
            public void onCompletion() {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("filePath", tempPath);
                //4.0改成on
                evaluateRootWindowScript(JsConst.ON_COMPLETED_PLAY_AUDIO, getJSONFromMap(map).toString());
                evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_PLAY_AUDIO, getJSONFromMap(map).toString());
            }

            @Override
            public void onInterrupt() {

            }

            @Override
            public void onError(String s) {

            }

            @Override
            public void onPlaying(long l) {

            }
        });
        player.start(AudioManager.STREAM_VOICE_CALL);
    }

    /**
     * 停止播放音频
     * @param params
     */
    public void stopPlay(String params[]) {
        if (player == null) {
            player = new AudioPlayer(mContext);
        }
        if (!player.isPlaying()) {
            return;
        }
        boolean flag = params.length == 1 && BUtility.isNumeric(params[0]);
        final int funcId = flag ? Integer.parseInt(params[0]) : -1;
        player.setOnPlayListener(new OnPlayListener() {
            @Override
            public void onPrepared() {
                Log.i(TAG, "[stopPlay]----onPrepared----");
            }

            @Override
            public void onCompletion() {
                Log.i(TAG, "[stopPlay]----onCompletion----");
                if(funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_PLAY_AUDIO, null);
                }
            }

            @Override
            public void onInterrupt() {
                Log.i(TAG, "[stopPlay]----onInterrupt----");
                if(funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_PLAY_AUDIO, null);
                }
            }

            @Override
            public void onError(String s) {
                Log.i(TAG, "[onError]-----" + s);
            }

            @Override
            public void onPlaying(long l) {

            }
        });
        player.stop();
    }
    /**
     * 判断是否正在录制音频
     * @param params
     */
    public boolean isRecording(String params[]) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        boolean flag = false;
        if (audioRecorder != null) {
            flag = audioRecorder.isRecording();

        }
        result.put("result", flag);
        evaluateRootWindowScript(JsConst.CALLBACK_IS_RECORDING, getJSONFromMap(result).toString());
        return flag;
    }

    /**
     * 录音
     * @param params
     */
    public void recordAudioForDuration(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        int duration = 0;
        float updateTime;  //当前Android不支持
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            duration = jsonObject.optInt("duration");
            updateTime = jsonObject.optInt("updateTime");
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
        }
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        final int funcId = flag ? Integer.parseInt(params[1]) : -1;

        if (audioRecorder == null) {
            audioRecorder = getAudioRecorder(duration, new IAudioRecordCallback() {
                private File audioFile = null;
                @Override
                public void onRecordReady() {

                }

                @Override
                public void onRecordStart(File file, RecordType recordType) {
                    audioFile = file;
                    result.put("filePath", file.getAbsolutePath());
                    result.put("status", 1);
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, getJSONFromMap(result));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_BEGAN_RECORD_AUDIO, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onRecordSuccess(File file, long l, RecordType recordType) {
                    Log.i(TAG, "[record audio success]");
                    result.put("status", 2);
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, getJSONFromMap(result));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_RECORD_AUDIO, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onRecordFail() {

                }

                @Override
                public void onRecordCancel() {
                    result.put("status", 3);
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, getJSONFromMap(result));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_CANCEL_RECORD_AUTIO, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onRecordReachedMaxTime(int i) {
                    result.put("filePath", audioFile.getAbsolutePath());
                    result.put("status", 2);
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, getJSONFromMap(result));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_RECORD_AUDIO, getJSONFromMap(result).toString());
                    }
                }
            });
        }
        audioRecorder.startRecord();
    }

    private AudioRecorder getAudioRecorder(int maxDuration, IAudioRecordCallback callback) {
        if (audioRecorder == null) {
            return new AudioRecorder(mContext, RecordType.AAC, maxDuration,  callback);
        }
        return audioRecorder;
    }
    /**
     * 停止录制音频
     * @param params
     */
    public void stopRecord(String params[]) {
        if (audioRecorder != null) {
            audioRecorder.completeRecord(false);//停止录音
        }
    }

    /**
     * 取消录音
     * @param params
     */
    public void cancelRecord(String params[]){
        if (audioRecorder != null) {
            audioRecorder.completeRecord(true);
        }
    }

    //-----------------------------------------------------群组相关-----------------------------------------------

    /**
     * 获取我的所有群组
     * @param params
     */
    public void allMyTeams(String params[]) {
        boolean flag = params.length == 1 && BUtility.isNumeric(params[0]);
        final int funcId = flag ? Integer.parseInt(params[0]): -1 ;

        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> teams) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                List<TeamVo> list = new ArrayList<TeamVo>();
                for (Team team : teams) {
                    TeamVo vo = DataUtil.trans2TeamVo(team);
                    list.add(vo);
                }
                result.put("teams", list);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_ALL_MY_TEAMS, new Gson().toJson(result));
                }
            }

            @Override
            public void onFailed(int i) {
                Log.i(TAG, "[Query Team List Failed] error:" + i);
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", i);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_ALL_MY_TEAMS, getJSONFromMap(result).toString());
                }

            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "[Query Team List Exception] error:" + throwable.getMessage());
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", throwable.getMessage());
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_ALL_MY_TEAMS, getJSONFromMap(result).toString());
                }
            }
        });
    }

    /**
     * 本地获取群组信息
     * @param params
     */
    public void teamById(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        String teamId = "";

        try {
            jsonObject = new JSONObject(json);
            teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", MSG_TEAM_ID_EMPTY);
                evaluateRootWindowScript(JsConst.CALLBACK_TEAM_BY_ID, getJSONFromMap(result).toString());
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }

        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        final int funcId = flag ? Integer.parseInt(params[1]) : -1;

        NIMClient.getService(TeamService.class).queryTeam(teamId).setCallback(
                new RequestCallbackTemplate<Team>("teamById", funcId, JsConst.CALLBACK_TEAM_BY_ID, new CustomDataUtil<Team>() {
                    @Override
                    public String getDataStr(Team team) {
                        HashMap<String, Object> result = new HashMap<String, Object>();
                        TeamVo vo = DataUtil.trans2TeamVo(team);
                        result.put("team", vo);
                        return new Gson().toJson(result);
                    }
                })
        );
    }

    /**
     * 获取Team的信息。
     * 该接口和teamById的实现方式一样，此处是为了和ios保持一致。
     * @param params
     */
    public void fetchTeamInfo(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String teamId = "";
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", MSG_TEAM_ID_EMPTY);
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_INFO, getJSONFromMap(result).toString());
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        final int funcId = flag ? Integer.parseInt(params[1]) : -1;

        NIMClient.getService(TeamService.class).searchTeam(teamId).setCallback(
                new RequestCallbackTemplate<Team>("fetchTeamInfo", funcId, JsConst.CALLBACK_FETCH_TEAM_INFO, new CustomDataUtil<Team>() {
                    @Override
                    public String getDataStr(Team team) {
                        TeamVo vo = DataUtil.trans2TeamVo(team);
                        result.put("team", vo);
                        return new Gson().toJson(result);
                    }
                }));
    }

    /**
     * 创建群组
     * @param params
     */
    public void createTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(json);
            String name = jsonObject.optString("name");
            int type = jsonObject.optInt("type", 0);
            int joinMode = jsonObject.optInt("joinMode", 0);
            String postscript = jsonObject.optString("postscript");
            String intro = jsonObject.optString("intro");
            String announcement = jsonObject.optString("announcement");
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);

            List<String> userIdList = new ArrayList<String>();
            if (users != null && users.length() > 0) {
                int len = users.length();
                for (int i = 0; i < len; i++) {
                    userIdList.add(users.getString(i));
                }
            }
            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(mContext, "uexNIM");
            String userAccount = sharedPreferencesHelper.getUserAccount();

            if (!userIdList.contains(userAccount)) {
                userIdList.add(userAccount); //把当前用户自己的id加进去
            }
            HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
            fields.put(TeamFieldEnum.Name, name);
            TeamTypeEnum teamType = TeamTypeEnum.Normal;
            if(type == 1) {
                teamType = TeamTypeEnum.Advanced;
            }
            VerifyTypeEnum verifyType = VerifyTypeEnum.Free;
            if (joinMode == 1) {
                verifyType = VerifyTypeEnum.Apply;
            }
            if (joinMode == 2) {
                verifyType = VerifyTypeEnum.Private;
            }
            fields.put(TeamFieldEnum.Introduce, intro);
            fields.put(TeamFieldEnum.VerifyType, verifyType);
            fields.put(TeamFieldEnum.Announcement, announcement);

            boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
            final int funcId = flag ? Integer.parseInt(params[1]) : -1;

            NIMClient.getService(TeamService.class).createTeam(fields, teamType, postscript, userIdList).setCallback(
                    new RequestCallbackTemplate<Team>("createTeam", funcId, JsConst.CALLBACK_CREATE_TEAM, new CustomDataUtil<Team>() {
                        @Override
                        public String getDataStr(Team team) {
                            result.put("teamId", team.getId());
                            result.put("error", "");
                            return getJSONFromMap(result).toString();
                        }
                    }));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 邀请用户入群
     * 请求完成后，如果是普通群，被邀请者将直接入群；如果是高级群，云信服务器会下发一条系统消息到目标用户，目标用户可以选择同意或者拒绝入群。
     * @param params
     */
    public void addUsers(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", MSG_TEAM_ID_EMPTY);
                evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result).toString());
                return;
            }
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);
            if (users == null || users.length() < 1) {
                result.put("error", "users can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result).toString());
                return;
            }
            List <String> userList = new ArrayList<String>();
            int len = users.length();
            for (int i = 0; i < len; i++) {
                userList.add(users.getString(i));
            }
            boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
            final int funcId = flag ? Integer.parseInt(params[1]) : -1;

            NIMClient.getService(TeamService.class).addMembers(teamId, userList).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onFailed(int i) {
                    //高级群不能直接拉人，发出邀请成功会返回810，此处应该认为邀请已发出
                    result.put("error", i);
                    if (funcId != -1) {
                        if (i == 810) {
                            callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                        } else {
                            callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                        }
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result).toString());
                    }
                }
            });
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     *  同意群邀请(仅限高级群)
     * @param params
     */
    public void acceptInviteWithTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        final int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String inviterId = jsonObject.optString("invitorId"); //invitorId--文档的拼写错误
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(inviterId)) {
                result.put("error", "teamId or invitorId can not be null");
                if (funcId == -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_ACCEPT_INVITE_WITH_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).acceptInvite(teamId, inviterId).setCallback(
                    new RequestCallbackTemplate<Void>("acceptInviteWithTeam", funcId, JsConst.CALLBACK_ACCEPT_INVITE_WITH_TEAM));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 拒绝别人的加群邀请
     * @param params
     */
    public void rejectInviteWithTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String inviterId = jsonObject.optString("invitorId"); //invitorId--文档的拼写错误
            String rejectReason = jsonObject.optString("rejectReason");
            boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
            final int funcId = flag ? Integer.parseInt(params[1]) : -1;

            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(inviterId)) {
                String msg = "teamId or invitorId can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_INVITE_WITH_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }

            NIMClient.getService(TeamService.class).declineInvite(teamId, inviterId, rejectReason).setCallback(
                    new RequestCallbackTemplate<Void>("rejectInviteWithTeam", funcId, JsConst.CALLBACK_REJECT_INVITE_WITH_TEAM));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 用户主动申请加群
     * @param params
     */
    public void applyToTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        final int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String message = jsonObject.optString("message");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", MSG_TEAM_ID_EMPTY);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).applyJoinTeam(teamId, message).setCallback(new RequestCallback<Team>() {
                @Override
                public void onSuccess(Team team) {
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onFailed(int code) {
                    if (code == 808) {
                        result.put("applyStatus", 2); //申请已发出
                    } else if (code == 809) {
                        result.put("applyStatus", 1);//已经在群里
                    } else {
                        result.put("error", code);
                    }
                    result.put("code", code);
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, getJSONFromMap(result));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result).toString());
                    }
                }
            });
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 通过申请
     * @param params
     */
    public void passApplyToTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        final int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String userId = jsonObject.optString("userId");
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(userId)) {
                result.put("error", "teamId or userId can not be null");
                if(funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).passApply(teamId, userId).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                    } else {
                        evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                    }
                }

                @Override
                public void onFailed(int code) {
                    if (code == 809) {
                        result.put("applyStatus", 1);//已经在群里
                    } else {
                        result.put("error", code);
                    }
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                        return;
                    }
                    evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                }
            });
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 拒绝申请
     * @param params
     */
    public void rejectApplyToTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        final int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String userId = jsonObject.optString("userId");
            String rejectReason = jsonObject.optString("rejectReason", "");
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(userId)) {
                result.put("error", "teamId or userId can not be null");
                if(funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED,  getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).rejectApply(teamId, userId, rejectReason).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                        return;
                    }
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                }

                @Override
                public void onFailed(int code) {
                    if (code == 509) {
                        result.put("error", "已拒绝");
                    } else {
                        result.put("error", code);
                    }
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_FAILED, DataHelper.gson.toJsonTree(result));
                        return;
                    }
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_FAILED, DataHelper.gson.toJsonTree(result));
                        return;
                    }
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result).toString());
                }
            });
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 修改群名称
     * @param params
     */
    public void updateTeamName(String params []) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;

        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String teamName = jsonObject.optString("teamName");
            updateTeamInfo(TeamFieldEnum.Name, teamId, teamName, funcId);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 修改群介绍(仅限高级群)
     * @param params
     */
    public void updateTeamIntro(String params []) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String intro = jsonObject.optString("intro");
            updateTeamInfo(TeamFieldEnum.Introduce, teamId, intro, funcId);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 修改群公告(仅限高级群)
     * @param params
     */
    public void updateTeamAnnouncement(String params []) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String announcement = jsonObject.optString("announcement");
            updateTeamInfo(TeamFieldEnum.Announcement, teamId, announcement, funcId);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }
    /**
     * 修改群验证方式(仅限高级群)
     * @param params
     */
    public void updateTeamJoinMode(String params []) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String joinMode = jsonObject.optString("joinMode");
            updateTeamInfo(TeamFieldEnum.VerifyType, teamId, joinMode, funcId);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 更新群信息，非对外接口
     * @param fieldType
     * @param teamId
     * @param value
     */
    private void updateTeamInfo(TeamFieldEnum fieldType, String teamId, Serializable value, int funcId) {
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String callbackFunTemp = "";
        String msg = "";
        if (fieldType == TeamFieldEnum.Name) {
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_NAME;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                msg = "teamId or teamName can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result).toString());
                }
                return;
            }
        } else if (fieldType == TeamFieldEnum.Introduce){
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_INTRO;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                msg = "teamId or intro can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result).toString());
                }
                return;
            }
        } else if (fieldType == TeamFieldEnum.Announcement) {
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_ANNOUNCEMENT;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                msg = "teamId or announcement can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result).toString());
                }
                return;
            }
        } else if (fieldType == TeamFieldEnum.VerifyType) {
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_JOIN_MODE;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                msg = "teamId or joinMode can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result).toString());
                }
                return;
            }

            if (Integer.parseInt((String)value) == 0) {
                value = VerifyTypeEnum.Free;
            } else if (Integer.parseInt((String)value) == 1) {
                value = VerifyTypeEnum.Apply;
            } else if (Integer.parseInt((String)value) == 2) {
                value = VerifyTypeEnum.Private;
            }
        }

        final String callbackFun = callbackFunTemp;
        NIMClient.getService(TeamService.class).updateTeam(teamId, fieldType, value).setCallback(
                new RequestCallbackTemplate<Void>("updateTeamInfo", funcId, callbackFun));
    }

    /**
     * 提升管理员
     * @param params
     */
    public void addManagersToTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);

            if (TextUtils.isEmpty(teamId) || users == null || users.length() < 1) {
                String msg = "teamId or users can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_ADD_MANAGER_TO_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }

            List <String> userList = new ArrayList<String>();
            int len = users.length();
            for (int i = 0; i < len; i++) {
                userList.add(users.getString(i));
            }
            NIMClient.getService(TeamService.class).addManagers(teamId, userList).setCallback(
                    new RequestCallbackTemplate<List<TeamMember>>("addManagersToTeam", funcId, JsConst.CALLBACK_ADD_MANAGER_TO_TEAM));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 移除管理员
     * @param params
     */
    public void removeManagersFromTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);

            if (TextUtils.isEmpty(teamId) || users == null || users.length() < 1) {
                String msg = "teamId or users can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_REMOVE_MANAGER_FROM_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }

            List <String> userList = new ArrayList<String>();
            int len = users.length();
            for (int i = 0; i < len; i++) {
                userList.add(users.getString(i));
            }

            NIMClient.getService(TeamService.class).removeManagers(teamId, userList).setCallback(
                    new RequestCallbackTemplate<List<TeamMember>>("removeManagersFromTeam", funcId,JsConst.CALLBACK_REMOVE_MANAGER_FROM_TEAM ));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 转让群(仅限高级群)
     * @param params
     */
    public void transferManagerWithTeam(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String newOwnerId = jsonObject.optString("newOwnerId");
            boolean isLeave = jsonObject.optBoolean("isLeave", false);
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(newOwnerId)) {
                String msg = "teamId or newOwnerId can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_TRANSFER_MANAGER_WITH_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).transferTeam(teamId, newOwnerId, isLeave).setCallback(
                    new RequestCallbackTemplate<List<TeamMember>>("transferManagerWithTeam", funcId, JsConst.CALLBACK_TRANSFER_MANAGER_WITH_TEAM));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 获取群成员
     * 有可能与服务器同步数据，此时网络传输数据量较大
     * @param params
     */
    public void fetchTeamMembers(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                String msg = "teamId can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_MEMBERS, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(
                    new RequestCallbackTemplate<List<TeamMember>>("fetchTeamMembers", funcId, JsConst.CALLBACK_FETCH_TEAM_MEMBERS, new CustomDataUtil<List<TeamMember>>() {
                        @Override
                        public String getDataStr(List<TeamMember> teamMembers) {
                            List<TeamMemberVo> list = new ArrayList<TeamMemberVo>();
                            for (TeamMember teamMember : teamMembers) {
                                TeamMemberVo vo = DataUtil.trans2TeamMemberVo(teamMember);
                                list.add(vo);
                            }
                            result.put("members", list);
                            return new Gson().toJson(result);
                        }
                    }));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 用户退群
     * @param params
     */
    public void quitTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                String msg = "teamId can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_QUIT_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).quitTeam(teamId).setCallback(new RequestCallbackTemplate<Void>("quitTeam", funcId, JsConst.CALLBACK_QUIT_TEAM));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 踢出用户
     * @param params
     */
    public void kickUsers(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            //android只支持一次踢出一个人, 和iOS不一致
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);
            if (users == null || users.length() < 1) {
                String msg = "users can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_KICK_USERS, getJSONFromMap(result).toString());
                }
                return;
            }

            if (TextUtils.isEmpty(teamId)) {
                String msg = "teamId can not be null";
                result.put("error", "teamId can not be null");
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_KICK_USERS, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).removeMember(teamId, users.getString(0)).setCallback(new RequestCallbackTemplate<Void>("kickUsers", funcId, JsConst.CALLBACK_KICK_USERS));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 解散群
     * @param params
     */
    public void dismissTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                String msg = "teamId can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_DISMISS_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).dismissTeam(teamId).setCallback(
                    new RequestCallbackTemplate<Void>("dismissTeam", funcId, JsConst.CALLBACK_DISMISS_TEAM));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 修改群消息通知状态 (关闭群消息提醒)
     * @param params
     */
    public void updateNotifyStateForTeam(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            Boolean notify = jsonObject.optBoolean("notify", true);
            if (TextUtils.isEmpty(teamId)) {
                String msg = "teamId can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_TEAM, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(TeamService.class).muteTeam(teamId, notify).setCallback(new RequestCallbackTemplate<Void>("updateNotifyStateForTeam", funcId, JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_TEAM));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }
    //---------------------------系统消息-----------------------

    /**
     * 获取本地存储的内置系统通知
     * @param params
     */
    public void fetchSystemNotifications(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            int limit = jsonObject.optInt("limit", 10); //默认10条
            List<SystemMessage> list = NIMClient.getService(SystemMessageService.class).querySystemMessagesBlock(0, limit);
            List<SystemMessageVo> voList = new ArrayList<SystemMessageVo>();
            if (list != null && list.size() > 0) {
                for (SystemMessage msg : list) {
                    voList.add(DataUtil.trans2SystemMsgVo(msg));
                }
            }
            result.put("notifications", voList);
            if (funcId != -1) {
                callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(result));
            } else {
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_SYSTEM_NOTIFICATION, new Gson().toJson(result));
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 获取本地存储的内置系统未读数
     * @param params
     */
    public int allNotificationsUnreadCount(String params[]) {
        int count = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock();
        final HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("count", count);
        evaluateRootWindowScript(JsConst.CALLBACK_ALL_NOTIFICATION_UNREAD_COUNT, getJSONFromMap(result).toString());
        return count;
    }

    /**
     * 删除本地存储的全部内置系统通知
     * @param params
     */
    public void deleteAllNotifications(String params[]) {
        NIMClient.getService(SystemMessageService.class).clearSystemMessages();
    }

    /**
     * 标记本地存储的全部内置系统通知为已读
     * @param params
     */
    public boolean markAllNotificationsAsRead(String params[]) {
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
            result.put("result", true);
            evaluateRootWindowScript(JsConst.CALLBACK_MARK_ALL_NOTIFICATIONS_AS_READ, getJSONFromMap(result).toString());
            return true;
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            result.put("result", false);
            evaluateRootWindowScript(JsConst.CALLBACK_MARK_ALL_NOTIFICATIONS_AS_READ, getJSONFromMap(result).toString());
            return false;
        }
    }

    /**
     *  发送自定义通知(客户端)
     * @param params
     */
    public boolean sendCustomNotification(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        String sessionId;
        int sessionType;
        boolean sendToOnlineUsersOnly;
        String content;
        String apnsContent;
        boolean shouldBeCounted;
        boolean apnsEnable;
        boolean apnsWithPrefix;

        try {
            JSONObject jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            sendToOnlineUsersOnly = jsonObject.optBoolean("sendToOnlineUsersOnly", true);
            content = jsonObject.optString("content");
            apnsContent =  jsonObject.optString("apnsContent");
            shouldBeCounted =  jsonObject.optBoolean("shouldBeCounted");
            apnsEnable = jsonObject.optBoolean("apnsEnable");
            apnsWithPrefix =  jsonObject.optBoolean("apnsWithPrefix");

            CustomNotification command = new CustomNotification();
            command.setSessionId(sessionId);
            command.setSessionType(SessionTypeEnum.typeOfValue(sessionType));
            CustomNotificationConfig config = new CustomNotificationConfig();
            config.enablePush = apnsEnable;
            config.enableUnreadCount = shouldBeCounted;
            config.enablePushNick = apnsWithPrefix; //仅针对iOS
            command.setConfig(config);

            command.setContent(content);
            command.setApnsText(apnsContent);
            command.setSendToOnlineUserOnly(sendToOnlineUsersOnly);
            //发送通知
            NIMClient.getService(MsgService.class).sendCustomNotification(command);
            return true;
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            return false;
        }
    }

    //----------------------------------用户资料托管----------------------------
    //获取用户资料
    public void userInfo(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String userId = jsonObject.optString("userId");
            if (TextUtils.isEmpty(userId)) {
                String msg = "userId can not be null";
                result.put("error", msg);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_USER_INFO, getJSONFromMap(result).toString());
                }
                return;
            }
            NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(userId);
            //是否需要消息提醒
            boolean notice = NIMClient.getService(FriendService.class).isNeedMessageNotify(userId);
            //是否在黑名单中
            boolean isInMyBlackList = NIMClient.getService(FriendService.class).isInBlackList(userId);
            result.put("userId", userId);
            result.put("alias", user.getName());
            result.put("notifyForNewMsg", notice);
            result.put("isInMyBlackList", isInMyBlackList);
            UserInfoVo vo = DataUtil.trans2UserInfoVo(user);
            result.put("userInfo",vo);
            if (funcId != -1) {
                callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(result));
            } else {
                evaluateRootWindowScript(JsConst.CALLBACK_USER_INFO, new Gson().toJson(result));
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 获取服务器用户资料
     * @param params
     */
    public void fetchUserInfos(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String usersStr = jsonObject.optString("userIds");
            JSONArray users = new JSONArray(usersStr);
            if (users == null || users.length() < 1) {
                result.put("error", MSG_INVALID_PARAMS);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_USER_INFOS, getJSONFromMap(result).toString());
                }
                return;
            }
            List<String> userIds = new ArrayList<String>();
            for (int i = 0; i < users.length(); i ++) {
                userIds.add(users.getString(i));
            }


            NIMClient.getService(UserService.class).fetchUserInfo(userIds)
                    .setCallback(new RequestCallbackTemplate<List<NimUserInfo>>("fetchUserInfos", funcId,JsConst.CALLBACK_FETCH_USER_INFOS, new CustomDataUtil<List<NimUserInfo>>() {
                        @Override
                        public String getDataStr(List<NimUserInfo> nimUserInfos) {
                            List<UserInfoVo> userInfoVoList = DataUtil.trans2UserInfoVoList(nimUserInfos);
                            result.put("users", userInfoVoList);
                            return new Gson().toJson(result);
                        }
                    }));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 更新当前用户信息
     * @param params
     */
    public void updateMyUserInfo(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String nickName = jsonObject.optString("nickname");
            String avatar = jsonObject.optString("avatar");
            String sign = jsonObject.optString("sign");
            int gender = jsonObject.optInt("gender", 1);
            String email = jsonObject.optString("email");
            String birth = jsonObject.optString("birth");
            String mobile = jsonObject.optString("mobile");
            String ext = jsonObject.optString("ext");
            Map<UserInfoFieldEnum, Object> fields = new HashMap<UserInfoFieldEnum, Object>();
            if (!TextUtils.isEmpty(nickName)) {
                fields.put(UserInfoFieldEnum.Name, nickName);
            }
            if (!TextUtils.isEmpty(avatar)) {
                fields.put(UserInfoFieldEnum.AVATAR, avatar);
            }
            if (!TextUtils.isEmpty(sign)) {
                fields.put(UserInfoFieldEnum.SIGNATURE, sign);
            }
            fields.put(UserInfoFieldEnum.GENDER, gender);
            if (!TextUtils.isEmpty(email)) {
                fields.put(UserInfoFieldEnum.EMAIL, email);
            }
            if (!TextUtils.isEmpty(birth)) {
                fields.put(UserInfoFieldEnum.BIRTHDAY, birth);
            }
            if (!TextUtils.isEmpty(mobile)) {
                fields.put(UserInfoFieldEnum.MOBILE, mobile);
            }
            if (!TextUtils.isEmpty(ext)) {
                fields.put(UserInfoFieldEnum.EXTEND, ext);
            }
            NIMClient.getService(UserService.class).updateUserInfo(fields)
                    .setCallback(new RequestCallbackTemplate<Void>("updateMyUserInfo", funcId, JsConst.CALLBACK_UPDATE_MY_USER_INFO));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    //--------------------------------用户关系托管-----------------------------------------

    /**
     * 获取好友列表
     * @param params
     */
    public void myFriends(String params[]) {
        boolean flag = params.length == 1 && BUtility.isNumeric(params[0]);
        int funcId = flag ? Integer.parseInt(params[0]): -1 ;
        HashMap<String, Object> result = new HashMap<String, Object>();
        List<String> friendAccounts = NIMClient.getService(FriendService.class).getFriendAccounts();
        if (friendAccounts == null || friendAccounts.size() == 0) {
            result.put("users", new JSONObject());
        } else {
            List<NimUserInfo>  userInfos = NIMClient.getService(UserService.class).getUserInfoList(friendAccounts);
            List<UserInfoVo> userInfoVoList = DataUtil.trans2UserInfoVoList(userInfos);
            result.put("users", userInfoVoList);
        }
        if (funcId != -1) {
            callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(result));
        } else {
            evaluateRootWindowScript(JsConst.CALLBACK_MY_FRIENDS, new Gson().toJson(result));
        }

    }

    /**
     * 好友请求
     * 好友请求包括请求添加好友以及同意/拒绝好友请求两种
     * @param params
     */
    public void requestFriend(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String userId = jsonObject.optString("userId");
            int operation = jsonObject.optInt("operation", 1);
            String message = jsonObject.optString("message");
            //1:添加好友(直接添加为好友,无需验证) 2:请求添加好友 3:通过添加好友请求 4:拒绝添加好友请求
            if (operation == 1 || operation == 2) {
                VerifyType verifyType = operation == 1 ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
                NIMClient.getService(FriendService.class).addFriend(new AddFriendData(userId, verifyType, message))
                        .setCallback(new RequestCallbackTemplate<Void>("requestFriend", funcId, JsConst.CALLback_REQUEST_FRIEND));
                return;
            }
            if (operation == 3 || operation == 4) {
                boolean approve = operation == 3 ? true: false;
                NIMClient.getService(FriendService.class).ackAddFriendRequest(userId, approve)
                        .setCallback(new RequestCallbackTemplate<Void>("requestFriend", funcId, JsConst.CALLback_REQUEST_FRIEND));
            }
        } catch (JSONException e) {
            Log.i(TAG, "[requestFriend]" + e.getMessage());
        }
    }

    /**
     * 删除好友
     * @param params
     */
    public void deleteFriend(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String account = jsonObject.optString("userId");
            NIMClient.getService(FriendService.class).deleteFriend(account).setCallback(new RequestCallbackTemplate<Void>("deleteFriend", funcId, JsConst.CALLback_DELETE_FRIEND));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 获取黑名单成员列表
     * @param params
     */
    public void myBlackList(String params[]) {
        boolean flag = params.length == 1 && BUtility.isNumeric(params[0]);
        int funcId = flag ? Integer.parseInt(params[0]) : -1;
        List<String> accounts = NIMClient.getService(FriendService.class).getBlackList();
        final HashMap<String, Object> result = new HashMap<String, Object>();
        if (accounts != null && accounts.size() > 0) {
            List<NimUserInfo> nimUserInfoList = NIMClient.getService(UserService.class).getUserInfoList(accounts);
            List<UserInfoVo> list = DataUtil.trans2UserInfoVoList(nimUserInfoList);
            result.put("users",list);
        } else {
            result.put("users", new Object[0]);
        }
        if (funcId != -1) {
            callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(result));
        } else {
            evaluateRootWindowScript(JsConst.CALLBACK_MY_BLACK_LIST, new Gson().toJson(result));
        }
    }

    /**
     * 添加用户到黑名单
     * @param params
     */
    public void addToBlackList(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String account = jsonObject.optString("userId");
            if (TextUtils.isEmpty(account)) {
                result.put("error", MSG_INVALID_PARAMS);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLback_ADD_TO_BLACK_LIST, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(FriendService.class).addToBlackList(account).setCallback(new RequestCallbackTemplate<Void>("addToBlackList", funcId, JsConst.CALLback_ADD_TO_BLACK_LIST));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 将用户移除黑名单
     * @param params
     */
    public void removeFromBlackBlackList(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String account = jsonObject.optString("userId");
            if (TextUtils.isEmpty(account)) {
                result.put("error", MSG_INVALID_PARAMS);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLback_REMOVE_FROM_BLACK_LIST, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(FriendService.class).removeFromBlackList(account).setCallback(
                    new RequestCallbackTemplate<Void>("removeFromBlackBlackList", funcId, JsConst.CALLback_REMOVE_FROM_BLACK_LIST));
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 判断某用户是否在自己的黑名单中
     * @param params
     */
    public void isUserInBlackList(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String account = jsonObject.optString("userId");
            if (TextUtils.isEmpty(account)) {
                result.put("error", MSG_INVALID_PARAMS);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLback_IS_USER_IN_BLACK_LIST, getJSONFromMap(result).toString());
                }
                return;
            }
            boolean isInBlackList = NIMClient.getService(FriendService.class).isInBlackList(account);
            result.put("result", isInBlackList);
            if (funcId != -1) {
                callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, isInBlackList);
            } else {
                evaluateRootWindowScript(JsConst.CALLback_IS_USER_IN_BLACK_LIST, getJSONFromMap(result).toString());
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     * 获取静音成员列表
     * @param params
     */
    public void myMuteUserList(String params[]) {
        boolean flag = params.length == 1 && BUtility.isNumeric(params[0]);
        int funcId = flag ? Integer.parseInt(params[0]): -1;

        List<String> accountList = NIMClient.getService(FriendService.class).getMuteList();
        HashMap<String, Object> result = new HashMap<String, Object>();
        if (accountList != null && accountList.size() > 0) {
            List<NimUserInfo> nimUserInfoList = NIMClient.getService(UserService.class).getUserInfoList(accountList);
            List<UserInfoVo> userInfoVoList = DataUtil.trans2UserInfoVoList(nimUserInfoList);
            result.put("users", userInfoVoList);
        } else {
            result.put("users", new Object[0]);
        }
        if (funcId != -1) {
            callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, DataHelper.gson.toJsonTree(result));
        } else {
            evaluateRootWindowScript(JsConst.CALLBACK_MY_MUTE_USER_LIST, new Gson().toJson(result));
        }
    }

    /**
     * 设置消息提醒
     * @param params
     */
    public void updateNotifyStateForUser(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;

        try {
            JSONObject jsonObject = new JSONObject(json);
            String account = jsonObject.optString("userId");
            boolean notify = jsonObject.optBoolean("notify", true);
            if (TextUtils.isEmpty(account)) {
                result.put("error", MSG_INVALID_PARAMS);
                if(funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_USER, getJSONFromMap(result).toString());
                }
                return;
            }
            NIMClient.getService(FriendService.class).setMessageNotify(account, notify);
            if (funcId != -1) {
                callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
            } else {
                evaluateRootWindowScript(JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_USER, "{}");
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    /**
     *  判断是否需要消息通知
     * @param params
     */
    public void notifyForNewMsgForUser(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String account = jsonObject.optString("userId");
            if (TextUtils.isEmpty(account)) {
                result.put("error",  MSG_INVALID_PARAMS);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_NOTIFY_FOR_NEW_MSG_FOR_USER, getJSONFromMap(result).toString());
                }
                return;
            }
            boolean notice = NIMClient.getService(FriendService.class).isNeedMessageNotify(account);
            result.put("result", notice);
            if (funcId != -1) {
                callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, notice);
            } else {
                evaluateRootWindowScript(JsConst.CALLBACK_NOTIFY_FOR_NEW_MSG_FOR_USER, getJSONFromMap(result).toString());
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }


    interface CustomDataUtil<T> {
        String getDataStr(T t);
    }

    class RequestCallbackTemplate <T> implements RequestCallback<T> {
        private String callBackFun;
        private String funName;
        private CustomDataUtil util;
        private int funcId; // 4.0的function传入

        public RequestCallbackTemplate (String funName, int funcId, String callBackFun, CustomDataUtil util) {
            this.funName = funName;
            this.callBackFun = callBackFun;
            this.util = util;
            this.funcId = funcId;
        }

        public RequestCallbackTemplate (String funName, int funcId, String callBackFun) {
            this.funName = funName;
            this.callBackFun = callBackFun;
            this.funcId = funcId;
        }
        @Override
        public void onSuccess(T t) {
            if (t instanceof  Void) {
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                } else {
                    evaluateRootWindowScript(callBackFun, "{}");
                }
            } else {
                if (util == null) {
                    if (funcId != -1) {
                        callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS);
                    } else {
                        evaluateRootWindowScript(callBackFun, "{}");
                    }
                } else {
                    if (funcId != -1) {
                        try {
                            callbackToJs(funcId, false, EUExCallback.F_C_SUCCESS, new JSONObject(util.getDataStr(t)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        evaluateRootWindowScript(callBackFun, util.getDataStr(t));
                    }
                }
            }
        }

        @Override
        public void onFailed(int code ) {
            Log.i(TAG, "[" + funName + "]fail code:" + code);
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("error", code);
            if (funcId != -1) {
                callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
            } else {
                evaluateRootWindowScript(callBackFun, getJSONFromMap(result).toString());
            }
        }

        @Override
        public void onException(Throwable throwable) {
            Log.i(TAG, "[" + funName + "]" + throwable.getMessage());
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("error", throwable.getMessage());
            if (funcId != -1) {
                callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
            } else {
                evaluateRootWindowScript(callBackFun, getJSONFromMap(result).toString());
            }
        }
    }

    //－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－聊天室相关－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
    public void enterChatRoom(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            String roomId = jsonObject.optString("roomId");
            String nickName = jsonObject.optString("nickName");
            String avatar = jsonObject.optString("avatar");
            JSONObject extension = jsonObject.optJSONObject("extension"); //用户扩展字段
            JSONObject notifyExtension = jsonObject.optJSONObject("notifyExtension");//通知扩展字段
            if (TextUtils.isEmpty(roomId)) {
                result.put("error", MSG_ROOM_ID_EMPTY);
                if (funcId != -1) {
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    evaluateRootWindowScript(JsConst.CALLBACK_ENTER_CHATROOM, getJSONFromMap(result).toString());
                }
                return;
            }
            EnterChatRoomData data = new EnterChatRoomData(roomId);
            data.setNick(nickName);
            data.setAvatar(avatar);
            if (extension != null) {
                data.setExtension(getMapFromJSON(extension));
            }
            if (notifyExtension != null) {
                data.setNotifyExtension(getMapFromJSON(notifyExtension));
            }
            NIMClient.getService(ChatRoomService.class).enterChatRoom(data).setCallback(
                    new RequestCallbackTemplate<Void>("enterChatRoom", funcId, JsConst.CALLBACK_ENTER_CHATROOM));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void exitChatRoom(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String json = params[0];
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(json);
            String roomId = jsonObject.optString("roomId");
            if (TextUtils.isEmpty(roomId)) {
                result.put("error", MSG_ROOM_ID_EMPTY);
                evaluateRootWindowScript(JsConst.CALLBACK_EXIT_CHATROOM, getJSONFromMap(result).toString());
                return;
            }
            NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getChatRoomHistoryMsg(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId = null;
        long startTime = 0;
        int limit = 10;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            startTime = jsonObject.optLong("startTime", 0);
            limit = jsonObject.optInt("limit", 10);
            if (TextUtils.isEmpty(roomId)) {
                if (funcId != -1) {
                    final HashMap<String, Object> result = new HashMap<String, Object>();
                    result.put("error", MSG_ROOM_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }

        NIMClient.getService(ChatRoomService.class).pullMessageHistory(roomId, startTime, limit)
                .setCallback(new RequestCallbackTemplate<List<ChatRoomMessage>>("getChatRoomHistoryMsg", funcId, JsConst.CALLBACK_GET_CHATROOM_HISTORY_MSG, new CustomDataUtil<List<ChatRoomMessage>>() {
                    @Override
                    public String getDataStr(List<ChatRoomMessage> messages) {
                        if (messages != null) {
                            List<ChatRoomMessageVo> list = new ArrayList<ChatRoomMessageVo>();
                            for (ChatRoomMessage msg : messages) {
                                ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(msg);
                                list.add(vo);
                            }
                            HashMap<String, Object> result = new HashMap<String, Object>();
                            result.put("messages", list);
                            return new Gson().toJson(result);
                        } else {
                            HashMap<String, Object> result = new HashMap<String, Object>();
                            result.put("messages", new JSONObject());
                            return new Gson().toJson(result);
                        }
                    }
                }));
    }

    //获取聊天室基本信息
    public void getChatRoomInfo(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId = null;

        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            if (TextUtils.isEmpty(roomId)) {
                if (funcId != -1) {
                    result.put("error", MSG_ROOM_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, "roomId is empty !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        NIMClient.getService(ChatRoomService.class)
                .fetchRoomInfo(roomId).setCallback(new RequestCallbackTemplate<ChatRoomInfo>("getChatRoomInfo", funcId, JsConst.CALLBACK_GET_CHATROOM_INFO, new CustomDataUtil<ChatRoomInfo>() {
            @Override
            public String getDataStr(ChatRoomInfo chatRoomInfo) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                ChatRoomInfoVo vo = DataUtil.trans2ChatRoomInfoVo(chatRoomInfo);
                result.put("data", vo);
                return new Gson().toJson(result);
            }
        }));
    }

    //获取聊天室成员
    public void getChatRoomMembers(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId = null;
        int type = 0; // 0 : Normal, 1: Guest, 2:Online normal
        long time = 0;
        int limit = 10;


        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            if (TextUtils.isEmpty(roomId)) {
                if (funcId != -1) {
                    result.put("error", MSG_ROOM_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, "roomId is empty !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            type = jsonObject.optInt("type", 0);
            time = jsonObject.optLong("time", 0);
            limit = jsonObject.optInt("limit", 10);
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        NIMClient.getService(ChatRoomService.class)
                .fetchRoomMembers(roomId, MemberQueryType.typeOfValue(type), time, limit)
                .setCallback(new RequestCallbackTemplate<List<ChatRoomMember>>("getChatRoomMembers", funcId, JsConst.CALLBACK_GET_CHATROOM_MEMBERS, new CustomDataUtil<List<ChatRoomMember>>() {
                    @Override
                    public String getDataStr(List<ChatRoomMember> chatRoomMembers) {
                        List<ChatRoomMemberVo> list = DataUtil.trans2ChatRoomMembers(chatRoomMembers);
                        HashMap<String, Object> result = new HashMap<String, Object>();
                        result.put("data", list);
                        return new Gson().toJson(result);
                    }
                }));
    }

    //通过用户 id 批量获取指定成员在聊天室中的信息
    public void getChatRoomMembersByIds(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId = null;
        JSONArray ids = null;
        List<String> accountList = new ArrayList<String>();
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");

            String usersStr = jsonObject.optString("userIds");
            ids = new JSONArray(usersStr);

            if (TextUtils.isEmpty(roomId)) {
                if (funcId != -1) {
                    result.put("error", MSG_ROOM_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, "roomId is empty !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (ids == null) {
                if (funcId != -1) {
                    result.put("error", "user id list is empty !");
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, "user id list is empty !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            for (int i = 0; i < ids.length(); i++) {
                accountList.add(ids.getString(i));
            }
        } catch (JSONException e) {
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }

         NIMClient.getService(ChatRoomService.class)
                .fetchRoomMembersByIds(roomId, accountList).setCallback(new RequestCallbackTemplate<List<ChatRoomMember>>("getChatRoomMembersByIds", funcId,
                 JsConst.CALLBACK_GET_CHATROOM_MEMBERS_BY_IDS, new CustomDataUtil<List<ChatRoomMember>>() {
             @Override
             public String getDataStr(List<ChatRoomMember> chatRoomMembers) {
                 List<ChatRoomMemberVo> list = DataUtil.trans2ChatRoomMembers(chatRoomMembers);
                 HashMap<String, Object> result = new HashMap<String, Object>();
                 result.put("data", list);
                 return new Gson().toJson(result);
             }
         }));
    }

    @Override
    public void onChatRoomStatusChanged(ChatRoomStatusChangeData data) {
        Log.i(TAG, "chat room online status changed to " + data.status.name());
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("roomId", data.roomId);
        int status = 0;
        if (data.status == StatusCode.CONNECTING) {
            Log.i(TAG, "ChatRoom:" + "连接中...");
        } else if (data.status == StatusCode.LOGINING) {
            Log.i(TAG, "ChatRoom:" + "连接中...");
            status = 0;
        } else if (data.status == StatusCode.LOGINED) {
            Log.i(TAG, "ChatRoom:" + "登录成功...");
            status = 1;
        } else if (data.status == StatusCode.UNLOGIN) {
            Log.i(TAG, "ChatRoom:" + "UNLOGIN...");
            status = 2;
        } else if (data.status == StatusCode.NET_BROKEN) {
            status = 3;
        }
        result.put("status", status);
        evaluateRootWindowScript(JsConst.ON_CHATROOM_STATUS_CHANGE, getJSONFromMap(result).toString().toString());
    }

    public void addUserToBlackList(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId;
        String account;
        boolean isAdd;

        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            account = jsonObject.optString("userId");
            isAdd = jsonObject.optBoolean("isAdd", true);

            if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(account)) {
                if (funcId != -1) {
                    result.put("error", "roomId or account is empty !");
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, "roomId or account is empty !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        MemberOption option = new MemberOption(roomId, account);
        NIMClient.getService(ChatRoomService.class)
                .markChatRoomBlackList(isAdd, option)
                .setCallback(new RequestCallbackTemplate<ChatRoomMember>("addUserToBlackList", funcId, JsConst.CALLBACK_ADD_USER_TO_BLACK_LIST));
    }

    public void muteUser(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId;
        String account;
        boolean isMute;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            account = jsonObject.optString("userId");
            isMute = jsonObject.optBoolean("isMute", true);

            if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(account)) {
                if (funcId != -1) {
                    result.put("error", MSG_ROOM_ID_OR_USER_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, MSG_ROOM_ID_OR_USER_ID_EMPTY, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        MemberOption option = new MemberOption(roomId, account);
        NIMClient.getService(ChatRoomService.class)
                .markChatRoomMutedList(isMute, option)
                .setCallback(new RequestCallbackTemplate<ChatRoomMember>("muteUser", funcId, JsConst.CALLBACK_MUTE_USER));
    }

    public void setAdmin(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId;
        String account;
        boolean isAdmin;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            account = jsonObject.optString("userId");
            isAdmin = jsonObject.optBoolean("isAdmin", true);

            if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(account)) {
                if (funcId != -1) {
                    result.put("error", MSG_ROOM_ID_OR_USER_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, MSG_ROOM_ID_OR_USER_ID_EMPTY, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        MemberOption option = new MemberOption(roomId, account);
        NIMClient.getService(ChatRoomService.class)
                .markChatRoomManager(isAdmin, option)
                .setCallback(new RequestCallbackTemplate<ChatRoomMember>("setAdmin", funcId, JsConst.CALLBACK_SET_ADMIN));
    }

    public void setNormal(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId;
        String account;
        boolean isNormal;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            account = jsonObject.optString("userId");
            isNormal = jsonObject.optBoolean("isNormal", true);

            if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(account)) {
                if (funcId != -1) {
                    result.put("error", MSG_ROOM_ID_OR_USER_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, MSG_ROOM_ID_OR_USER_ID_EMPTY, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        MemberOption option = new MemberOption(roomId, account);
        NIMClient.getService(ChatRoomService.class)
                .markNormalMember(isNormal, option)
                .setCallback(new RequestCallbackTemplate<ChatRoomMember>("setNormal", funcId, JsConst.CALLBACK_SET_NORMAL));
    }
    //踢出聊天室
    public void kickMemberFromChatRoom(String [] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String roomId;
        String account;
        String reason;
        boolean flag = params.length == 2 && BUtility.isNumeric(params[1]);
        int funcId = flag ? Integer.parseInt(params[1]) : -1;
        final HashMap<String, Object> result = new HashMap<String, Object>();

        try {
            jsonObject = new JSONObject(json);
            roomId = jsonObject.optString("roomId");
            account = jsonObject.optString("userId");
            reason = jsonObject.optString("reason");

            if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(account)) {
                if (funcId != -1) {
                    result.put("error", MSG_ROOM_ID_OR_USER_ID_EMPTY);
                    callbackToJs(funcId, false, EUExCallback.F_C_FAILED, getJSONFromMap(result));
                } else {
                    Toast.makeText(mContext, MSG_ROOM_ID_OR_USER_ID_EMPTY, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> reasonMap = new HashMap<String, Object>();
        reasonMap.put("reason", reason);
        NIMClient.getService(ChatRoomService.class)
                .kickMember(roomId, account, reasonMap)
                .setCallback(new RequestCallbackTemplate<Void>("kickMemberFromChatRoom", funcId, JsConst.CALLBACK_KICK_MEMBER_FROM_CHAT_ROOM));
    }

    @Override
    public void onChatRoomKickOutEvent(ChatRoomKickOutEvent chatRoomKickOutEvent) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("roomId", chatRoomKickOutEvent.getRoomId());
        result.put("code", chatRoomKickOutEvent.getReason().getValue());
        evaluateRootWindowScript(JsConst.ON_CHAT_ROOM_KICK_OUT_EVENT, getJSONFromMap(result).toString().toString());
    }

    public String getRealPath(String path){
        String realPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), path),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
        Log.i(TAG, "getRealPath:" + fileName);

        //先将assets文件写入到临时文件夹中
        if (path.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            //为res对应的文件生成一个临时文件到系统中
            File dir = new File(Environment.getExternalStorageDirectory(),
                    File.separator + TEMP_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            } else {
                //及时清理这个缓存文件夹中的数据
                for (File file: dir.listFiles()) {
                    file.delete();
                }
            }
            File destFile = new File(dir, fileName);
            try {
                destFile.deleteOnExit();
                destFile.createNewFile();
            } catch (IOException e) {
                Log.i(TAG, "[Create File]" +  e.getMessage());
                return null;
            }
            if (realPath.startsWith("/data")){
                CommonUtil.copyFile(new File(realPath), destFile);
                return destFile.getAbsolutePath();
            } else if( CommonUtil.saveFileFromAssetsToSystem(mContext, realPath, destFile)) {
                return destFile.getAbsolutePath();
            } else {
                Log.i(TAG, "[getRealPath error]");
                return null;
            }
        } else {
            return realPath;
        }
    }


    private JSONObject getJSONFromMap(Map<String, Object> result) {
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
        return jsonObject;
    }

    private Map<String, Object> getMapFromJSON(JSONObject jsonObject) throws JSONException{
        if (jsonObject == null) {
            return null;
        }
        Iterator<String> keys = jsonObject.keys();
        HashMap<String, Object> map = new HashMap<String, Object>();
        while(keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonObject.get(key));
        }
        return map;
    }

    private static SDKOptions getOptions(Context context) {
        SDKOptions options = new SDKOptions();

        // 配置保存图片，文件，log等数据的目录
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;
        // 配置数据库加密秘钥
        options.databaseEncryptKey = "NETEASE";
        // 配置是否需要预下载附件缩略图
        options.preloadAttach = true;
        // 配置附件缩略图的尺寸大小。表示向服务器请求缩略图文件的大小
        // 该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        options.thumbnailSize = 480 / 2;

        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = new UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return null;
            }

            @Override
            public int getDefaultIconResId() {
                return 0;
            }

            @Override
            public Bitmap getTeamIcon(String tid) {
                return null;
            }

            @Override
            public Bitmap getAvatarForMessageNotifier(String account) {
                return null;
            }

            @Override
            public String getDisplayNameForMessageNotifier(String account, String sessionId,
                                                           SessionTypeEnum sessionType) {
                return null;
            }
        };
        return options;
    }

    private static LoginInfo getLoginInfo(Context context){
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(context, "uexNIM");
        String account = sharedPreferencesHelper.getUserAccount();
        String token = sharedPreferencesHelper.getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);

    }
    private void evaluateRootWindowScript(String methodName, String jsonData) {
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        evaluateScript("root", 0, js);
//        if (methodName.startsWith("uexNIM.cb")) {
//            rootEBrowserView.loadUrl(js);
//        }
    }

    @Override
    protected boolean clean() {
        return false;
    }

    /**
     * 消息发送进度监听，文本消息会没有这个回调
     * @param result  返回的数据，map类型会转成string返回给前端
     */
    @Override
    public void onSendMessageWithProgress(Map<String, Object> result) {
       evaluateRootWindowScript(JsConst.ON_SEND_MESSAGE_WITH_PROGRESS, getJSONFromMap(result).toString());
    }

    @Override
    public void onReceivedMessages(List<IMMessage> messages) {
        Log.i(TAG, "[onReceivedMessages]");
        List<MessageVo> list = new ArrayList<MessageVo>();
        for (IMMessage message : messages) {
            list.add(DataUtil.trans2MessageVo(message));
        }

        evaluateRootWindowScript(JsConst.ON_RECIEVED_MESSAGE, new Gson().toJson(list));
    }

    @Override
    public void onMessageStatusChange(IMMessage msg) {

    }

    @Override
    public void onUpdateRecentSession(List<RecentContact> contacts) {

    }

    @Override
    public void onTeamRemoved(Team team) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        TeamVo vo = DataUtil.trans2TeamVo(team);
        result.put("team", vo);
        evaluateRootWindowScript(JsConst.ON_TEAM_REMOVED, new Gson().toJson(result));
    }

    @Override
    public void onTeamUpdated(List<Team> teams) {

        List<TeamVo> list = new ArrayList<TeamVo>();
        for (Team team : teams) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("team", DataUtil.trans2TeamVo(team));
            evaluateRootWindowScript(JsConst.ON_TEAM_UPDATED, new Gson().toJson(result));
        }

     }

    @Override
    public void onTeamMemberChanged(List<TeamMember> members) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        List<TeamMemberVo> list = new ArrayList<TeamMemberVo>();
        for (TeamMember member : members) {
            list.add(DataUtil.trans2TeamMemberVo(member));
        }
        result.put("data", list);
        evaluateRootWindowScript(JsConst.ON_TEAM_MEMBER_CHANGED, new Gson().toJson(result));
    }

    @Override
    public void onSystemMessageRecieved(SystemMessage message) {
        SystemMessageVo vo = DataUtil.trans2SystemMsgVo(message);
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("notification", vo);
        evaluateRootWindowScript(JsConst.ON_RECIEVED_SYSTEM_NOTIFICATION, new Gson().toJson(result));
    }

    @Override
    public void onReceivedCustomNotification(CustomNotification customNotification) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        CustomNotificationVo vo = DataUtil.trans2CustomNotificationVo(customNotification);
        result.put("notification", vo);
        System.out.println("-------->" + DataHelper.gson.toJson(result));
        evaluateRootWindowScript(JsConst.ON_RECIEVED_CUSTOM_SYSTEM_NOTIFICATION, new Gson().toJson(result));
    }

    @Override
    public void onUserInfoUpdate(List<NimUserInfo> users) {
        for(NimUserInfo user: users) {
            UserInfoVo vo = DataUtil.trans2UserInfoVo(user);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("user", vo);
            evaluateRootWindowScript(JsConst.ON_USER_INFO_UPDATE, new Gson().toJson(map));
        }
    }

    @Override
    public void onReceivedChatRoomMessage(List<ChatRoomMessage> messages) {
        List<ChatRoomMessageVo> list = new ArrayList<ChatRoomMessageVo>();
        for (ChatRoomMessage msg : messages) {
            ChatRoomMessageVo vo = DataUtil.trans2ChatRoomMessageVo(msg);
            list.add(vo);
        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        Log.i(TAG, "[Receive Msg]" + new Gson().toJson(list));
        evaluateRootWindowScript(JsConst.ON_RECIEVED_MESSAGE, new Gson().toJson(list));
    }

    @Override
    public void onBlackListChanged(BlackListChangedNotify blackListChangedNotify) {
        evaluateRootWindowScript(JsConst.ON_BLACK_LIST_CHANGED, null);
    }
}