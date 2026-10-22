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
import modele.Etudiant;
import modele.Utilisateur;

public class RaceVerif {
    public static void main(String[] args) {
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        DemandeDAO demandes = new DemandeDAO();
        Utilisateur yohan = utilisateurs.chercherParEmail("yohan@mail.fr");

        int acceptes = demandes.listerTutoresAcceptes(yohan.getId()).size();
        System.out.println("Tutorés acceptés de Yohan : " + acceptes + " (attendu : 3)");
        System.out.println(acceptes == 3 ? "OK    : la capacité de 3 est respectée"
                                         : "ECHEC : la capacité est dépassée ou non atteinte");

        for (Demande d : demandes.lister()) {
            demandes.supprimer(d.getId());
        }
        for (Utilisateur u : utilisateurs.lister()) {
            if (u instanceof Etudiant) {
                utilisateurs.supprimer(u.getId());
            }
        }
        System.out.println("Base nettoyée : " + utilisateurs.lister().size() + " utilisateur (attendu : 1)");
    }
}
