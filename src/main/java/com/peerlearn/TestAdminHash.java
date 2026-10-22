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
import modele.Utilisateur;
import util.MotDePasse;

public class TestAdminHash {
    public static void main(String[] args) {
        Utilisateur admin = new UtilisateurDAO().chercherParEmail("admin@mail.fr");
        System.out.println("Administrateur trouvé : " + (admin != null));

        System.out.print("Saisis le mot de passe de l'administrateur : ");
        String saisi = new Scanner(System.in).nextLine();

        System.out.println("Mot de passe saisi accepté : "
                + MotDePasse.verifier(saisi, admin.getMdp()) + " (attendu : true)");
        System.out.println("Ancien mot de passe admin123 accepté : "
                + MotDePasse.verifier("admin123", admin.getMdp()) + " (attendu : false)");
        System.out.println("Hachage stocké illisible : "
                + !admin.getMdp().contains(saisi) + " (attendu : true)");
    }
}