/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */

import dao.CampusDAO;
import dao.UtilisateurDAO;
import modele.Etudiant;
import modele.Niveau;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;
import util.MotDePasse;

public class TestInscriptionConnexion {
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
        CampusDAO campus = new CampusDAO();
        String email = "pahola@mail.fr";

        attendreRefus("Adresse absente de la liste du campus (R1)",
                () -> g.inscrire("inconnu@mail.fr", "X", "Y", "mdp"));

        Etudiant e = g.inscrire(email, "Lago", "Pahola", "monMotDePasse1");
        verifier("Inscription : compte créé avec un id de la base", e.getId() > 0);
        verifier("Le niveau vient de la liste : L2", e.getNiveau() == Niveau.L2);
        verifier("Un nouvel étudiant n'est pas tuteur", !e.isEstTuteurActif());

        Utilisateur relu = utilisateurs.chercherParEmail(email);
        verifier("Le mot de passe n'est pas stocké en clair", !relu.getMdp().contains("monMotDePasse1"));
        verifier("Le hachage stocké correspond au mot de passe",
                MotDePasse.verifier("monMotDePasse1", relu.getMdp()));

        attendreRefus("Inscription en double", () -> g.inscrire(email, "Autre", "Nom", "mdp"));

        Utilisateur connecte = g.connecter(email, "monMotDePasse1");
        verifier("Connexion : renvoie un Etudiant", connecte instanceof Etudiant);
        verifier("Connexion : même compte", connecte.getId() == e.getId());
        attendreRefus("Mauvais mot de passe", () -> g.connecter(email, "mauvais"));
        attendreRefus("Adresse inconnue", () -> g.connecter("inconnu@mail.fr", "monMotDePasse1"));
        attendreRefus("Injection SQL dans l'adresse", () -> g.connecter("' OR '1'='1", "x"));

        campus.modifierNiveau(email, Niveau.L3);
        Utilisateur apres = g.connecter(email, "monMotDePasse1");
        verifier("Niveau relu à la connexion : L3",
                ((Etudiant) apres).getNiveau() == Niveau.L3);
        verifier("Le niveau est enregistré en base",
                ((Etudiant) utilisateurs.chercherParId(e.getId())).getNiveau() == Niveau.L3);

        campus.modifierNiveau(email, Niveau.L2);
        verifier("Suppression du compte de test", utilisateurs.supprimer(e.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);
        verifier("Liste du campus remise en état : L2", campus.chercherNiveau(email) == Niveau.L2);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}