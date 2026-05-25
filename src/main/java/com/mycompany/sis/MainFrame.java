package com.mycompany.sis;

import javax.swing.*;
import java.awt.*;

/**
 * Single-window host for the entire SIS application.
 * All screens (Login, Admin Dashboard, Student Dashboard, sub-panels)
 * are swapped in-place using CardLayout — no secondary JFrames.
 */
public class MainFrame extends JFrame implements AppSettings.ChangeListener {

    // Card names
    public static final String CARD_LOGIN        = "LOGIN";
    public static final String CARD_ADMIN        = "ADMIN";
    public static final String CARD_STUDENT      = "STUDENT";
    public static final String CARD_STUDENTS_TBL = "STUDENTS_TABLE";
    public static final String CARD_ENROLL       = "ENROLL";
    public static final String CARD_SUBJECTS     = "SUBJECTS";
    public static final String CARD_ANNOUNCEMENTS= "ANNOUNCEMENTS";
    public static final String CARD_SETTINGS     = "SETTINGS";

    private final CardLayout   cards  = new CardLayout();
    private final JPanel       deck   = new JPanel(cards);

    // Lazily-created panels (rebuilt on demand)
    private LoginPanel         loginPanel;
    private AdminDashPanel     adminPanel;
    private StudentDashPanel   studentPanel;
    private StudentTablePanel  studentTablePanel;
    private EnrollPanel        enrollPanel;
    private SubjectsPanel      subjectsPanel;
    private AnnouncementsPanel announcementsPanel;
    private SettingsPanel      settingsPanel;

    // Session state
    private String  loggedInUser = "";
    private boolean isAdmin      = false;

    // Tracks which card is currently visible so rebuild can restore it
    private String  currentCard  = CARD_LOGIN;

    // Singleton
    private static MainFrame instance;
    public static MainFrame get() { return instance; }

    public MainFrame() {
        instance = this;
        setTitle("SIS — Student Information System");
        setSize(960, 640);
        setMinimumSize(new Dimension(800, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        deck.setBackground(AppSettings.get().bgDark());
        setContentPane(deck);

        // Build initial panels
        rebuildAll();

        showCard(CARD_LOGIN);
        setVisible(true);
        AppSettings.get().addListener(this);
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    public void showCard(String name) {
        currentCard = name;
        cards.show(deck, name);
    }

    /** Called after a successful login. */
    public void onLogin(String username, boolean adminRole) {
        this.loggedInUser = username;
        this.isAdmin      = adminRole;
        rebuildAll();
        showCard(adminRole ? CARD_ADMIN : CARD_STUDENT);
    }

    /** Called on logout. */
    public void onLogout() {
        loggedInUser = "";
        isAdmin      = false;
        rebuildAll();
        showCard(CARD_LOGIN);
    }

    // ── AppSettings listener ─────────────────────────────────────────────────

    @Override
    public void onSettingsChanged() {
        SwingUtilities.invokeLater(() -> {
            String restore = currentCard; // save before rebuild wipes it
            deck.setBackground(AppSettings.get().bgDark());
            rebuildAll();
            showCard(restore);           // jump straight back to where the user was
        });
    }

    // ── Panel management ────────────────────────────────────────────────────

    private void rebuildAll() {
        deck.removeAll();

        loginPanel         = new LoginPanel(this);
        adminPanel         = new AdminDashPanel(this);
        studentPanel       = new StudentDashPanel(this);
        studentTablePanel  = new StudentTablePanel(this);
        enrollPanel        = new EnrollPanel(this);
        subjectsPanel      = new SubjectsPanel(this);
        announcementsPanel = new AnnouncementsPanel(this);
        settingsPanel      = new SettingsPanel(this);

        deck.add(loginPanel,         CARD_LOGIN);
        deck.add(adminPanel,         CARD_ADMIN);
        deck.add(studentPanel,       CARD_STUDENT);
        deck.add(studentTablePanel,  CARD_STUDENTS_TBL);
        deck.add(enrollPanel,        CARD_ENROLL);
        deck.add(subjectsPanel,      CARD_SUBJECTS);
        deck.add(announcementsPanel, CARD_ANNOUNCEMENTS);
        deck.add(settingsPanel,      CARD_SETTINGS);

        deck.revalidate();
        deck.repaint();
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String  getLoggedInUser() { return loggedInUser; }
    public boolean isAdmin()         { return isAdmin; }

    /** Force-refresh a specific panel (e.g. after enrolling a student). */
    public void refreshStudentTable() {
        studentTablePanel.reload();
    }

    public void refreshAnnouncements() {
        announcementsPanel.reload();
    }

    public void refreshRolesTab() {
        settingsPanel.reloadRoles();
    }
}