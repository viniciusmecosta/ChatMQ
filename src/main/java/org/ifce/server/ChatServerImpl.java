package org.ifce.server;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ifce.model.Message;
import org.ifce.rmi.ChatClient;
import org.ifce.rmi.ChatServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {
    private final Session session;
    private final Map<String, MessageConsumer> consumers = new ConcurrentHashMap<>();

    public ChatServerImpl() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        factory.setTrustAllPackages(true);
        Connection conn = factory.createConnection();
        conn.start();
        session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    }

    @Override
    public void createQueue(String name) throws RemoteException {
        try {
            session.createQueue(name);
        } catch (Exception e) {
            throw new RemoteException("Erro", e);
        }
    }

    @Override
    public void registerClient(String name, ChatClient client) throws RemoteException {
        try {
            MessageConsumer consumer = session.createConsumer(session.createQueue(name));
            consumer.setMessageListener(msg -> {
                try {
                    client.receiveMessage((Message) ((ObjectMessage) msg).getObject());
                    msg.acknowledge();
                } catch (Exception e) {
                    try {
                        unregisterClient(name);
                    } catch (Exception ignored) {
                    }
                }
            });
            consumers.put(name, consumer);
            System.out.println("Cliente conectado: " + name);
        } catch (Exception e) {
            throw new RemoteException("Erro", e);
        }
    }

    @Override
    public void unregisterClient(String name) throws RemoteException {
        try {
            MessageConsumer c = consumers.remove(name);
            if (c != null) c.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void sendMessage(Message msg) throws RemoteException {
        try {
            MessageProducer producer = session.createProducer(session.createQueue(msg.receiver()));
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(session.createObjectMessage(msg));
            producer.close();
        } catch (Exception e) {
            throw new RemoteException("Erro", e);
        }
    }
}