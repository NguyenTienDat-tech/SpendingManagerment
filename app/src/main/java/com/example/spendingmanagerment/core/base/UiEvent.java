package com.example.spendingmanagerment.core.base;

import androidx.annotation.Nullable;

/** Delivers an effect once, including when LiveData reconnects after rotation. */
public final class UiEvent<T> {
    private final T value;
    private boolean handled;

    public UiEvent(T value) { this.value = value; }

    @Nullable
    public T consume() {
        if (handled) return null;
        handled = true;
        return value;
    }
}
