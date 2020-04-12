package com.goldardieste.javagram.client;

import com.goldardieste.javagram.common.IRemoteUserTunnel;
import com.goldardieste.javagram.common.RemoteUser;
import com.goldardieste.javagram.common.StatusType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class will manage all the remote users related to the local user in the client (friends, friendship
 * requests...), and it will also manage any tunnels that are needed to communicate with other users.
 */
public class CurrentUserFacade {

    /* ----- Attributes ----- */

    /**
     * Username that identifies the Javagram user who has opened a session in the client.
     */
    private final String username;

    /**
     * Contains all the {@link LocalUserTunnel} that the client has opened to that the intended remote users may
     * communicate with him.
     * <p>
     * Key -> username.
     * Value -> the {@link LocalUserTunnel} that the client has opened for him.
     */
    private final Map<String, LocalUserTunnel> openedTunnels;

    /**
     * Contains all the {@link IRemoteUserTunnel} that remote users have opened so that the client may communicate
     * with them.
     * <p>
     * Key -> username.
     * Value -> its corresponding {@link IRemoteUserTunnel}.
     */
    private final Map<String, IRemoteUserTunnel> receivedTunnels;

    /**
     * Registers all the remote Javagram users whose current status, in relation to the client, is known, sorting them
     * by their {@link StatusType}.
     * <p>
     * Key -> {@link StatusType}.
     * Value -> all the known {@link RemoteUser} that are in the state given by their key.
     */
    private final Map<StatusType, Set<RemoteUser>> statusTypeSetMap;

    /**
     * Contains all the remote Javagram users whose current status, in relation to the client, is known.
     * <p>
     * Key -> username.
     * Value -> {@link RemoteUser} that represents the remote user.
     */
    private final Map<String, RemoteUser> remoteUserMap;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link CurrentUserFacade} for the specified Javagram user.
     *
     * @param username name that identifies the Javagram user that the client is going to initiate a session in behalf
     *                 of.
     */
    public CurrentUserFacade(String username) {
        this.username = username;
        this.openedTunnels = null;
        this.receivedTunnels = null;
        this.remoteUserMap = null;
        this.statusTypeSetMap = null;
    }


    /* ----- Methods ----- */

    /**
     * Returns the specified {@link RemoteUser} stored in {@link #remoteUserMap}.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @return if the specified remote user is found, returns its stored {@link RemoteUser}.
     */
    public RemoteUser getRemoteUser(String remoteUser) {

        return null;
    }

    /**
     * Returns all the {@link RemoteUser} stored in {@link #remoteUserMap} whose statuses correspond to the specified
     * one.
     *
     * @param status {@link StatusType} that the all returned {@link RemoteUser} must have.
     * @return all the {@link RemoteUser} in {@link #remoteUserMap} that have the specified status.
     */
    public List<RemoteUser> getRemoteUsers(StatusType status) {

        return null;
    }

    /**
     * Stores the given {@link RemoteUser}, updating both {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     *
     * @param remoteUser {@link RemoteUser} that will be stored.
     */
    public void addRemoteUser(RemoteUser remoteUser) {

    }

    /**
     * Stores all the given {@link RemoteUser}, updating both {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     * Assumes that all of them have the specified {@link StatusType}.
     *
     * @param remoteUsers all the {@link RemoteUser} that will be stored.
     * @param status      status that all of the given remote users are in.
     */
    public void addRemoteUsers(List<RemoteUser> remoteUsers, StatusType status) {

    }

    /**
     * Updates the stored data for the given remote user; if he was not previously stored, it will now be. Updates both
     * {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     *
     * @param remoteUser {@link RemoteUser} that contains the most recent information about its corresponding remote
     *                   user.
     */
    public void updateRemoteUser(RemoteUser remoteUser) {

    }

    /**
     * Removes the specified {@link RemoteUser}, updating both {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     *
     * @param remoteUser {@link RemoteUser} that will be removed.
     */
    public void removeRemoteUser(String remoteUser) {

    }

    /**
     * Updates the status of the specified remote user, effectively changing {@link #remoteUserMap} and
     * {@link #statusTypeSetMap}.
     *
     * @param remoteUser {@link RemoteUser} that contains the most recent information about its corresponding remote
     *                   user.
     */
    public void updateRemoteUserStatus(String remoteUser, StatusType status) {

    }

    /**
     * Checks if the status of the specified remote user matches the given status.
     *
     * @param remoteUser remote user whose current status will be compared.
     * @param status     status that will be compared to the one in the remote user.
     * @return if the status of the remote user matches the specified one.
     */
    public boolean checkRemoteUserStatus(String remoteUser, StatusType status) {

        return true;
    }

    /**
     * Sets up a {@link IRemoteUserTunnel} that the specified remote user may use to communicate with the client.
     *
     * @param remoteUser name that identified the remote user who will receive the {@link IRemoteUserTunnel}.
     * @return instance of {@link IRemoteUserTunnel} that the remote user may use to communicate with the client.
     */
    public IRemoteUserTunnel prepareTunnel(String remoteUser) {

        return null;
    }

    /**
     * Saves a {@link IRemoteUserTunnel} that a remote user has prepared so that the client may communicate with him.
     *
     * @param remoteUserTunnel {@link IRemoteUserTunnel} that the remote user has prepared for the client.
     */
    public void storeTunnel(IRemoteUserTunnel remoteUserTunnel) {

    }

    /**
     * Closes any {@link IRemoteUserTunnel} that the client may have opened for the specified remote user, and also
     * deletes any {@link IRemoteUserTunnel} that the remote user may have opened for the client.
     *
     * @param remoteUser name by which the remote user can be identified.
     */
    public void closeTunnels(String remoteUser) {

    }

    /**
     * Sends a given message to the specified remote user. He must have previously given the client a {@link
     * IRemoteUserTunnel} that allows the client to communicate with him.
     *
     * @param remoteUser name by which the remote user that will be sent the message can be identified.
     * @param message    content of the message that will be sent.
     */
    public void sendMessage(String remoteUser, String message) {

    }

    /**
     * Stores the given {@link IRemoteUserTunnel} so that the client may communicate with the corresponding remote user
     * whenever he desires to. It also sets up a {@link IRemoteUserTunnel} that the remote user may use to communicate
     * with the client.
     *
     * @param remoteUser       name by which the remote user can be identified.
     * @param remoteUserTunnel {@link IRemoteUserTunnel} that the remote user has prepared so that the client may
     *                         communicate with him.
     * @return {@link IRemoteUserTunnel} that the client has prepared so that the remote user may communicate with him.
     */
    public IRemoteUserTunnel replyChatRequest(String remoteUser, IRemoteUserTunnel remoteUserTunnel) {
        return null;
    }
}
