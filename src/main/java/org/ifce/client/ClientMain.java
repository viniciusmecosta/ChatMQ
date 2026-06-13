package org.ifce.client;

import org.ifce.ui.ChatWindow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JPanel loginPanel = new JPanel(new BorderLayout(0, 10));
            loginPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel titleLabel = new JLabel("ChatMQ");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            loginPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel inputPanel = new JPanel(new BorderLayout(0, 5));
            inputPanel.add(new JLabel("Digite seu nome:"), BorderLayout.NORTH);
            JTextField usernameField = new JTextField();
            usernameField.setPreferredSize(new Dimension(250, 35));
            usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            inputPanel.add(usernameField, BorderLayout.CENTER);

            loginPanel.add(inputPanel, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(null, loginPanel, "Entrar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                System.exit(0);
            }

            String clientName = usernameField.getText();
            if (clientName == null || clientName.trim().isEmpty()) {
                System.exit(0);
            }

            try {
                ChatManager manager = new ChatManager(clientName.trim());
                ChatWindow window = new ChatWindow(manager);

                manager.connect("localhost", 1099, window::displayMessage);

                window.setLocationRelativeTo(null);
                window.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao conectar ao servidor: " + e.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}