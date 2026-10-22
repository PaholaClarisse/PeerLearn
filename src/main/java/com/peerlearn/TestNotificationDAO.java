/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */

import dao.NotificationDAO;
import dao.UtilisateurDAO;
import java.util.ArrayList;
import modele.Etudiant;
import modele.Niveau;
import modele.Notification;
import service.RegleGestionException;

public class TestNotificationDAO {
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
        NotificationDAO dao = new NotificationDAO();

        Etudiant e1 = utilisateurs.ajouterEtudiant("Test", "Un", "test.n1@mail.fr", "mdp", Niveau.L1);
        Etudiant e2 = utilisateurs.ajouterEtudiant("Test", "Deux", "test.n2@mail.fr", "mdp", Niveau.L2);

        verifier("Aucune notification au départ", dao.listerParUtilisateur(e1.getId()).isEmpty());

        Notification n1 = dao.ajouter(e1.getId(), "Votre demande a été acceptée.");
        verifier("Notification insérée : l'id est attribué", n1.getId() > 0);
        dao.ajouter(e1.getId(), "Exercice publié. L'apostrophe ne casse rien : ' OR '1'='1");
        dao.ajouter(e2.getId(), "Message pour l'autre utilisateur.");

        ArrayList<Notification> liste = dao.listerParUtilisateur(e1.getId());
        verifier("Liste : 2 notifications pour le premier utilisateur", liste.size() == 2);
        verifier("Liste : ordre d'envoi et contenu conservés",
                liste.get(0).getId() == n1.getId()
                && liste.get(0).getMessage().equals("Votre demande a été acceptée."));
        verifier("Apostrophes et texte d'injection stockés comme simple texte",
                liste.get(1).getMessage().equals("Exercice publié. L'apostrophe ne casse rien : ' OR '1'='1"));
        verifier("Chaque utilisateur ne voit que ses notifications",
                dao.listerParUtilisateur(e2.getId()).size() == 1);

        verifier("Toutes non lues au départ",
                dao.compterNonLues(e1.getId()) == 2 && !liste.get(0).isLue());
        verifier("Marquer comme lues : 2 lignes modifiées", dao.marquerToutesLues(e1.getId()) == 2);
        verifier("Plus aucune notification non lue", dao.compterNonLues(e1.getId()) == 0);
        verifier("Le second utilisateur garde sa notification non lue", dao.compterNonLues(e2.getId()) == 1);
        verifier("Relecture : la notification est marquée lue",
                dao.listerParUtilisateur(e1.getId()).get(0).isLue());

        attendreRefus("Notification pour un utilisateur inexistant refusée par la base",
                () -> dao.ajouter(99999, "Bonjour"));
        attendreRefus("Message de plus de 500 caractères refusé",
                () -> dao.ajouter(e1.getId(), "x".repeat(501)));

        verifier("Suppression d'une notification", dao.supprimer(n1.getId()));
        verifier("Suppression des notifications restantes du premier utilisateur : 1",
                dao.supprimerParUtilisateur(e1.getId()) == 1);
        verifier("Suppression des notifications du second utilisateur : 1",
                dao.supprimerParUtilisateur(e2.getId()) == 1);
        verifier("Suppression des deux comptes de test",
                utilisateurs.supprimer(e1.getId()) && utilisateurs.supprimer(e2.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}