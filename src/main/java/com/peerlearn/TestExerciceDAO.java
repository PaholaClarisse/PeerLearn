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
import dao.UtilisateurDAO;
import java.time.LocalDate;
import modele.Etudiant;
import modele.Exercice;
import modele.Niveau;
import modele.TypeFichier;
import service.RegleGestionException;

public class TestExerciceDAO {
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
        ExerciceDAO dao = new ExerciceDAO();

        verifier("Aucun exercice au départ", dao.lister().isEmpty());

        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.x1@mail.fr", "mdp", Niveau.L3);

        Exercice x1 = dao.ajouter(new Exercice(tuteur, "TD1 : les boucles", "exercices/td1.pdf"));
        verifier("Exercice inséré : l'id est attribué", x1.getId() > 0);

        Exercice relu = dao.chercherParId(x1.getId());
        verifier("Relecture : titre et type PDF conservés",
                relu != null && relu.getTitre().equals("TD1 : les boucles") && relu.getType() == TypeFichier.PDF);
        verifier("Relecture : tuteur et chemin conservés",
                relu.getTuteur().getId() == tuteur.getId() && relu.getChemin().equals("exercices/td1.pdf"));
        verifier("Relecture : date du jour", relu.getDatePublication().equals(LocalDate.now()));

        Exercice x2 = dao.ajouter(new Exercice(tuteur, "TD2", "exercices/td2.docx"));
        verifier("Type DOCX conservé", dao.chercherParId(x2.getId()).getType() == TypeFichier.DOCX);

        verifier("Les exercices du tuteur : 2", dao.listerParTuteur(tuteur.getId()).size() == 2);
        verifier("Tous les exercices : 2", dao.lister().size() == 2);

        Etudiant fantome = new Etudiant(99999, "Fantome", "X", "fantome@mail.fr", "mdp", Niveau.L3);
        attendreRefus("Exercice d'un tuteur inexistant refusé par la base",
                () -> dao.ajouter(new Exercice(0, fantome, "TD", TypeFichier.PDF, "td.pdf", LocalDate.now())));

        verifier("Id inconnu : null", dao.chercherParId(99999) == null);
        verifier("Suppression du premier exercice", dao.supprimer(x1.getId()));
        verifier("Suppression du second exercice", dao.supprimer(x2.getId()));
        verifier("Plus aucun exercice", dao.lister().isEmpty());
        verifier("Suppression du compte de test", utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
