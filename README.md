# Person 1 — Data & Core Models (Brew & Bite)

## What's in this folder

This is **Person 1's complete deliverable** for the Brew & Bite group project.
It covers the entire data/model/persistence layer that all other team members build on top of.

---

## Package structure

```
com.brewbite/
├── model/
│   ├── AppState.java               ← Singleton; central state shared across all controllers
│   ├── user/
│   │   ├── User.java               ← Data class: username, password, role
│   │   └── UserRole.java           ← Enum: CUSTOMER, BARISTA, MANAGER
│   ├── menu/
│   │   ├── MenuItem.java           ← Abstract base class (Inheritance root)
│   │   ├── Beverage.java           ← Extends MenuItem; adds type, size, customizations
│   │   ├── Pastry.java             ← Extends MenuItem; adds pastryType, variation
│   │   ├── MenuCatalog.java        ← Holds all items; fires Observer events on changes
│   │   ├── BeverageSize.java       ← Enum: SMALL, MEDIUM, LARGE (with price multiplier)
│   │   └── Customization.java      ← Data class: add-on name + extra cost
│   ├── order/
│   │   ├── Order.java              ← Data class: customer name, items, status, timestamps
│   │   ├── OrderItem.java          ← Data class: one line in an order (snapshot + line total)
│   │   ├── OrderQueue.java         ← FIFO queue; fires Observer events on state changes
│   │   └── OrderStatus.java        ← Enum: PENDING → IN_PROGRESS → READY → FULFILLED
│   └── inventory/
│       ├── Ingredient.java         ← Data class: name, quantity, unit
│       ├── IngredientRequirement.java ← Data class: ingredient name + amount per item
│       └── InventoryManager.java   ← Tracks stock; fires Observer events on changes
├── factory/
│   └── MenuItemFactory.java        ← Factory Method Pattern; creates all MenuItem types
├── observer/
│   ├── BrewObserver.java           ← Observer interface (onUpdate callback)
│   └── Observable.java             ← Observable interface (add/remove/notify)
└── persistence/
    └── PersistenceManager.java     ← Reads/writes all JSON files; seeds defaults on first run
```

---

## OO patterns implemented here

| Pattern / Principle | Where |
|---|---|
| **Factory Method** | `MenuItemFactory` — creates every MenuItem type via a named factory method |
| **Observer** | `OrderQueue`, `InventoryManager`, `MenuCatalog` all implement `Observable`; UI controllers will implement `BrewObserver` |
| **Inheritance** | `MenuItem` ← `Beverage`, `Pastry` |
| **Composition** | `Order` has `OrderItem` list; `MenuItem` has `IngredientRequirement` list; `Beverage` has `Customization` list |
| **SRP** | Each class has one job: `PersistenceManager` only does I/O; `InventoryManager` only tracks stock; `OrderQueue` only manages the queue |

---

## How other team members use this code

### Person 2 — Customer UI
```java
AppState state = AppState.getInstance();
state.initialize(); // call once at app startup

// Start a customer session
state.startCustomerSession("Alice");

// Browse the menu
List<MenuItem> beverages = state.getMenuCatalog().getByCategory("Beverage");

// Place an order
Order order = new Order(state.getCurrentUser().getUsername());
OrderItem item = new OrderItem(selectedItem, 1, "Large");
item.addChosenCustomization(new Customization("Extra Shot", 0.75));
order.addItem(item);
state.getOrderQueue().placeOrder(order);
```

### Person 3 — Barista UI
```java
// Register as observer to get live order updates
state.getOrderQueue().addObserver(this); // 'this' implements BrewObserver

// Get pending orders (FIFO)
List<Order> pending = state.getOrderQueue().getPendingOrders();

// Advance order status
state.getOrderQueue().advanceOrderStatus(selectedOrder);

// Implement BrewObserver callback
@Override
public void onUpdate(String eventType, Object data) {
    if (eventType.equals(OrderQueue.EVENT_ORDER_PLACED)) {
        // refresh the order list in UI
    }
}
```

### Person 4 — Manager UI
```java
// Watch inventory changes
state.getInventoryManager().addObserver(this);

// Restock an ingredient
state.getInventoryManager().restock("Coffee Beans", 500);

// Add a new menu item
MenuItem newItem = MenuItemFactory.createLatte(); // or build manually
state.getMenuCatalog().addItem(newItem);
```

---

## Running the tests

```bash
mvn test
```

All 20+ unit tests are in `src/test/java/com/brewbite/AppStateTest.java`.

---

## Building the full project

```bash
mvn package
java -jar target/brew-and-bite-1.0-SNAPSHOT.jar
```

> Note: JavaFX needs to be on the module path. See pom.xml — the maven-shade-plugin
> bundles everything into a fat JAR. If running from command line without an IDE,
> you may need to add `--add-modules javafx.controls,javafx.fxml` to the java command.

---

## Default login credentials (from users.json seed)

| Role    | Username  | Password   |
|---------|-----------|------------|
| Barista | barista1  | brew123    |
| Barista | barista2  | brew123    |
| Manager | manager1  | manage456  |

Customers do not log in — they just enter their name.
