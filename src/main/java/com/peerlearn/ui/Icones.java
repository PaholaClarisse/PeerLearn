package com.peerlearn.ui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

/** Petits pictogrammes dessinés à la main (traits fins), sans image ni dépendance. Grille de 18 x 18. */
final class Icones {

    private Icones() { }

    /** Dessine l'icône associée au titre de page, dans le carré (x, y, 18, 18). Couleur déjà choisie dans g. */
    static void dessiner(Graphics2D g0, String titre, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (titre) {
            case "Tableau de bord" -> {
                g.drawRoundRect(2, 2, 5, 5, 2, 2);
                g.drawRoundRect(11, 2, 5, 5, 2, 2);
                g.drawRoundRect(2, 11, 5, 5, 2, 2);
                g.drawRoundRect(11, 11, 5, 5, 2, 2);
            }
            case "Trouver un tuteur" -> {
                g.drawOval(2, 2, 10, 10);
                g.drawLine(10, 10, 16, 16);
            }
            case "Mes demandes", "Demandes reçues" -> { // plateau de réception
                g.drawLine(2, 10, 6, 10);
                g.drawLine(12, 10, 16, 10);
                g.drawLine(6, 10, 7, 13);
                g.drawLine(11, 10, 12, 13);
                g.drawLine(7, 13, 11, 13);
                g.drawLine(2, 10, 4, 3);
                g.drawLine(16, 10, 14, 3);
                g.drawLine(4, 3, 14, 3);
                g.drawLine(2, 10, 2, 16);
                g.drawLine(16, 10, 16, 16);
                g.drawLine(2, 16, 16, 16);
            }
            case "Messagerie" -> {
                g.drawRoundRect(2, 2, 14, 10, 4, 4);
                Path2D queue = new Path2D.Double();
                queue.moveTo(6, 12);
                queue.lineTo(6, 16);
                queue.lineTo(10, 12);
                g.draw(queue);
            }
            case "Exercices", "Exercices publiés", "Rapports" -> { // feuille avec lignes
                Path2D f = new Path2D.Double();
                f.moveTo(4, 2); f.lineTo(11, 2); f.lineTo(15, 6); f.lineTo(15, 16); f.lineTo(4, 16); f.closePath();
                g.draw(f);
                g.drawLine(11, 2, 11, 6);
                g.drawLine(11, 6, 15, 6);
                g.drawLine(7, 10, 12, 10);
                g.drawLine(7, 13, 12, 13);
            }
            case "Progression", "Statistiques" -> {
                g.drawLine(2, 16, 16, 16);
                g.drawLine(4, 16, 4, 11);
                g.drawLine(9, 16, 9, 6);
                g.drawLine(14, 16, 14, 2);
            }
            case "Séances" -> {
                g.drawRoundRect(2, 3, 14, 13, 3, 3);
                g.drawLine(2, 7, 16, 7);
                g.drawLine(6, 1, 6, 4);
                g.drawLine(12, 1, 12, 4);
                g.fillOval(6, 10, 2, 2);
                g.fillOval(10, 10, 2, 2);
            }
            case "Notifications" -> {
                Path2D c = new Path2D.Double();
                c.moveTo(3, 13); c.lineTo(5, 11); c.lineTo(5, 8);
                c.append(new Arc2D.Double(5, 2, 8, 8, 180, -180, Arc2D.OPEN), true);
                c.lineTo(13, 11); c.lineTo(15, 13); c.closePath();
                g.draw(c);
                g.drawArc(7, 13, 4, 4, 180, 180);
            }
            case "Évaluations" -> { // étoile
                Path2D s = new Path2D.Double();
                for (int i = 0; i < 10; i++) {
                    double r = i % 2 == 0 ? 8 : 3.6;
                    double a = Math.PI / 5 * i - Math.PI / 2;
                    double px = 9 + r * Math.cos(a);
                    double py = 9.5 + r * Math.sin(a);
                    if (i == 0) s.moveTo(px, py); else s.lineTo(px, py);
                }
                s.closePath();
                g.draw(s);
            }
            case "Matières" -> { // livre ouvert
                g.drawLine(9, 4, 9, 15);
                Path2D l = new Path2D.Double();
                l.moveTo(9, 4); l.curveTo(7, 2, 4, 2, 2, 3); l.lineTo(2, 14); l.curveTo(4, 13, 7, 13, 9, 15);
                g.draw(l);
                Path2D r = new Path2D.Double();
                r.moveTo(9, 4); r.curveTo(11, 2, 14, 2, 16, 3); r.lineTo(16, 14); r.curveTo(14, 13, 11, 13, 9, 15);
                g.draw(r);
            }
            case "Tuteurs" -> { // deux personnes
                g.drawOval(2, 3, 6, 6);
                g.drawArc(0, 11, 10, 8, 0, 180);
                g.drawOval(10, 4, 5, 5);
                g.drawArc(9, 12, 9, 7, 0, 180);
            }
            default -> { // Mon compte, Mon profil tuteur : une personne
                g.drawOval(5, 2, 8, 8);
                g.drawArc(2, 12, 14, 12, 0, 180);
            }
        }
        g.dispose();
    }
}
