package com.brewbite.model.menu;

import java.util.Objects;

/**
 * Represents a pastry on the Brew & Bite menu
 * (Croissant, Muffin, or Cookie).
 *
 * Demonstrates INHERITANCE — extends MenuItem.
 * Pastries have no size options or complex customizations;
 * they simply add a "variation" field (e.g. "Blueberry", "Chocolate Chip").
 *
 * Responsibility: Model a single pastry offering with a fixed price
 * and an optional variation label.
 */
public class Pastry extends MenuItem {

    /** The pastry sub-type, e.g. "Croissant", "Muffin", "Cookie". */
    private String pastryType;

    /**
     * Variation within the sub-type, e.g. "Butter", "Blueberry",
     * "Chocolate Chip". May be empty if there is only one variant.
     */
    private String variation;

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public Pastry() {
        super();
    }

    /**
     * Full constructor.
     *
     * @param name       display name, e.g. "Blueberry Muffin"
     * @param pastryType "Croissant", "Muffin", or "Cookie"
     * @param variation  specific variant, e.g. "Blueberry"
     * @param basePrice  fixed price (no size multiplier)
     */
    public Pastry(String name, String pastryType, String variation, double basePrice) {
        super(name, basePrice);
        this.pastryType = pastryType;
        this.variation  = variation;
    }

    // ── MenuItem abstract implementations ────────────────────────────────────

    /**
     * Returns "Pastry" as the category label.
     * Demonstrates POLYMORPHISM.
     */
    @Override
    public String getCategory() {
        return "Pastry";
    }

    /**
     * Pastries have a fixed price — no size multiplier.
     *
     * @return basePrice unchanged
     */
    @Override
    public double calculatePrice() {
        return getBasePrice();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getPastryType()              { return pastryType; }
    public void   setPastryType(String type)   { this.pastryType = type; }

    public String getVariation()               { return variation; }
    public void   setVariation(String v)       { this.variation = v; }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pastry other)) return false;
        return super.equals(o) &&
               Objects.equals(pastryType, other.pastryType) &&
               Objects.equals(variation,  other.variation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pastryType, variation);
    }

    @Override
    public String toString() {
        return "Pastry{id='" + getId() + "', name='" + getName() +
               "', type=" + pastryType + ", variation=" + variation +
               ", price=" + String.format("%.2f", calculatePrice()) + "}";
    }
}
