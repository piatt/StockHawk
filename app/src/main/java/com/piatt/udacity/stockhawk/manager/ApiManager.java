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
    private StocksApi stocksApi;
    @Getter static ApiManager manager = new ApiManager();

    private ApiManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(StocksApi.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        stocksApi = retrofit.create(StocksApi.class);
    }

    public Observable<Stock> getQuote(String symbol) {
        return stocksApi.getQuote(symbol)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private interface StocksApi {
        String API_BASE_URL = "http://dev.markitondemand.com/MODApis/Api/v2/";
        String API_ENDPOINT_QUOTE = "Quote/json";
        String API_PARAM_SYMBOL = "symbol";

        @GET(API_ENDPOINT_QUOTE)
        Observable<Stock> getQuote(@Query(API_PARAM_SYMBOL) String symbol);
    }
}