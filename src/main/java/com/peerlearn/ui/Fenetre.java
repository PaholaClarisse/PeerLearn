package com.peerlearn.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import modele.Administrateur;
import modele.Etudiant;
import modele.Utilisateur;
import service.GestionTutorat;

/**
 * Fenêtre unique de l'application. Un CardLayout alterne entre
 * l'écran de connexion et l'écran d'accueil du rôle connecté.
 */
public class Fenetre extends JFrame {

    private static final String CONNEXION = "connexion";
    private static final String INSCRIPTION = "inscription";
    private static final String ACCUEIL = "accueil";

    private final GestionTutorat service;
    private final CardLayout cartes = new CardLayout();
    private final JPanel racine = new JPanel(cartes);
    private PanneauConnexion connexion;
    private PanneauInscription inscription;
    private JPanel accueilCourant;

    public Fenetre(GestionTutorat service) {
        super("PeerLearn");
        this.service = service;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 640));
        setPreferredSize(new Dimension(1120, 700));
        setContentPane(racine);
        connexion = new PanneauConnexion(this, service);
        racine.add(connexion, CONNEXION);
        inscription = new PanneauInscription(this, service);
        racine.add(inscription, INSCRIPTION);
        afficherConnexion();
        pack();
        setLocationRelativeTo(null);
    }

    public void afficherInscription() {
        inscription.reinitialiser();
        setTitle("PeerLearn - Créer un compte");
        cartes.show(racine, INSCRIPTION);
    }

    /** Retour à la connexion avec l'adresse déjà saisie (après une inscription réussie). */
    public void afficherConnexion(String email) {
        connexion.preremplir(email);
        afficherConnexion();
    }

    public void afficherConnexion() {
        // On détruit l'écran d'accueil : ses minuteurs (messagerie, compteur) s'arrêtent avec lui.
        if (accueilCourant != null) {
            racine.remove(accueilCourant);
            accueilCourant = null;
        }
        setTitle("PeerLearn - Connexion");
        cartes.show(racine, CONNEXION);
        racine.revalidate();
        racine.repaint();
    }

    /** Choisit l'écran avec instanceof, comme le prévoit le contrat. */
    public void afficherAccueil(Utilisateur connecte) {
        if (accueilCourant != null) {
            racine.remove(accueilCourant);
        }
        if (connecte instanceof Administrateur) {
            accueilCourant = new PanneauAdmin(this, service, connecte);
        } else if (connecte instanceof Etudiant) {
            accueilCourant = new PanneauEtudiant(this, service, (Etudiant) connecte);
        } else {
            Taches.erreur(this, "Rôle inconnu.");
            return;
        }
        racine.add(accueilCourant, ACCUEIL);
        setTitle("PeerLearn");
        cartes.show(racine, ACCUEIL);
        racine.revalidate();
        racine.repaint();
    }
}
