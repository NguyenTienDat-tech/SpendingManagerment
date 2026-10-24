package com.example.spendingmanagerment.ui.home_user.addtransaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.core.utils.TransactionOptions;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.databinding.FragmentAddTransactionBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.LocalDate;
import java.util.function.Consumer;

@AndroidEntryPoint
public class AddTransactionFragment extends BottomSheetDialogFragment {
    private FragmentAddTransactionBinding binding;
    private AddTransactionViewModel viewModel;
    private boolean rendering;

    @Override public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);


            if (sheet != null) {
                ViewGroup.LayoutParams params = sheet.getLayoutParams();
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                sheet.setLayoutParams(params);
            }


            BottomSheetBehavior<?> behavior = dialog.getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddTransactionViewModel.class);
        setOnClick();
        stateData();
        eventData();
    }

    private void setOnClick() {
        binding.incomeBtn.setOnClickListener(view -> viewModel.onTypeChange(TransactionEntity.INCOME));
        binding.expenseBtn.setOnClickListener(view -> viewModel.onTypeChange(TransactionEntity.EXPENSE));
        binding.date.setOnClickListener(view -> {
            AddTransactionState state = viewModel.getState().getValue();
            if (state == null) return;
            new DatePickerDialog(requireContext(), (picker, year, month, day) ->
                    viewModel.onDateChange(LocalDate.of(year, month + 1, day)), state.date.getYear(),
                    state.date.getMonthValue() - 1, state.date.getDayOfMonth()).show();
        });
        binding.category.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.app_category).setItems(R.array.categories,
                        (dialog, which) -> viewModel.onCategoryChange(TransactionOptions.CATEGORIES[which])).show());
        binding.account.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.app_account).setItems(R.array.accounts,
                        (dialog, which) -> viewModel.onAccountChange(TransactionOptions.ACCOUNTS[which])).show());
        watch(binding.amount, viewModel::onAmountChange);
        watch(binding.note, viewModel::onNoteChange);
        binding.saveTransactionBtn.setOnClickListener(view -> viewModel.saveTransactionClick());
        binding.deleteTransactionBtn.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.confirm_delete).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.app_delete, (dialog, which) -> viewModel.deleteTransactionClick()).show());
        binding.cancelBtn.setOnClickListener(view -> NavHostFragment.findNavController(this).popBackStack());
    }
    private void watch(EditText field, Consumer<String> onChange) {
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (!rendering) onChange.accept(text.toString());
            }
            @Override public void afterTextChanged(Editable text) { }
        });
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void setText(EditText field, String value) {
        if (!field.getText().toString().equals(value)) field.setText(value);
    }


    private void stateData() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            rendering = true;
            binding.title.setText(state.id == 0 ? R.string.add_transaction : R.string.edit_transaction);
            binding.date.setText(DateUtils.formatDate(state.date, false, getResources().getConfiguration().getLocales().get(0)));
            setText(binding.amount, state.amount);
            setText(binding.note, state.note);
            binding.category.setText(TransactionOptions.categoryLabel(getResources(), state.category));
            binding.account.setText(TransactionOptions.accountLabel(getResources(), state.account));
            boolean income = TransactionEntity.INCOME.equals(state.type);
            binding.incomeBtn.setAlpha(income ? 1f : 0.45f);
            binding.expenseBtn.setAlpha(income ? 0.45f : 1f);
            binding.incomeBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.greenColor));
            binding.expenseBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.redColor));
            binding.saveTransactionBtn.setEnabled(!state.busy);
            binding.deleteTransactionBtn.setEnabled(!state.busy);
            binding.deleteTransactionBtn.setVisibility(state.id == 0 ? View.GONE : View.VISIBLE);
            binding.amount.setEnabled(!state.busy);
            binding.note.setEnabled(!state.busy);
            binding.date.setEnabled(!state.busy);
            binding.category.setEnabled(!state.busy);
            binding.account.setEnabled(!state.busy);
            binding.incomeBtn.setEnabled(!state.busy);
            binding.expenseBtn.setEnabled(!state.busy);
            binding.cancelBtn.setEnabled(!state.busy);
            setCancelable(!state.busy);
            rendering = false;
        });
    }


    private void eventData() {
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            AddTransactionEvent effect = event.consume();
            if (effect == null) return;
            if (effect.type == AddTransactionEvent.Type.CLOSE) NavHostFragment.findNavController(this).popBackStack();
            else Toast.makeText(requireContext(), effect.messageRes, Toast.LENGTH_SHORT).show();
        });
    }
}
