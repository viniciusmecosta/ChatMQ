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
    private final Map<String, ChatClient> onlineClients = new ConcurrentHashMap<>();

    public ChatServerImpl() throws Exception {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        factory.setTrustAllPackages(true);
        Connection conn = factory.createConnection();
        conn.start();
        session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    }

    @Override
    public void createQueue(String name) throws RemoteException {
        try { session.createQueue(name); } catch (Exception e) { throw new RemoteException("Erro", e); }
    }

    @Override
    public void registerClient(String name, ChatClient client) throws RemoteException {
        onlineClients.put(name, client);
        System.out.println("Cliente online: " + name);

        try {
            MessageConsumer consumer = session.createConsumer(session.createQueue(name));
            jakarta.jms.Message jmsMsg;
            while ((jmsMsg = consumer.receiveNoWait()) != null) {
                if (jmsMsg instanceof ObjectMessage objMsg) {
                    client.receiveMessage((Message) objMsg.getObject());
                }
                jmsMsg.acknowledge();
            }
            consumer.close();
        } catch (Exception e) { throw new RemoteException("Erro", e); }
    }

    @Override
    public void unregisterClient(String name) throws RemoteException {
        onlineClients.remove(name);
        System.out.println("Cliente offline: " + name);
    }

    @Override
    public void sendMessage(Message msg) throws RemoteException {
        ChatClient receiver = onlineClients.get(msg.receiver());

        if (receiver != null) {
            try {
                receiver.receiveMessage(msg);
            } catch (RemoteException e) {
                onlineClients.remove(msg.receiver());
                sendToActiveMQ(msg);
            }
        } else {
            sendToActiveMQ(msg);
        }
    }

    private void sendToActiveMQ(Message msg) throws RemoteException {
        try {
            MessageProducer producer = session.createProducer(session.createQueue(msg.receiver()));
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(session.createObjectMessage(msg));
            producer.close();
        } catch (Exception e) { throw new RemoteException("Erro ao salvar no broker", e); }
    }
}