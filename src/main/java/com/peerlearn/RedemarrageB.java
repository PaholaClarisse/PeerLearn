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
import modele.Utilisateur;
import service.GestionTutorat;

public class RedemarrageB {
    public static void main(String[] args) {
        GestionTutorat g = new GestionTutorat();
        Utilisateur u = g.connecter("pahola@mail.fr", "nouveauMdp2026");
        System.out.println("Connexion après redémarrage réussie : " + u.getNom());
        System.out.println("Notifications : " + g.mesNotifications(u));
        System.out.println("Non lues : " + g.nombreNotificationsNonLues(u) + " (attendu : 1)");
        new UtilisateurDAO().supprimer(u.getId());
        System.out.println("Compte de test supprimé.");
    }
}
