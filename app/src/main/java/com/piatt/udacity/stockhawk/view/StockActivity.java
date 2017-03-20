package com.piatt.udacity.stockhawk.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.manager.StockManager;
import com.piatt.udacity.stockhawk.manager.StockManager.StockIntervals;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class StockActivity extends RxAppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.current_price_view) TextView currentPriceView;
    @BindView(R.id.price_delta_view) TextView priceDeltaView;
    @BindView(R.id.percent_delta_view) TextView percentDeltaView;
    @BindView(R.id.name_view) TextView nameView;
    @BindView(R.id.last_close_price_view) TextView lastClosePriceView;
    @BindView(R.id.last_open_price_view) TextView lastOpenPriceView;
    @BindView(R.id.day_low_price_view) TextView dayLowPriceView;
    @BindView(R.id.day_high_price_view) TextView dayHighPriceView;
    @BindView(R.id.year_low_price_view) TextView yearLowPriceView;
    @BindView(R.id.year_high_price_view) TextView yearHighPriceView;
    @BindView(R.id.eps_view) TextView epsView;
    @BindView(R.id.peg_ratio_view) TextView pegRatioView;
    @BindView(R.id.volume_view) TextView volumeView;
    @BindView(R.id.average_volume_view) TextView averageVolumeView;
    @BindView(R.id.market_cap_view) TextView marketCapView;
    @BindView(R.id.timestamp_view) TextView timestampView;
    @BindView(R.id.chart_view) CandleStickChart chartView;

    private static final String STOCK_KEY = "STOCK_KEY";
    private StockManager stockManager;
    private StorageManager storageManager;
    private String symbol;

    public static Intent buildIntent(Context context, String symbol) {
        Intent intent = new Intent(context, StockActivity.class);
        intent.putExtra(STOCK_KEY, symbol);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_activity);
        ButterKnife.bind(this);

        stockManager = StockHawkApplication.getApp().getStockManager();
        storageManager = StockHawkApplication.getApp().getStorageManager();
        symbol = getIntent().getStringExtra(STOCK_KEY);

        configureToolbar();
        configureStockView();
        configureChartView();
    }

    private void configureToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(symbol);

        RxToolbar.navigationClicks(toolbar)
                .compose(bindToLifecycle())
                .subscribe(click -> onBackPressed());
    }

    private void configureStockView() {
        Stock stock = storageManager.getStock(symbol);
        currentPriceView.setText(stock.getCurrentPrice());
        priceDeltaView.setText(stock.getPriceDelta());
        percentDeltaView.setText(stock.getPercentDelta());
        int deltaColor = stock.hasPositiveDelta() ? Color.GREEN : Color.RED;
        priceDeltaView.setTextColor(deltaColor);
        percentDeltaView.setTextColor(deltaColor);
        nameView.setText(stock.getName());
//        itemView.setContentDescription(getContentDescription(stock));
        lastClosePriceView.setText(stock.getLastClosePrice());
        lastOpenPriceView.setText(stock.getLastOpenPrice());
        dayLowPriceView.setText(stock.getDayLowPrice());
        dayHighPriceView.setText(stock.getDayHighPrice());
        yearLowPriceView.setText(stock.getYearLowPrice());
        yearHighPriceView.setText(stock.getYearHighPrice());
        epsView.setText(stock.getEps());
        pegRatioView.setText(stock.getPegRatio());
        volumeView.setText(stock.getVolume());
        averageVolumeView.setText(stock.getAverageVolume());
        marketCapView.setText(stock.getMarketCap());
        timestampView.setText(stock.getTimestamp());
    }

    private void configureChartView() {
        //TODO: Link stockInterval to buttons on chart
        chartView.invalidate();
        stockManager.fetchStockIntervals(symbol, StockIntervals.WEEK);

        AtomicInteger index = new AtomicInteger(-1);

        stockManager.onStockIntervalsEvent()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .map(stockIntervals -> Stream.of(stockIntervals)
                        .map(stockInterval -> new CandleEntry(index.incrementAndGet(), stockInterval.getHighPrice(), stockInterval.getLowPrice(), stockInterval.getOpenPrice(), stockInterval.getClosePrice()))
                        .collect(Collectors.toList()))
                .subscribe(entries -> {
                    CandleDataSet dataSet = new CandleDataSet(entries, "CHART");
                    CandleData lineData = new CandleData(dataSet);
                    chartView.setData(lineData);
                    chartView.invalidate();
                });
    }
}