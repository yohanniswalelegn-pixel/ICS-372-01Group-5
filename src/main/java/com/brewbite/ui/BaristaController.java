package com.brewbite.ui;

import com.brewbite.model.AppState;
import com.brewbite.model.order.Order;
import com.brewbite.model.order.OrderQueue;
import com.brewbite.model.order.OrderStatus;
import com.brewbite.observer.BrewObserver;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import java.util.Optional;

import java.util.List;

public class BaristaController implements BrewObserver {

    private AppState state = AppState.getInstance();
    private boolean loggedIn = false;

    @FXML
    private ListView<Order> orderListView;

    @FXML
    private Button advanceButton;

    @FXML
    private Button completeButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label loginStatusLabel;

    @FXML
    public void initialize() {
        init();

        orderListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);

                if (empty || order == null) {
                    setText(null);
                } else {
                    StringBuilder text = new StringBuilder();

                    text.append(order.getDisplayHeader()).append("\n");

                    for (var item : order.getItems()) {
                        text.append(" - ")
                            .append(item.getDisplaySummary())
                            .append("\n");
                    }

                    text.append("Status: ")
                        .append(order.getStatus())
                        .append("\n");

                    text.append("Total: $")
                        .append(String.format("%.2f", order.getTotal()));

                    setText(text.toString());
                }
            }
        });

        updateLoginState();
    }

    public void init() {
        state.getOrderQueue().addObserver(this);
    }

    private void updateLoginState() {
        orderListView.setDisable(!loggedIn);
        advanceButton.setDisable(!loggedIn);
        completeButton.setDisable(!loggedIn);
        logoutButton.setDisable(!loggedIn);
        loginButton.setDisable(loggedIn);

        if (loggedIn) {
            loginStatusLabel.setText("Logged in as Barista");
            refreshOrders();
        } else {
            loginStatusLabel.setText("Logged out");
            orderListView.getItems().clear();
        }
    }

    @FXML
    private void onLoginClicked() {
    final String correctUsername = "barista";
    final String correctPassword = "coffee123";

    while (!loggedIn) {
        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Barista Login");
        usernameDialog.setHeaderText("Enter username");
        usernameDialog.setContentText("Username:");

        Optional<String> usernameResult = usernameDialog.showAndWait();

        if (usernameResult.isEmpty()) {
            return;
        }

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Barista Login");
        passwordDialog.setHeaderText("Enter password");
        passwordDialog.setContentText("Password:");

        Optional<String> passwordResult = passwordDialog.showAndWait();

        if (passwordResult.isEmpty()) {
            return;
        }

        String username = usernameResult.get();
        String password = passwordResult.get();

        if (username.equals(correctUsername) && password.equals(correctPassword)) {
            loggedIn = true;
            updateLoginState();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText("Incorrect username or password");
            alert.setContentText("Please try again.");
            alert.showAndWait();
            }
         }
    }

    @FXML
    private void onLogoutClicked() {
        loggedIn = false;
        updateLoginState();
    }

    public void refreshOrders() {
        if (!loggedIn) {
            return;
        }

        List<Order> pending = state.getOrderQueue().getPendingOrders();
        orderListView.getItems().setAll(pending);
    }

    public void handleAdvance(Order selectedOrder) {
        if (loggedIn && selectedOrder != null) {
            state.getOrderQueue().advanceOrderStatus(selectedOrder);
        }
    }

    public void handleCompleteOrder(Order selectedOrder) {
        if (!loggedIn || selectedOrder == null) {
            return;
        }

        if (selectedOrder.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            state.getOrderQueue().advanceOrderStatus(selectedOrder);
        } else {
            System.out.println("Order must be READY_FOR_PICKUP before completing.");
        }
    }

    @FXML
    private void onAdvanceClicked() {
        Order selectedOrder = orderListView.getSelectionModel().getSelectedItem();
        handleAdvance(selectedOrder);
    }

    @FXML
    private void onCompleteClicked() {
        Order selectedOrder = orderListView.getSelectionModel().getSelectedItem();
        handleCompleteOrder(selectedOrder);
    }

    @Override
    public void onUpdate(String eventType, Object data) {
        if (!loggedIn) {
            return;
        }

        if (eventType.equals(OrderQueue.EVENT_ORDER_PLACED)
                || eventType.equals(OrderQueue.EVENT_STATUS_CHANGED)
                || eventType.equals(OrderQueue.EVENT_ORDER_FULFILLED)) {
            refreshOrders();
        }
    }
}