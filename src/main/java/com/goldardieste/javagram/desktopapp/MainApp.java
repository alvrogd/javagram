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

        // TODO these paths will break when moving to another environment
        // SSL configuration
        System.setProperty("javax.net.ssl.keyStore", "./build/resources/main/com/goldardieste/javagram/client/javagram_client_keystore.ks");
        System.setProperty("javax.net.ssl.keyStorePassword", "javagram");
        System.setProperty("javax.net.ssl.trustStore", "./build/resources/main/com/goldardieste/javagram/client/javagram_truststore.ks");
        System.setProperty("javax.net.ssl.trustStorePassword", "javagram");

        // Security manager
        System.setProperty("java.security.debug", "access,failure,policy");
        System.setProperty("java.security.policy", "file:./build/resources/main/com/goldardieste/javagram/client/java.policy");
        System.setSecurityManager(new SecurityManager());

        // Desktop app
        launch(args);
    }
}