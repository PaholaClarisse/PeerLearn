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
import dao.EvaluationDAO;
import dao.MatiereDAO;
import dao.UtilisateurDAO;
import java.time.LocalDate;
import modele.Demande;
import modele.Etudiant;
import modele.EvaluationTuteur;
import modele.Matiere;
import modele.Niveau;
import modele.StatutDemande;
import service.RegleGestionException;

public class TestEvaluationDAO {
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
        DemandeDAO demandes = new DemandeDAO();
        EvaluationDAO dao = new EvaluationDAO();
        Matiere matiere = new MatiereDAO().lister().get(0);

        verifier("Aucune évaluation au départ", dao.lister().isEmpty());

        Etudiant tutore = utilisateurs.ajouterEtudiant("Test", "Tutore", "test.e1@mail.fr", "mdp", Niveau.L1);
        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.e2@mail.fr", "mdp", Niveau.L3);
        Demande d = demandes.ajouter(new Demande(tutore, tuteur, matiere));

        EvaluationTuteur e1 = dao.ajouter(new EvaluationTuteur(d, 4, "Explications claires."));
        verifier("Évaluation insérée : l'id est attribué", e1.getId() > 0);

        EvaluationTuteur relue = dao.chercherParId(e1.getId());
        verifier("Relecture : note et commentaire conservés",
                relue != null && relue.getNote() == 4 && relue.getCommentaire().equals("Explications claires."));
        verifier("Relecture : demande conservée", relue.getDemande().getId() == d.getId());
        verifier("Relecture : date du jour", relue.getDateEvaluation().equals(LocalDate.now()));

        EvaluationTuteur parDemande = dao.chercherParDemande(d.getId());
        verifier("Recherche par demande", parDemande != null && parDemande.getId() == e1.getId());

        attendreRefus("Deuxième évaluation pour la même demande",
                () -> dao.ajouter(new EvaluationTuteur(d, 5, "Encore")));
        attendreRefus("Note de 7 refusée par la base",
                () -> dao.ajouter(new EvaluationTuteur(0, d, 7, "Test", LocalDate.now())));

        Demande fantome = new Demande(99999, tutore, tuteur, matiere, LocalDate.now(), StatutDemande.ACCEPTEE);
        attendreRefus("Évaluation d'une demande inexistante refusée par la base",
                () -> dao.ajouter(new EvaluationTuteur(fantome, 3, "Test")));

        verifier("Aucune évaluation pour une demande inconnue", dao.chercherParDemande(99999) == null);
        verifier("Id inconnu : null", dao.chercherParId(99999) == null);
        verifier("Une évaluation dans la liste", dao.lister().size() == 1);

        verifier("Suppression de l'évaluation", dao.supprimer(e1.getId()));
        verifier("Plus aucune évaluation", dao.lister().isEmpty());
        verifier("Suppression de la demande", demandes.supprimer(d.getId()));
        verifier("Suppression des deux comptes de test",
                utilisateurs.supprimer(tutore.getId()) && utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
