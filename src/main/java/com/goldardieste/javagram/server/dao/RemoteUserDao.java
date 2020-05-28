package com.goldardieste.javagram.server.dao;

import com.goldardieste.javagram.common.datacontainers.RemoteUser;

import java.util.Objects;

public class RemoteUserDao {

    /* ----- Attributes ----- */

    /**
     * Name by which the user can be identified.
     */
    private final String username;

    /**
     * State in which the remote user will be seen by the local user.
     */
    private StatusTypeUserDAO status;


    /* ----- Constructor ----- */

    /**
     * Initializes a instance of {@link RemoteUser} using the given data.
     *
     * @param username remote user's name.
     * @param status   remote user's status.
     */
    public RemoteUserDao(String username, StatusTypeUserDAO status) {
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
    public StatusTypeUserDAO getStatus() {
        return status;
    }

    /**
     * Updates the value of {@link #status}.
     *
     * @param status new {@link #status}.
     */
    public void setStatus(StatusTypeUserDAO status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteUserDao that = (RemoteUserDao) o;
        return username.equals(that.username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
