package org.ifce.server;

import org.ifce.rmi.ChatServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServerImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ChatServer", server);

            System.out.println("Servidor iniciado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}