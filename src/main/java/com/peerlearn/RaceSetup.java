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
import java.time.DayOfWeek;
import java.time.LocalTime;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import service.GestionTutorat;

public class RaceSetup {
    public static void main(String[] args) {
        GestionTutorat g = new GestionTutorat();
        Matiere m = g.listerMatieres(new UtilisateurDAO().chercherParEmail("admin@mail.fr")).get(0);

        Etudiant pahola = g.inscrire("pahola@mail.fr", "Lago", "Pahola", "motdepasse1");
        Etudiant ines = g.inscrire("ines@mail.fr", "Girard", "Ines", "motdepasse2");
        Etudiant hugo = g.inscrire("hugo@mail.fr", "Roux", "Hugo", "motdepasse3");
        Etudiant chloe = g.inscrire("chloe@mail.fr", "Petit", "Chloe", "motdepasse4");
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "motdepasse5");

        g.definirProfilTuteur(yohan, m, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(yohan);

        Demande d1 = g.envoyerDemande(pahola, yohan);
        Demande d2 = g.envoyerDemande(ines, yohan);
        g.envoyerDemande(hugo, yohan);
        g.envoyerDemande(chloe, yohan);
        g.validerDemande(yohan, d1);
        g.validerDemande(yohan, d2);

        System.out.println("Prêt : Yohan a 2 tutorés, il reste une seule place.");
        System.out.println("Demandes en attente : Hugo et Chloé.");
    }
}