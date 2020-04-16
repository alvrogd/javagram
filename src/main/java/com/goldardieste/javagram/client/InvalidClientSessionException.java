package com.goldardieste.javagram.client;

/**
 * This exception is thrown when an operation that is requested to the client cannot be performed due to the current
 * user session being in an incompatible state. For example, it may be thrown when the client tries to retrieve a list
 * of friends before logging in, or when the clients tries to log in when he has already initiated a session with the
 * server.
 */
public class InvalidClientSessionException extends ClientOperationFailedException {

    /* ----- Constructor ----- */

    /**
     * Creates a new {@link InvalidClientSessionException} using the specified cause.
     *
     * @param message message that details the cause of the exception.
     */
    public InvalidClientSessionException(String message) {
        super(message);
    }
}
