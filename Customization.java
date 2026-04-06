package com.brewbite.model.menu;

import java.util.Objects;

/**
 * Data class representing an optional customization for a Beverage
 * (e.g. "Extra Shot", "Oat Milk", "Sugar-Free Syrup").
 *
 * Responsibility: Store the name and extra cost of one add-on option.
 * Used by Beverage to list available customizations and by OrderItem
 * to record which ones were chosen.
 */
public class Customization {

    private String name;       // e.g. "Extra Shot"
    private double extraCost;  // additional charge in dollars

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public Customization() {}

    /**
     * Full constructor.
     *
     * @param name      display name of the customization
     * @param extraCost additional cost; must be >= 0
     */
    public Customization(String name, double extraCost) {
        if (extraCost < 0) throw new IllegalArgumentException("Extra cost cannot be negative.");
        this.name      = name;
        this.extraCost = extraCost;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getName()             { return name; }
    public void   setName(String name)  { this.name = name; }

    public double getExtraCost()             { return extraCost; }
    public void   setExtraCost(double cost)  { this.extraCost = cost; }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customization other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Customization{name='" + name + "', extraCost=" + extraCost + "}";
    }
}
