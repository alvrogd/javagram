package com.goldardieste.javagram.server;

import com.goldardieste.javagram.common.ConfigurationParameters;

import java.io.IOException;

public class RunServer {

    public static void main(String[] args) throws IOException {

        ServerFacade server = new ServerFacade("org.postgresql.ds.PGSimpleDataSource", "jdbc:postgresql://localhost",
        5432, "javagram", "javagram_admin", "javagram_admin");

        new RMIRegistry(ConfigurationParameters.RMI_PORT, ConfigurationParameters.RMI_IDENTIFIER, (ServerFacadeProxy)server.getProxy());

        System.in.read();
    }
}
