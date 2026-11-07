package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import modele.Utilisateur;
import service.GestionTutorat;

/** Administration : indicateurs globaux et tableaux de statistiques (8 méthodes du contrat). */
public class PanneauStatistiques extends JPanel implements Actualisable {

    /** Toutes les statistiques, lues en une seule fois hors du thread Swing. */
    private record Bilan(int tuteurs, int tutores, int seances, Double taux,
                         Map<String, Integer> demandes, Map<String, Double> notes,
                         Map<String, Double> evaluations, Map<String, Integer> absences) { }

    private final GestionTutorat service;
    private final Utilisateur admin;

    private final JLabel kTuteurs = new JLabel("-");
    private final JLabel kTutores = new JLabel("-");
    private final JLabel kSeances = new JLabel("-");
    private final JLabel kTaux = new JLabel("-");

    private final Libelles.ModeleTable mDemandes = new Libelles.ModeleTable("Matière", "Demandes");
    private final Libelles.ModeleTable mNotes = new Libelles.ModeleTable("Tuteur (nom)", "Moyenne des notes");
    private final Libelles.ModeleTable mEvaluations = new Libelles.ModeleTable("Tuteur (nom)", "Moyenne des évaluations");
    private final Libelles.ModeleTable mAbsences = new Libelles.ModeleTable("Tutoré", "Absences");

    public PanneauStatistiques(GestionTutorat service, Utilisateur admin) {
        this.service = service;
        this.admin = admin;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel indicateurs = new JPanel(new GridLayout(1, 4, 10, 0));
        indicateurs.add(carte(kTuteurs, "Tuteurs actifs"));
        indicateurs.add(carte(kTutores, "Tutorés suivis"));
        indicateurs.add(carte(kSeances, "Séances planifiées"));
        indicateurs.add(carte(kTaux, "Taux d'acceptation"));

        JPanel tableaux = new JPanel(new GridLayout(2, 2, 10, 10));
        tableaux.add(bloc("Demandes par matière", mDemandes));
        tableaux.add(bloc("Notes des rapports, par tuteur", mNotes));
        tableaux.add(bloc("Évaluations reçues, par tuteur", mEvaluations));
        tableaux.add(bloc("Absences, par tutoré", mAbsences));

        JButton actualiser = new Bouton("Actualiser");
        actualiser.addActionListener(e -> actualiser());
        JPanel bas = new JPanel(new BorderLayout());
        bas.add(actualiser, BorderLayout.EAST);

        add(indicateurs, BorderLayout.NORTH);
        add(tableaux, BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> new Bilan(
                service.nombreTuteursActifs(admin),
                service.nombreTutoresSuivis(admin),
                service.nombreSeances(admin),
                service.tauxAcceptation(admin),
                service.demandesParMatiere(admin),
                service.moyenneNotesParTuteur(admin),
                service.moyenneEvaluationsParTuteur(admin),
                service.absencesParTutore(admin)), this::afficher);
    }

    private void afficher(Bilan b) {
        kTuteurs.setText(String.valueOf(b.tuteurs()));
        kTutores.setText(String.valueOf(b.tutores()));
        kSeances.setText(String.valueOf(b.seances()));
        kTaux.setText(b.taux() == null ? "n/a" : String.format(Locale.FRANCE, "%.0f %%", b.taux() * 100));
        remplirEntiers(mDemandes, b.demandes());
        remplirDecimaux(mNotes, b.notes());
        remplirDecimaux(mEvaluations, b.evaluations());
        remplirEntiers(mAbsences, b.absences());
    }

    private static void remplirEntiers(Libelles.ModeleTable m, Map<String, Integer> donnees) {
        m.setRowCount(0);
        donnees.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> m.addRow(new Object[] {e.getKey(), e.getValue()}));
    }

    private static void remplirDecimaux(Libelles.ModeleTable m, Map<String, Double> donnees) {
        m.setRowCount(0);
        donnees.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> m.addRow(new Object[] {e.getKey(), String.format(Locale.FRANCE, "%.1f / 5", e.getValue())}));
    }

    private static JPanel carte(JLabel valeur, String libelle) {
        valeur.setFont(valeur.getFont().deriveFont(Font.BOLD, 28f));
        valeur.setHorizontalAlignment(JLabel.CENTER);
        JLabel texte = new JLabel(libelle, JLabel.CENTER);
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 40)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        p.add(valeur, BorderLayout.CENTER);
        p.add(texte, BorderLayout.SOUTH);
        return p;
    }

    private static JPanel bloc(String titre, Libelles.ModeleTable modele) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.add(new JLabel(titre), BorderLayout.NORTH);
        p.add(Style.defiler(Style.table(new JTable(modele))), BorderLayout.CENTER);
        return p;
    }
}
