package com.goldardieste.javagram.desktopapp;

import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;

public class SessionModeWindowController extends AbstractController {

    /* ----- Methods ----- */

    /**
     * Sets the identification method as logging in to an already existing Javagram account, and loads the window where
     * an user must type in its Javagram account's data.
     *
     * @param e associated {@link Event}.
     * @throws IOException if the session data window's FXML file cannot be loaded.
     */
    @FXML
    public void chooseLogIn(Event e) throws IOException {
        loadSessionDataWindow(false);
    }

    /**
     * Sets the identification method as signing up a new Javagram account, and loads the window where an user must
     * type in its Javagram account's data.
     *
     * @param e associated {@link Event}.
     * @throws IOException if the session data window's FXML file cannot be loaded.
     */
    @FXML
    public void chooseSignUp(Event e) throws IOException {
        loadSessionDataWindow(true);
    }

    /**
     * Loads the window where an user must type in its Javagram account's data to log in or to sign up.
     *
     * @param signUp if the user is going to create a new account.
     * @throws IOException if the session data window's FXML file cannot be loaded.
     */
    private void loadSessionDataWindow(boolean signUp) throws IOException {

        SessionDataWindowController controller = (SessionDataWindowController) loadNewScene("session_data_window");
        controller.setSignUpMode(signUp);
    }
}
