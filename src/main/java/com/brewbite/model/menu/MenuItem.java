package com.brewbite.model.menu;

import com.brewbite.model.inventory.IngredientRequirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for all items on the Brew & Bite menu.
 *
 * Demonstrates INHERITANCE — Beverage and Pastry extend this class,
 * inheriting common fields (id, name, basePrice, ingredients) while
 * adding type-specific behavior.
 *
 * Responsibility: Define the shared contract and data common to every
 * menu item. Subclasses fill in category-specific details.
 */
public abstract class MenuItem {

    private String id;        // unique identifier (UUID)
    private String name;      // display name, e.g. "Latte"
    private double basePrice; // price before size/customization adjustments
    private boolean available; // false if manually removed by manager

    /**
     * Ingredient requirements consumed when this item is prepared.
     * Demonstrates COMPOSITION — MenuItem "has a" list of requirements.
     */
    private List<IngredientRequirement> ingredientRequirements;

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public MenuItem() {
        this.id = UUID.randomUUID().toString();
        this.ingredientRequirements = new ArrayList<>();
        this.available = true;
    }

    /**
     * Base constructor used by subclass constructors.
     *
     * @param name      display name
     * @param basePrice base price in dollars
     */
    public MenuItem(String name, double basePrice) {
        this();
        this.name      = name;
        this.basePrice = basePrice;
    }

    // ── Abstract methods ─────────────────────────────────────────────────────

    /**
     * Returns the category label shown in the UI (e.g. "Beverage", "Pastry").
     * Demonstrates POLYMORPHISM — each subclass returns its own label.
     *
     * @return category string
     */
    public abstract String getCategory();

    /**
     * Calculates the final price for a given configuration of this item.
     * Subclasses apply size multipliers, extra-shot charges, etc.
     *
     * @return calculated price in dollars
     */
    public abstract double calculatePrice();

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getId()                          { return id; }
    public void   setId(String id)                 { this.id = id; }

    public String getName()                        { return name; }
    public void   setName(String name)             { this.name = name; }

    public double getBasePrice()                   { return basePrice; }
    public void   setBasePrice(double price)       { this.basePrice = price; }

    public boolean isAvailable()                   { return available; }
    public void    setAvailable(boolean available) { this.available = available; }

    public List<IngredientRequirement> getIngredientRequirements() {
        return ingredientRequirements;
    }
    public void setIngredientRequirements(List<IngredientRequirement> reqs) {
        this.ingredientRequirements = reqs;
    }

    /** Convenience method to add a single requirement. */
    public void addIngredientRequirement(IngredientRequirement req) {
        this.ingredientRequirements.add(req);
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getCategory() + "{id='" + id + "', name='" + name +
               "', basePrice=" + basePrice + "}";
    }
}
