package com.goldardieste.javagram.common.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface contains all the operations that a Javagram tunnel must support to allow one client to communicate
 * with another one directly. That is, the communication is unidirectional.
 */
public interface IRemoteUserTunnel extends Remote {

    /* ----- Methods ----- */

    /**
     * Sends, from one end of the tunnel to the other, the given message.
     *
     * @param message content of the message that will be sent.
     * @throws RemoteException error during a remote procedure call.
     */
    void transmitMessage(String message) throws RemoteException;
}
