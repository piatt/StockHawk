package com.piatt.udacity.stockhawk.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockActivity extends RxAppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
//    @BindView(R.id.name_view) TextView nameView;
//    @BindView(R.id.current_price_view) TextView currentPriceView;
//    @BindView(R.id.price_delta_view) TextView priceDeltaView;
//    @BindView(R.id.percent_delta_view) TextView percentDeltaView;
    @BindView(R.id.close_price_view) TextView closePriceView;
    @BindView(R.id.open_price_view) TextView openPriceView;
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

    private StorageManager storageManager = StorageManager.getManager();
    private static final String STOCK_KEY = "STOCK_KEY";

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

        String symbol = getIntent().getStringExtra(STOCK_KEY);
        configureToolbar(symbol);
        configureStockView(symbol);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureToolbar(String symbol) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(symbol);
    }

    private void configureStockView(String symbol) {
        Stock stock = storageManager.getStock(symbol);
//        nameView.setText(stock.getName());
//        currentPriceView.setText(stock.getCurrentPrice());
//        priceDeltaView.setText(stock.getPriceDelta());
//        percentDeltaView.setText(stock.getPercentDelta());
//        int deltaColor = stock.hasPositiveDelta() ? Color.GREEN : Color.RED;
//        priceDeltaView.setTextColor(deltaColor);
//        percentDeltaView.setTextColor(deltaColor);
//        itemView.setContentDescription(getContentDescription(stock));
        closePriceView.setText(stock.getClosePrice());
        openPriceView.setText(stock.getOpenPrice());
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
}