package com.brewbite.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FxmlTestLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML without a controller to test the layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ManagerView.fxml"));
        // Disable controller loading for this test
        loader.setControllerFactory(param -> null);
        Scene scene = new Scene(loader.load());
        
        stage.setTitle("Manager View - FXML Test");
        stage.setScene(scene);
        stage.setWidth(850);
        stage.setHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}