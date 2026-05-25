package com.mycompany.sis;

/**
 * Application entry point.
 * Boots the single MainFrame window; all screens live inside it.
 */
public class Sis {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(MainFrame::new);
    }
}
