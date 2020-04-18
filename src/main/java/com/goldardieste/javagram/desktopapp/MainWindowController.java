package com.goldardieste.javagram.desktopapp;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainWindowController extends AbstractController {

    /* ----- FXML Attributes ----- */

    /**
     * Where the leftmost icons reside.
     */
    @FXML
    private VBox sidebarMenu;

    /**
     * Where each possible user entry resides.
     */
    @FXML
    private VBox sidebarItems;

    /**
     * Where the user may type in a message to send it.
     */
    @FXML
    private TextArea textAreaMessage;

    /**
     * Where all messages for a certain chat are contained.
     */
    @FXML
    private VBox messagesContainer;


    /* ----- Constructor ----- */

    /* ----- Methods ----- */

    /**
     * Makes any possibly needed post-processing of the main window's contents.
     */
    @FXML
    private void initialize() {

    }

    /**
     * If the user has typed in any text, sends a message to the selected user.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void sendMessage(Event e) {
        System.out.println("sending " + this.textAreaMessage.getText());
        this.textAreaMessage.clear();
    }

    public void prueba() {

        for (int i = 0; i < 40; i++) {

            FXMLLoader loader;

            loader = new FXMLLoader(getClass().getResource("fxml/sidebar_item.fxml"));

            try {
                // The message is appended to the currently shown messages
                this.sidebarItems.getChildren().add(loader.load());

                // And its contents are set to the received data
                UserEntryController controller = loader.getController();
                controller.updateContents("user", "patat", "patata");


            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        for (int i = 0; i < 40; i++) {

            FXMLLoader loader;

            loader = new FXMLLoader(getClass().getResource("fxml/message_outgoing.fxml"));

            try {
                // The message is appended to the currently shown messages
                this.messagesContainer.getChildren().add(loader.load());

                // And its contents are set to the received data
                MessageController controller = loader.getController();
                controller.updateContents("asdasdasdasdsd");


            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}
