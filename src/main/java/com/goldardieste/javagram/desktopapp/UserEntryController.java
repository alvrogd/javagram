package com.goldardieste.javagram.desktopapp;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javax.naming.Context;

/**
 * This class is needed to generate custom user entries that are shown in the main window's sidebar.
 */
public class UserEntryController {

    /* ----- Attributes ----- */

    /**
     * Name that can identify the user for whom this entry was made.
     */
    private String username;

    /**
     * {@link MainWindowController} that orchestrates events in the window where the entry is being shown.
     */
    private MainWindowController mainWindowController;


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


    /* ----- Getters & setters ----- */

    /**
     * Retrieves the current {@link #username}.
     *
     * @return {@link #username}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Updates the value of {@link #mainWindowController}.
     *
     * @param mainWindowController new {@link #mainWindowController}.
     */
    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
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

        if (this.mainWindowController != null) {
            this.mainWindowController.handleEntryClick(this);
        }
    }

    /**
     * Adds a highlighting effect to the corresponding user entry, if it is not present yet.
     */
    public void addHighlighting() {

        ObservableList<String> styles = this.entryContainer.getStyleClass();

        if (!styles.contains("sidebarItemSelected")) {
            this.entryContainer.getStyleClass().add("sidebarItemSelected");
        }
    }

    /**
     * Removes a highlighting effect from the corresponding user entry if it is present.
     */
    public void removeHighlighting() {
        this.entryContainer.getStyleClass().remove("sidebarItemSelected");
    }

    /**
     * Handles an user's right click in the entry.
     *
     * @param e associated {@link ContextMenuEvent}.
     */
    @FXML
    public void handleRightClick(ContextMenuEvent e) {

        if (this.mainWindowController != null) {
            this.mainWindowController.handleEntryRightClick(this, e);
        }
    }

    /**
     * Shows the given {@link ContextMenu} over the user entry.
     *
     * @param contextMenu the {@link ContextMenu}.
     * @param e           {@link ContextMenuEvent} that triggered the context menu creation.
     */
    public void showContextMenu(ContextMenu contextMenu, ContextMenuEvent e) {

        contextMenu.show(this.entryContainer.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    }
}
