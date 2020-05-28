package com.goldardieste.javagram.desktopapp.identifieduser;

import com.goldardieste.javagram.common.datacontainers.RemoteUser;
import com.goldardieste.javagram.common.StatusType;

/**
 * This class contains all the possible filters that determine which {@link RemoteUser}s can be rendered as user
 * entries in the desktop app.
 */
public enum UserEntriesFilter {

    /* ----- Enums ----- */

    /**
     * Shows online and offline friends.
     */
    CURRENT_FRIENDS,

    /**
     * Shows users that have been sent a friendship request to the client, or that have sent one to the client.
     */
    REQUESTS,

    /**
     * Shows all users.
     */
    NONE;


    /* ----- Methods ----- */

    /**
     * Checks if the given {@link StatusType} is allowed or not by this {@link UserEntriesFilter}.
     *
     * @param statusType the {@link StatusType}.
     * @return if it is allowed.
     */
    public boolean isStatusTypeAllowed(StatusType statusType) {

        boolean allowed = false;

        switch (this) {

            case CURRENT_FRIENDS:
                allowed = statusType.equals(StatusType.ONLINE) || statusType.equals(StatusType.DISCONNECTED);
                break;

            case REQUESTS:
                allowed = statusType.equals(StatusType.FRIENDSHIP_SENT) ||
                        statusType.equals(StatusType.FRIENDSHIP_RECEIVED);
                break;

            case NONE:
                allowed = !statusType.equals(StatusType.NOT_RELATED);
                break;
        }

        return allowed;
    }
}
