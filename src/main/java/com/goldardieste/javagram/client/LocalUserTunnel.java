package com.goldardieste.javagram.client;

import com.goldardieste.javagram.common.IRemoteUserTunnel;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class implements a Javagram tunnel that allows one client to communicate with another one directly.
 */
public class LocalUserTunnel extends UnicastRemoteObject implements IRemoteUserTunnel {

    /* ----- Attributes ----- */

    /**
     * Name that identifies the user for which this {@link LocalUserTunnel} has been opened. That is, this object has
     * been instantiated so that the remote user may send messages through it to communicate with the local user.
     */
    private final String remoteUser;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link LocalUserTunnel} that is prepared to be used.
     *
     * @param remoteUser name that identifies the remote user that this {@link LocalUserTunnel} has been opened for.
     * @throws RemoteException the remote object cannot be successfully exported.
     */
    public LocalUserTunnel(String remoteUser) throws RemoteException {
        super();
        this.remoteUser = remoteUser;
    }


    /* ----- Methods ----- */

    /**
     * {@inheritDoc}
     */
    @Override
    public void transmitMessage(String message) {

        System.out.println("The " + this.remoteUser + " user has sent the following message: " + message);
    }
}
