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
import dao.MessageDAO;
import dao.UtilisateurDAO;
import java.time.LocalDate;
import java.util.ArrayList;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.Message;
import modele.Niveau;
import modele.StatutDemande;
import service.RegleGestionException;

public class TestMessageDAO {
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
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        DemandeDAO demandes = new DemandeDAO();
        MessageDAO dao = new MessageDAO();
        Matiere matiere = new MatiereDAO().lister().get(0);

        Etudiant tutore = utilisateurs.ajouterEtudiant("Test", "Tutore", "test.m1@mail.fr", "mdp", Niveau.L1);
        Etudiant tuteur = utilisateurs.ajouterEtudiant("Test", "Tuteur", "test.m2@mail.fr", "mdp", Niveau.L3);
        Demande d = demandes.ajouter(new Demande(tutore, tuteur, matiere));

        verifier("Aucun message au départ", dao.listerParDemande(d).isEmpty());

        Message m1 = dao.ajouter(new Message(d, tutore, "Bonjour, quand commence-t-on ?"));
        verifier("Message inséré : l'id est attribué", m1.getId() > 0);

        Message relu = dao.chercherParId(m1.getId());
        verifier("Relecture : contenu conservé",
                relu != null && relu.getContenu().equals("Bonjour, quand commence-t-on ?"));
        verifier("Relecture : expéditeur et demande conservés",
                relu.getExpediteur().getId() == tutore.getId() && relu.getDemande().getId() == d.getId());

        Message m2 = dao.ajouter(new Message(d, tuteur, "Mardi à 18 h. L'exercice : ' OR '1'='1"));
        verifier("Apostrophes et texte d'injection stockés comme simple texte",
                dao.chercherParId(m2.getId()).getContenu().equals("Mardi à 18 h. L'exercice : ' OR '1'='1"));

        ArrayList<Message> conversation = dao.listerParDemande(d);
        verifier("Conversation : 2 messages dans l'ordre d'envoi",
                conversation.size() == 2
                && conversation.get(0).getId() == m1.getId()
                && conversation.get(1).getId() == m2.getId());

        Demande fantome = new Demande(99999, tutore, tuteur, matiere, LocalDate.now(), StatutDemande.ACCEPTEE);
        attendreRefus("Message vers une demande inexistante refusé par la base",
                () -> dao.ajouter(new Message(fantome, tutore, "Bonjour")));

        verifier("Id inconnu : null", dao.chercherParId(99999) == null);
        verifier("Suppression du premier message", dao.supprimer(m1.getId()));
        verifier("Suppression du second message", dao.supprimer(m2.getId()));
        verifier("Plus aucun message", dao.listerParDemande(d).isEmpty());
        verifier("Suppression de la demande", demandes.supprimer(d.getId()));
        verifier("Suppression des deux comptes de test",
                utilisateurs.supprimer(tutore.getId()) && utilisateurs.supprimer(tuteur.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}