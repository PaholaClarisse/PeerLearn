package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import modele.EvaluationTuteur;
import modele.Etudiant;
import modele.RapportSuivi;
import modele.Utilisateur;
import service.GestionTutorat;

/**
 * Administration : suivi des tuteurs et retrait d'un tuteur dont les tutorés ne progressent pas.
 * Le contrat n'a pas de méthode « lister les tuteurs » : la liste est construite à partir des
 * rapports et des évaluations, donc seuls les tuteurs qui en ont au moins un y figurent.
 */
public class PanneauTuteurs extends JPanel implements Actualisable {

    private record Donnees(ArrayList<RapportSuivi> rapports, ArrayList<EvaluationTuteur> evaluations) { }

    /** Cumuls d'un tuteur. */
    private static final class Ligne {
        final Etudiant tuteur;
        int nbRapports;
        int sommeRapports;
        int nbEvaluations;
        int sommeEvaluations;

        Ligne(Etudiant tuteur) {
            this.tuteur = tuteur;
        }
    }

    private final GestionTutorat service;
    private final Utilisateur admin;
    private final Libelles.ModeleTable modele = new Libelles.ModeleTable(
            "Tuteur", "Matière", "Statut", "Rapports", "Moy. notes", "Évaluations", "Moy. éval.");
    private final JTable table = Style.table(new JTable(modele));
    private final JButton boutonRetirer = new Bouton("Retirer ce tuteur");
    private ArrayList<Ligne> lignes = new ArrayList<>();

    public PanneauTuteurs(GestionTutorat service, Utilisateur admin) {
        this.service = service;
        this.admin = admin;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JLabel("Tuteurs ayant au moins un rapport ou une évaluation"), BorderLayout.NORTH);
        add(Style.defiler(table), BorderLayout.CENTER);

        JPanel bas = new JPanel(new BorderLayout());
        JLabel aide = new JLabel("Retirer un tuteur termine ses suivis, refuse ses demandes en attente et l'empêche de se réactiver.");
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton actualiser = new Bouton("Actualiser");
        actualiser.addActionListener(e -> actualiser());
        boutons.add(boutonRetirer);
        boutons.add(actualiser);
        bas.add(aide, BorderLayout.NORTH);
        bas.add(boutons, BorderLayout.SOUTH);
        add(bas, BorderLayout.SOUTH);

        boutonRetirer.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> majBouton());
        boutonRetirer.addActionListener(e -> retirer());
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> new Donnees(service.consulterRapports(admin), service.consulterEvaluations(admin)),
                this::afficher);
    }

    private void afficher(Donnees d) {
        LinkedHashMap<Integer, Ligne> parTuteur = new LinkedHashMap<>();
        for (RapportSuivi r : d.rapports()) {
            Etudiant t = r.getSeance().getTuteur();
            Ligne l = parTuteur.computeIfAbsent(t.getId(), k -> new Ligne(t));
            l.nbRapports++;
            l.sommeRapports += r.getNote();
        }
        for (EvaluationTuteur e : d.evaluations()) {
            Etudiant t = e.getDemande().getTuteur();
            Ligne l = parTuteur.computeIfAbsent(t.getId(), k -> new Ligne(t));
            l.nbEvaluations++;
            l.sommeEvaluations += e.getNote();
        }
        lignes = new ArrayList<>(parTuteur.values());
        modele.setRowCount(0);
        for (Ligne l : lignes) {
            modele.addRow(new Object[] {Libelles.nom(l.tuteur), l.tuteur.getMatiere() == null ? "" : l.tuteur.getMatiere(),
                    statut(l.tuteur), l.nbRapports, moyenne(l.sommeRapports, l.nbRapports),
                    l.nbEvaluations, moyenne(l.sommeEvaluations, l.nbEvaluations)});
        }
        majBouton();
    }

    private static String statut(Etudiant t) {
        if (t.isRetire()) return "Retiré";
        return t.isEstTuteurActif() ? "Actif" : "En pause";
    }

    private static String moyenne(int somme, int nombre) {
        return nombre == 0 ? "-" : String.format(Locale.FRANCE, "%.1f / 5", (double) somme / nombre);
    }

    private Ligne selection() {
        int ligne = table.getSelectedRow();
        return ligne >= 0 && ligne < lignes.size() ? lignes.get(ligne) : null;
    }

    private void majBouton() {
        Ligne l = selection();
        boutonRetirer.setEnabled(l != null && !l.tuteur.isRetire());
    }

    private void retirer() {
        Ligne l = selection();
        if (l == null) return;
        if (!Dialogue.confirmer(this, "Retirer un tuteur",
                "Retirer " + Libelles.nom(l.tuteur) + " du dispositif ?\n\n"
                        + "- ses suivis en cours seront terminés (les tutorés sont prévenus) ;\n"
                        + "- ses demandes en attente seront refusées ;\n"
                        + "- il ne pourra plus se réactiver.\n\nCette action est définitive.",
                "Retirer le tuteur", true)) return;
        Etudiant tuteur = l.tuteur;
        Taches.executer(this, () -> { service.retirerTuteur(admin, tuteur); return null; },
                r -> Taches.info(this, Libelles.nom(tuteur) + " a été retiré du dispositif."), this::actualiser);
    }
}
