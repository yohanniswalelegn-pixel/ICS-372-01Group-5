package com.brewbite.model.order;

import com.brewbite.model.menu.Customization;
import com.brewbite.model.menu.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data class representing a single line item within an Order.
 *
 * An OrderItem captures a snapshot of what was ordered: the menu item,
 * the quantity, and any customizations chosen at order time.
 * This is intentionally separate from MenuItem so that changes to the
 * catalog after an order is placed do not alter historical records.
 *
 * Demonstrates COMPOSITION — Order "has" a list of OrderItems,
 * and OrderItem "has" a list of chosen Customizations.
 *
 * Responsibility: Store the details and computed line-total for one
 * entry in an order.
 */
public class OrderItem {

    private String     menuItemId;     // foreign key to MenuItem
    private String     menuItemName;   // snapshot of name at order time
    private String     category;       // "Beverage" or "Pastry"
    private int        quantity;
    private double     unitPrice;      // price of one unit at order time (includes size)
    private String     sizeLabel;      // e.g. "Large" — empty for pastries

    /** Customizations chosen for this specific line item. */
    private List<Customization> chosenCustomizations;

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public OrderItem() {
        this.chosenCustomizations = new ArrayList<>();
    }

    /**
     * Convenience constructor — builds an OrderItem from a MenuItem snapshot.
     *
     * @param item      the MenuItem being ordered
     * @param quantity  number of units
     * @param sizeLabel size display label (empty string for pastries)
     */
    public OrderItem(MenuItem item, int quantity, String sizeLabel) {
        this();
        this.menuItemId   = item.getId();
        this.menuItemName = item.getName();
        this.category     = item.getCategory();
        this.quantity     = quantity;
        this.unitPrice    = item.calculatePrice();
        this.sizeLabel    = sizeLabel;
    }

    // ── Business helpers ─────────────────────────────────────────────────────

    /**
     * Computes the total price for this line item:
     * (unitPrice + sum of customization costs) × quantity.
     *
     * @return line total in dollars
     */
    public double getLineTotal() {
        double customizationTotal = chosenCustomizations.stream()
                .mapToDouble(Customization::getExtraCost)
                .sum();
        return (unitPrice + customizationTotal) * quantity;
    }

    /** Adds a chosen customization to this order line. */
    public void addChosenCustomization(Customization c) {
        this.chosenCustomizations.add(c);
    }

    /** Returns a human-readable summary for UI display. */
    public String getDisplaySummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(menuItemName);
        if (sizeLabel != null && !sizeLabel.isBlank()) {
            sb.append(" (").append(sizeLabel).append(")");
        }
        if (!chosenCustomizations.isEmpty()) {
            sb.append(" + ");
            chosenCustomizations.forEach(c -> sb.append(c.getName()).append(", "));
            sb.setLength(sb.length() - 2); // trim trailing comma
        }
        sb.append(" x").append(quantity);
        sb.append(" = $").append(String.format("%.2f", getLineTotal()));
        return sb.toString();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getMenuItemId()                              { return menuItemId; }
    public void   setMenuItemId(String id)                     { this.menuItemId = id; }

    public String getMenuItemName()                            { return menuItemName; }
    public void   setMenuItemName(String name)                 { this.menuItemName = name; }

    public String getCategory()                                { return category; }
    public void   setCategory(String category)                 { this.category = category; }

    public int  getQuantity()                                  { return quantity; }
    public void setQuantity(int quantity)                      { this.quantity = quantity; }

    public double getUnitPrice()                               { return unitPrice; }
    public void   setUnitPrice(double price)                   { this.unitPrice = price; }

    public String getSizeLabel()                               { return sizeLabel; }
    public void   setSizeLabel(String label)                   { this.sizeLabel = label; }

    public List<Customization> getChosenCustomizations()       { return chosenCustomizations; }
    public void setChosenCustomizations(List<Customization> c) { this.chosenCustomizations = c; }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem other)) return false;
        return Objects.equals(menuItemId, other.menuItemId) &&
               quantity == other.quantity &&
               Objects.equals(sizeLabel, other.sizeLabel) &&
               Objects.equals(chosenCustomizations, other.chosenCustomizations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItemId, quantity, sizeLabel, chosenCustomizations);
    }

    @Override
    public String toString() {
        return "OrderItem{" + getDisplaySummary() + "}";
    }
}
