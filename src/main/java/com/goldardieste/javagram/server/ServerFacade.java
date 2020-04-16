package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.*;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds the main logic of the Javagram server. Therefore, it orchestrates any operation that a client may
 * request so that it is completed successfully.
 */
public class ServerFacade implements IServer {

    /* ----- Attributes ----- */

    /**
     * It will take care of creating, verifying and deleting the users' sessions.
     */
    private final CurrentSessionsManager currentSessionsManager;

    /**
     * It will take care of accessing and modifying the server's database.
     */
    private final UsersDAO usersDAO;

    /**
     * It will mask the real server so that it is not directly exposed to the clients.
     */
    private final IServer proxy;

    /**
     * Stores, for each currently logged in user, the {@link IServerNotificationsListener} that will attend any
     * notification that the server may need to send to the user's client.
     * <p>
     * Key -> username.
     * Value -> its corresponding {@link IServerNotificationsListener} in the client where the user is logged in.
     */
    private final Map<String, IServerNotificationsListener> serverNotificationsListeners;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link ServerFacade} to provide services to Javagram clients. It also takes care of creating the
     * proxy that will be exported as a remote object in a RMI registry.
     *
     * @param jdbcDriver   identifier of the driver that allows contacting the database.
     * @param jdbcURL      database's URL.
     * @param jdbcPort     database's port.
     * @param jdbcDatabase database's name.
     * @param jdbcUsername username of a valid account in the database.
     * @param jdbcPassword password of a valid account in the database.
     * @throws IllegalStateException if the connection with the database cannot be successfully established, or if the
     *                               the server's proxy cannot be instantiated.
     */
    public ServerFacade(String jdbcDriver, String jdbcURL, int jdbcPort, String jdbcDatabase, String jdbcUsername,
                        String jdbcPassword) {

        this.currentSessionsManager = new CurrentSessionsManager();
        this.usersDAO = new UsersDAO(jdbcDriver, jdbcURL, jdbcPort, jdbcDatabase, jdbcUsername, jdbcPassword);

        try {
            this.proxy = new ServerFacadeProxy(this);

        } catch (RemoteException e) {
            System.err.println("The server's proxy could not be instantiated");
            throw new IllegalStateException(e);
        }

        this.serverNotificationsListeners = new ConcurrentHashMap<>();
    }


    /* ----- Methods ----- */

    /**
     * {@inheritDoc}
     */
    @Override
    public UserToken signUp(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) throws ServerOperationFailedException {

        Connection connection = null;
        UserToken userToken = null;

        try {
            // 1. User is registered
            connection = this.usersDAO.getConnection();

            if (!this.usersDAO.existsUser(connection, username)) {

                // Will throw an exception if an user with the given username already exists
                this.usersDAO.createUser(connection, username, passwordHash);

                // 2. It is automatically logged in
                userToken = this.currentSessionsManager.initiateSession(username);

                // 3. Client's listener is stored for later usage
                this.serverNotificationsListeners.put(username, serverNotificationsListener);

            } else {
                throw new ServerOperationFailedException("The specified username is already registered");
            }

        } catch (DaoOperationException e) {
            System.err.println("Could not register the specified user");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not register the specified user");

        } finally {
            // 4. If the previous steps have been completed successfully, the operations will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }

        return userToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserToken login(String username, String passwordHash, IServerNotificationsListener
            serverNotificationsListener) throws ServerOperationFailedException {

        Connection connection = null;
        UserToken userToken = null;

        try {
            // 1. Credentials are validated
            connection = this.usersDAO.getConnection();

            if (this.usersDAO.verifyUserCredentials(connection, username, passwordHash)) {

                // 2. If they are valid, the user gets logged in
                userToken = this.currentSessionsManager.initiateSession(username);

                // 3. Client's listener is stored for later usage
                this.serverNotificationsListeners.put(username, serverNotificationsListener);

                // 4. All current friends of the user are notified about him coming online
                try {
                    List<RemoteUserDao> currentFriends =
                            this.usersDAO.retrieveFriends(connection, username, StatusTypeUserDAO.ACCEPTED_FRIENDSHIP);
                    currentFriends.forEach((f) ->
                            notifyOnlineUserAboutUserStatus(f.getUsername(), username, StatusType.ONLINE));

                } catch (DaoOperationException e) {
                    System.err.println("Could not notify a given user's friends about him coming online");
                    e.printStackTrace();

                    // An exception is thrown even tough the user has successfully identified as is current friends
                    // will not be able to communicate with him
                    throw new ServerOperationFailedException("Could not initiate the session in the Javagram network");
                }

            } else {
                throw new ServerOperationFailedException("The specified credentials are not valid");
            }

        } catch (DaoOperationException e) {
            System.err.println("Could not verify the given credentials");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not initiate the session in the Javagram network");

        } finally {
            // 5. If the previous steps have been completed successfully, the operations will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }

        return userToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect(UserToken token) throws ServerOperationFailedException {

        Connection connection = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            // All current friends of the user are notified about him going offline
            List<RemoteUserDao> currentFriends =
                    this.usersDAO.retrieveFriends(connection, username, StatusTypeUserDAO.ACCEPTED_FRIENDSHIP);
            currentFriends.forEach((f) ->
                    notifyOnlineUserAboutUserStatus(f.getUsername(), username, StatusType.DISCONNECTED));

            // The given token will no longer be valid
            this.currentSessionsManager.terminateSession(token);

            // The client's listener is no longer needed
            this.serverNotificationsListeners.remove(username);

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not close the specified session");

        } catch (DaoOperationException e) {
            System.err.println("Could not notify a given user's friends about him going offline");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not close the specified session");
        }

        finally {
            // If the previous steps have been completed successfully, the operations will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteUser> retrieveFriends(UserToken token) throws ServerOperationFailedException {

        List<RemoteUser> result = new ArrayList<>();
        Connection connection = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            result.addAll(remoteUserFromRemoteUserDao(this.usersDAO.retrieveFriends(connection, username)));

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not retrieve the friends of the specified user");

        } catch (DaoOperationException e) {
            System.err.println("Could not retrieve the friends of the specified user");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not retrieve the friends of the specified user");

        } finally {
            // If the previous steps have been completed successfully, the operation will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteUser> retrieveFriends(UserToken token, StatusType status) throws ServerOperationFailedException {

        List<RemoteUser> result = new ArrayList<>();
        Connection connection = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            result.addAll(remoteUserFromRemoteUserDao(this.usersDAO.retrieveFriends(connection, username,
                    StatusTypeUserDAO.statusTypeUserDaoFromStatusType(status))));

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not retrieve the specified friends of the given user");

        } catch (DaoOperationException e) {
            System.err.println("Could not retrieve the specified friends of the given user");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not retrieve the specified friends of the given user");

        } finally {
            // If the previous steps have been completed successfully, the operation will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRemoteUserTunnel initiateChat(UserToken token, IRemoteUserTunnel localTunnel, String remoteUser) throws
            ServerOperationFailedException {

        Connection connection = null;
        IRemoteUserTunnel result = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            // Both users must be current friends
            if (this.usersDAO.checkUsersStatus(connection, username, remoteUser,
                    StatusTypeUserDAO.ACCEPTED_FRIENDSHIP)) {

                // And the remote user must be online
                IServerNotificationsListener listener = this.serverNotificationsListeners.get(remoteUser);

                if (listener != null) {
                    result = listener.replyChatRequest(username, localTunnel);
                } else {
                    throw new ServerOperationFailedException("The specified remote user is not currently available");
                }
            } else {
                throw new ServerOperationFailedException("The client is not friends with the specified remote user");
            }

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not establish a connection to the specified remote user");

        } catch (DaoOperationException | RemoteException e) {
            System.err.println("Could not establish a connection to the specified remote user");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not establish a connection to the specified remote user");

        } finally {
            // If the previous steps have been completed successfully, the operation will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {

        Connection connection = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            // If the user that sends the request has already received one from the other user, they become friends
            // instantly
            if (this.usersDAO.checkUsersStatus(connection, username, remoteUser,
                    StatusTypeUserDAO.FRIENDSHIP_RECEIVED)) {

                this.usersDAO.updateUsersStatus(connection, username, remoteUser,
                        StatusTypeUserDAO.ACCEPTED_FRIENDSHIP);

                // The remote user is notified if he is currently online
                notifyOnlineUserAboutUserStatus(remoteUser, username, StatusType.ONLINE);
            }

            // If a request has already been sent, or if the users are already friends, the method will thrown an
            // exception
            else {
                this.usersDAO.updateUsersStatus(connection, username, remoteUser, StatusTypeUserDAO.FRIENDSHIP_SENT);

                // The remote user is notified if he is currently online (StatusType will always be ONLINE as the
                // client has just accepted the friendship request)
                notifyOnlineUserAboutUserStatus(remoteUser, username, StatusType.FRIENDSHIP_RECEIVED);
            }

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not send a friendship request to the specified user");

        } catch (DaoOperationException e) {
            System.err.println("Could not send a friendship request to the specified user");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not send a friendship request to the specified user");

        } finally {
            // If the previous steps have been completed successfully, the operation will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {

        boolean online = false;
        Connection connection = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            // The user that performs the operation must have already received a friendship request from the other user
            if (this.usersDAO.checkUsersStatus(connection, username, remoteUser,
                    StatusTypeUserDAO.FRIENDSHIP_RECEIVED)) {

                this.usersDAO.updateUsersStatus(connection, username, remoteUser,
                        StatusTypeUserDAO.ACCEPTED_FRIENDSHIP);

                // The remote user is notified if he is currently online (StatusType will always be ONLINE as the
                // client has just accepted the friendship request)
                notifyOnlineUserAboutUserStatus(remoteUser, username, StatusType.ONLINE);

                // The user that accepts the requested is informed about his new friend being currently available or
                // not
                online = this.serverNotificationsListeners.containsKey(remoteUser);

            } else {
                throw new ServerOperationFailedException("The specified remote user has not sent a friendship " +
                        "request");

            }

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not accept a friendship request from the specified user");

        } catch (DaoOperationException e) {
            System.err.println("Could not accept a friendship request from the specified user");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not accept a friendship request from the specified user");

        } finally {
            // If the previous steps have been completed successfully, the operation will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }

        return online;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {

        Connection connection = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            // The user that performs the operation must have already received a friendship request from the other
            // user. Otherwise, an existing relationship could be erased
            if (this.usersDAO.checkUsersStatus(connection, username, remoteUser,
                    StatusTypeUserDAO.FRIENDSHIP_RECEIVED)) {

                this.usersDAO.updateUsersStatus(connection, username, remoteUser,
                        StatusTypeUserDAO.NOT_RELATED);

                // The remote user is notified if he is currently online
                notifyOnlineUserAboutUserStatus(remoteUser, username, StatusType.NOT_RELATED);

            } else {
                throw new ServerOperationFailedException("The specified remote user has not sent a friendship " +
                        "request");

            }

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not reject a friendship request from the specified user");

        } catch (DaoOperationException e) {
            System.err.println("Could not reject a friendship request from the specified user");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not reject a friendship request from the specified user");

        } finally {
            // If the previous steps have been completed successfully, the operation will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endFriendship(UserToken token, String remoteUser) throws ServerOperationFailedException {

        Connection connection = null;

        try {
            String username = this.currentSessionsManager.getUserFromSession(token);

            connection = this.usersDAO.getConnection();

            // The users must be friends
            if (this.usersDAO.checkUsersStatus(connection, username, remoteUser,
                    StatusTypeUserDAO.ACCEPTED_FRIENDSHIP)) {

                this.usersDAO.updateUsersStatus(connection, username, remoteUser,
                        StatusTypeUserDAO.NOT_RELATED);

                // The remote user is notified if he is currently online
                notifyOnlineUserAboutUserStatus(remoteUser, username, StatusType.NOT_RELATED);

            } else {
                throw new ServerOperationFailedException("The specified users are not friends currently");

            }

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not end a friendship between the two specified users");

        } catch (DaoOperationException e) {
            System.err.println("Could not end a friendship between the two specified users");
            e.printStackTrace();
            throw new ServerOperationFailedException("Could not end a friendship between the two specified users");

        } finally {
            // If the previous steps have been completed successfully, the operation will seem successful to the
            // client even if the connection cannot be closed
            closeDaoConnection(connection);
        }
    }

    /**
     * Requests to {@link #usersDAO} that the specified connection is closed.
     *
     * @param connection connection is going to be closed; it must have been previously retrieved from
     *                   {@link #usersDAO}.
     */
    private void closeDaoConnection(Connection connection) {

        try {
            if (connection != null) {
                this.usersDAO.freeConnection(connection);
            }

        } catch (DaoOperationException e) {
            System.err.println("Could not close an opened connection to the database");
            e.printStackTrace();
        }
    }

    /**
     * Returns the corresponding user for the given {@link UserToken}, as long as it is legitimate.
     *
     * @param token the {@link UserToken}.
     * @return username claimed by the token if the latter is legitimate; otherwise, it will return null.
     */
    private String usernameFromToken(UserToken token) {

        String username = null;

        try {
            username = this.currentSessionsManager.getUserFromSession(token);

        } catch (InvalidUserTokenException e) {
            System.err.println("An illegitimate token has been received");
            e.printStackTrace();
        }

        return username;
    }

    /**
     * Converts the given collection of {@link RemoteUserDao} to a collection of {@link RemoteUser}.
     *
     * @param remoteUsersDao collection that contains the {@link RemoteUserDao} instances.
     * @return collection that contains the equivalent {@link RemoteUser} instances.
     */
    private List<RemoteUser> remoteUserFromRemoteUserDao(List<RemoteUserDao> remoteUsersDao) {

        List<RemoteUser> result = new ArrayList<>();

        for (RemoteUserDao remoteUserDao : remoteUsersDao) {
            result.add(new RemoteUser(remoteUserDao.getUsername(),
                    statusTypeFromStatusTypeUserDao(remoteUserDao.getStatus(), remoteUserDao.getUsername())));
        }

        return result;
    }

    /**
     * Converts the given {@link StatusTypeUserDAO} to its corresponding {@link StatusType}.
     *
     * @param status     the {@link StatusTypeUserDAO}.
     * @param remoteUser name by which the remote user for which the status is being converted; it is needed as {@link
     *                   StatusType} differentiates between online and offline current friends.
     * @return the equivalent {@link StatusType}.
     */
    private StatusType statusTypeFromStatusTypeUserDao(StatusTypeUserDAO status, String remoteUser) {

        StatusType result = null;

        switch (status) {
            case ACCEPTED_FRIENDSHIP:
                result = this.serverNotificationsListeners.containsKey(remoteUser) ? StatusType.ONLINE :
                        StatusType.DISCONNECTED;
                break;
            case FRIENDSHIP_SENT:
                result = StatusType.FRIENDSHIP_SENT;
                break;
            case FRIENDSHIP_RECEIVED:
                result = StatusType.FRIENDSHIP_RECEIVED;
                break;
            case NOT_RELATED:
                result = StatusType.NOT_RELATED;
                break;
        }

        return result;
    }

    /**
     * Notifies the specified remote user about an update in the relation that he maintains with another user.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @param user       name by which the other user can be identified.
     * @param status     current status between the two users.
     */
    private void notifyOnlineUserAboutUserStatus(String remoteUser, String user, StatusType status) {

        // The remote user must be online to receive the notification
        IServerNotificationsListener listener = this.serverNotificationsListeners.get(remoteUser);

        if (listener != null) {
            try {
                listener.updateRemoteUserStatus(new RemoteUser(user, status));

            } catch (RemoteException e) {
                System.err.println("A remote user that is online could not be successfully notified");
                e.printStackTrace();
            }
        }
    }
}
