package com.peerlearn.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

/**
 * Charte graphique de PeerLearn : couleurs, police et apparence de base.
 * Si la bibliothèque FlatLaf est présente dans le classpath (dépendance facultative du pom.xml),
 * elle est utilisée ; sinon on prend Nimbus, fourni avec Java. Les éléments propres à l'application
 * (menu, boutons, tableaux) sont dessinés par notre code et ont le même rendu dans les deux cas.
 */
public final class Theme {

    public static final Color ACCENT = new Color(0x2563EB);
    public static final Color ACCENT_FONCE = new Color(0x1D4ED8);
    public static final Color ACCENT_CLAIR = new Color(0xDBEAFE);

    public static final Color MENU = new Color(0x0F172A);
    public static final Color MENU_SURVOL = new Color(0x1E293B);
    public static final Color MENU_TEXTE = new Color(0xCBD5E1);
    public static final Color MENU_DISCRET = new Color(0x64748B);

    public static final Color FOND = new Color(0xF8FAFC);
    public static final Color CARTE = Color.WHITE;
    public static final Color RAYURE = new Color(0xF8FAFC);
    public static final Color ENTETE = new Color(0xF1F5F9);
    public static final Color BORDURE = new Color(0xE2E8F0);
    public static final Color BORDURE_FORTE = new Color(0xCBD5E1);

    public static final Color TEXTE = new Color(0x0F172A);
    public static final Color TEXTE_DISCRET = new Color(0x64748B);

    public static final Color SUCCES = new Color(0x15803D);
    public static final Color ATTENTE = new Color(0xB45309);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_TEXTE = new Color(0xB91C1C);

    private static final String FAMILLE = choisirFamille();
    private static boolean flatlaf;

    private Theme() { }

    public static boolean flatlafActif() {
        return flatlaf;
    }

    public static Font police(float taille, int style) {
        return new Font(FAMILLE, style, Math.round(taille));
    }

    public static Font police(float taille) {
        return police(taille, Font.PLAIN);
    }

    /** À appeler une seule fois, avant de créer la première fenêtre. */
    public static void installer() {
        // Réglages Nimbus : à poser AVANT l'installation du thème
        UIManager.put("control", FOND);
        UIManager.put("nimbusBase", new Color(0x1E40AF));
        UIManager.put("nimbusBlueGrey", new Color(0x94A3B8));
        UIManager.put("nimbusFocus", ACCENT);
        UIManager.put("nimbusSelectionBackground", ACCENT);
        UIManager.put("nimbusLightBackground", Color.WHITE);
        UIManager.put("text", TEXTE);

        flatlaf = installerParNom("com.formdev.flatlaf.FlatLightLaf");
        if (!flatlaf) {
            boolean nimbus = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    nimbus = installerParNom(info.getClassName());
                }
            }
            if (!nimbus) {
                installerParNom(UIManager.getSystemLookAndFeelClassName());
            }
        }

        UIManager.put("Panel.background", FOND);
        UIManager.put("Viewport.background", Color.WHITE);
        UIManager.put("OptionPane.background", FOND);
        UIManager.put("TextField.margin", new Insets(4, 8, 4, 8));
        UIManager.put("PasswordField.margin", new Insets(4, 8, 4, 8));
        UIManager.put("TextField.contentMargins", new Insets(5, 8, 5, 8));
        UIManager.put("PasswordField.contentMargins", new Insets(5, 8, 5, 8));
        if (flatlaf) {
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("Component.focusColor", ACCENT);
            UIManager.put("Component.focusedBorderColor", ACCENT);
            UIManager.put("TextComponent.selectionBackground", ACCENT_CLAIR);
        }
        appliquerPolice();
    }

    private static boolean installerParNom(String classe) {
        try {
            UIManager.setLookAndFeel(classe);
            return true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                 | UnsupportedLookAndFeelException e) {
            return false;
        }
    }

    private static void appliquerPolice() {
        FontUIResource f = new FontUIResource(police(13));
        Enumeration<Object> cles = UIManager.getDefaults().keys();
        while (cles.hasMoreElements()) {
            Object cle = cles.nextElement();
            if (cle instanceof String s && s.endsWith(".font")) {
                UIManager.put(cle, f);
            }
        }
        UIManager.put("defaultFont", f);
    }

    private static String choisirFamille() {
        String[] disponibles = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String souhaitee : new String[] {"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "Noto Sans", "DejaVu Sans"}) {
            if (Arrays.asList(disponibles).contains(souhaitee)) {
                return souhaitee;
            }
        }
        return Font.SANS_SERIF;
    }
}
