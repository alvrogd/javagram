package com.goldardieste.javagram.desktopapp;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * This class represents the FXML controller for the input region to send a new friendship request.
 */
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

    /**
     * Used to show the user a warning about a failed request operation.
     */
    @FXML
    private Label labelWarning;


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
     * Checks the currently typed username so that it warns the user if it is empty or exceeds the maximum length.
     *
     * @param e associated {@link Event}.
     * @return if the currently typed-in value is valid.
     */
    @FXML
    public boolean checkUsername(Event e) {

        String username = this.inputNewFriend.getText().trim();
        ObservableList<String> styles = this.inputNewFriend.getStyleClass();

        // TODO substitute hardcoded length
        boolean valid = !username.isBlank() && username.length() < 32;

        // The user is warned if the typed-in username is not valid
        if (!valid) {

            if (!styles.contains("defaultInputError")) {
                styles.add("defaultInputError");
            }
        }

        // Otherwise, the warning is removed
        else {
            styles.remove("defaultInputError");
        }

        return valid;
    }

    /**
     * Sends a friendship request to the specified user, as long as the input is valid.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void sendFriendshipRequest(Event e) {

        // Any previous warning is hidden while the request is handled
        this.labelWarning.setVisible(false);

        String username = this.inputNewFriend.getText().trim();

        if (checkUsername(null)) {

            boolean successful = this.mainWindowController.sendFriendshipRequest(username);

            // Shows a warning saying that the request could not be sent
            if (!successful) {
                this.labelWarning.setText("Could not send the request. ¿Is the username\ncorrect?");
                this.labelWarning.setVisible(true);
            }
        }
    }
}
