package com.goldardieste.javagram.desktopapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class initializes the Javagram desktop app.
 */
public class MainApp extends Application {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/session_mode_window.fxml"));
        Parent root = loader.load();
        AbstractController controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setTitle("Javagram");
        stage.setScene(scene);
        stage.show();

        controller.setStage(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}