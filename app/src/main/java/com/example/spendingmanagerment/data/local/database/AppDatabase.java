package com.example.spendingmanagerment.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.spendingmanagerment.data.local.dao.TransactionDao;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;

@Database(entities = {TransactionEntity.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
}
