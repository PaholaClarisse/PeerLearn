/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */

import dao.RapportDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import java.time.LocalDate;
import modele.Etudiant;
import modele.Niveau;
import modele.RapportSuivi;
import modele.SeanceTutorat;
import service.RegleGestionException;

public class TestRapportDAO {
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
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        SeanceDAO seances = new SeanceDAO();
        RapportDAO dao = new RapportDAO();

        verifier("Aucun rapport au départ", dao.lister().isEmpty());

        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.r1@mail.fr", "mdp", Niveau.L3);
        Etudiant t1 = utilisateurs.ajouterEtudiant("Test", "Un", "test.r2@mail.fr", "mdp", Niveau.L1);
        Etudiant t2 = utilisateurs.ajouterEtudiant("Test", "Deux", "test.r3@mail.fr", "mdp", Niveau.L1);
        SeanceTutorat s = seances.ajouter(new SeanceTutorat(tuteur, LocalDate.now().plusWeeks(1), 60));

        RapportSuivi r1 = dao.ajouter(new RapportSuivi(s, t1, 4, "Bon travail, à revoir les boucles."));
        verifier("Rapport inséré : l'id est attribué", r1.getId() > 0);

        RapportSuivi relu = dao.chercherParId(r1.getId());
        verifier("Relecture : note et observations conservées",
                relu != null && relu.getNote() == 4
                && relu.getObservations().equals("Bon travail, à revoir les boucles."));
        verifier("Relecture : séance et tutoré conservés",
                relu.getSeance().getId() == s.getId() && relu.getTutore().getId() == t1.getId());
        verifier("Relecture : date du jour", relu.getDateRapport().equals(LocalDate.now()));

        RapportSuivi r2 = dao.ajouter(new RapportSuivi(s, t2, 0, "Séance difficile."));
        verifier("Une note de 0 est acceptée", dao.chercherParId(r2.getId()).getNote() == 0);

        attendreRefus("Deuxième rapport pour la même séance et le même tutoré",
                () -> dao.ajouter(new RapportSuivi(s, t1, 3, "Doublon")));
        attendreRefus("Note de 6 refusée par la base",
                () -> dao.ajouter(new RapportSuivi(0, s, t1, 6, "Test", LocalDate.now())));

        verifier("Les rapports du premier tutoré : 1", dao.listerParTutore(t1.getId()).size() == 1);
        verifier("Tous les rapports : 2", dao.lister().size() == 2);
        verifier("Id inconnu : null", dao.chercherParId(99999) == null);

        verifier("Suppression du premier rapport", dao.supprimer(r1.getId()));
        verifier("Suppression du second rapport", dao.supprimer(r2.getId()));
        verifier("Plus aucun rapport", dao.lister().isEmpty());
        verifier("Suppression de la séance", seances.supprimer(s.getId()));
        verifier("Suppression des trois comptes de test",
                utilisateurs.supprimer(t1.getId()) && utilisateurs.supprimer(t2.getId())
                && utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}