package com.brewbite;

import com.brewbite.factory.MenuItemFactory;
import com.brewbite.model.AppState;
import com.brewbite.model.menu.Beverage;
import com.brewbite.model.order.Order;
import com.brewbite.model.order.OrderItem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BrewBiteApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // TEMP test order
        Order testOrder = new Order("Test Customer");
        Beverage latte = MenuItemFactory.createLatte();
        testOrder.addItem(new OrderItem(latte, 1, "Medium"));
        AppState.getInstance().getOrderQueue().placeOrder(testOrder);

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/brewbite/ui/BaristaView.fxml")
        );

        Scene scene = new Scene(loader.load(), 500, 400);

        stage.setScene(scene);
        stage.setTitle("Barista Panel");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}