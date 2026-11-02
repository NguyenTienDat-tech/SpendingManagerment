package com.example.spendingmanagerment.data.repository;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.spendingmanagerment.data.local.dao.TransactionDao;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BooleanSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionRepository {
    public interface Callback { void onComplete(boolean success); }
    private final TransactionDao transactionDao;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public TransactionRepository(TransactionDao transactionDao, ExecutorService executor) {
        this.transactionDao = transactionDao;
        this.executor = executor;
    }

    public LiveData<List<TransactionEntity>> listTransactions(long start, long end) {
        return transactionDao.listTransactions(start, end);
    }

    public LiveData<TransactionEntity> getTransaction(long id) {
        return transactionDao.getTransaction(id);
    }

    public void saveTransaction(TransactionEntity transaction, Callback callback) {
        executeWrite(() -> transaction.id == 0
                ? transactionDao.insertTransaction(transaction) > 0
                : transactionDao.updateTransaction(transaction) == 1, callback);
    }

    public void deleteTransaction(long id, Callback callback) {
        executeWrite(() -> transactionDao.deleteTransaction(id) == 1, callback);
    }

    private void executeWrite(BooleanSupplier operation, Callback callback) {
        executor.execute(() -> {
            boolean success;
            try {
                success = operation.getAsBoolean();
            } catch (RuntimeException exception) {
                success = false;
            }
            boolean result = success;
            mainHandler.post(() -> callback.onComplete(result));
        });
    }
}
