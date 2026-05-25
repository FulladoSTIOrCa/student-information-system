package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Announcements panel — card inside MainFrame.
 */
public class AnnouncementsPanel extends JPanel {

    private final MainFrame        frame;
    private DefaultTableModel      model;

    public AnnouncementsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        build();
    }

    /** Called externally to refresh data after posting. */
    public void reload() {
        if (model == null) return;
        model.setRowCount(0);
        for (String[] row : SupabaseService.getAnnouncements()) model.addRow(row);
    }

    private void build() {
        removeAll();
        AppSettings s = AppSettings.get();
        boolean admin = frame.isAdmin();
        setBackground(s.bgDark());

        // ── Top bar ─────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(s.bgMid());
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton backBtn = makeIconBtn("←", s);
        backBtn.addActionListener(e ->
            frame.showCard(admin ? MainFrame.CARD_ADMIN : MainFrame.CARD_STUDENT));
        left.add(backBtn);
        JLabel titleLbl = new JLabel("📢  Announcements");
        titleLbl.setFont(s.fontH2());
        titleLbl.setForeground(s.textMain());
        left.add(titleLbl);
        topBar.add(left, BorderLayout.WEST);

        if (admin) {
            JButton addBtn = accentBtn("+ New Announcement", s.accentAmber(), s);
            addBtn.addActionListener(e -> openAddDialog(s));
            topBar.add(addBtn, BorderLayout.EAST);
        }

        // ── Table ───────────────────────────────────────────────────────────
        String[] cols = {"Title","Content","Posted At"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setBackground(s.bgDark());
        table.setForeground(s.textMain());
        table.setFont(s.fontBody());
        table.setRowHeight(rowHeight(s));
        table.setGridColor(s.bgPanel());
        table.setSelectionBackground(new Color(245,158,11,60));
        table.setSelectionForeground(s.textMain());
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(s.getLayout() != AppSettings.Layout.DEFAULT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);

        JTableHeader header = table.getTableHeader();
        header.setBackground(s.bgPanel());
        header.setForeground(s.textSub());
        header.setFont(s.fontBold());
        header.setBorder(BorderFactory.createMatteBorder(0,0,2,0,s.accentAmber()));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(s.fontBody());
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (sel) { setBackground(new Color(245,158,11,60)); setForeground(s.textMain()); }
                else if (row % 2 == 0) { setBackground(s.bgDark()); setForeground(s.textMain()); }
                else { setBackground(s.bgMid()); setForeground(s.textMain()); }
                return this;
            }
        });

        reload();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(s.bgDark());

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private void openAddDialog(AppSettings s) {
        JDialog dialog = new JDialog(frame, "New Announcement", true);
        dialog.setSize(500, 360);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(s.bgMid());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(s.bgMid());
        form.setBorder(new EmptyBorder(22, 26, 12, 26));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField();
        styleDialogField(titleField, s);

        JTextArea contentArea = new JTextArea(5, 20);
        contentArea.setFont(s.fontBody());
        contentArea.setForeground(s.textMain());
        contentArea.setBackground(s.bgDark());
        contentArea.setCaretColor(s.textMain());
        contentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(s.border(), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        gbc.gridx=0; gbc.gridy=0; gbc.weightx=0; gbc.anchor=GridBagConstraints.NORTHWEST;
        form.add(formLabel("Title", s), gbc);
        gbc.gridx=1; gbc.weightx=1;
        form.add(titleField, gbc);
        gbc.gridx=0; gbc.gridy=1; gbc.weightx=0;
        form.add(formLabel("Content", s), gbc);
        gbc.gridx=1; gbc.weightx=1; gbc.weighty=1; gbc.fill=GridBagConstraints.BOTH;
        form.add(new JScrollPane(contentArea), gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(s.bgMid());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(s.fontBody());
        cancelBtn.setForeground(s.textSub());
        cancelBtn.setBackground(s.bgPanel());
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(new EmptyBorder(9, 18, 9, 18));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JButton postBtn = accentBtn("Post Announcement", s.accentAmber(), s);

        btnPanel.add(cancelBtn);
        btnPanel.add(postBtn);
        panel.add(form,     BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        dialog.setContentPane(panel);

        cancelBtn.addActionListener(e -> dialog.dispose());
        postBtn.addActionListener(e -> {
            String title   = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Title and content cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (SupabaseService.postAnnouncement(title, content)) {
                JOptionPane.showMessageDialog(dialog, "Announcement posted!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                reload();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to post.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private JButton accentBtn(String text, Color bg, AppSettings s) {
        JButton btn = new JButton(text);
        btn.setFont(s.fontBold());
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleDialogField(JTextField f, AppSettings s) {
        f.setFont(s.fontBody());
        f.setForeground(s.textMain());
        f.setBackground(s.bgDark());
        f.setCaretColor(s.textMain());
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(s.border(), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private JLabel formLabel(String text, AppSettings s) {
        JLabel lbl = new JLabel(text + ":");
        lbl.setFont(s.fontBold());
        lbl.setForeground(s.textSub());
        return lbl;
    }

    private JButton makeIconBtn(String icon, AppSettings s) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
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

    private int rowHeight(AppSettings s) {
        return switch (s.getLayout()) {
            case LIST -> 26;
            case GRID -> 48;
            default   -> 34;
        };
    }
}
