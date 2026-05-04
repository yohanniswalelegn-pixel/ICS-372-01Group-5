import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class CustomerUI extends Application {

    private AppState state;

    private ListView<MenuItem> menuList = new ListView<>();
    private TextArea orderDisplay = new TextArea();
    private Label totalLabel = new Label("Total: $0.00");

    private ComboBox<String> sizeSelector = new ComboBox<>();
    private CheckBox extraShot = new CheckBox("Extra Shot");
    private CheckBox oatMilk = new CheckBox("Oat Milk");
    private CheckBox almondMilk = new CheckBox("Almond Milk");

    private Order currentOrder;

    @Override
    public void start(Stage stage) {

        state = AppState.getInstance();
        state.initialize();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter your name:");
        String name = dialog.showAndWait().orElse("Guest");

        state.startCustomerSession(name);
        currentOrder = new Order(state.getCurrentUser().getUsername());

        List<MenuItem> beverages = state.getMenuCatalog().getByCategory("Beverage");
        List<MenuItem> pastries = state.getMenuCatalog().getByCategory("Pastry");

        menuList.setItems(FXCollections.observableArrayList());
        menuList.getItems().addAll(beverages);
        menuList.getItems().addAll(pastries);

        sizeSelector.getItems().addAll("Small", "Medium", "Large");

        Button addBtn = new Button("Add Item");
        Button placeOrderBtn = new Button("Place Order");
        Button clearBtn = new Button("Clear Order");

        orderDisplay.setEditable(false);

        addBtn.setOnAction(e -> {
            MenuItem selected = menuList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            OrderItem orderItem = new OrderItem(
                    selected,
                    1,
                    sizeSelector.getValue()
            );

            if (extraShot.isSelected())
                orderItem.addChosenCustomization(new Customization("Extra Shot", 0.75));

            if (oatMilk.isSelected())
                orderItem.addChosenCustomization(new Customization("Oat Milk", 0.50));

            if (almondMilk.isSelected())
                orderItem.addChosenCustomization(new Customization("Almond Milk", 0.50));

            currentOrder.addItem(orderItem);
            updateDisplay();
        });

        placeOrderBtn.setOnAction(e -> {
            state.getOrderQueue().placeOrder(currentOrder);

            showAlert("Order placed!");

            currentOrder = new Order(state.getCurrentUser().getUsername());
            updateDisplay();
        });

        clearBtn.setOnAction(e -> {
            currentOrder = new Order(state.getCurrentUser().getUsername());
            updateDisplay();
        });

        VBox left = new VBox(10,
                new Label("Menu"),
                menuList
        );

        VBox right = new VBox(10,
                new Label("Size"), sizeSelector,
                new Label("Customizations"),
                extraShot, oatMilk, almondMilk,
                addBtn,
                new Label("Current Order"),
                orderDisplay,
                totalLabel,
                new HBox(10, placeOrderBtn, clearBtn)
        );

        left.setPrefWidth(200);

        HBox root = new HBox(10, left, right);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 600, 500));
        stage.setTitle("Customer Ordering");
        stage.show();
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();

        for (OrderItem item : currentOrder.getItems()) {
            sb.append(item.getMenuItem().getName())
              .append(" (").append(item.getSize()).append(") - $")
              .append(String.format("%.2f", item.calculatePrice()))
              .append("\n");
        }

        orderDisplay.setText(sb.toString());
        totalLabel.setText("Total: $" +
                String.format("%.2f", currentOrder.calculateTotal()));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}