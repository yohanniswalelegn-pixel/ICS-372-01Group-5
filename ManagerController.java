package com.brewbite.controller;

import com.brewbite.model.AppState;
import com.brewbite.model.inventory.InventoryManager;
import com.brewbite.model.menu.MenuCatalog;
import com.brewbite.model.order.OrderQueue;
import com.brewbite.observer.BrewObserver;
import javafx.fxml.FXML;

public class ManagerController implements BrewObserver {
    @FXML private TableView<Ingredient> inventoryTable;
    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableView<Order> salesHistoryTable;

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

    private void refreshInventoryTable() {
        // 1. Get a snapshot of all ingredients from the inventory manager
        List<Ingredient> ingredients = AppState.getInstance().getInventoryManager().getAllIngredients();

        // 2. Update the UI thread
        Platform.runLater(() -> {
            // Set the new items into the table
            inventoryTable.getItems().setAll(ingredients);
            inventoryTable.refresh(); // Forces a visual refresh of the cells
        });
    }

    private void refreshMenuTable() {
        // 1. Get the full list of items from the catalog
        List<MenuItem> items = AppState.getInstance().getMenuCatalog().getAllItems();

        // 2. Update the UI thread
        Platform.runLater(() -> {
            menuTable.getItems().setAll(items);
            menuTable.refresh();
        });
    }

    private void updateSalesHistory() {
        // 1. Retrieve the history of fulfilled orders
        List<Order> history = AppState.getInstance().getOrderQueue().getFulfilledOrders();

        // 2. Update the UI thread
        Platform.runLater(() -> {
            // Assuming salesHistoryTable is a TableView or ListView
            salesHistoryTable.getItems().setAll(history);
            salesHistoryTable.refresh();
        });
    }
    @FXML
    public void handleRestock(){
        
    }

    @FXML
    public void handleAddMenuItem(){

    }

    @FXML
    public void handleSalesHistory(){

    }

    @FXML
    public void handleEditMenuItem(){

    }

    @FXML
    public void handleRemoveMenuItem(){

    }

    @FXML
    public void handleLogout(){

    }
}