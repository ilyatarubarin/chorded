package com.chorded.app.session;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;

public class AppSession {

    private static AppSession instance;

    private SessionType type;
    private String uid;

    private AppSession() {}

    public static AppSession get() {
        if (instance == null) {
            instance = new AppSession();
        }
        return instance;
    }

    // ===============================
    // INIT (ОБЯЗАТЕЛЬНО ВЫЗЫВАТЬ)
    // ===============================
    public void init(Context context) {
        SessionStorage storage = new SessionStorage(context);

        if (!storage.hasSession()) {
            type = null;
            uid = null;
            return;
        }

        type = storage.getType();
        uid = storage.getUid();
    }

    // ===============================
    // START MODES
    // ===============================
    public void startGuest(Context context) {
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener(result -> {
                    type = SessionType.GUEST;
                    uid = result.getUser().getUid(); // ← ВАЖНО
                    new SessionStorage(context).saveGuest();
                });
    }


    public void startAuth(Context context, String uid) {
        type = SessionType.AUTH;
        this.uid = uid;
        new SessionStorage(context).saveAuth(uid);
    }

    // ===============================
    // STATE
    // ===============================
    public boolean isGuest() {
        return type == SessionType.GUEST;
    }

    public boolean isAuth() {
        return type == SessionType.AUTH;
    }

    public String getUid() {
        return uid;
    }

    // ===============================
    // LOGOUT
    // ===============================
    public void logout(Context context) {
        type = null;
        uid = null;
        FirebaseAuth.getInstance().signOut();
        new SessionStorage(context).clear();
    }
}
