package com.brewbite.model.inventory;

import com.brewbite.observer.BrewObserver;
import com.brewbite.observer.Observable;
import com.brewbite.model.menu.MenuItem;
import com.brewbite.model.order.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the Brew & Bite ingredient inventory.
 *
 * Implements Observable (Observer Pattern) so that the Manager UI
 * receives real-time updates whenever inventory levels change.
 *
 * Responsibility: Track ingredient stock levels, check availability
 * before orders are placed, deduct ingredients when orders are confirmed,
 * and allow managers to restock. This is a domain-model class — it does
 * NOT interact with the UI or persistence layers directly.
 */
public class InventoryManager implements Observable {

    // ── Event type constants used when notifying observers ───────────────────
    public static final String EVENT_INVENTORY_UPDATED = "INVENTORY_UPDATED";
    public static final String EVENT_ITEM_LOW_STOCK    = "ITEM_LOW_STOCK";

    /** Threshold below which an ingredient is flagged as low stock. */
    private static final double LOW_STOCK_THRESHOLD = 50.0;

    /** The ingredient store — keyed by ingredient name. */
    private final Map<String, Ingredient> ingredients = new HashMap<>();

    /** Registered observers (typically Manager UI controller). */
    private final List<BrewObserver> observers = new ArrayList<>();

    // ── Observable implementation ────────────────────────────────────────────

    @Override
    public void addObserver(BrewObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(BrewObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String eventType, Object data) {
        for (BrewObserver obs : observers) {
            obs.onUpdate(eventType, data);
        }
    }

    // ── Inventory operations ─────────────────────────────────────────────────

    /**
     * Adds or replaces an ingredient in the inventory.
     * Used during initial seed loading.
     *
     * @param ingredient the ingredient to register
     */
    public void addIngredient(Ingredient ingredient) {
        ingredients.put(ingredient.getName(), ingredient);
    }

    /**
     * Returns the ingredient with the given name, or null if not tracked.
     *
     * @param name ingredient name
     * @return the Ingredient, or null
     */
    public Ingredient getIngredient(String name) {
        return ingredients.get(name);
    }

    /**
     * Returns all tracked ingredients as an unmodifiable snapshot.
     */
    public List<Ingredient> getAllIngredients() {
        return List.copyOf(ingredients.values());
    }

    /**
     * Checks whether all ingredients required for a single OrderItem
     * are currently in stock.
     *
     * @param item       the OrderItem being checked
     * @param menuItem   the corresponding MenuItem (holds requirements)
     * @return true if all ingredients are available
     */
    public boolean canFulfill(OrderItem item, MenuItem menuItem) {
        for (var req : menuItem.getIngredientRequirements()) {
            if (req.getAmountRequired() <= 0) continue;
            Ingredient ing = ingredients.get(req.getIngredientName());
            if (ing == null || !ing.hasSufficient(req.getAmountRequired() * item.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deducts the ingredients consumed by a single OrderItem.
     * Caller must have verified canFulfill() first.
     *
     * @param item     the OrderItem being fulfilled
     * @param menuItem the corresponding MenuItem
     */
    public void consumeIngredients(OrderItem item, MenuItem menuItem) {
        for (var req : menuItem.getIngredientRequirements()) {
            if (req.getAmountRequired() <= 0) continue;
            Ingredient ing = ingredients.get(req.getIngredientName());
            if (ing != null) {
                ing.consume(req.getAmountRequired() * item.getQuantity());
                if (ing.getQuantity() < LOW_STOCK_THRESHOLD) {
                    notifyObservers(EVENT_ITEM_LOW_STOCK, ing);
                }
            }
        }
        notifyObservers(EVENT_INVENTORY_UPDATED, getAllIngredients());
    }

    /**
     * Restocks a named ingredient by the specified amount.
     * Used from the Manager screen.
     *
     * @param ingredientName the ingredient to restock
     * @param amount         quantity to add
     * @throws IllegalArgumentException if the ingredient is not tracked
     */
    public void restock(String ingredientName, double amount) {
        Ingredient ing = ingredients.get(ingredientName);
        if (ing == null) {
            throw new IllegalArgumentException("Unknown ingredient: " + ingredientName);
        }
        ing.restock(amount);
        notifyObservers(EVENT_INVENTORY_UPDATED, getAllIngredients());
    }

    /**
     * Returns a map snapshot of ingredient name → current quantity.
     * Useful for serialization.
     */
    public Map<String, Double> getStockSnapshot() {
        Map<String, Double> snapshot = new HashMap<>();
        ingredients.forEach((name, ing) -> snapshot.put(name, ing.getQuantity()));
        return snapshot;
    }
}
