package com.goldardieste.javagram.common;

import java.rmi.RemoteException;

/**
 * Thrown when an operation made by a Javagram server could not be completed successfully.
 */
public class ServerOperationFailedException extends RemoteException {

    /* ----- Constructor ----- */

    /**
     * Creates a new {@link ServerOperationFailedException} using the specified cause.
     *
     * @param message message that details the cause of the exception.
     */
    public ServerOperationFailedException(String message) {
        super(message);
    }
}
