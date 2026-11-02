package com.example.spendingmanagerment.ui.home_user.addtransaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.core.base.UiEvent;
import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.data.repository.TransactionRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.time.LocalDate;
import javax.inject.Inject;

@HiltViewModel
public class AddTransactionViewModel extends ViewModel {
    private static final String KEY_TRANSACTION_ID = "transactionId";
    private static final String KEY_SELECTED_DAY = "selectedDay";
    private static final String KEY_DRAFT_LOADED = "draftLoaded";
    private static final String KEY_DATE = "date";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_NOTE = "note";

    private static final long MAX_AMOUNT = 999_999_999_999L;

    private final TransactionRepository repository;
    private final SavedStateHandle savedStateHandle;


    private final MediatorLiveData<AddTransactionState> state = new MediatorLiveData<>();
    private final MutableLiveData<UiEvent<AddTransactionEvent>> event = new MutableLiveData<>();


    private final long id;
    private boolean busy;



    @Inject
    public AddTransactionViewModel(TransactionRepository repository, SavedStateHandle savedStateHandle) {
        this.repository = repository;
        this.savedStateHandle = savedStateHandle;

        Long transactionId = savedStateHandle.get(KEY_TRANSACTION_ID);
        id = transactionId == null ? 0 : transactionId;

        if (!savedStateHandle.contains(KEY_DATE)) {
            Long day = savedStateHandle.get(KEY_SELECTED_DAY);
            savedStateHandle.set(KEY_DATE, day == null || day == 0 ? LocalDate.now().toEpochDay() : day);
        }

        busy = id > 0 && !Boolean.TRUE.equals(savedStateHandle.get(KEY_DRAFT_LOADED));

        publishState();

        if (busy) {
            LiveData<TransactionEntity> transaction = repository.getTransaction(id);
            state.addSource(transaction, entity -> {
                state.removeSource(transaction);
                busy = false;


                if (entity == null) {
                    error(R.string.transaction_missing);
                    publishState();
                    return;
                }


                savedStateHandle.set(KEY_DATE, DateUtils.fromMillis(entity.date).toEpochDay());
                savedStateHandle.set(KEY_AMOUNT, Long.toString(entity.amount));
                savedStateHandle.set(KEY_TYPE, entity.type);
                savedStateHandle.set(KEY_CATEGORY, entity.category);
                savedStateHandle.set(KEY_ACCOUNT, entity.account);
                savedStateHandle.set(KEY_NOTE, entity.note);
                savedStateHandle.set(KEY_DRAFT_LOADED, true);


                publishState();
            });
        }
    }



    public LiveData<AddTransactionState> getState() {
        return state;
    }
    public LiveData<UiEvent<AddTransactionEvent>> getEvent() {
        return event;
    }



    public void onAmountChange(String amount) {
        update(KEY_AMOUNT, amount);
    }
    public void onTypeChange(String type) {
        update(KEY_TYPE, type);
    }
    public void onCategoryChange(String category) {
        update(KEY_CATEGORY, category);
    }
    public void onAccountChange(String account) {
        update(KEY_ACCOUNT, account);
    }
    public void onNoteChange(String note) {
        update(KEY_NOTE, note);
    }



    public void onDateChange(LocalDate date) {
        savedStateHandle.set(KEY_DATE, date.toEpochDay());
        publishState();
    }


    private void update(String key, String value) {
        savedStateHandle.set(key, value);
        publishState();
    }


    private String value(String key, String fallback) {
        String value = savedStateHandle.get(key);
        return value == null ? fallback : value;
    }


    private void publishState() {
        Long day = savedStateHandle.get(KEY_DATE);
        state.setValue(new AddTransactionState(id, LocalDate.ofEpochDay(day == null ? LocalDate.now().toEpochDay() : day),
                value(KEY_AMOUNT, ""), value(KEY_TYPE, TransactionEntity.EXPENSE), value(KEY_CATEGORY, ""),
                value(KEY_ACCOUNT, ""), value(KEY_NOTE, ""), busy));
    }


    public void saveTransactionClick() {
        if (busy) {
            return;
        }


        AddTransactionState current = state.getValue();


        if (current == null) {
            return;
        }


        long amount;
        try {
            amount = Long.parseLong(current.amount.trim());
        }
        catch (NumberFormatException exception) {
            error(R.string.invalid_amount);
            return;
        }


        if (amount <= 0 || amount > MAX_AMOUNT) {
            error(R.string.invalid_amount);
            return;
        }
        if (current.category.isEmpty() || current.account.isEmpty()) {
            error(R.string.required_fields);
            return;
        }


        busy = true;
        publishState();
        repository.saveTransaction(new TransactionEntity(id, amount, DateUtils.startOfDay(current.date), current.type, current.category, current.account, current.note.trim()), this::onWriteComplete);
    }


    public void deleteTransactionClick() {
        if (busy || id == 0) {
            return;
        }


        busy = true;
        publishState();
        repository.deleteTransaction(id, this::onWriteComplete);
    }


    private void onWriteComplete(boolean success) {
        busy = false;
        publishState();


        if (success) {
            event.setValue(new UiEvent<>(new AddTransactionEvent(AddTransactionEvent.Type.CLOSE, 0)));
        }
        else {
            error(R.string.database_error);
        }
    }


    private void error(int messageRes) {
        event.setValue(new UiEvent<>(new AddTransactionEvent(AddTransactionEvent.Type.ERROR, messageRes)));
    }
}
