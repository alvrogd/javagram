package com.goldardieste.javagram.client.exposed;

import com.goldardieste.javagram.client.unexposed.CurrentUserFacade;
import com.goldardieste.javagram.common.datacontainers.RemoteUser;

/**
 * When any addition/update about a {@link RemoteUser} occurs in {@link CurrentUserFacade}, a consumer will probably be
 * interested in being notified about it. This consumer must implement this interface, so that, if it is available,
 * every local change about the currently stored {@link RemoteUser} will also be
 * forwarded to it.
 */
public interface RemoteUsersListener {

    /* ----- Methods -----*/

    /**
     * Due to a change in the currently stored {@link RemoteUser}s in {@link CurrentUserFacade}, the listener is
     * notified about it. It will receive an instance that represents the new or modified {@link RemoteUser}, so that
     * it handles it as it sees fit.
     *
     * @param remoteUser the {@link RemoteUser} instance.
     */
    void forwardRemoteUserChange(RemoteUser remoteUser);

    /**
     * Due to a removal if a {@link RemoteUser} in {@link CurrentUserFacade}, the listener is notified about it. It
     * will receive an instance that represents the deleted {@link RemoteUser}, so that it handles it as it sees fit.
     *
     * @param remoteUser the {@link RemoteUser} instance.
     */
    void forwardRemoteUserDeletion(RemoteUser remoteUser);
}
