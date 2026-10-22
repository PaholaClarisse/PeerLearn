/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */

import dao.UtilisateurDAO;
import modele.Administrateur;
import modele.Utilisateur;

public class TestUtilisateurDAO {
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
        UtilisateurDAO dao = new UtilisateurDAO();

        Utilisateur admin = dao.chercherParEmail("admin@mail.fr");
        verifier("L'administrateur est trouvé par son email", admin != null);
        verifier("La lecture renvoie le bon type : Administrateur", admin instanceof Administrateur);
        verifier("Son rôle est ADMIN", admin != null && admin.getRole().equals("ADMIN"));
        verifier("Son nom est Admin", admin != null && admin.getNom().equals("Admin"));

        Utilisateur parId = dao.chercherParId(admin.getId());
        verifier("Relecture par id", parId != null && parId.getMail().equals("admin@mail.fr"));

        verifier("Email inconnu : null", dao.chercherParEmail("inconnu@mail.fr") == null);
        verifier("Id inconnu : null", dao.chercherParId(99999) == null);
        verifier("Email d'injection : traité comme un texte, aucun résultat",
                dao.chercherParEmail("' OR '1'='1") == null);
        verifier("1 utilisateur dans la base", dao.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}