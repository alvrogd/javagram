package com.goldardieste.javagram.common.datacontainers;

import com.goldardieste.javagram.common.interfaces.IRemoteUserTunnel;

import java.io.Serializable;

/**
 * Contains all data that is returned when a communication establishment is requested to a remote user, so that the
 * client can communicate with him.
 */
public class NewChatData implements Serializable {

    /* ----- Attributes ----- */

    /**
     * The {@link IRemoteUserTunnel} that the receiver of the request has created so that the requester may communicate
     * with him.
     */
    public final IRemoteUserTunnel remoteUserTunnel;

    /**
     * The encrypted AES key that the receiver of the request has created so that the requester may encrypt messages
     * to communicate with him.
     */
    public final String encryptedCommunicationSecret;


    /* ----- Constructor ----- */

    /**
     * Generates an {@link NewChatData} that contains the returned data to initializing a communication.
     *
     * @param remoteUserTunnel             the new {@link #remoteUserTunnel}.
     * @param encryptedCommunicationSecret the new {@link #encryptedCommunicationSecret}.
     */
    public NewChatData(IRemoteUserTunnel remoteUserTunnel, String encryptedCommunicationSecret) {
        this.remoteUserTunnel = remoteUserTunnel;
        this.encryptedCommunicationSecret = encryptedCommunicationSecret;
    }
}
