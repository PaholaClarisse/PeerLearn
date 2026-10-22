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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.StatutDemande;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;

public class TestValidation {
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
        DemandeDAO demandes = new DemandeDAO();
        ArrayList<Matiere> liste = new MatiereDAO().lister();
        Matiere m1 = liste.get(0);
        Matiere m2 = liste.get(1);
        Utilisateur admin = utilisateurs.chercherParEmail("admin@mail.fr");

        Etudiant pahola = g.inscrire("pahola@mail.fr", "Lago", "Pahola", "mdp1");
        Etudiant ines = g.inscrire("ines@mail.fr", "Girard", "Ines", "mdp2");
        Etudiant hugo = g.inscrire("hugo@mail.fr", "Roux", "Hugo", "mdp3");
        Etudiant chloe = g.inscrire("chloe@mail.fr", "Petit", "Chloe", "mdp4");
        Etudiant dylan = g.inscrire("dylan@mail.fr", "Leroy", "Dylan", "mdp5");
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "mdp6");
        Etudiant alice = g.inscrire("alice@mail.fr", "Martin", "Alice", "mdp7");

        g.definirProfilTuteur(yohan, m1, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(yohan);
        g.definirProfilTuteur(alice, m2, DayOfWeek.THURSDAY, LocalTime.of(10, 0));
        g.activerTuteur(alice);

        Demande dp = g.envoyerDemande(pahola, yohan);
        Demande di = g.envoyerDemande(ines, yohan);
        Demande dh = g.envoyerDemande(hugo, yohan);
        Demande dc = g.envoyerDemande(chloe, yohan);
        Demande dd = g.envoyerDemande(dylan, yohan);
        Demande dpa = g.envoyerDemande(pahola, alice);

        g.definirProfilTuteur(alice, m2, DayOfWeek.TUESDAY, LocalTime.of(18, 0));

        attendreRefus("Un autre tuteur accepte la demande", () -> g.validerDemande(alice, dp));
        attendreRefus("Le tutoré accepte sa propre demande", () -> g.validerDemande(pahola, dp));
        attendreRefus("Un administrateur accepte une demande", () -> g.validerDemande(admin, dp));

        g.validerDemande(yohan, dp);
        verifier("Acceptation enregistrée en base",
                demandes.chercherParId(dp.getId()).getStatut() == StatutDemande.ACCEPTEE);
        attendreRefus("Accepter une demande déjà acceptée", () -> g.validerDemande(yohan, dp));

        attendreRefus("R6 à l'acceptation : le tutoré a déjà un suivi à cet horaire",
                () -> g.validerDemande(alice, dpa));
        verifier("Après le refus R6, la demande reste EN_ATTENTE en base",
                demandes.chercherParId(dpa.getId()).getStatut() == StatutDemande.EN_ATTENTE);

        g.validerDemande(yohan, di);
        g.validerDemande(yohan, dh);
        verifier("Yohan a 3 tutorés acceptés", g.mesTutores(yohan).size() == 3);

        attendreRefus("R3 : une 4e acceptation est refusée", () -> g.validerDemande(yohan, dc));
        verifier("Après le refus R3, la demande reste EN_ATTENTE (rollback)",
                demandes.chercherParId(dc.getId()).getStatut() == StatutDemande.EN_ATTENTE);

        g.terminerSuivi(ines, di);
        verifier("Un suivi terminé libère une place", g.mesTutores(yohan).size() == 2);
        g.validerDemande(yohan, dc);
        verifier("La place libérée permet d'accepter la demande suivante", g.mesTutores(yohan).size() == 3);

        verifier("Suppression des 6 demandes",
                demandes.supprimer(dp.getId()) && demandes.supprimer(di.getId())
                && demandes.supprimer(dh.getId()) && demandes.supprimer(dc.getId())
                && demandes.supprimer(dd.getId()) && demandes.supprimer(dpa.getId()));
        verifier("Suppression des 7 comptes de test",
                utilisateurs.supprimer(pahola.getId()) && utilisateurs.supprimer(ines.getId())
                && utilisateurs.supprimer(hugo.getId()) && utilisateurs.supprimer(chloe.getId())
                && utilisateurs.supprimer(dylan.getId()) && utilisateurs.supprimer(yohan.getId())
                && utilisateurs.supprimer(alice.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);
        verifier("Plus aucune demande", demandes.lister().isEmpty());

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
