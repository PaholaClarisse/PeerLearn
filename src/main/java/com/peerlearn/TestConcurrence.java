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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.StatutDemande;
import service.GestionTutorat;
import service.RegleGestionException;

public class TestConcurrence {
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

    private static Thread lancer(GestionTutorat g, Etudiant tuteur, Demande d, CountDownLatch depart,
                                 AtomicInteger succes, AtomicInteger refus, AtomicReference<String> message) {
        return new Thread(() -> {
            try {
                depart.await();
                g.validerDemande(tuteur, d);
                succes.incrementAndGet();
            } catch (RegleGestionException e) {
                refus.incrementAndGet();
                message.set(e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        GestionTutorat g = new GestionTutorat();
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        DemandeDAO demandes = new DemandeDAO();
        Matiere m1 = new MatiereDAO().lister().get(0);

        Etudiant pahola = g.inscrire("pahola@mail.fr", "Lago", "Pahola", "mdp1");
        Etudiant ines = g.inscrire("ines@mail.fr", "Girard", "Ines", "mdp2");
        Etudiant hugo = g.inscrire("hugo@mail.fr", "Roux", "Hugo", "mdp3");
        Etudiant chloe = g.inscrire("chloe@mail.fr", "Petit", "Chloe", "mdp4");
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "mdp5");

        g.definirProfilTuteur(yohan, m1, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(yohan);

        Demande dp = g.envoyerDemande(pahola, yohan);
        Demande di = g.envoyerDemande(ines, yohan);
        Demande dh = g.envoyerDemande(hugo, yohan);
        Demande dc = g.envoyerDemande(chloe, yohan);
        g.validerDemande(yohan, dp);
        g.validerDemande(yohan, di);
        verifier("2 tutorés acceptés au départ : il reste une seule place", g.mesTutores(yohan).size() == 2);

        // Deux "fenêtres" distinctes, qui acceptent chacune une demande différente en même temps
        GestionTutorat fenetre1 = new GestionTutorat();
        GestionTutorat fenetre2 = new GestionTutorat();
        CountDownLatch depart = new CountDownLatch(1);
        AtomicInteger succes = new AtomicInteger();
        AtomicInteger refus = new AtomicInteger();
        AtomicReference<String> message = new AtomicReference<>("");

        Thread t1 = lancer(fenetre1, yohan, dh, depart, succes, refus, message);
        Thread t2 = lancer(fenetre2, yohan, dc, depart, succes, refus, message);
        t1.start();
        t2.start();
        depart.countDown();
        t1.join();
        t2.join();

        verifier("Exactement 1 acceptation réussit", succes.get() == 1);
        verifier("Exactement 1 acceptation est refusée", refus.get() == 1);
        verifier("Le refus est bien celui de la capacité : " + message.get(),
                message.get().contains("Capacité"));
        verifier("Yohan a exactement 3 tutorés en base",
                demandes.listerTutoresAcceptes(yohan.getId()).size() == 3);

        StatutDemande s1 = demandes.chercherParId(dh.getId()).getStatut();
        StatutDemande s2 = demandes.chercherParId(dc.getId()).getStatut();
        verifier("Une seule des deux demandes est ACCEPTEE",
                (s1 == StatutDemande.ACCEPTEE) != (s2 == StatutDemande.ACCEPTEE));
        verifier("L'autre reste EN_ATTENTE",
                s1 == StatutDemande.EN_ATTENTE || s2 == StatutDemande.EN_ATTENTE);

        verifier("Suppression des 4 demandes",
                demandes.supprimer(dp.getId()) && demandes.supprimer(di.getId())
                && demandes.supprimer(dh.getId()) && demandes.supprimer(dc.getId()));
        verifier("Suppression des 5 comptes de test",
                utilisateurs.supprimer(pahola.getId()) && utilisateurs.supprimer(ines.getId())
                && utilisateurs.supprimer(hugo.getId()) && utilisateurs.supprimer(chloe.getId())
                && utilisateurs.supprimer(yohan.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);
        verifier("Plus aucune demande", demandes.lister().isEmpty());

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}