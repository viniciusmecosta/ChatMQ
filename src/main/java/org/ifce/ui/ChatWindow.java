package org.ifce.ui;

import org.ifce.client.ChatManager;
import org.ifce.model.Message;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ChatWindow extends JFrame {
    private final ChatManager chatManager;
    private final DefaultListModel<String> contactListModel;
    private final JList<String> contactList;
    private final JTextArea messageArea;
    private final JTextField inputField;
    private final JButton toggleStatusButton;
    private final DateTimeFormatter timeFormatter;

    public ChatWindow(ChatManager chatManager) {
        this.chatManager = chatManager;
        this.contactListModel = new DefaultListModel<>();
        this.contactList = new JList<>(contactListModel);
        this.messageArea = new JTextArea();
        this.inputField = new JTextField();
        this.toggleStatusButton = new JButton("Ficar Online");
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        setupUI();
        setupActions();
    }

    private void setupUI() {
        setTitle("ChatMQ - " + chatManager.getClientName());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(150, 0));
        rightPanel.add(new JLabel("Contatos", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(contactList), BorderLayout.CENTER);

        JPanel contactButtonsPanel = new JPanel(new GridLayout(1, 2));
        JButton addContactBtn = new JButton("+");
        JButton removeContactBtn = new JButton("-");
        contactButtonsPanel.add(addContactBtn);
        contactButtonsPanel.add(removeContactBtn);
        rightPanel.add(contactButtonsPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(toggleStatusButton, BorderLayout.WEST);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        JButton sendButton = new JButton("Enviar");
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupActions() {
        toggleStatusButton.addActionListener(e -> toggleOnlineStatus());
        ((JButton) ((JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(2)).getComponent(0)).addActionListener(e -> addContact());
        ((JButton) ((JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(2)).getComponent(1)).addActionListener(e -> removeContact());

        JButton sendButton = (JButton) ((JPanel) getContentPane().getComponent(2)).getComponent(2);
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void toggleOnlineStatus() {
        try {
            if (chatManager.isOnline()) {
                chatManager.goOffline();
                toggleStatusButton.setText("Ficar Online");
                appendLog("Sistema: Você está offline.");
            } else {
                chatManager.goOnline();
                toggleStatusButton.setText("Ficar Offline");
                appendLog("Sistema: Você está online.");
            }
        } catch (Exception ex) {
            appendLog("Erro ao alterar status: " + ex.getMessage());
        }
    }

    private void addContact() {
        String name = JOptionPane.showInputDialog(this, "Nome do contato:");
        if (name != null && !name.trim().isEmpty()) {
            chatManager.addContact(name.trim());
            updateContactList();
        }
    }

    private void removeContact() {
        String selected = contactList.getSelectedValue();
        if (selected != null) {
            chatManager.removeContact(selected);
            updateContactList();
        }
    }

    private void updateContactList() {
        contactListModel.clear();
        chatManager.getContacts().forEach(contactListModel::addElement);
    }

    private void sendMessage() {
        String content = inputField.getText().trim();
        String selectedContact = contactList.getSelectedValue();

        if (content.isEmpty() || selectedContact == null) {
            JOptionPane.showMessageDialog(this, "Selecione um contato e digite uma mensagem.");
            return;
        }

        try {
            chatManager.sendMessage(selectedContact, content);
            appendLog("Você -> " + selectedContact + ": " + content);
            inputField.setText("");
        } catch (Exception ex) {
            appendLog("Erro ao enviar: " + ex.getMessage());
        }
    }

    public void displayMessage(Message message) {
        String time = message.timestamp().format(timeFormatter);
        appendLog(String.format("[%s] %s: %s", time, message.sender(), message.content()));
    }

    private void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(text + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
}