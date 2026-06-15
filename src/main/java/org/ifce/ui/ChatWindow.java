package org.ifce.ui;

import org.ifce.client.ChatManager;
import org.ifce.model.Message;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatWindow extends JFrame {
    private final ChatManager manager;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
    private final CardLayout cards = new CardLayout();
    private final JPanel chatArea = new JPanel(cards);
    private final Map<String, JPanel> panels = new HashMap<>();
    private final Map<String, JScrollPane> scrolls = new HashMap<>();
    private final List<JLabel> pendingLabels = new ArrayList<>();
    private final JTextField input = new JTextField();
    private final JButton btnStatus = createBtn("Offline", new Color(108, 117, 125));
    private final JButton btnSend = createBtn("Enviar", new Color(0, 168, 132));
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
    private String selected = null;

    public ChatWindow(ChatManager manager) {
        this.manager = manager;
        setTitle("ChatMQ - " + manager.getClientName());
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.add(btnStatus, BorderLayout.NORTH);

        list.setFixedCellHeight(40);
        list.setFont(new Font("SansSerif", Font.BOLD, 14));
        list.addListSelectionListener(e -> selectContact());
        sidebar.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new GridLayout(1, 2));
        JButton btnAdd = createBtn("+", new Color(40, 167, 69));
        JButton btnRem = createBtn("-", new Color(220, 53, 69));
        btnAdd.addActionListener(e -> addContact());
        btnRem.addActionListener(e -> remContact());
        pnlBtns.add(btnAdd);
        pnlBtns.add(btnRem);
        sidebar.add(pnlBtns, BorderLayout.SOUTH);

        chatArea.setBackground(new Color(229, 221, 213));
        chatArea.add(new JPanel(), "EMPTY");

        JPanel bottom = new JPanel(new BorderLayout());
        input.setEnabled(false);
        btnSend.setEnabled(false);
        input.addActionListener(e -> sendMsg());
        btnSend.addActionListener(e -> sendMsg());
        btnStatus.addActionListener(e -> toggleStatus());
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(btnSend, BorderLayout.EAST);

        add(sidebar, BorderLayout.WEST);
        add(chatArea, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JButton createBtn(String txt, Color c) {
        JButton b = new JButton(txt);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void selectContact() {
        selected = list.getSelectedValue();
        if (selected != null) {
            if (!panels.containsKey(selected)) createChat(selected);
            cards.show(chatArea, selected);
            input.setEnabled(true);
            btnSend.setEnabled(true);
        } else {
            cards.show(chatArea, "EMPTY");
            input.setEnabled(false);
            btnSend.setEnabled(false);
        }
    }

    private void createChat(String c) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(229, 221, 213));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(new Color(229, 221, 213));
        wrap.add(p, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        panels.put(c, p);
        scrolls.put(c, scroll);
        chatArea.add(scroll, c);
    }

    private void toggleStatus() {
        try {
            if (manager.isOnline()) {
                manager.goOffline();
                btnStatus.setText("Offline");
                btnStatus.setBackground(new Color(108, 117, 125));
            } else {
                manager.goOnline();
                btnStatus.setText("Online");
                btnStatus.setBackground(new Color(0, 168, 132));

                for (JLabel l : pendingLabels) {
                    l.setText(">");
                }
                pendingLabels.clear();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro");
        }
    }

    private void addContact() {
        JTextField f = new JTextField(15);
        if (JOptionPane.showConfirmDialog(this, f, "Novo Contato", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == 0) {
            String n = f.getText().trim().toLowerCase();
            if (n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome do contato não pode ser vazio.");
            } else if (n.equals(manager.getClientName())) {
                JOptionPane.showMessageDialog(this, "Você não pode adicionar a si mesmo.");
            } else if (manager.getContacts().contains(n)) {
                JOptionPane.showMessageDialog(this, "Este contato já existe na sua lista.");
            } else {
                manager.addContact(n);
                updateList();
            }
        }
    }

    private void remContact() {
        if (selected != null) {
            manager.removeContact(selected);
            updateList();
            cards.show(chatArea, "EMPTY");
        }
    }

    private void updateList() {
        listModel.clear();
        for (String c : manager.getContacts()) {
            listModel.addElement(c);
        }
    }

    private void sendMsg() {
        if (selected == null || input.getText().trim().isEmpty()) return;
        try {
            String txt = input.getText().trim();
            boolean sent = manager.sendMessage(selected, txt);
            renderMsg(selected, txt, true, sent, java.time.LocalDateTime.now());
            input.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro");
        }
    }

    public void displayMessage(Message m) {
        if (m.sender().equals(manager.getClientName())) return;
        if (!panels.containsKey(m.sender())) {
            manager.addContact(m.sender());
            updateList();
            createChat(m.sender());
        }
        renderMsg(m.sender(), m.content(), false, true, m.timestamp());
    }

    private void renderMsg(String contact, String txt, boolean isMe, boolean sent, java.time.LocalDateTime time) {
        SwingUtilities.invokeLater(() -> {
            JPanel p = panels.get(contact);
            if (p == null) return;

            JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
            row.setOpaque(false);

            JPanel bubble = new JPanel(new BorderLayout());
            bubble.setBackground(isMe ? new Color(217, 253, 211) : Color.WHITE);
            bubble.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextArea area = new JTextArea(txt);
            area.setEditable(false);
            area.setOpaque(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);

            int w = SwingUtilities.computeStringWidth(area.getFontMetrics(area.getFont()), txt);
            area.setPreferredSize(new Dimension(Math.min(w + 20, 300), area.getPreferredSize().height));

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            foot.setOpaque(false);
            foot.add(new JLabel(time.format(fmt)));

            if (isMe) {
                JLabel status = new JLabel(sent ? ">" : "...");
                if (!sent) pendingLabels.add(status);
                foot.add(status);
            }

            bubble.add(area, BorderLayout.CENTER);
            bubble.add(foot, BorderLayout.SOUTH);
            row.add(bubble);
            p.add(row);
            p.revalidate();
            p.repaint();

            JScrollBar vert = scrolls.get(contact).getVerticalScrollBar();
            vert.setValue(vert.getMaximum());
        });
    }
}