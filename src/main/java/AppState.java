package com.brewbite.model;

import com.brewbite.model.inventory.InventoryManager;
import com.brewbite.model.menu.MenuCatalog;
import com.brewbite.model.order.OrderQueue;
import com.brewbite.model.user.User;
import com.brewbite.model.user.UserRole;
import com.brewbite.persistence.PersistenceManager;

import java.util.List;

/**
 * Central application state — the single source of truth for all shared
 * model objects in the Brew & Bite system.
 *
 * Implements a simple Singleton so that every controller in the MVC
 * architecture works with the same catalog, inventory, and order queue
 * without passing them around manually.
 *
 * Responsibility: Own and expose the three core model objects
 * (MenuCatalog, InventoryManager, OrderQueue), manage the logged-in
 * user session, and coordinate startup (seed + load) and shutdown (save).
 *
 * This class belongs to the Domain Model layer. It does NOT reference
 * any JavaFX or UI code.
 */
public class AppState {

    // ── Singleton ────────────────────────────────────────────────────────────
    private static AppState instance;

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    private AppState() {}

    // ── Core model objects ────────────────────────────────────────────────────
    private final MenuCatalog      menuCatalog      = new MenuCatalog();
    private final InventoryManager inventoryManager = new InventoryManager();
    private final OrderQueue       orderQueue       = new OrderQueue();

    /** Currently logged-in user (null between sessions). */
    private User currentUser;

    /** All staff accounts loaded from users.json. */
    private List<User> staffAccounts;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Initializes the application:
     * 1. Writes default seed files if missing.
     * 2. Loads all saved state from JSON.
     *
     * Call this once from the JavaFX Application.start() method,
     * before showing any UI.
     */
    public void initialize() {
        PersistenceManager.seedIfMissing();
        PersistenceManager.loadAll(menuCatalog, inventoryManager, orderQueue);
        staffAccounts = PersistenceManager.loadUsers();
    }

    /**
     * Persists all application state to JSON.
     * Call this from the JavaFX Application.stop() method (on window close).
     */
    public void shutdown() {
        PersistenceManager.saveAll(menuCatalog, inventoryManager, orderQueue);
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Attempts to authenticate a staff login.
     *
     * @param username the entered username
     * @param password the entered password
     * @return the matching User if credentials are valid, null otherwise
     */
    public User authenticate(String username, String password) {
        if (staffAccounts == null) return null;
        return staffAccounts.stream()
                .filter(u -> u.getUsername().equals(username) && u.authenticate(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * Starts a customer session (no credentials needed).
     *
     * @param customerName the name the customer entered
     */
    public void startCustomerSession(String customerName) {
        this.currentUser = new User(customerName);
    }

    /**
     * Sets the current user after a successful staff login.
     *
     * @param user the authenticated staff user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /** Clears the current session (returns to the role-selection screen). */
    public void logout() {
        this.currentUser = null;
    }

    // ── Convenience role checks ───────────────────────────────────────────────

    public boolean isLoggedIn()   { return currentUser != null; }
    public boolean isCustomer()   { return isLoggedIn() && currentUser.getRole() == UserRole.CUSTOMER; }
    public boolean isBarista()    { return isLoggedIn() && currentUser.getRole() == UserRole.BARISTA; }
    public boolean isManager()    { return isLoggedIn() && currentUser.getRole() == UserRole.MANAGER; }

    // ── Getters ───────────────────────────────────────────────────────────────

    public MenuCatalog      getMenuCatalog()      { return menuCatalog; }
    public InventoryManager getInventoryManager() { return inventoryManager; }
    public OrderQueue       getOrderQueue()       { return orderQueue; }
    public User             getCurrentUser()      { return currentUser; }
}
