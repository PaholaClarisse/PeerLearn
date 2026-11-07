package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import modele.Etudiant;
import modele.Exercice;
import modele.Rendu;
import service.GestionTutorat;

/** Espace tuteur : publier un exercice (PDF, DOC, DOCX) et consulter les rendus de mes tutorés. */
public class PanneauExercicesPublies extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Etudiant connecte;
    private final Libelles.ModeleTable modeleExercices =
            new Libelles.ModeleTable("Titre", "Type", "Publié le", "Fichier");
    private final Libelles.ModeleTable modeleRendus =
            new Libelles.ModeleTable("Tutoré", "Déposé le", "Type", "Fichier");
    private final JTable tableExercices = Style.table(new JTable(modeleExercices));
    { tableExercices.putClientProperty("messageVide", "Vous n'avez publié aucun exercice. Utilisez « Publier un exercice »."); }
    private final JTable tableRendus = Style.table(new JTable(modeleRendus));
    private final JButton boutonPublier = new Bouton("Publier un exercice");
    private final JButton boutonOuvrir = new Bouton("Ouvrir l'exercice");
    private final JButton boutonOuvrirRendu = new Bouton("Ouvrir le rendu");
    private ArrayList<Exercice> exercices = new ArrayList<>();
    private ArrayList<Rendu> rendus = new ArrayList<>();

    public PanneauExercicesPublies(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tableExercices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableRendus.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel haut = new JPanel(new BorderLayout(0, 4));
        haut.add(new JLabel("Mes exercices publiés"), BorderLayout.NORTH);
        haut.add(Style.defiler(tableExercices), BorderLayout.CENTER);
        JPanel boutonsHaut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton actualiser = new Bouton("Actualiser");
        boutonsHaut.add(boutonPublier);
        boutonsHaut.add(boutonOuvrir);
        boutonsHaut.add(actualiser);
        haut.add(boutonsHaut, BorderLayout.SOUTH);

        JPanel bas = new JPanel(new BorderLayout(0, 4));
        bas.add(new JLabel("Rendus de l'exercice sélectionné"), BorderLayout.NORTH);
        bas.add(Style.defiler(tableRendus), BorderLayout.CENTER);
        JPanel boutonsBas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutonsBas.add(boutonOuvrirRendu);
        bas.add(boutonsBas, BorderLayout.SOUTH);

        haut.setPreferredSize(new java.awt.Dimension(400, 260));
        bas.setPreferredSize(new java.awt.Dimension(400, 220));
        JSplitPane coupe = new JSplitPane(JSplitPane.VERTICAL_SPLIT, haut, bas);
        coupe.setResizeWeight(0.5);
        coupe.setBorder(null);
        add(coupe, BorderLayout.CENTER);

        boutonOuvrir.setEnabled(false);
        boutonOuvrirRendu.setEnabled(false);
        tableExercices.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            boutonOuvrir.setEnabled(tableExercices.getSelectedRow() >= 0);
            chargerRendus();
        });
        tableRendus.getSelectionModel().addListSelectionListener(e ->
                boutonOuvrirRendu.setEnabled(tableRendus.getSelectedRow() >= 0));

        actualiser.addActionListener(e -> actualiser());
        boutonPublier.addActionListener(e -> publier());
        boutonOuvrir.addActionListener(e -> {
            Exercice x = exerciceSelectionne();
            if (x != null) Fichiers.ouvrir(this, x.getChemin());
        });
        boutonOuvrirRendu.addActionListener(e -> {
            int ligne = tableRendus.getSelectedRow();
            if (ligne >= 0 && ligne < rendus.size()) Fichiers.ouvrir(this, rendus.get(ligne).getChemin());
        });
    }

    @Override
    public void actualiser() {
        Exercice avant = exerciceSelectionne();
        int idAvant = avant == null ? -1 : avant.getId();
        Taches.executer(this, () -> service.mesExercices(connecte), (ArrayList<Exercice> liste) -> {
            exercices = new ArrayList<>(liste);
            exercices.sort((a, b) -> b.getDatePublication().compareTo(a.getDatePublication()));
            modeleExercices.setRowCount(0);
            int aSelectionner = -1;
            for (int i = 0; i < exercices.size(); i++) {
                Exercice x = exercices.get(i);
                modeleExercices.addRow(new Object[] {x.getTitre(), x.getType(), x.getDatePublication(),
                        Fichiers.nom(x.getChemin())});
                if (x.getId() == idAvant) aSelectionner = i;
            }
            if (aSelectionner >= 0) {
                tableExercices.setRowSelectionInterval(aSelectionner, aSelectionner);
            } else {
                modeleRendus.setRowCount(0);
                rendus = new ArrayList<>();
            }
        });
    }

    private Exercice exerciceSelectionne() {
        int ligne = tableExercices.getSelectedRow();
        return ligne >= 0 && ligne < exercices.size() ? exercices.get(ligne) : null;
    }

    private void chargerRendus() {
        Exercice x = exerciceSelectionne();
        modeleRendus.setRowCount(0);
        rendus = new ArrayList<>();
        if (x == null) return;
        Taches.executer(this, () -> service.consulterRendus(connecte, x), (ArrayList<Rendu> liste) -> {
            if (exerciceSelectionne() == null || exerciceSelectionne().getId() != x.getId()) return;
            rendus = new ArrayList<>(liste);
            modeleRendus.setRowCount(0);
            for (Rendu r : rendus) {
                modeleRendus.addRow(new Object[] {Libelles.nom(r.getTutore()), r.getDateRendu(), r.getType(),
                        Fichiers.nom(r.getChemin())});
            }
        });
    }

    private void publier() {
        JTextField titre = new JTextField(26);
        JTextField fichier = new JTextField(26);
        fichier.setEditable(false);
        JButton parcourir = new Bouton("Parcourir...");
        parcourir.addActionListener(e -> {
            String chemin = Fichiers.choisir(this);
            if (chemin != null) fichier.setText(chemin);
        });

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Titre"), c);
        c.gridx = 1; c.gridwidth = 2; p.add(titre, c);
        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Document"), c);
        c.gridx = 1; p.add(fichier, c);
        c.gridx = 2; p.add(parcourir, c);
        c.gridx = 1; c.gridy = 2; c.gridwidth = 2;
        p.add(new JLabel("PDF, DOC ou DOCX. Vos tutorés seront prévenus."), c);

        if (!Dialogue.formulaire(this, "Publier un exercice", p, "Publier")) return;
        String t = titre.getText().trim();
        String chemin = fichier.getText().trim();
        Taches.executer(this, () -> service.publierExercice(connecte, t, chemin.isEmpty() ? null : chemin),
                x -> { }, this::actualiser);
    }
}
