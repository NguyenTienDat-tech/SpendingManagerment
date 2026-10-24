package com.example.spendingmanagerment.data.repository;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.example.spendingmanagerment.data.local.dao.TransactionDao;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.data.local.excel.TransactionExcel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExcelRepository {
    public static final String MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public interface Callback { void onComplete(boolean success, int count); }

    private final Context context;
    private final TransactionDao transactionDao;
    private final TransactionExcel excel;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public ExcelRepository(@ApplicationContext Context context, TransactionDao transactionDao,
                           TransactionExcel excel, ExecutorService executor) {
        this.context = context;
        this.transactionDao = transactionDao;
        this.excel = excel;
        this.executor = executor;
    }

    public void exportExcel(Uri uri, Callback callback) {
        executor.execute(() -> {
            boolean success = false;
            int count = 0;
            try (OutputStream output = context.getContentResolver().openOutputStream(uri, "wt")) {
                if (output == null) throw new IOException("Unable to open destination");
                List<TransactionEntity> transactions = transactionDao.listAllTransactions();
                excel.write(output, transactions);
                count = transactions.size();
                success = true;
            } catch (IOException | RuntimeException exception) { success = false; }
            complete(callback, success, success ? count : 0);
        });
    }

    public void importExcel(Uri uri, Callback callback) {
        executor.execute(() -> {
            boolean success = false;
            int count = 0;
            try {
                List<TransactionEntity> transactions;
                try (InputStream input = context.getContentResolver().openInputStream(uri)) {
                    if (input == null) throw new IOException("Unable to open source");
                    transactions = excel.read(input);
                }
                // Read and validate the entire file before Room performs one atomic batch upsert.
                transactionDao.upsertTransactions(transactions);
                count = transactions.size();
                success = true;
            } catch (IOException | RuntimeException exception) { success = false; }
            complete(callback, success, success ? count : 0);
        });
    }

    public void clearData(Callback callback) {
        executor.execute(() -> {
            try { complete(callback, true, transactionDao.deleteAllTransactions()); }
            catch (RuntimeException exception) { complete(callback, false, 0); }
        });
    }

    private void complete(Callback callback, boolean success, int count) {
        mainHandler.post(() -> callback.onComplete(success, count));
    }
}
