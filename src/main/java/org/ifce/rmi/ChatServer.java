package org.ifce.rmi;

import org.ifce.model.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer extends Remote {
    void createQueue(String clientName) throws RemoteException;

    void registerClient(String clientName, ChatClient client) throws RemoteException;

    void unregisterClient(String clientName) throws RemoteException;

    void sendMessage(Message message) throws RemoteException;
}