/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import dao.UtilisateurDAO;
import java.util.Scanner;
import modele.Etudiant;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;

public class TestMysqlArrete {
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

    private static void essayer(String titre, Runnable action) {
        try {
            action.run();
            echecs++;
            System.out.println("ECHEC : " + titre + " n'a pas échoué alors que MySQL est arrêté");
        } catch (RegleGestionException e) {
            ok++;
            System.out.println("OK    : " + titre + " -> " + e.getMessage());
        } catch (RuntimeException e) {
            echecs++;
            System.out.println("PLANTAGE : " + titre + " -> " + e.getClass().getSimpleName()
                    + " : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner clavier = new Scanner(System.in);
        GestionTutorat g = new GestionTutorat();
        Utilisateur admin = new UtilisateurDAO().chercherParEmail("admin@mail.fr");

        verifier("MySQL démarré : les 6 matières se chargent", g.listerMatieres(admin).size() == 6);

        System.out.println(">>> Arrêtez maintenant le service MySQL (services.msc), puis cliquez ici et appuyez sur Entrée.");
        clavier.nextLine();

        essayer("Charger les matières, MySQL arrêté", () -> g.listerMatieres(admin));
        essayer("Inscription, MySQL arrêté",
                () -> g.inscrire("pahola@mail.fr", "Lago", "Pahola", "motdepasse1"));
        essayer("Connexion, MySQL arrêté", () -> g.connecter("admin@mail.fr", "peuImporte"));
        essayer("Statistiques, MySQL arrêté", () -> g.nombreTuteursActifs(admin));

        System.out.println(">>> Redémarrez MySQL, attendez « En cours d'exécution », puis cliquez ici et appuyez sur Entrée.");
        clavier.nextLine();

        verifier("MySQL redémarré : les matières se chargent à nouveau", g.listerMatieres(admin).size() == 6);
        Etudiant e = g.inscrire("pahola@mail.fr", "Lago", "Pahola", "motdepasse1");
        verifier("MySQL redémarré : l'inscription fonctionne", e.getId() > 0);
        verifier("Compte de test supprimé", new UtilisateurDAO().supprimer(e.getId()));

        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}
