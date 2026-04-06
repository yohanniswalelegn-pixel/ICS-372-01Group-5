package com.brewbite.observer;

/**
 * Observable interface for the Observer Pattern implementation.
 *
 * Model classes that broadcast changes implement this interface.
 * This keeps the observer registration logic separate from the
 * model's primary data responsibility (Single Responsibility Principle).
 *
 * Responsibility: Define the contract for registering, removing,
 * and notifying observers.
 */
public interface Observable {

    /**
     * Registers an observer to receive future change notifications.
     *
     * @param observer the observer to add
     */
    void addObserver(BrewObserver observer);

    /**
     * Unregisters an observer so it no longer receives notifications.
     *
     * @param observer the observer to remove
     */
    void removeObserver(BrewObserver observer);

    /**
     * Notifies all currently registered observers of a state change.
     *
     * @param eventType string key identifying the type of change
     * @param data      the changed data object
     */
    void notifyObservers(String eventType, Object data);
}
