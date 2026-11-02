package com.example.spendingmanagerment.core.utils;

import android.content.res.Resources;
import com.example.spendingmanagerment.R;

public final class TransactionOptions {
    public static final String[] CATEGORIES = {"salary", "business", "investment", "loan", "rent", "other"};
    public static final String[] ACCOUNTS = {"cash", "bank", "mb_bank", "viettel_money", "other"};
    private TransactionOptions() { }

    public static String categoryLabel(Resources resources, String code) {
        return label(resources.getStringArray(R.array.categories), CATEGORIES, code);
    }
    public static String accountLabel(Resources resources, String code) {
        return label(resources.getStringArray(R.array.accounts), ACCOUNTS, code);
    }
    private static String label(String[] labels, String[] codes, String code) {
        for (int i = 0; i < codes.length; i++) if (codes[i].equals(code)) return labels[i];
        return code;
    }
}
