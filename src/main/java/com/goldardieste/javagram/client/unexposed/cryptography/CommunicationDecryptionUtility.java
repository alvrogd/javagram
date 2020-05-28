package com.goldardieste.javagram.client.unexposed.cryptography;

import com.goldardieste.javagram.client.unexposed.LocalUserTunnel;


/**
 * Due to communication between users being encrypted, when a {@link LocalUserTunnel} receives data from a remote user,
 * it will need to decrypt it before doing anything else with it.
 */
public interface CommunicationDecryptionUtility {

    /* ----- Methods ----- */

    /**
     * Decrypts the given {@link String} that was sent by the specified remote user.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @param contents   data that is encrypted.
     * @return the decrypted data.
     * @throws IllegalArgumentException if no encryption with the remote user has been previously configured.
     * @throws IllegalStateException    if the data cannot be decrypted.
     */
    String decryptString(String remoteUser, String contents);
}
