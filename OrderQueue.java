package com.brewbite.model.order;

import com.brewbite.observer.BrewObserver;
import com.brewbite.observer.Observable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Manages the FIFO queue of active orders and the history of fulfilled orders.
 *
 * Implements Observable (Observer Pattern) so that both the Barista UI
 * and the Manager UI receive real-time updates when orders change.
 *
 * Responsibility: Maintain order state, enforce FIFO ordering, and
 * broadcast change events to all registered observers. This class does
 * NOT interact with the UI or persistence layers directly.
 */
public class OrderQueue implements Observable {

    // ── Event type constants ─────────────────────────────────────────────────
    public static final String EVENT_ORDER_PLACED         = "ORDER_PLACED";
    public static final String EVENT_STATUS_CHANGED       = "STATUS_CHANGED";
    public static final String EVENT_ORDER_FULFILLED      = "ORDER_FULFILLED";

    /** Active orders awaiting or in-progress fulfillment (FIFO). */
    private final Deque<Order> pendingOrders = new ArrayDeque<>();

    /** Completed orders — accessible to managers as a history log. */
    private final List<Order> fulfilledOrders = new ArrayList<>();

    /** Registered observers (Barista and Manager UI controllers). */
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

    // ── Queue operations ─────────────────────────────────────────────────────

    /**
     * Adds a new order to the back of the pending queue.
     * Notifies observers with EVENT_ORDER_PLACED.
     *
     * @param order the placed order; must not be empty
     * @throws IllegalArgumentException if the order has no items
     */
    public void placeOrder(Order order) {
        if (order.isEmpty()) {
            throw new IllegalArgumentException("Cannot place an empty order.");
        }
        pendingOrders.addLast(order);
        notifyObservers(EVENT_ORDER_PLACED, order);
    }

    /**
     * Advances the status of the given order by one step.
     * If the order becomes FULFILLED it is moved to fulfilledOrders.
     * Notifies observers accordingly.
     *
     * @param order the order whose status should advance
     */
    public void advanceOrderStatus(Order order) {
        order.advanceStatus();

        if (order.getStatus() == OrderStatus.FULFILLED) {
            pendingOrders.remove(order);
            fulfilledOrders.add(order);
            notifyObservers(EVENT_ORDER_FULFILLED, order);
        } else {
            notifyObservers(EVENT_STATUS_CHANGED, order);
        }
    }

    /**
     * Returns an unmodifiable snapshot of the current pending orders
     * in FIFO order (oldest first).
     */
    public List<Order> getPendingOrders() {
        return List.copyOf(pendingOrders);
    }

    /**
     * Returns an unmodifiable snapshot of all fulfilled orders.
     */
    public List<Order> getFulfilledOrders() {
        return List.copyOf(fulfilledOrders);
    }

    /**
     * Finds an order by its ID across both pending and fulfilled lists.
     *
     * @param orderId the ID to search for
     * @return the matching Order, or null if not found
     */
    public Order findById(String orderId) {
        for (Order o : pendingOrders) {
            if (o.getOrderId().equals(orderId)) return o;
        }
        for (Order o : fulfilledOrders) {
            if (o.getOrderId().equals(orderId)) return o;
        }
        return null;
    }

    /**
     * Restores saved orders on application startup (from persistence).
     * Does NOT fire observer events — used only during load phase.
     *
     * @param pending   previously pending orders in order
     * @param fulfilled previously fulfilled orders
     */
    public void restoreOrders(List<Order> pending, List<Order> fulfilled) {
        pendingOrders.clear();
        fulfilledOrders.clear();
        pendingOrders.addAll(pending);
        fulfilledOrders.addAll(fulfilled);
    }
}
