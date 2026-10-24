package com.example.spendingmanagerment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.data.local.dao.TransactionDao;
import com.example.spendingmanagerment.data.local.database.AppDatabase;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TransactionDaoTest {
    private AppDatabase database;
    private TransactionDao dao;

    @Before public void setUp() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().getTargetContext(), AppDatabase.class).build();
        dao = database.transactionDao();
    }
    @After public void tearDown() { database.close(); }

    @Test public void dateRangeIsStartInclusiveEndExclusiveAndOrdersNewestFirst() throws Exception {
        long start = DateUtils.startOfDay(LocalDate.of(2026, 10, 23));
        long end = DateUtils.startOfDay(LocalDate.of(2026, 10, 24));
        insert(10, start - 1);
        long first = insert(20, start);
        long last = insert(30, end - 1);
        insert(40, end);
        List<TransactionEntity> rows = awaitValue(dao.listTransactions(start, end));
        assertEquals(2, rows.size());
        assertEquals(last, rows.get(0).id);
        assertEquals(first, rows.get(1).id);
    }

    @Test public void updateAndDeletePersistAndMissingRowsAreReported() throws Exception {
        long id = insert(100, 1_000);
        TransactionEntity entity = awaitValue(dao.getTransaction(id));
        entity.amount = 250;
        entity.note = "updated";
        assertEquals(1, dao.updateTransaction(entity));
        TransactionEntity updated = awaitValue(dao.getTransaction(id));
        assertEquals(250, updated.amount);
        assertEquals("updated", updated.note);
        assertEquals(1, dao.deleteTransaction(id));
        assertNull(awaitValue(dao.getTransaction(id)));
        assertEquals(0, dao.deleteTransaction(id));
        assertEquals(0, dao.updateTransaction(entity));
    }

    private long insert(long amount, long date) {
        return dao.insertTransaction(new TransactionEntity(0, amount, date, TransactionEntity.EXPENSE, "rent", "cash", ""));
    }

    private <T> T awaitValue(LiveData<T> liveData) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        Observer<T> observer = value -> { result.set(value); latch.countDown(); };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> liveData.observeForever(observer));
        try {
            assertTrue("Room query timed out", latch.await(5, TimeUnit.SECONDS));
            return result.get();
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> liveData.removeObserver(observer));
        }
    }
}
