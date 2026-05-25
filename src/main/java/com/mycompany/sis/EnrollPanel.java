package com.mycompany.sis;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Enroll-student form — card inside MainFrame.
 */
public class EnrollPanel extends JPanel {

    private final MainFrame frame;
    private JTextField idField, nameField, addressField, emailField, phoneField, emergencyField;
    private JComboBox<String> genderBox, yearBox, sectionBox, statusBox, scholarshipBox, courseBox;
    private List<String[]> courseData;

    public EnrollPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        removeAll();
        AppSettings s = AppSettings.get();
        setBackground(s.bgDark());

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(s.bgMid());
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton backBtn = makeIconBtn("←", s);
        backBtn.addActionListener(e -> frame.showCard(MainFrame.CARD_ADMIN));
        left.add(backBtn);
        JLabel titleLbl = new JLabel("➕  Enroll New Student");
        titleLbl.setFont(s.fontH2());
        titleLbl.setForeground(s.textMain());
        left.add(titleLbl);
        topBar.add(left, BorderLayout.WEST);

        // ── Form ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(s.bgDark());
        form.setBorder(new EmptyBorder(22, 60, 12, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(7, 7, 7, 7);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        idField        = new JTextField();
        nameField      = new JTextField();
        addressField   = new JTextField();
        emailField     = new JTextField();
        phoneField     = new JTextField();
        emergencyField = new JTextField();

        for (JTextField f : new JTextField[]{idField,nameField,addressField,emailField,phoneField,emergencyField})
            styleField(f, s);

        genderBox      = makeCombo(s, "Male","Female","Other");
        yearBox        = makeCombo(s, "1st Year","2nd Year","3rd Year","4th Year");
        sectionBox     = makeCombo(s, "A","B","C","D");
        statusBox      = makeCombo(s, "Regular","Irregular","Transferee","Returnee");
        scholarshipBox = makeCombo(s, "None","CHED","DOST","Institutional","Other");

        courseBox  = new JComboBox<>();
        styleCombo(courseBox, s);
        courseData = SupabaseService.getCourses();
        if (courseData.isEmpty()) courseBox.addItem("(No courses found)");
        else for (String[] c : courseData) courseBox.addItem(c[1] + " — " + c[2]);

        int row = 0;
        addRow(form, gbc, row++, "Student ID",        idField,        s);
        addRow(form, gbc, row++, "Full Name",          nameField,      s);
        addRow(form, gbc, row++, "Gender",             genderBox,      s);
        addRow(form, gbc, row++, "Home Address",       addressField,   s);
        addRow(form, gbc, row++, "Email",              emailField,     s);
        addRow(form, gbc, row++, "Phone No",           phoneField,     s);
        addRow(form, gbc, row++, "Emergency Contact",  emergencyField, s);
        addRow(form, gbc, row++, "Course",             courseBox,      s);
        addRow(form, gbc, row++, "Year Level",         yearBox,        s);
        addRow(form, gbc, row++, "Section",            sectionBox,     s);
        addRow(form, gbc, row++, "Academic Status",    statusBox,      s);
        addRow(form, gbc, row++, "Scholarship",        scholarshipBox, s);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(BorderFactory.createEmptyBorder());
        scrollForm.getViewport().setBackground(s.bgDark());
        scrollForm.setBackground(s.bgDark());

        // ── Button bar ───────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setBackground(s.bgMid());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(s.fontBody());
        cancelBtn.setForeground(s.textSub());
        cancelBtn.setBackground(s.bgPanel());
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> frame.showCard(MainFrame.CARD_ADMIN));

        JButton enrollBtn = new JButton("✔  Enroll Student");
        enrollBtn.setFont(s.fontBold());
        enrollBtn.setForeground(Color.WHITE);
        enrollBtn.setBackground(s.accentGreen());
        enrollBtn.setFocusPainted(false);
        enrollBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        enrollBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        enrollBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { enrollBtn.setBackground(s.accentGreen().darker()); }
            public void mouseExited(MouseEvent e)  { enrollBtn.setBackground(s.accentGreen()); }
        });
        enrollBtn.addActionListener(e -> submit(s));

        btnPanel.add(cancelBtn);
        btnPanel.add(enrollBtn);

        add(topBar,    BorderLayout.NORTH);
        add(scrollForm, BorderLayout.CENTER);
        add(btnPanel,  BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row,
                        String label, JComponent field, AppSettings s) {
        gbc.gridx=0; gbc.gridy=row; gbc.weightx=0;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(s.fontBold());
        lbl.setForeground(s.textSub());
        form.add(lbl, gbc);
        gbc.gridx=1; gbc.weightx=1;
        form.add(field, gbc);
    }

    private void styleField(JTextField f, AppSettings s) {
        f.setFont(s.fontBody());
        f.setForeground(s.textMain());
        f.setBackground(s.bgMid());
        f.setCaretColor(s.textMain());
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(s.border(), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private JComboBox<String> makeCombo(AppSettings s, String... items) {
        JComboBox<String> box = new JComboBox<>(items);
        styleCombo(box, s);
        return box;
    }

    private void styleCombo(JComboBox<String> box, AppSettings s) {
        box.setFont(s.fontBody());
        box.setForeground(s.textMain());
        box.setBackground(s.bgMid());
        box.setBorder(BorderFactory.createLineBorder(s.border(), 1));
    }

    private void submit(AppSettings s) {
        String id   = idField.getText().trim();
        String name = nameField.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Student ID and Full Name are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idx = courseBox.getSelectedIndex();
        if (courseData.isEmpty() || idx < 0) {
            JOptionPane.showMessageDialog(frame,
                "No course selected.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int courseId = Integer.parseInt(courseData.get(idx)[0]);

        boolean ok = SupabaseService.enrollStudent(
            id, name,
            (String) genderBox.getSelectedItem(),
            addressField.getText().trim(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            emergencyField.getText().trim(),
            courseId,
            (String) yearBox.getSelectedItem(),
            (String) sectionBox.getSelectedItem(),
            (String) statusBox.getSelectedItem(),
            (String) scholarshipBox.getSelectedItem()
        );

        if (ok) {
            // Derive the same credentials SupabaseService just created
            String firstName = name.contains(" ") ? name.substring(0, name.indexOf(" ")) : name;
            String genUsername = firstName + java.time.Year.now().getValue();
            String genPassword = firstName + "123";

            JOptionPane.showMessageDialog(frame,
                "<html><b>Student enrolled successfully!</b><br><br>"
                + "<b>Login credentials have been created:</b><br>"
                + " Username: <code>" + genUsername + "</code><br>"
                + " Password: <code>" + genPassword + "</code><br><br>"
                + "<small>Please share these with the student.</small></html>",
                "Enrolled", JOptionPane.INFORMATION_MESSAGE);
            frame.refreshStudentTable();
            frame.showCard(MainFrame.CARD_ADMIN);
        } else {
            JOptionPane.showMessageDialog(frame,
                "Enrollment failed. Student ID may already exist.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
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