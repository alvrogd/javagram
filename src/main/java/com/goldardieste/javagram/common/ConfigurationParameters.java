package com.goldardieste.javagram.common;

/**
 * This class contains parameters related to the configuration of the Javagram's server. Its only purpose is to ease
 * the process of testing.
 */
public class ConfigurationParameters {

    // TODO insert final values

    /**
     * Address where the RMI registry that hosts the server can be located.
     */
    public static final String RMI_ADDRESS = "localhost";

    /**
     * Port where the RMI registry that hosts the server can be located.
     */
    public static final int RMI_PORT = 1099;

    /**
     * Name by which the RMI registry that hosts the server can be located.
     */
    public static final String RMI_IDENTIFIER = "javagram_server";

    /**
     * Identifier of the driver that will be used to contact the server's database.
     */
    private final String JDBC_DRIVER = "org.postgresql.Driver";

    /**
     * URL where the server's database can be located.
     */
    public static final String JDBC_URL = "jdbc:postgresql://localhost";

    /**
     * Port where the server's database can be located.
     */
    public static final int JDBC_PORT = 5432;

    /**
     * Name by which the server's database can be located.
     */
    public static final String JDBC_DATABASE_URL = "javagram";

    /**
     * Username to log into an account in the server's database.
     */
    public static final String JDBC_USERNAME = "javagram_admin";

    /**
     * Password to log into an account in the server's database.
     */
    public static final String JDBC_PASSWORD = "javagram_admin";
}
