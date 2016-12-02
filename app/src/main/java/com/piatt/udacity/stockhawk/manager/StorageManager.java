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

import lombok.AllArgsConstructor;
import lombok.Getter;
import rx.Observable;
import rx.subjects.PublishSubject;

public class StorageManager {
    private final String PREF_TAG = "STOCK_HAWK";
    private final String PREF_TIMESTAMP = "TIMESTAMP";
    private final String PREF_STOCKS = "STOCKS";
    private final int ADD_STOCK_EVENT = 0;
    private final int UPDATE_STOCK_EVENT = 1;
    private final int REMOVE_STOCK_EVENT = 2;

    private Preference<String> timestamp;
    private Preference<List<Stock>> stockStorage;
    private PublishSubject<StockEvent> stockEventBus = PublishSubject.create();
    @Getter private static StorageManager manager = new StorageManager();

    private StorageManager() {
        SharedPreferences sharedPreferences = StockHawkApplication.getApp().getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        RxSharedPreferences rxSharedPreferences = RxSharedPreferences.create(sharedPreferences);
        timestamp = rxSharedPreferences.getString(PREF_TIMESTAMP);
        stockStorage = rxSharedPreferences.getObject(PREF_STOCKS, new ArrayList<>(), new StockStorageAdapter());
    }

    public void setTimestamp(String timestamp) {
        this.timestamp.set(timestamp);
    }

    public List<Stock> getStocks() {
        return stockStorage.get();
    }

    public Stock getStock(String symbol) {
        Optional<Stock> stockOptional = Stream.of(getStocks()).filter(stock -> stock.getSymbol().equals(symbol)).findFirst();
        return stockOptional.isPresent() ? stockOptional.get() : null;
    }

    public boolean hasStock(Stock stock) {
        return getStocks().contains(stock);
    }

    public void addStock(Stock stock) {
        List<Stock> stocks = getStocks();
        if (stocks.add(stock)) {
            stockStorage.set(stocks);
            stockEventBus.onNext(new StockEvent(stock, ADD_STOCK_EVENT));
        }
    }

    public void updateStocks(List<Stock> updatedStocks) {
        List<Stock> stocks = getStocks();
        Stream.of(updatedStocks).forEach(stock -> {
            if (stocks.remove(stock)) {
                stocks.add(stock);
                stockEventBus.onNext(new StockEvent(stock, UPDATE_STOCK_EVENT));
            }
        });
        stockStorage.set(stocks);
    }

    public void removeStock(Stock stock) {
        List<Stock> stocks = getStocks();
        if (stocks.remove(stock)) {
            stockStorage.set(stocks);
            stockEventBus.onNext(new StockEvent(stock, REMOVE_STOCK_EVENT));
        }
    }

    public Observable<Stock> onStockAdded() {
        return stockEventBus.filter(stockEvent -> stockEvent.getType() == ADD_STOCK_EVENT).map(StockEvent::getStock);
    }

    public Observable<Stock> onStockUpdated() {
        return stockEventBus.filter(stockEvent -> stockEvent.getType() == UPDATE_STOCK_EVENT).map(StockEvent::getStock);
    }

    public Observable<Stock> onStockRemoved() {
        return stockEventBus.filter(stockEvent -> stockEvent.getType() == REMOVE_STOCK_EVENT).map(StockEvent::getStock);
    }

    public Observable<Integer> onSizeChanged() {
        return Observable.merge(onStockAdded(), onStockRemoved()).map(stock -> getStocks().size()).startWith(getStocks().size());
    }

    public Observable<String> onTimestampChanged() {
        return timestamp.asObservable();
    }

    @AllArgsConstructor
    private class StockEvent {
        @Getter private Stock stock;
        @Getter private int type;
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