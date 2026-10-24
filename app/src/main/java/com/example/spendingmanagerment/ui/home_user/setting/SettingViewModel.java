package com.example.spendingmanagerment.ui.home_user.setting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.spendingmanagerment.core.base.UiEvent;
import com.example.spendingmanagerment.data.repository.SettingRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class SettingViewModel extends ViewModel {
    private final SettingRepository repository;
    private final MutableLiveData<SettingState> state = new MutableLiveData<>();
    private final MutableLiveData<UiEvent<SettingEvent>> event = new MutableLiveData<>();
    @Inject
    public SettingViewModel(SettingRepository repository) {
        this.repository = repository;
        state.setValue(new SettingState(repository.isDarkMode()));
    }


    public LiveData<SettingState> getState() {
        return state;
    }


    public LiveData<UiEvent<SettingEvent>> getEvent() {
        return event;
    }


    public void onDarkModeChange(boolean enabled) {
        if (enabled == repository.isDarkMode()) return;
        repository.setDarkMode(enabled);
        state.setValue(new SettingState(enabled));
        event.setValue(new UiEvent<>(SettingEvent.APPLY_THEME));
    }


    public void languageClick() { event.setValue(new UiEvent<>(SettingEvent.SHOW_LANGUAGE_PICKER)); }
}
