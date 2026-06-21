package org.ifce.client;

import org.ifce.model.Message;
import org.ifce.rmi.ChatServer;

import java.rmi.registry.LocateRegistry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatManager {
    private final String name;
    private final List<String> contacts = new ArrayList<>();
    private final List<Message> pending = new ArrayList<>();
    private ChatServer server;
    private ChatClientImpl client;
    private boolean online = false;
    private String host;
    private int port;

    public ChatManager(String name) {
        this.name = name;
    }

    public void setup(String host, int port, Consumer<Message> onMsg) throws Exception {
        this.host = host;
        this.port = port;
        this.client = new ChatClientImpl(onMsg);
    }

    public void goOnline() throws Exception {
        if (!online) {
            server = (ChatServer) LocateRegistry.getRegistry(host, port).lookup("ChatServer");
            server.createQueue(name);
            server.registerClient(name, client);
            online = true;

            for (Message m : pending) {
                server.sendMessage(m);
            }
            pending.clear();
        }
    }

    public void goOffline() throws Exception {
        if (online && server != null) {
            try {
                server.unregisterClient(name);
            } catch (Exception ignored) {}
            server = null;
            online = false;
        }
    }

    public boolean sendMessage(String to, String content) throws Exception {
        Message m = new Message(name, to.toLowerCase(), content, LocalDateTime.now());
        if (online && server != null) {
            server.sendMessage(m);
            return true;
        }
        pending.add(m);
        return false;
    }

    public void addContact(String c) {
        String n = c.toLowerCase();
        if (!n.equals(name) && !contacts.contains(n)) {
            contacts.add(n);
        }
    }

    public void removeContact(String c) {
        contacts.remove(c.toLowerCase());
    }

    public List<String> getContacts() {
        return new ArrayList<>(contacts);
    }

    public boolean isOnline() {
        return online;
    }

    public String getClientName() {
        return name;
    }
}