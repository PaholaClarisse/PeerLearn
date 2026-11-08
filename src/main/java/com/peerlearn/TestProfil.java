/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import modele.Profil;
import service.ProfilService;
import service.RegleGestionException;

public class TestProfil {
    public static void main(String[] args) {
        ProfilService service = new ProfilService();
        int idUser = 1;   // À REMPLACER par un id existant dans la table utilisateurs

        try {
            // Test 1 : création (ou modification si le profil existe déjà)
            service.enregistrerProfil(idUser, "Étudiante en informatique", "Informatique L2", "profils/1.png");
            Profil p = service.obtenirProfil(idUser);
            System.out.println("Test 1 : " + p.getDescription() + " | " + p.getFiliere()
                    + " | " + p.getPhoto() + " | date_maj = " + p.getDateModification());

            // Test 2 : modification
            service.enregistrerProfil(idUser, "Description modifiée", "Informatique L2", null);
            p = service.obtenirProfil(idUser);
            System.out.println("Test 2 : " + p.getDescription() + " | date_maj = " + p.getDateModification());

            // Test 3 : extension interdite (doit lever une exception)
            service.enregistrerProfil(idUser, "Test", "Informatique L2", "document.pdf");
            System.out.println("Test 3 : ERREUR, l'exception n'a pas été levée");
        } catch (RegleGestionException e) {
            System.out.println("Exception attendue au test 3 : " + e.getMessage());
        }
    }
}