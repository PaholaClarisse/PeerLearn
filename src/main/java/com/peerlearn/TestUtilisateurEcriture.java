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
import modele.Etudiant;
import modele.Matiere;
import modele.Niveau;
import modele.Utilisateur;
import service.RegleGestionException;

public class TestUtilisateurEcriture {
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
        UtilisateurDAO dao = new UtilisateurDAO();
        Matiere matiere = new MatiereDAO().lister().get(0);
        String email = "test.dao@mail.fr";

        Etudiant e = dao.ajouterEtudiant("Test", "Dao", email, "mdp", Niveau.L3);
        verifier("Ajout : l'id est attribué par la base", e.getId() > 0);

        Utilisateur relu = dao.chercherParEmail(email);
        verifier("La lecture renvoie le bon type : Etudiant", relu instanceof Etudiant);
        verifier("Le niveau est conservé : L3", ((Etudiant) relu).getNiveau() == Niveau.L3);
        verifier("Un nouvel étudiant n'est pas tuteur et n'a pas de matière",
                !((Etudiant) relu).isEstTuteurActif() && ((Etudiant) relu).getMatiere() == null);

        attendreRefus("Email en double", () -> dao.ajouterEtudiant("Autre", "Dao", email, "mdp", Niveau.L1));

        e.choisirMatiere(matiere);
        e.proposerCreneau(DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        e.activerTuteur();
        dao.modifierProfil(e);
        Etudiant profil = (Etudiant) dao.chercherParId(e.getId());
        verifier("Profil enregistré : tuteur actif", profil.isEstTuteurActif());
        verifier("Profil enregistré : matière", profil.getMatiere().getId() == matiere.getId());
        verifier("Profil enregistré : mardi à 18:00",
                profil.getJour() == DayOfWeek.TUESDAY && profil.getHeure().equals(LocalTime.of(18, 0)));
        verifier("Capacité enregistrée : 3", profil.getCapaciteMax() == 3);

        e.mettreEnPause();
        dao.modifierProfil(e);
        Etudiant enPause = (Etudiant) dao.chercherParId(e.getId());
        verifier("Mise en pause : inactif, mais matière et horaire conservés",
                !enPause.isEstTuteurActif() && enPause.getMatiere() != null && enPause.getJour() != null);

        e.retirer();
        dao.modifierProfil(e);
        verifier("Tuteur retiré : le drapeau est enregistré",
                ((Etudiant) dao.chercherParId(e.getId())).isRetire());

        verifier("Suppression du compte de test", dao.supprimer(e.getId()));
        verifier("Le compte n'existe plus", dao.chercherParId(e.getId()) == null);
        verifier("1 utilisateur dans la base", dao.lister().size() == 1);

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
