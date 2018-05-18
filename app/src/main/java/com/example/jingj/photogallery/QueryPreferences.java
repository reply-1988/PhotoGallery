package com.example.jingj.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {

    private static String PREF_SEARCH_QUERY = "searchQuery";
    private static String PREF_LAST_RESULT_ID = "lastResulted";


    //获得储存在SharedPreferences中的值
    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    //写入储存在SharedPreferences中的值
    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null);
    }

    public static void setfLastResultId(Context context, String lastResultID) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultID)
                .apply();
    }
}
