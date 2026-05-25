/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sis;

/**
 *
 * @author Jason
 */
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Singleton that holds all user-configurable settings.
 * Listeners are notified whenever a setting changes so every
 * open window can repaint itself immediately.
 */
public class AppSettings {
 
    // ── Enums ────────────────────────────────────────────────────────────────
    public enum Theme  { DARK, LIGHT }
    public enum Layout { DEFAULT, LIST, GRID }
 
    // ── Singleton ────────────────────────────────────────────────────────────
    private static AppSettings instance;
    public  static AppSettings get() {
        if (instance == null) instance = new AppSettings();
        return instance;
    }
    private AppSettings() {}
 
    // ── State ────────────────────────────────────────────────────────────────
    private Theme  theme    = Theme.DARK;
    private Layout layout   = Layout.DEFAULT;
    private int    fontSize = 13;          // base body font size
 
    // ── Getters ──────────────────────────────────────────────────────────────
    public Theme  getTheme()    { return theme;    }
    public Layout getLayout()   { return layout;   }
    public int    getFontSize() { return fontSize; }
    public boolean isDark()     { return theme == Theme.DARK; }
 
    // ── Setters ──────────────────────────────────────────────────────────────
    public void setTheme(Theme t)    { theme    = t; fireChange(); }
    public void setLayout(Layout l)  { layout   = l; fireChange(); }
    public void setFontSize(int s)   { fontSize = s; fireChange(); }
 
    // ── Resolved colors (used everywhere instead of hard-coded values) ───────
    public Color bgDark()   { return isDark() ? new Color(15,23,42)   : new Color(241,245,249); }
    public Color bgMid()    { return isDark() ? new Color(30,41,59)   : new Color(226,232,240); }
    public Color bgPanel()  { return isDark() ? new Color(51,65,85)   : new Color(203,213,225); }
    public Color textMain() { return isDark() ? new Color(248,250,252): new Color(15,23,42);    }
    public Color textSub()  { return isDark() ? new Color(148,163,184): new Color(71,85,105);   }
    public Color border()   { return isDark() ? new Color(71,85,105)  : new Color(148,163,184); }
 
    // Fixed accents stay the same in both themes
    public Color accentIndigo()  { return new Color(99,102,241);  }
    public Color accentGreen()   { return new Color(34,197,94);   }
    public Color accentAmber()   { return new Color(245,158,11);  }
    public Color accentSlate()   { return new Color(100,116,139); }
    public Color accentDanger()  { return new Color(239,68,68);   }
 
    // ── Font helpers ─────────────────────────────────────────────────────────
    public Font fontBody()  { return new Font("Segoe UI", Font.PLAIN,  fontSize);     }
    public Font fontBold()  { return new Font("Segoe UI", Font.BOLD,   fontSize);     }
    public Font fontSmall() { return new Font("Segoe UI", Font.PLAIN,  fontSize - 1); }
    public Font fontTitle() { return new Font("Georgia",  Font.BOLD,   fontSize + 11);}
    public Font fontH2()    { return new Font("Georgia",  Font.BOLD,   fontSize + 5); }
 
    // ── Change listeners ─────────────────────────────────────────────────────
    public interface ChangeListener { void onSettingsChanged(); }
    private final List<ChangeListener> listeners = new ArrayList<>();
    public void addListener(ChangeListener l)    { listeners.add(l);    }
    public void removeListener(ChangeListener l) { listeners.remove(l); }
    private void fireChange() {
        // run on EDT
        SwingUtilities.invokeLater(() -> listeners.forEach(ChangeListener::onSettingsChanged));
    }
}
