package com.peerlearn.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/** Moitié gauche des écrans de connexion et d'inscription : dégradé, logo et trois arguments. */
public class PanneauMarque extends JComponent {
    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(0x1E3A8A), w, h, new Color(0x2563EB)));
        g.fillRect(0, 0, w, h);

        // cercles décoratifs
        g.setColor(new Color(255, 255, 255, 18));
        g.fillOval(-90, h - 230, 330, 330);
        g.fillOval(w - 170, -110, 300, 300);

        int x = 56;
        int y = 72;
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, 46, 46, 14, 14);
        g.setColor(Theme.ACCENT);
        g.setFont(Theme.police(26, Font.BOLD));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("P", x + (46 - fm.stringWidth("P")) / 2, y + (46 - fm.getHeight()) / 2 + fm.getAscent());
        g.setColor(Color.WHITE);
        g.setFont(Theme.police(24, Font.BOLD));
        g.drawString("PeerLearn", x + 62, y + 31);

        g.setFont(Theme.police(34, Font.BOLD));
        g.drawString("Apprendre", x, y + 150);
        g.drawString("ensemble.", x, y + 194);

        g.setFont(Theme.police(15));
        g.setColor(new Color(0xDBEAFE));
        String[] arguments = {
            "Trouvez un tuteur dans votre matière",
            "Suivez votre progression séance après séance",
            "Échangez et partagez des exercices",
        };
        int ligne = y + 250;
        for (String a : arguments) {
            g.setColor(new Color(255, 255, 255, 60));
            g.fillOval(x, ligne - 12, 18, 18);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawPolyline(new int[] {x + 5, x + 8, x + 13}, new int[] {ligne - 3, ligne, ligne - 7}, 3);
            g.setColor(new Color(0xDBEAFE));
            g.drawString(a, x + 30, ligne + 2);
            ligne += 36;
        }

        g.setFont(Theme.police(12));
        g.setColor(new Color(255, 255, 255, 150));
        g.drawString("Tutorat entre étudiants - Université de Tours, site de Blois", x, h - 40);
        g.dispose();
    }
}
