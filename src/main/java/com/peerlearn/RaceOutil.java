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
import dao.UtilisateurDAO;
import modele.Demande;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;

public class RaceOutil {
    public static void accepter(String emailTutore) throws InterruptedException {
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        Utilisateur yohan = utilisateurs.chercherParEmail("yohan@mail.fr");
        Utilisateur tutore = utilisateurs.chercherParEmail(emailTutore);
        Demande demande = new DemandeDAO().listerParTutore(tutore.getId()).get(0);
        GestionTutorat g = new GestionTutorat();

        long maintenant = System.currentTimeMillis();
        long depart = (maintenant / 60000 + 1) * 60000;
        System.out.println("Acceptation de " + emailTutore + " prête. Départ dans "
                + (depart - maintenant) / 1000 + " secondes, à la minute pile.");
        Thread.sleep(depart - maintenant);

        try {
            g.validerDemande(yohan, demande);
            System.out.println("RESULTAT : acceptation de " + emailTutore + " RÉUSSIE");
        } catch (RegleGestionException e) {
            System.out.println("RESULTAT : acceptation de " + emailTutore + " REFUSÉE -> " + e.getMessage());
        }
    }
}