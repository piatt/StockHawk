package com.piatt.udacity.stockhawk.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.piatt.udacity.stockhawk.R;
import com.piatt.udacity.stockhawk.StockHawkApplication;
import com.piatt.udacity.stockhawk.manager.StockManager;
import com.trello.rxlifecycle.components.support.RxAppCompatDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class StockSearchDialog extends RxAppCompatDialogFragment {
    @BindView(R.id.search_layout) TextInputLayout searchLayout;
    @BindView(R.id.search_view) TextInputEditText searchView;
    @BindView(R.id.add_button) AppCompatButton addButton;

    private boolean restarted;
    private StockManager stockManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stockManager = StockHawkApplication.getApp().getStockManager();
        restarted = savedInstanceState != null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.stock_search_dialog, null);
        ButterKnife.bind(this, view);

        configureSearchView();
        configureSearchActions();

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.search_title)
                .setView(view)
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void configureSearchView() {
        RxTextView.textChanges(searchView)
                .compose(bindToLifecycle())
                .filter(text -> text.length() == 0)
                .subscribe(change -> searchLayout.setError(null));
    }

    private void configureSearchActions() {
        Observable<Void> addButtonObservable = RxView.clicks(addButton);
        Observable<Integer> searchViewObservable = RxTextView.editorActions(searchView)
                .filter(actionId -> actionId == EditorInfo.IME_ACTION_DONE);

        Observable.merge(addButtonObservable, searchViewObservable)
                .compose(bindToLifecycle())
                .filter(actionId -> searchView.getText().length() > 0)
                .subscribe(actionId -> {
                    String symbol = searchView.getText().toString();
                    stockManager.addStock(symbol);
                });

        stockManager.onStockSearchEvent(restarted)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.isComplete()) {
                        dismissAllowingStateLoss();
                    } else if (searchView.getText().length() > 0) {
                        searchLayout.setError(event.getMessage());
                    }
                });
    }
}