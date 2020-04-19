package com.goldardieste.javagram.client;

/**
 * When a {@link LocalUserTunnel} receives data from a remote user, it will probably need to be served to a consumer.
 * This consumer must implement this interface, so that, if it is available, every {@link LocalUserTunnel} will send to
 * it all incoming data.
 */
public interface LocalTunnelsListener {

    /* ----- Methods -----*/

    /**
     * The listener receives an incoming message and handles it as it sees fit.
     *
     * @param remoteUser name by which the remote user that sent the message can be identified.
     * @param message the message.
     */
    void forwardIncomingMessage(String remoteUser, String message);
}
