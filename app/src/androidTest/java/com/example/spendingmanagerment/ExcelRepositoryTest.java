package com.example.spendingmanagerment;

import android.content.Context;
import android.net.Uri;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.spendingmanagerment.data.local.dao.TransactionDao;
import com.example.spendingmanagerment.data.local.database.AppDatabase;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.data.local.excel.TransactionExcel;
import com.example.spendingmanagerment.data.repository.ExcelRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExcelRepositoryTest {
    private AppDatabase database;
    private TransactionDao dao;
    private ExecutorService executor;
    private ExcelRepository repository;
    private File file;

    @Before public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        dao = database.transactionDao();
        executor = Executors.newSingleThreadExecutor();
        repository = new ExcelRepository(context, dao, new TransactionExcel(), executor);
        file = File.createTempFile("spending-excel-test-", ".xlsx", context.getCacheDir());
    }

    @After public void tearDown() throws Exception {
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        database.close();
        if (file.exists()) assertTrue(file.delete());
    }

    @Test public void exportsImportsUpsertsAndClearsThroughContentResolver() throws Exception {
        long id = dao.insertTransaction(new TransactionEntity(0, 250000, 1792717200000L,
                "EXPENSE", "rent", "cash", "Thú cưng & <test>"));
        assertEquals(1, await(callback -> repository.exportExcel(Uri.fromFile(file), callback), true));
        assertEquals(1, dao.deleteAllTransactions());
        assertEquals(1, await(callback -> repository.importExcel(Uri.fromFile(file), callback), true));
        assertEquals(id, dao.listAllTransactions().get(0).id);
        assertEquals("Thú cưng & <test>", dao.listAllTransactions().get(0).note);
        assertEquals(1, await(callback -> repository.importExcel(Uri.fromFile(file), callback), true));
        assertEquals(1, dao.listAllTransactions().size());
        assertEquals(1, await(repository::clearData, true));
        assertTrue(dao.listAllTransactions().isEmpty());
    }

    @Test public void invalidWorkbookLeavesExistingTransactionsIntact() throws Exception {
        dao.insertTransaction(new TransactionEntity(0, 1000, 0, "INCOME", "salary", "cash", "keep"));
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write("invalid workbook".getBytes(StandardCharsets.UTF_8));
        }
        await(callback -> repository.importExcel(Uri.fromFile(file), callback), false);
        assertEquals(1, dao.listAllTransactions().size());
        assertEquals("keep", dao.listAllTransactions().get(0).note);
    }

    private interface Operation { void run(ExcelRepository.Callback callback); }

    private int await(Operation operation, boolean expectedSuccess) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean();
        AtomicInteger count = new AtomicInteger();
        operation.run((result, rows) -> { success.set(result); count.set(rows); latch.countDown(); });
        assertTrue("Excel operation timed out", latch.await(10, TimeUnit.SECONDS));
        assertEquals(expectedSuccess, success.get());
        return count.get();
    }
}
