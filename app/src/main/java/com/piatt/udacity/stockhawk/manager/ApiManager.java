package com.piatt.udacity.stockhawk.manager;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.piatt.udacity.stockhawk.model.Stock;
import com.piatt.udacity.stockhawk.model.StockResponse;
import com.piatt.udacity.stockhawk.model.StocksResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ApiManager {
    private StockApi stockApi;
    @Getter private static ApiManager manager = new ApiManager();

    private ApiManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(StockApi.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        stockApi = retrofit.create(StockApi.class);
    }

    public Observable<Stock> getStock(String symbol) {
        List<String> symbols = Collections.singletonList(symbol);
        return stockApi.getStock(getStockQueryMap(symbols))
                .map(StockResponse::getStock)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Stock>> getStocks(List<String> symbols) {
        Observable<List<Stock>> stocksObservable;

        if (symbols.size() == 1) {
            stocksObservable = stockApi.getStock(getStockQueryMap(symbols))
                    .doOnNext(stockResponse -> StorageManager.getManager().setTimestamp(stockResponse.getTimestamp()))
                    .map(StockResponse::getStock)
                    .toList();
        } else {
            stocksObservable = stockApi.getStocks(getStockQueryMap(symbols))
                    .doOnNext(stocksResponse -> StorageManager.getManager().setTimestamp(stocksResponse.getTimestamp()))
                    .map(StocksResponse::getStocks);
        }

        return stocksObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Map<String, String> getStockQueryMap(List<String> symbols) {
        String formattedSymbols = Stream.of(symbols)
                .map(symbol -> String.format("'%s'", symbol))
                .collect(Collectors.joining(","));

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(StockApi.API_QUERY_KEY, String.format(StockApi.API_QUERY_VALUE, formattedSymbols));
        queryMap.put(StockApi.API_FORMAT_KEY, StockApi.API_FORMAT_VALUE);
        queryMap.put(StockApi.API_ENVIRONMENT_KEY, StockApi.API_ENVIRONMENT_VALUE);

        return queryMap;
    }

    private interface StockApi {
        String API_BASE_URL = "https://query.yahooapis.com/v1/public/";
        String API_QUERY_ENDPOINT = "yql";
        String API_QUERY_KEY = "q";
        String API_QUERY_VALUE = "select * from yahoo.finance.quotes where symbol in (%s)";
        String API_FORMAT_KEY = "format";
        String API_FORMAT_VALUE = "json";
        String API_ENVIRONMENT_KEY = "env";
        String API_ENVIRONMENT_VALUE = "store://datatables.org/alltableswithkeys";

        @GET(API_QUERY_ENDPOINT)
        Observable<StockResponse> getStock(@QueryMap Map<String, String> stockQueryMap);

        @GET(API_QUERY_ENDPOINT)
        Observable<StocksResponse> getStocks(@QueryMap Map<String, String> stockQueryMap);
    }
}