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
import java.util.ArrayList;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.Niveau;

public class TestDemandeListes {
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

    public static void main(String[] args) {
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        DemandeDAO dao = new DemandeDAO();
        Matiere matiere = new MatiereDAO().lister().get(0);

        verifier("Aucune demande au départ", dao.lister().isEmpty());

        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.l1@mail.fr", "mdp", Niveau.L3);
        Etudiant t1 = utilisateurs.ajouterEtudiant("Test", "Un", "test.l2@mail.fr", "mdp", Niveau.L1);
        Etudiant t2 = utilisateurs.ajouterEtudiant("Test", "Deux", "test.l3@mail.fr", "mdp", Niveau.L1);
        Etudiant t3 = utilisateurs.ajouterEtudiant("Test", "Trois", "test.l4@mail.fr", "mdp", Niveau.L1);

        Demande d1 = new Demande(t1, tuteur, matiere);
        d1.accepter();
        dao.ajouter(d1);
        Demande d2 = new Demande(t2, tuteur, matiere);
        d2.accepter();
        dao.ajouter(d2);
        Demande d3 = new Demande(t3, tuteur, matiere);
        dao.ajouter(d3);

        verifier("3 demandes reçues par le tuteur", dao.listerParTuteur(tuteur.getId()).size() == 3);
        verifier("1 demande envoyée par le premier tutoré", dao.listerParTutore(t1.getId()).size() == 1);
        verifier("Le tuteur n'a envoyé aucune demande", dao.listerParTutore(tuteur.getId()).isEmpty());
        verifier("Toutes les demandes : 3", dao.lister().size() == 3);

        ArrayList<Etudiant> acceptes = dao.listerTutoresAcceptes(tuteur.getId());
        verifier("2 tutorés acceptés (la demande en attente ne compte pas)", acceptes.size() == 2);
        verifier("Ce sont les bons tutorés (comparaison par id)",
                acceptes.contains(t1) && acceptes.contains(t2) && !acceptes.contains(t3));

        d1.terminer();
        dao.modifierStatut(d1);
        verifier("Suivi terminé : la place est libérée (1 tutoré accepté)",
                dao.listerTutoresAcceptes(tuteur.getId()).size() == 1);

        Etudiant relu = (Etudiant) utilisateurs.chercherParId(tuteur.getId());
        verifier("Avant restauration : liste des tutorés vide", relu.getTutores().isEmpty());
        relu.restaurerTutores(dao.listerTutoresAcceptes(tuteur.getId()));
        verifier("Après restauration : 1 tutoré, le bon",
                relu.getTutores().size() == 1 && relu.getTutores().contains(t2));

        verifier("Suppression des 3 demandes",
                dao.supprimer(d1.getId()) && dao.supprimer(d2.getId()) && dao.supprimer(d3.getId()));
        verifier("Suppression des 4 comptes de test",
                utilisateurs.supprimer(t1.getId()) && utilisateurs.supprimer(t2.getId())
                && utilisateurs.supprimer(t3.getId()) && utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
