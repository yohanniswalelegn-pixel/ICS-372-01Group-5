package com.brewbite.model.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Data class representing a customer's order in the Brew & Bite system.
 *
 * Demonstrates COMPOSITION — an Order "has" a list of OrderItems.
 *
 * Responsibility: Store the full state of one order: its identity,
 * items, status, timestamps, and customer name.
 */
public class Order {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM d, h:mm a");

    private String      orderId;
    private String      customerName;
    private List<OrderItem> items;
    private OrderStatus status;
    private LocalDateTime placedAt;
    private LocalDateTime fulfilledAt; // null until fulfilled

    // ── Constructors ─────────────────────────────────────────────────────────

    /** No-arg constructor required for JSON deserialization. */
    public Order() {
        this.orderId  = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.items    = new ArrayList<>();
        this.status   = OrderStatus.PENDING;
        this.placedAt = LocalDateTime.now();
    }

    /**
     * Convenience constructor for placing a new order.
     *
     * @param customerName the name entered by the customer
     */
    public Order(String customerName) {
        this();
        this.customerName = customerName;
    }

    // ── Business helpers ─────────────────────────────────────────────────────

    /**
     * Adds one item to the order.
     *
     * @param item the configured OrderItem to add
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    /**
     * Calculates the sum of all line totals.
     *
     * @return order total in dollars
     */
    public double getTotal() {
        return items.stream().mapToDouble(OrderItem::getLineTotal).sum();
    }

    /**
     * Returns true if this order has no items.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Advances the order to the next logical status.
     * PENDING → IN_PROGRESS → READY_FOR_PICKUP → FULFILLED
     * Once FULFILLED, calling this is a no-op.
     */
    public void advanceStatus() {
        switch (status) {
            case PENDING          -> status = OrderStatus.IN_PROGRESS;
            case IN_PROGRESS      -> status = OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> {
                status      = OrderStatus.FULFILLED;
                fulfilledAt = LocalDateTime.now();
            }
            case FULFILLED        -> { /* already done */ }
        }
    }

    /** Returns a short display string for the order header. */
    public String getDisplayHeader() {
        return "Order #" + orderId + " — " + customerName +
               " | " + placedAt.format(DISPLAY_FMT) +
               " | " + status.getDisplayName();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getOrderId()                      { return orderId; }
    public void   setOrderId(String id)             { this.orderId = id; }

    public String getCustomerName()                 { return customerName; }
    public void   setCustomerName(String name)      { this.customerName = name; }

    public List<OrderItem> getItems()               { return items; }
    public void            setItems(List<OrderItem> items) { this.items = items; }

    public OrderStatus getStatus()                  { return status; }
    public void        setStatus(OrderStatus s)     { this.status = s; }

    public LocalDateTime getPlacedAt()              { return placedAt; }
    public void          setPlacedAt(LocalDateTime t) { this.placedAt = t; }

    public LocalDateTime getFulfilledAt()           { return fulfilledAt; }
    public void          setFulfilledAt(LocalDateTime t) { this.fulfilledAt = t; }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return Objects.equals(orderId, other.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "Order{id=" + orderId + ", customer=" + customerName +
               ", items=" + items.size() + ", status=" + status +
               ", total=$" + String.format("%.2f", getTotal()) + "}";
    }
}
