package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.ConfigurationParameters;

import java.io.IOException;

public class RunServer {

    public static void main(String[] args) throws IOException {

        ServerFacade server = new ServerFacade(ConfigurationParameters.JDBC_DRIVER, ConfigurationParameters.JDBC_URL,
                ConfigurationParameters.JDBC_PORT, ConfigurationParameters.JDBC_DATABASE,
                ConfigurationParameters.JDBC_USERNAME, ConfigurationParameters.JDBC_PASSWORD);

        new RMIRegistry(ConfigurationParameters.RMI_PORT, ConfigurationParameters.RMI_IDENTIFIER,
                (ServerFacadeProxy) server.getProxy());

        System.in.read();
    }
}
