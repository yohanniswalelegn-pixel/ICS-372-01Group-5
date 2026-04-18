package com.brewbite.model.order;

/**
 * Lifecycle states for an Order in the Brew & Bite system.
 * Baristas move orders through these states from the fulfillment screen.
 */
public enum OrderStatus {

    PENDING         ("Pending"),
    IN_PROGRESS     ("In Progress"),
    READY_FOR_PICKUP("Ready for Pickup"),
    FULFILLED       ("Fulfilled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
