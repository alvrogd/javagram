package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.RemoteUser;

import java.util.List;

/**
 * This class will hold the duty of accessing and updating the server's database where all the information about the
 * users is stored.
 */
public class UsersDAO {

    /* ----- Attributes ----- */

    /**
     * URL where the database can be located.
     */
    private final String jdbcURL;

    /**
     * Port where the database can be located.
     */
    private final int jdbcPort;

    /**
     * Name by which the database can be located.
     */
    private final String jdbcDatabase;


    /* ----- Constructor ----- */

    /**
     * Initializes an {@link UsersDAO} which establishes a connection to the specified database.
     *
     * @param jdbcURL      database's URL.
     * @param jdbcPort     database's port.
     * @param jdbcDatabase database's name.
     */
    public UsersDAO(String jdbcURL, int jdbcPort, String jdbcDatabase) {
        this.jdbcURL = jdbcURL;
        this.jdbcPort = jdbcPort;
        this.jdbcDatabase = jdbcDatabase;

        connectToDatabase();
    }


    /* ----- Methods ----- */
    // TODO implement all methods

    /**
     * Tries to establish a connection to the specified databased via {@link #jdbcURL}, {@link #jdbcPort} and
     * {@link #jdbcDatabase}.
     */
    private void connectToDatabase() {

    }

    /**
     * Checks if a certain user is registered in the database.
     *
     * @param username name by which the user can be identified.
     * @return if the user exists in the database.
     */
    public boolean existsUser(String username) {

        return true;
    }

    /**
     * Registers a new user in the database.
     *
     * @param username     name by which the user will be identified.
     * @param passwordHash hash of the user's password.
     */
    public void createUser(String username, String passwordHash) {

    }

    /**
     * Verifies if the credentials given for a certain user are valid or not. That is, the given password's hash is
     * checked against the one that was stored in the database when creating the user.
     *
     * @param username     name of the user whose credentials will be checked.
     * @param passwordHash hash of the password that will be compared to the user's credentials.
     * @return if the given credentials match the actual user's credentials.
     */
    public boolean verifyUserCredentials(String username, String passwordHash) {

        return true;
    }

    /**
     * Retrieves a collection that contains all the users that the specified one is related to in any way. That is, the
     * retrieved remote users may be current friends of the local one, they may have sent him a friendship request, or
     * the may also received a friendship request from him.
     *
     * @param username name of the user whose related remote users will be retrieved.
     * @return all the related users that have been found.
     */
    public List<RemoteUser> retrieveFriends(String username) {

        return null;
    }

    /**
     * Retrieves a collection that contains all the users that the specified one is related to in a way determined by
     * the given state.
     *
     * @param username name of the user whose related remote users will be retrieved.
     * @param status   status in which the remote users will be in relation to the other user.
     * @return all the related users that have been found.
     */
    public List<RemoteUser> retrieveFriends(String username, StatusTypeUserDAO status) {

        return null;
    }

    /**
     * Checks if the specified relation exists between the two given users.
     * <p>
     * When {@link StatusTypeUserDAO} is "FRIENDSHIP_SENT", it means that the first user has sent a friendship request
     * to the second one. If it is "FRIENDSHIP_RECEIVED", it means that the first user has received a friendship
     * request from the second one.
     *
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @param status     which relation will be checked between the two users.
     * @return if the specified status is the actual one stored in the database.
     */
    public boolean checkUsersStatus(String firstUser, String secondUser, StatusTypeUserDAO status) {

        return true;
    }

    /**
     * Updates the relation between the two given users so that it matches the specified one.
     * <p>
     * When {@link StatusTypeUserDAO} is "FRIENDSHIP_SENT", it means that the first user has sent a friendship request
     * to the second one. If it is "FRIENDSHIP_RECEIVED", it means that the first user has received a friendship
     * request from the second one.
     *
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @param status     which relation will be set between the two users.
     */
    public void updateUsersStatus(String firstUser, String secondUser, StatusTypeUserDAO status) {

    }
}
