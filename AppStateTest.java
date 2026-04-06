package com.brewbite;

import com.brewbite.factory.MenuItemFactory;
import com.brewbite.model.AppState;
import com.brewbite.model.inventory.Ingredient;
import com.brewbite.model.inventory.InventoryManager;
import com.brewbite.model.menu.*;
import com.brewbite.model.order.*;
import com.brewbite.model.user.User;
import com.brewbite.model.user.UserRole;
import com.brewbite.observer.BrewObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Person 1's model classes, factory, inventory, and observer code.
 *
 * Run with: mvn test
 */
class AppStateTest {

    // ── MenuItemFactory ───────────────────────────────────────────────────────

    @Test
    void factoryCreatesLatte() {
        Beverage latte = MenuItemFactory.createLatte();
        assertNotNull(latte);
        assertEquals("Latte", latte.getName());
        assertEquals("Coffee", latte.getBeverageType());
        assertFalse(latte.getAvailableCustomizations().isEmpty());
        assertFalse(latte.getIngredientRequirements().isEmpty());
    }

    @Test
    void factoryCreatesMuffin() {
        Pastry muffin = MenuItemFactory.createMuffin("Blueberry");
        assertNotNull(muffin);
        assertEquals("Blueberry Muffin", muffin.getName());
        assertEquals("Muffin", muffin.getPastryType());
    }

    @Test
    void factoryCreatesByNameReturnsNullForUnknown() {
        assertNull(MenuItemFactory.createByName("Unknown Item"));
    }

    @Test
    void factoryCreatesByNameReturnsCorrectType() {
        MenuItem item = MenuItemFactory.createByName("Green Tea");
        assertInstanceOf(Beverage.class, item);
        assertEquals("Green Tea", item.getName());
    }

    // ── BeverageSize price multiplier ─────────────────────────────────────────

    @Test
    void beveragePriceScalesWithSize() {
        Beverage latte = MenuItemFactory.createLatte(); // basePrice = 4.50
        latte.setSelectedSize(BeverageSize.SMALL);
        assertEquals(4.50 * 1.0, latte.calculatePrice(), 0.001);

        latte.setSelectedSize(BeverageSize.LARGE);
        assertEquals(4.50 * 1.5, latte.calculatePrice(), 0.001);
    }

    // ── Pastry fixed price ─────────────────────────────────────────────────────

    @Test
    void pastryPriceIsFixed() {
        Pastry cookie = MenuItemFactory.createCookie("Chocolate Chip");
        assertEquals(2.75, cookie.calculatePrice(), 0.001);
        assertEquals("Pastry", cookie.getCategory());
    }

    // ── OrderItem line total ───────────────────────────────────────────────────

    @Test
    void orderItemLineTotalIncludesCustomizations() {
        Beverage latte = MenuItemFactory.createLatte();
        latte.setSelectedSize(BeverageSize.MEDIUM); // 4.50 * 1.25 = 5.625

        OrderItem item = new OrderItem(latte, 2, "Medium");
        item.addChosenCustomization(new Customization("Extra Shot", 0.75));
        // (5.625 + 0.75) * 2 = 12.75
        assertEquals(12.75, item.getLineTotal(), 0.001);
    }

    // ── Order total and status ────────────────────────────────────────────────

    @Test
    void orderTotalSumsAllItems() {
        Order order = new Order("Alice");
        Beverage espresso = MenuItemFactory.createEspresso();
        espresso.setSelectedSize(BeverageSize.SMALL); // 3.00
        order.addItem(new OrderItem(espresso, 1, "Small"));

        Pastry croissant = MenuItemFactory.createCroissant("Butter"); // 3.50
        order.addItem(new OrderItem(croissant, 1, ""));

        assertEquals(6.50, order.getTotal(), 0.001);
    }

    @Test
    void orderStatusAdvancesCorrectly() {
        Order order = new Order("Bob");
        assertEquals(OrderStatus.PENDING, order.getStatus());
        order.advanceStatus();
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        order.advanceStatus();
        assertEquals(OrderStatus.READY_FOR_PICKUP, order.getStatus());
        order.advanceStatus();
        assertEquals(OrderStatus.FULFILLED, order.getStatus());
        assertNotNull(order.getFulfilledAt());
    }

    @Test
    void fulfilledOrderStatusDoesNotAdvanceFurther() {
        Order order = new Order("Carol");
        order.advanceStatus();
        order.advanceStatus();
        order.advanceStatus(); // now FULFILLED
        order.advanceStatus(); // should be no-op
        assertEquals(OrderStatus.FULFILLED, order.getStatus());
    }

    // ── OrderQueue FIFO ───────────────────────────────────────────────────────

    @Test
    void orderQueueMaintainsFifoOrder() {
        OrderQueue queue = new OrderQueue();
        Order first  = makeOrder("First");
        Order second = makeOrder("Second");
        queue.placeOrder(first);
        queue.placeOrder(second);

        List<Order> pending = queue.getPendingOrders();
        assertEquals("First",  pending.get(0).getCustomerName());
        assertEquals("Second", pending.get(1).getCustomerName());
    }

    @Test
    void fulfilledOrderMovesOutOfPendingQueue() {
        OrderQueue queue = new OrderQueue();
        Order order = makeOrder("Dave");
        queue.placeOrder(order);

        queue.advanceOrderStatus(order); // PENDING → IN_PROGRESS
        queue.advanceOrderStatus(order); // IN_PROGRESS → READY
        queue.advanceOrderStatus(order); // READY → FULFILLED

        assertTrue(queue.getPendingOrders().isEmpty());
        assertEquals(1, queue.getFulfilledOrders().size());
    }

    @Test
    void emptyOrderCannotBePlaced() {
        OrderQueue queue = new OrderQueue();
        Order empty = new Order("Eve");
        assertThrows(IllegalArgumentException.class, () -> queue.placeOrder(empty));
    }

    // ── InventoryManager ─────────────────────────────────────────────────────

    @Test
    void inventoryDeductsOnConsume() {
        InventoryManager inv = new InventoryManager();
        inv.addIngredient(new Ingredient("Coffee Beans", 100, "g"));

        Beverage latte = MenuItemFactory.createLatte();
        OrderItem item = new OrderItem(latte, 1, "Medium");
        assertTrue(inv.canFulfill(item, latte));

        inv.consumeIngredients(item, latte);
        assertEquals(82.0, inv.getIngredient("Coffee Beans").getQuantity(), 0.001);
    }

    @Test
    void inventoryRejectsOrderWhenStockInsufficient() {
        InventoryManager inv = new InventoryManager();
        inv.addIngredient(new Ingredient("Coffee Beans", 5, "g")); // need 18g

        Beverage latte = MenuItemFactory.createLatte();
        OrderItem item = new OrderItem(latte, 1, "Medium");
        assertFalse(inv.canFulfill(item, latte));
    }

    @Test
    void restockIncreasesQuantity() {
        InventoryManager inv = new InventoryManager();
        inv.addIngredient(new Ingredient("Milk", 100, "ml"));
        inv.restock("Milk", 500);
        assertEquals(600.0, inv.getIngredient("Milk").getQuantity(), 0.001);
    }

    // ── Observer Pattern ──────────────────────────────────────────────────────

    @Test
    void observerReceivesOrderPlacedEvent() {
        OrderQueue queue = new OrderQueue();
        List<String> events = new ArrayList<>();
        BrewObserver obs = (eventType, data) -> events.add(eventType);
        queue.addObserver(obs);

        queue.placeOrder(makeOrder("Frank"));
        assertTrue(events.contains(OrderQueue.EVENT_ORDER_PLACED));
    }

    @Test
    void observerReceivesInventoryUpdatedEvent() {
        InventoryManager inv = new InventoryManager();
        inv.addIngredient(new Ingredient("Sugar", 200, "g"));
        List<String> events = new ArrayList<>();
        inv.addObserver((eventType, data) -> events.add(eventType));

        inv.restock("Sugar", 100);
        assertTrue(events.contains(InventoryManager.EVENT_INVENTORY_UPDATED));
    }

    @Test
    void removedObserverDoesNotReceiveEvents() {
        OrderQueue queue = new OrderQueue();
        List<String> events = new ArrayList<>();
        BrewObserver obs = (eventType, data) -> events.add(eventType);
        queue.addObserver(obs);
        queue.removeObserver(obs);

        queue.placeOrder(makeOrder("Grace"));
        assertTrue(events.isEmpty());
    }

    // ── User authentication ───────────────────────────────────────────────────

    @Test
    void userAuthenticatesWithCorrectPassword() {
        User barista = new User("barista1", "brew123", UserRole.BARISTA);
        assertTrue(barista.authenticate("brew123"));
        assertFalse(barista.authenticate("wrong"));
    }

    @Test
    void customerHasNoPassword() {
        User customer = new User("Alice");
        assertEquals(UserRole.CUSTOMER, customer.getRole());
        assertFalse(customer.authenticate("anything"));
    }

    // ── MenuCatalog ───────────────────────────────────────────────────────────

    @Test
    void catalogFiltersAvailableItemsOnly() {
        MenuCatalog catalog = new MenuCatalog();
        Beverage latte = MenuItemFactory.createLatte();
        Beverage espresso = MenuItemFactory.createEspresso();
        espresso.setAvailable(false);
        catalog.addItem(latte);
        catalog.addItem(espresso);

        assertEquals(1, catalog.getAvailableItems().size());
        assertEquals("Latte", catalog.getAvailableItems().get(0).getName());
    }

    @Test
    void catalogGetsByCategoryCorrectly() {
        MenuCatalog catalog = new MenuCatalog();
        catalog.addItem(MenuItemFactory.createLatte());
        catalog.addItem(MenuItemFactory.createCroissant("Butter"));

        assertEquals(1, catalog.getByCategory("Beverage").size());
        assertEquals(1, catalog.getByCategory("Pastry").size());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Order makeOrder(String customerName) {
        Order order = new Order(customerName);
        Beverage latte = MenuItemFactory.createLatte();
        order.addItem(new OrderItem(latte, 1, "Medium"));
        return order;
    }
}
