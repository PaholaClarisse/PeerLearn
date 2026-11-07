package com.peerlearn.ui;

import modele.Etudiant;
import modele.Matiere;
import service.GestionTutorat;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Parcours tutoré : choisir une matière, voir les tuteurs, envoyer une demande. */
public class PanneauRechercheTuteur extends JPanel {

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final JComboBox<Matiere> choixMatiere = new JComboBox<>();
    private final JButton boutonChercher = new Bouton("Rechercher");
    private final JButton boutonDemande = new Bouton("Envoyer une demande");
    private final DefaultTableModel modeleTable =
            new DefaultTableModel(new String[] {"Prénom", "Nom"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable table = Style.table(new JTable(modeleTable));
    { table.putClientProperty("messageVide", "Choisissez une matière puis cliquez sur Rechercher pour voir les tuteurs disponibles."); }
    private ArrayList<Etudiant> tuteursAffiches = new ArrayList<>();

    public PanneauRechercheTuteur(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout(8, 12));
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 28, 12, 28));

        JPanel haut = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        haut.setOpaque(false);
        choixMatiere.setPreferredSize(new java.awt.Dimension(240, 34));
        haut.add(new JLabel("Matière"));
        haut.add(choixMatiere);
        haut.add(boutonChercher);
        add(haut, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(Style.defiler(table), BorderLayout.CENTER);

        JPanel bas = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        bas.setOpaque(false);
        ((Bouton) boutonDemande).principal();
        bas.add(boutonDemande);
        add(bas, BorderLayout.SOUTH);

        boutonChercher.addActionListener(e -> chercher());
        boutonDemande.addActionListener(e -> envoyerDemande());
        chargerMatieres();
    }

    private void chargerMatieres() {
        Taches.executer(this, () -> service.listerMatieres(connecte), (ArrayList<Matiere> matieres) ->
                choixMatiere.setModel(new DefaultComboBoxModel<>(matieres.toArray(new Matiere[0]))));
    }

    private void chercher() {
        Matiere m = (Matiere) choixMatiere.getSelectedItem();
        if (m == null) return;
        Taches.executer(this, () -> service.rechercherTuteurs(connecte, m), (ArrayList<Etudiant> tuteurs) -> {
            tuteursAffiches = tuteurs;
            modeleTable.setRowCount(0);
            for (Etudiant t : tuteurs) {
                modeleTable.addRow(new Object[] {t.getPrenom(), t.getNom()});
            }
        });
    }

    private void envoyerDemande() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            Taches.erreur(this, "Sélectionnez d'abord un tuteur.");
            return;
        }
        Etudiant tuteur = tuteursAffiches.get(ligne);
        Taches.executer(this, () -> service.envoyerDemande(connecte, tuteur),
                demande -> Taches.info(this, "Demande envoyée."));
    }
}
