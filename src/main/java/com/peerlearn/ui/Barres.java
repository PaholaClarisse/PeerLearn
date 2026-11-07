package com.peerlearn.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

/** Histogramme horizontal : une barre par libellé, triées de la plus grande à la plus petite. */
public class Barres extends JPanel {

    private Map<String, Double> donnees = new LinkedHashMap<>();
    private final String vide;
    private final String suffixe;
    private double maxFixe = 0;

    public Barres(String vide, String suffixe) {
        this.vide = vide;
        this.suffixe = suffixe;
        setOpaque(false);
        setPreferredSize(new Dimension(300, 190));
    }

    /** Impose l'échelle (ex. 5 pour des notes sur 5) au lieu de la plus grande valeur. */
    public Barres echelle(double max) {
        this.maxFixe = max;
        return this;
    }

    public void setDonnees(Map<String, ? extends Number> brut, int limite) {
        LinkedHashMap<String, Double> tri = new LinkedHashMap<>();
        brut.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().doubleValue(), a.getValue().doubleValue()))
                .limit(limite)
                .forEach(e -> tri.put(e.getKey(), e.getValue().doubleValue()));
        this.donnees = tri;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(Theme.police(12));
        FontMetrics fm = g.getFontMetrics();
        if (donnees.isEmpty()) {
            g.setColor(Theme.TEXTE_DISCRET);
            g.drawString(vide, 0, fm.getAscent() + 4);
            g.dispose();
            return;
        }
        double max = maxFixe > 0 ? maxFixe : donnees.values().stream().mapToDouble(d -> d).max().orElse(1);
        if (max <= 0) max = 1;
        int libelle = Math.min(130, donnees.keySet().stream().mapToInt(fm::stringWidth).max().orElse(60) + 10);
        int valeurLarg = fm.stringWidth("00,0" + suffixe) + 8;
        int zone = Math.max(20, getWidth() - libelle - valeurLarg);
        int pas = 30;
        int y = 4;
        for (Map.Entry<String, Double> e : donnees.entrySet()) {
            String nom = e.getKey();
            while (fm.stringWidth(nom) > libelle - 8 && nom.length() > 2) nom = nom.substring(0, nom.length() - 2) + "…";
            g.setColor(Theme.TEXTE);
            g.drawString(nom, 0, y + 15);
            int w = (int) Math.round(zone * e.getValue() / max);
            g.setColor(new Color(0xE2E8F0));
            g.fillRoundRect(libelle, y + 4, zone, 12, 12, 12);
            g.setColor(Theme.ACCENT);
            g.fillRoundRect(libelle, y + 4, Math.max(w, 6), 12, 12, 12);
            double v = e.getValue();
            String txt = (v == Math.rint(v) ? String.valueOf((long) v) : String.format(java.util.Locale.FRANCE, "%.1f", v)) + suffixe;
            g.setColor(Theme.TEXTE_DISCRET);
            g.drawString(txt, libelle + zone + 8, y + 15);
            y += pas;
        }
        g.dispose();
    }
}
