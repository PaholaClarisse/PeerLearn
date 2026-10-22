/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import dao.MatiereDAO;
import modele.Matiere;
import service.RegleGestionException;

public class TestMatiereDAO {
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
        MatiereDAO dao = new MatiereDAO();

        verifier("6 matières au départ", dao.lister().size() == 6);

        Matiere chimie = dao.ajouter("Chimie");
        verifier("Ajout : l'id est attribué par la base", chimie.getId() > 0);
        verifier("7 matières après l'ajout", dao.lister().size() == 7);

        Matiere relue = dao.chercherParId(chimie.getId());
        verifier("Relecture par id", relue != null && relue.getNom().equals("Chimie"));
        verifier("Id inconnu : null", dao.chercherParId(99999) == null);

        try {
            dao.ajouter("Chimie");
            echecs++;
            System.out.println("ECHEC : un doublon aurait dû être refusé");
        } catch (RegleGestionException e) {
            ok++;
            System.out.println("OK    : Doublon refusé -> " + e.getMessage());
        }

        Matiere injection = dao.ajouter("' OR '1'='1");
        verifier("Texte d'injection stocké comme simple texte",
                dao.chercherParId(injection.getId()).getNom().equals("' OR '1'='1"));

        dao.modifier(new Matiere(chimie.getId(), "Chimie organique"));
        verifier("Modification",
                dao.chercherParId(chimie.getId()).getNom().equals("Chimie organique"));

        verifier("Suppression de la matière ajoutée", dao.supprimer(chimie.getId()));
        verifier("Suppression du texte d'injection", dao.supprimer(injection.getId()));
        verifier("Suppression d'un id inconnu : false", !dao.supprimer(99999));
        verifier("6 matières à la fin", dao.lister().size() == 6);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}