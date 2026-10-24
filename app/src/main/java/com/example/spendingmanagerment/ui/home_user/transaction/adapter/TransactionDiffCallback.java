package com.example.spendingmanagerment.ui.home_user.transaction.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;

public class TransactionDiffCallback extends DiffUtil.ItemCallback<TransactionEntity> {
    @Override
    public boolean areItemsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
        return oldItem.id == newItem.id;
    }
    @Override
    public boolean areContentsTheSame(@NonNull TransactionEntity a, @NonNull TransactionEntity b) {
        return a.amount == b.amount && a.date == b.date && a.type.equals(b.type) && a.category.equals(b.category) && a.account.equals(b.account) && a.note.equals(b.note);
    }
}
