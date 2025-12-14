package com.chorded.app.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionStorage {

    private static final String PREFS = "app_session";
    private static final String KEY_TYPE = "type";
    private static final String KEY_UID = "uid";

    private final SharedPreferences prefs;

    public SessionStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveGuest() {
        prefs.edit()
                .putString(KEY_TYPE, SessionType.GUEST.name())
                .remove(KEY_UID)
                .apply();
    }

    public void saveAuth(String uid) {
        prefs.edit()
                .putString(KEY_TYPE, SessionType.AUTH.name())
                .putString(KEY_UID, uid)
                .apply();
    }

    public boolean hasSession() {
        return prefs.contains(KEY_TYPE);
    }

    public SessionType getType() {
        String t = prefs.getString(KEY_TYPE, null);
        return t == null ? null : SessionType.valueOf(t);
    }

    public String getUid() {
        return prefs.getString(KEY_UID, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
