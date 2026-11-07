package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import modele.Demande;
import modele.Etudiant;
import modele.StatutDemande;
import service.GestionTutorat;

/** Espace tutoré : mes demandes envoyées, annulation et fin de suivi. */
public class PanneauMesDemandes extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final Libelles.ModeleTable modele =
            new Libelles.ModeleTable("Tuteur", "Matière", "Créneau", "Date", "Statut");
    private final JTable table = Style.table(new JTable(modele));
    { table.putClientProperty("messageVide", "Vous n'avez pas encore envoyé de demande. Cherchez un tuteur dans « Trouver un tuteur »."); }
    private final JButton boutonAnnuler = new Bouton("Annuler la demande");
    private final JButton boutonTerminer = new Bouton("Terminer le suivi");
    private final JButton boutonActualiser = new Bouton("Actualiser");
    private ArrayList<Demande> demandes = new ArrayList<>();

    public PanneauMesDemandes(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(Style.defiler(table), BorderLayout.CENTER);

        JPanel bas = new JPanel();
        bas.add(boutonAnnuler);
        bas.add(boutonTerminer);
        bas.add(boutonActualiser);
        add(bas, BorderLayout.SOUTH);

        boutonAnnuler.setEnabled(false);
        boutonTerminer.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> majBoutons());
        boutonActualiser.addActionListener(e -> actualiser());
        boutonAnnuler.addActionListener(e -> annuler());
        boutonTerminer.addActionListener(e -> terminer());
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> service.demandesEnvoyees(connecte), (ArrayList<Demande> liste) -> {
            demandes = liste;
            modele.setRowCount(0);
            for (Demande d : liste) {
                Etudiant t = d.getTuteur();
                String creneau = t.getJour() == null ? "" : Libelles.jour(t.getJour()) + " " + t.getHeure();
                modele.addRow(new Object[] {Libelles.nom(t), d.getMatiere(), creneau,
                        d.getDateDemande(), Libelles.statut(d.getStatut())});
            }
            majBoutons();
        });
    }

    private Demande selection() {
        int ligne = table.getSelectedRow();
        return ligne < 0 ? null : demandes.get(ligne);
    }

    /** Annuler : seulement EN_ATTENTE. Terminer : seulement ACCEPTEE. */
    private void majBoutons() {
        Demande d = selection();
        boutonAnnuler.setEnabled(d != null && d.getStatut() == StatutDemande.EN_ATTENTE);
        boutonTerminer.setEnabled(d != null && d.getStatut() == StatutDemande.ACCEPTEE);
    }

    private void annuler() {
        Demande d = selection();
        if (d == null) return;
        Taches.executer(this, () -> { service.annulerDemande(connecte, d); return null; },
                r -> { }, this::actualiser);
    }

    private void terminer() {
        Demande d = selection();
        if (d == null) return;
        if (!Dialogue.confirmer(this, "Terminer le suivi",
                "Terminer le suivi avec " + Libelles.nom(d.getTuteur()) + " ?\nCette action est définitive et libère sa place.",
                "Terminer le suivi", true)) return;
        Taches.executer(this, () -> { service.terminerSuivi(connecte, d); return null; },
                r -> { }, this::actualiser);
    }
}
