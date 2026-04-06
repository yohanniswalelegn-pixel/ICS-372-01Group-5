package com.brewbite.model.menu;

import com.brewbite.observer.BrewObserver;
import com.brewbite.observer.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds and manages the full menu catalog for Brew & Bite.
 *
 * Implements Observable (Observer Pattern) so that the Customer UI
 * and Manager UI update in real-time when items are added, changed,
 * or removed.
 *
 * Responsibility: Store all MenuItem objects and provide filtered views
 * (by category/type). Manager operations (add, update, remove) also
 * live here because they are business-logic changes to the catalog, not
 * UI concerns.
 */
public class MenuCatalog implements Observable {

    // ── Event type constants ─────────────────────────────────────────────────
    public static final String EVENT_ITEM_ADDED   = "MENU_ITEM_ADDED";
    public static final String EVENT_ITEM_UPDATED = "MENU_ITEM_UPDATED";
    public static final String EVENT_ITEM_REMOVED = "MENU_ITEM_REMOVED";

    /** All menu items (both available and unavailable). */
    private final List<MenuItem> items = new ArrayList<>();

    /** Registered observers. */
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

    // ── Catalog operations ───────────────────────────────────────────────────

    /**
     * Adds a new item to the catalog.
     * Notifies observers with EVENT_ITEM_ADDED.
     *
     * @param item the item to add
     */
    public void addItem(MenuItem item) {
        items.add(item);
        notifyObservers(EVENT_ITEM_ADDED, item);
    }

    /**
     * Updates an existing item in-place (by ID).
     * Notifies observers with EVENT_ITEM_UPDATED.
     *
     * @param updated the item carrying the new state (must have matching id)
     */
    public void updateItem(MenuItem updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updated.getId())) {
                items.set(i, updated);
                notifyObservers(EVENT_ITEM_UPDATED, updated);
                return;
            }
        }
    }

    /**
     * Removes an item from the catalog by ID.
     * Notifies observers with EVENT_ITEM_REMOVED.
     *
     * @param itemId the ID of the item to remove
     */
    public void removeItem(String itemId) {
        MenuItem removed = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst().orElse(null);
        if (removed != null) {
            items.remove(removed);
            notifyObservers(EVENT_ITEM_REMOVED, removed);
        }
    }

    /**
     * Returns a copy of all items (available and unavailable).
     */
    public List<MenuItem> getAllItems() {
        return List.copyOf(items);
    }

    /**
     * Returns only items that are currently available.
     */
    public List<MenuItem> getAvailableItems() {
        return items.stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Returns all available items of a given category ("Beverage" or "Pastry").
     *
     * @param category the category label to filter by
     */
    public List<MenuItem> getByCategory(String category) {
        return items.stream()
                .filter(MenuItem::isAvailable)
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Returns all available beverages of a given type ("Coffee" or "Tea").
     *
     * @param beverageType "Coffee" or "Tea"
     */
    public List<Beverage> getBeveragesByType(String beverageType) {
        return items.stream()
                .filter(MenuItem::isAvailable)
                .filter(i -> i instanceof Beverage)
                .map(i -> (Beverage) i)
                .filter(b -> b.getBeverageType().equalsIgnoreCase(beverageType))
                .collect(Collectors.toList());
    }

    /**
     * Finds a single item by ID.
     *
     * @param id the item ID
     * @return the MenuItem or null if not found
     */
    public MenuItem findById(String id) {
        return items.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst().orElse(null);
    }

    /**
     * Replaces all items with the provided list.
     * Used during initial seed loading (does NOT fire events).
     *
     * @param loaded list of items loaded from JSON
     */
    public void restoreItems(List<MenuItem> loaded) {
        items.clear();
        items.addAll(loaded);
    }
}
