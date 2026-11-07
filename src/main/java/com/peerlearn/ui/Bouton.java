package com.peerlearn.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import javax.swing.JButton;

/** Bouton arrondi dessiné par l'application : même rendu quel que soit le thème Swing. */
public class Bouton extends JButton {

    private enum Style { SECONDAIRE, PRINCIPAL, DANGER, SOMBRE }

    private Style style = Style.SECONDAIRE;

    public Bouton(String texte) {
        super(texte);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        appliquerPolice();
    }

    /** Action principale de l'écran (bleu plein). */
    public Bouton principal() {
        style = Style.PRINCIPAL;
        appliquerPolice();
        return this;
    }

    /** Action destructrice ou définitive (rouge). */
    public Bouton danger() {
        style = Style.DANGER;
        appliquerPolice();
        return this;
    }

    /** Variante pour fond sombre (menu latéral). */
    public Bouton sombre() {
        style = Style.SOMBRE;
        appliquerPolice();
        return this;
    }

    private void appliquerPolice() {
        setFont(Theme.police(13, style == Style.PRINCIPAL || style == Style.DANGER ? Font.BOLD : Font.PLAIN));
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText() == null ? "" : getText()) + 36, 36);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean actif = isEnabled();
        boolean survol = getModel().isRollover();
        boolean appuye = getModel().isArmed() && getModel().isPressed();
        Color fond;
        Color texte;
        Color bord = null;
        switch (style) {
            case PRINCIPAL -> {
                fond = !actif ? new Color(0x93C5FD) : appuye ? Theme.ACCENT_FONCE.darker() : survol ? Theme.ACCENT_FONCE : Theme.ACCENT;
                texte = Color.WHITE;
            }
            case DANGER -> {
                fond = !actif ? new Color(0xFCA5A5) : appuye ? new Color(0x7F1D1D) : survol ? new Color(0xB91C1C) : Theme.DANGER;
                texte = Color.WHITE;
            }
            case SOMBRE -> {
                fond = survol ? Theme.MENU_SURVOL : Theme.MENU;
                texte = Theme.MENU_TEXTE;
                bord = new Color(0x334155);
            }
            default -> {
                fond = !actif ? new Color(0xF1F5F9) : appuye ? new Color(0xE2E8F0) : survol ? new Color(0xF1F5F9) : Color.WHITE;
                texte = actif ? Theme.TEXTE : new Color(0x94A3B8);
                bord = actif ? Theme.BORDURE_FORTE : Theme.BORDURE;
            }
        }
        int w = getWidth();
        int h = getHeight();
        g.setColor(fond);
        g.fillRoundRect(0, 0, w, h, 10, 10);
        if (bord != null) {
            g.setColor(bord);
            g.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
        }
        if (isFocusOwner()) {
            g.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 140));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);
        }

        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        String t = getText() == null ? "" : getText();
        int x = (w - fm.stringWidth(t)) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(texte);
        g.drawString(t, x, y);
        g.dispose();
    }
}
