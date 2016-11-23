package com.piatt.udacity.stockhawk.model;

import lombok.Getter;
import lombok.Setter;

public class StockResponse {
    @Getter @Setter StockQuery query;

    public Stock getStock() {
        return query.getResults().getQuote();
    }

    private class StockQuery {
        @Getter @Setter StockQueryResult results;
    }

    private class StockQueryResult {
        @Getter @Setter Stock quote;
    }
}