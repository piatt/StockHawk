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
    private BehaviorSubject<StockManagerEvent> searchStockEventBus;
    private BehaviorSubject<StockManagerEvent> updateStocksEventBus;

    public StockManager() {
        apiManager = StockHawkApplication.getApp().getApiManager();
        storageManager = StockHawkApplication.getApp().getStorageManager();
        searchStockEventBus = BehaviorSubject.create();
        updateStocksEventBus = BehaviorSubject.create();
    }

    public void addStock(String symbol) {
        if (StockHawkApplication.getApp().isNetworkAvailable()) {
            Observable.just(symbol)
                    .toList()
                    .flatMap(symbols -> apiManager.getStocks(symbols))
                    .map(stocks -> stocks.get(0))
                    .filter(stock -> {
                        boolean valid = stock.isValid();
                        if (!valid) {
                            searchStockEventBus.onNext(new StockManagerEvent(R.string.invalid_message));
                        }
                        return valid;
                    })
                    .filter(stock -> {
                        boolean duplicateStock = storageManager.hasStock(stock);
                        if (duplicateStock) {
                            searchStockEventBus.onNext(new StockManagerEvent(R.string.duplicate_message));
                        }
                        return !duplicateStock;
                    })
                    .subscribe(stock -> {
                        storageManager.addStock(stock);
                        searchStockEventBus.onNext(new StockManagerEvent(R.string.data_message, true));
                    }, error -> searchStockEventBus.onNext(new StockManagerEvent(R.string.error_message)));
        } else {
            searchStockEventBus.onNext(new StockManagerEvent(R.string.connection_message));
        }
    }

    public void updateStocks() {
        if (StockHawkApplication.getApp().isNetworkAvailable()) {
            Observable.from(storageManager.getStocks())
                    .map(Stock::getSymbol)
                    .toList()
                    .filter(symbols -> symbols.size() > 0)
                    .flatMap(symbols -> apiManager.getStocks(symbols))
                    .doOnSubscribe(() -> updateStocksEventBus.onNext(new StockManagerEvent(true)))
                    .doOnCompleted(() -> updateStocksEventBus.onNext(new StockManagerEvent(false)))
                    .subscribe(stocks -> {
                        storageManager.updateStocks(stocks);
                        updateStocksEventBus.onNext(new StockManagerEvent(R.string.data_message, true));
                    }, error -> updateStocksEventBus.onNext(new StockManagerEvent(R.string.error_message)));
        } else {
            updateStocksEventBus.onNext(new StockManagerEvent(R.string.connection_message));
        }
    }

    public Observable<StockManagerEvent> onStockSearchEvent(boolean restarted) {
        if (!restarted) {
            searchStockEventBus.onCompleted();
            searchStockEventBus = BehaviorSubject.create();
        }
        return searchStockEventBus.asObservable();
    }

    public Observable<StockManagerEvent> onStocksUpdateEvent(boolean restarted) {
        Observable<StockManagerEvent> stocksUpdateObservable = updateStocksEventBus.asObservable();
        boolean hasInProgressEvent = updateStocksEventBus.hasValue() && updateStocksEventBus.getValue().isRunning();
        return restarted && !hasInProgressEvent ? stocksUpdateObservable.skip(1) : stocksUpdateObservable;
    }

    public class StockManagerEvent {
        @Getter private boolean running;
        @Getter private String message;
        @Getter private boolean complete;

        public StockManagerEvent(boolean running) {
            this.running = running;
        }

        public StockManagerEvent(int messageId) {
            this(messageId, false);
        }

        public StockManagerEvent(int messageId, boolean complete) {
            message = StockHawkApplication.getApp().getString(messageId);
            this.complete = complete;
        }
    }
}