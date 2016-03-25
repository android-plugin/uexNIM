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
import com.google.gson.reflect.TypeToken;
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
import com.netease.nimlib.sdk.media.player.AudioPlayer;
import com.netease.nimlib.sdk.media.player.OnPlayListener;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexnim.util.ContactHttpClient;
import org.zywx.wbpalmstar.plugin.uexnim.util.DataUtil;
import org.zywx.wbpalmstar.plugin.uexnim.util.MD5;
import org.zywx.wbpalmstar.plugin.uexnim.util.NIMConstant;
import org.zywx.wbpalmstar.plugin.uexnim.util.SharedPreferencesHelper;
import org.zywx.wbpalmstar.plugin.uexnim.vo.MessageVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.OnlineClientVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamMemberVo;
import org.zywx.wbpalmstar.plugin.uexnim.vo.TeamVo;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EUExNIM extends EUExBase implements ListenerRegister.ListenersCallback {
    private static final String TAG = "EUExNIM";
    private AbortableFuture<LoginInfo> loginRequest;

    private final String MSG_TEAM_ID_EMPTY = "teamId can not be null";
    private ResoureFinder finder;

    public EUExNIM(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
        finder = ResoureFinder.getInstance(context);
    }

    public void registerApp(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String appKey = null;
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            if (TextUtils.isEmpty(jsonObject.optString("appKey"))) {
                result.put("result", false);
                result.put("error", 1);
                evaluateRootWindowScript(JsConst.CALLBACK_REGISTER_APP, getJSONFromMap(result));
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
            evaluateRootWindowScript(JsConst.CALLBACK_REGISTER_APP, getJSONFromMap(result));
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
                result.put("error", "userId or password is empty !");
                evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result));
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
                Log.i(TAG, "login onSuccess---->");
                result.put("result", true);
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
                evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result));
            }

            @Override
            public void onFailed(int code) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("result", false);
                result.put("error", code);
                result.put("userId", "");
                evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result));
            }

            @Override
            public void onException(Throwable throwable) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("result", false);
                result.put("error", -1); //system error
                evaluateRootWindowScript(JsConst.CALLBACK_LOGIN, getJSONFromMap(result));
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
        evaluateRootWindowScript(JsConst.ON_KICK, getJSONFromMap(result));
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
            result.put("clients", result);
            evaluateRootWindowScript(JsConst.ON_MULTI_LOGIN_CLIENTS_CHANGED, getJSONFromMap(result));
        }
    }

    //退出操作
    public void logout(String params[]) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            NIMClient.getService(AuthService.class).logout();
            result.put("result", true);
            evaluateRootWindowScript(JsConst.CALLBACK_LOGOUT, getJSONFromMap(result));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("result", false);
            result.put("error", e.toString());
            evaluateRootWindowScript(JsConst.CALLBACK_LOGOUT, getJSONFromMap(result));
        }
    }
    //注册用户，仅供测试时使用
    public void registerUser(String params[]) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        String json = params[0];
        JSONObject jsonObject;
        String account = null;
        String nickName = null;
        String password = null;
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            account = jsonObject.optString("userId");
            nickName = jsonObject.optString("nickname");
            password = jsonObject.optString("password");
            password = MD5.getStringMD5(password);

            if (TextUtils.isEmpty(account)
                    || TextUtils.isEmpty(password)) {
                result.put("result", false);
                result.put("error", "userId or password is empty !");
                evaluateRootWindowScript(JsConst.CALLBACK_REGISTER_USER, getJSONFromMap(result));
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            return;
        }

        ContactHttpClient.getInstance().register(mContext, account, nickName, password, new ContactHttpClient.ContactHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                result.put("result", true);
                evaluateRootWindowScript(JsConst.CALLBACK_REGISTER_USER, getJSONFromMap(result));
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                result.put("result", false);
                result.put("error", "code:" + code + "   msg:" + errorMsg);
                evaluateRootWindowScript(JsConst.CALLBACK_REGISTER_USER, getJSONFromMap(result));
            }
        });
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
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            content = jsonObject.optString("content");
            if (TextUtils.isEmpty(sessionId)) {
                Toast.makeText(mContext, "sessionId is empty !", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionType != 0 && sessionType != 1) {
                Toast.makeText(mContext, "Invalid sessionType !", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        final IMMessage message = MessageBuilder.createTextMessage(sessionId, SessionTypeEnum.typeOfValue(sessionType), content);
        Log.i(TAG, "message id:" + message.getUuid());
        willSendMsgCallback(sessionId, content, sessionType, message);
        Log.i("messageId", message.getUuid());
        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(sendMsgCallback);
    }

    private void willSendMsgCallback(String sessionId, String content, int sessionType, IMMessage message) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(NIMConstant.TEXT_MESSAGE_ID, message.getUuid());
        map.put(NIMConstant.TEXT_MESSAGE_TYPE, NIMConstant.MESSAGE_TYPE_TEXT);
        map.put(NIMConstant.TEXT_SESSION_ID, sessionId);
        map.put(NIMConstant.TEXT_SESSION_TYPE, sessionType);
        if (content != null) {
            map.put(NIMConstant.TEXT_CONTENT, content);
        }
        evaluateRootWindowScript(JsConst.WILL_SEND_MESSAGE, getJSONFromMap(map));
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
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                Toast.makeText(mContext, "sessionId or filePath is empty !", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionType != 0 && sessionType != 1) {
                Toast.makeText(mContext, "Invalid sessionType !", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        // 创建图片消息
        final IMMessage message = MessageBuilder.createImageMessage(
                sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                new File(getRealPath(filePath)), // 图片文件对象
                null // 文件显示名字，如果第三方 APP 不关注，可以为 null
        );
        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(sendMsgCallback);
        willSendMsgCallback(sessionId, null, sessionType, message);
    }


    private RequestCallback sendMsgCallback = new RequestCallback<Void>() {
        @Override
        public void onSuccess(Void avoid) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("result", true);
            evaluateRootWindowScript(JsConst.CALLBACK_DID_SEND_MESSAGE, getJSONFromMap(result));
        }

        @Override
        public void onFailed(int code) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("result", false);
            result.put("error", code);
            evaluateRootWindowScript(JsConst.CALLBACK_DID_SEND_MESSAGE, getJSONFromMap(result));
        }

        @Override
        public void onException(Throwable throwable) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("result", false);
            result.put("error", throwable.getMessage());
            evaluateRootWindowScript(JsConst.CALLBACK_DID_SEND_MESSAGE, getJSONFromMap(result));

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
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            latitude = jsonObject.optDouble("latitude");
            longitude = jsonObject.optDouble("longitude");
            title = jsonObject.optString("title");

            if (TextUtils.isEmpty(sessionId)) {
                Toast.makeText(mContext, "sessionId is empty !", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionType != 0 && sessionType != 1) {
                Toast.makeText(mContext, "Invalid sessionType !", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        // 发送其他类型的消息代码类似，演示如下
        // 创建地理位置消息
        IMMessage message = MessageBuilder.createLocationMessage(
                sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                latitude, // 纬度
                longitude, // 经度
                title // 地址信息描述
        );
        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(sendMsgCallback);
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
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                Toast.makeText(mContext, "sessionId or filePath is empty !", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionType != 0 && sessionType != 1) {
                Toast.makeText(mContext, "Invalid sessionType !", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        String realPath = getRealPath(filePath);
        File file = new File(realPath);
        MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(realPath));
        int duration = mp.getDuration();
        // 创建音频消息
        IMMessage message = MessageBuilder.createAudioMessage(
                sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                file, // 音频文件
                duration // 音频持续时间，单位是ms
        );
        NIMClient.getService(MsgService.class).sendMessage(message, false);
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
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                Toast.makeText(mContext, "sessionId or filePath is empty !", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionType != 0 && sessionType != 1) {
                Toast.makeText(mContext, "Invalid sessionType !", Toast.LENGTH_SHORT).show();
                return;
            }
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
        // 创建视频消息
        IMMessage message = MessageBuilder.createVideoMessage(
                sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                file, // 视频文件
                duration, // 视频持续时间
                width, // 视频宽度
                height, // 视频高度
                null // 视频显示名，可为空
        );
        NIMClient.getService(MsgService.class).sendMessage(message, false);
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
        try {
            jsonObject = new JSONObject(json);
            sessionId = jsonObject.optString("sessionId");
            sessionType = jsonObject.optInt("sessionType");
            filePath = jsonObject.optString("filePath");
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(filePath)) {
                Toast.makeText(mContext, "sessionId or filePath is empty !", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionType != 0 && sessionType != 1) {
                Toast.makeText(mContext, "Invalid sessionType !", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(getRealPath(filePath));
        IMMessage message = MessageBuilder.createFileMessage(
                sessionId, // 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
                SessionTypeEnum.typeOfValue(sessionType), // 聊天类型，单聊或群组
                file,
                file.getName());
        NIMClient.getService(MsgService.class).sendMessage(message, false);
    }

    //获取最近会话
    public void allRecentSession(String params[]) {
        NIMClient.getService(MsgService.class).queryRecentContacts().setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
            @Override
            public void onResult(int code, List<RecentContact> recents, Throwable e) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                if (code != ResponseCode.RES_SUCCESS || recents == null) {
                    map.put(NIMConstant.TEXT_RESULT, false);
                    map.put(NIMConstant.TEXT_ERROR, "无最近会话消息");
                    evaluateRootWindowScript(JsConst.CALLBACK_ALL_RECENT_SESSION, getJSONFromMap(map));
                    return;
                }
                if (recents.size() == 0) {
                    map.put(NIMConstant.TEXT_UNREAD_COUNT, 0);
                } else {
                    map.put(NIMConstant.TEXT_LAST_MESSAGE, recents.get(recents.size() - 1).getRecentMessageId());
                    int unreadNum = 0;
                    for (RecentContact r : recents) {
                        unreadNum += r.getUnreadCount();
                    }
                    map.put(NIMConstant.TEXT_UNREAD_COUNT, unreadNum);
                }

                evaluateRootWindowScript(JsConst.CALLBACK_ALL_RECENT_SESSION, getJSONFromMap(map));
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
                for (IMMessage msg:imMessages) {
                    list.add(DataUtil.trans2MessageVo(msg));
                }
                result.put("messages", new Gson().toJson(list));
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_MESSAGE_HISTORY, getJSONFromMap(result));
            }

            @Override
            public void onFailed(int code) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", code);
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_MESSAGE_HISTORY, getJSONFromMap(result));

            }

            @Override
            public void onException(Throwable throwable) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", throwable.getMessage());
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_MESSAGE_HISTORY, getJSONFromMap(result));
            }
        });
    }

     /**
     *切换音频的输出设备
     */
    public void switchAudioOutputDevice(String params []) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
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
        AudioPlayer player = new AudioPlayer(mContext);
        if(outputDevice == 0) {
            player.start(AudioManager.STREAM_VOICE_CALL);
        } else {
            player.start(AudioManager.STREAM_MUSIC);
        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("result", true);
        evaluateRootWindowScript(JsConst.CALLBACK_SWITCH_AUTIO_OUTPUT_DEVICE, getJSONFromMap(result));
    }

    public void isPlaying(String params[]) {
        AudioPlayer player = new AudioPlayer(mContext);
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("result", player.isPlaying());
        evaluateRootWindowScript(JsConst.CALLBACK_IS_PLAYING, getJSONFromMap(result));
    }

    public void playAudio(String params[]) {
        AudioPlayer player = new AudioPlayer(mContext);
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

        player.setDataSource(getRealPath(filePath));
        player.setOnPlayListener(new OnPlayListener() {
            @Override
            public void onPrepared() {
                evaluateRootWindowScript(JsConst.CALLBACK_BEGAN_PLAY_AUDIO, null);
            }

            @Override
            public void onCompletion() {
                evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_PLAY_AUDIO, null);
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
        player.start(AudioManager.STREAM_VOICE_CALL); //默认由听筒播放
    }

    /**
     * 停止播放音频
     * @param params
     */
    public void stopPlay(String params[]) {
        AudioPlayer player = new AudioPlayer(mContext);
        player.stop();
        player.setOnPlayListener(new OnPlayListener() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onCompletion() {
                evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_PLAY_AUDIO, null);
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
    }
    /**
     * 判断是否正在录制音频
     * @param params
     */
    public void isRecording(String params[]) {
        AudioRecorder record = new AudioRecorder(mContext, RecordType.AAC, 100, new IAudioRecordCallback() {
            @Override
            public void onRecordReady() {

            }

            @Override
            public void onRecordStart(File file, RecordType recordType) {

            }

            @Override
            public void onRecordSuccess(File file, long l, RecordType recordType) {

            }

            @Override
            public void onRecordFail() {

            }

            @Override
            public void onRecordCancel() {

            }

            @Override
            public void onRecordReachedMaxTime(int i) {

            }
        });
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("result", record.isRecording());
        evaluateRootWindowScript(JsConst.CALLBACK_IS_RECORDING, getJSONFromMap(result));
    }

    /**
     *
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
        float updateTime;
        final HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            jsonObject = new JSONObject(json);
            duration = jsonObject.optInt("duration");
            updateTime = jsonObject.optInt("updateTime");
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
            Toast.makeText(mContext, "JSON解析错误", Toast.LENGTH_SHORT).show();
        }
        AudioRecorder recorder = new AudioRecorder(mContext, RecordType.AAC, duration, new IAudioRecordCallback() {
            private File audioFile = null;
            @Override
            public void onRecordReady() {

            }

            @Override
            public void onRecordStart(File file, RecordType recordType) {
                audioFile = file;
                result.put("filePath", file.getAbsolutePath());
                evaluateRootWindowScript(JsConst.CALLBACK_BEGAN_RECORD_AUDIO,getJSONFromMap(result));
            }

            @Override
            public void onRecordSuccess(File file, long l, RecordType recordType) {
               Log.i(TAG, "[record audio success]");

            }

            @Override
            public void onRecordFail() {

            }

            @Override
            public void onRecordCancel() {

            }

            @Override
            public void onRecordReachedMaxTime(int i) {
                result.put("filePath", audioFile.getAbsolutePath());
                evaluateRootWindowScript(JsConst.CALLBACK_COMPLETED_RECORD_AUDIO,getJSONFromMap(result));
            }
        });
        recorder.startRecord();
    }

    //-----------------------------------------------------群组相关-----------------------------------------------

    /**
     * 获取我的所有群组
     * @param params
     */
    public void allMyTeams(String params[]) {
        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> teams) {
                HashMap<String, Object> result = new HashMap<String, Object>();
                List<TeamVo> list = new ArrayList<TeamVo>();
                for (Team team: teams) {
                    TeamVo vo = DataUtil.trans2TeamVo(team);
                    list.add(vo);
                }
                result.put("teams", new Gson().toJson(list, new TypeToken<List<TeamVo>>(){}.getType()));
                evaluateRootWindowScript(JsConst.CALLBACK_ALL_MY_TEAMS, getJSONFromMap(result));
            }

            @Override
            public void onFailed(int i) {
                Log.i(TAG, "[Query Team List Failed] error:" + i);
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", i);
                evaluateRootWindowScript(JsConst.CALLBACK_ALL_MY_TEAMS, getJSONFromMap(result));

            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "[Query Team List Exception] error:" + throwable.getMessage());
                HashMap<String, Object> result = new HashMap<String, Object>();
                result.put("error", throwable.getMessage());
                evaluateRootWindowScript(JsConst.CALLBACK_ALL_MY_TEAMS, getJSONFromMap(result));
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
                evaluateRootWindowScript(JsConst.CALLBACK_TEAM_BY_ID, getJSONFromMap(result));
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
        NIMClient.getService(TeamService.class).queryTeam(teamId).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team team) {
                TeamVo vo = DataUtil.trans2TeamVo(team);
                result.put("team", new Gson().toJson(vo));
                evaluateRootWindowScript(JsConst.CALLBACK_TEAM_BY_ID, getJSONFromMap(result));
            }

            @Override
            public void onFailed(int i) {
                Log.i(TAG, "[Query Team By Id Failed] error:" + i);
                result.put("error", i);
                evaluateRootWindowScript(JsConst.CALLBACK_TEAM_BY_ID, getJSONFromMap(result));
            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "[Query Team By Id Exception] error:" + throwable.getMessage());
                result.put("error", throwable.getMessage());
                evaluateRootWindowScript(JsConst.CALLBACK_TEAM_BY_ID, getJSONFromMap(result));
            }
        });
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
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_INFO, getJSONFromMap(result));
                return;
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
        NIMClient.getService(TeamService.class).searchTeam(teamId).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team team) {
                TeamVo vo = DataUtil.trans2TeamVo(team);
                result.put("team", new Gson().toJson(vo));
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_INFO, getJSONFromMap(result));
            }

            @Override
            public void onFailed(int i) {
                Log.i(TAG, "[Query Team By Id Failed] error:" + i);
                result.put("error", i);
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_INFO, getJSONFromMap(result));
            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "[Query Team By Id Exception] error:" + throwable.getMessage());
                result.put("error", throwable.getMessage());
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_INFO, getJSONFromMap(result));
            }
        });
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
            JSONArray users = jsonObject.optJSONArray("users");
            List<String> userIdList = new ArrayList<String>();
            if (users != null && users.length() > 0) {
                int len = users.length();
                for (int i = 0; i < len; i++) {
                    userIdList.add(users.getString(i));
                }
            } else {
                SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(mContext, "uexNIM");
                String userAccount = sharedPreferencesHelper.getUserAccount();
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
            NIMClient.getService(TeamService.class).createTeam(fields, teamType, postscript, userIdList).setCallback(new RequestCallback<Team>() {
                @Override
                public void onSuccess(Team team) {
                    result.put("teamId", team.getId());
                    result.put("error", "");
                    evaluateRootWindowScript(JsConst.CALLBACK_CREATE_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_CREATE_TEAM, getJSONFromMap(result));

                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_CREATE_TEAM, getJSONFromMap(result));
                }
            });
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
                evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result));
                return;
            }
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);
            if (users == null || users.length() < 1) {
                result.put("error", "users can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result));
                return;
            }
            List <String> userList = new ArrayList<String>();
            int len = users.length();
            for (int i = 0; i < len; i++) {
                userList.add(users.getString(i));
            }
            NIMClient.getService(TeamService.class).addMembers(teamId, userList).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    //高级群不能直接拉人，发出邀请成功会返回810，此处应该认为邀请已发出
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result));
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String inviterId = jsonObject.optString("invitorId"); //invitorId--文档的拼写错误
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(inviterId)) {
                result.put("error", "teamId or invitorId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_ACCEPT_INVITE_WITH_TEAM, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).acceptInvite(teamId, inviterId).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_ACCEPT_INVITE_WITH_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_ACCEPT_INVITE_WITH_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_ACCEPT_INVITE_WITH_TEAM, getJSONFromMap(result));
                }
            });
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

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
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(inviterId)) {
                result.put("error", "teamId or invitorId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_REJECT_INVITE_WITH_TEAM, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).declineInvite(teamId, inviterId, rejectReason).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_INVITE_WITH_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_INVITE_WITH_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_INVITE_WITH_TEAM, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String message = jsonObject.optString("message");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", "teamId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).applyJoinTeam(teamId, message).setCallback(new RequestCallback<Team>() {
                @Override
                public void onSuccess(Team team) {
                    evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result));
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
                    evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_APPLY_JOIN_TEAM, getJSONFromMap(result));
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String userId = jsonObject.optString("userId");
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(userId)) {
                result.put("error", "teamId or userId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).passApply(teamId, userId).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int code) {
                    if (code == 809) {
                        result.put("applyStatus", 1);//已经在群里
                    } else {
                        result.put("error", code);
                    }
                    evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_PASS_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String userId = jsonObject.optString("userId");
            String rejectReason = jsonObject.optString("rejectReason", "");
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(userId)) {
                result.put("error", "teamId or userId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).rejectApply(teamId, userId, rejectReason).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int code) {
                    if (code == 509) {
                        result.put("error", "已拒绝");
                    } else {
                        result.put("error", code);
                    }
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_REJECT_APPLY_JOIN_TO_TEAM, getJSONFromMap(result));
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String teamName = jsonObject.optString("teamName");
            updateTeamInfo(TeamFieldEnum.Name, teamId, teamName);
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String intro = jsonObject.optString("intro");
            updateTeamInfo(TeamFieldEnum.Introduce, teamId, intro);
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String announcement = jsonObject.optString("announcement");
            updateTeamInfo(TeamFieldEnum.Announcement, teamId, announcement);
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String joinMode = jsonObject.optString("joinMode");
            updateTeamInfo(TeamFieldEnum.VerifyType, teamId, joinMode);
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
    private void updateTeamInfo(TeamFieldEnum fieldType, String teamId, Serializable value) {
        final HashMap<String, Object> result = new HashMap<String, Object>();
        String callbackFunTemp = "";
        if (fieldType == TeamFieldEnum.Name) {
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_NAME;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                result.put("error", "teamId or teamName can not be null");
                evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result));
                return;
            }
        } else if (fieldType == TeamFieldEnum.Introduce){
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_INTRO;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                result.put("error", "teamId or intro can not be null");
                evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result));
                return;
            }
        } else if (fieldType == TeamFieldEnum.Announcement) {
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_ANNOUNCEMENT;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                result.put("error", "teamId or announcement can not be null");
                evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result));
                return;
            }
        } else if (fieldType == TeamFieldEnum.VerifyType) {
            callbackFunTemp = JsConst.CALLBACK_UPDATE_TEAM_JOIN_MODE;
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty((String)value)) {
                result.put("error", "teamId or joinMode can not be null");
                evaluateRootWindowScript(callbackFunTemp, getJSONFromMap(result));
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
        NIMClient.getService(TeamService.class).updateTeam(teamId, fieldType, value).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                evaluateRootWindowScript(callbackFun, getJSONFromMap(result));
            }

            @Override
            public void onFailed(int i) {
                result.put("error", i);
                evaluateRootWindowScript(callbackFun, getJSONFromMap(result));
            }

            @Override
            public void onException(Throwable throwable) {
                result.put("error", throwable.getMessage());
                evaluateRootWindowScript(callbackFun, getJSONFromMap(result));
            }
        });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);

            if (TextUtils.isEmpty(teamId) || users == null || users.length() < 1) {
                result.put("error", "teamId or users can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_ADD_MANAGER_TO_TEAM, getJSONFromMap(result));
                return;
            }

            List <String> userList = new ArrayList<String>();
            int len = users.length();
            for (int i = 0; i < len; i++) {
                userList.add(users.getString(i));
            }
            NIMClient.getService(TeamService.class).addManagers(teamId, userList).setCallback(new RequestCallback<List<TeamMember>>() {
                @Override
                public void onSuccess(List<TeamMember> teamMembers) {
                    evaluateRootWindowScript(JsConst.CALLBACK_ADD_MANAGER_TO_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_ADD_MANAGER_TO_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_ADD_MANAGER_TO_TEAM, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);

            if (TextUtils.isEmpty(teamId) || users == null || users.length() < 1) {
                result.put("error", "teamId or users can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_REMOVE_MANAGER_FROM_TEAM, getJSONFromMap(result));
                return;
            }

            List <String> userList = new ArrayList<String>();
            int len = users.length();
            for (int i = 0; i < len; i++) {
                userList.add(users.getString(i));
            }

            NIMClient.getService(TeamService.class).removeManagers(teamId, userList).setCallback(new RequestCallback<List<TeamMember>>() {
                @Override
                public void onSuccess(List<TeamMember> teamMembers) {
                    evaluateRootWindowScript(JsConst.CALLBACK_REMOVE_MANAGER_FROM_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_REMOVE_MANAGER_FROM_TEAM, getJSONFromMap(result));

                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_REMOVE_MANAGER_FROM_TEAM, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            String newOwnerId = jsonObject.optString("newOwnerId");
            boolean isLeave = jsonObject.optBoolean("isLeave", false);
            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(newOwnerId)) {
                result.put("error", "teamId or newOwnerId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_TRANSFER_MANAGER_WITH_TEAM, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).transferTeam(teamId, newOwnerId, isLeave).setCallback(new RequestCallback<List<TeamMember>>() {
                @Override
                public void onSuccess(List<TeamMember> teamMembers) {
                    evaluateRootWindowScript(JsConst.CALLBACK_TRANSFER_MANAGER_WITH_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_TRANSFER_MANAGER_WITH_TEAM, getJSONFromMap(result));

                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_TRANSFER_MANAGER_WITH_TEAM, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", "teamId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_MEMBERS, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallback<List<TeamMember>>() {
                @Override
                public void onSuccess(List<TeamMember> teamMembers) {
                    List<TeamMemberVo> list = new ArrayList<TeamMemberVo>();
                    for (TeamMember teamMember : teamMembers) {
                        TeamMemberVo vo = DataUtil.tans2TeamMemberVo(teamMember);
                        list.add(vo);
                    }
                    result.put("members", new Gson().toJson(list));
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_MEMBERS, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_MEMBERS, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_FETCH_TEAM_MEMBERS, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", "teamId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_QUIT_TEAM, getJSONFromMap(result));
                return;
            }
            NIMClient.getService(TeamService.class).quitTeam(teamId).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_QUIT_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_QUIT_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_QUIT_TEAM, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            //android只支持一次踢出一个人, 和iOS不一致
            String usersStr = jsonObject.optString("users");
            JSONArray users = new JSONArray(usersStr);
            if (users == null || users.length() < 1) {
                result.put("error", "users can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_ADD_USERS, getJSONFromMap(result));
                return;
            }

            if (TextUtils.isEmpty(teamId)) {
                result.put("error", "teamId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_KICK_USERS, getJSONFromMap(result));
                return;
            }

            NIMClient.getService(TeamService.class).removeMember(teamId, users.getString(0)).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_KICK_USERS, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_KICK_USERS, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_KICK_USERS, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", "teamId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_DISMISS_TEAM, getJSONFromMap(result));
                return;
            }

            NIMClient.getService(TeamService.class).dismissTeam(teamId).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_DISMISS_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_DISMISS_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_DISMISS_TEAM, getJSONFromMap(result));
                }
            });
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
        try {
            jsonObject = new JSONObject(json);
            String teamId = jsonObject.optString("teamId");
            Boolean notify = jsonObject.optBoolean("notify", true);
            if (TextUtils.isEmpty(teamId)) {
                result.put("error", "teamId can not be null");
                evaluateRootWindowScript(JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_TEAM, getJSONFromMap(result));
                return;
            }

            NIMClient.getService(TeamService.class).muteTeam(teamId, notify).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    evaluateRootWindowScript(JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onFailed(int i) {
                    result.put("error", i);
                    evaluateRootWindowScript(JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_TEAM, getJSONFromMap(result));
                }

                @Override
                public void onException(Throwable throwable) {
                    result.put("error", throwable.getMessage());
                    evaluateRootWindowScript(JsConst.CALLBACK_UPDATE_NOTIFY_STATE_FOR_TEAM, getJSONFromMap(result));
                }
            });
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public String getRealPath(String path){
        String realPath=BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), path),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        return realPath;
    }


    private String getJSONFromMap(Map<String, Object> result) {
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
       evaluateRootWindowScript(JsConst.ON_SEND_MESSAGE_WITH_PROGRESS, getJSONFromMap(result));
    }

    @Override
    public void onReceivedMessages(List<IMMessage> messages) {
        Log.i(TAG, "[onReceivedMessages]");
        List<MessageVo> list = new ArrayList<MessageVo>();
        for (IMMessage message : messages) {
            list.add(DataUtil.trans2MessageVo(message));
        }
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("data", new Gson().toJson(list));
        evaluateRootWindowScript(JsConst.ON_RECIEVED_MESSAGE, getJSONFromMap(result));
    }

    @Override
    public void onMessageStatusChange(IMMessage msg) {
//        String json = "";
//        if (msg.getAttachment() != null) {
//            json = msg.getAttachment().toJson(true);
//        }
//        Log.i(TAG, "[onMessageStatusChange]" + msg.getAttachStatus() + "  JSON:  " + json);

    }

    @Override
    public void onUpdateRecentSession(List<RecentContact> contacts) {

    }
}
