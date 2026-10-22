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
import dao.NotificationDAO;
import dao.UtilisateurDAO;
import java.time.DayOfWeek;
import java.time.LocalTime;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.StatutDemande;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;

public class TestRetraitTuteur {
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
        NotificationDAO notifications = new NotificationDAO();
        Matiere m1 = new MatiereDAO().lister().get(0);
        Utilisateur admin = utilisateurs.chercherParEmail("admin@mail.fr");

        Etudiant hugo = g.inscrire("hugo@mail.fr", "Roux", "Hugo", "mdp1");
        Etudiant chloe = g.inscrire("chloe@mail.fr", "Petit", "Chloe", "mdp2");
        Etudiant dylan = g.inscrire("dylan@mail.fr", "Leroy", "Dylan", "mdp3");
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "mdp4");
        Etudiant alice = g.inscrire("alice@mail.fr", "Martin", "Alice", "mdp5");

        g.definirProfilTuteur(yohan, m1, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(yohan);
        Demande dh = g.envoyerDemande(hugo, yohan);
        Demande dc = g.envoyerDemande(chloe, yohan);
        Demande dd = g.envoyerDemande(dylan, yohan);
        g.validerDemande(yohan, dh);
        g.validerDemande(yohan, dc);
        verifier("Yohan a 2 tutorés avant le retrait", g.mesTutores(yohan).size() == 2);

        attendreRefus("Un tutoré retire un tuteur", () -> g.retirerTuteur(hugo, yohan));
        attendreRefus("Retirer un tuteur nul", () -> g.retirerTuteur(admin, null));

        g.retirerTuteur(admin, yohan);
        Etudiant relu = (Etudiant) utilisateurs.chercherParId(yohan.getId());
        verifier("Tuteur retiré et inactif en base", relu.isRetire() && !relu.isEstTuteurActif());
        verifier("Les suivis acceptés passent à TERMINEE",
                demandes.chercherParId(dh.getId()).getStatut() == StatutDemande.TERMINEE
                && demandes.chercherParId(dc.getId()).getStatut() == StatutDemande.TERMINEE);
        verifier("La demande en attente passe à REFUSEE",
                demandes.chercherParId(dd.getId()).getStatut() == StatutDemande.REFUSEE);
        verifier("Plus aucun tutoré", g.mesTutores(yohan).isEmpty());
        verifier("Le profil (matière et horaire) est conservé",
                relu.getMatiere() != null && relu.getJour() != null);

        attendreRefus("Un tuteur retiré ne peut plus s'activer", () -> g.activerTuteur(yohan));
        verifier("Un tuteur retiré n'apparaît plus dans la recherche",
                g.rechercherTuteurs(alice, m1).isEmpty());
        verifier("Hugo a été prévenu du retrait",
                g.mesNotifications(hugo).stream().anyMatch(m -> m.contains("retiré")));
        verifier("Chloé aussi",
                g.mesNotifications(chloe).stream().anyMatch(m -> m.contains("retiré")));
        verifier("Dylan, dont la demande était en attente, n'a pas de message de retrait",
                g.mesNotifications(dylan).stream().noneMatch(m -> m.contains("retiré")));
        attendreRefus("Retirer deux fois le même tuteur", () -> g.retirerTuteur(admin, yohan));

        notifications.supprimerParUtilisateur(hugo.getId());
        notifications.supprimerParUtilisateur(chloe.getId());
        notifications.supprimerParUtilisateur(dylan.getId());
        notifications.supprimerParUtilisateur(yohan.getId());
        notifications.supprimerParUtilisateur(alice.getId());
        verifier("Suppression des 3 demandes",
                demandes.supprimer(dh.getId()) && demandes.supprimer(dc.getId()) && demandes.supprimer(dd.getId()));
        verifier("Suppression des 5 comptes de test",
                utilisateurs.supprimer(hugo.getId()) && utilisateurs.supprimer(chloe.getId())
                && utilisateurs.supprimer(dylan.getId()) && utilisateurs.supprimer(yohan.getId())
                && utilisateurs.supprimer(alice.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
