package com.goldardieste.javagram.desktopapp.identifieduser;

import com.goldardieste.javagram.client.exposed.ClientFacade;
import com.goldardieste.javagram.client.exposed.ClientOperationFailedException;
import com.goldardieste.javagram.client.exposed.LocalTunnelsListener;
import com.goldardieste.javagram.client.exposed.RemoteUsersListener;
import com.goldardieste.javagram.common.datacontainers.RemoteUser;
import com.goldardieste.javagram.common.StatusType;
import com.goldardieste.javagram.desktopapp.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

// TODO coded in a bit of a rush, so the best choices have not been definitely made
// TODO message input should grow to comfortably type long messages
// TODO multi-line messages break the messages' boxes (they do not grow in height accordingly)
// TODO all messages should have a timestamp that represents when they were sent, which would also be
//  displayed in the GUI
/**
 * This class represents the FXML controller that orchestrates the main window of the desktop app; that is, provides
 * most of the app's funcionality to the user.
 */
public class MainWindowController extends AbstractController implements LocalTunnelsListener, RemoteUsersListener {

    /* ----- Attributes ----- */

    /**
     * Provides all the back-end functionality to the desktop app.
     * <p>
     * It is only accessed by the JavaFX thread.
     */
    private ClientFacade clientFacade;

    /**
     * Determines which status the retrieved remote users must have to be shown as entries.
     * <p>
     * It is only accessed by the JavaFX thread.
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
     * <p>
     * It is only accessed by the JavaFX thread.
     */
    private UserEntryController currentSelectedEntry;

    /**
     * Contains, for each remote user, how many messages he has send and that have not been read yet by the client.
     */
    private final Map<String, Integer> unreadMessagesCount;

    /**
     * If it is set to true, the user's entries will not be rendered no matter what, as their place is being taken by
     * the input where the user may send a new friendship request, or where the user may change his current password.
     * <p>
     * It is only modified by the JavaFX thread; other threads may read it.
     */
    private boolean overlappedInputActive;

    /**
     * This lock must be acquired to read/modify {@link #overlappedInputActive}. Even though all events from JavaFX
     * are dispatched using a single thread, as the notifications about remote users can be received and displayed at
     * any time due to {@link RemoteUsersListener}, there could be race conditions if it where not for this lock.
     */
    private final ReentrantLock overlappingInputLock;


    /* ----- FXML attributes ----- */

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
     * Left menu's option to show the menu to change the user's current password.
     */
    @FXML
    private HBox menuOptionUpdatePassword;

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
     * Button that sends the currently typed message to the selected remote user.
     */
    @FXML
    private Button btnSend;

    /**
     * Contains {@link #messagesContainer}.
     */
    @FXML
    private ScrollPane messagesScrollPane;

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
        this.unreadMessagesCount = new HashMap<>();
        this.overlappedInputActive = false;
        this.overlappingInputLock = new ReentrantLock();

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
       NOTE: some methods will only be called from the JavaFX thread, just as happens with reading/writing certain
             variables; therefore, race conditions are not a concern due to JavaFX being a single threaded event
             dispatcher model.
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

        this.overlappingInputLock.lock();
        this.overlappedInputActive = false;
        this.overlappingInputLock.unlock();

        synchronized (this.retrievedRemoteUsers) {
            regenerateUserEntries();
        }

        updateMenuOptionsHighlighting(0);
    }

    /**
     * Sets the users entries' filter to only show current friends.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void activateFilterFriends(Event e) {

        this.userEntriesFilter = UserEntriesFilter.CURRENT_FRIENDS;

        this.overlappingInputLock.lock();
        this.overlappedInputActive = false;
        this.overlappingInputLock.unlock();

        synchronized (this.retrievedRemoteUsers) {
            regenerateUserEntries();
        }

        updateMenuOptionsHighlighting(1);
    }

    /**
     * Sets the users entries' filter to only show sent or received friendship requests.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void activateFilterRequests(Event e) {

        this.userEntriesFilter = UserEntriesFilter.REQUESTS;

        this.overlappingInputLock.lock();
        this.overlappedInputActive = false;
        this.overlappingInputLock.unlock();

        synchronized (this.retrievedRemoteUsers) {
            regenerateUserEntries();
        }

        updateMenuOptionsHighlighting(2);
    }

    /**
     * Closes the currently opened session.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void logout(Event e) {
        handleClosing();
        getStage().close();
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

        this.overlappingInputLock.lock();

        try {
            // So that the user entries do not override
            this.overlappedInputActive = true;

            entries.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/input_new_friendship.fxml"));

            // The field where the remote user's username will be typed is shown where the user entries usually
            // are, along with the button to send the request
            entries.add(loader.load());

            InputNewFriendController controller = loader.getController();
            controller.setMainWindowController(this);

        } catch (IOException exception) {
            exception.printStackTrace();

        } finally {
            this.overlappingInputLock.unlock();
        }

        updateMenuOptionsHighlighting(3);
    }

    /**
     * Sends a friendship request to the specified remote user, and shows again all the user entries if successful.
     *
     * @param remoteUser name by which the remote user can be identified.
     * @return if the request could be sent successfully.
     */
    public boolean sendFriendshipRequest(String remoteUser) {

        boolean successful = false;

        this.overlappingInputLock.lock();

        try {
            this.clientFacade.requestFriendship(remoteUser);

            // If the request is successfully sent, the user entries are shown again
            this.overlappedInputActive = false;
            successful = true;

        } catch (ClientOperationFailedException exception) {
            exception.printStackTrace();

        } finally {
            this.overlappingInputLock.unlock();
        }

        // So that the user sees instantly that the request has been sent
        if (successful) {
            activateFilterRequests(null);
        }

        return successful;
    }

    /**
     * Shows an input where the user may change his current password.
     *
     * @param e associated {@link Event}.
     */
    @FXML
    public void showChangePasswordInput(Event e) {

        ObservableList<Node> entries = this.sidebarItems.getChildren();

        this.overlappingInputLock.lock();

        try {
            // So that the user entries do not override
            this.overlappedInputActive = true;

            entries.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/input_change_password.fxml"));

            entries.add(loader.load());

            InputChangePasswordController controller = loader.getController();
            controller.setMainWindowController(this);

        } catch (IOException exception) {
            exception.printStackTrace();

        } finally {
            this.overlappingInputLock.unlock();
        }

        updateMenuOptionsHighlighting(4);
    }

    /**
     * Updates the password of the current user, and shows again all the user entries if successful.
     *
     * @param currentPassword password given as the user's current password.
     * @param newPassword password given as the user's new password.
     * @return if the password could be changed successfully.
     */
    public boolean updatePassword(String currentPassword, String newPassword) {

        boolean successful = false;

        this.overlappingInputLock.lock();

        try {
            this.clientFacade.updatePassword(currentPassword, newPassword);

            // If the password has been successfully changed, the user entries are shown again
            this.overlappedInputActive = false;
            successful = true;

        } catch (ClientOperationFailedException exception) {
            exception.printStackTrace();

        } finally {
            this.overlappingInputLock.unlock();
        }

        // All users will be now shown
        if (successful) {
            removeFilter(null);
        }

        return successful;
    }

    /**
     * Removes the highlighting from all the left menu's options, except from the one that is specified through its
     * index. The entry at the top has an index of 0, and the one at the bottom has han index of 3 (logout option is
     * not considered).
     *
     * @param indexHighlight index of the menu option that is going to be highlighted.
     */
    private void updateMenuOptionsHighlighting(int indexHighlight) {

        ObservableList<String> styles;

        // 0 -> no filter
        styles = this.menuOptionAllUsers.getStyleClass();

        if (indexHighlight == 0) {
            if (!styles.contains("sidebarMenuItemSelected")) {
                styles.add("sidebarMenuItemSelected");
            }
            if (!styles.contains("sidebarMenuItemNoFilterSelected")) {
                styles.add("sidebarMenuItemNoFilterSelected");
            }
        } else {
            styles.remove("sidebarMenuItemSelected");
            styles.remove("sidebarMenuItemNoFilterSelected");
        }

        // 1 -> all friends
        styles = this.menuOptionFriends.getStyleClass();

        if (indexHighlight == 1) {
            if (!styles.contains("sidebarMenuItemSelected")) {
                styles.add("sidebarMenuItemSelected");
            }
            if (!styles.contains("sidebarMenuItemCurrentFriendsSelected")) {
                styles.add("sidebarMenuItemCurrentFriendsSelected");
            }
        } else {
            styles.remove("sidebarMenuItemSelected");
            styles.remove("sidebarMenuItemCurrentFriendsSelected");
        }

        // 2 -> requests
        styles = this.menuOptionRequests.getStyleClass();

        if (indexHighlight == 2) {
            if (!styles.contains("sidebarMenuItemSelected")) {
                styles.add("sidebarMenuItemSelected");
            }
            if (!styles.contains("sidebarMenuItemRequestsSelected")) {
                styles.add("sidebarMenuItemRequestsSelected");
            }
        } else {
            styles.remove("sidebarMenuItemSelected");
            styles.remove("sidebarMenuItemRequestsSelected");
        }

        // 3 -> send new request
        styles = this.menuOptionNewFriend.getStyleClass();

        if (indexHighlight == 3) {
            if (!styles.contains("sidebarMenuItemSelected")) {
                styles.add("sidebarMenuItemSelected");
            }
            if (!styles.contains("sidebarMenuItemNewFriendSelected")) {
                styles.add("sidebarMenuItemNewFriendSelected");
            }
        } else {
            styles.remove("sidebarMenuItemSelected");
            styles.remove("sidebarMenuItemNewFriendSelected");
        }

        // 4 -> change password
        styles = this.menuOptionUpdatePassword.getStyleClass();

        if (indexHighlight == 4) {
            if (!styles.contains("sidebarMenuItemSelected")) {
                styles.add("sidebarMenuItemSelected");
            }
            if (!styles.contains("sidebarMenuItemPasswordSelected")) {
                styles.add("sidebarMenuItemPasswordSelected");
            }
        } else {
            styles.remove("sidebarMenuItemSelected");
            styles.remove("sidebarMenuItemPasswordSelected");
        }
    }

    /**
     * Performs any tasks that are required to successfully stop the execution of the desktop app.
     */
    public void handleClosing() {

        try {
            this.clientFacade.disconnect();
            // The app will just end its execution instead of asking the user if he wants to sign up/sign in to a new
            // account
            this.clientFacade.haltExecution();

        } catch (ClientOperationFailedException exception) {
            System.err.println("Execution of the Javagram client could not be successfully stopped");
            exception.printStackTrace();
        }
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

            // Shows the chat history with the selected user, if any
            initiateAndShowChat(remoteUser);

            // If the current selected entry had any not-read-messages counter, it gets reset as the corresponding
            // message history will be rendered
            Integer counter = this.unreadMessagesCount.get(remoteUser);

            if(counter != null) {
                this.unreadMessagesCount.put(remoteUser, 0);
            }

            this.currentSelectedEntry = userEntryController;

            // This method updates the message input area and puts highlighting to the appropriate entry
            // TODO highly inefficient
            regenerateUserEntries();
        }
    }

    /**
     * Initiates the communication with the specified remote user, which is supposed to be online, and renders again
     * the chat region to show that communication's contents.
     *
     * @param remoteUser name by which the remote user can be identified.
     */
    private void initiateAndShowChat(String remoteUser) {

        // When the user clicks on an user entry of a current online friend, a communication with him is
        // established
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

        // The chat history is redrawn even if the selected user is not online, to show the "no messages" warning
        // Thread-safe access is guaranteed through this.retrievedRemoteUsers
        regenerateChatHistory(this.initiatedChats.get(remoteUser));
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
                }
            }
        }
    }

    /**
     * Checks if the user has pressed the return key while typing a message. In such case, the app tries to send the
     * currently typed in message, if any.
     *
     * @param e associated {@link KeyEvent}.
     */
    @FXML
    public void checkSendingViaReturnKey(KeyEvent e) {

        if (e.getCode().equals(KeyCode.ENTER)) {

            // If CTRL is not pressed
            if (!e.isControlDown()) {
                // A send operation is attempted
                sendMessage(null);
            } else {
                // Otherwise, a carriage return is appended to the text field
                this.textAreaMessage.appendText(System.getProperty("line.separator"));
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

        synchronized (this.retrievedRemoteUsers) {

            // The remote user's counter is also incremented if his entry is not currently selected
            if(this.currentSelectedEntry == null || !this.currentSelectedEntry.getUsername().equals(remoteUser)) {

                Integer counter = this.unreadMessagesCount.get(remoteUser);

                int newValue = (counter != null ? counter : 0) + 1;
                this.unreadMessagesCount.put(remoteUser, newValue);

                regenerateUserEntries();
            }
        }
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

        synchronized (this.retrievedRemoteUsers) {

            ChatHistory chatHistory = this.initiatedChats.get(remoteUser);

            // A new chat history for the remote user is created if it did not exist yet
            if (chatHistory == null) {
                chatHistory = new ChatHistory();
                this.initiatedChats.put(remoteUser, chatHistory);
            }

            // The given message is registered
            chatHistory.addMessage(message, outgoing);

            if (this.currentSelectedEntry != null && this.currentSelectedEntry.getUsername().equals(remoteUser)) {
                regenerateChatHistory(chatHistory);
            }
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
                            loader = new FXMLLoader(getClass().getResource("../fxml/message_outgoing.fxml"));
                        } else {
                            loader = new FXMLLoader(getClass().getResource("../fxml/message_incoming.fxml"));
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

            this.overlappingInputLock.lock();

            try {
                if (!this.overlappedInputActive) {

                    entries.clear();

                    // If the previously selected entry has been found again or not
                    boolean previousEntryFound = false;

                    for (RemoteUser remoteUser : this.retrievedRemoteUsers.values()) {

                        if (this.userEntriesFilter.isStatusTypeAllowed(remoteUser.getStatus())) {

                            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/sidebar_item.fxml"));

                            try {
                                // The user's entry is appended to the currently shown ones
                                entries.add(loader.load());

                                UserEntryController controller = loader.getController();

                                // If the corresponding user has any not-read-messages counter, it also gets shown
                                Integer counter = this.unreadMessagesCount.get(remoteUser.getUsername());

                                controller.updateContents(remoteUser.getUsername(), remoteUser.getUsername(),
                                        remoteUser.getStatus().toString(), counter != null ? counter : 0);
                                controller.setMainWindowController(this);

                                // Due to removing all entries to generate them again in every minor change, the one
                                // that is equal to the previously selected one must be set as the selected one
                                if (this.currentSelectedEntry != null &&
                                        this.currentSelectedEntry.getUsername().equals(controller.getUsername())) {

                                    previousEntryFound = true;

                                    this.currentSelectedEntry = controller;
                                    this.currentSelectedEntry.addHighlighting();

                                    // And, if the user is now online, it could be that he was previously a pending
                                    // friend, so the communication with him must be established
                                    if (this.retrievedRemoteUsers.get(this.currentSelectedEntry.getUsername())
                                            .getStatus().equals(StatusType.ONLINE)) {
                                        initiateAndShowChat(this.currentSelectedEntry.getUsername());
                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // If the previously selected entry has not been found, the currently selected one is set to null
                    if (!previousEntryFound) {
                        this.currentSelectedEntry = null;
                    }

                    // The scroll of the container of all messages is set to the maximum, so that the new message is
                    // shown
                    // https://stackoverflow.com/questions/26152642/get-the-height-of-a-node-in-javafx-generate-a-layout-pass
                    this.messagesScrollPane.applyCss();
                    this.messagesScrollPane.layout();
                    this.messagesScrollPane.setVvalue(1.0);
                }
            } finally {
                this.overlappingInputLock.unlock();
            }

            updateMessageArea();
        });
    }

    /**
     * Checks if the currently selected remote user is online or not:
     * <p>
     * - If it is only, the user may type in a message in {@link #textAreaMessage} and send it.
     * - Otherwise, both {@link #textAreaMessage} and {@link #btnSend} are disabled, showing a message that says the
     * selected user is not online if it is a friend, or that the user is not friends with him yet.
     */
    private void updateMessageArea() {

        boolean entryIsSelected = this.currentSelectedEntry != null;
        StatusType status = entryIsSelected ?
                this.retrievedRemoteUsers.get(this.currentSelectedEntry.getUsername()).getStatus() : null;

        // If no user has been selected or if the selected one is not online
        if (!entryIsSelected || !status.equals(StatusType.ONLINE)) {

            this.btnSend.setDisable(true);
            this.textAreaMessage.setDisable(true);

            // If no user has been selected
            if (!entryIsSelected) {
                this.textAreaMessage.setText("No user has been selected");
            }

            // If the selected one is not online
            else {
                // If the selected one is a friend that is offline
                if (status.equals(StatusType.DISCONNECTED)) {
                    this.textAreaMessage.setText("The selected friend is not online");
                }

                // Otherwise, the user and the client are not current friends, so they cannot communicate
                else {
                    this.textAreaMessage.setText("You are not friends with the selected user");
                }
            }
        }

        // If the selected user is online
        else {
            this.btnSend.setDisable(false);
            this.textAreaMessage.setDisable(false);
            this.textAreaMessage.clear();
        }
    }
}
