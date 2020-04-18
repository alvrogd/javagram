package com.goldardieste.javagram.desktopapp;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class SessionDataWindowController extends AbstractController {

    /* ----- Attributes ----- */

    /**
     * If the user is going to create a new Javagram account or not.
     */
    private boolean signUpMode;


    /* ----- FXML attributes ----- */

    /**
     * Where the user types in an username.
     */
    @FXML
    private TextField fieldUsername;

    /**
     * Where the user types in a password.
     */
    @FXML
    private PasswordField fieldPassword;


    /* ----- Setters ----- */

    /**
     * Updates the value of {@link #signUpMode}.
     *
     * @param signUpMode new {@link #signUpMode}.
     */
    public void setSignUpMode(boolean signUpMode) {
        this.signUpMode = signUpMode;
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

        String username = this.fieldUsername.getText().trim();
        ObservableList<String> styles = this.fieldUsername.getStyleClass();

        // TODO substitute hardcoded length
        boolean valid = !username.isBlank() && username.length() < 32;

        // The user is warned if the typed-in username is not valid
        if (!valid) {

            styles.remove("sessionDataInputValid");

            if (!styles.contains("sessionDataInputWrong")) {
                styles.add("sessionDataInputWrong");
            }
        }

        // Otherwise, the warning is removed
        else {
            styles.remove("sessionDataInputWrong");

            if (!styles.contains("sessionDataInputValid")) {
                styles.add("sessionDataInputValid");
            }
        }

        return valid;
    }

    /**
     * Checks the currently typed password so that it warns the user if it is empty or exceeds the maximum length.
     *
     * @param e associated {@link Event}.
     * @return if the currently typed-in value is valid.
     */
    @FXML
    public boolean checkPassword(Event e) {

        String password = this.fieldPassword.getText().trim();
        ObservableList<String> styles = this.fieldPassword.getStyleClass();

        // TODO substitute hardcoded length
        boolean valid = !password.isBlank() && password.length() < 256;

        // The user is warned if the typed-in password is not valid
        if (!valid) {

            styles.remove("sessionDataInputValid");

            if (!styles.contains("sessionDataInputWrong")) {
                styles.add("sessionDataInputWrong");
            }
        }

        // Otherwise, the warning is removed
        else {
            styles.remove("sessionDataInputWrong");

            if (!styles.contains("sessionDataInputValid")) {
                styles.add("sessionDataInputValid");
            }
        }

        return valid;
    }

    /**
     * Initiates a session in the Javagram network using the given data by the user.
     *
     * @param e associated {@link Event}.
     */
    public void initiateSession(Event e) throws IOException {

        // Both username and password must be valid to proceed
        String username = this.fieldUsername.getText().trim();
        String password = this.fieldPassword.getText().trim();

        // Both expressions are evaluated even if the first one is false
        if (checkUsername(null) & checkPassword(null)) {

            MainWindowController controller = (MainWindowController) loadNewScene("main_window");

            controller.prueba();
            // TODO
            // The Controller used for the Model-View-Controller architecture that will handle the chat's features is
            // initialized
            //CommunicationHandler communicationHandler = new CommunicationHandler(chatController,
                    //this.fieldAddress.getText(), Integer.parseInt(this.fieldPort.getText()));

            // So that all resources are properly freed when the application ends its execution
            //primaryStage.setOnCloseRequest(e -> communicationHandler.handleClosing());
        }
    }
}
