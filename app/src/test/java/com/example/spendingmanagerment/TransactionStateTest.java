package com.example.spendingmanagerment;

import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.ui.home_user.transaction.TransactionState;
import com.example.spendingmanagerment.ui.home_user.stats.StatsState;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class TransactionStateTest {
    private TransactionEntity transaction(long id, long amount, String type, String category) {
        return new TransactionEntity(id, amount, 0, type, category, "cash", "");
    }

    @Test public void totalsKeepWholeVndAndSeparateIncomeFromExpense() {
        TransactionState state = new TransactionState(LocalDate.of(2026, 10, 23), false, Arrays.asList(
                transaction(1, 5_000_000_001L, TransactionEntity.INCOME, "salary"),
                transaction(2, 50_001L, TransactionEntity.EXPENSE, "rent"),
                transaction(3, 100_000L, TransactionEntity.EXPENSE, "other")));
        assertEquals(5_000_000_001L, state.income);
        assertEquals(150_001L, state.expense);
        assertEquals(4_999_850_000L, state.income - state.expense);
    }

    @Test public void statisticsGroupCategoriesAndExcludeOtherTransactionType() {
        StatsState state = new StatsState(LocalDate.now(), true, TransactionEntity.EXPENSE, Arrays.asList(
                transaction(1, 100, TransactionEntity.EXPENSE, "rent"),
                transaction(2, 200, TransactionEntity.EXPENSE, "rent"),
                transaction(3, 50, TransactionEntity.EXPENSE, "other"),
                transaction(4, 1_000, TransactionEntity.INCOME, "salary")));
        assertEquals(350, state.total);
        assertEquals(Long.valueOf(300), state.categories.get("rent"));
        assertEquals(2, state.categories.size());
        assertFalse(state.categories.containsKey("salary"));
    }

    @Test public void emptyPeriodHasZeroTotalsAndNoChartCategories() {
        TransactionState transactions = new TransactionState(LocalDate.now(), false, Collections.emptyList());
        StatsState stats = new StatsState(LocalDate.now(), false, TransactionEntity.EXPENSE, Collections.emptyList());
        assertEquals(0, transactions.income);
        assertEquals(0, transactions.expense);
        assertEquals(0, stats.total);
        assertTrue(stats.categories.isEmpty());
    }
}
