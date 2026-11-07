package com.peerlearn.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Mise en forme commune des tableaux, listes, titres et zones défilantes. */
public final class Style {

    /** Mots d'état affichés en couleur dans les tableaux (le texte reste lisible sans la couleur). */
    private static final Map<String, Color> ETATS = Map.ofEntries(
            Map.entry("Acceptée", Theme.SUCCES),
            Map.entry("Actif", Theme.SUCCES),
            Map.entry("Présent", Theme.SUCCES),
            Map.entry("Pointée", Theme.SUCCES),
            Map.entry("Planifiée", Theme.ACCENT),
            Map.entry("En attente", Theme.ATTENTE),
            Map.entry("En pause", Theme.ATTENTE),
            Map.entry("Non pointé", Theme.ATTENTE),
            Map.entry("Passée, à pointer", Theme.ATTENTE),
            Map.entry("Refusée", Theme.DANGER_TEXTE),
            Map.entry("Annulée", Theme.DANGER_TEXTE),
            Map.entry("Retiré", Theme.DANGER_TEXTE),
            Map.entry("Absent", Theme.DANGER_TEXTE),
            Map.entry("Terminée", Theme.TEXTE_DISCRET));

    private Style() { }

    /** Titre d'une section d'écran (au-dessus d'un tableau, d'un formulaire...). */
    public static JLabel section(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Theme.police(13, Font.BOLD));
        l.setForeground(Theme.TEXTE);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

    /** Texte d'aide discret. */
    public static JLabel aide(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Theme.police(12));
        l.setForeground(Theme.TEXTE_DISCRET);
        return l;
    }

    public static JScrollPane defiler(JComponent contenu) {
        JScrollPane sp = new JScrollPane();
        sp.setViewport(new VueAvecMessage());
        sp.setViewportView(contenu);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDURE));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static <T extends JTable> T table(T t) {
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setGridColor(Theme.BORDURE);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.setSelectionBackground(Theme.ACCENT_CLAIR);
        t.setSelectionForeground(Theme.TEXTE);
        t.setFont(Theme.police(13));
        CelluleTableau rendu = new CelluleTableau();
        t.setDefaultRenderer(Object.class, rendu);
        t.setDefaultRenderer(Number.class, rendu);
        // Le message « tableau vide » est dessiné par la zone visible : on la redessine en entier
        // à chaque changement de données, sinon l'ancien message reste affiché sous les lignes.
        javax.swing.event.TableModelListener redessin = e -> {
            java.awt.Container parent = t.getParent();
            if (parent instanceof javax.swing.JViewport) parent.repaint();
        };
        t.getModel().addTableModelListener(redessin);
        t.addPropertyChangeListener("model", e -> {
            if (e.getNewValue() instanceof javax.swing.table.TableModel m) m.addTableModelListener(redessin);
        });
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new EnteteTableau());
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        return t;
    }

    public static <T> JList<T> liste(JList<T> l) {
        l.setFixedCellHeight(-1);
        l.setSelectionBackground(Theme.ACCENT_CLAIR);
        l.setSelectionForeground(Theme.TEXTE);
        l.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> liste, Object valeur, int index,
                                                          boolean selectionne, boolean focus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(liste, valeur, index, selectionne, false);
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDURE),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                c.setBackground(selectionne ? Theme.ACCENT_CLAIR : Color.WHITE);
                c.setForeground(Theme.TEXTE);
                c.setFont(Theme.police(13, selectionne ? Font.BOLD : Font.PLAIN));
                return c;
            }
        });
        return l;
    }

    /** Zone visible d'un tableau : affiche un message centré quand le tableau est vide. */
    private static final class VueAvecMessage extends javax.swing.JViewport {
        @Override
        public void paint(java.awt.Graphics g0) {
            super.paint(g0);
            if (!(getView() instanceof JTable t) || t.getRowCount() > 0) return;
            Object perso = t.getClientProperty("messageVide");
            String texte = perso == null ? "Rien à afficher pour l'instant." : perso.toString();
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(Theme.police(13));
            g.setColor(Theme.TEXTE_DISCRET);
            java.awt.FontMetrics fm = g.getFontMetrics();
            int y = Math.min(getHeight() / 2, 90);
            // coupe en lignes de 60 caractères au plus
            java.util.List<String> lignes = new java.util.ArrayList<>();
            StringBuilder ligne = new StringBuilder();
            for (String mot : texte.split(" ")) {
                if (ligne.length() + mot.length() > 60) { lignes.add(ligne.toString().trim()); ligne.setLength(0); }
                ligne.append(mot).append(' ');
            }
            lignes.add(ligne.toString().trim());
            for (String l : lignes) {
                g.drawString(l, (getWidth() - fm.stringWidth(l)) / 2, y);
                y += fm.getHeight() + 2;
            }
            g.dispose();
        }
    }

    private static final class CelluleTableau extends DefaultTableCellRenderer {
        private Color pastille;

        @Override
        protected void paintComponent(java.awt.Graphics g0) {
            if (pastille == null) {
                super.paintComponent(g0);
                return;
            }
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            java.awt.FontMetrics fm = g.getFontMetrics(getFont());
            int w = fm.stringWidth(getText()) + 24;
            int h = 22;
            int y = (getHeight() - h) / 2;
            g.setColor(new Color(pastille.getRed(), pastille.getGreen(), pastille.getBlue(), 32));
            g.fillRoundRect(8, y, w, h, h, h);
            g.dispose();
            setOpaque(false);
            super.paintComponent(g0);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object valeur, boolean selectionne,
                                                       boolean focus, int ligne, int colonne) {
            super.getTableCellRendererComponent(table, valeur, selectionne, false, ligne, colonne);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setBackground(selectionne ? Theme.ACCENT_CLAIR : (ligne % 2 == 0 ? Color.WHITE : Theme.RAYURE));
            Color couleur = valeur == null ? null : ETATS.get(valeur.toString());
            pastille = couleur;
            if (couleur != null) {
                setForeground(couleur);
                setFont(Theme.police(12, Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 12));
            } else {
                setForeground(Theme.TEXTE);
                setFont(Theme.police(13));
            }
            return this;
        }
    }

    private static final class EnteteTableau extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object valeur, boolean selectionne,
                                                       boolean focus, int ligne, int colonne) {
            super.getTableCellRendererComponent(table, valeur, false, false, ligne, colonne);
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(true);
            setBackground(Theme.ENTETE);
            setForeground(Theme.TEXTE_DISCRET);
            setFont(Theme.police(12, Font.BOLD));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDURE_FORTE),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }
}
