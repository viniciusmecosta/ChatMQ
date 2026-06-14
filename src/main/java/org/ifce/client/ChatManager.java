package org.ifce.client;

import org.ifce.model.Message;
import org.ifce.rmi.MessageBroker;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatManager {

    private final String clientName;
    private final List<String> contacts;
    private final List<Message> pendingMessages;
    private MessageBroker broker;
    private ChatClientImpl chatClient;
    private boolean isOnline;

    public ChatManager(String clientName) {
        this.clientName = clientName;
        this.contacts = new ArrayList<>();
        this.pendingMessages = new ArrayList<>();
        this.isOnline = false;
    }

    public void connect(String host, int port, Consumer<Message> onMessageReceived) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        this.broker = (MessageBroker) registry.lookup("MessageBroker");
        this.chatClient = new ChatClientImpl(onMessageReceived);
        this.broker.createQueue(clientName);
    }

    public void goOnline() throws Exception {
        if (!isOnline && broker != null) {
            broker.registerClient(clientName, chatClient);
            isOnline = true;

            for (Message msg : pendingMessages) {
                broker.sendMessage(msg);
            }
            pendingMessages.clear();
        }
    }

    public void goOffline() throws Exception {
        if (isOnline && broker != null) {
            broker.unregisterClient(clientName);
            isOnline = false;
        }
    }

    public boolean sendMessage(String receiver, String content) throws Exception {
        Message message = new Message(clientName, receiver.toLowerCase(), content, LocalDateTime.now());

        if (isOnline && broker != null) {
            broker.sendMessage(message);
            return true;
        } else {
            pendingMessages.add(message);
            return false;
        }
    }

    public void addContact(String contactName) {
        String normalized = contactName.toLowerCase();
        if (!contacts.contains(normalized)) {
            contacts.add(normalized);
        }
    }

    public void removeContact(String contactName) {
        contacts.remove(contactName.toLowerCase());
    }

    public List<String> getContacts() {
        return new ArrayList<>(contacts);
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getClientName() {
        return clientName;
    }
}