package com.piatt.udacity.stockhawk.model;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = "symbol")
public class Stock {
    @Getter @Setter @SerializedName("Symbol") String symbol;
    @Getter @Setter @SerializedName("Name") String name;
    @Setter @SerializedName("LastTradePriceOnly") String price;
    @Setter @SerializedName("Change") String priceDelta;
    @Setter @SerializedName("PercentChange") String percentDelta;

    private transient final String DELTA_PATTERN = "([+-]*)(\\d+\\.\\d+)(%*)";
    private transient final DecimalFormat decimalFormat;
    private transient final Pattern pattern;

    public Stock() {
        decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        pattern = Pattern.compile(DELTA_PATTERN);
    }

    public String getPrice() {
        return price != null ? decimalFormat.format(Float.parseFloat(price)) : price;
    }

    public String getPriceDelta() {
        if (priceDelta != null) {
            Matcher matcher = pattern.matcher(priceDelta);
            if (matcher.find()) {
                String sign = matcher.group(1);
                float number = Float.parseFloat(matcher.group(2));
                return sign.concat(decimalFormat.format(number));
            }
        }
        return priceDelta;
    }

    public String getPercentDelta() {
        if (percentDelta != null) {
            Matcher matcher = pattern.matcher(percentDelta);
            if (matcher.find()) {
                String sign = matcher.group(1);
                float number = Float.parseFloat(matcher.group(2));
                String percent = matcher.group(3);
                return sign.concat(decimalFormat.format(number)).concat(percent);
            }
        }
        return percentDelta;
    }


    public boolean hasPositiveDelta() {
        return priceDelta != null && Float.parseFloat(priceDelta) >= 0;
    }

    public boolean isValid() {
        return name != null;
    }
}