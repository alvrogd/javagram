package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.UserToken;

import java.util.HashMap;
import java.util.Map;

/**
 * This class will take the responsibility for creating, verifying and deleting the session
 * ({@link com.goldardieste.javagram.common.UserToken}) that all clients must use so that the server can verify their
 * identity when requesting any operation.
 */
public class CurrentSessionsManager {

    /* ----- Attributes ----- */

    /**
     * Each session provided to an user will have a secret associated (that is, a random String) which is needed to
     * compute the token's signature.
     * <p>
     * Key -> username.
     * Value -> secret.
     */
    private final Map<String, String> secrets;


    /* ----- Constructor ----- */

    /**
     * Initializes an instance of {@link CurrentSessionsManager}.
     */
    public CurrentSessionsManager() {
        this.secrets = new HashMap<>();
    }


    /* ----- Methods ----- */
    // TODO implement all methods
    // TODO terminateSession and getUserFromSession will use getUsernameFromTokenContent to retrieve the username and after that they will verify the token validity

    /**
     * Initializes a session for the specified user.
     *
     * @param username name by which the user can be identified.
     * @return {@link UserToken} that represents the newly created session.
     */
    public UserToken initiateSession(String username) {

        return null;
    }

    /**
     * Terminates the session identified by the given {@link UserToken}.
     *
     * @param token identifies the session that will be terminated.
     */
    public void terminateSession(UserToken token) {

    }

    /**
     * Retrieves the user that is identified by the given {@link UserToken}.
     *
     * @param token identifies the user that will be retrieved.
     * @return username of the identified user.
     */
    public String getUserFromSession(UserToken token) {

        return null;
    }

    /**
     * Parses the content of the given {@link UserToken} to retrieve the username of the user that the token claims to
     * belong to.
     *
     * @param token token whose content will be parsed.
     * @return username that the token contains.
     */
    private String getUsernameFromTokenContent(UserToken token) {

        return null;
    }

    /**
     * Checks if the given token is an actual token that was previously generated by the server. Otherwise, the token
     * will not be valid as it must have been modified outside the server.
     *
     * @param token token which will be verified.
     * @return if the token is legit.
     */
    private boolean verifyToken(UserToken token) {

        return true;
    }
}
