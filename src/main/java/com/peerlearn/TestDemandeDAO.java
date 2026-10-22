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
import dao.UtilisateurDAO;
import java.time.LocalDate;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.Niveau;
import modele.StatutDemande;
import service.RegleGestionException;

public class TestDemandeDAO {
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
        DemandeDAO dao = new DemandeDAO();
        Matiere matiere = new MatiereDAO().lister().get(0);

        Etudiant tutore = utilisateurs.ajouterEtudiant("Test", "Tutore", "test.d1@mail.fr", "mdp", Niveau.L1);
        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.d2@mail.fr", "mdp", Niveau.L3);

        Demande d = dao.ajouter(new Demande(tutore, tuteur, matiere));
        verifier("Demande insérée : l'id est attribué", d.getId() > 0);

        Demande relue = dao.chercherParId(d.getId());
        verifier("Relecture : demande en attente",
                relue != null && relue.getStatut() == StatutDemande.EN_ATTENTE);
        verifier("Relecture : tutoré, tuteur et matière conservés",
                relue.getTutore().getId() == tutore.getId()
                && relue.getTuteur().getId() == tuteur.getId()
                && relue.getMatiere().getId() == matiere.getId());
        verifier("Relecture : date du jour", relue.getDateDemande().equals(LocalDate.now()));

        d.accepter();
        dao.modifierStatut(d);
        verifier("Statut ACCEPTEE enregistré",
                dao.chercherParId(d.getId()).getStatut() == StatutDemande.ACCEPTEE);

        d.terminer();
        dao.modifierStatut(d);
        verifier("Statut TERMINEE enregistré",
                dao.chercherParId(d.getId()).getStatut() == StatutDemande.TERMINEE);

        verifier("Id inconnu : null", dao.chercherParId(99999) == null);

        attendreRefus("Demande vers soi-même refusée par la base",
                () -> dao.ajouter(new Demande(0, tutore, tutore, matiere,
                        LocalDate.now(), StatutDemande.EN_ATTENTE)));

        attendreRefus("Supprimer un utilisateur qui a une demande",
                () -> utilisateurs.supprimer(tutore.getId()));

        verifier("Suppression de la demande", dao.supprimer(d.getId()));
        verifier("La demande n'existe plus", dao.chercherParId(d.getId()) == null);
        verifier("Suppression des deux comptes de test",
                utilisateurs.supprimer(tutore.getId()) && utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
