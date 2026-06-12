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
    private MessageBroker broker;
    private ChatClientImpl chatClient;
    private boolean isOnline;

    public ChatManager(String clientName) {
        this.clientName = clientName;
        this.contacts = new ArrayList<>();
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
        }
    }

    public void goOffline() throws Exception {
        if (isOnline && broker != null) {
            broker.unregisterClient(clientName);
            isOnline = false;
        }
    }

    public void sendMessage(String receiver, String content) throws Exception {
        if (broker != null) {
            Message message = new Message(clientName, receiver, content, LocalDateTime.now());
            broker.sendMessage(message);
        }
    }

    public void addContact(String contactName) {
        if (!contacts.contains(contactName)) {
            contacts.add(contactName);
        }
    }

    public void removeContact(String contactName) {
        contacts.remove(contactName);
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