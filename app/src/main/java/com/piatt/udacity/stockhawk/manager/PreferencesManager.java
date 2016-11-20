package com.piatt.udacity.stockhawk.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.piatt.udacity.stockhawk.StocksApplication;

import java.util.Set;

import lombok.Getter;

public class PreferencesManager {
    public static final String PREF_TAG = "STOCK_HAWK";
    public static final String PREF_SYMBOLS = "SYMBOLS";

    @Getter Preference<Set<String>> savedSymbols;
    @Getter static PreferencesManager manager = new PreferencesManager();

    public PreferencesManager() {
        SharedPreferences sharedPreferences = StocksApplication.getApp().getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        RxSharedPreferences rxSharedPreferences = RxSharedPreferences.create(sharedPreferences);
        savedSymbols = rxSharedPreferences.getStringSet(PREF_SYMBOLS);
    }
}