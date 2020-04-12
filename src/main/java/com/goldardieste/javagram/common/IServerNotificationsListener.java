package com.goldardieste.javagram.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface contains all the operations that a Javagram client must support. All of these operations are supposed
 * to be called just by a Javagram server when it must notify the client about anything.
 */
public interface IServerNotificationsListener extends Remote {

    /* ----- Methods ----- */

    /**
     * The corresponding client is asked to initialize a connection with a remote user, through which the may
     * communicate.
     *
     * @param remoteUser       name by which the remote user that asks to establish the connection can be identified.
     * @param remoteUserTunnel {@link IRemoteUserTunnel} that the remote user has opened so that the client may
     *                         communicate with him.
     * @return if the client has accepted the request, a {@link IRemoteUserTunnel} through which the remote user may
     * send him messages is returned.
     * @throws RemoteException error during a remote procedure call.
     */
    IRemoteUserTunnel replyChatRequest(String remoteUser, IRemoteUserTunnel remoteUserTunnel) throws RemoteException;

    /**
     * 1. If the status of the specified remote user has been already retrieved by the client, it is substituted by the
     * new one. For example, when a remote user accepts a friendship request that the client had sent.
     * <p>
     * 2. If the status of the specified remote user has not already been retrieved, the remote user is added as an
     * user of interest, and its only know status is also stored. For example, when a remote user sends the client a
     * friendship request. A whole {@link RemoteUser} is transmitted so that any other possible information that is
     * also needed in the future is also transmitted.
     *
     * @param remoteUser {@link RemoteUser} that contains the name that identifies the remote user whose current status
     *                   (which also contains), in relation to the client, is being transmitted.
     * @throws RemoteException error during a remote procedure call.
     */
    void updateRemoteUserStatus(RemoteUser remoteUser) throws RemoteException;
}
