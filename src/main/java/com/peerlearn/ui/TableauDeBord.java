package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import modele.Administrateur;
import modele.Demande;
import modele.Etudiant;
import modele.SeanceTutorat;
import modele.StatutDemande;
import modele.StatutSeance;
import modele.Utilisateur;
import service.GestionTutorat;

/**
 * Page d'accueil de chaque rôle : indicateurs, graphique et raccourcis.
 * N'utilise que des méthodes de lecture du contrat (aucune écriture, notifications non marquées lues).
 */
public class TableauDeBord extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Utilisateur connecte;
    private final Navigation nav;
    /** Colonne qui épouse la largeur de la fenêtre (pas de barre horizontale). */
    private static final class Colonne extends JPanel implements javax.swing.Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return 96; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private final JPanel contenu = new Colonne();

    // --- étudiant (espace tutoré)
    private final Carte kSuivis = new Carte("Tuteurs actuels", "demandes acceptées");
    private final Carte kAttente = new Carte("Demandes en attente", "en cours de réponse").teinte(Theme.ATTENTE);
    private final Carte kNote = new Carte("Moyenne de suivi", "sur 5").teinte(Theme.SUCCES);
    private final Carte kNotifs = new Carte("Notifications", "non lues").teinte(Theme.DANGER_TEXTE);
    private final CourbeNotes courbe = new CourbeNotes();
    private final Libelles.ModeleTable mNotifs = new Libelles.ModeleTable("Dernières notifications");

    // --- tuteur
    private final Carte kTutores = new Carte("Mes tutorés", (String) null);
    private final Carte kDemandes = new Carte("Demandes à traiter", "reçues, en attente").teinte(Theme.ATTENTE);
    private final Carte kSeancesTuteur = new Carte("Séances planifiées", "à venir ou à pointer");
    private final Carte kEval = new Carte("Mon évaluation", "moyenne sur 5").teinte(Theme.SUCCES);
    private final Libelles.ModeleTable mTutores = new Libelles.ModeleTable("Tutoré", "Niveau");
    private final Libelles.ModeleTable mSeances = new Libelles.ModeleTable("Date", "Durée", "État");

    // --- administrateur
    private final Carte kTuteurs = new Carte("Tuteurs actifs", (String) null);
    private final Carte kTutoresAdmin = new Carte("Tutorés suivis", (String) null).teinte(Theme.SUCCES);
    private final Carte kSeancesAdmin = new Carte("Séances planifiées", (String) null).teinte(Theme.ATTENTE);
    private final Carte kTaux = new Carte("Taux d'acceptation", "des demandes").teinte(Theme.ACCENT_FONCE);
    private final Barres barresMatieres = new Barres("Aucune demande pour l'instant.", "");
    private final Barres barresEval = new Barres("Aucune évaluation pour l'instant.", " / 5").echelle(5);
    private final Libelles.ModeleTable mAbsences = new Libelles.ModeleTable("Tutoré", "Absences");

    private record Etudiante(int suivis, int attente, double moyenne, boolean aMoyenne, int nonLues,
                             ArrayList<Integer> notes, List<String> notifs,
                             Integer tutores, Integer demandes, Integer seances, Double eval,
                             List<SeanceTutorat> prochaines, List<Etudiant> eleves) { }

    private record Admin(int tuteurs, int tutores, int seances, Double taux,
                         Map<String, Integer> demandes, Map<String, Double> evals,
                         Map<String, Integer> absences) { }

    public TableauDeBord(GestionTutorat service, Utilisateur connecte, Navigation nav) {
        this.service = service;
        this.connecte = connecte;
        this.nav = nav;
        setLayout(new BorderLayout());
        setOpaque(false);

        contenu.setLayout(new javax.swing.BoxLayout(contenu, javax.swing.BoxLayout.Y_AXIS));
        contenu.setOpaque(false);
        contenu.setBorder(BorderFactory.createEmptyBorder(18, 28, 24, 28));

        String prenom = connecte.getPrenom() == null ? "" : connecte.getPrenom();
        String phrase = connecte instanceof Administrateur ? "Voici l'activité de la plateforme."
                : estTuteur() ? "Voici l'état de vos tutorats."
                : "Voici où en est votre suivi.";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE));
        date = date.substring(0, 1).toUpperCase(Locale.FRANCE) + date.substring(1);
        String salut = connecte instanceof Administrateur ? "Bonjour, administrateur" : "Bonjour " + prenom;
        contenu.add(new Bandeau(salut, phrase, date));
        contenu.add(espace(16));

        if (connecte instanceof Administrateur) {
            construireAdmin();
        } else if (estTuteur()) {
            construireTuteur();
        } else {
            construireTutore();
        }

        JScrollPane defil = new JScrollPane(contenu);
        defil.setBorder(null);
        defil.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        defil.setViewportBorder(null);
        defil.setOpaque(false);
        defil.getViewport().setOpaque(false);
        defil.getVerticalScrollBar().setUnitIncrement(16);
        add(defil, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------ construction

    /** Étudiants L1/L2 : uniquement l'espace tutoré. */
    private void construireTutore() {
        contenu.add(titre("Mon suivi"));
        contenu.add(rangee(kSuivis, kAttente, kNote, kNotifs));
        contenu.add(espace(14));
        JPanel milieu = new JPanel(new GridLayout(1, 2, 14, 0));
        milieu.setOpaque(false);
        milieu.add(new Carte("Ma progression", courbe));
        milieu.add(new Carte("Dernières notifications", tableCompacte(mNotifs)));
        milieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        milieu.setAlignmentX(LEFT_ALIGNMENT);
        contenu.add(milieu);
        contenu.add(espace(14));
        contenu.add(actions(
                action("Trouver un tuteur", "Trouver un tuteur", true),
                action("Écrire à mon tuteur", "Messagerie", false),
                action("Mes exercices", "Exercices", false)));
    }

    /** Étudiants L3 et plus : uniquement l'espace tuteur (un tuteur n'est pas tutoré). */
    private void construireTuteur() {
        contenu.add(titre("Mon activité de tuteur"));
        contenu.add(rangee(kTutores, kDemandes, kSeancesTuteur, kEval));
        contenu.add(espace(14));
        JPanel milieu = new JPanel(new GridLayout(1, 2, 14, 0));
        milieu.setOpaque(false);
        milieu.add(new Carte("Mes tutorés", tableCompacte(mTutores)));
        milieu.add(new Carte("Prochaines séances", tableCompacte(mSeances)));
        milieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        milieu.setAlignmentX(LEFT_ALIGNMENT);
        contenu.add(milieu);
        contenu.add(espace(14));
        JPanel bas = new JPanel(new GridLayout(1, 1));
        bas.setOpaque(false);
        bas.add(new Carte("Dernières notifications", tableCompacte(mNotifs)));
        bas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        bas.setAlignmentX(LEFT_ALIGNMENT);
        contenu.add(bas);
        contenu.add(espace(14));
        contenu.add(actions(
                action("Traiter les demandes", "Demandes reçues", true),
                action("Enregistrer une séance", "Séances", false),
                action("Publier un exercice", "Exercices publiés", false),
                action("Mon profil", "Mon profil tuteur", false)));
    }

    /** Bandeau d'accueil : dégradé bleu, salutation, phrase de contexte et date. */
    private static final class Bandeau extends JPanel {
        private final String date;

        Bandeau(String salut, String phrase, String date) {
            this.date = date;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
            JLabel l1 = new JLabel(salut);
            l1.setFont(Theme.police(24, Font.BOLD));
            l1.setForeground(java.awt.Color.WHITE);
            JLabel l2 = new JLabel(phrase);
            l2.setFont(Theme.police(14));
            l2.setForeground(new java.awt.Color(0xDBEAFE));
            JPanel textes = new JPanel(new GridLayout(2, 1, 0, 2));
            textes.setOpaque(false);
            textes.add(l1);
            textes.add(l2);
            add(textes, BorderLayout.WEST);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
            setPreferredSize(new Dimension(300, 92));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g0) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            java.awt.Shape forme = new java.awt.geom.RoundRectangle2D.Double(0, 0, w, h, 18, 18);
            g.setPaint(new java.awt.GradientPaint(0, 0, new java.awt.Color(0x1E3A8A), w, h, Theme.ACCENT));
            g.fill(forme);
            g.setClip(forme);
            g.setColor(new java.awt.Color(255, 255, 255, 20));
            g.fillOval(w - 150, -70, 220, 220);
            g.fillOval(w - 290, h - 50, 130, 130);
            g.setClip(null);
            g.setFont(Theme.police(13, Font.BOLD));
            g.setColor(new java.awt.Color(255, 255, 255, 210));
            java.awt.FontMetrics fm = g.getFontMetrics();
            g.drawString(date, w - 28 - fm.stringWidth(date), h / 2 + fm.getAscent() / 2 - 2);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    private boolean estTuteur() {
        return connecte instanceof Etudiant e && e.getNiveau().peutTutorer();
    }

    private void construireAdmin() {
        contenu.add(titre("Vue d'ensemble de la plateforme"));
        contenu.add(rangee(kTuteurs, kTutoresAdmin, kSeancesAdmin, kTaux));
        contenu.add(espace(14));
        JPanel milieu = new JPanel(new GridLayout(1, 2, 14, 0));
        milieu.setOpaque(false);
        milieu.add(new Carte("Demandes par matière", barresMatieres));
        milieu.add(new Carte("Évaluation moyenne des tuteurs", barresEval));
        milieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));
        milieu.setAlignmentX(LEFT_ALIGNMENT);
        contenu.add(milieu);
        contenu.add(espace(14));
        JPanel bas = new JPanel(new GridLayout(1, 1));
        bas.setOpaque(false);
        bas.add(new Carte("Tutorés les plus absents", tableCompacte(mAbsences)));
        bas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        bas.setAlignmentX(LEFT_ALIGNMENT);
        contenu.add(bas);
        contenu.add(espace(14));
        contenu.add(actions(
                action("Lire les rapports", "Rapports", true),
                action("Consulter les évaluations", "Évaluations", false),
                action("Gérer les tuteurs", "Tuteurs", false)));
    }

    // ---------------------------------------------------------------- données

    @Override
    public void actualiser() {
        if (connecte instanceof Administrateur) {
            Taches.executer(this, () -> new Admin(
                    service.nombreTuteursActifs(connecte),
                    service.nombreTutoresSuivis(connecte),
                    service.nombreSeances(connecte),
                    service.tauxAcceptation(connecte),
                    service.demandesParMatiere(connecte),
                    service.moyenneEvaluationsParTuteur(connecte),
                    service.absencesParTutore(connecte)), this::afficher);
        } else {
            boolean tuteur = estTuteur();
            Taches.executer(this, () -> lireEtudiant(tuteur), this::afficher);
        }
    }

    private Etudiante lireEtudiant(boolean tuteur) {
        int suivis = 0, attente = 0;
        ArrayList<Integer> notes = new ArrayList<>();
        if (!tuteur) {
            ArrayList<Demande> envoyees = service.demandesEnvoyees(connecte);
            suivis = (int) envoyees.stream().filter(d -> d.getStatut() == StatutDemande.ACCEPTEE).count();
            attente = (int) envoyees.stream().filter(d -> d.getStatut() == StatutDemande.EN_ATTENTE).count();
            notes = service.consulterProgression(connecte);
        }
        double moyenne = notes.stream().mapToInt(Integer::intValue).average().orElse(0);
        int nonLues = service.nombreNotificationsNonLues(connecte);
        ArrayList<String> notifs = service.mesNotifications(connecte);

        Integer tutores = null, demandes = null, seances = null;
        List<Etudiant> eleves = new ArrayList<>();
        Double eval = null;
        List<SeanceTutorat> prochaines = new ArrayList<>();
        if (tuteur) {
            eleves = service.mesTutores(connecte);
            tutores = eleves.size();
            demandes = (int) service.demandesRecues(connecte).stream()
                    .filter(d -> d.getStatut() == StatutDemande.EN_ATTENTE).count();
            ArrayList<SeanceTutorat> toutes = service.mesSeances(connecte);
            LocalDate auj = LocalDate.now();
            prochaines = toutes.stream()
                    .filter(s -> s.getStatut() == StatutSeance.PLANIFIEE && !s.getDateSeance().isBefore(auj))
                    .sorted((a, b) -> a.getDateSeance().compareTo(b.getDateSeance()))
                    .limit(5).toList();
            seances = (int) toutes.stream().filter(s -> s.getStatut() == StatutSeance.PLANIFIEE).count();
            eval = service.consulterMaMoyenne(connecte);
        }
        return new Etudiante(suivis, attente, moyenne, !notes.isEmpty(), nonLues, notes,
                notifs.stream().limit(5).toList(), tutores, demandes, seances, eval, prochaines, eleves);
    }

    private void afficher(Etudiante e) {
        mNotifs.setRowCount(0);
        if (e.notifs().isEmpty()) mNotifs.addRow(new Object[] {"Aucune notification."});
        e.notifs().forEach(n -> mNotifs.addRow(new Object[] {n}));

        if (e.tutores() == null) {
            kSuivis.setValeur(String.valueOf(e.suivis()));
            kAttente.setValeur(String.valueOf(e.attente()));
            kNote.setValeur(e.aMoyenne() ? String.format(Locale.FRANCE, "%.1f", e.moyenne()) : "n/a");
            kNotifs.setValeur(String.valueOf(e.nonLues()));
            courbe.setNotes(e.notes());
            return;
        }
        kTutores.setValeur(e.tutores() + " / " + ((Etudiant) connecte).getCapaciteMax());
        kDemandes.setValeur(String.valueOf(e.demandes()));
        kSeancesTuteur.setValeur(String.valueOf(e.seances()));
        kEval.setValeur(e.eval() == null ? "n/a" : String.format(Locale.FRANCE, "%.1f", e.eval()));
        mTutores.setRowCount(0);
        for (Etudiant t : e.eleves()) mTutores.addRow(new Object[] {Libelles.nom(t), t.getNiveau()});
        if (e.eleves().isEmpty()) mTutores.addRow(new Object[] {"Aucun tutoré pour l'instant", ""});
        mSeances.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRANCE);
        for (SeanceTutorat s : e.prochaines()) {
            mSeances.addRow(new Object[] {s.getDateSeance().format(f), s.getDureeMinutes() + " min", "Planifiée"});
        }
        if (e.prochaines().isEmpty()) mSeances.addRow(new Object[] {"Aucune séance à venir", "", ""});
    }

    private void afficher(Admin a) {
        kTuteurs.setValeur(String.valueOf(a.tuteurs()));
        kTutoresAdmin.setValeur(String.valueOf(a.tutores()));
        kSeancesAdmin.setValeur(String.valueOf(a.seances()));
        kTaux.setValeur(a.taux() == null ? "n/a" : String.format(Locale.FRANCE, "%.0f %%", a.taux() * 100));
        barresMatieres.setDonnees(a.demandes(), 6);
        barresEval.setDonnees(a.evals(), 6);
        mAbsences.setRowCount(0);
        a.absences().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(en -> mAbsences.addRow(new Object[] {en.getKey(), en.getValue()}));
        if (a.absences().isEmpty()) mAbsences.addRow(new Object[] {"Aucune absence enregistrée", ""});
    }

    // ------------------------------------------------------------------ outils

    private JPanel rangee(Carte... cartes) {
        JPanel p = new JPanel(new GridLayout(1, cartes.length, 14, 0));
        p.setOpaque(false);
        for (Carte c : cartes) p.add(c);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private JPanel ligne() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        return p;
    }

    private JLabel titre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Theme.police(14, Font.BOLD));
        l.setForeground(Theme.TEXTE_DISCRET);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private java.awt.Component espace(int h) {
        return javax.swing.Box.createVerticalStrut(h);
    }

    private javax.swing.JComponent tableCompacte(Libelles.ModeleTable m) {
        JTable t = Style.table(new JTable(m));
        t.setRowSelectionAllowed(false);
        JScrollPane sp = Style.defiler(t);
        sp.setPreferredSize(new Dimension(300, 150));
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private Bouton action(String libelle, String destination, boolean principale) {
        Bouton b = new Bouton(libelle);
        if (principale) b.principal();
        b.addActionListener(e -> nav.selectionner(destination));
        return b;
    }

    private JPanel actions(Bouton... boutons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        for (Bouton b : boutons) {
            p.add(b);
            p.add(javax.swing.Box.createHorizontalStrut(10));
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }
}
