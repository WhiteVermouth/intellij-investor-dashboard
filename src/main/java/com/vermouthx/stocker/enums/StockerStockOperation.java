package com.vermouthx.stocker.enums;

public enum StockerStockOperation {
    STOCK_ADD("Add"),
    STOCK_DELETE("Delete");

    private final String operation;

    StockerStockOperation(String operation) {
        this.operation = operation;
    }

    public static StockerStockOperation mapOf(String des) {
        if ("Add".equals(des)) {
            return STOCK_ADD;
        }
        return STOCK_DELETE;
    }

    public String getOperation() {
        return operation;
    }
}
