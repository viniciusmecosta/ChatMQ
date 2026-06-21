package org.ifce.client;

import org.ifce.ui.ChatWindow;
import javax.swing.*;
import java.awt.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JLabel("Digite seu nome:"), BorderLayout.NORTH);
            JTextField field = new JTextField(15);
            p.add(field, BorderLayout.CENTER);

            if (JOptionPane.showConfirmDialog(null, p, "Entrar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != 0) {
                System.exit(0);
            }

            String name = field.getText().trim().toLowerCase();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "O nome de usuário não pode ser vazio.");
                System.exit(0);
            }

            try {
                ChatManager manager = new ChatManager(name);
                ChatWindow window = new ChatWindow(manager);

                manager.configure("localhost", 1099, window::displayMessage);
                window.toggleStatus();

                window.setLocationRelativeTo(null);
                window.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}