package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import modele.RapportSuivi;
import modele.Utilisateur;
import service.GestionTutorat;

/** Administration : rapports de suivi complets, observations comprises (R8). */
public class PanneauRapports extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Utilisateur admin;
    private final Libelles.ModeleTable modele =
            new Libelles.ModeleTable("Date", "Tuteur", "Tutoré", "Séance", "Note");
    private final JTable table = Style.table(new JTable(modele));
    { table.putClientProperty("messageVide", "Aucun rapport de suivi n'a encore été rédigé."); }
    private final JTextArea details = new JTextArea();
    private ArrayList<RapportSuivi> rapports = new ArrayList<>();

    public PanneauRapports(GestionTutorat service, Utilisateur admin) {
        this.service = service;
        this.admin = admin;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);

        JPanel haut = new JPanel(new BorderLayout(0, 4));
        haut.add(new JLabel("Rapports de suivi (du plus récent au plus ancien)"), BorderLayout.NORTH);
        haut.add(Style.defiler(table), BorderLayout.CENTER);
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton actualiser = new Bouton("Actualiser");
        actualiser.addActionListener(e -> actualiser());
        boutons.add(actualiser);
        haut.add(boutons, BorderLayout.SOUTH);

        JPanel bas = new JPanel(new BorderLayout(0, 4));
        bas.add(new JLabel("Observations du tuteur (rapport sélectionné)"), BorderLayout.NORTH);
        bas.add(Style.defiler(details), BorderLayout.CENTER);

        haut.setPreferredSize(new java.awt.Dimension(400, 300));
        bas.setPreferredSize(new java.awt.Dimension(400, 180));
        JSplitPane coupe = new JSplitPane(JSplitPane.VERTICAL_SPLIT, haut, bas);
        coupe.setResizeWeight(0.5);
        coupe.setBorder(null);
        add(coupe, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) afficherDetails();
        });
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> service.consulterRapports(admin), (ArrayList<RapportSuivi> liste) -> {
            rapports = new ArrayList<>(liste);
            Collections.reverse(rapports); // plus récents d'abord
            modele.setRowCount(0);
            for (RapportSuivi r : rapports) {
                modele.addRow(new Object[] {r.getDateRapport(), Libelles.nom(r.getSeance().getTuteur()),
                        Libelles.nom(r.getTutore()), r.getSeance().getDateSeance(), r.getNote() + " / 5"});
            }
            details.setText("");
        });
    }

    private void afficherDetails() {
        int ligne = table.getSelectedRow();
        if (ligne < 0 || ligne >= rapports.size()) {
            details.setText("");
            return;
        }
        String obs = rapports.get(ligne).getObservations();
        details.setText(obs == null || obs.isBlank() ? "(aucune observation)" : obs);
        details.setCaretPosition(0);
    }
}
