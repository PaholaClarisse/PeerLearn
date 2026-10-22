/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import dao.ExerciceDAO;
import dao.RenduDAO;
import dao.UtilisateurDAO;
import java.time.LocalDate;
import modele.Etudiant;
import modele.Exercice;
import modele.Niveau;
import modele.Rendu;
import modele.TypeFichier;
import service.RegleGestionException;

public class TestRenduDAO {
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
        ExerciceDAO exercices = new ExerciceDAO();
        RenduDAO dao = new RenduDAO();

        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.u1@mail.fr", "mdp", Niveau.L3);
        Etudiant tutore = utilisateurs.ajouterEtudiant("Test", "Tutore", "test.u2@mail.fr", "mdp", Niveau.L1);
        Exercice x = exercices.ajouter(new Exercice(tuteur, "TD1 : les boucles", "exercices/td1.pdf"));

        verifier("Aucun rendu au départ", dao.listerParExercice(x).isEmpty());

        Rendu r1 = dao.ajouter(new Rendu(x, tutore, "rendus/tutore_td1.docx"));
        verifier("Rendu inséré : l'id est attribué", r1.getId() > 0);

        Rendu relu = dao.chercherParId(r1.getId());
        verifier("Relecture : type DOCX et chemin conservés",
                relu != null && relu.getType() == TypeFichier.DOCX
                && relu.getChemin().equals("rendus/tutore_td1.docx"));
        verifier("Relecture : exercice et tutoré conservés",
                relu.getExercice().getId() == x.getId() && relu.getTutore().getId() == tutore.getId());
        verifier("Relecture : date du jour", relu.getDateRendu().equals(LocalDate.now()));

        Rendu parCouple = dao.chercherParExerciceEtTutore(x, tutore.getId());
        verifier("Recherche par exercice et tutoré", parCouple != null && parCouple.getId() == r1.getId());
        verifier("Aucun rendu pour un autre tutoré",
                dao.chercherParExerciceEtTutore(x, tuteur.getId()) == null);

        attendreRefus("Deuxième rendu du même exercice par le même tutoré",
                () -> dao.ajouter(new Rendu(x, tutore, "rendus/tutore_td1_v2.pdf")));

        Exercice fantome = new Exercice(99999, tuteur, "TD", TypeFichier.PDF, "td.pdf", LocalDate.now());
        attendreRefus("Rendu d'un exercice inexistant refusé par la base",
                () -> dao.ajouter(new Rendu(0, fantome, tutore, TypeFichier.PDF, "r.pdf", LocalDate.now())));

        verifier("Un rendu dans la liste de l'exercice", dao.listerParExercice(x).size() == 1);
        verifier("Id inconnu : null", dao.chercherParId(99999) == null);

        verifier("Suppression du rendu", dao.supprimer(r1.getId()));
        verifier("Plus aucun rendu", dao.listerParExercice(x).isEmpty());
        verifier("Suppression de l'exercice", exercices.supprimer(x.getId()));
        verifier("Suppression des deux comptes de test",
                utilisateurs.supprimer(tutore.getId()) && utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
