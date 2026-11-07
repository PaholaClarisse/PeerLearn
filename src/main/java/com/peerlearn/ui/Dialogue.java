package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Boîtes de dialogue aux couleurs de l'application (remplacent JOptionPane) :
 * information, erreur, confirmation et petit formulaire.
 */
public final class Dialogue {

    private enum Type {
        INFO(Theme.SUCCES), ERREUR(Theme.DANGER_TEXTE), CONFIRMATION(Theme.ATTENTE), DANGER(Theme.DANGER_TEXTE), FORMULAIRE(Theme.ACCENT);

        final Color couleur;

        Type(Color couleur) {
            this.couleur = couleur;
        }
    }

    private Dialogue() { }

    public static void info(Component parent, String message) {
        afficher(parent, Type.INFO, "PeerLearn", message, null, "OK", null);
    }

    public static void erreur(Component parent, String message) {
        afficher(parent, Type.ERREUR, "Une erreur est survenue", message, null, "Fermer", null);
    }

    /** Confirmation : renvoie true si l'utilisateur valide. danger=true colore l'action en rouge. */
    public static boolean confirmer(Component parent, String titre, String message, String libelleOk, boolean danger) {
        return afficher(parent, danger ? Type.DANGER : Type.CONFIRMATION, titre, message, null, libelleOk, "Annuler");
    }

    /** Petit formulaire dans une fenêtre propre : renvoie true si l'utilisateur valide. */
    public static boolean formulaire(Component parent, String titre, JComponent contenu, String libelleOk) {
        return afficher(parent, Type.FORMULAIRE, titre, null, contenu, libelleOk, "Annuler");
    }

    // ------------------------------------------------------------------ interne

    private static boolean afficher(Component parent, Type type, String titre, String message,
                                    JComponent contenu, String ok, String annuler) {
        Window fenetre = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(fenetre, "PeerLearn", JDialog.ModalityType.APPLICATION_MODAL);
        boolean[] valide = {false};

        JPanel corps = new JPanel(new BorderLayout(16, 0));
        corps.setBackground(Color.WHITE);
        corps.setBorder(BorderFactory.createEmptyBorder(24, 26, 8, 26));
        if (type != Type.FORMULAIRE) corps.add(new Icone(type), BorderLayout.WEST);

        JPanel texte = new JPanel(new BorderLayout(0, 6));
        texte.setOpaque(false);
        JLabel t = new JLabel(titre);
        t.setFont(Theme.police(17, Font.BOLD));
        t.setForeground(Theme.TEXTE);
        texte.add(t, BorderLayout.NORTH);
        if (message != null) {
            JLabel m = new JLabel("<html><body style='width:330px'>" + html(message) + "</body></html>");
            m.setFont(Theme.police(13));
            m.setForeground(Theme.TEXTE_DISCRET);
            m.setVerticalAlignment(JLabel.TOP);
            texte.add(m, BorderLayout.CENTER);
        }
        if (contenu != null) {
            contenu.setOpaque(false);
            JPanel enveloppe = new JPanel(new BorderLayout());
            enveloppe.setOpaque(false);
            enveloppe.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            enveloppe.add(contenu, BorderLayout.CENTER);
            texte.add(enveloppe, BorderLayout.CENTER);
        }
        corps.add(texte, BorderLayout.CENTER);

        Bouton bOk = new Bouton(ok);
        if (type == Type.DANGER || type == Type.ERREUR) bOk.danger(); else bOk.principal();
        bOk.addActionListener(e -> { valide[0] = true; d.dispose(); });
        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bas.setBackground(Color.WHITE);
        bas.setBorder(BorderFactory.createEmptyBorder(14, 26, 20, 26));
        if (annuler != null) {
            Bouton bAnnuler = new Bouton(annuler);
            bAnnuler.addActionListener(e -> d.dispose());
            bas.add(bAnnuler);
        }
        bas.add(bOk);

        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(Color.WHITE);
        d.add(corps, BorderLayout.CENTER);
        d.add(bas, BorderLayout.SOUTH);
        d.getRootPane().setDefaultButton(bOk);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.setResizable(false);
        d.pack();
        if (d.getWidth() < 420) d.setSize(420, d.getHeight());
        d.setLocationRelativeTo(fenetre);
        d.setVisible(true); // bloque jusqu'à la fermeture
        return valide[0];
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    /** Pastille ronde teintée avec un pictogramme dessiné. */
    private static final class Icone extends JComponent {
        private final Type type;

        Icone(Type type) {
            this.type = type;
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = type.couleur;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
            g.fillOval(0, 0, 43, 43);
            g.setColor(c);
            g.setStroke(new java.awt.BasicStroke(3f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            switch (type) {
                case INFO -> g.drawPolyline(new int[] {13, 20, 31}, new int[] {23, 30, 14}, 3);
                case ERREUR -> {
                    g.drawLine(15, 15, 28, 28);
                    g.drawLine(28, 15, 15, 28);
                }
                default -> { // attention : point d'exclamation
                    g.drawLine(21, 12, 21, 25);
                    g.fillOval(19, 29, 4, 4);
                }
            }
            g.dispose();
        }
    }
}
