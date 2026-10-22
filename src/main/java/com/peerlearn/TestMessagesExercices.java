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
import dao.EvaluationDAO;
import dao.ExerciceDAO;
import dao.MatiereDAO;
import dao.MessageDAO;
import dao.NotificationDAO;
import dao.RenduDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import modele.Demande;
import modele.Etudiant;
import modele.EvaluationTuteur;
import modele.Exercice;
import modele.Matiere;
import modele.Message;
import modele.Rendu;
import modele.SeanceTutorat;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;

public class TestMessagesExercices {
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
        SeanceDAO seances = new SeanceDAO();
        MessageDAO messages = new MessageDAO();
        EvaluationDAO evaluations = new EvaluationDAO();
        ExerciceDAO exercices = new ExerciceDAO();
        RenduDAO rendus = new RenduDAO();
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
        SeanceTutorat s1 = g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(1), 60);
        g.enregistrerPresence(yohan, s1, hugo);

        // Messagerie (R7)
        attendreRefus("Message dans une demande non acceptée (R7)",
                () -> g.envoyerMessage(dylan, dd, "Bonjour"));
        Message msg1 = g.envoyerMessage(hugo, dh, "Bonjour, quand commence-t-on ?");
        Message msg2 = g.envoyerMessage(yohan, dh, "Mardi à 18 h.");
        verifier("Conversation de 2 messages dans l'ordre",
                g.consulterConversation(hugo, dh).size() == 2
                && g.consulterConversation(hugo, dh).get(0).getId() == msg1.getId());
        attendreRefus("Un tiers écrit dans la conversation", () -> g.envoyerMessage(alice, dh, "Je m'incruste"));
        attendreRefus("Un tiers lit la conversation", () -> g.consulterConversation(alice, dh));
        attendreRefus("Message vide", () -> g.envoyerMessage(hugo, dh, "   "));
        attendreRefus("Un administrateur écrit dans une conversation", () -> g.envoyerMessage(admin, dh, "Test"));

        // Évaluation du tuteur
        attendreRefus("Évaluer un tuteur sans avoir assisté à une séance",
                () -> g.evaluerTuteur(chloe, dc, 5, "Super"));
        attendreRefus("Évaluer le suivi d'un autre étudiant", () -> g.evaluerTuteur(hugo, dc, 4, "Test"));
        attendreRefus("Note de 7 pour un tuteur", () -> g.evaluerTuteur(hugo, dh, 7, "Test"));
        EvaluationTuteur eval = g.evaluerTuteur(hugo, dh, 4, "Explications claires.");
        verifier("Yohan voit sa moyenne : 4.0", Double.valueOf(4.0).equals(g.consulterMaMoyenne(yohan)));
        verifier("Pas d'évaluation : pas de moyenne", g.consulterMaMoyenne(alice) == null);
        attendreRefus("Évaluer deux fois le même suivi", () -> g.evaluerTuteur(hugo, dh, 5, "Encore"));
        attendreRefus("Un tutoré lit toutes les évaluations", () -> g.consulterEvaluations(hugo));
        verifier("L'administrateur voit 1 évaluation", g.consulterEvaluations(admin).size() == 1);
        verifier("Évaluation moyenne de Sokegbe : 4.0",
                Double.valueOf(4.0).equals(g.moyenneEvaluationsParTuteur(admin).get("Sokegbe")));

        // Statistiques
        g.refuserDemande(yohan, dd);
        verifier("Taux d'acceptation : 2 acceptées sur 3 traitées",
                Math.abs(g.tauxAcceptation(admin) - 2.0 / 3) < 1e-9);
        attendreRefus("Un tutoré lit le taux d'acceptation", () -> g.tauxAcceptation(hugo));

        // Exercices et rendus
        attendreRefus("Publier un exercice sans être tuteur", () -> g.publierExercice(hugo, "TD1", "td1.pdf"));
        attendreRefus("Exercice au format interdit", () -> g.publierExercice(yohan, "TD1", "td1.exe"));
        attendreRefus("Exercice sans titre", () -> g.publierExercice(yohan, "   ", "td1.pdf"));
        Exercice td1 = g.publierExercice(yohan, "TD1 : les boucles", "exercices/td1.pdf");
        verifier("Exercice publié : l'id est attribué", td1.getId() > 0);
        verifier("Hugo voit l'exercice", g.consulterExercices(hugo).size() == 1);
        verifier("Dylan (non suivi) ne le voit pas", g.consulterExercices(dylan).isEmpty());
        attendreRefus("Rendu d'un tutoré non suivi", () -> g.deposerRendu(dylan, td1, "rendu.pdf"));
        attendreRefus("Rendu au format interdit", () -> g.deposerRendu(hugo, td1, "rendu.zip"));
        Rendu rendu = g.deposerRendu(hugo, td1, "rendus/hugo_td1.docx");
        verifier("Rendu déposé : l'id est attribué", rendu.getId() > 0);
        attendreRefus("Deuxième rendu du même exercice", () -> g.deposerRendu(hugo, td1, "rendus/v2.pdf"));
        verifier("Yohan voit 1 rendu", g.consulterRendus(yohan, td1).size() == 1);
        attendreRefus("Un tutoré lit les rendus", () -> g.consulterRendus(hugo, td1));

        // Notifications en base
        verifier("Hugo a été prévenu de l'acceptation",
                g.mesNotifications(hugo).stream().anyMatch(m -> m.contains("acceptée")));
        verifier("Yohan a été prévenu du rendu",
                g.mesNotifications(yohan).stream().anyMatch(m -> m.contains("a rendu l'exercice")));
        verifier("Hugo a des notifications non lues", g.nombreNotificationsNonLues(hugo) > 0);
        int avant = g.mesNotifications(hugo).size();
        g.marquerNotificationsLues(hugo);
        verifier("Plus aucune notification non lue", g.nombreNotificationsNonLues(hugo) == 0);
        verifier("Les notifications survivent à un redémarrage (nouvelle instance)",
                new GestionTutorat().mesNotifications(hugo).size() == avant);

        // Nettoyage
        notifications.supprimerParUtilisateur(hugo.getId());
        notifications.supprimerParUtilisateur(chloe.getId());
        notifications.supprimerParUtilisateur(dylan.getId());
        notifications.supprimerParUtilisateur(yohan.getId());
        notifications.supprimerParUtilisateur(alice.getId());
        verifier("Suppression du rendu", rendus.supprimer(rendu.getId()));
        verifier("Suppression de l'exercice", exercices.supprimer(td1.getId()));
        verifier("Suppression de l'évaluation", evaluations.supprimer(eval.getId()));
        verifier("Suppression des 2 messages", messages.supprimer(msg1.getId()) && messages.supprimer(msg2.getId()));
        verifier("Suppression de la séance", seances.supprimer(s1.getId()));
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
