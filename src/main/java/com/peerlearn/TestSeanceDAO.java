/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */

import dao.SeanceDAO;
import dao.UtilisateurDAO;
import java.time.LocalDate;
import modele.Etudiant;
import modele.Niveau;
import modele.SeanceTutorat;
import modele.StatutSeance;
import service.RegleGestionException;

public class TestSeanceDAO {
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
        SeanceDAO dao = new SeanceDAO();

        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.s1@mail.fr", "mdp", Niveau.L3);
        Etudiant t1 = utilisateurs.ajouterEtudiant("Test", "Un", "test.s2@mail.fr", "mdp", Niveau.L1);
        Etudiant t2 = utilisateurs.ajouterEtudiant("Test", "Deux", "test.s3@mail.fr", "mdp", Niveau.L1);

        SeanceTutorat s = dao.ajouter(new SeanceTutorat(tuteur, LocalDate.now().plusWeeks(1), 60));
        verifier("Séance insérée : l'id est attribué", s.getId() > 0);

        SeanceTutorat relue = dao.chercherParId(s.getId());
        verifier("Relecture : séance planifiée de 60 minutes",
                relue != null && relue.getStatut() == StatutSeance.PLANIFIEE && relue.getDureeMinutes() == 60);
        verifier("Relecture : tuteur et date conservés",
                relue.getTuteur().getId() == tuteur.getId()
                && relue.getDateSeance().equals(LocalDate.now().plusWeeks(1)));
        verifier("Aucun pointage au départ",
                relue.getPresents().isEmpty() && relue.getAbsents().isEmpty());

        dao.enregistrerPointage(s, t1, true);
        dao.enregistrerPointage(s, t2, false);
        SeanceTutorat pointee = dao.chercherParId(s.getId());
        verifier("Relecture : 1 présent, le bon",
                pointee.getPresents().size() == 1 && pointee.getPresents().get(0).getId() == t1.getId());
        verifier("Relecture : 1 absent, le bon",
                pointee.getAbsents().size() == 1 && pointee.getAbsents().get(0).getId() == t2.getId());
        attendreRefus("Pointer deux fois le même tutoré",
                () -> dao.enregistrerPointage(s, t1, false));

        SeanceTutorat s2 = dao.ajouter(new SeanceTutorat(tuteur, LocalDate.now().plusWeeks(2), 90));
        LocalDate nouvelle = LocalDate.now().plusWeeks(3);
        s2.reporter(nouvelle);
        dao.modifier(s2);
        verifier("Report enregistré", dao.chercherParId(s2.getId()).getDateSeance().equals(nouvelle));
        s2.annuler();
        dao.modifier(s2);
        verifier("Annulation enregistrée",
                dao.chercherParId(s2.getId()).getStatut() == StatutSeance.ANNULEE);

        attendreRefus("Durée nulle refusée par la base",
                () -> dao.ajouter(new SeanceTutorat(0, tuteur, LocalDate.now().plusWeeks(1), 0, StatutSeance.PLANIFIEE)));

        verifier("Id inconnu : null", dao.chercherParId(99999) == null);
        verifier("Suppression d'une séance pointée (transaction)", dao.supprimer(s.getId()));
        verifier("La séance n'existe plus", dao.chercherParId(s.getId()) == null);
        verifier("Suppression de la séance annulée", dao.supprimer(s2.getId()));
        verifier("Suppression des trois comptes de test",
                utilisateurs.supprimer(t1.getId()) && utilisateurs.supprimer(t2.getId())
                && utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}