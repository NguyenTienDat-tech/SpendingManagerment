package com.example.spendingmanagerment.ui.home_user.transaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.databinding.FragmentTransactionBinding;
import com.example.spendingmanagerment.ui.home_user.transaction.adapter.TransactionAdapter;
import com.google.android.material.tabs.TabLayout;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.LocalDate;
import java.util.Locale;

@AndroidEntryPoint
public class TransactionFragment extends Fragment {
    private FragmentTransactionBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        adapter = new TransactionAdapter(viewModel::itemClick);
        binding.transactionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.transactionList.setAdapter(adapter);
        TransactionState initial = viewModel.getState().getValue();
        if (initial != null) binding.tabLayout.selectTab(binding.tabLayout.getTabAt(initial.monthly ? 1 : 0));
        setOnClick();
        stateData();
        eventData();
    }


    private void setOnClick() {
        binding.previousDateBtn.setOnClickListener(view -> viewModel.previousDateClick());
        binding.nextDateBtn.setOnClickListener(view -> viewModel.nextDateClick());
        binding.currentDate.setOnClickListener(view -> viewModel.calendarClick());
        binding.floatingActionButton.setOnClickListener(view -> viewModel.addTransactionClick());
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                viewModel.onPeriodChange(tab.getPosition() == 1);
                if (tab.getPosition() == 2) viewModel.calendarClick();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) viewModel.calendarClick();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.transactionList.setAdapter(null);
        binding = null;
        adapter = null;
    }


    private void stateData() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            binding.currentDate.setText(DateUtils.formatDate(state.date, state.monthly, locale));
            binding.incomeLbl.setText(DateUtils.formatAmount(state.income, locale));
            binding.expenseLbl.setText(DateUtils.formatAmount(state.expense, locale));
            binding.totalLbl.setText(DateUtils.formatAmount(state.income - state.expense, locale));
            adapter.submitList(state.transactions);
            binding.emptyState.setVisibility(state.transactions.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }


    private void eventData() {
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            TransactionEvent effect = event.consume();
            if (effect == null) return;
            TransactionState state = viewModel.getState().getValue();
            if (state == null) return;
            if (effect.type == TransactionEvent.Type.SHOW_DATE_PICKER) {
                new DatePickerDialog(requireContext(), (picker, year, month, day) ->
                        viewModel.onDateChange(LocalDate.of(year, month + 1, day)),
                        state.date.getYear(), state.date.getMonthValue() - 1, state.date.getDayOfMonth()).show();
            } else {
                Bundle arguments = new Bundle();
                arguments.putLong("transactionId", effect.transactionId);
                arguments.putLong("selectedDay", state.date.toEpochDay());
                NavHostFragment.findNavController(this).navigate(R.id.action_transaction_to_addTransaction, arguments);
            }
        });
    }
}
