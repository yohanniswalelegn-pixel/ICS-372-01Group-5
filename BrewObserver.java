package com.brewbite.observer;

/**
 * Observer interface for the Observer Pattern implementation.
 *
 * Any class that needs to react to model changes (e.g. new orders,
 * status updates, inventory changes) implements this interface.
 *
 * Responsibility: Define the callback contract so that UI controllers
 * (Views/Controllers in MVC) can register themselves with model objects
 * and receive real-time change notifications without tight coupling.
 *
 * Usage:
 *   1. A model class (e.g. OrderQueue) implements Observable.
 *   2. UI controllers implement BrewObserver and call observable.addObserver(this).
 *   3. When the model changes, it calls notifyObservers(), which invokes
 *      onUpdate(eventType, data) on every registered observer.
 */
public interface BrewObserver {

    /**
     * Called by an Observable when its state has changed.
     *
     * @param eventType a string key describing what changed,
     *                  e.g. "ORDER_PLACED", "STATUS_CHANGED", "INVENTORY_UPDATED"
     * @param data      the changed object (cast to the expected type in the handler)
     */
    void onUpdate(String eventType, Object data);
}
