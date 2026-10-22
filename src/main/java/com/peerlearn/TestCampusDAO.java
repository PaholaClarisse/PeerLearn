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
import modele.Niveau;
import service.RegleGestionException;

public class TestCampusDAO {
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
        CampusDAO dao = new CampusDAO();
        String email = "test.c1@mail.fr";

        verifier("10 adresses au départ", dao.listerEmails().size() == 10);
        verifier("yohan@mail.fr est en L3", dao.chercherNiveau("yohan@mail.fr") == Niveau.L3);
        verifier("pahola@mail.fr est en L2", dao.chercherNiveau("pahola@mail.fr") == Niveau.L2);
        verifier("Adresse absente de la liste : null (R1)", dao.chercherNiveau("inconnu@mail.fr") == null);
        verifier("Une adresse de la liste existe", dao.existe("alice@mail.fr"));
        verifier("Une adresse absente n'existe pas", !dao.existe("inconnu@mail.fr"));
        verifier("Texte d'injection : traité comme une adresse, aucun résultat",
                dao.chercherNiveau("' OR '1'='1") == null);

        dao.ajouter(email, Niveau.L1);
        verifier("Ajout : le niveau est conservé", dao.chercherNiveau(email) == Niveau.L1);
        verifier("11 adresses après l'ajout", dao.listerEmails().size() == 11);
        attendreRefus("Adresse en double", () -> dao.ajouter(email, Niveau.L2));

        dao.modifierNiveau(email, Niveau.L3);
        verifier("Passage de L1 à L3 enregistré", dao.chercherNiveau(email) == Niveau.L3);

        verifier("Suppression de l'adresse de test", dao.supprimer(email));
        verifier("L'adresse n'existe plus", dao.chercherNiveau(email) == null);
        verifier("10 adresses à la fin", dao.listerEmails().size() == 10);
        verifier("Suppression d'une adresse inconnue : false", !dao.supprimer("inconnu@mail.fr"));

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}