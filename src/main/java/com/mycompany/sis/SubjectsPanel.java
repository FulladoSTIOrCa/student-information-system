package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Subjects panel — card inside MainFrame.
 * Admin: shows all subjects.
 * Student: shows only subjects belonging to their enrolled course.
 */
public class SubjectsPanel extends JPanel {

    private final MainFrame frame;

    public SubjectsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        build();
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

        // ── Fetch data depending on role ─────────────────────────────────────
        List<String[]> rows;
        String panelTitle;

        if (admin) {
            // Admin sees everything
            rows       = SupabaseService.getSubjects();
            panelTitle = "📚  Subjects  (All Courses)";
        } else {
            // Student sees only their course's subjects
            int courseId = SupabaseService.getCourseIdForUser(frame.getLoggedInUser());
            if (courseId > 0) {
                rows = SupabaseService.getSubjectsByCourse(courseId);
                // Look up the course name to show in the title
                String courseName = getCourseNameForId(courseId);
                panelTitle = "📚  Subjects  —  " + courseName;
            } else {
                // Fallback: no course found, show nothing with a message
                rows       = List.of();
                panelTitle = "📚  Subjects";
            }
        }

        JLabel titleLbl = new JLabel(panelTitle);
        titleLbl.setFont(s.fontH2());
        titleLbl.setForeground(s.textMain());
        left.add(titleLbl);
        topBar.add(left, BorderLayout.WEST);

        // ── Table ───────────────────────────────────────────────────────────
        String[] cols = {"Subject Code", "Subject Name", "Units"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (String[] row : rows) model.addRow(row);

        JTable table = new JTable(model);
        styleTable(table, s, s.accentGreen());
        table.getColumnModel().getColumn(1).setPreferredWidth(340);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(s.bgDark());

        // ── Status bar ──────────────────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(s.bgMid());
        statusBar.setBorder(new EmptyBorder(8, 22, 8, 22));

        String statusText = rows.isEmpty()
            ? "No subjects found for your course"
            : rows.size() + " subject(s) found";
        JLabel statusLbl = new JLabel(statusText);
        statusLbl.setFont(s.fontSmall());
        statusLbl.setForeground(s.textSub());
        statusBar.add(statusLbl);

        // ── Empty state message (student with no course assigned) ────────────
        if (rows.isEmpty() && !admin) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(s.bgDark());
            JLabel emptyLbl = new JLabel(
                "<html><div style='text-align:center'>"
                + "⚠  No subjects found for your course.<br>"
                + "<small>Please contact your administrator.</small>"
                + "</div></html>");
            emptyLbl.setFont(s.fontBody());
            emptyLbl.setForeground(s.textSub());
            emptyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            emptyPanel.add(emptyLbl);
            add(topBar,     BorderLayout.NORTH);
            add(emptyPanel, BorderLayout.CENTER);
            add(statusBar,  BorderLayout.SOUTH);
        } else {
            add(topBar,   BorderLayout.NORTH);
            add(scroll,   BorderLayout.CENTER);
            add(statusBar, BorderLayout.SOUTH);
        }

        revalidate(); repaint();
    }

    // ── Helper: resolve course name from id without an extra DB call ──────────
    private String getCourseNameForId(int courseId) {
        for (String[] c : SupabaseService.getCourses()) {
            if (Integer.parseInt(c[0]) == courseId) {
                // c[1] = course_code, c[2] = course_name
                return c[1] + " — " + c[2];
            }
        }
        return "Unknown Course";
    }

    private void styleTable(JTable table, AppSettings s, Color accent) {
        table.setBackground(s.bgDark());
        table.setForeground(s.textMain());
        table.setFont(s.fontBody());
        table.setRowHeight(rowHeight(s));
        table.setGridColor(s.bgPanel());
        table.setSelectionBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
        table.setSelectionForeground(s.textMain());
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(s.getLayout() != AppSettings.Layout.DEFAULT);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(s.bgPanel());
        header.setForeground(s.textSub());
        header.setFont(s.fontBold());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, accent));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(s.fontBody());
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (sel) {
                    setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                    setForeground(s.textMain());
                } else if (row % 2 == 0) {
                    setBackground(s.bgDark()); setForeground(s.textMain());
                } else {
                    setBackground(s.bgMid()); setForeground(s.textMain());
                }
                return this;
            }
        });
    }

    private int rowHeight(AppSettings s) {
        return switch (s.getLayout()) {
            case LIST -> 26;
            case GRID -> 48;
            default   -> 34;
        };
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