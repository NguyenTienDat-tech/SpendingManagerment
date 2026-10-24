package com.example.spendingmanagerment.ui.home_user.stats;

import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatsState {
    public final LocalDate date;
    public final boolean monthly;
    public final String type;
    public final Map<String, Long> categories;
    public final long total;

    public StatsState(LocalDate date, boolean monthly, String type, List<TransactionEntity> transactions) {
        this.date = date;
        this.monthly = monthly;
        this.type = type;
        Map<String, Long> categories = new LinkedHashMap<>();
        long total = 0;
        for (TransactionEntity transaction : transactions) {
            if (!type.equals(transaction.type)) continue;
            Long previous = categories.get(transaction.category);
            categories.put(transaction.category, (previous == null ? 0 : previous) + transaction.amount);
            total += transaction.amount;
        }
        this.categories = Collections.unmodifiableMap(categories);
        this.total = total;
    }
}
