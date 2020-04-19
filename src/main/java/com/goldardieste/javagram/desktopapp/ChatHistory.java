package com.goldardieste.javagram.desktopapp;

import java.util.ArrayList;
import java.util.List;

/**
 * This class acts as a wrapper for a sent or received Javagram message, so that the desktop app may regenerate any
 * chat history on demand. The access to this class' values is not thread-safe by default.
 */
public class ChatHistory {

    /* ----- Attributes ----- */

    /**
     * Contains all the received or sent messages in the chat history in chronological order.
     */
    private final List<String> messages;

    /**
     * Contains one entry for each message in {@link #messages}, in the same order, to determine if the message was
     * sent by the client or if it was received from a remote user.
     */
    private final List<Boolean> outgoing;


    /* ----- Constructor ----- */

    /**
     * Initializes an empty {@link ChatHistory}.
     */
    public ChatHistory() {
        this.messages = new ArrayList<>();
        this.outgoing = new ArrayList<>();

        // TODO remove
        this.messages.add("Hola que tal");
        this.outgoing.add(true);

        this.messages.add("Bien, y tu?");
        this.outgoing.add(false);

        this.messages.add("Genial");
        this.outgoing.add(true);
    }


    /* ----- Getters ----- */

    /**
     * Retrieves the current {@link #messages}.
     *
     * @return {@link #messages}.
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Retrieves the current {@link #outgoing}.
     *
     * @return {@link #outgoing}.
     */
    public List<Boolean> getOutgoing() {
        return outgoing;
    }


    /* ----- Methods ----- */

    /**
     * Stores the given message.
     *
     * @param message  contents of the message
     * @param outgoing if the message was sent by the client.
     */
    public void addMessage(String message, boolean outgoing) {
        this.messages.add(message);
        this.outgoing.add(outgoing);
    }
}
