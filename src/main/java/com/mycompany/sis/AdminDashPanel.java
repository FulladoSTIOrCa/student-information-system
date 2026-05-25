package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Admin dashboard — card inside MainFrame.
 * Fixed layout: centered card with evenly-spaced buttons, no alignment drift.
 */
public class AdminDashPanel extends JPanel {

    private final MainFrame frame;

    public AdminDashPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        removeAll();
        AppSettings s = AppSettings.get();

        // ── Background ──────────────────────────────────────────────────────
        JPanel bg = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, s.bgDark(), 0, getHeight(), s.bgMid());
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // ── NavBar ──────────────────────────────────────────────────────────
        bg.add(new NavBar(frame, "Admin Dashboard", false, true, true), BorderLayout.NORTH);

        // ── Center card ─────────────────────────────────────────────────────
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(s.bgMid());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(s.border(), 1),
            new EmptyBorder(32, 48, 32, 48)
        ));
        card.setMaximumSize(new Dimension(460, 600));

        // Header
        JLabel icon  = emoji("🛡️", 42, s);
        JLabel title = heading("Admin Dashboard", s);
        JLabel sub   = subtitle("Manage students, enrollment and announcements", s);
        card.add(icon);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));

        // Buttons
        JButton[] btns = {
            dashBtn("👥  View Students",  "View and manage all enrolled students", s.accentIndigo(), s),
            dashBtn("➕  Enroll Student", "Register a new student",                s.accentGreen(),  s),
            dashBtn("📢  Announcements",  "Post and manage school announcements",   s.accentAmber(),  s),
            dashBtn("📚  Subjects",        "View available subjects and units",      new Color(139,92,246), s),
        };

        btns[0].addActionListener(e -> frame.showCard(MainFrame.CARD_STUDENTS_TBL));
        btns[1].addActionListener(e -> frame.showCard(MainFrame.CARD_ENROLL));
        btns[2].addActionListener(e -> frame.showCard(MainFrame.CARD_ANNOUNCEMENTS));
        btns[3].addActionListener(e -> frame.showCard(MainFrame.CARD_SUBJECTS));

        for (JButton b : btns) {
            card.add(b);
            card.add(Box.createVerticalStrut(10));
        }

        center.add(card);
        bg.add(center, BorderLayout.CENTER);
        add(bg, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private JLabel emoji(String e, int size, AppSettings s) {
        JLabel l = new JLabel(e);
        l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JLabel heading(String text, AppSettings s) {
        JLabel l = new JLabel(text);
        l.setFont(s.fontTitle());
        l.setForeground(s.textMain());
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JLabel subtitle(String text, AppSettings s) {
        JLabel l = new JLabel("<html><div style='text-align:center'>" + text + "</div></html>");
        l.setFont(s.fontSmall());
        l.setForeground(s.textSub());
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JButton dashBtn(String text, String tooltip, Color accent, AppSettings s) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setFont(s.fontBold());
        btn.setForeground(s.textMain());
        btn.setBackground(s.bgPanel());
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 2),
            new EmptyBorder(14, 20, 14, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(accent.darker()); btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(s.bgPanel()); btn.setForeground(s.textMain()); }
        });
        return btn;
    }
}
