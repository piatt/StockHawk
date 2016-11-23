package com.piatt.udacity.stockhawk.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.support.design.widget.RxSnackbar;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.manager.ApiManager;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class StocksActivity extends RxAppCompatActivity {
    @BindView(R.id.refresh_button) AppCompatImageButton refreshButton;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.message_layout) LinearLayout messageLayout;
    @BindView(R.id.stocks_view) RecyclerView stocksView;
    @BindView(R.id.add_button) FloatingActionButton addButton;

    private StockAdapter stockAdapter;
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
        Log.d("TEST", "onStart");
        updateStocks();
    }

    private void configureStocksView() {
        stockAdapter = new StockAdapter();
        stocksView.setLayoutManager(new LinearLayoutManager(this));
        stocksView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        stocksView.setAdapter(stockAdapter);

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
            Observable.from(storageManager.getStocks())
                    .map(Stock::getSymbol)
                    .doOnSubscribe(() -> {
                        if (!refreshLayout.isRefreshing()) {
                            refreshLayout.setRefreshing(true);
                        }
                    })
                    .doOnNext(this::updateStock)
                    .doOnCompleted(() -> {
                        refreshLayout.setRefreshing(false);
                        if (storageManager.getStocks().size() > 0) {
                            Snackbar.make(messageLayout, R.string.data_message, Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        } else {
            refreshLayout.setRefreshing(false);
            Snackbar.make(messageLayout, R.string.connection_message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateStock(String symbol) {
        ApiManager.getManager().getStock(symbol)
                .subscribe(stock -> {
                    storageManager.updateStock(stock);
                }, error -> {
                    Snackbar.make(messageLayout, R.string.error_message, Snackbar.LENGTH_LONG).show();
                });
    }

    private ItemTouchHelper.Callback stockItemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START | ItemTouchHelper.END) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Stock stock = stockAdapter.removeStock(position);

            Snackbar snackbar = Snackbar.make(messageLayout, getString(R.string.remove_message, stock.getSymbol()), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.remove_action, click -> {
                stockAdapter.addStock(stock, position);
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