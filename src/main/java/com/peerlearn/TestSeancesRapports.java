/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import dao.DemandeDAO;
import dao.MatiereDAO;
import dao.RapportDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.RapportSuivi;
import modele.SeanceTutorat;
import modele.StatutSeance;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;

public class TestSeancesRapports {
    private static int ok = 0;
    private static int echecs = 0;

    private static void verifier(String titre, boolean condition) {
        if (condition) {
            ok++;
        } else {
            echecs++;
        }
        System.out.println((condition ? "OK    : " : "ECHEC : ") + titre);
    }

    private static void attendreRefus(String titre, Runnable action) {
        try {
            action.run();
            echecs++;
            System.out.println("ECHEC : " + titre + " aurait dû être refusé");
        } catch (RegleGestionException e) {
            ok++;
            System.out.println("OK    : " + titre + " -> " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        GestionTutorat g = new GestionTutorat();
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        DemandeDAO demandes = new DemandeDAO();
        SeanceDAO seances = new SeanceDAO();
        RapportDAO rapports = new RapportDAO();
        Matiere m1 = new MatiereDAO().lister().get(0);
        Utilisateur admin = utilisateurs.chercherParEmail("admin@mail.fr");

        Etudiant hugo = g.inscrire("hugo@mail.fr", "Roux", "Hugo", "mdp1");
        Etudiant chloe = g.inscrire("chloe@mail.fr", "Petit", "Chloe", "mdp2");
        Etudiant dylan = g.inscrire("dylan@mail.fr", "Leroy", "Dylan", "mdp3");
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "mdp4");
        Etudiant alice = g.inscrire("alice@mail.fr", "Martin", "Alice", "mdp5");

        g.definirProfilTuteur(yohan, m1, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(yohan);
        g.definirProfilTuteur(alice, m1, DayOfWeek.THURSDAY, LocalTime.of(10, 0));
        g.activerTuteur(alice);
        Demande dh = g.envoyerDemande(hugo, yohan);
        Demande dc = g.envoyerDemande(chloe, yohan);
        g.validerDemande(yohan, dh);
        g.validerDemande(yohan, dc);

        attendreRefus("Un tutoré enregistre une séance",
                () -> g.enregistrerSeance(hugo, LocalDate.now().plusWeeks(1), 60));
        SeanceTutorat s1 = g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(1), 60);
        verifier("Séance enregistrée : l'id est attribué", s1.getId() > 0);
        attendreRefus("Deux séances la même semaine (R2)",
                () -> g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(1), 60));
        attendreRefus("Durée nulle", () -> g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(2), 0));
        verifier("Une séance dans la liste du tuteur", g.mesSeances(yohan).size() == 1);
        verifier("Un autre tuteur n'a aucune séance", g.mesSeances(alice).isEmpty());

        g.enregistrerPresence(yohan, s1, hugo);
        g.enregistrerAbsence(yohan, s1, chloe);
        SeanceTutorat relue = seances.chercherParId(s1.getId());
        verifier("Présence et absence enregistrées en base",
                relue.getPresents().size() == 1 && relue.getAbsents().size() == 1
                && relue.getPresents().get(0).getId() == hugo.getId());
        attendreRefus("Pointer deux fois le même tutoré", () -> g.enregistrerPresence(yohan, s1, hugo));
        attendreRefus("Pointer un étudiant non suivi", () -> g.enregistrerPresence(yohan, s1, dylan));
        attendreRefus("Un autre tuteur pointe la séance", () -> g.enregistrerPresence(alice, s1, hugo));

        attendreRefus("Rapport pour un tutoré absent", () -> g.redigerRapport(yohan, s1, chloe, 4, "Test"));
        attendreRefus("Rapport avec une note de 6", () -> g.redigerRapport(yohan, s1, hugo, 6, "Test"));
        RapportSuivi r = g.redigerRapport(yohan, s1, hugo, 4, "Observation confidentielle");
        verifier("Rapport enregistré : l'id est attribué", r.getId() > 0);
        attendreRefus("Deuxième rapport pour la même séance et le même tutoré",
                () -> g.redigerRapport(yohan, s1, hugo, 3, "Doublon"));
        attendreRefus("Un autre tuteur rédige le rapport", () -> g.redigerRapport(alice, s1, hugo, 3, "Test"));

        verifier("R8 : le tutoré voit sa note",
                g.consulterProgression(hugo).size() == 1 && g.consulterProgression(hugo).get(0) == 4);
        verifier("Un tutoré sans rapport : aucune note", g.consulterProgression(chloe).isEmpty());
        attendreRefus("Un tutoré lit les rapports complets (R8)", () -> g.consulterRapports(hugo));
        verifier("L'administrateur voit 1 rapport, observations comprises",
                g.consulterRapports(admin).size() == 1
                && g.consulterRapports(admin).get(0).getObservations().equals("Observation confidentielle"));

        SeanceTutorat s2 = g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(2), 90);
        verifier("Deuxième séance enregistrée", s2.getId() > 0);
        attendreRefus("Report vers une semaine déjà occupée (R2)",
                () -> g.reporterSeance(yohan, s2, LocalDate.now().plusWeeks(1)));
        attendreRefus("Report dans le passé",
                () -> g.reporterSeance(yohan, s2, LocalDate.now().minusDays(1)));
        attendreRefus("Un autre étudiant reporte la séance",
                () -> g.reporterSeance(alice, s2, LocalDate.now().plusWeeks(5)));
        g.reporterSeance(yohan, s2, LocalDate.now().plusWeeks(3));
        verifier("Report enregistré en base",
                seances.chercherParId(s2.getId()).getDateSeance().equals(LocalDate.now().plusWeeks(3)));
        g.annulerSeance(yohan, s2);
        verifier("Annulation enregistrée en base",
                seances.chercherParId(s2.getId()).getStatut() == StatutSeance.ANNULEE);
        attendreRefus("Annuler deux fois", () -> g.annulerSeance(yohan, s2));
        attendreRefus("Annuler une séance déjà pointée", () -> g.annulerSeance(yohan, s1));
        attendreRefus("Pointer une séance annulée", () -> g.enregistrerPresence(yohan, s2, hugo));
        SeanceTutorat s3 = g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(2), 60);
        verifier("La semaine d'une séance annulée est libérée (R2)", s3.getId() > 0);

        verifier("2 séances planifiées (l'annulée n'est pas comptée)", g.nombreSeances(admin) == 2);
        verifier("Statistique d'absences : 1 pour Petit",
                Integer.valueOf(1).equals(g.absencesParTutore(admin).get("Petit")));
        verifier("Moyenne des notes de Sokegbe : 4.0",
                Double.valueOf(4.0).equals(g.moyenneNotesParTuteur(admin).get("Sokegbe")));
        attendreRefus("Un tutoré lit le nombre de séances", () -> g.nombreSeances(hugo));

        verifier("Suppression du rapport", rapports.supprimer(r.getId()));
        verifier("Suppression des 3 séances",
                seances.supprimer(s1.getId()) && seances.supprimer(s2.getId()) && seances.supprimer(s3.getId()));
        verifier("Suppression des 2 demandes", demandes.supprimer(dh.getId()) && demandes.supprimer(dc.getId()));
        verifier("Suppression des 5 comptes de test",
                utilisateurs.supprimer(hugo.getId()) && utilisateurs.supprimer(chloe.getId())
                && utilisateurs.supprimer(dylan.getId()) && utilisateurs.supprimer(yohan.getId())
                && utilisateurs.supprimer(alice.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
