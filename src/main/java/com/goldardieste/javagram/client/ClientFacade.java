package com.goldardieste.javagram.client;

import com.goldardieste.javagram.client.cryptography.CryptographicServices;
import com.goldardieste.javagram.common.*;

import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.concurrent.locks.ReentrantLock;

// TODO catch and throw exception when the server is not reachable

/**
 * This class holds the main logic of the Javagram client. Therefore, it orchestrates any operation that the user may
 * request via the GUI so that it is completed successfully.
 */
public class ClientFacade implements IServerNotificationsListener {

    /* ----- Attributes ----- */

    /**
     * Acts as an intermediary between the Javagram server and this class, so that the last one does not have to be
     * exposed as a remote object.
     */
    private final ServerOperationsFacade serverOperationsFacade;

    /**
     * Manages all remote users related to the currently identified one, and also manages all tunnels used to
     * communicate with other users.
     */
    private CurrentUserFacade currentUserFacade;

    /**
     * {@link UserToken} that a Javagram server has generated to verify the identity of the currently logged in user.
     * It will be requested whenever the client requests an operation to the server.
     */
    private UserToken userToken;

    /**
     * {@link ReentrantLock} that a thread must acquire to check/modify the value of {@link #userToken}.
     */
    private final ReentrantLock userTokenLock;

    /**
     * {@link CryptographicServices} used to encrypt/decrypt communications with other Javagram users.
     */
    private CryptographicServices cryptographicServices;


    /* ----- Constructor ----- */

    /**
     * Initializes an empty {@link ClientFacade} that will establish a connection to the specified server. The client
     * must log in or sign up for a Javagram user account before performing any other operation. After initializing the
     * object, it is strongly recommended to consider executing {@link #setLocalTunnelsListener(LocalTunnelsListener)}
     * and {@link #setRemoteUsersListener(RemoteUsersListener)} to fully configure the client's back-end.
     *
     * @param rmiRemoteAddress         address where the Javagram server can be located.
     * @param rmiRemotePort            port where the Javagram server can be located.
     * @param javagramServerIdentifier name by which the Javagram server can be located.
     * @throws IllegalStateException if the connection with the server cannot be successfully established, or if the
     *                               the client's proxy cannot be instantiated.
     */
    public ClientFacade(String rmiRemoteAddress, int rmiRemotePort, String javagramServerIdentifier) throws
            IllegalStateException {

        try {
            this.serverOperationsFacade = new ServerOperationsFacade(rmiRemoteAddress, rmiRemotePort,
                    javagramServerIdentifier, this);

        } catch (RemoteException e) {
            System.err.println("The client's proxy could not be instantiated");
            throw new IllegalStateException(e);
        }

        this.userTokenLock = new ReentrantLock();
    }


    /* ----- Methods ----- */

    /**
     * Checks if the client has successfully identified in a Javagram server as a Javagram user.
     *
     * @return if the client is identified as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public boolean isSessionInitiated() throws ClientOperationFailedException {

        boolean initiated = false;

        this.userTokenLock.lock();

        try {
            initiated = this.userToken != null;

        } finally {
            // The lock must always be released
            this.userTokenLock.unlock();
        }

        return initiated;
    }

    /**
     * Asks the Javagram server to create a new Javagram user, and the client is also automatically logged in.
     *
     * @param username name by which the user will be identified.
     * @param password user's password.
     * @return true if the request has been completed successfully.
     * @throws InvalidClientSessionException  if the current client is already logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public boolean signUp(String username, String password) throws ClientOperationFailedException {

        if (!isSessionInitiated()) {

            this.userTokenLock.lock();

            try {
                this.userToken = this.serverOperationsFacade.signUp(username, password);
                this.currentUserFacade = new CurrentUserFacade(username);
                this.cryptographicServices = new CryptographicServices();
                CurrentUserFacade.setCommunicationDecryptionUtility(this.cryptographicServices);

            } catch (RemoteException e) {
                System.err.println("The server could not perform the requested sign up operation");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not perform the requested sign up " +
                        "operation");

            } finally {
                // The lock must always be released
                this.userTokenLock.unlock();
            }

        } else {
            System.err.println("A session has already been initiated");
            throw new InvalidClientSessionException("A session has already been initiated");
        }

        return isSessionInitiated();
    }

    /**
     * Asks the Javagram server to initialize a session using the specified Javagram user.
     *
     * @param username name by which the user can be identified.
     * @param password user's password.
     * @return true if the request has been completed successfully.
     * @throws InvalidClientSessionException  if the current client is already logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public boolean login(String username, String password) throws ClientOperationFailedException {

        if (!isSessionInitiated()) {

            this.userTokenLock.lock();

            try {
                this.userToken = this.serverOperationsFacade.login(username, password);
                this.currentUserFacade = new CurrentUserFacade(username);
                this.cryptographicServices = new CryptographicServices();
                CurrentUserFacade.setCommunicationDecryptionUtility(this.cryptographicServices);

            } catch (RemoteException e) {
                System.err.println("The server could not perform the requested log in operation");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not perform the requested log in " +
                        "operation");

            } finally {
                // The lock must always be released
                this.userTokenLock.unlock();
            }

        } else {
            System.err.println("A session has already been initiated");
            throw new InvalidClientSessionException("A session has already been initiated");
        }

        return isSessionInitiated();
    }

    /**
     * Asks the Javagram server to update the password of the current user.
     *
     * @param currentPassword user's current password.
     * @param newPassword user's new password.
     * @return true if the request has been completed successfully.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public boolean updatePassword(String currentPassword, String newPassword) throws ClientOperationFailedException {

        boolean successful = false;

        if (isSessionInitiated()) {

            try {
                this.serverOperationsFacade.updatePassword(this.userToken, currentPassword, newPassword);

                successful = true;

            } catch (RemoteException e) {
                System.err.println("The server could not perform the requested password update operation");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not perform the requested password " +
                        "update");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }

        return successful;
    }

    /**
     * Asks the Javagram server to terminate the session that was previously initiated. It also performs any needed
     * clean up to free any resources used by the currently logged in user.
     *
     * @return true if the request has been completed successfully.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public boolean disconnect() throws ClientOperationFailedException {

        boolean successful = false;

        if (isSessionInitiated()) {

            this.userTokenLock.lock();

            try {
                this.serverOperationsFacade.disconnect(this.userToken);

                // The current user token and the current user's facade are only removed when the client receives the
                // confirmation about the disconnection
                this.userToken = null;

                this.currentUserFacade.haltExecution();
                this.currentUserFacade = null;

                successful = true;

            } catch (RemoteException e) {
                System.err.println("The server could not perform the requested disconnection operation");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not perform the requested disconnection " +
                        "operation");

            } finally {
                // The lock must always be released
                this.userTokenLock.unlock();
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }

        return successful;
    }

    /**
     * Asks the Javagram server to provide a collection that contains all the users that the specified one is related,
     * to in any way, and they will be stored by {@link #currentUserFacade}. That is, the retrieved remote users may be
     * current friends of the local one, they may have sent him a friendship request, or they may also have received a
     * friendship request from him.
     *
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public void retrieveFriends() throws ClientOperationFailedException {

        if (isSessionInitiated()) {

            try {
                this.currentUserFacade.addRemoteUsers(this.serverOperationsFacade.retrieveFriends(this.userToken));

            } catch (RemoteException e) {
                System.err.println("The server could not retrieved the requested remote users");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not retrieved the requested remote users");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }
    }

    /**
     * Asks the Javagram server to provide a collection that contains all the users that the specified one is related
     * to in a way determined by the given state, and they will be stored by {@link #currentUserFacade}.
     *
     * @param status status in which the remote users will be in relation to the other user.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public void retrieveFriends(StatusType status) throws ClientOperationFailedException {

        if (isSessionInitiated()) {

            try {
                this.currentUserFacade.addRemoteUsers(this.serverOperationsFacade.retrieveFriends(this.userToken,
                        status), status);

            } catch (RemoteException e) {
                System.err.println("The server could not retrieved the requested remote users");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not retrieved the requested remote users");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }
    }

    /**
     * The client prepares and then requests the Javagram server to contact with a remote user who will be asked to
     * initialize a communication channel with the client, through which the may communicate.
     *
     * @param remoteUser name by which the remote user that will be asked can be identified.
     * @return if the remote user accepts the request, therefore allowing the client to communicate with him.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public boolean initiateChat(String remoteUser) throws ClientOperationFailedException {

        boolean successful = false;

        if (isSessionInitiated()) {

            if (this.currentUserFacade.checkRemoteUserStatus(remoteUser, StatusType.ONLINE)
                    || this.currentUserFacade.checkRemoteUserStatus(remoteUser, StatusType.DISCONNECTED)) {

                if (!isChatInitiated(remoteUser)) {

                    try {
                        IRemoteUserTunnel localTunnel = this.currentUserFacade.prepareTunnel(remoteUser);

                        NewChatData chatData = this.serverOperationsFacade.initiateChat(this.userToken,
                                localTunnel, this.cryptographicServices.getPublicKey(), remoteUser);

                        this.currentUserFacade.storeTunnel(remoteUser, chatData.remoteUserTunnel);
                        this.cryptographicServices.storeSecretForCommunication(remoteUser,
                                chatData.encryptedCommunicationSecret);

                        successful = true;

                    } catch (RemoteException e) {
                        System.err.println("The server could not transmit the request to initiate a chat");
                        e.printStackTrace();
                        throw new ClientOperationFailedException("The server could not transmit the request to " +
                                "initiate a chat");

                    } catch (TunnelOperationException e) {
                        System.err.println("Could not open a local tunnel to allow the remote user to communicate " +
                                "with the client");
                        e.printStackTrace();
                        throw new ClientOperationFailedException("Could not open a local tunnel to allow the remote " +
                                "user to communicate with the client");
                    }
                }

            } else {
                throw new ClientOperationFailedException("The client is not currently friends with the specified " +
                        "remote user");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }

        return successful;
    }

    /**
     * Checks if the chat with the specified remote user is ready.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @return if the chat is ready.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public boolean isChatInitiated(String remoteUser) throws ClientOperationFailedException {

        boolean initiated = false;

        if (isSessionInitiated()) {

            initiated = this.currentUserFacade.areBothTunnelsPrepared(remoteUser);

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }

        return initiated;
    }

    /**
     * The client sends a given message to the specified remote user. A connection between the client and that remote
     * user must have been previously initiated using {@link ClientFacade#initiateChat(String)}.
     *
     * @param remoteUser name by which the remote user that will be sent the message can be identified.
     * @param message    content of the message that will be sent.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public void sendMessage(String remoteUser, String message) throws ClientOperationFailedException {

        if (isSessionInitiated()) {

            if (isChatInitiated(remoteUser)) {

                try {
                    this.currentUserFacade.sendMessage(remoteUser, this.cryptographicServices.encryptString(remoteUser,
                            message));

                } catch (TunnelOperationException e) {
                    System.err.println("Could not send the given message to the specified user");
                    e.printStackTrace();
                    throw new ClientOperationFailedException("Could not send the given message to the specified user");
                }

            } else {
                throw new ClientOperationFailedException("The chat with the specified remote user has not been " +
                        "initiated yet");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }
    }

    /**
     * Asks the Javagram server to send a friendship request to the remote user on behalf of the client, as long as it
     * did not already exist. The remote user will receive the petition whether or not he is currently online or not,
     * and he will even receive it each time he comes online until the request gets rejected or accepted.
     *
     * @param remoteUser name by which the user who will receive the request can be identified.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public void requestFriendship(String remoteUser) throws ClientOperationFailedException {

        if (isSessionInitiated()) {

            // The specified RemoteUser must not be the current one
            if (!this.currentUserFacade.getIdentifiedUser().equals(remoteUser)) {

                try {
                    // It only makes sense to send a friendship request if:
                    // - The users are not friends yet
                    // - The remote user has not sent a friendship request yet
                    // - The user has not sent a friendship request yet
                    //
                    // Therefore, due to the possible states in which a RemoteUser can be, a friendship request will only
                    // be sent if the remote user has not been retrieved previously.
                    if (this.currentUserFacade.getRemoteUser(remoteUser) == null) {
                        this.serverOperationsFacade.requestFriendship(this.userToken, remoteUser);
                        this.currentUserFacade.updateRemoteUserStatus(remoteUser, StatusType.FRIENDSHIP_SENT);
                    }

                } catch (RemoteException e) {
                    System.err.println("The server could not register the friendship request");
                    e.printStackTrace();
                    throw new ClientOperationFailedException("The server could not register the friendship request");
                }
            } else {
                throw new ClientOperationFailedException("The specified user to which the request was going to be " +
                        "sent is the client himself");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }
    }

    /**
     * Asks the Javagram server to accept a friendship request, as long as the remote user has sent it previously to
     * the client. The remote user will also be notified about it if he is online.
     *
     * @param remoteUser name by which the user who sent the request can be identified.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public void acceptFriendship(String remoteUser) throws ClientOperationFailedException {

        if (isSessionInitiated()) {

            try {
                // It only makes sense to accept a friendship request if it has been received
                if (this.currentUserFacade.checkRemoteUserStatus(remoteUser, StatusType.FRIENDSHIP_RECEIVED)) {

                    boolean online = this.serverOperationsFacade.acceptFriendship(this.userToken, remoteUser);

                    this.currentUserFacade.updateRemoteUserStatus(remoteUser, online ? StatusType.ONLINE :
                            StatusType.DISCONNECTED);
                }

            } catch (RemoteException e) {
                System.err.println("The server could not accept the friendship request");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not accept the friendship request");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }
    }

    /**
     * Asks the Javagram server to reject a friendship request, as long as the remote user has sent it previously to
     * the client. The remote user will also be notified about it if he is online.
     *
     * @param remoteUser name by which the user who sent the request can be identified.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public void rejectFriendship(String remoteUser) throws ClientOperationFailedException {

        if (isSessionInitiated()) {

            try {
                // It only makes sense to reject a friendship request if it has been received
                if (this.currentUserFacade.checkRemoteUserStatus(remoteUser, StatusType.FRIENDSHIP_RECEIVED)) {

                    this.serverOperationsFacade.rejectFriendship(this.userToken, remoteUser);

                    this.currentUserFacade.removeRemoteUser(remoteUser);
                    // There is no need to close any tunnel as the client cannot have established any connection to the
                    // specified remote user as they were not friends
                }

            } catch (RemoteException e) {
                System.err.println("The server could not reject the friendship request");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not reject the friendship request");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }
    }

    /**
     * Asks the Javagram server to terminate a friendship, as long as it existed previously between the client and the
     * remote user. The remote user will also be notified about it if he is online.
     *
     * @param remoteUser name by which the client's friend request can be identified.
     * @throws InvalidClientSessionException  if the current client is not logged in as a Javagram user.
     * @throws ClientOperationFailedException if the operation could not be completed successfully.
     */
    public void endFriendship(String remoteUser) throws ClientOperationFailedException {

        if (isSessionInitiated()) {

            try {
                // It only makes sense to end a friendship if the users are currently friends
                if (this.currentUserFacade.checkRemoteUserStatus(remoteUser, StatusType.ONLINE)
                        || this.currentUserFacade.checkRemoteUserStatus(remoteUser, StatusType.DISCONNECTED)) {

                    this.serverOperationsFacade.endFriendship(this.userToken, remoteUser);

                    this.currentUserFacade.removeRemoteUser(remoteUser);
                    this.currentUserFacade.closeTunnels(remoteUser);
                }

            } catch (RemoteException e) {
                System.err.println("The server could not end the friendship");
                e.printStackTrace();
                throw new ClientOperationFailedException("The server could not end the friendship");

            } catch (TunnelOperationException e) {
                System.err.println("Could not close the connections with the specified user");
                e.printStackTrace();
                throw new ClientOperationFailedException("Could not close the connections with the specified user");
            }

        } else {
            System.err.println("No valid user session has been established yet");
            throw new InvalidClientSessionException("No valid user session has been established yet");
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public NewChatData replyChatRequest(String remoteUser, IRemoteUserTunnel remoteUserTunnel, PublicKey
            remoteUserPublicKey) throws ClientListeningOperationFailedException {

        NewChatData result = null;

        // No checks are made to determine if the user session is valid, as the Javagram server will only execute
        // remote methods in the client when it is logged in
        try {
            IRemoteUserTunnel localTunnel = this.currentUserFacade.replyChatRequest(remoteUser, remoteUserTunnel);
            String secret = this.cryptographicServices.generateSecretForCommunication(remoteUser, remoteUserPublicKey);

            result = new NewChatData(localTunnel, secret);

        } catch (TunnelOperationException e) {
            System.err.println("Could not set up a connection for a remote user that wants to communicate with" +
                    "the client");
            e.printStackTrace();
            throw new ClientListeningOperationFailedException("Could not set up a connection for a remote user that " +
                    "wants to communicate with the client");
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRemoteUserStatus(RemoteUser remoteUser) {

        // No checks are made to determine if the user session is valid, as the Javagram server will only execute
        // remote methods in the client when it is logged in
        if (remoteUser.getStatus().equals(StatusType.NOT_RELATED)) {

            this.currentUserFacade.removeRemoteUser(remoteUser.getUsername());

            try {
                this.currentUserFacade.closeTunnels(remoteUser.getUsername());

            } catch (TunnelOperationException e) {
                System.err.println("Could not close the connections with the specified user");
                e.printStackTrace();
            }

        } else {
            this.currentUserFacade.updateRemoteUserStatus(remoteUser.getUsername(), remoteUser.getStatus());
        }
    }

    /**
     * Updates the {@link LocalTunnelsListener} that all {@link LocalUserTunnel} will use when receiving data.
     *
     * @param localTunnelsListener the new {@link LocalTunnelsListener}.
     */
    public void setLocalTunnelsListener(LocalTunnelsListener localTunnelsListener) {
        CurrentUserFacade.setLocalTunnelsListener(localTunnelsListener);
    }

    /**
     * Updates the {@link RemoteUsersListener} that {@link #currentUserFacade} will use when modifying the currently
     * stored {@link RemoteUser}s.
     *
     * @param remoteUsersListener the new {@link RemoteUsersListener}.
     */
    public void setRemoteUsersListener(RemoteUsersListener remoteUsersListener) {
        this.currentUserFacade.setRemoteUsersListener(remoteUsersListener);
    }

    /**
     * Performs any tasks that are required to successfully stop the execution of the Javagram client's back-end.
     */
    public void haltExecution() {

        this.serverOperationsFacade.haltExecution();
    }
}
