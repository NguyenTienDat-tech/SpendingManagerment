package com.example.spendingmanagerment.ui.home_user.activitymain;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.core.base.UiEvent;
import com.example.spendingmanagerment.data.repository.ExcelRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private final ExcelRepository repository;
    private final MutableLiveData<MainState> state = new MutableLiveData<>(new MainState(false));
    private final MutableLiveData<UiEvent<MainEvent>> event = new MutableLiveData<>();

    @Inject public MainViewModel(ExcelRepository repository) {
        this.repository = repository;
    }

    public LiveData<MainState> getState() {
        return state;
    }

    public LiveData<UiEvent<MainEvent>> getEvent() {
        return event;
    }

    public void exportExcelClick() {
        emit(MainEvent.Type.EXPORT_PICKER, null);
    }

    public void importExcelClick() {
        emit(MainEvent.Type.IMPORT_PICKER, null);
    }

    public void clearDataClick() {
        emit(MainEvent.Type.CONFIRM_CLEAR, null);
    }

    public void onImportFileSelected(Uri uri) {
        if (uri != null) emit(MainEvent.Type.CONFIRM_IMPORT, uri);
    }

    public void exportExcel(Uri uri) {
        if (uri == null || isBusy()) return;
        state.setValue(new MainState(true));
        repository.exportExcel(uri, (success, count) -> complete(success, count,
                R.string.excel_export_success, R.string.excel_export_error));
    }

    public void importExcel(Uri uri) {
        if (uri == null || isBusy()) return;
        state.setValue(new MainState(true));
        repository.importExcel(uri, (success, count) -> complete(success, count,
                R.string.excel_import_success, R.string.excel_import_error));
    }

    public void clearData() {
        if (isBusy()) return;
        state.setValue(new MainState(true));
        repository.clearData((success, count) -> complete(success, count,
                R.string.clear_data_success, R.string.clear_data_error));
    }

    private boolean isBusy() {
        return state.getValue() != null && state.getValue().busy;
    }

    private void emit(MainEvent.Type type, Uri uri) {
        if (!isBusy()) event.setValue(new UiEvent<>(new MainEvent(type, uri, 0, 0)));
    }

    private void complete(boolean success, int count, int successRes, int errorRes) {
        state.setValue(new MainState(false));
        event.setValue(new UiEvent<>(new MainEvent(MainEvent.Type.MESSAGE, null, success ? successRes : errorRes, count)));
    }
}
