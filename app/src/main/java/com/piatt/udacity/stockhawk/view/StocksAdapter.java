package com.piatt.udacity.stockhawk.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.piatt.udacity.stockhawk.view.StocksAdapter.StockViewHolder;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

public class StocksAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private Context context;
    private List<Stock> stocks;

    public StocksAdapter(Context context) {
        this.context = context;

        StorageManager storageManager = StockHawkApplication.getApp().getStorageManager();
        stocks = storageManager.getStocks();

        storageManager.onStocksUpdated()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateStocks);
    }

    private void updateStocks(List<Stock> stocks) {
        this.stocks = stocks;
        notifyDataSetChanged();
    }

    public int addStock(Stock stock) {
        int position = getItemCount();
        addStock(stock, position);
        return position;
    }

    public void addStock(Stock stock, int position) {
        stocks.add(position, stock);
        notifyItemInserted(position);
    }

    public Stock removeStock(int position) {
        Stock stock = stocks.remove(position);
        notifyItemRemoved(position);
        return stock;
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StockViewHolder viewHolder, int position) {
        viewHolder.onBind(stocks.get(position));
    }

    public class StockViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.symbol_view) TextView symbolView;
        @BindView(R.id.name_view) TextView nameView;
        @BindView(R.id.current_price_view) TextView currentPriceView;
        @BindView(R.id.price_delta_view) TextView priceDeltaView;
        @BindView(R.id.percent_delta_view) TextView percentDeltaView;
        @BindString(R.string.stock_item_message) String stockItemMessage;

        public StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            RxView.clicks(itemView).subscribe(click -> onClick());
        }

        public void onBind(Stock stock) {
            symbolView.setText(stock.getSymbol());
            nameView.setText(stock.getName());
            currentPriceView.setText(stock.getCurrentPrice());
            priceDeltaView.setText(stock.getPriceDelta());
            percentDeltaView.setText(stock.getPercentDelta());
            int deltaColor = stock.hasPositiveDelta() ? Color.GREEN : Color.RED;
            priceDeltaView.setTextColor(deltaColor);
            percentDeltaView.setTextColor(deltaColor);
            itemView.setContentDescription(getContentDescription(stock));
        }

        private String getContentDescription(Stock stock) {
            return String.format(stockItemMessage, stock.getSymbol(), stock.getName(), stock.getCurrentPrice(), stock.getPriceDelta(), stock.getPercentDelta());
        }

        private void onClick() {
            String symbol = stocks.get(getAdapterPosition()).getSymbol();
            context.startActivity(StockActivity.buildIntent(context, symbol));
        }
    }
}