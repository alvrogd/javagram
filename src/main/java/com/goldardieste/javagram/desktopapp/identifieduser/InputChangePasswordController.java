package com.goldardieste.javagram.desktopapp.identifieduser;

import com.goldardieste.javagram.desktopapp.identifieduser.MainWindowController;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class InputChangePasswordController {

    /* ----- Attributes ----- */

    /**
     * {@link MainWindowController} that orchestrates events in the window where the entry is being shown.
     */
    private MainWindowController mainWindowController;


    /* ----- FXML attributes ----- */

    /**
     * Where the user may type in his current password.
     */
    @FXML
    private PasswordField inputCurrentPassword;

    /**
     * Where the user may type in his new password.
     */
    @FXML
    private PasswordField inputNewPassword;

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
     * Checks the currently typed current password so that it warns the user if it is empty or exceeds the maximum
     * length.
     *
     * @param e associated {@link Event}.
     * @return if the currently typed-in value is valid.
     */
    @FXML
    public boolean checkCurrentPassword(Event e) {

        return checkPasswordField(this.inputCurrentPassword);
    }

    /**
     * Checks the currently typed new password so that it warns the user if it is empty or exceeds the maximum length.
     *
     * @param e associated {@link Event}.
     * @return if the currently typed-in value is valid.
     */
    @FXML
    public boolean checkNewPassword(Event e) {

        return checkPasswordField(this.inputNewPassword);
    }

    /**
     * Checks the specified password field so to that it warns the user if it is empty or exceeds the maximum length.
     *
     * @param field {@link PasswordField} that will be checked.
     * @return if the currently typed-in value is valid.
     */
    private boolean checkPasswordField(PasswordField field) {

        String password = field.getText().trim();
        ObservableList<String> styles = field.getStyleClass();

        // TODO substitute hardcoded length
        boolean valid = !password.isBlank() && password.length() < 256;

        // The user is warned if the typed-in password is not valid
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
     * Updates the user's current password, as long as the typed-in values are valid.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void updatePassword(Event e) {

        // Any previous warning is hidden while the request is handled
        this.labelWarning.setVisible(false);

        String currentPassword = this.inputCurrentPassword.getText().trim();
        String newPassword = this.inputNewPassword.getText().trim();

        if (checkNewPassword(null) & checkCurrentPassword(null)) {

            boolean successful = this.mainWindowController.updatePassword(currentPassword, newPassword);

            // Shows a warning saying that the update could not be performed
            if (!successful) {
                this.labelWarning.setText("Could not update your password. ¿Is your\ncurrent password correct?");
                this.labelWarning.setVisible(true);
            }
        }
    }
}
