package com.goldardieste.javagram.desktopapp;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * This class is needed to generate custom user entries that are shown in the main window's sidebar.
 */
public class UserEntryController {

    /* ----- Attributes ----- */

    /**
     * Name that can identify the user for whom this entry was made.
     */
    private String username;


    /* ----- FXML attributes ----- */

    /**
     * Root container of the entry's elements.
     */
    @FXML
    private HBox entryContainer;

    /**
     * Text shown at the top-left.
     */
    @FXML
    private Label header;

    /**
     * Text shown at the top-right.
     */
    @FXML
    private Label time;

    /**
     * Text shown at the bottom-left.
     */
    @FXML
    private Label description;


    /* ----- Constructor ----- */

    /**
     * Generates a default {@link UserEntryController} to represent an user entry.
     */
    public UserEntryController() {
    }


    /* ----- Methods ----- */

    /**
     * Updates the view's contents using the given data. Sets the current time in the corresponding field.
     *
     * @param username    name by which the user can be identified.
     * @param header      the new header's contents.
     * @param description the new description's contents.
     */
    public void updateContents(String username, String header, String description) {

        this.username = username;
        this.header.setText(header);
        this.description.setText(description);
    }

    /**
     * Handles an user's click in the entry.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void handleClick(Event e) {
        System.out.println("CLick en " + this.username);
        this.entryContainer.getStyleClass().add("sidebarItemSelected");
    }
}
