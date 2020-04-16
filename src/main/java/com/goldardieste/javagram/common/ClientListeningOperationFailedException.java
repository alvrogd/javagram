package com.goldardieste.javagram.common;

import java.rmi.RemoteException;

/**
 * Thrown when an operation made by a Javagram client (due to a Javagram server's request) could not be completed
 * successfully.
 */
public class ClientListeningOperationFailedException extends RemoteException {

    /* ----- Constructor ----- */

    /**
     * Creates a new {@link ClientListeningOperationFailedException} using the specified cause.
     *
     * @param message message that details the cause of the exception.
     */
    public ClientListeningOperationFailedException(String message) {
        super(message);
    }
}
