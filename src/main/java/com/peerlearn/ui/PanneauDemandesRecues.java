package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import modele.Demande;
import modele.Etudiant;
import modele.StatutDemande;
import service.GestionTutorat;

/** Espace tuteur : demandes reçues (accepter / refuser) et liste de mes tutorés. */
public class PanneauDemandesRecues extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final Libelles.ModeleTable modeleDemandes =
            new Libelles.ModeleTable("Tutoré", "Matière", "Date", "Statut");
    private final Libelles.ModeleTable modeleTutores =
            new Libelles.ModeleTable("Prénom", "Nom", "Niveau");
    private final JTable tableDemandes = Style.table(new JTable(modeleDemandes));
    { tableDemandes.putClientProperty("messageVide", "Aucune demande reçue pour l'instant."); }
    private final JTable tableTutores = Style.table(new JTable(modeleTutores));
    private final JCheckBox seulementEnAttente = new JCheckBox("Seulement les demandes en attente", true);
    private final JButton boutonAccepter = new Bouton("Accepter");
    private final JButton boutonRefuser = new Bouton("Refuser");
    private final JButton boutonActualiser = new Bouton("Actualiser");
    private ArrayList<Demande> toutes = new ArrayList<>();
    private ArrayList<Demande> affichees = new ArrayList<>();

    public PanneauDemandesRecues(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tableDemandes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel haut = new JPanel(new BorderLayout());
        haut.add(seulementEnAttente, BorderLayout.WEST);
        JPanel boutons = new JPanel();
        boutons.add(boutonAccepter);
        boutons.add(boutonRefuser);
        boutons.add(boutonActualiser);
        haut.add(boutons, BorderLayout.EAST);

        JPanel centre = new JPanel(new GridLayout(2, 1, 8, 8));
        JPanel blocDemandes = new JPanel(new BorderLayout());
        blocDemandes.add(new JLabel("Demandes reçues"), BorderLayout.NORTH);
        blocDemandes.add(Style.defiler(tableDemandes), BorderLayout.CENTER);
        JPanel blocTutores = new JPanel(new BorderLayout());
        blocTutores.add(new JLabel("Mes tutorés"), BorderLayout.NORTH);
        blocTutores.add(Style.defiler(tableTutores), BorderLayout.CENTER);
        centre.add(blocDemandes);
        centre.add(blocTutores);

        add(haut, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);

        seulementEnAttente.addActionListener(e -> afficher());
        boutonActualiser.addActionListener(e -> actualiser());
        boutonAccepter.addActionListener(e -> accepter());
        boutonRefuser.addActionListener(e -> refuser());
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> service.demandesRecues(connecte), (ArrayList<Demande> liste) -> {
            toutes = liste;
            afficher();
        });
        Taches.executer(this, () -> service.mesTutores(connecte), (ArrayList<Etudiant> t) -> {
            modeleTutores.setRowCount(0);
            for (Etudiant e : t) {
                modeleTutores.addRow(new Object[] {e.getPrenom(), e.getNom(), e.getNiveau()});
            }
        });
    }

    private void afficher() {
        affichees = new ArrayList<>();
        modeleDemandes.setRowCount(0);
        for (Demande d : toutes) {
            if (seulementEnAttente.isSelected() && d.getStatut() != StatutDemande.EN_ATTENTE) continue;
            affichees.add(d);
            modeleDemandes.addRow(new Object[] {Libelles.nom(d.getTutore()), d.getMatiere(),
                    d.getDateDemande(), Libelles.statut(d.getStatut())});
        }
    }

    private Demande selection() {
        int ligne = tableDemandes.getSelectedRow();
        if (ligne < 0) {
            Taches.erreur(this, "Sélectionnez d'abord une demande.");
            return null;
        }
        return affichees.get(ligne);
    }

    private void accepter() {
        Demande d = selection();
        if (d == null) return;
        // validerDemande : transaction avec verrou (R3 : trois tutorés au maximum)
        Taches.executer(this, () -> { service.validerDemande(connecte, d); return null; },
                r -> Taches.info(this, "Demande acceptée. " + Libelles.nom(d.getTutore()) + " est maintenant votre tutoré."),
                this::actualiser);
    }

    private void refuser() {
        Demande d = selection();
        if (d == null) return;
        Taches.executer(this, () -> { service.refuserDemande(connecte, d); return null; },
                r -> { }, this::actualiser);
    }
}
