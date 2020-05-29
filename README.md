# Javagram

Javagram is a **centralized P2P and instant messaging service** written in Java. It heavily relies on the Java RMI technology.

## Modules

Javagram is composed of the **three following modules**:

* A **server** which provides clients' backends with the required services to get identified in the service and to establish communication with another clients' backends.

* A **client backend** which provides the GUI app with the required services to communicate with the server and with another clients' backends.

* A **GUI app** which acts as a mediator between the actual user and the client's backend.

### Server module

**As of now, only a single Javagram server can be run**, due to:

* Not being able to specify where the server will listen for connections.
* Bot being able to specify where the GUI app will try to communicate with the server.

However, it should be enough for any experimental purposes with the service.

The server module can be found in the **`server` package**.

The server requires a **SQL database to store the currently existing users** of the service.

### Client backend module

The client backend module can be found in the **`client` package**. Two key packages can be found inside this one:

* `exposed`: elements that the GUI app will need to integrate the client's backend.
* `unexposed`: all the remaining elements of the client's backend.

### GUI app module

As many GUI apps as desired can be simultaneously run. Each one will take care of setting up its own client's backend.

The GUI app module can be found in the **`desktopapp` package**. Two key packages can be found inside this one:

* `identifieduser`: elements that the GUI app uses when the user has been successfully identified as a Javagram user.
* `unidentifieduser`: elements that the GUI app uses when the user has not already identified as a Javagram user.

The GUI app is **built on JavaFX**.

### The fourth major package

There is another major package left. The **`common` package** contains all the elements that both the server and client backend modules share to be able to successfully communicate.

## Features

### Account management

A user is able to:

* **Register** as a new Javagram user.
* **Log in** into an existing Javagram user.
* **Change the password** of its Javagram user, after being logged in.

### Social interactions

A Javagram user is able to:

* **Establish and manage friendships with another Javagram users**:
  - Send friendship requests to another users, as long as their usernames are known, even if they are currently offline.
  - See incoming friendship requests, to accept or to reject them.
  - End currently established friendships with another users.
  - See which friends are currently online.

* **Communicate directly with another Javagram users** that are logged into the same Javagram server:
  - Communication between two users is restricted to text messages of any length.
  - The communication's data is sent directly from one user to another. Therefore, no data is stored in the server to further increase privacy.
  - Besides that, no data is stored in the user's clients. That is, a user loses all received messages after logging out.

### Security

* All **users' passwords**:
  - Are given their own **pseudorandom 512-bit salt**.
  - Are **stored in the database** as the resulting **hash of the password + its salt using the PBKDF2** function.

* A **client's session is uniquely represented by a pseudorandom token**, which is therefore needed to carry out any operation on behalf of that Javagram user.

* **Communication between a client's backend and the server** is encrypted using **TLS**.

* **Communication between two clients' backends** is end-to-end encrypted using a combination of **RSA + AES**.
  - The secret used to encrypt the communication between them will be regenerated each time anyone of them goes offline and comes back.

### Watch some features in motion

_**Note:** the corresponding GIFs may take some time to load depending on your connection._

#### Logging into the service

![Logging into the service - Demo](readme_resources/feat_login.gif)

#### Establishing a new friendship

![Establishing a new friendship - Demo](readme_resources/feat_newFriend.gif)

#### Two users communicating in real time

![Two users communicating in real time - Demo](readme_resources/feat_communication.gif)

## Getting started

To experiment with this service, start by **downloading or cloning this repository**.

### Prerequisites

You will need to have a **development environment that supports the following technologies**:

* Java 11.
* JavaFX 11.
* Gradle.
* Docker.

You will also need to **set up the SQL database that the Javagram server will use**. The server is currently configured to establish a connection with the PostgreSQL server that is defined in the `src/main/scripts` directory, using a `javagram` database defined in its public schema.

1. Initialize the PostgreSQL server by running `docker-compose up` inside that directory.

2. Create the database's tables using their definitions in the ```generate_database.sql``` script.

3. The server's database will now be ready!

### Running the server

After initializing the database, the Javagram server can be run by executing the **main method in the `server.RunServer` class**. This method performs the following actions:

1. Initialize a Java RMI server to be able to later export the Javagram server as a remote object accessible by the Javagram clients.

2. Initialize the Javagram server, which automatically establishes a connection with the database.

3. Export the Javagram server using the RMI server.

4. The Javagram server will now be ready and listening for incoming connections!

### Running a client instance

Once the Javagram server is ready to provide clients with the Javagram services, an instance of a client can be run by executing the **main method in the `desktopapp.MainApp` class**. This method performs the following actions:

1. Initialize the JavaFX GUI.

2. Initialize the client's backend that will provide the GUI with any required services.

After successfully running the client, the user will need to **either register as a new Javagram user** (the user is automatically logged in), **or to log in** as an already existing user. At last, the GUI's main window will be shown, and the user will now be ready to communicate with another users!

## Built With

* [Java 11](https://adoptopenjdk.net/) - Heavily relying on its Java RMI technology.
* [JavaFX 11](https://openjfx.io/index.html) - Upon which the GUI is built.
* [Java JWT](https://github.com/auth0/java-jwt) - An implementation of JSON Web Tokens in Java.
* [PostgreSQL](https://www.postgresql.org/) - The database which the Javagram server was configured to used.
* [HikariCP](https://github.com/brettwooldridge/HikariCP) - The JDBC connection pool on which the server relies to communicate multiple times in parallel with the database.
* [Gradle](https://gradle.org/) - To handle the project's dependencies.

## Authors

* **Álvaro Goldar Dieste** - [alvrogd](https://github.com/alvrogd)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.