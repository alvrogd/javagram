package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.ConfigurationParameters;

import java.io.IOException;
import java.util.Scanner;

public class RunServer {

    public static void main(String[] args) throws IOException {

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
