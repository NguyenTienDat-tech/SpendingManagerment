package com.example.spendingmanagerment.ui.home_user.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.databinding.FragmentSettingBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Locale;

@AndroidEntryPoint
public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private SettingViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingViewModel.class);


        Locale locale = getResources().getConfiguration().getLocales().get(0);
        binding.tvCurrentLanguage.setText("vi".equals(locale.getLanguage()) ? "Tiếng Việt" : "English");


        setOnClick();
        stateData();
        eventData();
    }


    private void setOnClick() {
        binding.switchDarkMode.setOnCheckedChangeListener((button, checked) -> viewModel.onDarkModeChange(checked));
        binding.layoutDarkMode.setOnClickListener(view -> viewModel.onDarkModeChange(!binding.switchDarkMode.isChecked()));
        binding.layoutLanguage.setOnClickListener(view -> viewModel.languageClick());
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void stateData() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> binding.switchDarkMode.setChecked(state.darkMode));
    }


    private void eventData() {
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            SettingEvent effect = event.consume();
            if (effect == null) return;

            if (effect == SettingEvent.APPLY_THEME) {
                SettingState state = viewModel.getState().getValue();
                if (state != null) AppCompatDelegate.setDefaultNightMode(state.darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }

            else if (effect == SettingEvent.SHOW_LANGUAGE_PICKER) {
                String[] languages = {"Tiếng Việt", "English"};
                int selected = "vi".equals(getResources().getConfiguration().getLocales().get(0).getLanguage()) ? 0 : 1;
                new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.language)
                        .setSingleChoiceItems(languages, selected, (dialog, which) -> {
                            dialog.dismiss();
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(which == 0 ? "vi" : "en"));
                        }).setNegativeButton(R.string.cancel, null).show();
            }
        });
    }
}
