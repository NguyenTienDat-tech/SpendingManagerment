package com.example.spendingmanagerment.ui.home_user.transaction.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.core.utils.TransactionOptions;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.databinding.RowTransactionBinding;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<TransactionEntity, TransactionAdapter.TransactionViewHolder> {
    public interface OnTransactionClick { void onClick(long id); }
    private final OnTransactionClick onTransactionClick;

    public TransactionAdapter(OnTransactionClick onTransactionClick) {
        super(new TransactionDiffCallback());
        this.onTransactionClick = onTransactionClick;
    }


    @NonNull @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionViewHolder(RowTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity transaction = getItem(position);
        RowTransactionBinding binding = holder.binding;
        Locale locale = binding.getRoot().getResources().getConfiguration().getLocales().get(0);
        binding.transactionCategory.setText(TransactionOptions.categoryLabel(binding.getRoot().getResources(), transaction.category));
        binding.accountLbl.setText(TransactionOptions.accountLabel(binding.getRoot().getResources(), transaction.account));
        binding.transactionDate.setText(DateUtils.formatDate(DateUtils.fromMillis(transaction.date), false, locale));
        boolean income = TransactionEntity.INCOME.equals(transaction.type);
        binding.transactionAmount.setText(binding.getRoot().getContext().getString(R.string.signed_amount,
                income ? "+" : "−", DateUtils.formatAmount(transaction.amount, locale)));
        binding.transactionAmount.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), income ? R.color.greenColor : R.color.redColor));
        binding.categoryIcon.setImageResource(income ? R.drawable.ic_salary : R.drawable.ic_bag);
        binding.getRoot().setOnClickListener(view -> onTransactionClick.onClick(transaction.id));
    }


    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final RowTransactionBinding binding;
        TransactionViewHolder(RowTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
