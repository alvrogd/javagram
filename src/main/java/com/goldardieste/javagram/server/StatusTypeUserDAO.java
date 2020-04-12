package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.RemoteUser;

/**
 * This class contains all the possible states that a {@link RemoteUser} may be in and that can be recognized using the
 * available information in the user's database.
 */
public enum StatusTypeUserDAO {

    /**
     * The remote user is an actual friend.
     */
    ACCEPTED_FRIENDSHIP,

    /**
     * The remote user was sent a friendship request.
     */
    FRIENDSHIP_SENT,

    /**
     * A friendship request from the remote user has been received.
     */
    FRIENDSHIP_RECEIVED
}
