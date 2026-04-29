package com.brewbite.controller;

import com.brewbite.model.AppState;
import com.brewbite.model.inventory.Ingredient;
import com.brewbite.model.inventory.InventoryManager;
import com.brewbite.model.menu.MenuCatalog;
import com.brewbite.model.menu.MenuItem;
import com.brewbite.model.order.Order;
import com.brewbite.model.order.OrderQueue;
import com.brewbite.observer.BrewObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for the Manager UI.
 * Responsibilities: Manage menu items, restock inventory, view sales history,
 * and react to real-time system changes via the Observer pattern.
 */
public class ManagerController implements BrewObserver {

    // --- FXML UI Components ---
    @FXML private TableView<Ingredient> inventoryTable;
    @FXML private TableColumn<Ingredient, String> ingNameCol;
    @FXML private TableColumn<Ingredient, Double> ingQtyCol;
    @FXML private TableColumn<Ingredient, String> ingUnitCol;

    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> itemNameCol;
    @FXML private TableColumn<MenuItem, Double> itemPriceCol;
    @FXML private TableColumn<MenuItem, String> itemCategoryCol;
    @FXML private TableColumn<MenuItem, Boolean> itemAvailableCol;

    @FXML private TableView<Order> salesHistoryTable;
    @FXML private TableColumn<Order, String> orderIdCol;
    @FXML private TableColumn<Order, String> customerCol;
    @FXML private TableColumn<Order, Double> totalCol;

    @FXML private TextField restockAmountField;
    @FXML private Label statusMessageLabel;

    @FXML private TextField basePriceField;
    @FXML private CheckBox availableCheckBox;

    //  Domain Models 
    private final MenuCatalog menuCatalog = AppState.getInstance().getMenuCatalog();
    private final InventoryManager inventoryManager = AppState.getInstance().getInventoryManager();
    private final OrderQueue orderQueue = AppState.getInstance().getOrderQueue();

    /**
     * Initializes the controller, sets up data binding, and registers observers.
     */
    @FXML
    public void initialize() {
        setupTableBindings();
        loadInitialData();

        // Register as an observer for real-time updates 
        menuCatalog.addObserver(this);
        inventoryManager.addObserver(this);
        orderQueue.addObserver(this);
    }

    private void setupTableBindings() {
        // Inventory Table
        ingNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        ingQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        ingUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        // Menu Table
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        itemPriceCol.setCellValueFactory(new PropertyValueFactory<>("basePrice"));
        itemCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        itemAvailableCol.setCellValueFactory(new PropertyValueFactory<>("available"));

        // Sales History Table
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void loadInitialData() {
        inventoryTable.setItems(FXCollections.observableArrayList(inventoryManager.getAllIngredients()));
        menuTable.setItems(FXCollections.observableArrayList(menuCatalog.getAllItems()));
        salesHistoryTable.setItems(FXCollections.observableArrayList(orderQueue.getFulfilledOrders()));
    }


    /**
     * Reacts to notifications from the Model layer 
     */
    @Override
    public void onUpdate(String eventType, Object data) {
        // UI updates must happen on the JavaFX Application Thread
        Platform.runLater(() -> {
            switch (eventType) {
                case InventoryManager.EVENT_INVENTORY_UPDATED -> refreshInventory();
                case InventoryManager.EVENT_ITEM_LOW_STOCK -> handleLowStockAlert((Ingredient) data);
                case MenuCatalog.EVENT_ITEM_ADDED, MenuCatalog.EVENT_ITEM_REMOVED, MenuCatalog.EVENT_ITEM_UPDATED -> refreshMenu();
                case OrderQueue.EVENT_ORDER_FULFILLED -> refreshSalesHistory();
            }
        });
    }

    //  Manager Action Methods 

    /**
     * Adds quantity to a selected ingredient 
     */
    @FXML
    private void handleRestock() {
        Ingredient selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Select an ingredient to restock.");
            return;
        }

        try {
            double amount = Double.parseDouble(restockAmountField.getText());
            inventoryManager.restock(selected.getName(), amount);
            restockAmountField.clear();
            updateStatus("Restocked " + selected.getName());
        } catch (NumberFormatException e) {
            updateStatus("Invalid amount entered.");
        }
    }

    /**
     * Removes the selected item from the catalog 
     */
    @FXML
    private void handleRemoveMenuItem() {
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            menuCatalog.removeItem(selected.getId());
            updateStatus("Removed: " + selected.getName());
        }
    }

    /**
     * Toggles whether an item is currently available for customers 
     */
    @FXML
    private void handleToggleAvailability() {
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setAvailable(!selected.isAvailable());
            menuCatalog.updateItem(selected);
            updateStatus(selected.getName() + " availability updated.");
        }
    }
    
    @FXML
    private void handleAddMenuItem() {
        String name = newItemNameField.getText();
        if (name == null || name.isEmpty()) {
            updateStatus("Error: Enter a name for the new item.");
            return;
        }

        MenuItem newItem = MenuItemFactory.createByName(name);
        if (newItem != null) {
            menuCatalog.addItem(newItem); 
            updateStatus("Added new item: " + name);
            newItemNameField.clear();
        } else {
            updateStatus("Error: Item type not recognized by Factory.");
        }
    }

    @FXML
    private void handleEditMenuItem() {
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            updateStatus("Error: Select a menu item to edit.");
            return;
        }

        try {
            double newPrice = Double.parseDouble(basePriceField.getText());
            
            // Update the object properties
            selected.setBasePrice(newPrice);
            selected.setAvailable(availableCheckBox.isSelected());

            /* * Push update to Model. Because Version 1 implements BrewObserver, 
            * calling updateItem() will trigger the onUpdate() method and 
            * refresh the table automatically.
            */
            menuCatalog.updateItem(selected);
            updateStatus("Updated: " + selected.getName());
            
        } catch (NumberFormatException e) {
            updateStatus("Error: Please enter a valid price.");
        }
    }
    
    @FXML
    private void handleLogout() {
        AppState.getInstance().logout();

        try {
            //Load the Login view
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/brewbite/view/LoginView.fxml")
            );
            javafx.scene.Parent root = loader.load();

            // Get the current stage and switch the scene
            javafx.stage.Stage stage = (javafx.stage.Stage) statusMessageLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("BrewBite - Login");
            stage.show();

        } catch (java.io.IOException e) {
            updateStatus("Error: Could not load the login screen.");
            e.printStackTrace(); 
        }
    }

    // Helper Methods

    private void refreshInventory() {
        inventoryTable.setItems(FXCollections.observableArrayList(inventoryManager.getAllIngredients()));
    }

    private void refreshMenu() {
        menuTable.setItems(FXCollections.observableArrayList(menuCatalog.getAllItems()));
    }

    private void refreshSalesHistory() {
        salesHistoryTable.setItems(FXCollections.observableArrayList(orderQueue.getFulfilledOrders()));
    }

    private void handleLowStockAlert(Ingredient ing) {
        updateStatus("ALERT: " + ing.getName() + " is low (" + ing.getQuantity() + ing.getUnit() + ")!");
    }

    private void updateStatus(String message) {
        statusMessageLabel.setText(message);
    }
}