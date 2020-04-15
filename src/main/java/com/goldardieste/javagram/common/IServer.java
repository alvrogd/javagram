package com.goldardieste.javagram.common;

import java.rmi.RemoteException;
import java.util.List;

/**
 * This interface contains all the operations that a Javagram server must support.
 */
public interface IServer {

    /* ----- Methods ----- */

    /**
     * Registers a new Javagram user, and it is also automatically logged in.
     *
     * @param username                    name by which the user will be identified.
     * @param passwordHash                hash of the user's password.
     * @param serverNotificationsListener listener, at the client side, for when the server needs to notify anything
     *                                    (for example, an incoming friendship request).
     * @return {@link UserToken} which the client must use to verify its identity
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    UserToken signUp(String username, String passwordHash, IServerNotificationsListener serverNotificationsListener)
            throws RemoteException;

    /**
     * Initializes a session using the specified Javagram user. Invalidates any previously opened session.
     *
     * @param username                    name by which the user can be identified.
     * @param passwordHash                hash of the user's password.
     * @param serverNotificationsListener listener, at the client side, for when the server needs to notify anything
     *                                    (for example, an incoming friendship request).
     * @return {@link UserToken} which the client must use to verify its identity
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    UserToken login(String username, String passwordHash, IServerNotificationsListener serverNotificationsListener)
            throws RemoteException;

    /**
     * Terminates a session that was previously initiated.
     *
     * @param token {@link UserToken} that identifies the session which will be terminated.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    void disconnect(UserToken token) throws RemoteException;

    /**
     * Retrieves a collection that contains all the users that the specified one is related to in any way. That is, the
     * retrieved remote users may be current friends of the local one, they may have sent him a friendship request, or
     * the may also received a friendship request from him.
     *
     * @param token identifies the user on whose behalf the operation will be performed.
     * @return all the related users that have been found.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    List<RemoteUser> retrieveFriends(UserToken token) throws RemoteException;

    /**
     * Retrieves a collection that contains all the users that the specified one is related to in a way determined by
     * the given state.
     *
     * @param token  identifies the user on whose behalf the operation will be performed.
     * @param status status in which the remote users will be in relation to the other user.
     * @return all the related users that have been found.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    List<RemoteUser> retrieveFriends(UserToken token, StatusType status) throws RemoteException;

    /**
     * The server asks the remote user, in behalf of the client that requests the operation, to initialize a connection
     * through which they may communicate.
     *
     * @param token       identifies the user on whose behalf the operation will be performed.
     * @param localTunnel {@link IRemoteUserTunnel} that the client has opened so that the remote user may send him
     *                    messages.
     * @param remoteUser  name by which the remote user that will be asked can be identified.
     * @return if the remote user accepts the request, a {@link IRemoteUserTunnel} through which the client may send
     * him messages is returned.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    IRemoteUserTunnel initiateChat(UserToken token, IRemoteUserTunnel localTunnel, String remoteUser) throws
            RemoteException;

    /**
     * A friendship request is sent to the remote user on behalf of the client, as long as it did not already exist. If
     * the remote user has already sent a friendship request, they automatically become friends.
     * <p>
     * The request is also registered, so that the remote user may receive in the future even if he is or goes offline.
     * The remote user will also be notified about it if he is online.
     *
     * @param token      identifies the user on whose behalf the operation will be performed.
     * @param remoteUser name by which the user who will receive the request can be identified.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    void requestFriendship(UserToken token, String remoteUser) throws RemoteException;

    /**
     * A friendship request is accepted, as long as the remote user has sent it previously to the client. The remote
     * user will also be notified about it if he is online.
     *
     * @param token      identifies the user on whose behalf the operation will be performed.
     * @param remoteUser name by which the user who sent the request can be identified.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    void acceptFriendship(UserToken token, String remoteUser) throws RemoteException;

    /**
     * A friendship request is rejected, as long as the remote user has sent it previously to the client. The remote
     * user will also be notified about it if he is online.
     *
     * @param token      identifies the user on whose behalf the operation will be performed.
     * @param remoteUser name by which the user who sent the request can be identified.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    void rejectFriendship(UserToken token, String remoteUser) throws RemoteException;

    /**
     * A friendship is terminated, as long as it existed previously between the client and the remote user. The remote
     * user will also be notified about it if he is online.
     *
     * @param token      identifies the user on whose behalf the operation will be performed.
     * @param remoteUser name by which the client's friend request can be identified.
     * @throws RemoteException irrecoverable error during a remote procedure call.
     */
    void endFriendship(UserToken token, String remoteUser) throws RemoteException;
}
