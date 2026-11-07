package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Carte blanche arrondie : soit un indicateur chiffré, soit un conteneur titré. */
public class Carte extends JPanel {

    private final JLabel valeur = new JLabel("-");
    private Color teinte = Theme.ACCENT;

    /** Carte indicateur : grand chiffre, libellé et précision. */
    public Carte(String libelle, String precision) {
        super(new BorderLayout(0, 2));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        valeur.setFont(Theme.police(30, Font.BOLD));
        valeur.setForeground(Theme.TEXTE);
        JLabel l = new JLabel(libelle);
        l.setFont(Theme.police(12, Font.BOLD));
        l.setForeground(Theme.TEXTE_DISCRET);
        add(l, BorderLayout.NORTH);
        add(valeur, BorderLayout.CENTER);
        if (precision != null) add(Style.aide(precision), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(180, 108));
    }

    /** Carte conteneur : titre puis contenu. */
    public Carte(String titre, javax.swing.JComponent contenu) {
        super(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        add(Style.section(titre), BorderLayout.NORTH);
        add(contenu, BorderLayout.CENTER);
    }

    public Carte teinte(Color c) {
        this.teinte = c;
        valeur.setForeground(c);
        return this;
    }

    public void setValeur(String texte) {
        valeur.setText(texte);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g.setColor(Theme.BORDURE);
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g.dispose();
        super.paintComponent(g0);
    }
}
