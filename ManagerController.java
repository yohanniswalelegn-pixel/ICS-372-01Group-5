package com.brewbite.controller;

import com.brewbite.model.AppState;
import com.brewbite.model.inventory.InventoryManager;
import com.brewbite.model.menu.MenuCatalog;
import com.brewbite.model.order.OrderQueue;
import com.brewbite.observer.BrewObserver;
import javafx.fxml.FXML;

public class ManagerController implements BrewObserver {

    //done to get the instance of the system
    @FXML
    public void initialize() {
        // Register this controller to listen for changes in all three subsystems
        AppState.getInstance().getInventoryManager().addObserver(this);
        AppState.getInstance().getMenuCatalog().addObserver(this);
        AppState.getInstance().getOrderQueue().addObserver(this);

        // Initial load of data into UI components
        refreshInventoryTable();
        refreshMenuTable();
    }

    @Override
    public void onUpdate(String eventType, Object data) {
        // Use the event constants to decide which part of the UI to refresh
        switch (eventType) {
            case InventoryManager.EVENT_INVENTORY_UPDATED:
            case InventoryManager.EVENT_ITEM_LOW_STOCK:
                refreshInventoryTable(); // Update the stock list visually
                break;

            case MenuCatalog.EVENT_ITEM_ADDED:
            case MenuCatalog.EVENT_ITEM_UPDATED:
            case MenuCatalog.EVENT_ITEM_REMOVED:
                refreshMenuTable(); // Update the product list visually
                break;

            case OrderQueue.EVENT_ORDER_FULFILLED:
                updateSalesHistory(); // Update the history log
                break;
        }
}
}