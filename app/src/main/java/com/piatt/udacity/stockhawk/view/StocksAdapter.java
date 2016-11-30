package com.piatt.udacity.stockhawk.view;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.piatt.udacity.stockhawk.view.StocksAdapter.StockViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

public class StocksAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private List<Stock> stocks;

    public StocksAdapter() {
        this(new ArrayList<>());
    }

    public StocksAdapter(List<Stock> stocks) {
        this.stocks = stocks;
        notifyDataSetChanged();

        StorageManager storageManager = StorageManager.getManager();
        Observable.merge(storageManager.onStockAdded(), storageManager.onStockUpdated())
                .subscribe(this::addOrUpdateStock);
    }

    private void addOrUpdateStock(Stock stock) {
        int position = stocks.indexOf(stock);
        if (position == -1) {
            stocks.add(stock);
            notifyItemInserted(getItemCount() - 1);
        } else {
            stocks.remove(stock);
            stocks.add(position, stock);
            notifyItemChanged(position, null);
        }
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
        @BindView(R.id.price_view) TextView priceView;
        @BindView(R.id.price_delta_view) TextView priceDeltaView;
        @BindView(R.id.percent_delta_view) TextView percentDeltaView;

        public StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            RxView.clicks(itemView).subscribe(click -> onClick());
        }

        public void onBind(Stock stock) {
            symbolView.setText(stock.getSymbol());
            nameView.setText(stock.getName());
            priceView.setText(stock.getPrice());
            priceDeltaView.setText(stock.getPriceDelta());
            percentDeltaView.setText(stock.getPercentDelta());
            int deltaColor = stock.hasPositiveDelta() ? Color.GREEN : Color.RED;
            priceDeltaView.setTextColor(deltaColor);
            percentDeltaView.setTextColor(deltaColor);
        }

        private void onClick() {
            Snackbar.make(itemView, stocks.get(getAdapterPosition()).getSymbol() + " clicked!", Snackbar.LENGTH_SHORT).show();
        }
    }
}