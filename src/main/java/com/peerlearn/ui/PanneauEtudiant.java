package com.peerlearn.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import modele.Etudiant;
import service.GestionTutorat;

/**
 * Écran de tout étudiant : L1/L2 = espace tutoré, L3 et plus = espace tuteur.
 * Décision actuelle : un tuteur n'est pas tutoré, les deux espaces ne se mélangent pas.
 */
public class PanneauEtudiant extends JPanel {

    public PanneauEtudiant(Fenetre fenetre, GestionTutorat service, Etudiant etu) {
        setLayout(new BorderLayout());

        Navigation nav = new Navigation(Libelles.nom(etu), "Étudiant, " + etu.getNiveau(),
                fenetre::afficherConnexion, () -> service.nombreNotificationsNonLues(etu));

        nav.section("Accueil");
        nav.ajouter("Tableau de bord", new TableauDeBord(service, etu, nav));

        if (peutTutorer(etu)) {
            // Un tuteur n'est pas tutoré (choix actuel) : il n'a que l'espace tuteur
            nav.section("Espace tuteur");
            nav.ajouter("Mon profil tuteur", new PanneauProfilTuteur(service, etu));
            nav.ajouter("Demandes reçues", new PanneauDemandesRecues(service, etu));
            nav.ajouter("Séances", new PanneauSeances(service, etu));
            nav.ajouter("Messagerie", new PanneauMessagerie(service, etu));
            nav.ajouter("Exercices publiés", new PanneauExercicesPublies(service, etu));
        } else {
            nav.section("Espace tutoré");
            nav.ajouter("Trouver un tuteur", new PanneauRechercheTuteur(service, etu));
            nav.ajouter("Mes demandes", new PanneauMesDemandes(service, etu));
            nav.ajouter("Messagerie", new PanneauMessagerie(service, etu));
            nav.ajouter("Exercices", new PanneauExercices(service, etu));
            nav.ajouter("Progression", new PanneauProgression(service, etu));
        }

        nav.section("Compte");
        PanneauNotifications notifications = new PanneauNotifications(service, etu);
        notifications.setApresLecture(nav::rafraichirBadge);
        nav.ajouter("Notifications", notifications);
        nav.ajouter("Mon compte", new PanneauCompte(service, etu));
        nav.suivreNotifications("Notifications");

        add(nav, BorderLayout.CENTER);
        nav.demarrer();
    }

    private static boolean peutTutorer(Etudiant e) {
        return e.getNiveau().peutTutorer();
    }
}
