/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */

import service.RegleGestionException;
import util.MotDePasse;

public class TestMotDePasse {
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
        String h1 = MotDePasse.hacher("secret123");
        String h2 = MotDePasse.hacher("secret123");

        verifier("Le hachage ne contient pas le mot de passe en clair", !h1.contains("secret123"));
        verifier("Format : 3 parties séparées par $", h1.split("\\$").length == 3);
        verifier("Le hachage tient dans la colonne (255 caractères)", h1.length() <= 255);
        verifier("Deux hachages du même mot de passe sont différents (sel)", !h1.equals(h2));
        verifier("Le bon mot de passe est accepté", MotDePasse.verifier("secret123", h1));
        verifier("Le bon mot de passe est accepté avec l'autre hachage", MotDePasse.verifier("secret123", h2));
        verifier("Un mauvais mot de passe est refusé", !MotDePasse.verifier("Secret123", h1));
        verifier("Un mot de passe vide est refusé", !MotDePasse.verifier("", h1));
        verifier("Un texte qui n'est pas un hachage est refusé", !MotDePasse.verifier("secret123", "secret123"));
        verifier("Un hachage altéré est refusé",
                !MotDePasse.verifier("secret123", h1.substring(0, h1.length() - 3) + "AAA"));
        verifier("Les valeurs null sont refusées",
                !MotDePasse.verifier(null, h1) && !MotDePasse.verifier("secret123", null));
        attendreRefus("Hacher un mot de passe vide", () -> MotDePasse.hacher(""));

        System.out.println("Exemple de hachage stocké : " + h1);
        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}