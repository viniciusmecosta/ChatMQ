package org.ifce.rmi;

import org.ifce.model.Message;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatClient extends Remote {
    void receiveMessage(Message message) throws RemoteException;
}