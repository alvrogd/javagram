package com.goldardieste.javagram.client;

import com.goldardieste.javagram.common.*;

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
    private ServerOperationsFacade serverOperationsFacade;

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


    /* ----- Constructor ----- */

    /**
     * Initializes an empty {@link ClientFacade}. The client must log in or sign up for a Javagram user account before
     * performing any other operation.
     */
    public ClientFacade() {
    }


    /* ----- Methods ----- */

    // TODO add method to configure Javagram server -> instantiates a ServerFacade
    // TODO implement all methods

    /**
     * Checks if the client as successfully identified in a Javagram server as a Javagram user.
     *
     * @return if the client is identified as a Javagram user.
     */
    public boolean isSessionInitiated() {

        return true;
    }

    /**
     * Asks the Javagram server to create a new Javagram user, and the client is also automatically logged in.
     *
     * @param username name by which the user will be identified.
     * @param password user's password.
     * @return true if the request has been completed successfully.
     */
    public boolean signUp(String username, String password) {

        return true;
    }

    /**
     * Asks the Javagram server to initialize a session using the specified Javagram user.
     *
     * @param username name by which the user can be identified.
     * @param password user's password.
     * @return true if the request has been completed successfully.
     */
    public boolean login(String username, String password) {

        return true;
    }

    /**
     * Asks the Javagram server to terminate the session that was previously initiated.
     *
     * @return true if the request has been completed successfully.
     */
    public boolean disconnect() {

        return true;
    }

    /**
     * Asks the Javagram server to provide a collection that contains all the users that the specified one is related,
     * to in any way, and they will be stored by {@link #currentUserFacade}. That is, the retrieved remote users may be
     * current friends of the local one, they may have sent him a friendship request, or the may also received a
     * friendship request from him.
     */
    public void retrieveFriends() {

    }

    /**
     * Asks the Javagram server to provide a collection that contains all the users that the specified one is related
     * to in a way determined by the given state, and they will be stored by {@link #currentUserFacade}.
     *
     * @param status status in which the remote users will be in relation to the other user.
     */
    public void retrieveFriends(StatusType status) {

    }

    /**
     * The client prepares and then requests the Javagram server to contact with a remote user who will be asked to
     * initialize a communication channel with the client, through which the may communicate.
     *
     * @param remoteUser name by which the remote user that will be asked can be identified.
     * @return if the remote user accepts the request, therefore allowing the client to communicate with him.
     */
    public boolean initiateChat(String remoteUser) {

        return true;
    }

    /**
     * The client sends a given message to the specified remote user. A connection between the client and that remote
     * user must have been previously initiated using {@link ClientFacade#initiateChat(String)}.
     *
     * @param remoteUser name by which the remote user that will be sent the message can be identified.
     * @param message    content of the message that will be sent.
     */
    public void sendMessage(String remoteUser, String message) {

    }

    /**
     * Asks the Javagram server to send a friendship request to the remote user on behalf of the client, as long as it
     * did not already exist. The remote user will receive the petition whether or not he is currently online or not,
     * and he will even receive it each time he comes online until the request gets rejected or accepted.
     *
     * @param remoteUser name by which the user who will receive the request can be identified.
     */
    public void requestFriendship(String remoteUser) {

    }

    /**
     * Asks the Javagram server to accept a friendship request, as long as the remote user has sent it previously to
     * the client. The remote user will also be notified about it if he is online.
     *
     * @param remoteUser name by which the user who sent the request can be identified.
     */
    public void acceptFriendship(String remoteUser) {

    }

    /**
     * Asks the Javagram server to reject a friendship request, as long as the remote user has sent it previously to
     * the client. The remote user will also be notified about it if he is online.
     *
     * @param remoteUser name by which the user who sent the request can be identified.
     */
    public void rejectFriendship(String remoteUser) {

    }

    /**
     * Asks the Javagram server to terminate a friendship, as long as it existed previously between the client and the
     * remote user. The remote user will also be notified about it if he is online.
     *
     * @param token      identifies the user on whose behalf the operation will be performed.
     * @param remoteUser name by which the client's friend request can be identified.
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
     * @param remoteUser
     */
    @Override
    public void updateRemoteUserStatus(RemoteUser remoteUser) {

    }
}
