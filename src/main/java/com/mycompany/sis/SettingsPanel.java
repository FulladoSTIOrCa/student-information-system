package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Settings panel — card inside MainFrame.
 *
 * Fixes applied vs. original SettingsUI:
 *  1. Font size slider NOW actually rebuilds all panels via AppSettings.fireChange(),
 *     which triggers MainFrame.onSettingsChanged() → rebuildAll(). The slider only
 *     commits on mouse-release (not while dragging) to avoid mid-drag rebuilds.
 *  2. Layout combo NOW actually applies: same fireChange() path propagates to
 *     StudentTablePanel / SubjectsPanel / AnnouncementsPanel which read s.getLayout().
 *  3. Admin Roles tab has a "🔄 Refresh" button that re-queries the DB.
 */
public class SettingsPanel extends JPanel {

    private final MainFrame frame;
    private DefaultTableModel rolesModel;
    private JLabel            rolesStatusLbl;

    public SettingsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        build();
    }

    /** Called by MainFrame to force a roles-table reload without rebuilding everything. */
    public void reloadRoles() {
        if (rolesModel == null) return;
        rolesModel.setRowCount(0);
        List<String[]> users = SupabaseService.getAllUsers();
        int count = 0;
        for (String[] u : users) {
            if (!"admin".equalsIgnoreCase(u[0])) {
                rolesModel.addRow(new Object[]{u[0], u[1], ""});
                count++;
            }
        }
        if (rolesStatusLbl != null)
            rolesStatusLbl.setText(count + " user(s) loaded");
    }

    private void build() {
        removeAll();
        AppSettings s = AppSettings.get();
        boolean masterAdmin = "admin".equalsIgnoreCase(frame.getLoggedInUser());

        setBackground(s.bgDark());

        // ── Top bar ─────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(s.bgMid());
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton backBtn = makeIconBtn("←", s);
        backBtn.addActionListener(e ->
            frame.showCard(frame.isAdmin() ? MainFrame.CARD_ADMIN : MainFrame.CARD_STUDENT));
        left.add(backBtn);
        JLabel titleLbl = new JLabel("⚙  Settings");
        titleLbl.setFont(s.fontH2());
        titleLbl.setForeground(s.textMain());
        left.add(titleLbl);
        topBar.add(left, BorderLayout.WEST);

        // ── Tabbed pane ─────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(s.bgDark());
        tabs.setForeground(s.textMain());
        tabs.setFont(s.fontBold());

        tabs.addTab("⚙  General",     buildGeneralTab(s));
        if (masterAdmin) {
            tabs.addTab("🛡  Admin Roles", buildRolesTab(s));
        }

        add(topBar, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
        revalidate(); repaint();
    }

    // ── General tab ─────────────────────────────────────────────────────────

    private JPanel buildGeneralTab(AppSettings s) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(s.bgDark());

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(s.bgMid());
        banner.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel bannerLbl = new JLabel("⚙  Preferences");
        bannerLbl.setFont(s.fontH2());
        bannerLbl.setForeground(s.textMain());
        banner.add(bannerLbl, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setBackground(s.bgDark());
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        // ── Theme ────────────────────────────────────────────────────────────
        content.add(sectionLabel("APPEARANCE", s));
        content.add(Box.createVerticalStrut(10));

        JPanel themeCard = makeCard(s);
        JPanel themeLbl  = cardLabel("Theme", "Switch between Dark and Light mode", s);

        JToggleButton themeToggle = makeToggle(
            s.isDark() ? "🌙  Dark" : "☀  Light", s.isDark(), s);

        themeToggle.addActionListener(e -> {
            boolean dark = themeToggle.isSelected();
            // Update text before the rebuild wipes this toggle
            themeToggle.setText(dark ? "🌙  Dark" : "☀  Light");
            AppSettings.get().setTheme(dark ? AppSettings.Theme.DARK : AppSettings.Theme.LIGHT);
            // fireChange() in setTheme() triggers MainFrame.onSettingsChanged() → rebuildAll()
        });

        themeCard.add(themeLbl,    BorderLayout.WEST);
        themeCard.add(themeToggle, BorderLayout.EAST);
        content.add(themeCard);
        content.add(Box.createVerticalStrut(12));

        // ── Font size ────────────────────────────────────────────────────────
        content.add(Box.createVerticalStrut(10));
        content.add(sectionLabel("TYPOGRAPHY", s));
        content.add(Box.createVerticalStrut(10));

        JPanel fontCard = makeCard(s);
        fontCard.setLayout(new BorderLayout(0, 8));
        fontCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel fontTop = new JPanel(new BorderLayout());
        fontTop.setOpaque(false);
        JPanel fontLabel = cardLabel("Font Size", "Adjust the base text size (11 – 18 px)", s);
        JLabel fontValLbl = new JLabel(s.getFontSize() + " px");
        fontValLbl.setFont(s.fontBold());
        fontValLbl.setForeground(s.accentIndigo());
        fontTop.add(fontLabel,  BorderLayout.WEST);
        fontTop.add(fontValLbl, BorderLayout.EAST);

        JSlider fontSlider = new JSlider(11, 18, s.getFontSize());
        fontSlider.setOpaque(false);
        fontSlider.setForeground(s.accentIndigo());
        fontSlider.setPaintTicks(true);
        fontSlider.setMajorTickSpacing(1);
        fontSlider.setSnapToTicks(true);

        // Update label while dragging (live preview of number only)
        fontSlider.addChangeListener(e -> {
            fontValLbl.setText(fontSlider.getValue() + " px");
        });

        // FIX: only commit (and trigger rebuild) when the user releases the slider
        fontSlider.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                AppSettings.get().setFontSize(fontSlider.getValue());
                // fireChange() inside setFontSize triggers MainFrame → rebuildAll()
                // which rebuilds every panel with the new font size
            }
        });

        fontCard.add(fontTop,    BorderLayout.NORTH);
        fontCard.add(fontSlider, BorderLayout.CENTER);
        content.add(fontCard);
        content.add(Box.createVerticalStrut(12));

        // ── Layout ───────────────────────────────────────────────────────────
        content.add(Box.createVerticalStrut(10));
        content.add(sectionLabel("LAYOUT MODE", s));
        content.add(Box.createVerticalStrut(10));

        JPanel layoutCard = makeCard(s);
        JPanel layoutLbl  = cardLabel("Table Layout", "How lists and tables are displayed", s);

        JComboBox<String> layoutBox = new JComboBox<>(new String[]{"Default","List","Grid"});
        styleCombo(layoutBox, s);
        switch (s.getLayout()) {
            case LIST -> layoutBox.setSelectedIndex(1);
            case GRID -> layoutBox.setSelectedIndex(2);
            default   -> layoutBox.setSelectedIndex(0);
        }

        // FIX: commit and fire change so StudentTablePanel etc. pick it up
        layoutBox.addActionListener(e -> {
            int i = layoutBox.getSelectedIndex();
            AppSettings.get().setLayout(
                i == 0 ? AppSettings.Layout.DEFAULT
              : i == 1 ? AppSettings.Layout.LIST
                       : AppSettings.Layout.GRID
            );
            // fireChange() in setLayout() triggers MainFrame → rebuildAll()
        });

        layoutCard.add(layoutLbl, BorderLayout.WEST);
        layoutCard.add(layoutBox, BorderLayout.EAST);
        content.add(layoutCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(s.bgDark());
        scroll.setBackground(s.bgDark());

        root.add(banner, BorderLayout.NORTH);
        root.add(scroll,  BorderLayout.CENTER);
        return root;
    }

    // ── Admin Roles tab ─────────────────────────────────────────────────────

    private JPanel buildRolesTab(AppSettings s) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(s.bgDark());

        // Banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(s.bgMid());
        banner.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel bannerLbl = new JLabel("🛡  Manage Admin Roles");
        bannerLbl.setFont(s.fontH2());
        bannerLbl.setForeground(s.textMain());

        JLabel noteLbl = new JLabel("Only you can grant / revoke admin access");
        noteLbl.setFont(s.fontSmall());
        noteLbl.setForeground(s.accentAmber());

        // FIX: Refresh button in the banner ───────────────────────────────
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(s.fontSmall());
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(s.accentIndigo());
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new EmptyBorder(7, 14, 7, 14));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { refreshBtn.setBackground(s.accentIndigo().darker()); }
            public void mouseExited(MouseEvent e)  { refreshBtn.setBackground(s.accentIndigo()); }
        });
        refreshBtn.addActionListener(e -> reloadRoles());

        JPanel bannerLeft = new JPanel(new BorderLayout(0, 4));
        bannerLeft.setOpaque(false);
        bannerLeft.add(bannerLbl, BorderLayout.NORTH);
        bannerLeft.add(noteLbl,   BorderLayout.SOUTH);

        banner.add(bannerLeft,  BorderLayout.WEST);
        banner.add(refreshBtn,  BorderLayout.EAST);

        // Table
        String[] cols = {"Username","Current Role","Action"};
        rolesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(rolesModel);
        styleRolesTable(table, s);

        // Action column renderer
        table.getColumn("Action").setCellRenderer((tbl, val, sel, foc, row, col) -> {
            String role      = (String) rolesModel.getValueAt(row, 1);
            boolean isAdmin  = "admin".equalsIgnoreCase(role);
            JButton btn = new JButton(isAdmin ? "Revoke Admin" : "Make Admin");
            btn.setFont(s.fontSmall());
            btn.setForeground(Color.WHITE);
            btn.setBackground(isAdmin ? s.accentDanger() : s.accentGreen());
            btn.setOpaque(true);
            btn.setBorder(new EmptyBorder(4, 10, 4, 10));
            return btn;
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col != 2 || row < 0) return;

                String username = (String) rolesModel.getValueAt(row, 0);
                String role     = (String) rolesModel.getValueAt(row, 1);
                boolean makeAdmin = !"admin".equalsIgnoreCase(role);

                int confirm = JOptionPane.showConfirmDialog(frame,
                    "<html>" + (makeAdmin ? "Grant admin to " : "Revoke admin from ")
                        + "<b>" + username + "</b>?</html>",
                    "Confirm Role Change",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = SupabaseService.setUserRole(username, makeAdmin ? "admin" : "student");
                    if (ok) {
                        rolesModel.setValueAt(makeAdmin ? "admin" : "student", row, 1);
                        table.repaint();
                        JOptionPane.showMessageDialog(frame,
                            "Role updated for " + username + ".", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame,
                            "Failed to update role.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Initial load
        reloadRoles();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(s.bgDark());

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(s.bgMid());
        statusBar.setBorder(new EmptyBorder(8, 20, 8, 20));
        rolesStatusLbl = new JLabel(rolesModel.getRowCount() + " user(s) loaded");
        rolesStatusLbl.setFont(s.fontSmall());
        rolesStatusLbl.setForeground(s.textSub());
        statusBar.add(rolesStatusLbl);

        root.add(banner,    BorderLayout.NORTH);
        root.add(scroll,    BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
        return root;
    }

    private void styleRolesTable(JTable table, AppSettings s) {
        table.setBackground(s.bgDark());
        table.setForeground(s.textMain());
        table.setFont(s.fontBody());
        table.setRowHeight(40);
        table.setGridColor(s.bgPanel());
        table.setSelectionBackground(new Color(99,102,241,60));
        table.setSelectionForeground(s.textMain());
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(s.bgPanel());
        header.setForeground(s.textSub());
        header.setFont(s.fontBold());
        header.setBorder(BorderFactory.createMatteBorder(0,0,2,0,s.accentIndigo()));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(s.fontBody());
                setBorder(new EmptyBorder(0, 14, 0, 14));
                setBackground(sel ? new Color(99,102,241,60) : row%2==0 ? s.bgDark() : new Color(21,32,52));
                setForeground(s.textMain());
                return this;
            }
        });
    }

    // ── Widget helpers ───────────────────────────────────────────────────────

    private JPanel makeCard(AppSettings s) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(s.bgMid());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(s.border(), 1),
            new EmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JPanel cardLabel(String title, String sub, AppSettings s) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(s.fontBold());
        t.setForeground(s.textMain());
        JLabel d = new JLabel(sub);
        d.setFont(s.fontSmall());
        d.setForeground(s.textSub());
        p.add(t); p.add(d);
        return p;
    }

    private JLabel sectionLabel(String text, AppSettings s) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(s.accentIndigo());
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JToggleButton makeToggle(String text, boolean selected, AppSettings s) {
        JToggleButton btn = new JToggleButton(text, selected);
        btn.setFont(s.fontBold());
        btn.setForeground(Color.WHITE);
        btn.setBackground(selected ? s.accentIndigo() : s.bgPanel());
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addItemListener(e ->
            btn.setBackground(btn.isSelected() ? s.accentIndigo() : s.bgPanel()));
        return btn;
    }

    private void styleCombo(JComboBox<String> box, AppSettings s) {
        box.setFont(s.fontBody());
        box.setForeground(s.textMain());
        box.setBackground(s.bgPanel());
        box.setBorder(BorderFactory.createLineBorder(s.border(), 1));
        box.setPreferredSize(new Dimension(130, 34));
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
}
