package com.piatt.udacity.stockhawk.manager;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.piatt.udacity.stockhawk.model.Stock;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.schedulers.Schedulers;

public class ApiManager {
    private StockApi stockApi;

    public ApiManager() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<Stock>>() {}.getType(), new StocksDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(StockApi.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        stockApi = retrofit.create(StockApi.class);
    }

    public Observable<List<Stock>> getStocks(List<String> symbols) {
        if (!symbols.isEmpty()) {
            return stockApi.getStocks(getStocksQueryMap(symbols)).subscribeOn(Schedulers.io());
        }
        return Observable.empty();
    }

    public Observable<List<Stock>> getStockIntervals(String symbol, String startDate, String endDate) {
        return stockApi.getStockIntervals(getStockIntervalsQueryMap(symbol, startDate, endDate));
    }

    private Map<String, String> getStocksQueryMap(List<String> symbols) {
        String formattedSymbols = Stream.of(symbols)
                .map(symbol -> String.format("'%s'", symbol))
                .collect(Collectors.joining(","));

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(StockApi.API_QUERY_KEY, String.format(StockApi.API_UPDATE_QUERY_VALUE, formattedSymbols));
        queryMap.put(StockApi.API_FORMAT_KEY, StockApi.API_FORMAT_VALUE);
        queryMap.put(StockApi.API_ENVIRONMENT_KEY, StockApi.API_ENVIRONMENT_VALUE);

        return queryMap;
    }

    private Map<String, String> getStockIntervalsQueryMap(String symbol, String startDate, String endDate) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(StockApi.API_QUERY_KEY, String.format(StockApi.API_INTERVAL_QUERY_VALUE_, symbol, startDate, endDate));
        queryMap.put(StockApi.API_FORMAT_KEY, StockApi.API_FORMAT_VALUE);
        queryMap.put(StockApi.API_ENVIRONMENT_KEY, StockApi.API_ENVIRONMENT_VALUE);

        return queryMap;
    }

    private class StocksDeserializer implements JsonDeserializer<List<Stock>> {
        @Override
        public List<Stock> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject() && json.getAsJsonObject().has(StockApi.API_QUERY_MEMBER)) {
                JsonElement quoteElement = json.getAsJsonObject()
                        .getAsJsonObject(StockApi.API_QUERY_MEMBER)
                        .getAsJsonObject(StockApi.API_RESULTS_MEMBER)
                        .get(StockApi.API_QUOTE_MEMBER);

                if (quoteElement.isJsonArray()) {
                    return new Gson().fromJson(quoteElement, typeOfT);
                } else if (quoteElement.isJsonObject()) {
                    Stock stock = context.deserialize(quoteElement, Stock.class);
                    return new ArrayList<>(Arrays.asList(stock));
                }
            }
            throw new RuntimeException();
        }
    }

    private interface StockApi {
        String API_BASE_URL = "https://query.yahooapis.com/v1/public/";
        String API_QUERY_ENDPOINT = "yql";
        String API_QUERY_KEY = "q";
        String API_UPDATE_QUERY_VALUE = "select * from yahoo.finance.quotes where symbol in (%s)";
        String API_INTERVAL_QUERY_VALUE_= "select * from yahoo.finance.historicaldata where symbol = '%s' and startDate = '%s' and endDate = '%s'";
        String API_FORMAT_KEY = "format";
        String API_FORMAT_VALUE = "json";
        String API_ENVIRONMENT_KEY = "env";
        String API_ENVIRONMENT_VALUE = "store://datatables.org/alltableswithkeys";
        String API_QUERY_MEMBER = "query";
        String API_RESULTS_MEMBER = "results";
        String API_QUOTE_MEMBER = "quote";

        @GET(API_QUERY_ENDPOINT)
        Observable<List<Stock>> getStocks(@QueryMap Map<String, String> stocksQueryMap);

        @GET(API_QUERY_ENDPOINT)
        Observable<List<Stock>> getStockIntervals(@QueryMap Map<String, String> stockIntervalsQueryMap);
    }
}