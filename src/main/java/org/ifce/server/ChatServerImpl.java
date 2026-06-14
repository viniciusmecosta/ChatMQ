package org.ifce.server;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ifce.model.Message;
import org.ifce.rmi.ChatClient;
import org.ifce.rmi.ChatServer;

import jakarta.jms.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    private final Connection connection;
    private final Session session;
    private final Map<String, MessageConsumer> activeConsumers;

    public ChatServerImpl() throws Exception {
        super();
        this.activeConsumers = new ConcurrentHashMap<>();

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        factory.setTrustAllPackages(true);

        this.connection = factory.createConnection();
        this.connection.start();
        this.session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    }

    @Override
    public void createQueue(String clientName) throws RemoteException {
        try {
            session.createQueue(clientName);
        } catch (JMSException e) {
            throw new RemoteException("Erro ao criar fila no ActiveMQ", e);
        }
    }

    @Override
    public void registerClient(String clientName, ChatClient client) throws RemoteException {
        try {
            Queue queue = session.createQueue(clientName);
            MessageConsumer consumer = session.createConsumer(queue);

            consumer.setMessageListener(jmsMessage -> {
                try {
                    if (jmsMessage instanceof ObjectMessage objMsg) {
                        Message chatMessage = (Message) objMsg.getObject();
                        client.receiveMessage(chatMessage);
                        jmsMessage.acknowledge();
                    }
                } catch (RemoteException e) {
                    try { unregisterClient(clientName); } catch (Exception ignored) {}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            activeConsumers.put(clientName, consumer);
            System.out.println("Cliente conectado: " + clientName);
        } catch (JMSException e) {
            throw new RemoteException("Erro ao registrar cliente no ActiveMQ", e);
        }
    }

    @Override
    public void unregisterClient(String clientName) throws RemoteException {
        MessageConsumer consumer = activeConsumers.remove(clientName);
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendMessage(Message message) throws RemoteException {
        try {
            Queue queue = session.createQueue(message.receiver());
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            ObjectMessage objMsg = session.createObjectMessage(message);
            producer.send(objMsg);
            producer.close();
        } catch (JMSException e) {
            throw new RemoteException("Erro ao enviar mensagem via ActiveMQ", e);
        }
    }
}