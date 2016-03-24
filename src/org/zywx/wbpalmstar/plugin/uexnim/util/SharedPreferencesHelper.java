package org.zywx.wbpalmstar.plugin.uexnim.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Fred on 2016/2/17.
 */
public class SharedPreferencesHelper {
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String KEY_USER_ACCOUNT = "account";
    private static final String KEY_USER_TOKEN = "token";

    public SharedPreferencesHelper(Context context, String name) {
        sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void setUserAccount(String account) {
        saveString(KEY_USER_ACCOUNT, account);
    }

    public String getUserAccount() {
        return getString(KEY_USER_ACCOUNT);
    }

    public void setUserToken(String token) {
        saveString(KEY_USER_TOKEN, token);
    }

    public String getUserToken() {
        return getString(KEY_USER_TOKEN);
    }

    private void saveString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    private  String getString(String key) {
        return sp.getString(key, null);
    }
}
