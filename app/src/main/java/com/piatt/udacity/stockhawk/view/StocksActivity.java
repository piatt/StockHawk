package com.piatt.udacity.stockhawk.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.LinearLayout;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.jakewharton.rxbinding.support.design.widget.RxSnackbar;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.manager.ApiManager;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StocksActivity extends RxAppCompatActivity {
    @BindView(R.id.refresh_button) AppCompatImageButton refreshButton;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.message_layout) LinearLayout messageLayout;
    @BindView(R.id.stocks_view) RecyclerView stocksView;
    @BindView(R.id.add_button) FloatingActionButton addButton;

    private StocksAdapter stocksAdapter;
    private StorageManager storageManager = StorageManager.getManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stocks_activity);
        ButterKnife.bind(this);

        configureStocksView();
        configureRefreshViews();
        configureAddButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateStocks();
    }

    private void configureStocksView() {
        stocksAdapter = new StocksAdapter();
        stocksView.setLayoutManager(new LinearLayoutManager(this));
        stocksView.setAdapter(stocksAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(stockItemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(stocksView);

        storageManager.onStockAdded()
                .compose(bindToLifecycle())
                .subscribe(stock -> {
                    int lastPosition = stocksView.getAdapter().getItemCount() - 1;
                    stocksView.smoothScrollToPosition(lastPosition);
                });
    }

    private void configureRefreshViews() {
        refreshLayout.setColorSchemeColors(Color.WHITE);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.accent);
        refreshLayout.setOnRefreshListener(this::updateStocks);

        RxView.clicks(refreshButton)
                .compose(bindToLifecycle())
                .subscribe(click -> updateStocks());

        storageManager.onSizeChanged()
                .compose(bindToLifecycle())
                .filter(size -> size == 0 || size == 1)
                .subscribe(size -> {
                    boolean isEmpty = size == 0;
                    RxView.visibility(messageLayout).call(isEmpty);
                    RxView.visibility(refreshButton).call(!isEmpty);
                    refreshLayout.setEnabled(!isEmpty);
                });
    }

    private void configureAddButton() {
        RxView.clicks(addButton)
                .compose(bindToLifecycle())
                .subscribe(click -> new StockSearchDialog().show(getSupportFragmentManager(), StockSearchDialog.class.getName()));

        RxRecyclerView.scrollEvents(stocksView)
                .compose(bindToLifecycle())
                .subscribe(event -> {
                    if (event.dy() > 0) {
                        addButton.hide();
                    } else if (event.dy() <= 0) {
                        addButton.show();
                    }
                });
    }

    private void updateStocks() {
        if (StockHawkApplication.getApp().isNetworkAvailable()) {
            List<String> symbols = Stream.of(storageManager.getStocks())
                    .map(Stock::getSymbol)
                    .collect(Collectors.toList());

            ApiManager.getManager().getStocks(symbols)
                    .doOnSubscribe(() -> {
                        if (!refreshLayout.isRefreshing()) {
                            refreshLayout.setRefreshing(true);
                        }
                    })
                    .doOnNext(stocks -> storageManager.updateStocks(stocks))
                    .doOnError(error -> Snackbar.make(messageLayout, R.string.error_message, Snackbar.LENGTH_LONG).show())
                    .doOnCompleted(() -> {
                        refreshLayout.setRefreshing(false);
                        if (storageManager.getStocks().size() > 0) {
                            Snackbar.make(messageLayout, R.string.data_message, Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .subscribe();
        } else {
            refreshLayout.setRefreshing(false);
            Snackbar.make(messageLayout, R.string.connection_message, Snackbar.LENGTH_LONG).show();
        }
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

            Snackbar snackbar = Snackbar.make(messageLayout, getString(R.string.remove_message, stock.getSymbol()), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.remove_action, click -> {
                stocksAdapter.addStock(stock, position);
                stocksView.smoothScrollToPosition(position);
            });
            snackbar.show();

            RxSnackbar.dismisses(snackbar)
                    .compose(bindToLifecycle())
                    .subscribe(event -> {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            StorageManager.getManager().removeStock(stock);
                        }
                    });
        }
    };
}