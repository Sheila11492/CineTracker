package com.example.cinetracker.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS = "cine_session_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASS = "contrasena";

    public static boolean saveLogin(Context ctx, int userId) {
        Context app = ctx.getApplicationContext();
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_USER_ID, userId);
        return editor.commit();
    }


    public static boolean saveCredentials(Context ctx, String email, String contrasena) {
        Context app = ctx.getApplicationContext();
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASS, contrasena);
        return editor.commit();
    }

    public static int getUserId(Context ctx) {
        Context app = ctx.getApplicationContext();
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getInt(KEY_USER_ID, -1);
    }

    public static String getSavedEmail(Context ctx) {
        Context app = ctx.getApplicationContext();
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_EMAIL, null);
    }

    public static String getSavedPassword(Context ctx) {
        Context app = ctx.getApplicationContext();
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_PASS, null);
    }

    public static void logout(Context ctx) {
        Context app = ctx.getApplicationContext();
        SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().clear().commit();
    }
}
