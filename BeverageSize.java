package com.brewbite.model.menu;

/**
 * Available sizes for Beverage menu items.
 * Each size carries a price multiplier applied on top of the base price.
 */
public enum BeverageSize {

    SMALL  ("Small",  1.0),
    MEDIUM ("Medium", 1.25),
    LARGE  ("Large",  1.5);

    private final String displayName;
    private final double priceMultiplier;

    BeverageSize(String displayName, double priceMultiplier) {
        this.displayName      = displayName;
        this.priceMultiplier  = priceMultiplier;
    }

    public String getDisplayName()      { return displayName; }
    public double getPriceMultiplier()  { return priceMultiplier; }

    @Override
    public String toString() { return displayName; }
}
