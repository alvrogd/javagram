package com.goldardieste.javagram.client;

import com.goldardieste.javagram.client.cryptography.CommunicationDecryptionUtility;
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

    /**
     * If it is not null, all {@link LocalUserTunnel} will forward to it all incoming data.
     */
    private static LocalTunnelsListener localTunnelsListener;

    /**
     * All {@link LocalUserTunnel} will use it to decrypt all incoming data.
     */
    private static CommunicationDecryptionUtility communicationDecryptionUtility;


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

        String decryptedMessage = LocalUserTunnel.communicationDecryptionUtility.decryptString(this.remoteUser,
                message);

        if (LocalUserTunnel.localTunnelsListener != null) {
            LocalUserTunnel.localTunnelsListener.forwardIncomingMessage(this.remoteUser, decryptedMessage);
        }

        System.out.println("The user '" + this.remoteUser + "' has sent the following message: " + decryptedMessage);
    }

    /**
     * Updates the value of {@link #localTunnelsListener}.
     *
     * @param localTunnelsListener new {@link #localTunnelsListener}.
     */
    public static void setLocalTunnelsListener(LocalTunnelsListener localTunnelsListener) {
        LocalUserTunnel.localTunnelsListener = localTunnelsListener;
    }

    /**
     * Updates the value of {@link #communicationDecryptionUtility}.
     *
     * @param utility new {@link #communicationDecryptionUtility}.
     */
    public static void setCommunicationDecryptionUtility(CommunicationDecryptionUtility utility) {
        LocalUserTunnel.communicationDecryptionUtility = utility;
    }
}
