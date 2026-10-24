package com.example.spendingmanagerment.ui.home_user.transaction;

public class TransactionEvent {
    public enum Type {
        OPEN_EDITOR,
        SHOW_DATE_PICKER
    }
    public final Type type;
    public final long transactionId;


    public TransactionEvent(Type type, long transactionId) {
        this.type = type;
        this.transactionId = transactionId;
    }
}
