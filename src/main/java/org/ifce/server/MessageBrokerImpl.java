package org.ifce.server;

import org.ifce.model.Message;
import org.ifce.rmi.ChatClient;
import org.ifce.rmi.MessageBroker;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageBrokerImpl extends UnicastRemoteObject implements MessageBroker {

    private final Map<String, ChatClient> onlineClients;
    private final Map<String, Queue<Message>> offlineQueues;

    public MessageBrokerImpl() throws RemoteException {
        super();
        this.onlineClients = new ConcurrentHashMap<>();
        this.offlineQueues = new ConcurrentHashMap<>();
    }

    @Override
    public void createQueue(String clientName) throws RemoteException {
        offlineQueues.putIfAbsent(clientName, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void registerClient(String clientName, ChatClient client) throws RemoteException {
        onlineClients.put(clientName, client);
        deliverOfflineMessages(clientName, client);
    }

    @Override
    public void unregisterClient(String clientName) throws RemoteException {
        onlineClients.remove(clientName);
    }

    @Override
    public void sendMessage(Message message) throws RemoteException {
        String receiver = message.receiver();
        ChatClient client = onlineClients.get(receiver);

        if (client != null) {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                unregisterClient(receiver);
                queueMessage(receiver, message);
            }
        } else {
            queueMessage(receiver, message);
        }
    }

    private void queueMessage(String receiver, Message message) {
        Queue<Message> queue = offlineQueues.get(receiver);
        if (queue != null) {
            queue.add(message);
        }
    }

    private void deliverOfflineMessages(String clientName, ChatClient client) {
        Queue<Message> queue = offlineQueues.get(clientName);
        if (queue != null) {
            while (!queue.isEmpty()) {
                Message message = queue.poll();
                try {
                    client.receiveMessage(message);
                } catch (RemoteException e) {
                    queueMessage(clientName, message);
                    break;
                }
            }
        }
    }
}