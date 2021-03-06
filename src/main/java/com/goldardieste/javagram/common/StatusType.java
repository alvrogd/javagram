package com.goldardieste.javagram.common;

import com.goldardieste.javagram.common.datacontainers.RemoteUser;

/**
 * This class contains all the possible states that a {@link RemoteUser} may be in.
 */
public enum StatusType {

    /* ----- Enums ----- */

    /**
     * The remote user is online.
     */
    ONLINE,

    /**
     * The remote user is offline.
     */
    DISCONNECTED,

    /**
     * The remote user was sent a friendship request.
     */
    FRIENDSHIP_SENT,

    /**
     * A friendship request from the remote user has been received.
     */
    FRIENDSHIP_RECEIVED,

    /**
     * The two users are not related at all. It will be represented in the database as the absence of a relation.
     */
    NOT_RELATED;


    /* ----- Methods ----- */

    @Override
    public String toString() {

        String result = null;

        switch (this) {
            case ONLINE:
                result = "Online";
                break;
            case DISCONNECTED:
                result = "Disconnected";
                break;
            case FRIENDSHIP_SENT:
                result = "Friendship sent";
                break;
            case FRIENDSHIP_RECEIVED:
                result = "Friendship received";
                break;
            case NOT_RELATED:
                result = "Not related";
                break;
        }

        return result;
    }
}
