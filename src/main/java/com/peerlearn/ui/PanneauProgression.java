package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import modele.Demande;
import modele.Etudiant;
import modele.StatutDemande;
import service.GestionTutorat;

/**
 * Espace tutoré : courbe des notes des rapports (jamais les observations, R8)
 * et évaluation de ses tuteurs.
 */
public class PanneauProgression extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final CourbeNotes courbe = new CourbeNotes();
    private final JLabel resume = new JLabel(" ");
    private final Libelles.ModeleTable modele = new Libelles.ModeleTable("Tuteur", "Matière", "Suivi");
    private final JTable table = Style.table(new JTable(modele));
    private final JButton boutonEvaluer = new Bouton("Évaluer ce tuteur");
    private final JButton boutonActualiser = new Bouton("Actualiser");
    private ArrayList<Demande> evaluables = new ArrayList<>();

    public PanneauProgression(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel haut = new JPanel(new BorderLayout(4, 4));
        haut.add(new JLabel("Ma progression (notes de mes rapports de suivi)"), BorderLayout.NORTH);
        haut.add(courbe, BorderLayout.CENTER);
        haut.add(resume, BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel bas = new JPanel(new BorderLayout(4, 4));
        bas.add(new JLabel("Évaluer un tuteur (suivis acceptés ou terminés)"), BorderLayout.NORTH);
        bas.add(Style.defiler(table), BorderLayout.CENTER);
        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.add(boutonEvaluer);
        boutons.add(boutonActualiser);
        bas.add(boutons, BorderLayout.SOUTH);

        JSplitPane coupe = new JSplitPane(JSplitPane.VERTICAL_SPLIT, haut, bas);
        coupe.setResizeWeight(0.55);
        coupe.setBorder(null);
        add(coupe, BorderLayout.CENTER);

        boutonEvaluer.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> boutonEvaluer.setEnabled(table.getSelectedRow() >= 0));
        boutonActualiser.addActionListener(e -> actualiser());
        boutonEvaluer.addActionListener(e -> evaluer());
    }

    @Override
    public void actualiser() {
        Taches.executer(this, () -> service.consulterProgression(connecte), (ArrayList<Integer> notes) -> {
            courbe.setNotes(notes);
            if (notes.isEmpty()) {
                resume.setText("Aucun rapport pour l'instant.");
            } else {
                double moyenne = notes.stream().mapToInt(Integer::intValue).average().orElse(0);
                resume.setText(String.format(Locale.FRANCE, "Moyenne : %.1f / 5 sur %d rapport(s)", moyenne, notes.size()));
            }
        });
        Taches.executer(this, () -> service.demandesEnvoyees(connecte), (ArrayList<Demande> liste) -> {
            evaluables = new ArrayList<>();
            modele.setRowCount(0);
            for (Demande d : liste) {
                if (d.getStatut() != StatutDemande.ACCEPTEE && d.getStatut() != StatutDemande.TERMINEE) continue;
                evaluables.add(d);
                modele.addRow(new Object[] {Libelles.nom(d.getTuteur()), d.getMatiere(), Libelles.statut(d.getStatut())});
            }
        });
    }

    private void evaluer() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) return;
        Demande d = evaluables.get(ligne);

        JSpinner choixNote = new JSpinner(new SpinnerNumberModel(3, 0, 5, 1));
        JTextArea commentaire = new JTextArea(4, 30);
        commentaire.setLineWrap(true);
        commentaire.setWrapStyleWord(true);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        JPanel note = new JPanel(new FlowLayout(FlowLayout.LEFT));
        note.add(new JLabel("Note (0 à 5)"));
        note.add(choixNote);
        p.add(note, BorderLayout.NORTH);
        p.add(Style.defiler(commentaire), BorderLayout.CENTER);
        p.add(new JLabel("Le tuteur ne verra que sa moyenne, jamais votre commentaire. Une seule évaluation par suivi."),
                BorderLayout.SOUTH);

        if (!Dialogue.formulaire(this, "Évaluer " + Libelles.nom(d.getTuteur()), p, "Envoyer l'évaluation")) return;
        int n = (Integer) choixNote.getValue();
        String texte = commentaire.getText().trim();
        Taches.executer(this, () -> service.evaluerTuteur(connecte, d, n, texte),
                r -> Taches.info(this, "Merci, votre évaluation est enregistrée."));
    }
}
