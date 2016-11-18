package com.piatt.udacity.stockhawk.model;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import lombok.Getter;
import lombok.Setter;

public class Stock {
    @Getter @Setter @SerializedName("Symbol") String symbol;
    @Getter @Setter @SerializedName("Name") String company;
    @Setter @SerializedName("LastPrice") float price;
    @Setter @SerializedName("Change") float priceDelta;
    @Setter @SerializedName("ChangePercent") float percentDelta;

    public String getPrice() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        return decimalFormat.format(price);
    }

    public String getPriceDelta() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setPositivePrefix("+");
        return decimalFormat.format(priceDelta);
    }

    public String getPercentDelta() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getPercentInstance();
        decimalFormat.setMultiplier(1);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setPositivePrefix("+");
        return decimalFormat.format(percentDelta);
    }

    public boolean hasPositiveDelta() {
        return priceDelta >= 0;
    }

    public boolean isValid() {
        return symbol != null;
    }
}