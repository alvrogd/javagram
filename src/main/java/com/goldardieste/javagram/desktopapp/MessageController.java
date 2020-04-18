package com.goldardieste.javagram.desktopapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * This class is needed to generate a message, be it outgoing or incoming.
 */
public class MessageController {

    /* ----- FXML attributes ----- */

    /**
     * The message's content.
     */
    @FXML
    private Label content;

    /**
     * The message's reception/sent time.
     */
    @FXML
    private Label time;


    /* ----- Methods ----- */

    /**
     * Updates the view's contents using the given data. Sets the current time in the corresponding field.
     *
     * @param content the new message's contents.
     */
    public void updateContents(String content) {

        this.content.setText(content);
    }
}
