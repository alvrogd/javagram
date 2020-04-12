package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * The sole purpose of this class is to act as an intermediary between the clients and the {@link ServerFacade} class.
 * Thus, {@link ServerFacade} is prevented from being exported as a remote object, enhancing the security of the
 * server-side because, otherwise, all of its references (other clients, {@link UsersDAO}...) could be exposed by
 * reverse-engineering.
 */
public class ServerFacadeProxy extends UnicastRemoteObject implements IServer {

    /* ----- Attributes ----- */

    /**
     * Reference to the actual Javagram server.
     */
    private final IServer service;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link ServerFacadeProxy} that will act as an intermediary between any client and the real server.
     *
     * @param service Javagram server which is being masked.
     * @throws RemoteException the remote object could not be successfully exported.
     */
    public ServerFacadeProxy(IServer service) throws RemoteException {
        super();
        this.service = service;
    }


    /* ----- Methods ----- */

    // TODO implement all methods

    /**
     * {@inheritDoc}
     */
    @Override
    public UserToken signUp(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserToken login(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect(UserToken token) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteUser> retrieveFriends(UserToken token) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteUser> retrieveFriends(UserToken token, StatusType status) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRemoteUserTunnel initiateChat(UserToken token, IRemoteUserTunnel localTunnel, String remoteUser) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestFriendship(UserToken token, String remoteUser) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptFriendship(UserToken token, String remoteUser) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectFriendship(UserToken token, String remoteUser) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endFriendship(UserToken token, String remoteUser) {

    }
}
