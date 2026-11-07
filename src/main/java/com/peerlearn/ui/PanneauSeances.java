package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;
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
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import modele.Etudiant;
import modele.SeanceTutorat;
import modele.StatutSeance;
import service.GestionTutorat;

/**
 * Espace tuteur : séances (planifier, reporter, annuler), pointage des tutorés
 * (présent / absent) et rapport de suivi. Les règles (une séance par semaine,
 * séance non modifiable une fois pointée...) sont vérifiées par le contrôleur.
 */
public class PanneauSeances extends JPanel implements Actualisable {

    private static final String NON_POINTE = "Non pointé";
    private static final String PRESENT = "Présent";
    private static final String ABSENT = "Absent";

    private final GestionTutorat service;
    private final Etudiant connecte;

    private final Libelles.ModeleTable modeleSeances =
            new Libelles.ModeleTable("Date", "Durée", "Statut", "Présents", "Absents");
    private final Libelles.ModeleTable modeleTutores =
            new Libelles.ModeleTable("Tutoré", "Pointage");
    private final JTable tableSeances = Style.table(new JTable(modeleSeances));
    private final JTable tableTutores = Style.table(new JTable(modeleTutores));

    private final JButton boutonPlanifier = new Bouton("Planifier une séance");
    private final JButton boutonReporter = new Bouton("Reporter");
    private final JButton boutonAnnuler = new Bouton("Annuler la séance");
    private final JButton boutonActualiser = new Bouton("Actualiser");
    private final JButton boutonPresent = new Bouton("Présent");
    private final JButton boutonAbsent = new Bouton("Absent");
    private final JButton boutonRapport = new Bouton("Rédiger un rapport");

    private ArrayList<SeanceTutorat> seances = new ArrayList<>();
    private ArrayList<Etudiant> tutores = new ArrayList<>();

    public PanneauSeances(GestionTutorat service, Etudiant connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tableSeances.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTutores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel haut = new JPanel(new BorderLayout(4, 4));
        haut.add(new JLabel("Mes séances"), BorderLayout.NORTH);
        haut.add(Style.defiler(tableSeances), BorderLayout.CENTER);
        JPanel boutonsHaut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutonsHaut.add(boutonPlanifier);
        boutonsHaut.add(boutonReporter);
        boutonsHaut.add(boutonAnnuler);
        boutonsHaut.add(boutonActualiser);
        haut.add(boutonsHaut, BorderLayout.SOUTH);

        JPanel bas = new JPanel(new BorderLayout(4, 4));
        bas.add(new JLabel("Pointage de la séance sélectionnée"), BorderLayout.NORTH);
        bas.add(Style.defiler(tableTutores), BorderLayout.CENTER);
        JPanel boutonsBas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutonsBas.add(boutonPresent);
        boutonsBas.add(boutonAbsent);
        boutonsBas.add(boutonRapport);
        bas.add(boutonsBas, BorderLayout.SOUTH);

        JSplitPane coupe = new JSplitPane(JSplitPane.VERTICAL_SPLIT, haut, bas);
        coupe.setResizeWeight(0.55);
        coupe.setBorder(null);
        add(coupe, BorderLayout.CENTER);

        tableSeances.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                remplirTutores();
                majBoutons();
            }
        });
        tableTutores.getSelectionModel().addListSelectionListener(e -> majBoutons());

        boutonActualiser.addActionListener(e -> actualiser());
        boutonPlanifier.addActionListener(e -> planifier());
        boutonReporter.addActionListener(e -> reporter());
        boutonAnnuler.addActionListener(e -> annuler());
        boutonPresent.addActionListener(e -> pointer(true));
        boutonAbsent.addActionListener(e -> pointer(false));
        boutonRapport.addActionListener(e -> rapport());
        majBoutons();
    }

    // ------------------------------------------------------------ chargement

    @Override
    public void actualiser() {
        int idSelection = seanceSelectionnee() == null ? -1 : seanceSelectionnee().getId();
        Taches.executer(this, () -> service.mesSeances(connecte), (ArrayList<SeanceTutorat> s) ->
                Taches.executer(this, () -> service.mesTutores(connecte), (ArrayList<Etudiant> t) -> {
                    seances = s;
                    tutores = t;
                    remplirSeances(idSelection);
                }));
    }

    private void remplirSeances(int idSelection) {
        modeleSeances.setRowCount(0);
        int aSelectionner = -1;
        for (int i = 0; i < seances.size(); i++) {
            SeanceTutorat s = seances.get(i);
            modeleSeances.addRow(new Object[] {s.getDateSeance(), s.getDureeMinutes() + " min", statutTexte(s),
                    noms(s.getPresents()), noms(s.getAbsents())});
            if (s.getId() == idSelection) aSelectionner = i;
        }
        if (aSelectionner >= 0) {
            tableSeances.setRowSelectionInterval(aSelectionner, aSelectionner);
        } else {
            remplirTutores();
            majBoutons();
        }
    }

    private void remplirTutores() {
        int ligne = tableTutores.getSelectedRow();
        Etudiant avant = ligne >= 0 && ligne < tutores.size() ? tutores.get(ligne) : null;
        modeleTutores.setRowCount(0);
        SeanceTutorat s = seanceSelectionnee();
        if (s == null) return;
        for (Etudiant t : tutores) {
            modeleTutores.addRow(new Object[] {Libelles.nom(t), pointage(s, t)});
        }
        int retour = avant == null ? -1 : tutores.indexOf(avant);
        if (retour >= 0) tableTutores.setRowSelectionInterval(retour, retour);
    }

    private static String statutTexte(SeanceTutorat s) {
        if (s.getStatut() == StatutSeance.ANNULEE) return "Annulée";
        if (!s.getPresents().isEmpty() || !s.getAbsents().isEmpty()) return "Pointée";
        if (s.getDateSeance().isBefore(LocalDate.now())) return "Passée, à pointer";
        return "Planifiée";
    }

    private static String noms(ArrayList<Etudiant> liste) {
        return liste.stream().map(Libelles::nom).collect(Collectors.joining(", "));
    }

    private static String pointage(SeanceTutorat s, Etudiant t) {
        if (s.getPresents().contains(t)) return PRESENT;
        if (s.getAbsents().contains(t)) return ABSENT;
        return NON_POINTE;
    }

    // ------------------------------------------------------------- sélection

    private SeanceTutorat seanceSelectionnee() {
        int ligne = tableSeances.getSelectedRow();
        return ligne >= 0 && ligne < seances.size() ? seances.get(ligne) : null;
    }

    private Etudiant tutoreSelectionne() {
        int ligne = tableTutores.getSelectedRow();
        return ligne >= 0 && ligne < tutores.size() ? tutores.get(ligne) : null;
    }

    /** Active seulement les actions que le contrôleur acceptera (il revérifie de toute façon). */
    private void majBoutons() {
        SeanceTutorat s = seanceSelectionnee();
        Etudiant t = tutoreSelectionne();
        boolean pointee = s != null && (!s.getPresents().isEmpty() || !s.getAbsents().isEmpty());
        boolean modifiable = s != null && s.getStatut() == StatutSeance.PLANIFIEE && !pointee
                && !s.getDateSeance().isBefore(LocalDate.now());
        boutonReporter.setEnabled(modifiable);
        boutonAnnuler.setEnabled(modifiable);

        String etat = s != null && t != null ? pointage(s, t) : "";
        boolean ouverte = s != null && s.getStatut() == StatutSeance.PLANIFIEE;
        boutonPresent.setEnabled(ouverte && NON_POINTE.equals(etat));
        boutonAbsent.setEnabled(ouverte && NON_POINTE.equals(etat));
        boutonRapport.setEnabled(ouverte && PRESENT.equals(etat));
    }

    // --------------------------------------------------------------- actions

    private void planifier() {
        DayOfWeek jour = connecte.getJour();
        LocalDate propose = jour == null ? LocalDate.now() : LocalDate.now().with(TemporalAdjusters.nextOrSame(jour));
        JSpinner choixDate = spinnerDate(propose);
        JSpinner choixDuree = new JSpinner(new SpinnerNumberModel(60, 15, 480, 15));

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Date"), c);
        c.gridx = 1; p.add(choixDate, c);
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Durée (minutes)"), c);
        c.gridx = 1; p.add(choixDuree, c);

        if (!confirmer(p, "Planifier une séance")) return;
        LocalDate date = dateDe(choixDate);
        int duree = (Integer) choixDuree.getValue();
        Taches.executer(this, () -> service.enregistrerSeance(connecte, date, duree), s -> { }, this::actualiser);
    }

    private void reporter() {
        SeanceTutorat s = seanceSelectionnee();
        if (s == null) return;
        LocalDate propose = s.getDateSeance().isBefore(LocalDate.now()) ? LocalDate.now() : s.getDateSeance();
        JSpinner choixDate = spinnerDate(propose);
        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel("Nouvelle date"));
        p.add(choixDate);
        if (!confirmer(p, "Reporter la séance du " + s.getDateSeance())) return;
        LocalDate date = dateDe(choixDate);
        Taches.executer(this, () -> { service.reporterSeance(connecte, s, date); return null; },
                r -> { }, this::actualiser);
    }

    private void annuler() {
        SeanceTutorat s = seanceSelectionnee();
        if (s == null) return;
        if (!Dialogue.confirmer(this, "Annuler la séance",
                "Annuler la séance du " + s.getDateSeance() + " ?\nVos tutorés seront prévenus.",
                "Annuler la séance", true)) return;
        Taches.executer(this, () -> { service.annulerSeance(connecte, s); return null; },
                r -> { }, this::actualiser);
    }

    /** Le pointage est définitif : il n'existe pas de méthode pour le corriger. */
    private void pointer(boolean present) {
        SeanceTutorat s = seanceSelectionnee();
        Etudiant t = tutoreSelectionne();
        if (s == null || t == null) return;
        if (!present) {
            if (!Dialogue.confirmer(this, "Enregistrer une absence",
                    "Enregistrer l'absence de " + Libelles.nom(t) + " ?\nL'administration et le tutoré seront prévenus.",
                    "Enregistrer l'absence", true)) return;
        }
        Taches.executer(this, () -> {
            if (present) {
                service.enregistrerPresence(connecte, s, t);
            } else {
                service.enregistrerAbsence(connecte, s, t);
            }
            return null;
        }, r -> { }, this::actualiser);
    }

    private void rapport() {
        SeanceTutorat s = seanceSelectionnee();
        Etudiant t = tutoreSelectionne();
        if (s == null || t == null) return;
        JSpinner choixNote = new JSpinner(new SpinnerNumberModel(3, 0, 5, 1));
        JTextArea observations = new JTextArea(5, 30);
        observations.setLineWrap(true);
        observations.setWrapStyleWord(true);

        JPanel p = new JPanel(new BorderLayout(6, 6));
        JPanel note = new JPanel(new FlowLayout(FlowLayout.LEFT));
        note.add(new JLabel("Note (0 à 5)"));
        note.add(choixNote);
        p.add(note, BorderLayout.NORTH);
        p.add(Style.defiler(observations), BorderLayout.CENTER);
        p.add(new JLabel("Le tutoré ne voit que la note ; les observations sont réservées à l'administration."),
                BorderLayout.SOUTH);

        if (!confirmer(p, "Rapport pour " + Libelles.nom(t))) return;
        int note0 = (Integer) choixNote.getValue();
        String texte = observations.getText().trim();
        Taches.executer(this, () -> service.redigerRapport(connecte, s, t, note0, texte),
                r -> Taches.info(this, "Rapport enregistré."), this::actualiser);
    }

    // --------------------------------------------------------------- outils

    private boolean confirmer(JPanel contenu, String titre) {
        return Dialogue.formulaire(this, titre, contenu, "Valider");
    }

    private static JSpinner spinnerDate(LocalDate d) {
        Date valeur = Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(valeur, null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
        return s;
    }

    private static LocalDate dateDe(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
