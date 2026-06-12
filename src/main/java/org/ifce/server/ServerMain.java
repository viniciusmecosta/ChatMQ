package org.ifce.server;

import org.ifce.rmi.MessageBroker;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            MessageBroker broker = new MessageBrokerImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MessageBroker", broker);
            System.out.println("Servidor de mensagens iniciado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}