package com.example.spendingmanagerment.ui.home_user.transaction;

import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransactionState {
    public final LocalDate date;
    public final boolean monthly;
    public final List<TransactionEntity> transactions;
    public final long income;
    public final long expense;

    public TransactionState(LocalDate date, boolean monthly, List<TransactionEntity> transactions) {
        this.date = date;
        this.monthly = monthly;
        this.transactions = Collections.unmodifiableList(new ArrayList<>(transactions));
        long income = 0;
        long expense = 0;
        for (TransactionEntity transaction : transactions) {
            if (TransactionEntity.INCOME.equals(transaction.type)) income += transaction.amount;
            else expense += transaction.amount;
        }
        this.income = income;
        this.expense = expense;
    }
}
