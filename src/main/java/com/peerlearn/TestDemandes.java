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

public class TestDemandes {
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
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "mdp3");
        Etudiant alice = g.inscrire("alice@mail.fr", "Martin", "Alice", "mdp4");
        Etudiant lucas = g.inscrire("lucas@mail.fr", "Moreau", "Lucas", "mdp5");
        Etudiant emma = g.inscrire("emma@mail.fr", "Durand", "Emma", "mdp6");

        g.definirProfilTuteur(yohan, m1, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(yohan);
        g.definirProfilTuteur(alice, m2, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(alice);
        g.definirProfilTuteur(lucas, m1, DayOfWeek.THURSDAY, LocalTime.of(10, 0));
        g.activerTuteur(lucas);

        attendreRefus("Demande vers un tuteur inactif", () -> g.envoyerDemande(pahola, emma));

        Demande d1 = g.envoyerDemande(pahola, yohan);
        verifier("Demande envoyée : id attribué et EN_ATTENTE",
                d1.getId() > 0 && d1.getStatut() == StatutDemande.EN_ATTENTE);
        Demande relue = demandes.chercherParId(d1.getId());
        verifier("Relue en base : bon tuteur, bonne matière",
                relue.getTuteur().getId() == yohan.getId() && relue.getMatiere().getId() == m1.getId());

        attendreRefus("2e demande au même tuteur (R5)", () -> g.envoyerDemande(pahola, yohan));
        attendreRefus("Deux tuteurs au même jour et à la même heure (R6)",
                () -> g.envoyerDemande(pahola, alice));
        attendreRefus("Deux tuteurs dans la même matière (R4)", () -> g.envoyerDemande(pahola, lucas));
        attendreRefus("Un administrateur envoie une demande", () -> g.envoyerDemande(admin, yohan));

        g.definirProfilTuteur(alice, m2, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0));
        Demande d2 = g.envoyerDemande(pahola, alice);
        verifier("Deux tuteurs de matières et d'horaires différents (R4)",
                d2.getStatut() == StatutDemande.EN_ATTENTE);

        attendreRefus("Un autre étudiant annule la demande", () -> g.annulerDemande(ines, d2));
        g.annulerDemande(pahola, d2);
        verifier("Annulation enregistrée en base",
                demandes.chercherParId(d2.getId()).getStatut() == StatutDemande.ANNULEE);
        attendreRefus("Annuler deux fois", () -> g.annulerDemande(pahola, d2));

        attendreRefus("Un autre tuteur refuse la demande", () -> g.refuserDemande(alice, d1));
        g.refuserDemande(yohan, d1);
        verifier("Refus enregistré en base",
                demandes.chercherParId(d1.getId()).getStatut() == StatutDemande.REFUSEE);

        Demande d3 = g.envoyerDemande(pahola, yohan);
        verifier("Après un refus, la demande peut être renvoyée",
                d3.getId() != d1.getId() && d3.getStatut() == StatutDemande.EN_ATTENTE);

        d3.accepter();
        demandes.modifierStatut(d3);
        verifier("Yohan a 1 tutoré accepté", g.mesTutores(yohan).size() == 1);

        attendreRefus("Un autre étudiant termine le suivi", () -> g.terminerSuivi(ines, d3));
        g.terminerSuivi(pahola, d3);
        verifier("Suivi terminé et enregistré",
                demandes.chercherParId(d3.getId()).getStatut() == StatutDemande.TERMINEE);
        verifier("La place est libérée : 0 tutoré", g.mesTutores(yohan).isEmpty());
        attendreRefus("Terminer deux fois", () -> g.terminerSuivi(pahola, d3));

        verifier("Yohan a reçu 2 demandes", g.demandesRecues(yohan).size() == 2);
        verifier("Pahola a envoyé 3 demandes", g.demandesEnvoyees(pahola).size() == 3);
        attendreRefus("Un administrateur consulte ses tutorés", () -> g.mesTutores(admin));

        verifier("Suppression des 3 demandes",
                demandes.supprimer(d1.getId()) && demandes.supprimer(d2.getId())
                && demandes.supprimer(d3.getId()));
        verifier("Suppression des 6 comptes de test",
                utilisateurs.supprimer(pahola.getId()) && utilisateurs.supprimer(ines.getId())
                && utilisateurs.supprimer(yohan.getId()) && utilisateurs.supprimer(alice.getId())
                && utilisateurs.supprimer(lucas.getId()) && utilisateurs.supprimer(emma.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);
        verifier("Plus aucune demande", demandes.lister().isEmpty());

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
