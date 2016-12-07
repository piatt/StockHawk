package com.piatt.udacity.stockhawk.manager;

import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.model.Stock;

import lombok.Getter;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class StockManager {
    private ApiManager apiManager;
    private StorageManager storageManager;
    private BehaviorSubject<StocksEvent> stocksEventBus;

    public StockManager() {
        apiManager = StockHawkApplication.getApp().getApiManager();
        storageManager = StockHawkApplication.getApp().getStorageManager();
        stocksEventBus = BehaviorSubject.create();
    }

    public void updateStocks() {
        if (StockHawkApplication.getApp().isNetworkAvailable()) {
            Observable.from(storageManager.getStocks())
                    .map(Stock::getSymbol)
                    .toList()
                    .flatMap(symbols -> apiManager.getStocks(symbols))
                    .doOnSubscribe(() -> stocksEventBus.onNext(new StocksEvent(true)))
                    .doOnNext(stocks -> storageManager.updateStocks(stocks))
                    .doOnError(error -> stocksEventBus.onNext(new StocksEvent(false, R.string.error_message)))
                    .doOnCompleted(() -> {
                        boolean isEmpty = storageManager.getStocks().isEmpty();
                        StocksEvent stocksEvent = isEmpty ? new StocksEvent(false) : new StocksEvent(false, R.string.data_message);
                        stocksEventBus.onNext(stocksEvent);
                    })
                    .subscribe(stocks -> {}, error -> {});
        } else {
            stocksEventBus.onNext(new StocksEvent(false, R.string.connection_message));
        }
    }

    public Observable<StocksEvent> onStocksEvent() {
        return stocksEventBus.asObservable();
    }

    public boolean hasPendingUpdate() {
        return stocksEventBus.hasValue() && stocksEventBus.getValue().isUpdating();
    }

    public class StocksEvent {
        @Getter private boolean updating;
        @Getter private String message;

        public StocksEvent(boolean updating) {
            this.updating = updating;
        }

        public StocksEvent(boolean updating, int messageId) {
            this.updating = updating;
            message = StockHawkApplication.getApp().getString(messageId);
        }
    }
}