package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Reusable top navigation bar that appears on every content panel.
 * Left side: back button + page title.
 * Right side: optional settings gear + logout button.
 */
public class NavBar extends JPanel {

    public NavBar(MainFrame frame, String title, boolean showBack,
                  boolean showSettings, boolean showLogout) {
        AppSettings s = AppSettings.get();
        setBackground(s.bgMid());
        setBorder(new EmptyBorder(12, 20, 12, 20));
        setLayout(new BorderLayout(12, 0));

        // Left: back + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        if (showBack) {
            JButton backBtn = iconBtn("←", s);
            backBtn.addActionListener(e ->
                frame.showCard(frame.isAdmin() ? MainFrame.CARD_ADMIN : MainFrame.CARD_STUDENT));
            left.add(backBtn);
        }

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(s.fontH2());
        titleLbl.setForeground(s.textMain());
        left.add(titleLbl);
        add(left, BorderLayout.WEST);

        // Right: settings + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        if (showSettings) {
            JButton settingsBtn = iconBtn("⚙", s);
            settingsBtn.addActionListener(e ->
                frame.showCard(MainFrame.CARD_SETTINGS));
            right.add(settingsBtn);
        }

        if (showLogout) {
            JButton logoutBtn = new JButton("Logout");
            logoutBtn.setFont(s.fontSmall());
            logoutBtn.setForeground(Color.WHITE);
            logoutBtn.setBackground(s.accentDanger());
            logoutBtn.setFocusPainted(false);
            logoutBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
            logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            logoutBtn.addActionListener(e -> frame.onLogout());
            right.add(logoutBtn);
        }

        add(right, BorderLayout.EAST);
    }

    private JButton iconBtn(String icon, AppSettings s) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setForeground(s.textSub());
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(4, 8, 4, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(s.textMain()); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(s.textSub());  }
        });
        return btn;
    }
}
