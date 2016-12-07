package com.piatt.udacity.stockhawk.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.model.Stock;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public class StorageManager {
    private final String PREF_TAG = "STOCK_HAWK";
    private final String PREF_TIMESTAMP = "TIMESTAMP";
    private final String PREF_STOCKS = "STOCKS";
    private Preference<String> timestamp;
    private Preference<List<Stock>> stockStorage;
    private PublishSubject<List<Stock>> updateStocksEventBus;
    private PublishSubject<Stock> addStockEventBus;
    private PublishSubject<Stock> removeStockEventBus;

    public StorageManager() {
        SharedPreferences sharedPreferences = StockHawkApplication.getApp().getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        RxSharedPreferences rxSharedPreferences = RxSharedPreferences.create(sharedPreferences);
        timestamp = rxSharedPreferences.getString(PREF_TIMESTAMP);
        stockStorage = rxSharedPreferences.getObject(PREF_STOCKS, new ArrayList<>(), new StockStorageAdapter());
        updateStocksEventBus = PublishSubject.create();
        addStockEventBus = PublishSubject.create();
        removeStockEventBus = PublishSubject.create();
    }

    public List<Stock> getStocks() {
        return stockStorage.get();
    }

    public void updateStocks(List<Stock> stocks) {
        timestamp.set(StockHawkApplication.getApp().getCurrentTimestamp());
        stockStorage.set(stocks);
        updateStocksEventBus.onNext(stocks);
    }

    public void addStock(Stock stock) {
        stock.setTimestamp(StockHawkApplication.getApp().getCurrentTimestamp());
        List<Stock> stocks = getStocks();
        if (stocks.add(stock)) {
            stockStorage.set(stocks);
            addStockEventBus.onNext(stock);
        }
    }

    public void removeStock(Stock stock) {
        List<Stock> stocks = getStocks();
        if (stocks.remove(stock)) {
            stockStorage.set(stocks);
            removeStockEventBus.onNext(stock);
        }
    }

    public Stock getStock(String symbol) {
        Optional<Stock> stockOptional = Stream.of(getStocks())
                .filter(stock -> stock.getSymbol().equals(symbol))
                .findFirst();
        return stockOptional.isPresent() ? stockOptional.get() : null;
    }

    public boolean hasStock(Stock stock) {
        return getStocks().contains(stock);
    }

    public Observable<String> onTimestampChanged() {
        return timestamp.asObservable();
    }

    public Observable<List<Stock>> onStocksUpdated() {
        return updateStocksEventBus.asObservable();
    }

    public Observable<Stock> onStockAdded() {
        return addStockEventBus.asObservable();
    }

    public Observable<Stock> onStockRemoved() {
        return removeStockEventBus.asObservable();
    }

    public Observable<Integer> onSizeChanged() {
        return Observable.merge(onStockAdded(), onStockRemoved()).map(stock -> getStocks().size()).startWith(getStocks().size());
    }

    private class StockStorageAdapter implements Preference.Adapter<List<Stock>> {
        private final Gson gson;
        private final Type type;

        public StockStorageAdapter() {
            gson = new Gson();
            type = new TypeToken<ArrayList<Stock>>() {}.getType();
        }

        @Override
        public List<Stock> get(@NonNull String key, @NonNull SharedPreferences preferences) {
            return gson.fromJson(preferences.getString(key, null), type);
        }

        @Override
        public void set(@NonNull String key, @NonNull List<Stock> stocks, @NonNull Editor editor) {
            editor.putString(key, gson.toJson(stocks));
        }
    }
}