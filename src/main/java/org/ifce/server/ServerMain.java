package org.ifce.server;

import org.ifce.rmi.ChatServer;

import java.rmi.registry.LocateRegistry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServerImpl();
            LocateRegistry.createRegistry(1099).rebind("ChatServer", server);
            System.out.println("Servidor RMI iniciado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}