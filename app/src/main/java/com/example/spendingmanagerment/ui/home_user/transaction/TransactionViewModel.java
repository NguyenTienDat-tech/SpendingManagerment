package com.example.spendingmanagerment.ui.home_user.transaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.spendingmanagerment.core.base.UiEvent;
import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.data.repository.TransactionRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class TransactionViewModel extends ViewModel {
    private final TransactionRepository repository;
    private final SavedStateHandle savedStateHandle;


    private final MediatorLiveData<TransactionState> state = new MediatorLiveData<>();
    private final MutableLiveData<UiEvent<TransactionEvent>> event = new MutableLiveData<>();


    private LiveData<List<TransactionEntity>> transactions;
    private LocalDate date;
    private boolean monthly;


    @Inject
    public TransactionViewModel(TransactionRepository repository, SavedStateHandle savedStateHandle) {
        this.repository = repository;
        this.savedStateHandle = savedStateHandle;
        Long day = savedStateHandle.get("day");
        date = day == null ? LocalDate.now() : LocalDate.ofEpochDay(day);
        monthly = Boolean.TRUE.equals(savedStateHandle.get("monthly"));
        loadTransactions();
    }


    public LiveData<TransactionState> getState() { return state; }
    public LiveData<UiEvent<TransactionEvent>> getEvent() { return event; }


    public void onPeriodChange(boolean monthly) {
        this.monthly = monthly;
        loadTransactions();
    }


    public void onDateChange(LocalDate date) {
        this.date = date;
        loadTransactions();
    }


    public void previousDateClick() {
        onDateChange(monthly ? date.minusMonths(1) : date.minusDays(1));
    }
    public void nextDateClick() {
        onDateChange(monthly ? date.plusMonths(1) : date.plusDays(1));
    }
    public void calendarClick() {
        event.setValue(new UiEvent<>(new TransactionEvent(TransactionEvent.Type.SHOW_DATE_PICKER, 0)));
    }
    public void addTransactionClick() {
        itemClick(0);
    }
    public void itemClick(long id) {
        event.setValue(new UiEvent<>(new TransactionEvent(TransactionEvent.Type.OPEN_EDITOR, id)));
    }


    private void loadTransactions() {
        savedStateHandle.set("day", date.toEpochDay());
        savedStateHandle.set("monthly", monthly);
        if (transactions != null) state.removeSource(transactions);
        LocalDate start = monthly ? date.withDayOfMonth(1) : date;
        LocalDate end = monthly ? start.plusMonths(1) : start.plusDays(1);
        state.setValue(new TransactionState(date, monthly, Collections.emptyList()));
        transactions = repository.listTransactions(DateUtils.startOfDay(start), DateUtils.startOfDay(end));
        state.addSource(transactions, list -> state.setValue(new TransactionState(date, monthly, list)));
    }
}
