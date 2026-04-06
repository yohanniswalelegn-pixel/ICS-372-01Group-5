package com.brewbite.model.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a beverage on the Brew & Bite menu (Coffee or Tea).
 *
 * Demonstrates INHERITANCE — extends MenuItem, inheriting id, name,
 * basePrice and ingredient requirements while adding beverage-specific
 * fields: type, selectedSize, and available customizations.
 *
 * Responsibility: Model a single beverage offering including its size
 * options and the list of available add-on customizations.
 */
public class Beverage extends MenuItem {

    /** Sub-type: "Coffee" or "Tea" (used for catalog filtering). */
    private String beverageType;

    /** The size selected when this Beverage is added to an order. */
    private BeverageSize selectedSize;

    /**
     * All customizations that CAN be applied to this beverage.
     * Demonstrates COMPOSITION — Beverage "has" a list of Customization objects.
     */
    private List<Customization> availableCustomizations;

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public Beverage() {
        super();
        this.selectedSize            = BeverageSize.MEDIUM;
        this.availableCustomizations = new ArrayList<>();
    }

    /**
     * Full constructor.
     *
     * @param name          display name, e.g. "Latte"
     * @param beverageType  "Coffee" or "Tea"
     * @param basePrice     base price for a Medium
     */
    public Beverage(String name, String beverageType, double basePrice) {
        super(name, basePrice);
        this.beverageType            = beverageType;
        this.selectedSize            = BeverageSize.MEDIUM;
        this.availableCustomizations = new ArrayList<>();
    }

    // ── MenuItem abstract implementations ────────────────────────────────────

    /**
     * Returns "Beverage" as the category label.
     * Demonstrates POLYMORPHISM.
     */
    @Override
    public String getCategory() {
        return "Beverage";
    }

    /**
     * Calculates price = basePrice × sizeMultiplier.
     * (Customization costs are added in OrderItem, which holds the
     *  chosen customizations for a specific order line.)
     */
    @Override
    public double calculatePrice() {
        return getBasePrice() * selectedSize.getPriceMultiplier();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getBeverageType()                  { return beverageType; }
    public void   setBeverageType(String type)       { this.beverageType = type; }

    public BeverageSize getSelectedSize()            { return selectedSize; }
    public void         setSelectedSize(BeverageSize size) { this.selectedSize = size; }

    public List<Customization> getAvailableCustomizations() {
        return availableCustomizations;
    }
    public void setAvailableCustomizations(List<Customization> list) {
        this.availableCustomizations = list;
    }

    /** Convenience method to register one more customization option. */
    public void addCustomization(Customization c) {
        this.availableCustomizations.add(c);
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Beverage other)) return false;
        return super.equals(o) &&
               Objects.equals(beverageType, other.beverageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), beverageType);
    }

    @Override
    public String toString() {
        return "Beverage{id='" + getId() + "', name='" + getName() +
               "', type=" + beverageType + ", size=" + selectedSize +
               ", price=" + String.format("%.2f", calculatePrice()) + "}";
    }
}
