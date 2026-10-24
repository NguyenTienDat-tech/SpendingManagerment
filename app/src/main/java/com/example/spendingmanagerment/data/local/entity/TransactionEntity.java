package com.example.spendingmanagerment.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions", indices = {@Index("date")})
public class TransactionEntity {
    public static final String INCOME = "INCOME";
    public static final String EXPENSE = "EXPENSE";
    @PrimaryKey(autoGenerate = true)
    public long id;
    // Whole VND avoids floating point rounding in totals.
    public long amount;
    public long date;
    @NonNull public String type;
    @NonNull public String category;
    @NonNull public String account;
    @NonNull public String note;

    public TransactionEntity(long id, long amount, long date, @NonNull String type,
                             @NonNull String category, @NonNull String account, @NonNull String note) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.account = account;
        this.note = note;
    }
}
