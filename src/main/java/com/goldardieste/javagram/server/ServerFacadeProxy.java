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
    private final ServerFacade maskedServer;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link ServerFacadeProxy} that will act as an intermediary between any client and the real server.
     *
     * @param serverFacade Javagram server which is being masked.
     * @throws RemoteException the remote object cannot be successfully exported.
     */
    public ServerFacadeProxy(ServerFacade serverFacade) throws RemoteException {
        super();
        this.maskedServer = serverFacade;
    }


    /* ----- Methods ----- */

    /**
     * {@inheritDoc}
     */
    @Override
    public UserToken signUp(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) throws ServerOperationFailedException {
        return this.maskedServer.signUp(username, passwordHash, serverNotificationsListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserToken login(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) throws ServerOperationFailedException {
        return this.maskedServer.login(username, passwordHash, serverNotificationsListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect(UserToken token) throws ServerOperationFailedException {
        this.maskedServer.disconnect(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteUser> retrieveFriends(UserToken token) throws ServerOperationFailedException {
        return this.maskedServer.retrieveFriends(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteUser> retrieveFriends(UserToken token, StatusType status) throws ServerOperationFailedException {
        return this.maskedServer.retrieveFriends(token, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRemoteUserTunnel initiateChat(UserToken token, IRemoteUserTunnel localTunnel, String remoteUser) throws
            ServerOperationFailedException {
        return this.maskedServer.initiateChat(token, localTunnel, remoteUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {
        this.maskedServer.requestFriendship(token, remoteUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {
        return this.maskedServer.acceptFriendship(token, remoteUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {
        this.maskedServer.rejectFriendship(token, remoteUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {
        this.maskedServer.endFriendship(token, remoteUser);
    }
}
