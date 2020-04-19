package com.goldardieste.javagram.desktopapp;

import com.goldardieste.javagram.client.ClientFacade;
import com.goldardieste.javagram.client.ClientOperationFailedException;
import com.goldardieste.javagram.client.LocalTunnelsListener;
import com.goldardieste.javagram.client.RemoteUsersListener;
import com.goldardieste.javagram.common.RemoteUser;
import com.goldardieste.javagram.common.StatusType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

// TODO coded in a bit of a rush, so it is not definitely the best design
public class MainWindowController extends AbstractController implements LocalTunnelsListener, RemoteUsersListener {

    /* ----- Attributes ----- */

    /**
     * Provides all the back-end functionality to the desktop app.
     */
    private ClientFacade clientFacade;

    /**
     * Determines which status the retrieved remote users must have to be shown as entries. If it is null, no filtering
     * is made.
     */
    private UserEntriesFilter userEntriesFilter;

    /**
     * Contains all the retrieved remote users related to the one that has logged in.
     */
    private final Map<String, RemoteUser> retrievedRemoteUsers;

    /**
     * Stores every current conversation with a remote user.
     */
    private final Map<String, ChatHistory> initiatedChats;

    /**
     * {@link UserEntryController} that corresponds the currently selected user entry. It is needed to:
     * - 1: remove its highlighting when another entry gets selected.
     * - 2: highlight it again when the entries are regenerated.
     * - 3: know to whom the user is sending messages.
     */
    private UserEntryController currentSelectedEntry;

    /**
     * If it is set to true, the user's entries will not be rendered no matter what, as their place is being taken by
     * the input where the user may send a new friendship request.
     */
    private boolean newFriendshipInputActive;

    /**
     * This lock must be acquired to read/modify {@link #newFriendshipInputActive}. Even though all events from JavaFX
     * are dispatched using a single thread, as the notifications about remote users can be received and displayed at
     * any time due to {@link RemoteUsersListener}, there could be race conditions if it where not for this lock.
     */
    private final ReentrantLock newFriendshipInputLock;


    /* ----- FXML attributes ----- */

    /**
     * Where the leftmost icons reside.
     */
    @FXML
    private VBox sidebarMenu;

    /**
     * Left menu's option to deactivate the users entries' filter.
     */
    @FXML
    private HBox menuOptionAllUsers;

    /**
     * Left menu's option to set the users entries' filter to only show current friends.
     */
    @FXML
    private HBox menuOptionFriends;

    /**
     * Left menu's option to set the users entries' filter to only show sent or received friendship requests.
     */
    @FXML
    private HBox menuOptionRequests;

    /**
     * Left menu's option to show the menu to send a friendship request.
     */
    @FXML
    private HBox menuOptionNewFriend;

    /**
     * Where each possible user entry resides.
     */
    @FXML
    private VBox sidebarItems;

    /**
     * Where the user may type in a message to send it.
     */
    @FXML
    private TextArea textAreaMessage;

    /**
     * Where all messages for a certain chat are contained.
     */
    @FXML
    private VBox messagesContainer;

    /**
     * Warning that is shown when no messages related to the requested chat history can be found.
     */
    @FXML
    private Label emptyChatWarning;


    /* ----- Constructor ----- */

    /**
     * Initializes an empty {@link MainWindowController}.
     */
    public MainWindowController() {
        this.retrievedRemoteUsers = new HashMap<>();
        this.initiatedChats = new HashMap<>();
        this.newFriendshipInputActive = false;
        this.newFriendshipInputLock = new ReentrantLock();

        this.userEntriesFilter = UserEntriesFilter.NONE;
    }


    /* ----- Setters ----- */

    /**
     * Updates the value of {@link #clientFacade}.
     *
     * @param clientFacade new {@link #clientFacade}.
     */
    public void setClientFacade(ClientFacade clientFacade) {

        this.clientFacade = clientFacade;

        // As the controller has now the client back-end's instance, it also registers itself in it as a listener of
        // both incoming messages and incoming updates about remote users
        this.clientFacade.setLocalTunnelsListener(this);
        this.clientFacade.setRemoteUsersListener(this);

        // And retrieves the remote users related to the one that has logged in
        try {
            this.clientFacade.retrieveFriends();
        } catch (ClientOperationFailedException exception) {
            exception.printStackTrace();
        }
    }


    /* ----- Methods ----- */

    /*
       NOTE: some methods will only be called from the JavaFX thread; therefore, race conditions are not a concern due
             to being s single thread.
     */

    /**
     * Makes any possibly needed post-processing of the main window's contents.
     */
    @FXML
    private void initialize() {
        // By default, no chat history is shown
        this.emptyChatWarning.setVisible(true);
    }

    /**
     * Removes the users entries' filter to show all remote users.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void removeFilter(Event e) {

        this.userEntriesFilter = UserEntriesFilter.NONE;

        synchronized (this.retrievedRemoteUsers) {
            regenerateUserEntries();
        }

        this.newFriendshipInputLock.lock();
        this.newFriendshipInputActive = false;
        this.newFriendshipInputLock.unlock();
    }

    /**
     * Sets the users entries' filter to only show current friends.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void activateFilterFriends(Event e) {

        this.userEntriesFilter = UserEntriesFilter.CURRENT_FRIENDS;

        synchronized (this.retrievedRemoteUsers) {
            regenerateUserEntries();
        }
    }

    /**
     * Sets the users entries' filter to only show sent or received friendship requests.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void activateFilterRequests(Event e) {

        this.userEntriesFilter = UserEntriesFilter.REQUESTS;

        synchronized (this.retrievedRemoteUsers) {
            regenerateUserEntries();
        }
    }

    /**
     * Closes the currently opened session.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void logout(Event e) {
        try {
            this.clientFacade.disconnect();
            handleClosing();
        } catch (ClientOperationFailedException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Shows an input where the user may specify the username of a remote user, and where he may send him a friendship
     * request.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void showNewFriendshipInput(Event e) {

        ObservableList<Node> entries = this.sidebarItems.getChildren();

        this.newFriendshipInputLock.lock();

        try {
            // So that the user entries do not override
            this.newFriendshipInputActive = true;

            entries.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/input_new_friendship.fxml"));

            // The field where the remote user's username will be typed is shown where the user entries usually
            // are, along with the button to send the request
            entries.add(loader.load());

            InputNewFriendController controller = loader.getController();
            controller.setMainWindowController(this);

        } catch (IOException exception) {
            exception.printStackTrace();

        } finally {
            this.newFriendshipInputLock.unlock();
        }
    }

    /**
     * Sends a friendship request to the specified remote user, and shows again all the user entries.
     *
     * @param remoteUser name by which the remote user can be identified.
     */
    public void sendFriendshipRequest(String remoteUser) {

        boolean successful = false;

        // TODO handle exceptions (already friend, already sent, non existing...)
        this.newFriendshipInputLock.lock();

        try {
            this.clientFacade.requestFriendship(remoteUser);

            // If the request is successfully sent, the user entries are shown again
            this.newFriendshipInputActive = false;
            successful = true;
            System.out.println("Sent to: " + remoteUser);

        } catch (ClientOperationFailedException exception) {
            exception.printStackTrace();

        } finally {
            this.newFriendshipInputLock.unlock();
        }

        // So that the user sees instantly that the request has been sent
        if(successful) {
            activateFilterRequests(null);
        }
    }

    /**
     * Performs any tasks that are required to successfully stop the execution of the desktop app.
     */
    public void handleClosing() {
        // TODO implement method
        System.exit(0);
    }

    /**
     * Handles a single click in the given {@link UserEntryController}. Therefore, shows the chat history that
     * corresponds to the remote user associated with the entry, and initializes a communication with him if it is not
     * ready yet.
     *
     * @param userEntryController the {@link UserEntryController}.
     */
    public void handleEntryClick(UserEntryController userEntryController) {

        String remoteUser = userEntryController.getUsername();

        synchronized (this.retrievedRemoteUsers) {

            // When the user clicks on an user entry of a current online friend, a communication with him is established
            boolean isOnline = this.retrievedRemoteUsers.get(remoteUser).getStatus().equals(StatusType.ONLINE);

            if (isOnline) {
                try {
                    if (!this.clientFacade.isChatInitiated(remoteUser)) {
                        this.clientFacade.initiateChat(remoteUser);
                    }


                } catch (ClientOperationFailedException exception) {
                    exception.printStackTrace();
                }
            }

            // The chat history is redrawn event if the selected user is not online, to show the "no messages" warning
            synchronized (this.initiatedChats) {
                regenerateChatHistory(this.initiatedChats.get(userEntryController.getUsername()));
            }
        }

        // The previous selected entry is no longer highlighted, in favour of the new one
        if (this.currentSelectedEntry != null) {
            this.currentSelectedEntry.removeHighlighting();
        }

        this.currentSelectedEntry = userEntryController;
        this.currentSelectedEntry.addHighlighting();
    }

    /**
     * Handles a right click in the given {@link UserEntryController}. Therefore, shows a context menu that will allow:
     * - If the user is a current friend, the user will be able to end the friendship.
     * - If the user has sent to the client a friendship request, the user will be able to accept it or reject it.
     *
     * @param userEntryController the {@link UserEntryController}.
     * @param e                   {@link ContextMenuEvent} that triggered the context menu creation.
     */
    public void handleEntryRightClick(UserEntryController userEntryController, ContextMenuEvent e) {

        String remoteUser = userEntryController.getUsername();

        ContextMenu contextMenu = generateContextualMenu(remoteUser);

        // If no actions are available, the previous method will return null, so there's no need to show a contextual
        // menu
        if (contextMenu != null) {
            userEntryController.showContextMenu(generateContextualMenu(remoteUser), e);
        }
    }

    /**
     * Generates a contextual menu for the specified remote user:
     * - If the user is a current friend, the user will be able to end the friendship.
     * - If the user has sent to the client a friendship request, the user will be able to accept it or reject it.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @return the {@link ContextMenu}; if no actions are available, null will be returned instead.
     */
    private ContextMenu generateContextualMenu(String remoteUser) {

        ContextMenu menu = null;

        synchronized (this.retrievedRemoteUsers) {

            // The resulting contextual menu depends on which the relation exists between the remote user and the
            // client
            RemoteUser user = this.retrievedRemoteUsers.get(remoteUser);

            switch (user.getStatus()) {

                case ONLINE:
                case DISCONNECTED:
                    menu = new ContextMenu();

                    MenuItem itemEndFriendship = new MenuItem("End friendship");
                    itemEndFriendship.setOnAction(e -> {
                        try {
                            this.clientFacade.endFriendship(remoteUser);
                        } catch (ClientOperationFailedException exception) {
                            exception.printStackTrace();
                        }
                    });

                    menu.getItems().add(itemEndFriendship);

                    break;

                case FRIENDSHIP_RECEIVED:
                    menu = new ContextMenu();

                    MenuItem itemAccept = new MenuItem("Accept");
                    itemAccept.setOnAction(e -> {
                        try {
                            this.clientFacade.acceptFriendship(remoteUser);
                        } catch (ClientOperationFailedException exception) {
                            exception.printStackTrace();
                        }
                    });

                    MenuItem itemReject = new MenuItem("Reject");
                    itemReject.setOnAction(e -> {
                        try {
                            this.clientFacade.rejectFriendship(remoteUser);
                        } catch (ClientOperationFailedException exception) {
                            exception.printStackTrace();
                        }
                    });

                    menu.getItems().addAll(itemAccept, itemReject);

                    break;

                default:
                    break;
            }
        }

        return menu;
    }

    /**
     * If the user has typed in any text, sends a message to the selected user.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void sendMessage(Event e) {

        String message = this.textAreaMessage.getText().trim();

        if (!message.isBlank()) {

            synchronized (this.retrievedRemoteUsers) {

                // An user entry must be selected, and it must belong to an online friend
                if (this.currentSelectedEntry != null && this.retrievedRemoteUsers.get(
                        this.currentSelectedEntry.getUsername()).getStatus().equals(StatusType.ONLINE)) {

                    try {
                        this.clientFacade.sendMessage(this.currentSelectedEntry.getUsername(), message);
                        registerMessageInChatHistory(this.currentSelectedEntry.getUsername(), message, true);
                        this.textAreaMessage.clear();

                    } catch (ClientOperationFailedException exception) {
                        exception.printStackTrace();
                    }

                } else {
                    System.err.println("No current user has been selected to send the message");
                }
            }
        }
    }

    /**
     * Appends the incoming message to the chat that corresponds the specified remote user.
     *
     * @param remoteUser name by which the remote user that sent the message can be identified.
     * @param message    the message.
     */
    @Override
    public void forwardIncomingMessage(String remoteUser, String message) {

        // TODO enhance
        registerMessageInChatHistory(remoteUser, message, false);
    }

    /**
     * Registers the given message in the specified user's {@link ChatHistory}. If it does not already exist, it
     * creates one.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @param message    content of the message.
     * @param outgoing   if the message has been sent by the client.
     */
    private void registerMessageInChatHistory(String remoteUser, String message, boolean outgoing) {

        synchronized (this.initiatedChats) {

            ChatHistory chatHistory = this.initiatedChats.get(remoteUser);

            // A new chat history for the remote user is created if it did not exist yet
            if (chatHistory == null) {
                chatHistory = new ChatHistory();
                this.initiatedChats.put(remoteUser, chatHistory);
            }

            // The given message is registered
            chatHistory.addMessage(message, outgoing);

            regenerateChatHistory(chatHistory);
        }
    }

    /**
     * Updates the chatting section so that the shown messages correspond to the given {@link ChatHistory}. If no
     * messages have been stored in the history, or if it is null, an "empty chat" message is shown.
     *
     * @param chatHistory {@link ChatHistory} that contains the messages that will be shown.
     */
    private void regenerateChatHistory(ChatHistory chatHistory) {

        // Just in case another thread updates the current chat via LocalTunnelsListener
        Platform.runLater(() -> {

            ObservableList<Node> entries = this.messagesContainer.getChildren();
            entries.clear();

            if (chatHistory != null && chatHistory.getMessages().size() > 0) {

                List<String> messages = chatHistory.getMessages();
                List<Boolean> outgoing = chatHistory.getOutgoing();

                this.emptyChatWarning.setVisible(false);

                // They are supposed to have the same size
                for (int i = 0; i < messages.size(); i++) {

                    String message = messages.get(i);
                    boolean out = outgoing.get(i);

                    FXMLLoader loader;

                    try {

                        if (out) {
                            loader = new FXMLLoader(getClass().getResource("fxml/message_outgoing.fxml"));
                        } else {
                            loader = new FXMLLoader(getClass().getResource("fxml/message_incoming.fxml"));
                        }

                        // The message is appended to the currently shown messages
                        entries.add(loader.load());

                        // And its contents are set to the received data
                        MessageController controller = loader.getController();
                        controller.updateContents(message);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                this.emptyChatWarning.setVisible(true);
            }
        });
    }

    /**
     * Updates the currently shown user entries to reflect the status of the given {@link RemoteUser}.
     *
     * @param remoteUser the {@link RemoteUser} instance.
     */
    @Override
    public void forwardRemoteUserChange(RemoteUser remoteUser) {

        synchronized (this.retrievedRemoteUsers) {

            // If the remote user was already being shown
            if (this.retrievedRemoteUsers.containsKey(remoteUser.getUsername())) {
                updateUserEntry(remoteUser);
            }

            // Otherwise
            else {
                addUserEntry(remoteUser);
            }
        }
    }

    /**
     * Removes the given {@link RemoteUser}.
     *
     * @param remoteUser the {@link RemoteUser} instance.
     */
    @Override
    public void forwardRemoteUserDeletion(RemoteUser remoteUser) {

        synchronized (this.retrievedRemoteUsers) {
            removeUserEntry(remoteUser);
        }
    }

    /**
     * If the currently selected filter in the sidebar menu allows the given remote user's state, it creates a new
     * entry for him.
     *
     * @param remoteUser the {@link RemoteUser}.
     */
    private void addUserEntry(RemoteUser remoteUser) {

        // TODO enhance
        this.retrievedRemoteUsers.put(remoteUser.getUsername(), remoteUser);
        regenerateUserEntries();
    }

    /**
     * - If the currently selected filter in the sidebar menu allows the new remote user's state, it creates a new
     * entry for him if it does not already exist.
     * - If the currently selected filter in the sidebar menu does not allow the new remoter user's state, it removes
     * any existing entry for him.
     *
     * @param remoteUser the {@link RemoteUser}.
     */
    private void updateUserEntry(RemoteUser remoteUser) {

        // TODO enhance
        this.retrievedRemoteUsers.put(remoteUser.getUsername(), remoteUser);
        regenerateUserEntries();
    }

    /**
     * If the currently selected filter in the sidebar menu allows the given remote user's state, it removes any
     * existing entry for him.
     *
     * @param remoteUser the {@link RemoteUser}.
     */
    private void removeUserEntry(RemoteUser remoteUser) {

        // TODO enhance
        this.retrievedRemoteUsers.remove(remoteUser.getUsername());
        regenerateUserEntries();
    }

    /**
     * Removes the currently shown user entries and generates new ones accordingly to the selected filter in the
     * sidebar menu.
     */
    private void regenerateUserEntries() {

        // Just in case another thread updates the current users via RemoteUsersListener
        Platform.runLater(() -> {

            ObservableList<Node> entries = this.sidebarItems.getChildren();

            this.newFriendshipInputLock.lock();

            try {
                if (!this.newFriendshipInputActive) {

                    entries.clear();

                    for (RemoteUser remoteUser : this.retrievedRemoteUsers.values()) {

                        if (this.userEntriesFilter.isStatusTypeAllowed(remoteUser.getStatus())) {

                            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/sidebar_item.fxml"));

                            try {
                                // The user's entry is appended to the currently shown ones
                                entries.add(loader.load());

                                // And its contents are set to the received data
                                UserEntryController controller = loader.getController();
                                controller.updateContents(remoteUser.getUsername(), remoteUser.getUsername(),
                                        remoteUser.getStatus().toString());
                                controller.setMainWindowController(this);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } finally {
                this.newFriendshipInputLock.unlock();
            }
        });
    }
}
