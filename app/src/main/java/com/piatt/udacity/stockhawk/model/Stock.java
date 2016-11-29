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
    @Getter @SerializedName("Symbol") private String symbol;
    @Getter @SerializedName("Name") private String name;
    @SerializedName("LastTradePriceOnly") private String price;
    @SerializedName("Change") private String priceDelta;
    @SerializedName("PercentChange") private String percentDelta;
    @SerializedName("Open") private String open;
    @SerializedName("PreviousClose") private String close;
    @SerializedName("DaysLow") private String dayLow;
    @SerializedName("DaysHigh") private String dayHigh;
    @SerializedName("YearLow") private String yearLow;
    @SerializedName("YearHigh") private String yearHigh;
    @SerializedName("MarketCapitalization") private String marketCap;
    @SerializedName("Volume") private String volume;
    @SerializedName("AverageDailyVolume") private String averageVolume;
    @SerializedName("EPSEstimateCurrentYear") private String eps;
    @SerializedName("PEGRatio") private String pegRatio;
    @Getter @Setter private String timestamp;

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