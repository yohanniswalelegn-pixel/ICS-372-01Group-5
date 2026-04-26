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

import java.util.List;

public class BaristaController implements BrewObserver {

    private AppState state = AppState.getInstance();

    @FXML
    private ListView<Order> orderListView;

    @FXML
    private Button advanceButton;

    @FXML
    private Button completeButton;

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

                    text.append("Total: $")
                        .append(String.format("%.2f", order.getTotal()));

                    setText(text.toString());
                }
            }
        });
    }

    // register observer and load first set of orders
    public void init() {
        state.getOrderQueue().addObserver(this);
        refreshOrders();
    }

    // get pending orders in FIFO order and show them in the UI
    public void refreshOrders() {
        List<Order> pending = state.getOrderQueue().getPendingOrders();
        orderListView.getItems().setAll(pending);
    }

    // advance selected order status
    public void handleAdvance(Order selectedOrder) {
        if (selectedOrder != null) {
            state.getOrderQueue().advanceOrderStatus(selectedOrder);
        }
    }

    // complete order only if ready for pickup
    public void handleCompleteOrder(Order selectedOrder) {
        if (selectedOrder == null) {
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

    // observer callback for live updates
    @Override
    public void onUpdate(String eventType, Object data) {
        if (eventType.equals(OrderQueue.EVENT_ORDER_PLACED)) {
            refreshOrders();
        }

        if (eventType.equals(OrderQueue.EVENT_STATUS_CHANGED)) {
            refreshOrders();
        }

        if (eventType.equals(OrderQueue.EVENT_ORDER_FULFILLED)) {
            refreshOrders();
        }
    }




    



}

