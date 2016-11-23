package com.piatt.udacity.stockhawk.manager;

import com.piatt.udacity.stockhawk.model.Stock;

import lombok.Getter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ApiManager {
    private StockApi stockApi;
    @Getter static ApiManager manager = new ApiManager();

    private ApiManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(StockApi.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        stockApi = retrofit.create(StockApi.class);
    }

    public Observable<Stock> getStock(String symbol) {
        return stockApi.getStock(symbol)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private interface StockApi {
        String API_BASE_URL = "http://dev.markitondemand.com/MODApis/Api/v2/";
        String API_ENDPOINT_STOCK = "Quote/json";
        String API_PARAM_SYMBOL = "symbol";

        @GET(API_ENDPOINT_STOCK)
        Observable<Stock> getStock(@Query(API_PARAM_SYMBOL) String symbol);
    }
}