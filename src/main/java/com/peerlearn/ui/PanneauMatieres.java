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
import javax.swing.JTextField;
import modele.Matiere;
import modele.Utilisateur;
import service.GestionTutorat;

/** Administration : liste des matières et ajout d'une nouvelle matière. */
public class PanneauMatieres extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Utilisateur admin;
    private final Libelles.ModeleTable modele = new Libelles.ModeleTable("Matière");
    private final JTextField champNom = new JTextField(24);
    private final JButton boutonAjouter = new Bouton("Ajouter");

    public PanneauMatieres(GestionTutorat service, Utilisateur admin) {
        this.service = service;
        this.admin = admin;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new JLabel("Matières proposées aux étudiants"), BorderLayout.NORTH);
        add(Style.defiler(Style.table(new JTable(modele))), BorderLayout.CENTER);

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bas.add(new JLabel("Nouvelle matière"));
        bas.add(champNom);
        bas.add(boutonAjouter);
        add(bas, BorderLayout.SOUTH);

        boutonAjouter.addActionListener(e -> ajouter());
        champNom.addActionListener(e -> ajouter()); // Entrée valide
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> service.listerMatieres(admin), (ArrayList<Matiere> liste) -> {
            modele.setRowCount(0);
            for (Matiere m : liste) {
                modele.addRow(new Object[] {m.getNom()});
            }
        });
    }

    private void ajouter() {
        String nom = champNom.getText().trim();
        if (nom.isEmpty()) {
            Taches.erreur(this, "Saisissez le nom de la matière.");
            return;
        }
        Taches.executer(this, () -> service.ajouterMatiere(admin, nom), m -> champNom.setText(""), this::actualiser);
    }
}
