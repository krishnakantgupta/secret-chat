package com.kk.secretchat;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class AppPref {

    public  static long lastOpenDate = 0;
    private static AppPref ourInstance = null;//new AppPref();
    private static SharedPreferences sharedPreferences;
    public static String fromID = "";

    private AppPref(Context context) {
        sharedPreferences = context.getSharedPreferences("com.kk.secretchat", Context.MODE_PRIVATE);
    }

    public static AppPref getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new AppPref(context);
        }
        return ourInstance;
    }

    public void saveUserData(String username, String password) {
        fromID = username;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }


    public String[] getUserData() {
        String username = sharedPreferences.getString("username", null);
        if (username == null) {
            return null;
        }
        fromID = username;
        String password = sharedPreferences.getString("password", null);
        return new String[]{username, password};
    }

    public void saveSenderID(String senderName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("senderName", senderName);
        editor.apply();
    }

    public String getSenderID() {
        return sharedPreferences.getString("senderName", null);
    }

    public void saveSecurePassword(String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("senderPassword", password);
        editor.apply();
    }

    public String getSecurePassword() {
        return sharedPreferences.getString("senderPassword", "141410");
    }

    public void saveCalHideFor(int minutes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("minutes", minutes);
        editor.apply();
    }

    public int getCalHideFor() {
        return sharedPreferences.getInt("minutes", -1);
    }



    public void saveLastSeen(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
    }

    public long getLastSean() {
        return sharedPreferences.getLong("lastSean", 0L);
    }
}
