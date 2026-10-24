package com.example.spendingmanagerment.ui.home_user.stats;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.core.utils.TransactionOptions;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.databinding.FragmentStatsBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.tabs.TabLayout;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@AndroidEntryPoint
public class StatsFragment extends Fragment {
    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;



    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);


        StatsState initial = viewModel.getState().getValue();
        if (initial != null) binding.tabLayout.selectTab(binding.tabLayout.getTabAt(initial.monthly ? 1 : 0));
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setNoDataText(getString(R.string.empty_statistics));
        binding.pieChart.getLegend().setWordWrapEnabled(true);
        binding.pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.black));
        binding.pieChart.setDrawHoleEnabled(false);
        android.content.res.TypedArray colors = requireContext().obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
        int textColor = colors.getColor(0, android.graphics.Color.BLACK);
        colors.recycle();
        binding.pieChart.getLegend().setTextColor(textColor);
        binding.pieChart.setNoDataTextColor(textColor);


        setOnClick();
        stateData();
        eventData();
    }


    private void setOnClick() {
        binding.previousDateBtn.setOnClickListener(view -> viewModel.previousDateClick());
        binding.nextDateBtn.setOnClickListener(view -> viewModel.nextDateClick());
        binding.currentDate.setOnClickListener(view -> viewModel.calendarClick());
        binding.incomeBtn.setOnClickListener(view -> viewModel.onTypeChange(TransactionEntity.INCOME));
        binding.expenseBtn.setOnClickListener(view -> viewModel.onTypeChange(TransactionEntity.EXPENSE));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { viewModel.onPeriodChange(tab.getPosition() == 1); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void stateData() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            Locale locale = getResources().getConfiguration().getLocales().get(0);
            binding.currentDate.setText(DateUtils.formatDate(state.date, state.monthly, locale));
            boolean income = TransactionEntity.INCOME.equals(state.type);
            binding.incomeBtn.setBackgroundResource(income ? R.drawable.income_selector : R.drawable.default_selector);
            binding.expenseBtn.setBackgroundResource(income ? R.drawable.default_selector : R.drawable.expense_selector);
            binding.incomeBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.expenseBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            binding.pieChart.setContentDescription(getString(R.string.stats_total, DateUtils.formatAmount(state.total, locale)));
            if (state.categories.isEmpty()) { binding.pieChart.clear(); return; }
            List<PieEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Long> category : state.categories.entrySet()) {
                entries.add(new PieEntry(category.getValue().floatValue(), TransactionOptions.categoryLabel(getResources(), category.getKey())));
            }
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(new int[] {R.color.category1, R.color.category2, R.color.category3,
                    R.color.category4, R.color.category5, R.color.category6}, requireContext());
            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override public String getPieLabel(float value, PieEntry entry) {
                    return String.format(locale, "%.1f%%", value / state.total * 100);
                }
            });
            binding.pieChart.setData(new PieData(dataSet));
            binding.pieChart.invalidate();
        });
    }


    private void eventData() {
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event.consume() == null) return;
            StatsState state = viewModel.getState().getValue();
            if (state == null) return;
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> viewModel.onDateChange(LocalDate.of(year, month + 1, day)),
                    state.date.getYear(), state.date.getMonthValue() - 1, state.date.getDayOfMonth()).show();
        });
    }
}
