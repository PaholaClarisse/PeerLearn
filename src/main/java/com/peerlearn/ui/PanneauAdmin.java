package com.peerlearn.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import modele.Utilisateur;
import service.GestionTutorat;

/** Écran de l'administration : supervision du dispositif. */
public class PanneauAdmin extends JPanel {

    public PanneauAdmin(Fenetre fenetre, GestionTutorat service, Utilisateur admin) {
        setLayout(new BorderLayout());

        Navigation nav = new Navigation("Administrateur", "Gestion de la plateforme",
                fenetre::afficherConnexion, () -> service.nombreNotificationsNonLues(admin));

        nav.section("Accueil");
        nav.ajouter("Tableau de bord", new TableauDeBord(service, admin, nav));

        nav.section("Supervision");
        nav.ajouter("Statistiques", new PanneauStatistiques(service, admin));
        nav.ajouter("Rapports", new PanneauRapports(service, admin));
        nav.ajouter("Évaluations", new PanneauEvaluations(service, admin));

        nav.section("Gestion");
        nav.ajouter("Matières", new PanneauMatieres(service, admin));
        nav.ajouter("Tuteurs", new PanneauTuteurs(service, admin));

        nav.section("Compte");
        PanneauNotifications notifications = new PanneauNotifications(service, admin);
        notifications.setApresLecture(nav::rafraichirBadge);
        nav.ajouter("Notifications", notifications);
        nav.ajouter("Mon compte", new PanneauCompte(service, admin));
        nav.suivreNotifications("Notifications");

        add(nav, BorderLayout.CENTER);
        nav.demarrer();
    }
}
