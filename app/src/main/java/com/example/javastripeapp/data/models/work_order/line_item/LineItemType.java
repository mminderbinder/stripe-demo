package com.example.javastripeapp.data.models.work_order.line_item;

public enum LineItemType {
    SERVICE_FEE("100"),
    GRATUITY("200"),
    DRIVEWAY("300"),
    WALKWAY("310"),
    SIDEWALK("320");

    // Tax code constants
    private static final String TAX_SERVICE = "txcd_20030000";
    private static final String TAX_SNOW_REMOVAL = "txcd_20070007";
    private static final String TAX_GRATUITY = "txcd_90020001";

    // Price constants
    private static final Double PRICE_SERVICE_FEE = 4.33;
    private static final Double PRICE_DRIVEWAY = 20.00;
    private static final Double PRICE_WALKWAY_SIDEWALK = 5.00;
    private static final Double PRICE_GRATUITY = 0.00;

    // Display name constants
    private static final String DISPLAY_SERVICE_FEE = "Service Fee";
    private static final String DISPLAY_DRIVEWAY = "Driveway";
    private static final String DISPLAY_SIDEWALK = "Sidewalk";
    private static final String DISPLAY_WALKWAY = "Walkway";
    private static final String DISPLAY_GRATUITY = "Gratuity";
    private final String itemCode;

    LineItemType(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public Integer getIntCode() {
        return switch (this) {
            case SERVICE_FEE -> 100;
            case DRIVEWAY -> 300;
            case WALKWAY -> 310;
            case SIDEWALK -> 320;
            case GRATUITY -> 200;
        };
    }

    public String getTaxCode() {
        return switch (this) {
            case SERVICE_FEE -> TAX_SERVICE;
            case DRIVEWAY, WALKWAY, SIDEWALK -> TAX_SNOW_REMOVAL;
            case GRATUITY -> TAX_GRATUITY;
        };
    }

    public Double getPrice() {
        return switch (this) {
            case SERVICE_FEE -> PRICE_SERVICE_FEE;
            case DRIVEWAY -> PRICE_DRIVEWAY;
            case WALKWAY, SIDEWALK -> PRICE_WALKWAY_SIDEWALK;
            case GRATUITY -> PRICE_GRATUITY;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case SERVICE_FEE -> DISPLAY_SERVICE_FEE;
            case DRIVEWAY -> DISPLAY_DRIVEWAY;
            case SIDEWALK -> DISPLAY_SIDEWALK;
            case WALKWAY -> DISPLAY_WALKWAY;
            case GRATUITY -> DISPLAY_GRATUITY;
        };
    }

    public LineItem createLineItem() {
        return new LineItem(getIntCode(), getTaxCode(), getDisplayName(), getPrice());
    }
}
