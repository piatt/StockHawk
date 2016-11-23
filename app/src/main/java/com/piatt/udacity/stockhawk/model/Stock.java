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

    private final String DELTA_PATTERN = "([+-]*)(\\d+\\.\\d+)(%*)";
    private final Pattern pattern;

    public Stock() {
        pattern = Pattern.compile(DELTA_PATTERN);
    }

    public String getPrice() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        return decimalFormat.format(Float.parseFloat(price));
    }

    public String getPriceDelta() {
        if (priceDelta != null) {
            Matcher matcher = pattern.matcher(priceDelta);
            if (matcher.find()) {
                DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
                decimalFormat.setMinimumFractionDigits(2);
                decimalFormat.setMaximumFractionDigits(2);
                return matcher.group(1)
                        .concat(decimalFormat.format(Float.parseFloat(matcher.group(2))));
            }
        }
        return priceDelta;
    }

    public String getPercentDelta() {
        if (percentDelta != null) {
            Matcher matcher = pattern.matcher(percentDelta);
            if (matcher.find()) {
                DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
                decimalFormat.setMinimumFractionDigits(2);
                decimalFormat.setMaximumFractionDigits(2);
                return matcher.group(1)
                        .concat(decimalFormat.format(Float.parseFloat(matcher.group(2))))
                        .concat(matcher.group(3));
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