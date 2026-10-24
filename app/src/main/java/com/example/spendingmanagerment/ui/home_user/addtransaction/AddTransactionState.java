package com.example.spendingmanagerment.ui.home_user.addtransaction;

import java.time.LocalDate;

public final class AddTransactionState {
    public final long id;
    public final LocalDate date;
    public final String amount;
    public final String type;
    public final String category;
    public final String account;
    public final String note;
    public final boolean busy;

    public AddTransactionState(long id, LocalDate date, String amount, String type, String category, String account, String note, boolean busy) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.account = account;
        this.note = note;
        this.busy = busy;
    }
}
