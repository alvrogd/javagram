package com.goldardieste.javagram.common;

import java.io.Serializable;

/**
 * This class represents an instance of another user of the service. For instance, a certain client will receive a
 * collection of {@link RemoteUser} as a representation of its friends.
 */
public class RemoteUser implements Serializable {

    /* ----- Attributes ----- */

    /**
     * Name by which the user can be identified.
     */
    private final String username;

    /**
     * State in which the remote user will be seen by the local user.
     */
    private StatusType status;


    /* ----- Constructor ----- */

    /**
     * Initializes a instance of {@link RemoteUser} using the given data.
     *
     * @param username remote user's name.
     * @param status   remote user's status.
     */
    public RemoteUser(String username, StatusType status) {
        this.username = username;
        this.status = status;
    }


    /* ----- Getters & setters ----- */

    /**
     * Retrieves the current {@link #username}.
     *
     * @return {@link #username}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Retrieves the current {@link #status}.
     *
     * @return {@link #status}.
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Updates the value of {@link #status}.
     *
     * @param status new {@link #status}.
     */
    public void setStatus(StatusType status) {
        this.status = status;
    }
}
