package com.goldardieste.javagram.server.dao;

/**
 * Encapsulates an exception in {@link UsersDAO} so that the corresponding {@link java.sql.SQLException} is not thrown
 * directly.
 */
public class DaoOperationException extends Exception {

    /* ----- Constructor ----- */

    /**
     * Creates a new {@link DaoOperationException} using the specified cause.
     *
     * @param e cause.
     */
    public DaoOperationException(Exception e) {
        super(e);
    }
}
