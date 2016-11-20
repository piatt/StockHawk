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

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerViewAdapter;
import com.jakewharton.rxbinding.view.RxView;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StocksApplication;
import com.piatt.udacity.stockhawk.manager.ApiManager;
import com.piatt.udacity.stockhawk.manager.PreferencesManager;
import com.piatt.udacity.stockhawk.model.Stock;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.Set;

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

    private StocksAdapter stocksAdapter;

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
    public void onResume() {
        super.onResume();
        fetchStocks();
    }

    private void configureStocksView() {
        stocksAdapter = new StocksAdapter();
        stocksView.setLayoutManager(new LinearLayoutManager(this));
        stocksView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        stocksView.setAdapter(stocksAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(stockItemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(stocksView);

        RxRecyclerViewAdapter.dataChanges(stocksAdapter)
                .compose(bindToLifecycle())
                .subscribe(adapter -> {
                    boolean isEmpty = adapter.getItemCount() <= 0;
                    RxView.visibility(messageLayout).call(isEmpty);
                });
    }

    private void configureRefreshViews() {
        refreshLayout.setColorSchemeColors(Color.WHITE);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.accent);
        refreshLayout.setOnRefreshListener(this::fetchStocks);
        RxView.clicks(refreshButton)
                .compose(bindToLifecycle())
                .subscribe(click -> fetchStocks());
    }

    private void configureAddButton() {
        RxView.clicks(addButton)
                .compose(bindToLifecycle())
                .subscribe(click -> {
                    Log.d("TEST", "Add a stock!");
                });

//        RxRecyclerView.scrollStateChanges(stocksView)
//                .compose(bindToLifecycle())
//                .subscribe(state -> {
//                    switch (state) {
//                        case SCROLL_STATE_IDLE: addButton.show();
//                            break;
//                        case SCROLL_STATE_DRAGGING: addButton.hide();
//                            break;
//                    }
//                });

//        RxRecyclerView.scrollEvents(stocksView)
//                .compose(bindToLifecycle())
//                .subscribe(state -> {
//                    Log.d("TEST", "dy: " + state.dy());
//                });
    }

    private void fetchStocks() {
        if (StocksApplication.getApp().isNetworkAvailable()) {
            Set<String> symbols = PreferencesManager.getManager().getSavedSymbols().get();
            Observable.from(symbols)
                    .doOnSubscribe(() -> {
                        if (!refreshLayout.isRefreshing()) {
                            refreshLayout.setRefreshing(true);
                        }
                    })
                    .doOnNext(this::fetchStock)
                    .doOnCompleted(() -> {
                        refreshLayout.setRefreshing(false);
                        Snackbar.make(messageLayout, R.string.data_message, Snackbar.LENGTH_LONG).show();
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
        else {
            refreshLayout.setRefreshing(false);
            Snackbar.make(messageLayout, R.string.connection_message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void fetchStock(String symbol) {
        ApiManager.getManager().getQuote(symbol)
                .subscribe(stock -> {
                    if (stock.isValid()) {
                        stocksAdapter.addStock(stock);
                    }
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
            Stock stock = stocksAdapter.removeStock(position);
            Snackbar.make(messageLayout, getString(R.string.remove_message, stock.getSymbol()), Snackbar.LENGTH_LONG)
                    .setAction(R.string.remove_action, view -> stocksAdapter.addStock(stock, position)).show();
        }
    };
}