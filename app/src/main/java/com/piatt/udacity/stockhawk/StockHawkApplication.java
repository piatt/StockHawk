package com.piatt.udacity.stockhawk;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import lombok.Getter;

public class StockHawkApplication extends Application {
    private ConnectivityManager connectivityManager;
    @Getter private static StockHawkApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isNetworkAvailable() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}