package com.piatt.udacity.stockhawk;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.piatt.udacity.stockhawk.manager.ApiManager;
import com.piatt.udacity.stockhawk.manager.StockManager;
import com.piatt.udacity.stockhawk.manager.StorageManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;

public class StockHawkApplication extends Application {
    private final String TIMESTAMP_FORMAT = "E h:mm a";
    private SimpleDateFormat timestampFormat;
    private ConnectivityManager connectivityManager;
    @Getter private ApiManager apiManager;
    @Getter private StorageManager storageManager;
    @Getter private StockManager stockManager;
    @Getter private static StockHawkApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        apiManager = new ApiManager();
        storageManager = new StorageManager();
        stockManager = new StockManager();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
    }

    public boolean isNetworkAvailable() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public String getCurrentTimestamp() {
        return timestampFormat.format(new Date());
    }
}