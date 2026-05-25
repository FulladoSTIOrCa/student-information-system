package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen — lives inside MainFrame's CardLayout.
 */
public class LoginPanel extends JPanel {

    private final MainFrame frame;
    private JTextField      usernameField;
    private JPasswordField  passwordField;

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        removeAll();
        AppSettings s = AppSettings.get();

        // Gradient background — centers the card
        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, s.bgDark(), 0, getHeight(), s.bgMid());
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // ── Card uses GridBagLayout so fields stretch full width ─────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(s.bgMid());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(s.border(), 1),
            new EmptyBorder(40, 48, 40, 48)
        ));
        card.setPreferredSize(new Dimension(400, 460));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int row = 0;

        // Icon
        JLabel iconLbl = new JLabel("🎓");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 10, 0);
        card.add(iconLbl, gbc);

        // Title
        JLabel titleLbl = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLbl.setFont(s.fontTitle());
        titleLbl.setForeground(s.textMain());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 4, 0);
        card.add(titleLbl, gbc);

        // Subtitle
        JLabel subLbl = new JLabel("Student Information System", SwingConstants.CENTER);
        subLbl.setFont(s.fontSmall());
        subLbl.setForeground(s.textSub());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 28, 0);
        card.add(subLbl, gbc);

        // Username label
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(s.fontBold());
        userLbl.setForeground(s.textSub());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 6, 0);
        card.add(userLbl, gbc);

        // Username field — stretches full card width
        usernameField = new JTextField();
        styleField(usernameField, s);
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 16, 0);
        card.add(usernameField, gbc);

        // Password label
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(s.fontBold());
        passLbl.setForeground(s.textSub());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 6, 0);
        card.add(passLbl, gbc);

        // Password field — stretches full card width
        passwordField = new JPasswordField();
        styleField(passwordField, s);
        passwordField.addActionListener(e -> doLogin());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 28, 0);
        card.add(passwordField, gbc);

        // Sign In button — stretches full card width
        JButton loginBtn = new JButton("Sign In  →");
        loginBtn.setFont(s.fontBold());
        loginBtn.setBackground(s.accentIndigo());
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(new EmptyBorder(13, 0, 13, 0));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(new Color(79, 70, 229)); }
            public void mouseExited(MouseEvent e)  { loginBtn.setBackground(s.accentIndigo()); }
        });
        loginBtn.addActionListener(e -> doLogin());
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 0, 0);
        card.add(loginBtn, gbc);

        bg.add(card);
        add(bg, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void styleField(JTextField f, AppSettings s) {
        f.setFont(s.fontBody());
        f.setForeground(s.textMain());
        f.setBackground(s.bgDark());
        f.setCaretColor(s.textMain());
        f.setPreferredSize(new Dimension(0, 44));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(s.border(), 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both username and password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String role = SupabaseService.login(user, pass);
        if (role == null) {
            JOptionPane.showMessageDialog(frame, "Invalid username or password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            frame.onLogin(user, role.equalsIgnoreCase("admin"));
        }
    }
}