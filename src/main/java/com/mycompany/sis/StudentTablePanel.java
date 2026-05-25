package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Student table panel — card inside MainFrame.
 * Admin: full columns (including Course) + drop/restore/delete.
 * Student: read-only slim view (ID, Name, Gender, Course).
 */
public class StudentTablePanel extends JPanel {

    private final MainFrame        frame;
    private DefaultTableModel      activeModel;
    private DefaultTableModel      deletedModel;
    private JTable                 table;
    private JLabel                 statusLbl;
    private JLabel                 titleLbl;
    private JButton                dropBtn;
    private JButton                restoreBtn;
    private JButton                toggleBtn;
    private JButton                deleteBtn;
    private boolean                showingDeleted = false;

    public StudentTablePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        build();
    }

    // Called externally after enroll
    public void reload() {
        loadData();
        applyStyle();
        updateStatus();
    }

    private void build() {
        removeAll();
        AppSettings s = AppSettings.get();
        boolean admin = frame.isAdmin();

        setBackground(s.bgDark());

        // ── Top bar ─────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(s.bgMid());
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton backBtn = makeIconBtn("←", s);
        backBtn.addActionListener(e ->
            frame.showCard(admin ? MainFrame.CARD_ADMIN : MainFrame.CARD_STUDENT));
        left.add(backBtn);

        titleLbl = new JLabel(admin ? "👥  Student Management" : "👥  Student Directory");
        titleLbl.setFont(s.fontH2());
        titleLbl.setForeground(s.textMain());
        left.add(titleLbl);
        topBar.add(left, BorderLayout.WEST);

        // Right: admin action buttons
        if (admin) {
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            toggleBtn  = makeBtn("Show Dropped", s.accentSlate(), s);
            dropBtn    = makeBtn("🗑  Drop",      s.accentDanger(), s);
            restoreBtn = makeBtn("♻  Restore",   s.accentGreen(),  s);
            deleteBtn  = makeBtn("☠  Delete",    new Color(127, 29, 29), s);
            restoreBtn.setVisible(false);

            toggleBtn.addActionListener(e -> {
                showingDeleted = !showingDeleted;
                if (showingDeleted) {
                    toggleBtn.setText("Show Active");
                    toggleBtn.setBackground(s.accentAmber());
                    dropBtn.setVisible(false);
                    restoreBtn.setVisible(true);
                    table.setModel(deletedModel);
                    titleLbl.setText("🗂  Dropped Students");
                } else {
                    toggleBtn.setText("Show Dropped");
                    toggleBtn.setBackground(s.accentSlate());
                    dropBtn.setVisible(true);
                    restoreBtn.setVisible(false);
                    table.setModel(activeModel);
                    titleLbl.setText("👥  Student Management");
                }
                applyStyle();
            });
            dropBtn.addActionListener(e    -> dropSelected(s));
            restoreBtn.addActionListener(e -> restoreSelected(s));
            deleteBtn.addActionListener(e  -> deleteSelected(s));

            right.add(toggleBtn);
            right.add(restoreBtn);
            right.add(dropBtn);
            right.add(deleteBtn);
            topBar.add(right, BorderLayout.EAST);
        }

        // ── Table setup ─────────────────────────────────────────────────────
        // Admin: full columns including Course
        // Student: slim view — ID, Name, Gender, Course
        String[] adminCols   = {"ID", "Name", "Gender", "Email", "Phone", "Year", "Section", "Status", "Course"};
        String[] studentCols = {"ID", "Name", "Gender", "Course"};
        String[] cols = admin ? adminCols : studentCols;

        activeModel  = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        deletedModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        loadData();

        table = new JTable(showingDeleted ? deletedModel : activeModel);
        applyStyle();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(s.bgDark());
        scroll.setBackground(s.bgDark());

        // ── Status bar ──────────────────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBar.setBackground(s.bgMid());
        statusBar.setBorder(new EmptyBorder(10, 22, 10, 22));
        statusLbl = new JLabel();
        statusLbl.setFont(s.fontSmall());
        statusLbl.setForeground(s.textSub());
        statusBar.add(statusLbl);
        updateStatus();

        add(topBar,    BorderLayout.NORTH);
        add(scroll,    BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    private void loadData() {
        if (activeModel == null) return;
        activeModel.setRowCount(0);
        deletedModel.setRowCount(0);
        boolean admin = frame.isAdmin();

        for (String[] row : SupabaseService.getStudents()) {
            // row indices from SupabaseService.getStudents():
            // [0] student_id  [1] full_name   [2] gender
            // [3] email       [4] phone_no    [5] year_level
            // [6] section     [7] academic_status
            // [8] is_deleted  [9] course_code [10] course_name
            boolean deleted = "true".equalsIgnoreCase(row[8]);
            String courseDisplay = row[9] != null ? row[9] : "—";

            if (admin) {
                Object[] r = {
                    row[0], row[1], row[2], row[3], row[4],
                    row[5], row[6], row[7], courseDisplay
                };
                if (deleted) deletedModel.addRow(r);
                else         activeModel.addRow(r);
            } else {
                if (!deleted) activeModel.addRow(new Object[]{
                    row[0], row[1], row[2], courseDisplay
                });
            }
        }
    }

    private void applyStyle() {
        if (table == null) return;
        AppSettings s = AppSettings.get();
        AppSettings.Layout layout = s.getLayout();

        table.setBackground(s.bgDark());
        table.setForeground(s.textMain());
        table.setFont(s.fontBody());
        table.setGridColor(s.bgPanel());
        table.setSelectionBackground(new Color(99, 102, 241, 80));
        table.setSelectionForeground(s.textMain());
        table.getTableHeader().setReorderingAllowed(false);

        switch (layout) {
            case LIST -> { table.setRowHeight(26); table.setShowHorizontalLines(true);  table.setShowVerticalLines(true);  }
            case GRID -> { table.setRowHeight(48); table.setShowHorizontalLines(true);  table.setShowVerticalLines(true);  }
            default   -> { table.setRowHeight(34); table.setShowHorizontalLines(true);  table.setShowVerticalLines(false); }
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(s.bgPanel());
        header.setForeground(s.textSub());
        header.setFont(s.fontBold());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, s.accentIndigo()));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        // Give the Course column a bit more width
        TableColumnModel cm = table.getColumnModel();
        int courseCol = frame.isAdmin() ? 8 : 3;
        if (cm.getColumnCount() > courseCol) {
            cm.getColumn(courseCol).setPreferredWidth(80);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(s.fontBody());
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (sel) {
                    setBackground(new Color(99, 102, 241, 80));
                    setForeground(s.textMain());
                } else if (row % 2 == 0) {
                    setBackground(s.bgDark());
                    setForeground(s.textMain());
                } else {
                    setBackground(s.bgMid());
                    setForeground(s.textMain());
                }
                return this;
            }
        });
    }

    private void updateStatus() {
        if (statusLbl == null || activeModel == null) return;
        statusLbl.setText("Active: " + activeModel.getRowCount()
            + "  •  Dropped: " + deletedModel.getRowCount());
    }

    private void dropSelected(AppSettings s) {
        int sel = table.getSelectedRow();
        if (sel < 0) { warn("Please select a student to drop."); return; }
        String id   = (String) activeModel.getValueAt(sel, 0);
        String name = (String) activeModel.getValueAt(sel, 1);
        if (confirm("<html>Drop <b>" + name + "</b> (" + id + ")?<br>Restorable within 30 days.</html>", "Confirm Drop")) {
            SupabaseService.dropStudent(id);
            Object[] row = new Object[activeModel.getColumnCount()];
            for (int i = 0; i < row.length; i++) row[i] = activeModel.getValueAt(sel, i);
            deletedModel.addRow(row);
            activeModel.removeRow(sel);
            updateStatus();
            JOptionPane.showMessageDialog(frame, name + " has been dropped.", "Dropped",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void restoreSelected(AppSettings s) {
        int sel = table.getSelectedRow();
        if (sel < 0) { warn("Please select a student to restore."); return; }
        String id   = (String) deletedModel.getValueAt(sel, 0);
        String name = (String) deletedModel.getValueAt(sel, 1);
        if (confirm("<html>Restore <b>" + name + "</b> (" + id + ")?</html>", "Confirm Restore")) {
            SupabaseService.restoreStudent(id);
            Object[] row = new Object[deletedModel.getColumnCount()];
            for (int i = 0; i < row.length; i++) row[i] = deletedModel.getValueAt(sel, i);
            activeModel.addRow(row);
            deletedModel.removeRow(sel);
            updateStatus();
            JOptionPane.showMessageDialog(frame, name + " has been restored.", "Restored",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelected(AppSettings s) {
        int sel = table.getSelectedRow();
        if (sel < 0) { warn("Please select a student to permanently delete."); return; }

        DefaultTableModel currentModel = showingDeleted ? deletedModel : activeModel;
        String id   = (String) currentModel.getValueAt(sel, 0);
        String name = (String) currentModel.getValueAt(sel, 1);

        int first = JOptionPane.showConfirmDialog(frame,
            "<html><b>Permanently delete " + name + " (" + id + ")?</b><br>"
            + "This will erase ALL of their records from the database immediately.<br>"
            + "This action <u>cannot</u> be undone.</html>",
            "⚠  Permanent Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (first != JOptionPane.YES_OPTION) return;

        int second = JOptionPane.showConfirmDialog(frame,
            "<html>Are you absolutely sure?<br>"
            + "<b>" + name + "</b>'s personal, contact, and academic info will be gone forever.</html>",
            "⚠  Final Confirmation",
            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (second != JOptionPane.YES_OPTION) return;

        boolean ok = SupabaseService.deleteStudentPermanently(id);
        if (ok) {
            currentModel.removeRow(sel);
            updateStatus();
            JOptionPane.showMessageDialog(frame,
                name + " has been permanently deleted.", "Deleted",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame,
                "Delete failed. Check the console for details.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "No Selection", JOptionPane.WARNING_MESSAGE);
    }

    private boolean confirm(String msg, String title) {
        return JOptionPane.showConfirmDialog(frame, msg, title,
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private JButton makeBtn(String text, Color bg, AppSettings s) {
        JButton btn = new JButton(text);
        btn.setFont(s.fontBold());
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
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