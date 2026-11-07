package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import modele.Etudiant;
import modele.Matiere;
import service.GestionTutorat;

/**
 * Espace tuteur : choisir la matière et le créneau, puis activer ou mettre en pause.
 * Le contrôleur relit la base avant chaque action ; ici on garde seulement ce qui
 * est affiché (état initial lu dans l'Etudiant renvoyé à la connexion).
 */
public class PanneauProfilTuteur extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final JComboBox<Matiere> choixMatiere = new JComboBox<>();
    private final JComboBox<DayOfWeek> choixJour = new JComboBox<>(DayOfWeek.values());
    private final JSpinner choixHeure = new JSpinner(new SpinnerDateModel());
    private final JButton boutonEnregistrer = new Bouton("Enregistrer le profil");
    private final JButton boutonActiver = new Bouton("Activer mon profil tuteur");
    private final JButton boutonPause = new Bouton("Mettre en pause");
    private final Pastille etat = new Pastille();
    private final JLabel aideEtat = Style.aide(" ");
    private final Jauge jauge = new Jauge();
    private final JLabel places = Style.aide(" ");
    private final JLabel moyenne = new JLabel("-");
    private final JLabel aideMoyenne = Style.aide("Moyenne des évaluations reçues");
    private boolean verrouille;

    // État affiché (initialisé depuis l'objet reçu à la connexion)
    private boolean actif;
    private boolean retire;

    public PanneauProfilTuteur(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        this.actif = connecte.isEstTuteurActif();
        this.retire = connecte.isRetire();

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 28, 24, 28));

        choixJour.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, Libelles.jour((DayOfWeek) v), i, s, f);
            }
        });
        choixHeure.setEditor(new JSpinner.DateEditor(choixHeure, "HH:mm"));
        if (connecte.getJour() != null) choixJour.setSelectedItem(connecte.getJour());
        choixHeure.setValue(versDate(connecte.getHeure() != null ? connecte.getHeure() : LocalTime.of(14, 0)));
        for (JComponent champ : new JComponent[] {choixMatiere, choixJour, choixHeure}) {
            champ.setPreferredSize(new Dimension(220, 34));
        }
        ((Bouton) boutonEnregistrer).principal();
        moyenne.setFont(Theme.police(30, Font.BOLD));
        moyenne.setForeground(Theme.SUCCES);

        JPanel colonnes = new JPanel(new GridLayout(1, 2, 14, 0));
        colonnes.setOpaque(false);
        colonnes.add(carteDisponibilites());
        colonnes.add(carteStatut());

        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(colonnes, BorderLayout.NORTH);
        add(haut, BorderLayout.CENTER);

        boutonEnregistrer.addActionListener(e -> enregistrer());
        boutonActiver.addActionListener(e -> activer());
        boutonPause.addActionListener(e -> pause());
        majAffichage();
        chargerMatieres();
    }

    private JPanel carteDisponibilites() {
        JPanel f = new JPanel(new java.awt.GridBagLayout());
        f.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        c.gridy = y++; c.insets = new Insets(0, 0, 14, 0);
        f.add(Style.aide("Quand et sur quelle matière souhaitez-vous aider ?"), c);
        String[] noms = {"Matière", "Jour de la semaine", "Heure"};
        JComponent[] champs = {choixMatiere, choixJour, choixHeure};
        for (int i = 0; i < noms.length; i++) {
            JLabel l = new JLabel(noms[i]);
            l.setFont(Theme.police(12, Font.BOLD));
            l.setForeground(Theme.TEXTE_DISCRET);
            c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
            f.add(l, c);
            c.gridy = y++; c.insets = new Insets(0, 0, 12, 0);
            f.add(champs[i], c);
        }
        c.gridy = y++; c.insets = new Insets(6, 0, 0, 0); c.fill = GridBagConstraints.NONE;
        f.add(boutonEnregistrer, c);
        return new Carte("Mes disponibilités", f);
    }

    private JPanel carteStatut() {
        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0); c.fill = GridBagConstraints.NONE;
        f.add(etat, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0);
        f.add(aideEtat, c);

        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
        f.add(etiquette("Tutorés actuels"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
        f.add(jauge, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0);
        f.add(places, c);

        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0);
        f.add(etiquette("Mon évaluation"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0);
        f.add(moyenne, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0);
        f.add(aideMoyenne, c);

        JPanel boutons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        boutons.setOpaque(false);
        boutons.add(boutonActiver);
        boutons.add(javax.swing.Box.createHorizontalStrut(10));
        boutons.add(boutonPause);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0);
        f.add(boutons, c);
        return new Carte("Mon statut", f);
    }

    private static JLabel etiquette(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Theme.police(12, Font.BOLD));
        l.setForeground(Theme.TEXTE_DISCRET);
        return l;
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> service.mesTutores(connecte), (ArrayList<Etudiant> t) ->
        {
            jauge.set(t.size(), connecte.getCapaciteMax());
            places.setText(t.size() + " sur " + connecte.getCapaciteMax() + " places occupées"
                    + (t.isEmpty() ? "" : " (matière et créneau verrouillés)"));
        });
        // le tuteur ne voit que sa moyenne, jamais les commentaires (R8)
        Taches.executer(this, () -> service.consulterMaMoyenne(connecte), (Double m) ->
        {
            moyenne.setText(m == null ? "n/a" : String.format(java.util.Locale.FRANCE, "%.1f / 5", m));
            aideMoyenne.setText(m == null ? "Aucune évaluation reçue pour l'instant" : "Moyenne des évaluations reçues");
        });
    }

    private void chargerMatieres() {
        Taches.executer(this, () -> service.listerMatieres(connecte), (ArrayList<Matiere> m) -> {
            choixMatiere.setModel(new DefaultComboBoxModel<>(m.toArray(new Matiere[0])));
            if (connecte.getMatiere() != null) choixMatiere.setSelectedItem(connecte.getMatiere());
        });
    }

    private void enregistrer() {
        Matiere m = (Matiere) choixMatiere.getSelectedItem();
        DayOfWeek j = (DayOfWeek) choixJour.getSelectedItem();
        LocalTime h = versHeure((Date) choixHeure.getValue());
        if (m == null) {
            Taches.erreur(this, "Choisissez une matière.");
            return;
        }
        Taches.executer(this, () -> { service.definirProfilTuteur(connecte, m, j, h); return null; },
                r -> Taches.info(this, "Profil enregistré. Vous pouvez maintenant l'activer."));
    }

    private void activer() {
        Taches.executer(this, () -> { service.activerTuteur(connecte); return null; }, r -> {
            actif = true;
            majAffichage();
            Taches.info(this, "Votre profil est actif : les tutorés peuvent vous trouver.");
        });
    }

    private void pause() {
        Taches.executer(this, () -> { service.mettreEnPause(connecte); return null; }, r -> {
            actif = false;
            majAffichage();
        });
    }

    private void majAffichage() {
        if (retire) {
            etat.set("Retiré", Theme.DANGER_TEXTE);
            aideEtat.setText("Retiré par l'administration : réactivation impossible.");
        } else if (actif) {
            etat.set("Actif", Theme.SUCCES);
            aideEtat.setText("Votre profil est visible dans les recherches des tutorés.");
        } else {
            etat.set("En pause", Theme.ATTENTE);
            aideEtat.setText("Votre profil est invisible dans les recherches.");
        }
        boutonActiver.setEnabled(!retire && !actif);
        boutonPause.setEnabled(actif);
    }

    /** Pastille colorée avec le statut. */
    private static final class Pastille extends JLabel {
        private Color couleur = Theme.TEXTE_DISCRET;

        Pastille() {
            setFont(Theme.police(13, Font.BOLD));
            setBorder(BorderFactory.createEmptyBorder(5, 28, 5, 16));
            setForeground(Color.WHITE);
        }

        void set(String texte, Color c) {
            setText(texte);
            couleur = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 28));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.setColor(couleur);
            g.fillOval(12, getHeight() / 2 - 4, 8, 8);
            g.dispose();
            setForeground(couleur);
            super.paintComponent(g0);
        }
    }

    /** Barre de remplissage : tutorés actuels sur capacité. */
    private static final class Jauge extends JComponent {
        private int n;
        private int max = 1;

        Jauge() {
            setPreferredSize(new Dimension(200, 12));
        }

        void set(int n, int max) {
            this.n = n;
            this.max = Math.max(1, max);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = 10;
            int y = (getHeight() - h) / 2;
            g.setColor(new Color(0xE2E8F0));
            g.fillRoundRect(0, y, getWidth(), h, h, h);
            double part = Math.min(1.0, (double) n / max);
            g.setColor(part >= 1.0 ? Theme.ATTENTE : Theme.ACCENT);
            if (part > 0) g.fillRoundRect(0, y, Math.max(h, (int) (getWidth() * part)), h, h, h);
            g.dispose();
        }
    }

    private static Date versDate(LocalTime h) {
        return Date.from(LocalDate.now().atTime(h).atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalTime versHeure(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }
}
