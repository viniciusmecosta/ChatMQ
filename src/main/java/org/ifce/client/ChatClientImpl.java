package org.ifce.client;

import org.ifce.model.Message;
import org.ifce.rmi.ChatClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {
    private final Consumer<Message> listener;

    public ChatClientImpl(Consumer<Message> listener) throws RemoteException {
        this.listener = listener;
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException {
        listener.accept(message);
    }
}