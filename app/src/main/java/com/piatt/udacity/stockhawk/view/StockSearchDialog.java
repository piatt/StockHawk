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
import com.piatt.udacity.stockhawk.manager.ApiManager;
import com.piatt.udacity.stockhawk.manager.StorageManager;
import com.trello.rxlifecycle.components.support.RxAppCompatDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class StockSearchDialog extends RxAppCompatDialogFragment {
    @BindView(R.id.search_layout) TextInputLayout searchLayout;
    @BindView(R.id.search_view) TextInputEditText searchView;
    @BindView(R.id.add_button) AppCompatButton addButton;

    private ApiManager apiManager = StockHawkApplication.getApp().getApiManager();
    private StorageManager storageManager = StockHawkApplication.getApp().getStorageManager();

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
                .flatMap(action -> {
                    if (StockHawkApplication.getApp().isNetworkAvailable()) {
                        String symbol = searchView.getText().toString();
                        return apiManager.getStock(symbol);
                    } else {
                        searchLayout.setError(getString(R.string.connection_message));
                        return Observable.empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(stock -> {
                    boolean valid = stock.isValid();
                    if (!valid) {
                        searchLayout.setError(getString(R.string.invalid_message));
                    }
                    return valid;
                })
                .filter(stock -> {
                    boolean duplicateStock = storageManager.hasStock(stock);
                    if (duplicateStock) {
                        searchLayout.setError(getString(R.string.duplicate_message));
                    }
                    return !duplicateStock;
                })
                .doOnError(error -> searchLayout.setError(getString(R.string.error_message)))
                .retry()
                .subscribe(stock -> {
                    storageManager.addStock(stock);
                    dismiss();
                }, error -> {});
    }
}