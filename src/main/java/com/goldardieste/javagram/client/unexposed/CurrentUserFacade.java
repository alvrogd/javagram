package com.goldardieste.javagram.client.unexposed;

import com.goldardieste.javagram.client.exposed.LocalTunnelsListener;
import com.goldardieste.javagram.client.exposed.RemoteUsersListener;
import com.goldardieste.javagram.client.unexposed.cryptography.CommunicationDecryptionUtility;
import com.goldardieste.javagram.common.interfaces.IRemoteUserTunnel;
import com.goldardieste.javagram.common.datacontainers.RemoteUser;
import com.goldardieste.javagram.common.StatusType;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class will manage all the remote users related to the local user in the client (friends, friendship
 * requests...), and it will also manage any tunnels that are needed to communicate with other users.
 */
public class CurrentUserFacade {

    /* ----- Attributes ----- */

    /**
     * Username that identifies the Javagram user who has opened a session in the client.
     */
    private final String identifiedUser;

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
     * by their {@link StatusType}. All the stored users are also present in {@link #remoteUserMap}.
     * <p>
     * Key -> {@link StatusType}.
     * Value -> all the known {@link RemoteUser} that are in the state given by their key.
     */
    private final Map<StatusType, Set<RemoteUser>> statusTypeSetMap;

    /**
     * Contains all the remote Javagram users whose current status, in relation to the client, is known. All the stored
     * users are also present in {@link #statusTypeSetMap}.
     * <p>
     * Key -> username.
     * Value -> {@link RemoteUser} that represents the remote user.
     */
    private final Map<String, RemoteUser> remoteUserMap;

    /**
     * {@link ReentrantLock} that a thread must acquire to modify {@link #statusTypeSetMap} and/or {@link
     * #remoteUserMap}.
     */
    private final ReentrantLock storedUsersLock;

    /**
     * {@link ReentrantLock} that a thread must acquire to modify {@link #openedTunnels} and/or {@link
     * #receivedTunnels}.
     */
    private final ReentrantLock tunnelsLock;

    /**
     * {@link RemoteUsersListener} that the object will use, if it is not null, when modifying {@link #remoteUserMap}
     * and/or {@link #statusTypeSetMap}.
     */
    private RemoteUsersListener remoteUsersListener;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link CurrentUserFacade} for the specified Javagram user.
     *
     * @param username name that identifies the Javagram user that the client is going to initiate a session in behalf
     *                 of.
     */
    public CurrentUserFacade(String username) {

        this.identifiedUser = username;
        this.openedTunnels = new HashMap<>();
        this.receivedTunnels = new HashMap<>();

        this.statusTypeSetMap = new EnumMap<>(StatusType.class);
        for (StatusType value : StatusType.values()) {
            this.statusTypeSetMap.put(value, new HashSet<>());    // One set for each possible status
        }

        this.remoteUserMap = new HashMap<>();
        this.storedUsersLock = new ReentrantLock();
        this.tunnelsLock = new ReentrantLock();
    }


    /* ----- Getters ----- */

    /**
     * Retrieves the current {@link #identifiedUser}.
     *
     * @return {@link #identifiedUser}.
     */
    public String getIdentifiedUser() {
        return identifiedUser;
    }


    /* ----- Methods ----- */

    /**
     * Returns the specified {@link RemoteUser} stored in {@link #remoteUserMap}.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @return if the specified remote user is found, returns its corresponding stored {@link RemoteUser}; otherwise,
     * returns null.
     */
    public RemoteUser getRemoteUser(String remoteUser) {

        RemoteUser result = null;

        this.storedUsersLock.lock();

        try {
            result = this.remoteUserMap.get(remoteUser);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        return result;
    }

    /**
     * Returns all the {@link RemoteUser} stored in {@link #remoteUserMap} whose statuses correspond to the specified
     * one.
     *
     * @param status {@link StatusType} that the all returned {@link RemoteUser} must have.
     * @return all the {@link RemoteUser} in {@link #remoteUserMap} that have the specified status.
     */
    public List<RemoteUser> getRemoteUsers(StatusType status) {

        List<RemoteUser> result = new ArrayList<>();

        this.storedUsersLock.lock();

        try {
            result.addAll(this.statusTypeSetMap.get(status));

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        return result;
    }

    /**
     * Stores the given {@link RemoteUser}, updating both {@link #remoteUserMap} and {@link #statusTypeSetMap}. No
     * changes will be made if it is already present.
     *
     * @param remoteUser {@link RemoteUser} that will be stored.
     */
    public void addRemoteUser(RemoteUser remoteUser) {

        this.storedUsersLock.lock();

        try {
            this.remoteUserMap.putIfAbsent(remoteUser.getUsername(), remoteUser);
            (this.statusTypeSetMap.get(remoteUser.getStatus())).add(remoteUser);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        if (this.remoteUsersListener != null) {
            this.remoteUsersListener.forwardRemoteUserChange(remoteUser);
        }
    }

    /**
     * Stores all the given {@link RemoteUser}, updating both {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     *
     * @param remoteUsers all the {@link RemoteUser} that will be stored.
     */
    public void addRemoteUsers(List<RemoteUser> remoteUsers) {

        this.storedUsersLock.lock();

        try {
            for (RemoteUser remoteUser : remoteUsers) {
                this.remoteUserMap.putIfAbsent(remoteUser.getUsername(), remoteUser);
                (this.statusTypeSetMap.get(remoteUser.getStatus())).add(remoteUser);
            }

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        for (RemoteUser remoteUser : remoteUsers) {
            if (this.remoteUsersListener != null) {
                this.remoteUsersListener.forwardRemoteUserChange(remoteUser);
            }
        }
    }

    /**
     * Stores all the given {@link RemoteUser}, updating both {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     * Assumes that all of them have the specified {@link StatusType}. The already present {@link RemoteUser} will not
     * be added.
     *
     * @param remoteUsers all the {@link RemoteUser} that will be stored.
     * @param status      status that all of the given remote users are in.
     */
    public void addRemoteUsers(List<RemoteUser> remoteUsers, StatusType status) {

        this.storedUsersLock.lock();

        try {
            for (RemoteUser remoteUser : remoteUsers) {
                this.remoteUserMap.putIfAbsent(remoteUser.getUsername(), remoteUser);
            }

            (this.statusTypeSetMap.get(status)).addAll(remoteUsers);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        for (RemoteUser remoteUser : remoteUsers) {
            if (this.remoteUsersListener != null) {
                this.remoteUsersListener.forwardRemoteUserChange(remoteUser);
            }
        }
    }

    /**
     * Updates the stored data for the given remote user; if he was not previously stored, it will now be. Updates both
     * {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     *
     * @param remoteUser {@link RemoteUser} that contains the most recent information about its corresponding remote
     *                   user.
     */
    public void updateRemoteUser(RemoteUser remoteUser) {

        this.storedUsersLock.lock();

        try {
            RemoteUser oldValue = this.remoteUserMap.put(remoteUser.getUsername(), remoteUser);

            // - If the user was already stored, and the target status map is the same one, the user must be removed
            //   from it nevertheless to be able to update its value
            // - If the user was already stored, and the target status map is different, the user must be removed from
            //   it
            if (oldValue != null) {
                // - If a remote user that had been sent a friendship request has now sent one to the client, assumes
                // that a race condition must have happened, due to the server automatically accepting friendships when
                // an user sends a request to another user that has already requested it
                // - The same may happen the other way around
                if ((oldValue.getStatus().equals(StatusType.FRIENDSHIP_SENT) &&
                        remoteUser.getStatus().equals(StatusType.FRIENDSHIP_RECEIVED)) ||
                        (oldValue.getStatus().equals(StatusType.FRIENDSHIP_RECEIVED) &&
                                remoteUser.getStatus().equals(StatusType.FRIENDSHIP_SENT))) {

                    // If a request has just been received, the remote user must be online
                    remoteUser.setStatus(StatusType.ONLINE);
                }

                (this.statusTypeSetMap.get(oldValue.getStatus())).remove(oldValue);
            }

            (this.statusTypeSetMap.get(remoteUser.getStatus())).add(remoteUser);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        if (this.remoteUsersListener != null) {
            this.remoteUsersListener.forwardRemoteUserChange(remoteUser);
        }
    }

    /**
     * Removes the specified {@link RemoteUser}, updating both {@link #remoteUserMap} and {@link #statusTypeSetMap}.
     *
     * @param remoteUser {@link RemoteUser} that will be removed.
     */
    public void removeRemoteUser(String remoteUser) {

        RemoteUser removedValue = null;

        this.storedUsersLock.lock();

        try {
            removedValue = this.remoteUserMap.remove(remoteUser);

            // If the user was present before, it will also be present in the following collection
            if (removedValue != null) {
                (this.statusTypeSetMap.get(removedValue.getStatus())).remove(removedValue);
            }

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        if (this.remoteUsersListener != null) {
            this.remoteUsersListener.forwardRemoteUserDeletion(removedValue);
        }
    }

    /**
     * Updates the status of the specified remote user, effectively changing {@link #remoteUserMap} and
     * {@link #statusTypeSetMap}.
     *
     * @param remoteUser {@link RemoteUser} that contains the most recent information about its corresponding remote
     *                   user.
     */
    public void updateRemoteUserStatus(String remoteUser, StatusType status) {

        // As of now, this method can just call the following one
        updateRemoteUser(new RemoteUser(remoteUser, status));
    }

    /**
     * Checks if the status of the specified remote user matches the given status.
     *
     * @param remoteUser remote user whose current status will be compared.
     * @param status     status that will be compared to the one in the remote user.
     * @return if the status of the remote user matches the specified one.
     */
    public boolean checkRemoteUserStatus(String remoteUser, StatusType status) {

        boolean check = false;

        this.storedUsersLock.lock();

        try {
            RemoteUser user = this.remoteUserMap.get(remoteUser);

            if (user != null) {
                check = user.getStatus().equals(status);
            }

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        return check;
    }

    /**
     * Sets up a {@link IRemoteUserTunnel} that the specified remote user may use to communicate with the client.
     *
     * @param remoteUser name that identified the remote user who will receive the {@link IRemoteUserTunnel}.
     * @return instance of {@link IRemoteUserTunnel} that the remote user may use to communicate with the client.
     * @throws TunnelOperationException if a previously opened local tunnel cannot be unexported, of if a new one
     *                                  cannot be exported.
     */
    public IRemoteUserTunnel prepareTunnel(String remoteUser) throws TunnelOperationException {

        LocalUserTunnel localTunnel = null;

        this.storedUsersLock.lock();

        try {
            localTunnel = prepareTunnelNonLocking(remoteUser);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        return localTunnel;
    }

    /**
     * Sets up a {@link IRemoteUserTunnel} that the specified remote user may use to communicate with the client. It
     * does not acquire any related locks.
     *
     * @param remoteUser name that identified the remote user who will receive the {@link IRemoteUserTunnel}.
     * @return instance of {@link IRemoteUserTunnel} that the remote user may use to communicate with the client.
     * @throws TunnelOperationException if a previously opened local tunnel cannot be unexported, of if a new one
     *                                  cannot be exported.
     */
    private LocalUserTunnel prepareTunnelNonLocking(String remoteUser) throws TunnelOperationException {

        LocalUserTunnel localTunnel = null;

        try {
            // If a local tunnel has already been opened for the given remote user, it can be reused
            localTunnel = this.openedTunnels.get(remoteUser);

            if (localTunnel == null) {

                localTunnel = new LocalUserTunnel(remoteUser);
            }

            // As the only operation that can throw a checked exception is the previous ones, all the other operations
            // are executed only if that one causes no error
            this.openedTunnels.put(remoteUser, localTunnel);

        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport a local tunnel");
            throw new TunnelOperationException(e);

        } catch (RemoteException e) {
            System.err.println("Could not export a local tunnel");
            throw new TunnelOperationException(e);
        }

        return localTunnel;
    }

    /**
     * Saves a {@link IRemoteUserTunnel} that a remote user has prepared so that the client may communicate with him.
     *
     * @param remoteUser       name by which the remote user can be identified.
     * @param remoteUserTunnel {@link IRemoteUserTunnel} that the remote user has prepared for the client.
     */
    public void storeTunnel(String remoteUser, IRemoteUserTunnel remoteUserTunnel) {

        this.storedUsersLock.lock();

        try {
            // Any previously received tunnel from the given remote user is removed
            this.receivedTunnels.put(remoteUser, remoteUserTunnel);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }
    }

    /**
     * Closes any {@link IRemoteUserTunnel} that the client may have opened for the specified remote user, and also
     * deletes any {@link IRemoteUserTunnel} that the remote user may have opened for the client.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @throws TunnelOperationException if a local tunnel cannot be unexported.
     */
    public void closeTunnels(String remoteUser) throws TunnelOperationException {

        this.tunnelsLock.lock();

        try {
            // 1. Any possible local tunnel is closed
            LocalUserTunnel localTunnel = this.openedTunnels.get(remoteUser);

            if (localTunnel != null) {

                // As local tunnels are remote objects, they must be unexported before deleting them
                UnicastRemoteObject.unexportObject(localTunnel, true);
            }

            // As the only operation that can throw a checked exception is the one that unexports the remote object,
            // all the other operations are executed only if this one causes no error
            this.openedTunnels.remove(remoteUser);

            // 2. Any received tunnel is closed
            this.receivedTunnels.remove(remoteUser);

        } catch (NoSuchObjectException e) {
            System.err.println("Could not unexport a local tunnel");
            throw new TunnelOperationException(e);

        } finally {
            // The lock must always be released
            this.tunnelsLock.unlock();
        }
    }

    /**
     * Checks if the two tunnels that are needed to communicate with a remote user are ready:
     * - The {@link LocalUserTunnel} will allow the client to send messages to the remote user.
     * - The {@link IRemoteUserTunnel} will allow the remote user to send messages to the client.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @return if the two tunnels are ready.
     */
    public boolean areBothTunnelsPrepared(String remoteUser) {

        boolean prepared = false;

        this.tunnelsLock.lock();

        try {
            prepared = this.openedTunnels.get(remoteUser) != null && this.receivedTunnels.get(remoteUser) != null;

        } finally {
            // The lock must always be released
            this.tunnelsLock.unlock();
        }

        return prepared;
    }

    /**
     * Sends a given message to the specified remote user. He must have previously given the client a {@link
     * IRemoteUserTunnel} that allows the client to communicate with him.
     *
     * @param remoteUser name by which the remote user that will be sent the message can be identified.
     * @param message    content of the message that will be sent.
     * @throws TunnelOperationException if no remote tunnel can be found for the specified user, of if the transmission
     *                                  of the message fails.
     */
    public void sendMessage(String remoteUser, String message) throws TunnelOperationException {

        this.storedUsersLock.lock();

        try {
            IRemoteUserTunnel remoteTunnel = this.receivedTunnels.get(remoteUser);

            if (remoteTunnel != null) {
                remoteTunnel.transmitMessage(message);

            } else {
                throw new TunnelOperationException("No remote tunnel has been received from the specified remote " +
                        "user");
            }

        } catch (RemoteException e) {
            System.err.println("Could not send a message trough a remote tunnel");
            throw new TunnelOperationException(e);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }
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
     * @throws TunnelOperationException if a previously opened local tunnel cannot be unexported, of if a new one
     *                                  cannot be exported.
     */
    public IRemoteUserTunnel replyChatRequest(String remoteUser, IRemoteUserTunnel remoteUserTunnel) throws
            TunnelOperationException {

        LocalUserTunnel localTunnel = null;

        this.storedUsersLock.lock();

        try {
            // 1. The local tunnel that the remote user will receive is set up
            localTunnel = prepareTunnelNonLocking(remoteUser);

            // As the only operation that can throw a checked exception is the previous one, all the other operations
            // are executed only if that one causes no error
            this.receivedTunnels.put(remoteUser, remoteUserTunnel);

        } finally {
            // The lock must always be released
            this.storedUsersLock.unlock();
        }

        return localTunnel;
    }

    /**
     * Updates the {@link LocalTunnelsListener} that all {@link LocalUserTunnel} will use when receiving data.
     *
     * @param localTunnelsListener the new {@link LocalTunnelsListener}.
     */
    public static void setLocalTunnelsListener(LocalTunnelsListener localTunnelsListener) {
        LocalUserTunnel.setLocalTunnelsListener(localTunnelsListener);
    }

    /**
     * Updates the {@link CommunicationDecryptionUtility} that all {@link LocalUserTunnel} will use when receiving
     * data.
     *
     * @param utility the new {@link CommunicationDecryptionUtility}.
     */
    public static void setCommunicationDecryptionUtility(CommunicationDecryptionUtility utility) {
        LocalUserTunnel.setCommunicationDecryptionUtility(utility);
    }

    /**
     * Updates the value of {@link #remoteUsersListener}.
     *
     * @param remoteUsersListener new {@link #remoteUsersListener}.
     */
    public void setRemoteUsersListener(RemoteUsersListener remoteUsersListener) {
        this.remoteUsersListener = remoteUsersListener;
    }

    /**
     * Performs any tasks that are required to successfully stop the execution of the Javagram client's back-end.
     */
    public void haltExecution() {

        // All local tunnels that have been opened are now closed
        this.tunnelsLock.lock();

        try {
            for (LocalUserTunnel localUserTunnel : this.openedTunnels.values()) {

                try {
                    UnicastRemoteObject.unexportObject(localUserTunnel, true);

                } catch (NoSuchObjectException e) {
                    System.err.println("A local user tunnel could not be unexported");
                    e.printStackTrace();
                }
            }

        } finally {
            // The lock must always be released
            this.tunnelsLock.unlock();
        }
    }
}
