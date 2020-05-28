package com.goldardieste.javagram.common.datacontainers;

import java.io.Serializable;

/**
 * This class will store all the information that the server needs to identify the identity of a certain client.
 */
public class UserToken implements Serializable {

    /* ----- Attributes ----- */

    /**
     * As of now, the content of the user token will just be a JSON Web Token.
     */
    private final String content;


    /* ----- Constructor ----- */

    /**
     * Creates a new {@link UserToken} that will contain the specified information.
     *
     * @param content information that will allow identifying a certain user.
     */
    public UserToken(String content) {
        this.content = content;
    }


    /* ----- Getters ----- */

    /**
     * Retrieves the current {@link #content}.
     *
     * @return {@link #content}.
     */
    public String getContent() {
        return content;
    }
}
