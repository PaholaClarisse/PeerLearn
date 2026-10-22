/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */

import dao.MatiereDAO;
import dao.UtilisateurDAO;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import modele.Administrateur;
import modele.Etudiant;
import modele.Matiere;
import service.GestionTutorat;
import service.RegleGestionException;

public class TestProfilTuteur {
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
        MatiereDAO matieres = new MatiereDAO();
        Administrateur admin = (Administrateur) utilisateurs.chercherParEmail("admin@mail.fr");

        ArrayList<Matiere> liste = g.listerMatieres(admin);
        verifier("6 matières listées", liste.size() == 6);
        Matiere m1 = liste.get(0);
        Matiere m2 = liste.get(1);

        Etudiant pahola = g.inscrire("pahola@mail.fr", "Lago", "Pahola", "mdp1");
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "mdp2");

        attendreRefus("Un L2 définit un profil de tuteur",
                () -> g.definirProfilTuteur(pahola, m1, DayOfWeek.TUESDAY, LocalTime.of(18, 0)));
        attendreRefus("Un L3 active son statut sans profil complet", () -> g.activerTuteur(yohan));

        g.definirProfilTuteur(yohan, m1, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        Etudiant profil = (Etudiant) utilisateurs.chercherParId(yohan.getId());
        verifier("Profil enregistré : matière et horaire",
                profil.getMatiere() != null && profil.getMatiere().getId() == m1.getId()
                && profil.getJour() == DayOfWeek.TUESDAY && profil.getHeure().equals(LocalTime.of(18, 0)));
        verifier("Pas encore actif", !profil.isEstTuteurActif());

        g.activerTuteur(yohan);
        verifier("Activation enregistrée",
                ((Etudiant) utilisateurs.chercherParId(yohan.getId())).isEstTuteurActif());

        ArrayList<Etudiant> trouves = g.rechercherTuteurs(pahola, m1);
        verifier("Recherche dans la matière : 1 tuteur, le bon",
                trouves.size() == 1 && trouves.get(0).getId() == yohan.getId());
        verifier("Recherche dans une autre matière : aucun tuteur", g.rechercherTuteurs(pahola, m2).isEmpty());
        verifier("Un tuteur ne se trouve pas lui-même", g.rechercherTuteurs(yohan, m1).isEmpty());
        attendreRefus("Un administrateur active un profil de tuteur", () -> g.activerTuteur(admin));

        g.mettreEnPause(yohan);
        verifier("Mise en pause enregistrée",
                !((Etudiant) utilisateurs.chercherParId(yohan.getId())).isEstTuteurActif());
        verifier("Un tuteur en pause n'apparaît plus", g.rechercherTuteurs(pahola, m1).isEmpty());

        attendreRefus("Un tutoré crée une matière", () -> g.ajouterMatiere(pahola, "Chimie test"));
        Matiere chimie = g.ajouterMatiere(admin, "Chimie test");
        verifier("Matière créée : l'id est attribué par la base", chimie.getId() > 0);
        verifier("7 matières listées", g.listerMatieres(pahola).size() == 7);
        attendreRefus("Matière en double, casse ignorée", () -> g.ajouterMatiere(admin, "CHIMIE TEST"));
        attendreRefus("Nom de matière vide", () -> g.ajouterMatiere(admin, "   "));

        verifier("Suppression de la matière de test", matieres.supprimer(chimie.getId()));
        verifier("Suppression des deux comptes de test",
                utilisateurs.supprimer(pahola.getId()) && utilisateurs.supprimer(yohan.getId()));
        verifier("1 utilisateur dans la base", utilisateurs.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}