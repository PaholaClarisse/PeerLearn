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
import modele.EvaluationTuteur;
import modele.Utilisateur;
import service.GestionTutorat;

/** Administration : évaluations des tuteurs par leurs tutorés, commentaires compris. */
public class PanneauEvaluations extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Utilisateur admin;
    private final Libelles.ModeleTable modele =
            new Libelles.ModeleTable("Date", "Tuteur", "Évalué par", "Matière", "Note");
    private final JTable table = Style.table(new JTable(modele));
    { table.putClientProperty("messageVide", "Aucune évaluation n'a encore été déposée."); }
    private final JTextArea details = new JTextArea();
    private ArrayList<EvaluationTuteur> evaluations = new ArrayList<>();

    public PanneauEvaluations(GestionTutorat service, Utilisateur admin) {
        this.service = service;
        this.admin = admin;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);

        JPanel haut = new JPanel(new BorderLayout(0, 4));
        haut.add(new JLabel("Évaluations des tuteurs (du plus récent au plus ancien)"), BorderLayout.NORTH);
        haut.add(Style.defiler(table), BorderLayout.CENTER);
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton actualiser = new Bouton("Actualiser");
        actualiser.addActionListener(e -> actualiser());
        boutons.add(actualiser);
        haut.add(boutons, BorderLayout.SOUTH);

        JPanel bas = new JPanel(new BorderLayout(0, 4));
        bas.add(new JLabel("Commentaire du tutoré (évaluation sélectionnée)"), BorderLayout.NORTH);
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
        Taches.executer(this, () -> service.consulterEvaluations(admin), (ArrayList<EvaluationTuteur> liste) -> {
            evaluations = new ArrayList<>(liste);
            Collections.reverse(evaluations);
            modele.setRowCount(0);
            for (EvaluationTuteur e : evaluations) {
                modele.addRow(new Object[] {e.getDateEvaluation(), Libelles.nom(e.getDemande().getTuteur()),
                        Libelles.nom(e.getDemande().getTutore()), e.getDemande().getMatiere(), e.getNote() + " / 5"});
            }
            details.setText("");
        });
    }

    private void afficherDetails() {
        int ligne = table.getSelectedRow();
        if (ligne < 0 || ligne >= evaluations.size()) {
            details.setText("");
            return;
        }
        String c = evaluations.get(ligne).getCommentaire();
        details.setText(c == null || c.isBlank() ? "(aucun commentaire)" : c);
        details.setCaretPosition(0);
    }
}
