package com.example.spendingmanagerment.data.local.module;

import android.content.Context;
import androidx.room.Room;
import com.example.spendingmanagerment.data.local.dao.TransactionDao;
import com.example.spendingmanagerment.data.local.database.AppDatabase;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {
    private DatabaseModule() { }
    @Provides @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "spending_managerment_db").build();
    }
    @Provides @Singleton
    public static TransactionDao provideTransactionDao(AppDatabase database) {
        return database.transactionDao();
    }
    @Provides @Singleton
    public static ExecutorService provideDatabaseExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
