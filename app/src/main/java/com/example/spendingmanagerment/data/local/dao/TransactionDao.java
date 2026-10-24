package com.example.spendingmanagerment.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import java.util.List;

@Dao
public interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE date >= :start AND date < :end ORDER BY date DESC, id DESC")
    LiveData<List<TransactionEntity>> listTransactions(long start, long end);
    @Query("SELECT * FROM transactions WHERE id = :id")
    LiveData<TransactionEntity> getTransaction(long id);
    @Insert
    long insertTransaction(TransactionEntity transaction);
    @Update
    int updateTransaction(TransactionEntity transaction);
    @Query("DELETE FROM transactions WHERE id = :id")
    int deleteTransaction(long id);

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    List<TransactionEntity> listAllTransactions();

    @Upsert
    void upsertTransactions(List<TransactionEntity> transactions);

    @Query("DELETE FROM transactions")
    int deleteAllTransactions();
}
