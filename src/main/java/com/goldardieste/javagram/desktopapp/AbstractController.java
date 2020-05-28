package com.goldardieste.javagram.desktopapp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class groups common functionality for all the desktop's app FXML controllers.
 */
public class AbstractController {

    /* ----- Attributes ----- */

    /**
     * Javagram desktop app's window.
     */
    private Stage stage;


    /* ----- Getters & setters ----- */

    /**
     * Retrieves the current {@link #stage}.
     *
     * @return {@link #stage}.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Updates the value of {@link #stage}.
     *
     * @param stage new {@link #stage}.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }


    /* ----- Methods ----- */

    /**
     * Loads into the Javagram window the new specified scene.
     *
     * @param fxmlFile filename where the scene is defined.
     * @return the controller of the new scene.
     * @throws IOException if the FXML file cannot be loaded.
     */
    public AbstractController loadNewScene(String fxmlFile) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        AbstractController controller = loader.getController();

        Scene scene = new Scene(root);
        this.stage.setScene(scene);
        this.stage.show();

        // The next scene's controller may require to modify the current scene
        controller.setStage(this.stage);

        return controller;
    }
}
