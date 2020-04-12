package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.*;

import java.util.List;
import java.util.Map;

/**
 * This class holds the main logic of the Javagram server. Therefore, it orchestrates any operation that a client may
 * request so that it is completed successfully.
 */
public class ServerFacade implements IServer {

    /* ----- Attributes ----- */

    /**
     * It will take care of creating, verifying and deleting the users' sessions.
     */
    private CurrentSessionsManager currentSessionsManager;

    /**
     * It will take care of accessing and modifying the server's database.
     */
    private UsersDAO usersDAO;

    /**
     * It will mask the real server so that it is not directly exposed to the clients.
     */
    private IServer proxy;

    /**
     * Stores, for each currently logged in user, the {@link IServerNotificationsListener} that will attend any
     * notification that the server may need to send to the user's client.
     *
     * Key -> username.
     * Value -> its corresponding {@link IServerNotificationsListener} in the client where the user is logged in.
     */
    private Map<String, IServerNotificationsListener> serverNotificationsListeners;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link ServerFacade} to provide services to Javagram clients. It also takes care of creating the
     * proxy that will be exported as a remote object in a RMI registry.
     */
    public ServerFacade() {
        // TODO constructor also needs DAO parameters
        // TODO instance helpers and proxy
        this.currentSessionsManager = currentSessionsManager;
        this.usersDAO = usersDAO;
    }


    /* ----- Getters & setters ----- */

    /**
     * Retrieves the current {@link #proxy}.
     *
     * @return {@link #proxy}.
     */
    public IServer getProxy() {
        return proxy;
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
