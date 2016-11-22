package com.piatt.udacity.stockhawk.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.piatt.udacity.stockhawk.StocksApplication;
import com.piatt.udacity.stockhawk.model.Stock;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import rx.Observable;

public class StorageManager {
    private static final String PREF_TAG = "STOCK_HAWK";
    private static final String PREF_STOCKS = "STOCKS";

    private Stock lastAddedStock;
    private Preference<Set<Stock>> stockStorage;
    @Getter static StorageManager manager = new StorageManager();

    private StorageManager() {
        SharedPreferences sharedPreferences = StocksApplication.getApp().getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        RxSharedPreferences rxSharedPreferences = RxSharedPreferences.create(sharedPreferences);
        stockStorage = rxSharedPreferences.getObject(PREF_STOCKS, new LinkedHashSet<>(), new StockStorageAdapter());
    }

    public Set<Stock> getStocks() {
        return stockStorage.get();
    }

    public boolean hasStock(Stock stock) {
        return stockStorage.get().contains(stock);
    }

    public void addStock(Stock stock) {
        Set<Stock> stocks = stockStorage.get();
        if (!hasStock(stock)) {
            stocks.add(stock);
            lastAddedStock = stock;
            stockStorage.set(stocks);
        }
    }

    public void removeStock(Stock stock) {
        Set<Stock> stocks = stockStorage.get();
        if (hasStock(stock)) {
            stocks.remove(stock);
            lastAddedStock = null;
            stockStorage.set(stocks);
        }
    }

    public Observable<Stock> getLastAddedStock() {
        return stockStorage.asObservable().filter(event -> lastAddedStock != null).map(event -> lastAddedStock);
    }

    private class StockStorageAdapter implements Preference.Adapter<Set<Stock>> {
        private final Gson gson;
        private final Type type;

        public StockStorageAdapter() {
            gson = new Gson();
            type = new TypeToken<LinkedHashSet<Stock>>() {}.getType();
        }

        @Override
        public Set<Stock> get(@NonNull String key, @NonNull SharedPreferences preferences) {
            return gson.fromJson(preferences.getString(key, null), type);
        }

        @Override
        public void set(@NonNull String key, @NonNull Set<Stock> stocks, @NonNull Editor editor) {
            editor.putString(key, gson.toJson(stocks));
        }
    }
}