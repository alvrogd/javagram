package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.ConfigurationParameters;

import java.io.IOException;
import java.util.Scanner;

public class RunServer {

    public static void main(String[] args) throws IOException {

        // SSL configuration
        System.setProperty("javax.net.ssl.keyStore", "./build/resources/main/com/goldardieste/javagram/server/javagram_keystore.ks");
        System.setProperty("javax.net.ssl.keyStorePassword", "javagram");
        System.setProperty("javax.net.ssl.trustStore", "./build/resources/main/com/goldardieste/javagram/server/javagram_truststore.ks");
        System.setProperty("javax.net.ssl.trustStorePassword", "javagram");

        // Security manager
        System.setProperty("java.security.policy", "file:./build/resources/main/com/goldardieste/javagram/server/java.policy");
        System.setSecurityManager(new SecurityManager());

        // Javagram Server
        ServerFacade server = new ServerFacade(ConfigurationParameters.JDBC_DRIVER, ConfigurationParameters.JDBC_URL,
                ConfigurationParameters.JDBC_PORT, ConfigurationParameters.JDBC_DATABASE,
                ConfigurationParameters.JDBC_USERNAME, ConfigurationParameters.JDBC_PASSWORD);

        // Exports the Javagram Server
        RMIRegistry rmiRegistry = new RMIRegistry(ConfigurationParameters.RMI_PORT,
                ConfigurationParameters.RMI_IDENTIFIER, (ServerFacadeProxy) server.getProxy());

        // Ends the execution
        System.out.println("Press any key to end the execution");
        (new Scanner(System.in)).nextLine();

        rmiRegistry.haltExecution();
    }
}
