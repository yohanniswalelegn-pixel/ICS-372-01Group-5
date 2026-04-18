package com.brewbite.model;

import com.brewbite.model.inventory.InventoryManager;
import com.brewbite.model.menu.MenuCatalog;
import com.brewbite.model.order.OrderQueue;
import com.brewbite.model.user.User;
import com.brewbite.model.user.UserRole;
import com.brewbite.persistence.PersistenceManager;

import java.util.List;

public class AppState {
    private static AppState instance;
    private User currentUser;
    private MenuCatalog menuCatalog;
    private OrderQueue orderQueue;
    private InventoryManager inventoryManager;
    private PersistenceManager persistenceManager;

    private AppState() {
        this.menuCatalog = new MenuCatalog();
        this.orderQueue = new OrderQueue();
        this.inventoryManager = new InventoryManager();
        this.persistenceManager = new PersistenceManager();
    }

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public MenuCatalog getMenuCatalog() {
        return menuCatalog;
    }

    public OrderQueue getOrderQueue() {
        return orderQueue;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
}