package com.goldardieste.javagram.client;

import com.goldardieste.javagram.common.IRemoteUserTunnel;

/**
 * This exception is thrown when an operation that involves {@link LocalUserTunnel} and/or {@link IRemoteUserTunnel}
 * cannot execute successfully.
 */
public class TunnelOperationException extends Exception {

    /* ----- Constructor ----- */

    /**
     * Creates a new {@link TunnelOperationException} using the specified cause.
     *
     * @param e cause.
     */
    public TunnelOperationException(Exception e) {
        super(e);
    }

    /**
     * Creates a new {@link TunnelOperationException} using the specified message.
     *
     * @param message message.
     */
    public TunnelOperationException(String message) {
        super(message);
    }
}
