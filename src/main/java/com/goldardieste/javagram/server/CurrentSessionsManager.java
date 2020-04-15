package com.goldardieste.javagram.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.goldardieste.javagram.common.UserToken;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * How many bytes in length will each secret be.
     */
    private final static int SECRET_BYTE_COUNT = 2048;


    /* ----- Constructor ----- */

    /**
     * Initializes an instance of {@link CurrentSessionsManager}.
     */
    public CurrentSessionsManager() {
        this.secrets = new ConcurrentHashMap<>();
    }


    /* ----- Methods ----- */

    // TODO concurrency compromises have been made to enhance the performance under highly concurrent scenarios

    /**
     * Initializes a session for the specified user. If any session was previously opened for the user, the latter also
     * gets invalidated.
     *
     * @param username name by which the user can be identified.
     * @return {@link UserToken} that represents the newly created session.
     */
    public UserToken initiateSession(String username) {

        // Each user will have his own random secret
        String secret = CryptographicServices.generateRandomStringBase64(CurrentSessionsManager.SECRET_BYTE_COUNT);
        // COMPROMISE: if multiple requests to initiate a session using the same user arrive at the same time, the
        // valid one will be the last that is put in the map even if it is not the one that was made in the last place,
        // in contrast to what the server promises in its interface
        this.secrets.put(username, secret);

        // All JWTs will be signed and verified using HMAC512
        Algorithm algorithm = Algorithm.HMAC512(secret);
        String token = JWT.create()
                .withClaim("username", username)
                .withIssuer("javagram_server")
                .sign(algorithm);

        return new UserToken(token);
    }

    /**
     * Terminates the session identified by the given {@link UserToken}.
     *
     * @param token identifies the session that will be terminated.
     * @throws InvalidUserTokenException if the given {@link UserToken} is not valid.
     * @return username corresponding to the session that has been closed.
     */
    public String terminateSession(UserToken token) throws InvalidUserTokenException {

        String usernameClaimed = getUsernameFromTokenContent(token);

        if (!verifyToken(usernameClaimed, token)) {
            throw new InvalidUserTokenException();
        } else {
            // As the user and its secret are removed from memory, the token will no longer be valid because (1) no
            // secret will be found when verifying the token's signature, therefore preventing the validation process
            // to complete, and (2) even if the user logs in again, a new secret will be generated (which is nearly
            // guaranteed to be different), and the validation process will fail likewise

            // COMPROMISE: no check will be performed to determine if the given token has been invalidated while
            // verifying it
            this.secrets.remove(usernameClaimed);
        }

        return usernameClaimed;
    }

    /**
     * Retrieves the user that is identified by the given {@link UserToken}.
     *
     * @param token identifies the user that will be retrieved.
     * @return username of the identified user.
     * @throws InvalidUserTokenException if the given {@link UserToken} is not valid.
     */
    public String getUserFromSession(UserToken token) throws InvalidUserTokenException {

        String usernameClaimed = getUsernameFromTokenContent(token);

        if (!verifyToken(usernameClaimed, token)) {
            throw new InvalidUserTokenException();
        } else {
            // COMPROMISE: no check will be performed to determine if the given token has been invalidated while
            // verifying it
            return usernameClaimed;
        }
    }

    /**
     * Parses the content of the given {@link UserToken} to retrieve the username of the user that the token claims to
     * belong to.
     *
     * @param token token whose content will be parsed.
     * @return username that the token contains.
     */
    private String getUsernameFromTokenContent(UserToken token) {

        // The token needs to be decoded before retrieving any data
        DecodedJWT decodedToken = JWT.decode(token.getContent());

        return decodedToken.getClaim("username").asString();
    }

    /**
     * Checks if the given token is an actual token that was previously generated by the server. Otherwise, the token
     * will not be valid as it must have been modified outside the server.
     *
     * @param username name that identifies the Javagram user that the token claims to belong to.
     * @param token    token which will be verified.
     * @return if the token is legitimate.
     */
    private boolean verifyToken(String username, UserToken token) {

        boolean valid = false;

        // 1. If the token is valid, the user that it claims to belong to must have a secret associated
        String secret = this.secrets.get(username);

        if (secret != null) {

            // 2. If the user has actually initiated a session, the token's contents must be verified

            // All JWTs will be signed and verified using HMAC512
            Algorithm algorithm = Algorithm.HMAC512(secret);

            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("username", username)
                    .withIssuer("javagram_server")
                    .build();

            try {
                verifier.verify(token.getContent());

                // If all the conditions to verify the token are met
                valid = true;
            } catch (JWTVerificationException ignored) {
            }
        }

        // COMPROMISE: as the secret is retrieved at the beginning of the method, a new session for the token's user
        // could be opened while verifying it, therefore invalidating the token, but no checks are made
        return valid;
    }
}
