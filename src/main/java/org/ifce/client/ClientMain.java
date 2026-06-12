package org.ifce.client;

import org.ifce.ui.ChatWindow;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        String clientName = JOptionPane.showInputDialog("Digite seu nome de usuário:");

        if (clientName == null || clientName.trim().isEmpty()) {
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                ChatManager manager = new ChatManager(clientName.trim());
                ChatWindow window = new ChatWindow(manager);

                manager.connect("localhost", 1099, window::displayMessage);

                window.setLocationRelativeTo(null);
                window.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao conectar ao servidor: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}