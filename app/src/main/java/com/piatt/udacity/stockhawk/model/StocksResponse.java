package com.piatt.udacity.stockhawk.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class StocksResponse {
    @Getter @Setter StockQuery query;

    public List<Stock> getStocks() {
        return query.getResults().getQuote();
    }

    private class StockQuery {
        @Getter @Setter StockQueryResult results;
    }

    private class StockQueryResult {
        @Getter @Setter List<Stock> quote;
    }
}