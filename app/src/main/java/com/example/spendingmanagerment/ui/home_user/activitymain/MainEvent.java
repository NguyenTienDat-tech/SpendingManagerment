package com.example.spendingmanagerment.ui.home_user.activitymain;

import android.net.Uri;

public final class MainEvent {
    public enum Type {
        EXPORT_PICKER,
        IMPORT_PICKER,
        CONFIRM_IMPORT,
        CONFIRM_CLEAR,
        MESSAGE
    }
    public final Type type;
    public final Uri uri;
    public final int messageRes;
    public final int count;

    public MainEvent(Type type, Uri uri, int messageRes, int count) {
        this.type = type;
        this.uri = uri;
        this.messageRes = messageRes;
        this.count = count;
    }
}
