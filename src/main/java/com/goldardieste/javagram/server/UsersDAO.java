package com.goldardieste.javagram.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

/**
 * This class will hold the duty of accessing and updating the server's database where all the information about the
 * users is stored.
 */
public class UsersDAO {

    /* ----- Attributes ----- */

    /**
     * Identifier of the driver that will be used to contact the database.
     */
    private final String jdbcDriver;

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

    /**
     * Username to log into an account in the database.
     */
    public final String jdbcUsername;

    /**
     * Password to log into an account in the database.
     */
    public final String jdbcPassword;

    /**
     * Contains all the JDBC configuration that will determine the properties of all the connections to the database
     * returned by {@link #hikariDataSource}.
     */
    private final HikariConfig hikariConfig;

    /**
     * Provides connections to the database on demand.
     */
    private final HikariDataSource hikariDataSource;


    /* ----- Constructor ----- */

    /**
     * Initializes an {@link UsersDAO} which establishes a connection to the specified database.
     *
     * @param jdbcDriver   identifier of the driver that allows contacting the database.
     * @param jdbcURL      database's URL.
     * @param jdbcPort     database's port.
     * @param jdbcDatabase database's name.
     * @param jdbcUsername username of a valid account in the database.
     * @param jdbcPassword password of a valid account in the database.
     * @throws IllegalStateException if the connection with the database cannot be successfully established.
     */
    public UsersDAO(String jdbcDriver, String jdbcURL, int jdbcPort, String jdbcDatabase, String jdbcUsername, String
            jdbcPassword) throws IllegalStateException {

        this.jdbcDriver = jdbcDriver;
        this.jdbcURL = jdbcURL;
        this.jdbcPort = jdbcPort;
        this.jdbcDatabase = jdbcDatabase;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;

        this.hikariConfig = new HikariConfig();
        this.hikariConfig.setJdbcUrl(this.jdbcURL + ":" + this.jdbcPort + "/" + this.jdbcDatabase);
        this.hikariConfig.setUsername(this.jdbcUsername);
        this.hikariConfig.setPassword(this.jdbcPassword);

        this.hikariConfig.setAutoCommit(false);
        this.hikariConfig.setDriverClassName(this.jdbcDriver);
        this.hikariConfig.setTransactionIsolation("TRANSACTION_REPEATABLE_READ");

        this.hikariDataSource = new HikariDataSource(this.hikariConfig);
    }


    /* ----- Methods ----- */

    /**
     * Returns a connection to the database that a single java thread may use to perform operations in it. All
     * retrieved connections must be closed by their corresponding thread when they are no longer required, using
     * the {@link #freeConnection(Connection)} method.
     *
     * @return connection to the database.
     * @throws DaoOperationException if a connection to the database cannot be established.
     */
    public Connection getConnection() throws DaoOperationException {

        try {
            return this.hikariDataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Could not establish a connection with the database");
            throw new DaoOperationException(e);
        }
    }

    /**
     * Closes the given connection to the database.
     *
     * @param connection connection that is going to be closed.
     * @throws DaoOperationException if the given operation cannot be closed.
     */
    public void freeConnection(Connection connection) throws DaoOperationException {

        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Could close the given connection to the database");
            throw new DaoOperationException(e);
        }
    }

    /**
     * Checks if a certain user is registered in the database.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param username   name by which the user can be identified.
     * @return if the user exists in the database.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    public boolean existsUser(Connection connection, String username) throws DaoOperationException {

        String statement = "SELECT * FROM users WHERE username=?";
        PreparedStatement stm = null;
        ResultSet resultSet = null;

        boolean found = false;

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, username);

            resultSet = stm.executeQuery();

            // If a match has been found
            found = resultSet.next();

        } catch (SQLException e) {
            System.err.println("Could not check if the specified user is registered");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (resultSet != null) resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return found;
    }

    /**
     * Registers a new user in the database.
     *
     * @param connection   connection to the database through which the operations are performed.
     * @param username     name by which the user will be identified.
     * @param passwordHash hash of the user's password.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    public void createUser(Connection connection, String username, String passwordHash) throws DaoOperationException {

        // If the user already exists, a conflict due to the PK constraint will arise
        String statement =
                "INSERT INTO users(username, password_hash, password_salt) " +
                "VALUES(?, ?, ?)";
        PreparedStatement stm = null;

        try {

            stm = connection.prepareStatement(statement);

            // Each user's password will have its own salt, and it will be secured using its salt
            byte[] passwordSalt = CryptographicServices.generatePasswordSalt();
            byte[] hash = CryptographicServices.hashString(passwordHash, passwordSalt);

            // Data is stored in the database using Base64
            stm.setString(1, username);
            stm.setString(2, CryptographicServices.StringBase64FromBytes(hash));
            stm.setString(3, CryptographicServices.StringBase64FromBytes(passwordSalt));

            stm.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            System.err.println("Could not register the specified user");
            throw new DaoOperationException(e);

        } finally {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Verifies if the credentials given for a certain user are valid or not. That is, the given password's hash is
     * checked against the one that was stored in the database when creating the user.
     *
     * @param connection   connection to the database through which the operations are performed.
     * @param username     name of the user whose credentials will be checked.
     * @param passwordHash hash of the password that will be compared to the user's credentials.
     * @return if the given credentials match the actual user's credentials.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    public boolean verifyUserCredentials(Connection connection, String username, String passwordHash) throws
            DaoOperationException {

        String statement = "SELECT password_hash, password_salt FROM users WHERE username=?";
        PreparedStatement stm = null;
        ResultSet result = null;

        boolean valid = false;

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, username);

            result = stm.executeQuery();

            // If the specified user is registered, the given data is checked against his; the stored hash must equal
            // the resulting hash using the given password and the generated hash when registering the user
            if (result.next()) {

                String databaseHash = result.getString(1);
                String databaseSalt = result.getString(2);

                valid = Arrays.equals(CryptographicServices.BytesFromStringBase64(databaseHash),
                        CryptographicServices.hashString(passwordHash,
                                CryptographicServices.BytesFromStringBase64(databaseSalt)));
            }

        } catch (SQLException e) {
            System.err.println("Could not check if the user credentials are valid");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (result != null) result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return valid;
    }

    /**
     * Retrieves a collection that contains all the users that the specified one is related to in any way. That is, the
     * retrieved remote users may be current friends of the local one, they may have sent him a friendship request, or
     * the may also received a friendship request from him.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param username   name of the user whose related remote users will be retrieved.
     * @return all the related users that have been found.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    public List<RemoteUserDao> retrieveFriends(Connection connection, String username) throws DaoOperationException {

        List<RemoteUserDao> result = new ArrayList<>();

        result.addAll(retrieveFriends(connection, username, StatusTypeUserDAO.ACCEPTED_FRIENDSHIP));
        result.addAll(retrieveFriends(connection, username, StatusTypeUserDAO.FRIENDSHIP_SENT));
        result.addAll(retrieveFriends(connection, username, StatusTypeUserDAO.FRIENDSHIP_RECEIVED));

        return result;
    }

    /**
     * Retrieves a collection that contains all the users that the specified one is related to in a way determined by
     * the given state.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param username   name of the user whose related remote users will be retrieved.
     * @param status     status in which the remote users will be in relation to the other user.
     * @return all the related users that have been found.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    public List<RemoteUserDao> retrieveFriends(Connection connection, String username, StatusTypeUserDAO status) throws
            DaoOperationException {

        List<RemoteUserDao> result = null;

        switch (status) {
            case ACCEPTED_FRIENDSHIP:
                result = retrieveCurrentFriends(connection, username);
                break;
            case FRIENDSHIP_SENT:
                result = retrieveFriendsSentRequest(connection, username);
                break;
            case FRIENDSHIP_RECEIVED:
                result = retrieveFriendsReceivedRequest(connection, username);
                break;
        }

        return result;
    }

    /**
     * Retrieves a collection that contains all the users that the specified one is friends with.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param username   name of the user whose friends will be retrieved.
     * @return all the related users that have been found.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private List<RemoteUserDao> retrieveCurrentFriends(Connection connection, String username) throws
            DaoOperationException {

        // Once two users become friends, two relations between them are made, so that each user can be identified in
        // the relation being both a receiver and a sender
        String statement = "SELECT receiver FROM have_relation WHERE sender=? AND status=" +
                StatusTypeUserDAO.ACCEPTED_FRIENDSHIP.getDaoValue();
        PreparedStatement stm = null;
        ResultSet resultSet = null;

        Set<RemoteUserDao> result = new HashSet<>();

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, username);

            resultSet = stm.executeQuery();

            while (resultSet.next()) {
                result.add(new RemoteUserDao(resultSet.getString(1),
                        StatusTypeUserDAO.ACCEPTED_FRIENDSHIP));
            }

        } catch (SQLException e) {
            System.err.println("Could not retrieve the current friends for the specified user");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (resultSet != null) resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Retrieves a collection that contains all the users that have received a friendship request from the specified
     * one.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param username   name of the user that has sent the friendship requests.
     * @return all the related users that have been found.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private List<RemoteUserDao> retrieveFriendsSentRequest(Connection connection, String username) throws
            DaoOperationException {

        String statement = "SELECT receiver FROM have_relation WHERE sender=? AND status=" +
                StatusTypeUserDAO.FRIENDSHIP_SENT.getDaoValue();
        PreparedStatement stm = null;
        ResultSet resultSet = null;

        Set<RemoteUserDao> result = new HashSet<>();

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, username);

            resultSet = stm.executeQuery();

            while (resultSet.next()) {
                result.add(new RemoteUserDao(resultSet.getString(1),
                        StatusTypeUserDAO.FRIENDSHIP_SENT));
            }

        } catch (SQLException e) {
            System.err.println("Could not retrieve the users to whom the specified one has requested a friendship");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (resultSet != null) resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Retrieves a collection that contains all the users that have sent a friendship request to the specified one.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param username   name of the user that has received the friendship requests.
     * @return all the related users that have been found.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private List<RemoteUserDao> retrieveFriendsReceivedRequest(Connection connection, String username) throws
            DaoOperationException {

        String statement = "SELECT sender FROM have_relation WHERE receiver=? AND status=" +
                StatusTypeUserDAO.FRIENDSHIP_SENT.getDaoValue();
        PreparedStatement stm = null;
        ResultSet resultSet = null;

        Set<RemoteUserDao> result = new HashSet<>();

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, username);

            resultSet = stm.executeQuery();

            while (resultSet.next()) {
                result.add(new RemoteUserDao(resultSet.getString(1),
                        StatusTypeUserDAO.FRIENDSHIP_RECEIVED));
            }

        } catch (SQLException e) {
            System.err.println("Could not retrieve the users from whom the specified one has received a friendship " +
                    "request");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (resultSet != null) resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Checks if the specified relation exists between the two given users.
     * <p>
     * When {@link StatusTypeUserDAO} is "FRIENDSHIP_SENT", it means that the first user has sent a friendship request
     * to the second one. If it is "FRIENDSHIP_RECEIVED", it means that the first user has received a friendship
     * request from the second one.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @param status     which relation will be checked between the two users.
     * @return if the specified status is the actual one stored in the database.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    public boolean checkUsersStatus(Connection connection, String firstUser, String secondUser, StatusTypeUserDAO
            status) throws DaoOperationException {

        boolean check = false;

        switch (status) {
            case ACCEPTED_FRIENDSHIP:
                check = checkUsersCurrentFriends(connection, firstUser, secondUser);
                break;
            case FRIENDSHIP_SENT:
                check = checkUsersSentRequest(connection, firstUser, secondUser);
                break;
            case FRIENDSHIP_RECEIVED:
                check = checkUsersSentRequest(connection, secondUser, firstUser);
                break;
            case NOT_RELATED:
                check = checkUsersNotRelated(connection, firstUser, secondUser);
                break;
        }

        return check;
    }

    /**
     * Checks if the two given users are current friends.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @return if the two given users are friends.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private boolean checkUsersCurrentFriends(Connection connection, String firstUser, String secondUser) throws
            DaoOperationException {

        // Once two users become friends, two relations between them are made, so that each user can be identified in
        // the relation being both a receiver and a sender
        String statement = "SELECT * FROM have_relation WHERE sender=? AND receiver=? AND status=" +
                StatusTypeUserDAO.ACCEPTED_FRIENDSHIP.getDaoValue();
        PreparedStatement stm = null;
        ResultSet resultSet = null;

        boolean valid = false;

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, firstUser);
            stm.setString(2, secondUser);

            resultSet = stm.executeQuery();

            valid = resultSet.next();

        } catch (SQLException e) {
            System.err.println("Could not check if the two specified users are friends");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (resultSet != null) resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return valid;
    }

    /**
     * Checks if the first user has sent a friendship request to the second one.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @return if the first user has sent a friendship request to the second one.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private boolean checkUsersSentRequest(Connection connection, String firstUser, String secondUser) throws
            DaoOperationException {

        String statement = "SELECT * FROM have_relation WHERE sender=? AND receiver=? AND status=" +
                StatusTypeUserDAO.FRIENDSHIP_SENT.getDaoValue();
        PreparedStatement stm = null;
        ResultSet resultSet = null;

        boolean valid = false;

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, firstUser);
            stm.setString(2, secondUser);

            resultSet = stm.executeQuery();

            valid = resultSet.next();

        } catch (SQLException e) {
            System.err.println("Could not check if the first user has sent a friendship request to the second one");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (resultSet != null) resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return valid;
    }

    /**
     * Checks if the two users are not related at all.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @return if the two users are not related in any way.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private boolean checkUsersNotRelated(Connection connection, String firstUser, String secondUser) throws
            DaoOperationException {

        String statement = "SELECT * FROM have_relation WHERE (receiver=? AND sender=?) OR (receiver=? AND sender=?)";
        PreparedStatement stm = null;
        ResultSet resultSet = null;

        boolean valid = false;

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, firstUser);
            stm.setString(2, secondUser);
            stm.setString(3, secondUser);
            stm.setString(4, firstUser);

            resultSet = stm.executeQuery();

            valid = resultSet.next();

        } catch (SQLException e) {
            System.err.println("Could not check if the two users are related in any way");
            throw new DaoOperationException(e);

        } finally {
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (resultSet != null) resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return valid;
    }

    /**
     * Updates the relation between the two given users so that it matches the specified one.
     * <p>
     * When {@link StatusTypeUserDAO} is "ACCEPTED_FRIENDSHIP", it means that the first user has accepted a request
     * that was previously sent by the second one.
     * <p>
     * When {@link StatusTypeUserDAO} is "FRIENDSHIP_SENT", it means that the first user has sent a friendship request
     * to the second one. If it is "FRIENDSHIP_RECEIVED", it means that the first user has received a friendship
     * request from the second one.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @param status     which relation will be set between the two users.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    public void updateUsersStatus(Connection connection, String firstUser, String secondUser, StatusTypeUserDAO status)
            throws DaoOperationException {

        switch (status) {
            case ACCEPTED_FRIENDSHIP:
                updateUsersStatusFriends(connection, firstUser, secondUser);
                break;
            case FRIENDSHIP_SENT:
                updateUsersStatusSentFriendship(connection, firstUser, secondUser);
                break;
            case FRIENDSHIP_RECEIVED:
                updateUsersStatusSentFriendship(connection, secondUser, firstUser);
                break;
        }
    }

    /**
     * Updates the relation between the two given users so that they are friends.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private void updateUsersStatusFriends(Connection connection, String firstUser, String secondUser) throws
            DaoOperationException {

        // 1. A new relation will be created from the user that accepts the request to the user that sent it

        // This transaction assures that, if a row already existed due to a petition being sent by the first user
        // because of race conditions even tough the other user had already sent a request, the "THEY ARE FRIENDS NOW"
        // statement is the one that remains nevertheless as the first user now wants to be friends with the second one
        String statementInsert =
                "INSERT INTO have_relation(sender, receiver, status) " +
                        "SELECT * " +
                        "FROM (SELECT ? AS sender, ? AS receiver, " + StatusTypeUserDAO.ACCEPTED_FRIENDSHIP.getDaoValue() + " AS status) AS tmp " +
                        "WHERE EXISTS(" +
                        "    SELECT * " +
                        "    FROM have_relation " +
                        "    WHERE sender=? AND receiver=? AND status=" + StatusTypeUserDAO.FRIENDSHIP_SENT.getDaoValue() +
                        ") " +
                        "ON CONFLICT ON CONSTRAINT have_relation_pk " +
                        "    DO UPDATE " +
                        "    SET status=" + StatusTypeUserDAO.ACCEPTED_FRIENDSHIP.getDaoValue();
        PreparedStatement stmInsert = null;

        // 2. The relation that previously pointed that the second user sent to the first one a request is now updated
        // to show that they are now friends

        // If not petition was made by the second user, the first statement will not have made any changes in the
        // database, and so this statement will not make any changes
        String statementUpdate = "UPDATE have_relation SET status=" +
                StatusTypeUserDAO.ACCEPTED_FRIENDSHIP.getDaoValue() + " WHERE sender=?, receiver=?";
        PreparedStatement stmUpdate = null;

        try {

            stmInsert = connection.prepareStatement(statementInsert);
            stmInsert.setString(1, firstUser);
            stmInsert.setString(2, secondUser);
            stmInsert.setString(3, secondUser);
            stmInsert.setString(4, firstUser);


            stmInsert.executeUpdate();

            stmUpdate = connection.prepareStatement(statementUpdate);
            stmUpdate.setString(1, secondUser);
            stmUpdate.setString(2, firstUser);

            stmUpdate.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            System.err.println("Could not create a friendship between the two specified users");
            throw new DaoOperationException(e);

        } finally {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stmInsert != null) stmInsert.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stmUpdate != null) stmUpdate.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the relation between the two given users so that the first one has requested a friendship to the second
     * one.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private void updateUsersStatusSentFriendship(Connection connection, String firstUser, String secondUser) throws
            DaoOperationException {

        // A new relation will be created from the user that sends the request to the user that receives it

        // If a row already existed:
        // - The petition could already be sent -> the statement would not change anything
        // - The friendship could already be established -> the statement would erase it if it executed
        String statement = "INSERT INTO have_relation(sender, receiver, status) VALUES (?, ?, " +
                StatusTypeUserDAO.ACCEPTED_FRIENDSHIP.getDaoValue() + ")";
        PreparedStatement stm = null;

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, firstUser);
            stm.setString(2, secondUser);

            stm.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            System.err.println("Could not register a new friendship request between the two specified users");
            throw new DaoOperationException(e);

        } finally {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes any existing relation between the two specified users.
     *
     * @param connection connection to the database through which the operations are performed.
     * @param firstUser  name by which the first user can be identified.
     * @param secondUser name by which the second user can be identified.
     * @throws DaoOperationException if the operation cannot be completed successfully.
     */
    private void updateUsersStatusNotRelated(Connection connection, String firstUser, String secondUser) throws
            DaoOperationException {

        // A new relation will be created from the user that sends the request to the user that receives it
        String statement = "DELETE FROM have_relation WHERE (receiver=? AND sender=?) OR (receiver=? AND sender=?)";
        PreparedStatement stm = null;

        try {

            stm = connection.prepareStatement(statement);
            stm.setString(1, firstUser);
            stm.setString(2, secondUser);
            stm.setString(3, secondUser);
            stm.setString(4, firstUser);

            stm.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            System.err.println("Could not delete any existing relation between the two specified users");
            throw new DaoOperationException(e);

        } finally {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stm != null) stm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
