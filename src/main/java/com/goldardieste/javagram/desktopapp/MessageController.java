package com.goldardieste.javagram.desktopapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

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

        this.content.setText(adaptText(content));
    }

    /**
     * Adapts the given text so that it fits in lines of 45 columns at most.
     *
     * @param text the text.
     * @return the adapted text.
     */
    private String adaptText(String text) {

        StringBuilder stringBuilder = new StringBuilder();

        int remaningCols = 45;

        // Words get appended once at a time
        for (String word : text.split(" ")) {

            if (remaningCols - word.length() <= 0) {

                stringBuilder.append('\n');
                remaningCols = 45;
            }

            stringBuilder.append(word).append(' ');
            remaningCols -= word.length() + 1;
        }

        return stringBuilder.toString();
    }
}
