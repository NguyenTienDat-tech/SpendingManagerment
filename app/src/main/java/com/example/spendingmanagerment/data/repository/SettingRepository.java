package com.example.spendingmanagerment.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingRepository {
    private final SharedPreferences preferences;
    @Inject
    public SettingRepository(@ApplicationContext Context context) {
        preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }
    public boolean isDarkMode() { return preferences.getBoolean("dark_mode", false); }
    public void setDarkMode(boolean enabled) { preferences.edit().putBoolean("dark_mode", enabled).apply(); }
}
