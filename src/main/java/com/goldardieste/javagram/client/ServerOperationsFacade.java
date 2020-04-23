package com.goldardieste.javagram.client;

import com.goldardieste.javagram.common.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
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
     * @throws RemoteException       the remote object cannot be successfully exported.
     * @throws IllegalStateException if the specified Javagram server cannot be retrieved.
     */
    public ServerOperationsFacade(String rmiRemoteAddress, int rmiRemotePort, String javagramServerIdentifier,
                                  ClientFacade clientFacade) throws RemoteException, IllegalStateException {
        super();
        this.clientFacade = clientFacade;

        this.rmiRemoteAddress = rmiRemoteAddress;
        this.rmiRemotePort = rmiRemotePort;
        this.javagramServerIdentifier = javagramServerIdentifier;
        this.javagramServer = retrieveJavagramServer();
    }


    /* ----- Methods ----- */

    /**
     * Retrieves an instance of {@link IServer} from the specified RMI registry.
     *
     * @return instance of {@link IServer} that represents a Javagram server.
     * @throws IllegalStateException if the specified Javagram server cannot be retrieved.
     */
    private IServer retrieveJavagramServer() throws IllegalStateException {

        String url = "rmi://" + this.rmiRemoteAddress + ":" + this.rmiRemotePort + "/" + this.javagramServerIdentifier;
        System.out.println(url);

        try {
            return (IServer) Naming.lookup(url);

        } catch (NotBoundException e) {
            System.err.println("The following remote object could not be found: " + url);
            throw new IllegalStateException("No remote Javagram server object could be retrieved", e);

        } catch (MalformedURLException e) {
            System.err.println("\"Naming.rebind\" received the following malformed URL: " + url);
            throw new IllegalStateException("No remote Javagram server object could be retrieved", e);

        } catch (RemoteException e) {
            System.err.println("The RMI registry could not be contacted: " + url);
            throw new IllegalStateException("No remote Javagram server object could be retrieved", e);
        }
    }

    /**
     * Calls {@link IServer#signUp(String, String, IServerNotificationsListener)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public UserToken signUp(String username, String passwordHash) throws RemoteException {
        return this.javagramServer.signUp(username, passwordHash, this);
    }

    /**
     * Calls {@link IServer#login(String, String, IServerNotificationsListener)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public UserToken login(String username, String passwordHash) throws RemoteException {
        return this.javagramServer.login(username, passwordHash, this);
    }

    /**
     * Calls {@link IServer#updatePassword(UserToken, String, String)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public void updatePassword(UserToken token, String passwordHash, String newPasswordHash) throws RemoteException {
        this.javagramServer.updatePassword(token, passwordHash, newPasswordHash);
    }

    /**
     * Calls {@link IServer#disconnect(UserToken)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public void disconnect(UserToken token) throws RemoteException {
        this.javagramServer.disconnect(token);
    }

    /**
     * Calls {@link IServer#retrieveFriends(UserToken)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public List<RemoteUser> retrieveFriends(UserToken token) throws RemoteException {
        return this.javagramServer.retrieveFriends(token);
    }

    /**
     * Calls {@link IServer#retrieveFriends(UserToken, StatusType)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public List<RemoteUser> retrieveFriends(UserToken token, StatusType status) throws RemoteException {
        return this.javagramServer.retrieveFriends(token, status);
    }

    /**
     * Calls {@link IServer#initiateChat(UserToken, IRemoteUserTunnel, PublicKey, String)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public NewChatData initiateChat(UserToken token, IRemoteUserTunnel localTunnel, PublicKey localPublicKey,
                                    String remoteUser) throws RemoteException {
        return this.javagramServer.initiateChat(token, localTunnel, localPublicKey, remoteUser);
    }

    /**
     * Calls {@link IServer#requestFriendship(UserToken, String)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public void requestFriendship(UserToken token, String remoteUser) throws RemoteException {
        this.javagramServer.requestFriendship(token, remoteUser);
    }

    /**
     * Calls {@link IServer#acceptFriendship(UserToken, String)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public boolean acceptFriendship(UserToken token, String remoteUser) throws RemoteException {
        return this.javagramServer.acceptFriendship(token, remoteUser);
    }

    /**
     * Calls {@link IServer#rejectFriendship(UserToken, String)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public void rejectFriendship(UserToken token, String remoteUser) throws RemoteException {
        this.javagramServer.rejectFriendship(token, remoteUser);
    }

    /**
     * Calls {@link IServer#endFriendship(UserToken, String)}.
     *
     * @throws RemoteException if {@link #javagramServer} cannot complete the requested operation.
     */
    public void endFriendship(UserToken token, String remoteUser) throws RemoteException {
        this.javagramServer.endFriendship(token, remoteUser);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public NewChatData replyChatRequest(String remoteUser, IRemoteUserTunnel remoteUserTunnel, PublicKey
            remoteUserPublicKey) throws RemoteException {
        return this.clientFacade.replyChatRequest(remoteUser, remoteUserTunnel, remoteUserPublicKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRemoteUserStatus(RemoteUser remoteUser) throws RemoteException {
        this.clientFacade.updateRemoteUserStatus(remoteUser);
    }

    /**
     * Unexports the client's proxy so that it is no longer a remote object.
     */
    public void haltExecution() {

        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException e) {
            System.err.println("The client's proxy could not be successfully unexported");
            e.printStackTrace();
        }
    }
}
