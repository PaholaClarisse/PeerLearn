package com.peerlearn.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Menu latéral + zone de contenu à pages. Remplace les onglets : les entrées sont regroupées
 * par sections, l'utilisateur connecté et le bouton de déconnexion sont en bas du menu, et
 * l'entrée « Notifications » affiche un compteur de messages non lus (relu toutes les 20 s).
 */
public class Navigation extends JPanel {

    private static final int LARGEUR_MENU = 240;

    private final CardLayout cartes = new CardLayout();
    private final JPanel pages = new JPanel(cartes);
    private final JPanel liste = new JPanel();
    private final JLabel titrePage = new JLabel(" ");
    private final LinkedHashMap<String, Entree> entrees = new LinkedHashMap<>();
    private final Callable<Integer> compteur;
    private final Timer minuteur = new Timer(20_000, e -> rafraichirBadge());
    private String courant;
    private String entreeNotifications;

    public Navigation(String utilisateur, String role, Runnable deconnexion, Callable<Integer> compteur) {
        this.compteur = compteur;
        setLayout(new BorderLayout());

        // ---- menu
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(Theme.MENU);
        menu.setPreferredSize(new Dimension(LARGEUR_MENU, 100));
        menu.add(new Marque(), BorderLayout.NORTH);
        liste.setLayout(new BoxLayout(liste, BoxLayout.Y_AXIS));
        liste.setBackground(Theme.MENU);
        liste.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        menu.add(liste, BorderLayout.CENTER);
        menu.add(new CarteUtilisateur(utilisateur, role, deconnexion), BorderLayout.SOUTH);

        // ---- contenu
        titrePage.setFont(Theme.police(20, Font.BOLD));
        titrePage.setForeground(Theme.TEXTE);
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(Color.WHITE);
        entete.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDURE),
                BorderFactory.createEmptyBorder(16, 28, 14, 28)));
        entete.add(titrePage, BorderLayout.CENTER);
        pages.setBackground(Theme.FOND);
        JPanel contenu = new JPanel(new BorderLayout());
        contenu.add(entete, BorderLayout.NORTH);
        contenu.add(pages, BorderLayout.CENTER);

        add(menu, BorderLayout.WEST);
        add(contenu, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------ construction

    public void section(String libelle) {
        JLabel l = new JLabel(libelle.toUpperCase());
        l.setFont(Theme.police(11, Font.BOLD));
        l.setForeground(Theme.MENU_DISCRET);
        l.setBorder(BorderFactory.createEmptyBorder(16, 14, 6, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        liste.add(l);
    }

    public void ajouter(String titre, JComponent page) {
        Entree e = new Entree(titre, page);
        entrees.put(titre, e);
        liste.add(e);
        pages.add(page, titre);
    }

    /** Désigne l'entrée qui porte le compteur de notifications non lues. */
    public void suivreNotifications(String titreEntree) {
        this.entreeNotifications = titreEntree;
    }

    /** Affiche la première entrée. À appeler une fois toutes les pages ajoutées. */
    public void demarrer() {
        if (!entrees.isEmpty()) {
            selectionner(entrees.keySet().iterator().next());
        }
    }

    // --------------------------------------------------------------- navigation

    public void selectionner(String titre) {
        Entree e = entrees.get(titre);
        if (e == null) return;
        if (courant != null) entrees.get(courant).setSelectionne(false);
        courant = titre;
        e.setSelectionne(true);
        titrePage.setText(titre);
        cartes.show(pages, titre);
        if (e.page instanceof Actualisable a) {
            a.actualiser();
        }
        rafraichirBadge();
    }

    public void setBadge(String titre, int nombre) {
        Entree e = entrees.get(titre);
        if (e != null) e.setBadge(nombre);
    }

    /** Relit le nombre de notifications non lues (sans boîte d'erreur en cas d'échec). */
    public void rafraichirBadge() {
        if (compteur == null || entreeNotifications == null) return;
        Taches.executerSilencieux(compteur, n -> setBadge(entreeNotifications, n));
    }

    // Le minuteur ne tourne que tant que l'écran est dans une fenêtre (arrêté à la déconnexion).
    @Override
    public void addNotify() {
        super.addNotify();
        minuteur.start();
    }

    @Override
    public void removeNotify() {
        minuteur.stop();
        super.removeNotify();
    }

    // ------------------------------------------------------------ composants

    /** Une entrée du menu. */
    private final class Entree extends JComponent {
        private final String titre;
        private final JComponent page;
        private boolean selectionne;
        private boolean survol;
        private int badge;

        Entree(String titre, JComponent page) {
            this.titre = titre;
            this.page = page;
            setAlignmentX(LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(LARGEUR_MENU - 16, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { survol = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { survol = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { selectionner(Entree.this.titre); requestFocusInWindow(); }
            });
            addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                        selectionner(Entree.this.titre);
                    }
                }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        void setSelectionne(boolean s) {
            selectionne = s;
            repaint();
        }

        void setBadge(int n) {
            badge = n;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (selectionne) {
                g.setColor(Theme.ACCENT);
                g.fillRoundRect(0, 3, w, h - 6, 10, 10);
            } else if (survol) {
                g.setColor(Theme.MENU_SURVOL);
                g.fillRoundRect(0, 3, w, h - 6, 10, 10);
            }
            if (isFocusOwner() && !selectionne) {
                g.setColor(Theme.MENU_DISCRET);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(1, 4, w - 3, h - 8, 10, 10);
            }
            g.setFont(Theme.police(13, selectionne ? Font.BOLD : Font.PLAIN));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(selectionne ? Color.WHITE : Theme.MENU_TEXTE);
            Icones.dessiner(g, titre, 14, (h - 18) / 2);
            g.setFont(Theme.police(13, selectionne ? Font.BOLD : Font.PLAIN));
            g.drawString(titre, 44, (h - fm.getHeight()) / 2 + fm.getAscent());

            if (badge > 0) {
                String t = badge > 9 ? "9+" : String.valueOf(badge);
                g.setFont(Theme.police(11, Font.BOLD));
                FontMetrics fb = g.getFontMetrics();
                int largeur = Math.max(20, fb.stringWidth(t) + 12);
                int x = w - largeur - 10;
                int y = (h - 20) / 2;
                g.setColor(Theme.DANGER);
                g.fillRoundRect(x, y, largeur, 20, 20, 20);
                g.setColor(Color.WHITE);
                g.drawString(t, x + (largeur - fb.stringWidth(t)) / 2, y + (20 - fb.getHeight()) / 2 + fb.getAscent());
            }
            g.dispose();
        }
    }

    /** Logo et nom de l'application, en haut du menu. */
    private static final class Marque extends JComponent {
        Marque() {
            setPreferredSize(new Dimension(LARGEUR_MENU, 72));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int y = (getHeight() - 36) / 2;
            g.setColor(Theme.ACCENT);
            g.fillRoundRect(20, y, 36, 36, 12, 12);
            g.setColor(Color.WHITE);
            g.setFont(Theme.police(20, Font.BOLD));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("P", 20 + (36 - fm.stringWidth("P")) / 2, y + (36 - fm.getHeight()) / 2 + fm.getAscent());
            g.setFont(Theme.police(19, Font.BOLD));
            fm = g.getFontMetrics();
            g.drawString("PeerLearn", 66, y + (36 - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Pastille d'initiales, nom, rôle et bouton de déconnexion, en bas du menu. */
    private static final class CarteUtilisateur extends JPanel {
        CarteUtilisateur(String nom, String role, Runnable deconnexion) {
            setLayout(new BorderLayout(0, 12));
            setBackground(Theme.MENU);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x1E293B)),
                    BorderFactory.createEmptyBorder(14, 16, 16, 16)));

            JPanel identite = new JPanel(new BorderLayout(10, 0));
            identite.setOpaque(false);
            identite.add(new Avatar(initiales(nom)), BorderLayout.WEST);
            JPanel textes = new JPanel();
            textes.setOpaque(false);
            textes.setLayout(new BoxLayout(textes, BoxLayout.Y_AXIS));
            JLabel n = new JLabel(nom);
            n.setFont(Theme.police(13, Font.BOLD));
            n.setForeground(Color.WHITE);
            JLabel r = new JLabel(role);
            r.setFont(Theme.police(12));
            r.setForeground(Theme.MENU_DISCRET);
            textes.add(n);
            textes.add(r);
            identite.add(textes, BorderLayout.CENTER);

            Bouton sortie = new Bouton("Se déconnecter").sombre();
            sortie.addActionListener(e -> deconnexion.run());
            add(identite, BorderLayout.CENTER);
            add(sortie, BorderLayout.SOUTH);
        }

        private static String initiales(String nom) {
            StringBuilder sb = new StringBuilder();
            for (String mot : nom.trim().split("\\s+")) {
                if (!mot.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(mot.charAt(0)));
            }
            return sb.toString();
        }
    }

    private static final class Avatar extends JComponent {
        private final String initiales;

        Avatar(String initiales) {
            this.initiales = initiales;
            setPreferredSize(new Dimension(38, 38));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(0x334155));
            g.fillOval(0, 0, 38, 38);
            g.setColor(Color.WHITE);
            g.setFont(Theme.police(13, Font.BOLD));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(initiales, (38 - fm.stringWidth(initiales)) / 2, (38 - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
