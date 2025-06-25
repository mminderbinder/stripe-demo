package com.example.javastripeapp.data.models.line_item;

public class LineItem {
    private final Integer intItemCode;
    private final String taxCode;
    private final String description;
    private final Double amount;

    LineItem() {
        this(null, null, null, null);
    }

    LineItem(Integer intItemCode, String taxCode, String description, Double amount) {
        this.intItemCode = intItemCode;
        this.taxCode = taxCode;
        this.description = description;
        this.amount = amount;
    }

    public Integer getIntItemCode() {
        return intItemCode;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }
}
