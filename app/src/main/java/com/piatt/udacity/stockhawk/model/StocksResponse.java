package com.piatt.udacity.stockhawk.model;

import com.annimon.stream.Stream;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.Getter;

public class StocksResponse {
    private StockQuery query;
    private transient final String TIMESTAMP_FORMAT = "E h:mm a";

    public List<Stock> getStocks() {
        List<Stock> stocks = query.getResults().getStocks();
        Stream.of(stocks).forEach(stock -> stock.setTimestamp(getTimestamp()));
        return stocks;
    }

    public String getTimestamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return simpleDateFormat.format(query.getTimestamp());
    }

    private class StockQuery {
        @Getter private StockQueryResult results;
        @Getter @SerializedName("created") private Date timestamp;
    }

    private class StockQueryResult {
        @Getter @SerializedName("quote") private List<Stock> stocks;
    }
}