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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import modele.Demande;
import modele.Etudiant;
import modele.Message;
import modele.StatutDemande;
import service.GestionTutorat;

/**
 * Messagerie entre un tuteur et ses tutorés (R7 : demande acceptée seulement).
 * Une conversation par demande acceptée, que l'on soit le tuteur ou le tutoré.
 * L'administrateur n'y a pas accès. Le fil se rafraîchit seul toutes les 10 secondes.
 */
public class PanneauMessagerie extends JPanel implements Actualisable {

    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRANCE);
    private static final int LARGEUR_MAX_BULLE = 340;

    /** Une ligne de la liste : une demande acceptée et son interlocuteur. */
    private static final class Conversation {
        final Demande demande;
        final String nom;
        final String role;

        Conversation(Demande demande, String nom, String role) {
            this.demande = demande;
            this.nom = nom;
            this.role = role;
        }
    }

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final DefaultListModel<Conversation> modeleListe = new DefaultListModel<>();
    private final JList<Conversation> liste = new JList<>(modeleListe);
    private final JPanel fil = new ColonneDefilable();
    private final JScrollPane defilFil = new JScrollPane(fil);
    private final JTextField saisie = new JTextField();
    private final JButton boutonEnvoyer = new Bouton("Envoyer").principal();
    private final JLabel titreFil = new JLabel("Conversation");
    private final JLabel sousTitreFil = Style.aide(" ");
    private final Timer minuteur = new Timer(10_000, e -> rafraichirSilencieusement());
    private int dernierNombre = -1;

    public PanneauMessagerie(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 28, 20, 28));

        // --- colonne de gauche : conversations
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.setCellRenderer(new RenduConversation());
        liste.setFixedCellHeight(64);
        JScrollPane defilListe = Style.defiler(liste);
        JPanel gauche = new JPanel(new BorderLayout());
        gauche.setOpaque(false);
        gauche.add(defilListe, BorderLayout.CENTER);
        gauche.setPreferredSize(new Dimension(260, 100));
        add(gauche, BorderLayout.WEST);

        // --- partie droite : fil + saisie
        titreFil.setFont(Theme.police(15, Font.BOLD));
        titreFil.setForeground(Theme.TEXTE);
        JPanel enTete = new JPanel(new BorderLayout());
        enTete.setBackground(Color.WHITE);
        enTete.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDURE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        enTete.add(titreFil, BorderLayout.CENTER);
        enTete.add(sousTitreFil, BorderLayout.SOUTH);

        fil.setLayout(new BoxLayout(fil, BoxLayout.Y_AXIS));
        fil.setBackground(Theme.FOND);
        fil.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        defilFil.setBorder(null);
        defilFil.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        defilFil.getViewport().setBackground(Theme.FOND);
        defilFil.getVerticalScrollBar().setUnitIncrement(16);

        saisie.putClientProperty("JTextField.placeholderText", "Écrivez un message…");
        saisie.setPreferredSize(new Dimension(100, 38));
        JPanel envoi = new JPanel(new BorderLayout(8, 0));
        envoi.setBackground(Color.WHITE);
        envoi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDURE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        envoi.add(saisie, BorderLayout.CENTER);
        envoi.add(boutonEnvoyer, BorderLayout.EAST);

        JPanel droite = new JPanel(new BorderLayout());
        droite.setBorder(BorderFactory.createLineBorder(Theme.BORDURE));
        droite.add(enTete, BorderLayout.NORTH);
        droite.add(defilFil, BorderLayout.CENTER);
        droite.add(envoi, BorderLayout.SOUTH);
        add(droite, BorderLayout.CENTER);

        liste.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                dernierNombre = -1;
                chargerFil(false);
                majEtat();
            }
        });
        boutonEnvoyer.addActionListener(e -> envoyer());
        saisie.addActionListener(e -> envoyer()); // Entrée envoie
        afficherVide("Aucune conversation pour l'instant.");
        majEtat();
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

    // ---------------------------------------------------------------- données

    @Override
    public void actualiser() {
        Conversation avant = liste.getSelectedValue();
        int idAvant = avant == null ? -1 : avant.demande.getId();
        Taches.executer(this, () -> service.demandesEnvoyees(connecte), (ArrayList<Demande> envoyees) ->
                Taches.executer(this, () -> service.demandesRecues(connecte), (ArrayList<Demande> recues) -> {
                    modeleListe.clear();
                    for (Demande d : envoyees) {
                        if (d.getStatut() == StatutDemande.ACCEPTEE) {
                            modeleListe.addElement(new Conversation(d, Libelles.nom(d.getTuteur()), "Mon tuteur"));
                        }
                    }
                    for (Demande d : recues) {
                        if (d.getStatut() == StatutDemande.ACCEPTEE) {
                            modeleListe.addElement(new Conversation(d, Libelles.nom(d.getTutore()), "Mon tutoré"));
                        }
                    }
                    int aSelectionner = modeleListe.isEmpty() ? -1 : 0;
                    for (int i = 0; i < modeleListe.size(); i++) {
                        if (modeleListe.get(i).demande.getId() == idAvant) aSelectionner = i;
                    }
                    if (aSelectionner >= 0) {
                        liste.setSelectedIndex(aSelectionner);
                    } else {
                        titreFil.setText("Conversation");
                        sousTitreFil.setText(" ");
                        afficherVide("Aucune conversation pour l'instant. La messagerie s'ouvre quand une demande de tutorat est acceptée.");
                    }
                    majEtat();
                }));
    }

    /** silencieux = rafraîchissement automatique : pas de boîte d'erreur, pas de saut de défilement inutile. */
    private void chargerFil(boolean silencieux) {
        Conversation c = liste.getSelectedValue();
        if (c == null) return;
        int id = c.demande.getId();
        if (silencieux) {
            Taches.executerSilencieux(() -> service.consulterConversation(connecte, c.demande), m -> afficher(id, m));
        } else {
            Taches.executer(this, () -> service.consulterConversation(connecte, c.demande), m -> afficher(id, m));
        }
    }

    private void rafraichirSilencieusement() {
        if (isShowing() && liste.getSelectedValue() != null) {
            chargerFil(true);
        }
    }

    private void afficher(int idDemande, ArrayList<Message> messages) {
        Conversation c = liste.getSelectedValue();
        if (c == null || c.demande.getId() != idDemande) return; // l'utilisateur a changé de conversation entre-temps
        if (messages.size() == dernierNombre) return;             // rien de nouveau
        dernierNombre = messages.size();
        titreFil.setText(c.nom);
        sousTitreFil.setText(c.role);

        fil.removeAll();
        if (messages.isEmpty()) {
            afficherVide("Aucun message pour l'instant. Écrivez le premier !");
            return;
        }
        LocalDate jourPrecedent = null;
        for (Message m : messages) {
            LocalDateTime quand = m.getDateEnvoi();
            if (jourPrecedent == null || !jourPrecedent.equals(quand.toLocalDate())) {
                fil.add(separateurJour(quand.toLocalDate()));
                jourPrecedent = quand.toLocalDate();
            }
            boolean moi = Objects.equals(m.getExpediteur(), connecte);
            fil.add(ligneMessage(m.getContenu(), quand.format(HEURE), moi));
            fil.add(javax.swing.Box.createVerticalStrut(6));
        }
        fil.revalidate();
        fil.repaint();
        SwingUtilities.invokeLater(() -> {
            var barre = defilFil.getVerticalScrollBar();
            barre.setValue(barre.getMaximum());
        });
    }

    private void afficherVide(String texte) {
        fil.removeAll();
        JLabel l = new JLabel("<html><body style='width:320px;text-align:center'>" + texte + "</body></html>", JLabel.CENTER);
        l.setFont(Theme.police(13));
        l.setForeground(Theme.TEXTE_DISCRET);
        l.setAlignmentX(CENTER_ALIGNMENT);
        fil.add(javax.swing.Box.createVerticalStrut(60));
        fil.add(l);
        fil.revalidate();
        fil.repaint();
    }

    private void envoyer() {
        Conversation c = liste.getSelectedValue();
        String texte = saisie.getText().trim();
        if (c == null || texte.isEmpty()) return;
        boutonEnvoyer.setEnabled(false);
        Taches.executer(this, () -> service.envoyerMessage(connecte, c.demande, texte), m -> {
            saisie.setText("");
            dernierNombre = -1;
            chargerFil(false);
        }, this::majEtat);
    }

    private void majEtat() {
        boolean choisie = liste.getSelectedValue() != null;
        boutonEnvoyer.setEnabled(choisie);
        saisie.setEnabled(choisie);
    }

    // ------------------------------------------------------------- composants

    private static JComponent separateurJour(LocalDate jour) {
        String texte = jour.equals(LocalDate.now()) ? "Aujourd'hui" : jour.format(JOUR);
        JLabel l = new JLabel(texte, JLabel.CENTER);
        l.setFont(Theme.police(11, Font.BOLD));
        l.setForeground(Theme.TEXTE_DISCRET);
        l.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(l, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private static JComponent ligneMessage(String contenu, String heure, boolean moi) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        ligne.add(new Bulle(contenu, heure, moi), moi ? BorderLayout.EAST : BorderLayout.WEST);
        ligne.setAlignmentX(Component.LEFT_ALIGNMENT);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, ligne.getPreferredSize().height));
        return ligne;
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    /** Bulle arrondie : bleue à droite pour moi, blanche à gauche pour l'autre. */
    private static final class Bulle extends JPanel {
        private final boolean moi;

        Bulle(String contenu, String heure, boolean moi) {
            this.moi = moi;
            setOpaque(false);
            setLayout(new BorderLayout(0, 3));
            setBorder(BorderFactory.createEmptyBorder(9, 14, 7, 14));
            Color texte = moi ? Color.WHITE : Theme.TEXTE;

            JLabel corps = new JLabel("<html>" + html(contenu) + "</html>");
            corps.setFont(Theme.police(13));
            corps.setForeground(texte);
            if (corps.getPreferredSize().width > LARGEUR_MAX_BULLE) {
                corps.setText("<html><body style='width:" + LARGEUR_MAX_BULLE + "px'>" + html(contenu) + "</body></html>");
            }
            JLabel h = new JLabel(heure);
            h.setFont(Theme.police(10));
            h.setForeground(moi ? new Color(0xDBEAFE) : Theme.TEXTE_DISCRET);
            h.setHorizontalAlignment(moi ? JLabel.RIGHT : JLabel.LEFT);
            add(corps, BorderLayout.CENTER);
            add(h, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(moi ? Theme.ACCENT : Color.WHITE);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            if (!moi) {
                g.setColor(Theme.BORDURE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Ligne de la liste : avatar à initiales, nom et rôle. */
    private static final class RenduConversation extends JPanel implements ListCellRenderer<Conversation> {
        private String initiales = "";
        private boolean selection;
        private final JLabel nom = new JLabel();
        private final JLabel role = Style.aide(" ");

        RenduConversation() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 62, 10, 10));
            nom.setFont(Theme.police(13, Font.BOLD));
            nom.setForeground(Theme.TEXTE);
            JPanel textes = new JPanel(new java.awt.GridLayout(2, 1));
            textes.setOpaque(false);
            textes.add(nom);
            textes.add(role);
            add(textes, BorderLayout.CENTER);
            setOpaque(false);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Conversation> l, Conversation c, int i, boolean sel, boolean foc) {
            nom.setText(c.nom);
            role.setText(c.role);
            String[] mots = c.nom.trim().split("\\s+");
            initiales = (mots[0].substring(0, 1) + (mots.length > 1 ? mots[mots.length - 1].substring(0, 1) : "")).toUpperCase();
            selection = sel;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(selection ? Theme.ACCENT_CLAIR : Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Theme.BORDURE);
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            int d = 38;
            int y = (getHeight() - d) / 2;
            g.setColor(selection ? Theme.ACCENT : new Color(0xCBD5E1));
            g.fillOval(14, y, d, d);
            g.setColor(selection ? Color.WHITE : Theme.TEXTE);
            g.setFont(Theme.police(13, Font.BOLD));
            var fm = g.getFontMetrics();
            g.drawString(initiales, 14 + (d - fm.stringWidth(initiales)) / 2, y + (d - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
            super.paintComponent(g0);
        }
    }
}
