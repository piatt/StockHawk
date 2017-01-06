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
    @SerializedName("Symbol") private String symbol;
    @Getter @SerializedName("Name") private String name;
    @SerializedName("LastTradePriceOnly") private String currentPrice;
    @SerializedName("Change") private String priceDelta;
    @SerializedName("PercentChange") private String percentDelta;
    @SerializedName("PreviousClose") private String closePrice;
    @SerializedName("Open") private String openPrice;
    @SerializedName("DaysLow") private String dayLowPrice;
    @SerializedName("DaysHigh") private String dayHighPrice;
    @SerializedName("YearLow") private String yearLowPrice;
    @SerializedName("YearHigh") private String yearHighPrice;
    @SerializedName("EPSEstimateCurrentYear") private String eps;
    @SerializedName("PEGRatio") private String pegRatio;
    @Getter @SerializedName("Volume") private String volume;
    @Getter @SerializedName("AverageDailyVolume") private String averageVolume;
    @Getter @SerializedName("MarketCapitalization") private String marketCap;
    @Getter @Setter private String timestamp;

    private transient final String DELTA_PATTERN = "([+-]*)(\\d+\\.\\d+)(%*)";
    private transient final DecimalFormat decimalFormat;
    private transient final Pattern deltaPattern;

    public Stock() {
        decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        deltaPattern = Pattern.compile(DELTA_PATTERN);
    }

    public boolean isValid() {
        return name != null;
    }

    public String getSymbol() {
        return symbol != null ? symbol.toUpperCase() : null;
    }

    public String getCurrentPrice() {
        return getFormattedDecimal(currentPrice);
    }

    public String getClosePrice() {
        return getFormattedDecimal(closePrice);
    }

    public String getOpenPrice() {
        return getFormattedDecimal(openPrice);
    }

    public String getDayLowPrice() {
        return getFormattedDecimal(dayLowPrice);
    }

    public String getDayHighPrice() {
        return getFormattedDecimal(dayHighPrice);
    }

    public String getYearLowPrice() {
        return getFormattedDecimal(yearLowPrice);
    }

    public String getYearHighPrice() {
        return getFormattedDecimal(yearHighPrice);
    }

    public String getEps() {
        return getFormattedDecimal(eps);
    }

    public String getPegRatio() {
        return getFormattedDecimal(pegRatio);
    }

    public String getPriceDelta() {
        if (priceDelta != null) {
            Matcher matcher = deltaPattern.matcher(priceDelta);
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
            Matcher matcher = deltaPattern.matcher(percentDelta);
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

    private String getFormattedDecimal(String price) {
        return price != null ? decimalFormat.format(Float.parseFloat(price)) : null;
    }
}