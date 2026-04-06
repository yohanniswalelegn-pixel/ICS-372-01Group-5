package com.brewbite.model.inventory;

import java.util.Objects;

/**
 * Data class representing a single ingredient tracked in the Brew & Bite inventory.
 * Each ingredient has a name, a current stock quantity, and a unit of measure.
 *
 * Responsibility: Store and expose the state of one inventory ingredient.
 */
public class Ingredient {

    private String name;       // e.g. "Coffee Beans"
    private double quantity;   // current stock level
    private String unit;       // e.g. "g", "ml", "units"

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public Ingredient() {}

    /**
     * Full constructor.
     *
     * @param name     display name of the ingredient
     * @param quantity initial stock quantity
     * @param unit     unit of measure (e.g. "g", "ml")
     */
    public Ingredient(String name, double quantity, String unit) {
        this.name     = name;
        this.quantity = quantity;
        this.unit     = unit;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getName()             { return name; }
    public void   setName(String n)     { this.name = n; }

    public double getQuantity()         { return quantity; }
    public void   setQuantity(double q) { this.quantity = q; }

    public String getUnit()             { return unit; }
    public void   setUnit(String u)     { this.unit = u; }

    // ── Business helpers ─────────────────────────────────────────────────────

    /**
     * Checks whether the requested amount can be fulfilled from current stock.
     *
     * @param amount the amount needed
     * @return true if stock >= amount
     */
    public boolean hasSufficient(double amount) {
        return quantity >= amount;
    }

    /**
     * Deducts the consumed amount from stock.
     * Caller must verify hasSufficient() before calling this method.
     *
     * @param amount amount to deduct
     * @throws IllegalStateException if stock would go negative
     */
    public void consume(double amount) {
        if (!hasSufficient(amount)) {
            throw new IllegalStateException(
                "Insufficient stock for " + name + ": need " + amount + " " + unit +
                " but only " + quantity + " available.");
        }
        quantity -= amount;
    }

    /**
     * Adds stock to the current quantity (restock operation).
     *
     * @param amount the amount to add; must be positive
     */
    public void restock(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restock amount must be positive.");
        }
        quantity += amount;
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Ingredient{name='" + name + "', quantity=" + quantity + " " + unit + "}";
    }
}
