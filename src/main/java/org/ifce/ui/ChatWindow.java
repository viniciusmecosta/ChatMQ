package org.ifce.ui;

import org.ifce.client.ChatManager;
import org.ifce.model.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatWindow extends JFrame {
    private final ChatManager chatManager;
    private final DefaultListModel<String> contactListModel;
    private final JList<String> contactList;
    private final CardLayout cardLayout;
    private final JPanel chatCards;
    private final Map<String, JPanel> chatPanels;
    private final Map<String, JScrollPane> chatScrolls;
    private final List<JLabel> pendingStatusLabels;
    private final JTextField inputField;
    private final JButton toggleStatusButton;
    private final JButton sendButton;
    private final DateTimeFormatter timeFormatter;

    private final boolean isDarkTheme;
    private final Color bgAppColor;
    private final Color bgChatColor;
    private final Color bubbleMeColor;
    private final Color bubbleOtherColor;
    private final Color textMainColor;
    private final Color textSubColor;
    private final Color inputBgColor;

    private String selectedContact = null;

    public ChatWindow(ChatManager chatManager) {
        this.chatManager = chatManager;
        this.contactListModel = new DefaultListModel<>();
        this.cardLayout = new CardLayout();
        this.chatCards = new JPanel(cardLayout);
        this.chatPanels = new HashMap<>();
        this.chatScrolls = new HashMap<>();
        this.pendingStatusLabels = new ArrayList<>();
        this.inputField = new JTextField();
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        Color sysBg = UIManager.getColor("Panel.background");
        this.isDarkTheme = (sysBg != null && (sysBg.getRed() + sysBg.getGreen() + sysBg.getBlue()) / 3 < 128);

        if (isDarkTheme) {
            bgAppColor = new Color(32, 44, 51);
            bgChatColor = new Color(11, 20, 26);
            bubbleMeColor = new Color(0, 92, 75);
            bubbleOtherColor = new Color(32, 44, 51);
            textMainColor = new Color(233, 237, 239);
            textSubColor = new Color(134, 150, 160);
            inputBgColor = new Color(42, 57, 66);
        } else {
            bgAppColor = new Color(240, 242, 245);
            bgChatColor = new Color(229, 221, 213);
            bubbleMeColor = new Color(217, 253, 211);
            bubbleOtherColor = Color.WHITE;
            textMainColor = new Color(17, 27, 33);
            textSubColor = new Color(102, 119, 129);
            inputBgColor = Color.WHITE;
        }

        this.contactList = new JList<>(contactListModel);
        this.toggleStatusButton = createSimpleButton("Offline", new Color(108, 117, 125));
        this.sendButton = createSimpleButton("Enviar", new Color(0, 168, 132));

        setupUI();
        setupActions();
    }

    private JButton createSimpleButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupUI() {
        setTitle("ChatMQ - " + chatManager.getClientName());
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(bgAppColor);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, isDarkTheme ? new Color(50, 50, 50) : new Color(210, 210, 210)));

        JPanel topSidebar = new JPanel(new BorderLayout());
        topSidebar.setBackground(bgAppColor);
        topSidebar.setBorder(new EmptyBorder(15, 15, 15, 15));

        toggleStatusButton.setPreferredSize(new Dimension(0, 40));
        topSidebar.add(toggleStatusButton, BorderLayout.CENTER);
        sidebar.add(topSidebar, BorderLayout.NORTH);

        JPanel centerSidebar = new JPanel(new BorderLayout());
        centerSidebar.setBackground(bgAppColor);
        centerSidebar.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel contactsTitle = new JLabel("Contatos", SwingConstants.CENTER);
        contactsTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        contactsTitle.setForeground(textMainColor);
        contactsTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        centerSidebar.add(contactsTitle, BorderLayout.NORTH);

        contactList.setBackground(bgAppColor);
        contactList.setForeground(textMainColor);
        contactList.setSelectionBackground(isDarkTheme ? new Color(42, 57, 66) : new Color(235, 235, 235));
        contactList.setSelectionForeground(textMainColor);
        contactList.setFixedCellHeight(55);
        contactList.setFont(new Font("SansSerif", Font.BOLD, 15));

        contactList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkTheme ? new Color(40, 40, 40) : new Color(230, 230, 230)),
                        new EmptyBorder(0, 15, 0, 15)
                ));
                return label;
            }
        });

        JScrollPane listScroll = new JScrollPane(contactList);
        listScroll.setBorder(BorderFactory.createLineBorder(isDarkTheme ? new Color(60, 60, 60) : new Color(200, 200, 200)));
        listScroll.getVerticalScrollBar().setUnitIncrement(16);
        centerSidebar.add(listScroll, BorderLayout.CENTER);

        JPanel contactActionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        contactActionPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        contactActionPanel.setBackground(bgAppColor);

        JButton btnAdd = createSimpleButton("+", new Color(40, 167, 69));
        JButton btnRemove = createSimpleButton("-", new Color(108, 117, 125));

        contactActionPanel.add(btnAdd);
        contactActionPanel.add(btnRemove);
        centerSidebar.add(contactActionPanel, BorderLayout.SOUTH);

        sidebar.add(centerSidebar, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);

        JPanel mainChatArea = new JPanel(new BorderLayout());
        chatCards.setBackground(bgChatColor);

        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(bgChatColor);
        chatCards.add(emptyPanel, "EMPTY");

        mainChatArea.add(chatCards, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(15, 0));
        bottomPanel.setBackground(bgAppColor);
        bottomPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        inputField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        inputField.setBackground(inputBgColor);
        inputField.setForeground(textMainColor);
        inputField.setCaretColor(textMainColor);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDarkTheme ? new Color(60, 60, 60) : new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        inputField.setEnabled(false);
        bottomPanel.add(inputField, BorderLayout.CENTER);

        sendButton.setEnabled(false);
        sendButton.setPreferredSize(new Dimension(120, 45));
        bottomPanel.add(sendButton, BorderLayout.EAST);

        mainChatArea.add(bottomPanel, BorderLayout.SOUTH);
        add(mainChatArea, BorderLayout.CENTER);

        cardLayout.show(chatCards, "EMPTY");
    }

    private void setupActions() {
        toggleStatusButton.addActionListener(e -> toggleOnlineStatus());

        JPanel sidebar = (JPanel) getContentPane().getComponent(0);
        JPanel centerSidebar = (JPanel) sidebar.getComponent(1);
        JPanel contactActionPanel = (JPanel) centerSidebar.getComponent(2);
        ((JButton) contactActionPanel.getComponent(0)).addActionListener(e -> addContact());
        ((JButton) contactActionPanel.getComponent(1)).addActionListener(e -> removeContact());

        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedContact = contactList.getSelectedValue();
                if (selectedContact != null) {
                    if (!chatPanels.containsKey(selectedContact)) {
                        createChatPanelForContact(selectedContact);
                    }
                    cardLayout.show(chatCards, selectedContact);
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    scrollToBottom(selectedContact);
                } else {
                    cardLayout.show(chatCards, "EMPTY");
                    inputField.setEnabled(false);
                    sendButton.setEnabled(false);
                }
            }
        });

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void createChatPanelForContact(String contact) {
        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(bgChatColor);
        historyPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(bgChatColor);
        wrapper.add(historyPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        chatPanels.put(contact, historyPanel);
        chatScrolls.put(contact, scrollPane);
        chatCards.add(scrollPane, contact);
    }

    private void toggleOnlineStatus() {
        try {
            if (chatManager.isOnline()) {
                chatManager.goOffline();
                toggleStatusButton.setText("Offline");
                toggleStatusButton.setBackground(new Color(108, 117, 125));
            } else {
                chatManager.goOnline();
                toggleStatusButton.setText("Online");
                toggleStatusButton.setBackground(new Color(0, 168, 132));

                for (JLabel statusLabel : pendingStatusLabels) {
                    statusLabel.setText(">");
                }
                pendingStatusLabels.clear();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void addContact() {
        JPanel inputPanel = new JPanel(new BorderLayout(0, 5));
        inputPanel.add(new JLabel("Nome do contato:"), BorderLayout.NORTH);
        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(250, 35));
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(nameField, BorderLayout.CENTER);

        Object[] options = {"Adicionar"};
        int result = JOptionPane.showOptionDialog(this, inputPanel, "Novo Contato",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == 0) {
            String name = nameField.getText();
            if (name != null && !name.trim().isEmpty()) {
                String normalizedName = name.trim().toLowerCase();
                if (normalizedName.equals(chatManager.getClientName())) {
                    JOptionPane.showMessageDialog(this, "Você não pode enviar mensagens para si mesmo.", "Aviso", JOptionPane.WARNING_MESSAGE);
                } else {
                    chatManager.addContact(normalizedName);
                    updateContactList();
                }
            }
        }
    }

    private void removeContact() {
        if (selectedContact != null) {
            chatManager.removeContact(selectedContact);
            updateContactList();
            cardLayout.show(chatCards, "EMPTY");
            inputField.setEnabled(false);
            sendButton.setEnabled(false);
        }
    }

    private void updateContactList() {
        contactListModel.clear();
        chatManager.getContacts().forEach(contactListModel::addElement);
    }

    private void sendMessage() {
        if (selectedContact == null) return;
        String content = inputField.getText().trim();
        if (content.isEmpty()) return;

        try {
            boolean sentImmediately = chatManager.sendMessage(selectedContact, content);
            appendBubble(selectedContact, content, true, java.time.LocalDateTime.now(), sentImmediately);
            inputField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    public void displayMessage(Message message) {
        String sender = message.sender();
        if (sender.equals(chatManager.getClientName())) return;

        if (!chatPanels.containsKey(sender)) {
            chatManager.addContact(sender);
            updateContactList();
            createChatPanelForContact(sender);
        }
        appendBubble(sender, message.content(), false, message.timestamp(), true);
    }

    private void appendBubble(String contactPanelKey, String text, boolean isMe, java.time.LocalDateTime timestamp, boolean sentImmediately) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = chatPanels.get(contactPanelKey);
            if (panel == null) return;

            JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 5));
            row.setOpaque(false);

            JPanel bubble = new JPanel(new BorderLayout(5, 5));
            bubble.setBackground(isMe ? bubbleMeColor : bubbleOtherColor);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isDarkTheme ? new Color(60, 60, 60) : new Color(210, 210, 210), 1, true),
                    BorderFactory.createEmptyBorder(10, 14, 8, 14)
            ));

            JTextArea textArea = new JTextArea(text);
            textArea.setEditable(false);
            textArea.setOpaque(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setForeground(textMainColor);
            textArea.setFont(new Font("SansSerif", Font.PLAIN, 15));

            int textWidth = SwingUtilities.computeStringWidth(textArea.getFontMetrics(textArea.getFont()), text);
            textArea.setPreferredSize(new Dimension(Math.min(textWidth + 30, 450), textArea.getPreferredSize().height));

            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            footerPanel.setOpaque(false);

            JLabel timeLabel = new JLabel(timestamp.format(timeFormatter));
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            timeLabel.setForeground(textSubColor);
            footerPanel.add(timeLabel);

            if (isMe) {
                JLabel statusLabel = new JLabel(sentImmediately ? ">" : "...");
                statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                statusLabel.setForeground(textSubColor);
                if (!sentImmediately) {
                    pendingStatusLabels.add(statusLabel);
                }
                footerPanel.add(statusLabel);
            }

            bubble.add(textArea, BorderLayout.CENTER);
            bubble.add(footerPanel, BorderLayout.SOUTH);

            row.add(bubble);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

            panel.add(row);
            panel.revalidate();
            panel.repaint();
            scrollToBottom(contactPanelKey);
        });
    }

    private void scrollToBottom(String contact) {
        SwingUtilities.invokeLater(() -> {
            JScrollPane scroll = chatScrolls.get(contact);
            if (scroll != null) {
                JScrollBar vertical = scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }
}