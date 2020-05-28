package com.goldardieste.javagram.client.unexposed.cryptography;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * The purpose of this class is to provide the client-side with the utilities to encrypt/decrypt the communication with
 * other users.
 */
// TODO use Diffie Hellman instead of RSA to obtain private keys for each communication
public class CryptographicServices implements CommunicationDecryptionUtility {

    /* ----- Attributes ----- */

    /**
     * Contains the local user's private and public keys.
     */
    private final KeyPair localUserKeyPair;

    /**
     * Contains, for each stored remote user (that are identified by their usernames), the AES key that is being used
     * to encrypt the communications with him.
     */
    private final Map<String, SecretKey> remoteUsersKeys;

    /**
     * Size in bits for the RSA keys.
     */
    private final static int RSA_KEY_SIZE = 2048;

    /**
     * Size in bits for the AES keys.
     */
    private final static int AES_KEY_SIZE = 128;

    /**
     * Size in bits for AES initialization vectors using Galois/Counter Mode.
     */
    private final static int AES_GCM_IV_SIZE = 96;

    /**
     * Size in bits for AES authentication tag using GCM.
     */
    private final static int AES_GCM_AUTH_TAG_SIZE = 128;


    /* ----- Constructor ----- */

    /**
     * Initializes a {@link CryptographicServices} for a local client, that generates a random pair of public and
     * private keys so that he may encrypt communications with other clients.
     *
     * @throws IllegalStateException if the RSA algorithm is not supported.
     */
    public CryptographicServices() {

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(CryptographicServices.RSA_KEY_SIZE);
            this.localUserKeyPair = keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            System.err.println("The system does not support RSA encryption");
            throw new IllegalStateException(e);
        }

        this.remoteUsersKeys = new HashMap<>();
    }


    /* ----- Methods ----- */

    /**
     * Returns the locally generated {@link PublicKey}.
     * @return the local {@link PublicKey}.
     */
    public PublicKey getPublicKey() {
        return this.localUserKeyPair.getPublic();
    }

    /**
     * Generates a secret AES key that the client may use to communicate with the corresponding remote user. The secret
     * is returned encrypted though RSA, using Base64, so that the remote user can also know it.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @param publicKey  public key of the remote user.
     * @return the encrypted AES key.
     * @throws IllegalStateException if the data cannot be encrypted using RSA.
     */
    public String generateSecretForCommunication(String remoteUser, PublicKey publicKey) {

        String encryptedAesKey = null;

        // The client is the one that must take care of generating the AES key for the communication
        byte[] aesKeyBytes = new byte[CryptographicServices.AES_KEY_SIZE / 8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(aesKeyBytes);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Once the key is generated, it is encrypted through RSA so that the remote client can know it
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedAesKeyBytes = cipher.doFinal(aesKey.getEncoded());

            encryptedAesKey = Base64.getMimeEncoder().withoutPadding().encodeToString(encryptedAesKeyBytes);

            synchronized (this.remoteUsersKeys) {
                this.remoteUsersKeys.put(remoteUser, aesKey);
            }

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException e) {
            System.err.println("Could not encrypt an AES key using RSA");
            throw new IllegalStateException(e);
        }

        return encryptedAesKey;
    }

    /**
     * Stores a secret AES key that a remote user has generated for the client to communicate with him. The key comes
     * encrypted using RSA.
     *
     * @param remoteUser      name by which the remote user can be identified.
     * @param encryptedSecret the encrypted AES key.
     * @throws IllegalStateException if the data cannot be decrypted using RSA.
     */
    public void storeSecretForCommunication(String remoteUser, String encryptedSecret) {

        // The received key is now decoded from Base64 and unencrypted through RSA
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, this.localUserKeyPair.getPrivate());

            SecretKey aesKey = new SecretKeySpec(cipher.doFinal(Base64.getMimeDecoder().decode(encryptedSecret)),
                    "AES");

            synchronized (this.remoteUsersKeys) {
                this.remoteUsersKeys.put(remoteUser, aesKey);
            }

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                IllegalBlockSizeException e) {
            System.err.println("Could not decrypt an AES key using RSA");
            throw new IllegalStateException(e);
        }
    }

    /**
     * Encrypts the given {@link String} using an already existing AES key for communication with the specified remote
     * user. It is returned using Base64.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @param contents   data that will be encrypted.
     * @return the encrypted data.
     * @throws IllegalArgumentException if no AES key can be found for the remote user.
     * @throws IllegalStateException if the data cannot be encrypted using AES.
     */
    public String encryptString(String remoteUser, String contents) {

        String encryptedContents = null;

        synchronized (this.remoteUsersKeys) {

            SecretKey aesKey = this.remoteUsersKeys.get(remoteUser);

            if (aesKey != null) {
                // Initialization vectors cannot be reused because the AES key could be trivially calculated
                // https://crypto.stackexchange.com/questions/2991/why-must-iv-key-pairs-not-be-reused-in-ctr-mode/2993#2993
                byte[] iv = new byte[CryptographicServices.AES_GCM_IV_SIZE / 8];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(iv);

                try {
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    GCMParameterSpec parameterSpec = new GCMParameterSpec(CryptographicServices.AES_GCM_AUTH_TAG_SIZE,
                            iv);
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec);

                    byte[] encryptedContentsBytes = cipher.doFinal(contents.getBytes(StandardCharsets.UTF_8));

                    // The authentication tag is automatically added to the contents
                    ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedContentsBytes.length);
                    byteBuffer.put(iv);
                    byteBuffer.put(encryptedContentsBytes);

                    encryptedContents = Base64.getMimeEncoder().withoutPadding().encodeToString(byteBuffer.array());

                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                        InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    System.err.println("Could not encrypt the given data using AES");
                    throw new IllegalStateException(e);
                }

            } else {
                throw new IllegalArgumentException("No communication secret has been generated for the specified " +
                        "remote user");
            }
        }

        return encryptedContents;
    }

    /**
     * Decrypts the given {@link String} in Base64 using an already existing AES key for communication with the
     * specified remote user.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @param contents   data that is encrypted.
     * @return the decrypted data.
     * @throws IllegalArgumentException if no AES key can be found for the remote user.
     * @throws IllegalStateException if the data cannot be decrypted using AES.
     */
    @Override
    public String decryptString(String remoteUser, String contents) {

        String decryptedContents = null;

        synchronized (this.remoteUsersKeys) {

            SecretKey aesKey = this.remoteUsersKeys.get(remoteUser);

            if (aesKey != null) {

                byte[] contentsBytes = Base64.getMimeDecoder().decode(contents);

                try {
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    AlgorithmParameterSpec gcmIv = new GCMParameterSpec(CryptographicServices.AES_GCM_AUTH_TAG_SIZE,
                            contentsBytes, 0, CryptographicServices.AES_GCM_IV_SIZE / 8);
                    cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmIv);

                    // The first 12 bytes are used for the IV
                    byte[] decryptedContentsBytes = cipher.doFinal(contentsBytes,
                            CryptographicServices.AES_GCM_IV_SIZE / 8, contentsBytes.length -
                            CryptographicServices.AES_GCM_IV_SIZE / 8);

                   decryptedContents = new String(decryptedContentsBytes, StandardCharsets.UTF_8);

                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                        InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    System.err.println("Could not encrypt the given data using AES");
                    throw new IllegalStateException(e);
                }

            } else {
                throw new IllegalArgumentException("No communication secret has been generated for the specified " +
                        "remote user");
            }
        }

        return decryptedContents;
    }
}
