package com.piatt.udacity.stockhawk.model;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;

public class StockResponse {
    private StockQuery query;
    private transient final String TIMESTAMP_FORMAT = "E h:mm a";

    public Stock getStock() {
        Stock stock = query.getResults().getStock();
        stock.setTimestamp(getTimestamp());
        return stock;
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
        @Getter @SerializedName("quote") private Stock stock;
    }
}