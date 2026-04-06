package com.brewbite.model.inventory;

import java.util.Objects;

/**
 * Data class that pairs an ingredient name with the quantity consumed
 * when one unit of a menu item is prepared.
 *
 * Responsibility: Represent a single ingredient-consumption rule for a menu item.
 * Used by InventoryManager to deduct stock when an order is placed.
 */
public class IngredientRequirement {

    private String ingredientName; // must match the name key in InventoryManager
    private double amountRequired; // quantity consumed per item ordered

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public IngredientRequirement() {}

    /**
     * Full constructor.
     *
     * @param ingredientName the name of the required ingredient
     * @param amountRequired the quantity consumed per item
     */
    public IngredientRequirement(String ingredientName, double amountRequired) {
        this.ingredientName = ingredientName;
        this.amountRequired = amountRequired;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getIngredientName()                { return ingredientName; }
    public void   setIngredientName(String name)     { this.ingredientName = name; }

    public double getAmountRequired()                { return amountRequired; }
    public void   setAmountRequired(double amount)   { this.amountRequired = amount; }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IngredientRequirement other)) return false;
        return Objects.equals(ingredientName, other.ingredientName) &&
               Double.compare(amountRequired, other.amountRequired) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientName, amountRequired);
    }

    @Override
    public String toString() {
        return "IngredientRequirement{" + ingredientName + " x" + amountRequired + "}";
    }
}
