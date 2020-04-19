package com.goldardieste.javagram.desktopapp;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class InputNewFriendController {

    /* ----- Attributes ----- */

    /**
     * {@link MainWindowController} that orchestrates events in the window where the entry is being shown.
     */
    private MainWindowController mainWindowController;


    /* ----- FXML attributes ----- */

    /**
     * Where the user may type in the username of the remote user to whom he wants to send a friendship request.
     */
    @FXML
    private TextField inputNewFriend;


    /* ----- Setters ----- */

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
     * Sends a friendship request to the specified user, as long as the input is valid.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void sendFriendshipRequest(Event e) {

        String username = this.inputNewFriend.getText().trim();

        // TODO remove hardcoded value
        if (!username.isBlank() && username.length() < 32) {
            this.mainWindowController.sendFriendshipRequest(username);
        }
    }
}
