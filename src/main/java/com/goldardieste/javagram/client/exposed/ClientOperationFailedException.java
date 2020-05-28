package com.goldardieste.javagram.client.exposed;

/**
 * This exception is thrown when the back-end of the client-side cannot successfully perform an operation.
 */
public class ClientOperationFailedException extends Exception {

    /* ----- Constructor ----- */

    /**
     * Creates a new {@link ClientOperationFailedException} using the specified cause.
     *
     * @param message message that details the cause of the exception.
     */
    public ClientOperationFailedException(String message) {
        super(message);
    }
}
