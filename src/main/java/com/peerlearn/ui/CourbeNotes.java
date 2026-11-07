package com.peerlearn.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;

/** Courbe simple des notes de suivi (0 à 5), dans l'ordre chronologique des rapports. */
public class CourbeNotes extends JPanel {

    private ArrayList<Integer> notes = new ArrayList<>();

    public CourbeNotes() {
        setPreferredSize(new Dimension(400, 220));
    }

    public void setNotes(ArrayList<Integer> notes) {
        this.notes = new ArrayList<>(notes);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color texte = UIManager.getColor("Label.foreground") != null ? UIManager.getColor("Label.foreground") : Color.DARK_GRAY;
        Color grille = new Color(texte.getRed(), texte.getGreen(), texte.getBlue(), 40);
        Color courbe = new Color(0x2563EB);
        FontMetrics fm = g.getFontMetrics();

        int gauche = 36, droite = 24, haut = 26, bas = 28;
        int largeur = getWidth() - gauche - droite;
        int hauteur = getHeight() - haut - bas;
        if (largeur <= 0 || hauteur <= 0) {
            g.dispose();
            return;
        }

        // grille et graduations 0..5
        for (int v = 0; v <= 5; v++) {
            int y = haut + hauteur - (int) Math.round(hauteur * v / 5.0);
            g.setColor(grille);
            g.drawLine(gauche, y, gauche + largeur, y);
            g.setColor(texte);
            String s = String.valueOf(v);
            g.drawString(s, gauche - 8 - fm.stringWidth(s), y + fm.getAscent() / 2 - 1);
        }

        if (notes.isEmpty()) {
            g.setColor(texte);
            String s = "Aucun rapport pour l'instant.";
            g.drawString(s, gauche + (largeur - fm.stringWidth(s)) / 2, haut + hauteur / 2);
            g.dispose();
            return;
        }

        int n = notes.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = n == 1 ? gauche + largeur / 2 : gauche + (int) Math.round((double) largeur * i / (n - 1));
            ys[i] = haut + hauteur - (int) Math.round(hauteur * notes.get(i) / 5.0);
        }

        g.setColor(courbe);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(xs, ys, n);
        for (int i = 0; i < n; i++) {
            g.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
        }

        g.setColor(texte);
        int pas = Math.max(1, n / 12); // évite de surcharger l'axe quand il y a beaucoup de rapports
        for (int i = 0; i < n; i += pas) {
            String s = String.valueOf(i + 1);
            g.drawString(s, xs[i] - fm.stringWidth(s) / 2, haut + hauteur + fm.getAscent() + 6);
            String note = String.valueOf(notes.get(i));
            g.drawString(note, xs[i] - fm.stringWidth(note) / 2, ys[i] - 8);
        }
        g.dispose();
    }
}
