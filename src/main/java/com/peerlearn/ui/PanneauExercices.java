package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import modele.Etudiant;
import modele.Exercice;
import service.GestionTutorat;

/** Espace tutoré : exercices publiés par mes tuteurs, et dépôt de mon rendu (un seul par exercice). */
public class PanneauExercices extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final Libelles.ModeleTable modele =
            new Libelles.ModeleTable("Tuteur", "Titre", "Type", "Publié le", "Fichier");
    private final JTable table = Style.table(new JTable(modele));
    { table.putClientProperty("messageVide", "Votre tuteur n'a publié aucun exercice pour l'instant."); }
    private final JButton boutonOuvrir = new Bouton("Ouvrir l'exercice");
    private final JButton boutonRendre = new Bouton("Déposer mon rendu");
    private ArrayList<Exercice> exercices = new ArrayList<>();

    public PanneauExercices(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JLabel("Exercices de mes tuteurs (suivis acceptés)"), BorderLayout.NORTH);
        add(Style.defiler(table), BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton actualiser = new Bouton("Actualiser");
        bas.add(boutonOuvrir);
        bas.add(boutonRendre);
        bas.add(actualiser);
        add(bas, BorderLayout.SOUTH);

        boutonOuvrir.setEnabled(false);
        boutonRendre.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean choisi = table.getSelectedRow() >= 0;
            boutonOuvrir.setEnabled(choisi);
            boutonRendre.setEnabled(choisi);
        });
        actualiser.addActionListener(e -> actualiser());
        boutonOuvrir.addActionListener(e -> {
            Exercice x = selection();
            if (x != null) Fichiers.ouvrir(this, x.getChemin());
        });
        boutonRendre.addActionListener(e -> rendre());
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> service.consulterExercices(connecte), (ArrayList<Exercice> liste) -> {
            exercices = new ArrayList<>(liste);
            exercices.sort((a, b) -> b.getDatePublication().compareTo(a.getDatePublication()));
            modele.setRowCount(0);
            for (Exercice x : exercices) {
                modele.addRow(new Object[] {Libelles.nom(x.getTuteur()), x.getTitre(), x.getType(),
                        x.getDatePublication(), Fichiers.nom(x.getChemin())});
            }
        });
    }

    private Exercice selection() {
        int ligne = table.getSelectedRow();
        return ligne >= 0 && ligne < exercices.size() ? exercices.get(ligne) : null;
    }

    private void rendre() {
        Exercice x = selection();
        if (x == null) return;
        String chemin = Fichiers.choisir(this);
        if (chemin == null) return;
        Taches.executer(this, () -> service.deposerRendu(connecte, x, chemin),
                r -> Taches.info(this, "Votre rendu pour « " + x.getTitre() + " » est déposé. Votre tuteur est prévenu."));
    }
}
