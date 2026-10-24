package com.example.spendingmanagerment.ui.home_user.addtransaction;

public final class AddTransactionEvent {
    public enum Type { CLOSE, ERROR }
    public final Type type;
    public final int messageRes;
    public AddTransactionEvent(Type type, int messageRes) {
        this.type = type;
        this.messageRes = messageRes;
    }
}
