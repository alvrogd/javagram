package com.goldardieste.javagram.desktopapp.unidentifieduser;

import com.goldardieste.javagram.client.exposed.ClientFacade;
import com.goldardieste.javagram.client.exposed.ClientOperationFailedException;
import com.goldardieste.javagram.common.ConfigurationParameters;
import com.goldardieste.javagram.desktopapp.AbstractController;
import com.goldardieste.javagram.desktopapp.identifieduser.MainWindowController;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * This class represents the FXML controller for the window where the user types in an username and password to log in
 * or to sign up.
 */
public class SessionDataWindowController extends AbstractController {

    /* ----- Attributes ----- */

    /**
     * If the user is going to create a new Javagram account or not.
     */
    private boolean signUpMode;

    /**
     * Provides all the back-end functionality to the desktop app.
     */
    private ClientFacade clientFacade;


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

    /**
     * Used to show the user warnings about failed log in/sign up attempts.
     */
    @FXML
    private Label labelWarning;


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
     * Switches back to the window where the user chooses to sign up or to log in.
     *
     * @param e associated {@link Event}.
     * @throws IOException if the session mode window's FXML file cannot be loaded.
     */
    @FXML
    public void goSessionModeWindow(Event e) throws IOException {
        loadNewScene("../fxml/session_mode_window.fxml");
    }

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

        // The previous warning is hidden while the request is handled
        this.labelWarning.setVisible(false);

        // Both username and password must be valid to proceed
        String username = this.fieldUsername.getText().trim();
        String password = this.fieldPassword.getText().trim();

        // Both expressions are evaluated even if the first one is false
        if (checkUsername(null) & checkPassword(null)) {

            // The Javagram client back-end is set up if it is not yet
            prepareClientBackEnd();

            try {
                // If the user is going to sign up
                if (this.signUpMode) {
                    clientFacade.signUp(username, password);

                } else {
                    clientFacade.login(username, password);
                }

                // If everything goes well, the main window is loaded
                MainWindowController controller = (MainWindowController) loadNewScene("../fxml/main_window.fxml");
                controller.setClientFacade(this.clientFacade);

                // So that all resources are properly freed when the application ends its execution
                getStage().setOnCloseRequest(event -> controller.handleClosing());

            } catch (ClientOperationFailedException exception) {
                exception.printStackTrace();

                // If the user was going to sign up
                if (this.signUpMode) {
                    this.labelWarning.setText("Could not sign up. The Javagram server could not be\nreachable, or " +
                            "the username may already be taken.");
                }
                else {
                    this.labelWarning.setText("Could not log in. The Javagram server could not be \nreachable, or " +
                            "the credentials could be wrong.");
                }

                this.labelWarning.setVisible(true);
            }
        }
    }

    /**
     * Creates an instance of {@link ClientFacade} that will provide all the required functionality to the desktop app.
     */
    private void prepareClientBackEnd() {

        if (this.clientFacade == null) {
            this.clientFacade = new ClientFacade(ConfigurationParameters.RMI_ADDRESS,
                    ConfigurationParameters.RMI_PORT, ConfigurationParameters.RMI_IDENTIFIER);
        }
    }

    /**
     * Performs any tasks that are required to successfully stop the execution of the desktop app.
     */
    public void handleClosing() {

        if(this.clientFacade != null) {
            this.clientFacade.haltExecution();
        }
    }
}
