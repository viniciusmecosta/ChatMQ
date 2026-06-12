package org.ifce.client;

import org.ifce.model.Message;
import org.ifce.rmi.ChatClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    private final Consumer<Message> messageListener;

    public ChatClientImpl(Consumer<Message> messageListener) throws RemoteException {
        super();
        this.messageListener = messageListener;
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException {
        messageListener.accept(message);
    }
}