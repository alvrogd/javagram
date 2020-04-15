package com.goldardieste.javagram.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * The purpose of this class is to provide the server-side with:
 * - Any needed Cryptographically Secure Pseudo-Random Number Generator (CSPRNG) utilities.
 * - Any password hashing utilities.
 */
public class CryptographicServices {

    /* ----- Methods ----- */

    /**
     * Generates as many random bytes as specified.
     *
     * @param byteCount how many random bytes will be generated.
     * @return generated random bytes.
     * @throws IllegalStateException if no secure CSPRNG algorithm can be found in the host system.
     */
    public static byte[] generateRandomBytes(int byteCount) {

        try {
            byte[] randomBytes = new byte[byteCount];

            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(randomBytes);

            return randomBytes;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.err.println("No secure CSPRNG algorithm could be found");

            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates as many random bytes as specified, and encodes them using Base64.
     *
     * @param byteCount how many random bytes will be generated.
     * @return Base64 encoding of the generated random bytes.
     */
    public static String generateRandomStringBase64(int byteCount) {

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        return encoder.encodeToString(generateRandomBytes(byteCount));
    }

    /**
     * Encodes the given data using Base64.
     *
     * @param bytes data that will be encoded.
     * @return Base64 encoding of the data.
     */
    public static String StringBase64FromBytes(byte[] bytes) {

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        return encoder.encodeToString(bytes);
    }

    /**
     * Decodes the given data in Base64 to an array of bytes.
     *
     * @param data data that will be decoded.
     * @return bytes that result from de decoding of the given data.
     */
    public static byte[] BytesFromStringBase64(String data) {

        Base64.Decoder decoder = Base64.getUrlDecoder();

        return decoder.decode(data);
    }

    /**
     * Generates a salt for a password. As all passwords will be hashed using PBKDF2WithHmacSHA512, their output length
     * will be 512 bits, so all salts will also be 512 bits in length.
     *
     * @return random salt that is 64 bytes in length.
     */
    public static byte[] generatePasswordSalt() {

        return CryptographicServices.generateRandomBytes(64);
    }

    /**
     * Hashes the given {@link String} along with the specified salt, using PBKDF2WithHmacSHA512. A total of 100.000
     * iterations will be performed.
     *
     * @param string data that will be hashed along the salt.
     * @param salt   the salt.
     * @return PBKDF2WithHmacSHA512 result from combining the data and the salt.
     * @throws IllegalStateException if the system does not support PBKDF2WithHmacSHA512.
     */
    public static byte[] hashString(String string, byte[] salt) {

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(string.toCharArray(), salt, 100000, 512);

            return factory.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            System.err.println("The system does not support hashing using PBKDF2WithHmacSHA512");

            throw new IllegalStateException(e);
        }
    }
}
