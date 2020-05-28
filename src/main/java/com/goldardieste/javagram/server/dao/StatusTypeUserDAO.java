package com.goldardieste.javagram.server.dao;

import com.goldardieste.javagram.common.datacontainers.RemoteUser;
import com.goldardieste.javagram.common.StatusType;

/**
 * This class contains all the possible states that a {@link RemoteUser} may be in and that can be recognized using the
 * available information in the user's database.
 */
public enum StatusTypeUserDAO {

    /* ----- Enums ----- */

    /**
     * The remote user is currently a friend.
     */
    ACCEPTED_FRIENDSHIP(0),

    /**
     * The remote user was sent a friendship request.
     */
    FRIENDSHIP_SENT(1),

    /**
     * A friendship request from the remote user has been received.
     */
    FRIENDSHIP_RECEIVED(1),

    /**
     * The two users are not related at all. It will be represented in the database as the absence of a relation.
     */
    NOT_RELATED(-1);


    /* ----- Attributes ----- */

    /**
     * Value which represents the {@link StatusTypeUserDAO} in the database.
     */
    private final int daoValue;


    /* ----- Constructor ----- */

    /**
     * Initializes a prefixed {@link StatusTypeUserDAO}.
     *
     * @param daoValue value for {@link #daoValue}.
     */
    private StatusTypeUserDAO(int daoValue) {
        this.daoValue = daoValue;
    }


    /* ----- Getters ----- */

    /**
     * Retrieves the current {@link #daoValue}.
     *
     * @return {@link #daoValue}.
     */
    public int getDaoValue() {
        return daoValue;
    }


    /* ----- Methods ----- */

    /**
     * Converts the given {@link StatusType} to its corresponding {@link StatusTypeUserDAO}.
     *
     * @param statusType the {@link StatusType}.
     * @return the equivalent {@link StatusTypeUserDAO}.
     */
    public static StatusTypeUserDAO statusTypeUserDaoFromStatusType(StatusType statusType) {

        StatusTypeUserDAO result = null;

        switch (statusType) {
            case ONLINE:
            case DISCONNECTED:
                result = StatusTypeUserDAO.ACCEPTED_FRIENDSHIP;
                break;
            case FRIENDSHIP_SENT:
                result = StatusTypeUserDAO.FRIENDSHIP_SENT;
                break;
            case FRIENDSHIP_RECEIVED:
                result = StatusTypeUserDAO.FRIENDSHIP_RECEIVED;
                break;
            case NOT_RELATED:
                result = StatusTypeUserDAO.NOT_RELATED;
                break;
        }

        return result;
    }
}
