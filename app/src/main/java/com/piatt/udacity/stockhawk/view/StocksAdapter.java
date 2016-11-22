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
import com.piatt.udacity.stockhawk.model.Stock;
import com.piatt.udacity.stockhawk.view.StocksAdapter.StockViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StocksAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private List<Stock> stocks = new ArrayList<>();

    public void addOrUpdateStock(Stock stock) {
        int position = stocks.indexOf(stock);
        if (position == -1) {
            stocks.add(stock);
            notifyItemInserted(stocks.size() - 1);
        } else {
            notifyItemChanged(position, stock);
        }
        //TODO: Remove once https://github.com/JakeWharton/RxBinding/issues/310 is resolved
        notifyDataSetChanged();
    }

    public void addStock(Stock stock, int position) {
        stocks.add(position, stock);
        notifyItemInserted(position);
        //TODO: Remove once https://github.com/JakeWharton/RxBinding/issues/310 is resolved
        notifyDataSetChanged();
    }

    public Stock removeStock(int position) {
        Stock stock = stocks.remove(position);
        notifyItemRemoved(position);
        //TODO: Remove once https://github.com/JakeWharton/RxBinding/issues/310 is resolved
        notifyDataSetChanged();
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
        @BindView(R.id.company_view) TextView companyView;
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
            companyView.setText(stock.getCompany());
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