package com.piatt.udacity.stockhawk.model;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = "symbol")
public class Stock {
    @SerializedName("Symbol") private String symbol;
    @SerializedName("LastTradePriceOnly") private String currentPrice;
    @SerializedName("Change") private String priceDelta;
    @SerializedName("PercentChange") private String percentDelta;
    @SerializedName("PreviousClose") private String lastClosePrice;
    @SerializedName("Open") private String openPrice;
    @SerializedName("Close") private String closePrice;
    @SerializedName("Low") private String lowPrice;
    @SerializedName("High") private String highPrice;
    @SerializedName("DaysLow") private String dayLowPrice;
    @SerializedName("DaysHigh") private String dayHighPrice;
    @SerializedName("YearLow") private String yearLowPrice;
    @SerializedName("YearHigh") private String yearHighPrice;
    @SerializedName("EPSEstimateCurrentYear") private String eps;
    @SerializedName("PEGRatio") private String pegRatio;
    @Getter @SerializedName("Name") private String name;
    @Getter @SerializedName("Date") private String date;
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
        return getPriceAsFormattedString(currentPrice);
    }

    public String getLastClosePrice() {
        return getPriceAsFormattedString(lastClosePrice);
    }

    public String getLastOpenPrice() {
        return getPriceAsFormattedString(openPrice);
    }

    public float getOpenPrice() {
        return getPriceAsFormattedDecimal(openPrice);
    }

    public float getClosePrice() {
        return getPriceAsFormattedDecimal(closePrice);
    }

    public float getLowPrice() {
        return getPriceAsFormattedDecimal(lowPrice);
    }

    public float getHighPrice() {
        return getPriceAsFormattedDecimal(highPrice);
    }

    public String getDayLowPrice() {
        return getPriceAsFormattedString(dayLowPrice);
    }

    public String getDayHighPrice() {
        return getPriceAsFormattedString(dayHighPrice);
    }

    public String getYearLowPrice() {
        return getPriceAsFormattedString(yearLowPrice);
    }

    public String getYearHighPrice() {
        return getPriceAsFormattedString(yearHighPrice);
    }

    public String getEps() {
        return getPriceAsFormattedString(eps);
    }

    public String getPegRatio() {
        return getPriceAsFormattedString(pegRatio);
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

    private String getPriceAsFormattedString(String price) {
        return price != null ? decimalFormat.format(Float.parseFloat(price)) : null;
    }

    private float getPriceAsFormattedDecimal(String price) {
        try {
            return decimalFormat.parse(price).floatValue();
        } catch (ParseException e) {
            return 0;
        }
    }
}