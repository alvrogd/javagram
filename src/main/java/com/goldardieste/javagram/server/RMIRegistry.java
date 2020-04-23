package com.goldardieste.javagram.server;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class will take care of initializing the RMI registry where the Javagram server will be accessible (if it is
 * not already, otherwise, it will just contact it). Therefore, the class will also export the server to the registry.
 */
public class RMIRegistry {

    /* ----- Attributes -----*/

    /**
     * Port where the RMI registry will be found.
     */
    private final int port;

    /**
     * Name by which the {@link #exportedServer} object can be retrieved from the RMI registry.
     */
    private final String identifier;

    /**
     * {@link ServerFacadeProxy} object that has been exported to the RMI registry, using the given {@link #port} and
     * {@link #identifier}.
     */
    private final ServerFacadeProxy exportedServer;

    /**
     * Reference to the RMI registry that this object may initialize when called. If the RMI registry already existed,
     * no reference will be stored to it.
     */
    private Registry rmiRegistry;


    /* ----- Constructor -----*/

    /**
     * Initializes a local RMI registry using the given port, and exports the given {@link ServerFacadeProxy} object so
     * that it may be used through that registry, via the given identifier, by any Javagram client.
     *
     * @param port           port where the RMI registry will be found.
     * @param identifier     name by which the given {@link ServerFacadeProxy} will be retrievable.
     * @param javagramServer {@link ServerFacadeProxy} that will be exported trough the RMI registry.
     */
    public RMIRegistry(int port, String identifier, ServerFacadeProxy javagramServer) {

        this.port = port;
        this.identifier = identifier;
        // The object that will be exported is stored so that it can be unexported when finishing the execution
        this.exportedServer = javagramServer;

        try {

            // The RMI registry is initialized
            startRMIRegistry();

            // The remote object is stored in the registry, so that it may be remotely used
            this.rmiRegistry.rebind(identifier, this.exportedServer);

            System.out.println("The Javagram server is now available at the RMI registry using using the name: " +
                    identifier);

        } catch (RemoteException e) {
            System.err.println("The RMI registry could not be initialized/contacted");
            e.printStackTrace();
        }
    }


    /* ----- Methods -----*/

    /**
     * Initializes a local RMI registry using the given port.
     *
     * @throws java.rmi.RemoteException if the RMI registry cannot be initialized.
     */
    private void startRMIRegistry() throws java.rmi.RemoteException {

        try {
            // Firstly, a quick check is performed to determine if the desired RMI registry already exists
            // Calling "list" if the registry does not already exist will throw an exception
            // TLS connections; host = null -> localhost
            this.rmiRegistry = LocateRegistry.getRegistry(null, this.port, new SslRMIClientSocketFactory());
            this.rmiRegistry.list();
        }

        // If no registry is found
        catch (RemoteException e) {
            System.out.println("No RMI registry could be found at port: " + this.port);

            // A new one is created. Its reference is also stored so that the object may close it when finishing its
            // execution
            // TLS connections
            this.rmiRegistry = LocateRegistry.createRegistry(this.port, new SslRMIClientSocketFactory(),
                    new SslRMIServerSocketFactory());
            System.out.println("RMI registry initialized at port: " + this.port);
        }
    }

    /**
     * The {@link #exportedServer} object is removed from the RMI registry, and it is also commanded to end its
     * execution.
     */
    public void haltExecution() {

        try {
            // 1. Removing the Javagram server from the RMI registry

            // First of all, the link with its identifier is destroyed
            this.rmiRegistry.unbind(this.identifier);

            // It is also requested to end its execution
            ((ServerFacadeProxy) this.exportedServer).haltExecution();

            // And it is fully unexported, so that it may not be bound again (it is needed to allow the application to
            // properly exit). The object is exported when it is initialized (UnicastRemoteObject)
            UnicastRemoteObject.unexportObject(this.exportedServer, true);

            System.out.println("The following Javagram server has been removed from the RMI registry: " + identifier);

            // 2. If this object has been the responsible for initializing the RMI registry, it may, and it also should
            // close it -> https://stackoverflow.com/a/1566824
            if (this.rmiRegistry != null) {
                UnicastRemoteObject.unexportObject(this.rmiRegistry, true);
            }

        } catch (RemoteException e) {
            System.err.println("The RMI registry could not be contacted");
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.err.println("The specified Javagram server is not bound to the given RMI registry: " +
                    "rmi://localhost:" + this.port + "/" + identifier);
            e.printStackTrace();
        }

        System.out.println("Execution successfully stopped");
    }
}
