package com.piatt.udacity.stockhawk.model;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = "symbol")
public class Stock {
    @Getter @Setter @SerializedName("Symbol") String symbol;
    @Getter @Setter @SerializedName("Name") String company;
    @Setter @SerializedName("LastPrice") float price;
    @Setter @SerializedName("Change") float priceDelta;
    @Setter @SerializedName("ChangePercent") float percentDelta;

    public String getPrice() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(price);
    }

    public String getPriceDelta() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setPositivePrefix("+");
        return format.format(priceDelta);
    }

    public String getPercentDelta() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getPercentInstance();
        format.setMultiplier(1);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        format.setPositivePrefix("+");
        return format.format(percentDelta);
    }

    public boolean hasPositiveDelta() {
        return priceDelta >= 0;
    }

    public boolean isValid() {
        return symbol != null;
    }
}