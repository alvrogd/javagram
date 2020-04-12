package com.goldardieste.javagram.common;

/**
 * This class contains all the possible states that a {@link RemoteUser} may be in.
 */
public enum StatusType {

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
     * The remote user has rejected a friendship request.
     */
    FRIENDSHIP_REJECTED,

    /**
     * The remote has decided to end the friendship with the local user.
     */
    FRIENDSHIP_ENDED
}
