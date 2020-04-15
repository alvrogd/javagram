package com.goldardieste.javagram.client;

import com.goldardieste.javagram.common.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * The main purpose of this class is to act as an intermediary between the server and the {@link ClientFacade}
 * class. Thus, {@link ClientFacade} is prevented from being exported as a remote object, enhancing the security
 * of the client-side because, otherwise, all of its references ({@link UserToken}...) could be exposed by
 * reverse-engineering.
 * <p>
 * Naturally, as the only reference to the client that the server will receive is an instance of this class, it also
 * implements {@link IServerNotificationsListener}.
 */
public class ServerOperationsFacade extends UnicastRemoteObject implements IServerNotificationsListener {

    /* ----- Attributes ----- */

    /**
     * Address where the Javagram server can be located.
     */
    private final String rmiRemoteAddress;

    /**
     * Port where the Javagram server can be located.
     */
    private final int rmiRemotePort;

    /**
     * Name by which the Javagram server can be located.
     */
    private final String javagramServerIdentifier;

    /**
     * Reference to the Javagram server that has been retrieved to communicate to the Javagram network.
     */
    private final IServer javagramServer;

    /**
     * Reference to the {@link ClientFacade} that orchestrates operations in the client. It is needed to perform any
     * related operations to the callbacks made by the server.
     */
    private final ClientFacade clientFacade;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link ServerOperationsFacade} that, using the given parameters, will immediately connect to a
     * Javagram server ({@link IServer}) so that the client is connected to the network.
     *
     * @param rmiRemoteAddress         address where the Javagram server can be located.
     * @param rmiRemotePort            port where the Javagram server can be located.
     * @param javagramServerIdentifier name by which the Javagram server can be located.
     * @param clientFacade             {@link ClientFacade} that orchestrates operations in the client.
     * @throws RemoteException the remote object cannot be successfully exported.
     */
    public ServerOperationsFacade(String rmiRemoteAddress, int rmiRemotePort, String javagramServerIdentifier,
                                  ClientFacade clientFacade) throws RemoteException {
        super();
        this.rmiRemoteAddress = rmiRemoteAddress;
        this.rmiRemotePort = rmiRemotePort;
        this.javagramServerIdentifier = javagramServerIdentifier;
        this.javagramServer = null;
        this.clientFacade = clientFacade;
    }


    /* ----- Methods ----- */

    // TODO implement all methods

    /**
     * Calls {@link IServer#signUp(String, String, IServerNotificationsListener)}.
     */
    public UserToken signUp(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) {
        return null;
    }

    /**
     * Calls {@link IServer#login(String, String, IServerNotificationsListener)}.
     */
    public UserToken login(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) {
        return null;
    }

    /**
     * Calls {@link IServer#disconnect(UserToken)}.
     */
    public void disconnect(UserToken token) {

    }

    /**
     * Calls {@link IServer#retrieveFriends(UserToken)}.
     */
    public List<RemoteUser> retrieveFriends(UserToken token) {
        return null;
    }

    /**
     * Calls {@link IServer#retrieveFriends(UserToken, StatusType)}.
     */
    public List<RemoteUser> retrieveFriends(UserToken token, StatusType status) {
        return null;
    }

    /**
     * Calls {@link IServer#initiateChat(UserToken, IRemoteUserTunnel, String)}.
     */
    public IRemoteUserTunnel initiateChat(UserToken token, IRemoteUserTunnel localTunnel, String remoteUser) {
        return null;
    }

    /**
     * Calls {@link IServer#requestFriendship(UserToken, String)}.
     */
    public void requestFriendship(UserToken token, String remoteUser) {

    }

    /**
     * Calls {@link IServer#acceptFriendship(UserToken, String)}.
     */
    public void acceptFriendship(UserToken token, String remoteUser) {

    }

    /**
     * Calls {@link IServer#rejectFriendship(UserToken, String)}.
     */
    public void rejectFriendship(UserToken token, String remoteUser) {

    }

    /**
     * Calls {@link IServer#endFriendship(UserToken, String)}.
     */
    public void endFriendship(UserToken token, String remoteUser) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRemoteUserTunnel replyChatRequest(String remoteUser, IRemoteUserTunnel remoteUserTunnel) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @param remoteUser
     */
    @Override
    public void updateRemoteUserStatus(RemoteUser remoteUser) {

    }
}
