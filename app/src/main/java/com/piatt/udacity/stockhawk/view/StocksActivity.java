package com.piatt.udacity.stockhawk.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.manager.ApiManager;
import com.piatt.udacity.stockhawk.model.Stock;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class StocksActivity extends AppCompatActivity {
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.stocks_view) RecyclerView stocksView;
    @BindView(R.id.empty_view) TextView emptyView;

    private StocksAdapter stocksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stocks_activity);
        ButterKnife.bind(this);

        configureStocksView();
        refreshLayout.setOnRefreshListener(this::fetchStocks);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchStocks();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void networkToast() {
        Toast.makeText(this, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    private void configureStocksView() {
        stocksAdapter = new StocksAdapter();
        stocksView.setLayoutManager(new LinearLayoutManager(this));
        stocksView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        stocksView.setAdapter(stocksAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(stockItemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(stocksView);
    }

    private void fetchStocks() {
        List<String> symbols = Arrays.asList("AAPL", "MSFT", "YHOO", "TSLA", "GOOG");

        Observable.from(symbols)
                .doOnSubscribe(() -> {
                    Log.d("TEST", "doOnSubscribe");
                    if (!refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(true);
                    }
                })
                .doOnNext(symbol -> {
                    Log.d("TEST", "doOnNext: " + symbol);
                    ApiManager.getApiManager().getQuote(symbol)
                            .subscribe(stock -> {
                                Log.d("TEST", "getQuote: " + symbol + " isValid: " + stock.isValid());
                                if (stock.isValid()) {
                                    stocksAdapter.addStock(stock);
                                }
                            });
                })
                .doOnCompleted(() -> {
                    Log.d("TEST", "doOnCompleted");
                    refreshLayout.setRefreshing(false);
                })
                .doOnError(Throwable::printStackTrace)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private ItemTouchHelper.Callback stockItemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START | ItemTouchHelper.END) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Stock stock = stocksAdapter.removeStock(position);

            Snackbar snackbar = Snackbar.make(stocksView, getString(R.string.stock_removal_message, stock.getSymbol()), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.stock_removal_action, view -> stocksAdapter.addStock(stock, position));
            snackbar.show();
        }
    };

    @OnClick(R.id.refresh_button)
    public void onRefreshButtonClick() {
        fetchStocks();
    }

    @OnClick(R.id.add_button)
    public void onAddButtonClick() {
        if (isConnected()) {
            new MaterialDialog.Builder(StocksActivity.this).title(R.string.symbol_search)
                    .content(R.string.content_test)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(R.string.input_hint, R.string.input_prefill, (dialog, input) -> {
                        // On FAB click, receive user input. Make sure the stock doesn't already exist
                        // in the DB and proceed accordingly
                    })
                    .show();
        } else {
            networkToast();
        }
    }
}