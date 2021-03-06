package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.*;
import com.goldardieste.javagram.common.datacontainers.NewChatData;
import com.goldardieste.javagram.common.datacontainers.RemoteUser;
import com.goldardieste.javagram.common.datacontainers.UserToken;
import com.goldardieste.javagram.common.exceptions.ServerOperationFailedException;
import com.goldardieste.javagram.common.interfaces.IRemoteUserTunnel;
import com.goldardieste.javagram.common.interfaces.IServer;
import com.goldardieste.javagram.common.interfaces.IServerNotificationsListener;
import com.goldardieste.javagram.server.dao.UsersDAO;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
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
        // TLS connections; port = 0 -> ephemeral port
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
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
    public void updatePassword(UserToken token, String passwordHash, String newPasswordHash) throws
            ServerOperationFailedException {
        this.maskedServer.updatePassword(token, passwordHash, newPasswordHash);
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
     *
     * @return
     */
    @Override
    public NewChatData initiateChat(UserToken token, IRemoteUserTunnel localTunnel, PublicKey localPublicKey,
                                    String remoteUser) throws ServerOperationFailedException {
        return this.maskedServer.initiateChat(token, localTunnel, localPublicKey, remoteUser);
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

    /**
     * Performs any tasks that are required to successfully stop the execution of the Javagram server.
     */
    public void haltExecution() {
        this.maskedServer.haltExecution();
    }
}
