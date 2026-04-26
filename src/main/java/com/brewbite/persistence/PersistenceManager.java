package com.brewbite.persistence;

import com.brewbite.model.inventory.Ingredient;
import com.brewbite.model.inventory.IngredientRequirement;
import com.brewbite.model.inventory.InventoryManager;
import com.brewbite.model.menu.*;
import com.brewbite.model.order.Order;
import com.brewbite.model.order.OrderItem;
import com.brewbite.model.order.OrderQueue;
import com.brewbite.model.order.OrderStatus;
import com.brewbite.model.user.User;
import com.brewbite.model.user.UserRole;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all JSON serialization and deserialization for the Brew & Bite system.
 *
 * Responsibility: Read application state from JSON files on startup,
 * and write it back on exit. This class is the only place in the
 * application that knows about file paths and JSON structure — all
 * other classes work with plain Java objects.
 *
 * Layered Architecture note: This class belongs to the Persistence layer.
 * It is called by the application entry point and should never be
 * called directly from UI controllers.
 */
public class PersistenceManager {

    // ── File paths ────────────────────────────────────────────────────────────
    private static final String DATA_DIR        = "data/";
    private static final String MENU_FILE       = DATA_DIR + "menu.json";
    private static final String INVENTORY_FILE  = DATA_DIR + "inventory.json";
    private static final String ORDERS_FILE     = DATA_DIR + "orders.json";
    private static final String USERS_FILE      = DATA_DIR + "users.json";

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ── Gson instance with custom adapters ────────────────────────────────────
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DT_FMT)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                            LocalDateTime.parse(json.getAsString(), DT_FMT))
            .create();

    // ── Private constructor — utility class ───────────────────────────────────
    private PersistenceManager() {}

    // ════════════════════════════════════════════════════════════════════════
    // SAVE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Saves the entire application state to JSON files.
     * Called when the application exits.
     *
     * @param catalog   the current menu catalog
     * @param inventory the current inventory manager
     * @param queue     the current order queue
     */
    public static void saveAll(MenuCatalog catalog,
                                InventoryManager inventory,
                                OrderQueue queue) {
        ensureDataDir();
        saveMenu(catalog);
        saveInventory(inventory);
        saveOrders(queue);
    }

    /** Writes all menu items to menu.json. */
    public static void saveMenu(MenuCatalog catalog) {
        List<JsonObject> jsonItems = new ArrayList<>();
        for (MenuItem item : catalog.getAllItems()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id",        item.getId());
            obj.addProperty("name",      item.getName());
            obj.addProperty("category",  item.getCategory());
            obj.addProperty("basePrice", item.getBasePrice());
            obj.addProperty("available", item.isAvailable());
            obj.add("ingredientRequirements",
                    GSON.toJsonTree(item.getIngredientRequirements()));

            if (item instanceof Beverage bev) {
                obj.addProperty("beverageType", bev.getBeverageType());
                obj.addProperty("selectedSize", bev.getSelectedSize().name());
                obj.add("availableCustomizations",
                        GSON.toJsonTree(bev.getAvailableCustomizations()));
            } else if (item instanceof Pastry pas) {
                obj.addProperty("pastryType", pas.getPastryType());
                obj.addProperty("variation",  pas.getVariation());
            }
            jsonItems.add(obj);
        }
        writeFile(MENU_FILE, GSON.toJson(jsonItems));
    }

    /** Writes all ingredients to inventory.json. */
    public static void saveInventory(InventoryManager inventory) {
        writeFile(INVENTORY_FILE, GSON.toJson(inventory.getAllIngredients()));
    }

    /** Writes all orders (pending and fulfilled) to orders.json. */
    public static void saveOrders(OrderQueue queue) {
        JsonObject root = new JsonObject();
        root.add("pending",   GSON.toJsonTree(queue.getPendingOrders()));
        root.add("fulfilled", GSON.toJsonTree(queue.getFulfilledOrders()));
        writeFile(ORDERS_FILE, GSON.toJson(root));
    }

    // ════════════════════════════════════════════════════════════════════════
    // LOAD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Loads and restores the full application state from JSON files.
     * Falls back to default seed data if any file is missing.
     *
     * @param catalog   the catalog to populate
     * @param inventory the inventory manager to populate
     * @param queue     the order queue to populate
     */
    public static void loadAll(MenuCatalog catalog,
                                InventoryManager inventory,
                                OrderQueue queue) {
        loadMenu(catalog);
        loadInventory(inventory);
        loadOrders(queue);
    }

    /** Loads menu items from menu.json into the catalog. */
    public static void loadMenu(MenuCatalog catalog) {
        String json = readFile(MENU_FILE);
        if (json == null) return;

        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        List<MenuItem> loaded = new ArrayList<>();

        for (JsonElement el : arr) {
            JsonObject obj      = el.getAsJsonObject();
            String category     = obj.get("category").getAsString();
            MenuItem item;

            if ("Beverage".equalsIgnoreCase(category)) {
                Beverage bev = new Beverage();
                bev.setBeverageType(obj.get("beverageType").getAsString());
                if (obj.has("selectedSize")) {
                    bev.setSelectedSize(BeverageSize.valueOf(
                            obj.get("selectedSize").getAsString()));
                }
                Type custType = new TypeToken<List<Customization>>(){}.getType();
                if (obj.has("availableCustomizations")) {
                    bev.setAvailableCustomizations(
                            GSON.fromJson(obj.get("availableCustomizations"), custType));
                }
                item = bev;
            } else {
                Pastry pas = new Pastry();
                if (obj.has("pastryType")) pas.setPastryType(obj.get("pastryType").getAsString());
                if (obj.has("variation"))  pas.setVariation(obj.get("variation").getAsString());
                item = pas;
            }

            item.setId(obj.get("id").getAsString());
            item.setName(obj.get("name").getAsString());
            item.setBasePrice(obj.get("basePrice").getAsDouble());
            item.setAvailable(obj.has("available") && obj.get("available").getAsBoolean());

            if (obj.has("ingredientRequirements")) {
                Type reqType = new TypeToken<List<IngredientRequirement>>(){}.getType();
                item.setIngredientRequirements(
                        GSON.fromJson(obj.get("ingredientRequirements"), reqType));
            }
            loaded.add(item);
        }
        catalog.restoreItems(loaded);
    }

    /** Loads ingredients from inventory.json into the inventory manager. */
    public static void loadInventory(InventoryManager inventory) {
        String json = readFile(INVENTORY_FILE);
        if (json == null) return;

        Type type = new TypeToken<List<Ingredient>>(){}.getType();
        List<Ingredient> list = GSON.fromJson(json, type);
        if (list != null) {
            list.forEach(inventory::addIngredient);
        }
    }

    /** Loads orders from orders.json into the queue. */
    public static void loadOrders(OrderQueue queue) {
        String json = readFile(ORDERS_FILE);
        if (json == null) return;

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Type orderListType = new TypeToken<List<Order>>(){}.getType();

        List<Order> pending   = GSON.fromJson(root.get("pending"),   orderListType);
        List<Order> fulfilled = GSON.fromJson(root.get("fulfilled"), orderListType);

        queue.restoreOrders(
            pending   != null ? pending   : new ArrayList<>(),
            fulfilled != null ? fulfilled : new ArrayList<>()
        );
    }

    // ── User accounts (read-only seed) ────────────────────────────────────────

    /**
     * Loads hardcoded staff accounts from users.json.
     * Returns an empty list if the file is missing — callers
     * should fall back to in-code defaults in that case.
     *
     * @return list of staff User objects
     */
    public static List<User> loadUsers() {
        String json = readFile(USERS_FILE);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<User>>(){}.getType();
        List<User> users = GSON.fromJson(json, type);
        return users != null ? users : new ArrayList<>();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SEED DATA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Writes the default seed JSON files if they do not yet exist.
     * Called once on first application launch.
     */
    public static void seedIfMissing() {
        ensureDataDir();
        if (!Files.exists(Path.of(MENU_FILE)))      writeSeedMenu();
        if (!Files.exists(Path.of(INVENTORY_FILE))) writeSeedInventory();
        if (!Files.exists(Path.of(USERS_FILE)))     writeSeedUsers();
        if (!Files.exists(Path.of(ORDERS_FILE)))    writeSeedOrders();
    }

    private static void writeSeedMenu() {
        writeFile(MENU_FILE, """
        [
          {
            "id": "bev-001", "name": "Latte", "category": "Beverage",
            "basePrice": 4.50, "available": true, "beverageType": "Coffee",
            "selectedSize": "MEDIUM",
            "ingredientRequirements": [
              {"ingredientName": "Coffee Beans", "amountRequired": 18},
              {"ingredientName": "Milk", "amountRequired": 200}
            ],
            "availableCustomizations": [
              {"name": "Extra Shot", "extraCost": 0.75},
              {"name": "Decaf", "extraCost": 0.00},
              {"name": "Oat Milk", "extraCost": 0.60},
              {"name": "Almond Milk", "extraCost": 0.60},
              {"name": "Sugar-Free Syrup", "extraCost": 0.50}
            ]
          },
          {
            "id": "bev-002", "name": "Cappuccino", "category": "Beverage",
            "basePrice": 4.25, "available": true, "beverageType": "Coffee",
            "selectedSize": "MEDIUM",
            "ingredientRequirements": [
              {"ingredientName": "Coffee Beans", "amountRequired": 18},
              {"ingredientName": "Milk", "amountRequired": 120}
            ],
            "availableCustomizations": [
              {"name": "Extra Shot", "extraCost": 0.75},
              {"name": "Decaf", "extraCost": 0.00},
              {"name": "Oat Milk", "extraCost": 0.60},
              {"name": "Almond Milk", "extraCost": 0.60},
              {"name": "Sugar-Free Syrup", "extraCost": 0.50}
            ]
          },
          {
            "id": "bev-003", "name": "Espresso", "category": "Beverage",
            "basePrice": 3.00, "available": true, "beverageType": "Coffee",
            "selectedSize": "MEDIUM",
            "ingredientRequirements": [
              {"ingredientName": "Coffee Beans", "amountRequired": 14}
            ],
            "availableCustomizations": [
              {"name": "Extra Shot", "extraCost": 0.75},
              {"name": "Decaf", "extraCost": 0.00},
              {"name": "Sugar-Free Syrup", "extraCost": 0.50}
            ]
          },
          {
            "id": "bev-004", "name": "Green Tea", "category": "Beverage",
            "basePrice": 3.00, "available": true, "beverageType": "Tea",
            "selectedSize": "MEDIUM",
            "ingredientRequirements": [
              {"ingredientName": "Tea Leaves", "amountRequired": 5},
              {"ingredientName": "Water", "amountRequired": 250}
            ],
            "availableCustomizations": [
              {"name": "Oat Milk", "extraCost": 0.60},
              {"name": "Almond Milk", "extraCost": 0.60},
              {"name": "Sugar-Free Syrup", "extraCost": 0.50},
              {"name": "Extra Honey", "extraCost": 0.25}
            ]
          },
          {
            "id": "bev-005", "name": "Black Tea", "category": "Beverage",
            "basePrice": 3.00, "available": true, "beverageType": "Tea",
            "selectedSize": "MEDIUM",
            "ingredientRequirements": [
              {"ingredientName": "Tea Leaves", "amountRequired": 5},
              {"ingredientName": "Water", "amountRequired": 250}
            ],
            "availableCustomizations": [
              {"name": "Oat Milk", "extraCost": 0.60},
              {"name": "Almond Milk", "extraCost": 0.60},
              {"name": "Sugar-Free Syrup", "extraCost": 0.50},
              {"name": "Extra Honey", "extraCost": 0.25}
            ]
          },
          {
            "id": "bev-006", "name": "Herbal Tea", "category": "Beverage",
            "basePrice": 3.25, "available": true, "beverageType": "Tea",
            "selectedSize": "MEDIUM",
            "ingredientRequirements": [
              {"ingredientName": "Herbal Blend", "amountRequired": 5},
              {"ingredientName": "Water", "amountRequired": 250}
            ],
            "availableCustomizations": [
              {"name": "Oat Milk", "extraCost": 0.60},
              {"name": "Almond Milk", "extraCost": 0.60},
              {"name": "Sugar-Free Syrup", "extraCost": 0.50},
              {"name": "Extra Honey", "extraCost": 0.25}
            ]
          },
          {
            "id": "pas-001", "name": "Butter Croissant", "category": "Pastry",
            "basePrice": 3.50, "available": true,
            "pastryType": "Croissant", "variation": "Butter",
            "ingredientRequirements": [
              {"ingredientName": "Flour", "amountRequired": 80},
              {"ingredientName": "Butter", "amountRequired": 30}
            ],
            "availableCustomizations": []
          },
          {
            "id": "pas-002", "name": "Chocolate Croissant", "category": "Pastry",
            "basePrice": 3.75, "available": true,
            "pastryType": "Croissant", "variation": "Chocolate",
            "ingredientRequirements": [
              {"ingredientName": "Flour", "amountRequired": 80},
              {"ingredientName": "Butter", "amountRequired": 30},
              {"ingredientName": "Chocolate Chips", "amountRequired": 20}
            ],
            "availableCustomizations": []
          },
          {
            "id": "pas-003", "name": "Blueberry Muffin", "category": "Pastry",
            "basePrice": 3.25, "available": true,
            "pastryType": "Muffin", "variation": "Blueberry",
            "ingredientRequirements": [
              {"ingredientName": "Flour", "amountRequired": 90},
              {"ingredientName": "Sugar", "amountRequired": 40}
            ],
            "availableCustomizations": []
          },
          {
            "id": "pas-004", "name": "Chocolate Chip Muffin", "category": "Pastry",
            "basePrice": 3.25, "available": true,
            "pastryType": "Muffin", "variation": "Chocolate Chip",
            "ingredientRequirements": [
              {"ingredientName": "Flour", "amountRequired": 90},
              {"ingredientName": "Sugar", "amountRequired": 40},
              {"ingredientName": "Chocolate Chips", "amountRequired": 30}
            ],
            "availableCustomizations": []
          },
          {
            "id": "pas-005", "name": "Chocolate Chip Cookie", "category": "Pastry",
            "basePrice": 2.75, "available": true,
            "pastryType": "Cookie", "variation": "Chocolate Chip",
            "ingredientRequirements": [
              {"ingredientName": "Flour", "amountRequired": 60},
              {"ingredientName": "Sugar", "amountRequired": 30},
              {"ingredientName": "Butter", "amountRequired": 20},
              {"ingredientName": "Chocolate Chips", "amountRequired": 20}
            ],
            "availableCustomizations": []
          },
          {
            "id": "pas-006", "name": "Oatmeal Raisin Cookie", "category": "Pastry",
            "basePrice": 2.75, "available": true,
            "pastryType": "Cookie", "variation": "Oatmeal Raisin",
            "ingredientRequirements": [
              {"ingredientName": "Flour", "amountRequired": 60},
              {"ingredientName": "Sugar", "amountRequired": 30},
              {"ingredientName": "Butter", "amountRequired": 20}
            ],
            "availableCustomizations": []
          }
        ]
        """);
    }

    private static void writeSeedInventory() {
        writeFile(INVENTORY_FILE, """
        [
          {"name": "Coffee Beans",   "quantity": 2000, "unit": "g"},
          {"name": "Milk",           "quantity": 5000, "unit": "ml"},
          {"name": "Tea Leaves",     "quantity": 500,  "unit": "g"},
          {"name": "Herbal Blend",   "quantity": 500,  "unit": "g"},
          {"name": "Water",          "quantity": 10000,"unit": "ml"},
          {"name": "Flour",          "quantity": 5000, "unit": "g"},
          {"name": "Butter",         "quantity": 2000, "unit": "g"},
          {"name": "Sugar",          "quantity": 2000, "unit": "g"},
          {"name": "Chocolate Chips","quantity": 1000, "unit": "g"}
        ]
        """);
    }

    private static void writeSeedUsers() {
        writeFile(USERS_FILE, """
        [
          {"username": "barista1", "password": "brew123",  "role": "BARISTA"},
          {"username": "barista2", "password": "brew123",  "role": "BARISTA"},
          {"username": "manager1", "password": "manage456","role": "MANAGER"}
        ]
        """);
    }

    private static void writeSeedOrders() {
        writeFile(ORDERS_FILE, """
        {"pending": [], "fulfilled": []}
        """);
    }

    // ════════════════════════════════════════════════════════════════════════
    // FILE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private static void ensureDataDir() {
        try {
            Files.createDirectories(Path.of(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    private static void writeFile(String path, String content) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(path))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Error writing " + path + ": " + e.getMessage());
        }
    }

    private static String readFile(String path) {
        try {
            if (!Files.exists(Path.of(path))) return null;
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            System.err.println("Error reading " + path + ": " + e.getMessage());
            return null;
        }
    }
}
