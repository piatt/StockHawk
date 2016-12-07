package com.piatt.udacity.stockhawk.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.design.widget.RxSnackbar;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.manager.StockManager;
import com.piatt.udacity.stockhawk.manager.StockManager.StocksEvent;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class StocksActivity extends RxAppCompatActivity {
    @BindView(R.id.timestamp_view) TextView timestampView;
    @BindView(R.id.refresh_button) AppCompatImageButton refreshButton;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.message_layout) LinearLayout messageLayout;
    @BindView(R.id.stocks_view) RecyclerView stocksView;
    @BindView(R.id.add_button) FloatingActionButton addButton;

    private final String STOCK_VIEW_KEY = "STOCK_VIEW_KEY";
    private StocksAdapter stocksAdapter;
    private StockManager stockManager;
    private StorageManager storageManager;
    private boolean restarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stocks_activity);
        ButterKnife.bind(this);

        stockManager = StockHawkApplication.getApp().getStockManager();
        storageManager = StockHawkApplication.getApp().getStorageManager();
        restarted = savedInstanceState != null;

        configureStocksView();
        configureRefreshViews();
        configureAddButton();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Parcelable stockViewState = stocksView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(STOCK_VIEW_KEY, stockViewState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STOCK_VIEW_KEY)) {
            Parcelable stockViewState = savedInstanceState.getParcelable(STOCK_VIEW_KEY);
            stocksView.getLayoutManager().onRestoreInstanceState(stockViewState);
        }
    }

    private void configureStocksView() {
        stocksAdapter = new StocksAdapter(this);
        stocksView.setLayoutManager(new LinearLayoutManager(this));
        stocksView.setAdapter(stocksAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(stockItemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(stocksView);

        if (!restarted) {
            stockManager.updateStocks();
        }

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
        refreshLayout.setOnRefreshListener(() -> stockManager.updateStocks());

        RxView.clicks(refreshButton)
                .compose(bindToLifecycle())
                .subscribe(click -> stockManager.updateStocks());

        Observable<StocksEvent> onStocksEvent = stockManager.onStocksEvent()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread());

        if (restarted && !stockManager.hasPendingUpdate()) {
            onStocksEvent = onStocksEvent.skip(1);
        }

        onStocksEvent.subscribe(stocksEvent -> {
            refreshLayout.setRefreshing(stocksEvent.isUpdating());
            String message = stocksEvent.getMessage();
            if (message != null) {
                Snackbar.make(messageLayout, message, Snackbar.LENGTH_LONG).show();
            }
        });

        storageManager.onSizeChanged()
                .compose(bindToLifecycle())
                .filter(size -> size == 0 || size == 1)
                .subscribe(size -> {
                    boolean isEmpty = size == 0;
                    RxView.visibility(messageLayout).call(isEmpty);
                    RxView.visibility(timestampView).call(!isEmpty);
                    RxView.visibility(refreshButton).call(!isEmpty);
                    refreshLayout.setEnabled(!isEmpty);
                });

        storageManager.onTimestampChanged()
                .compose(bindToLifecycle())
                .subscribe(timestamp -> timestampView.setText(getString(R.string.timestamp_message, timestamp)));
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
                            storageManager.removeStock(stock);
                        }
                    });
        }
    };
}