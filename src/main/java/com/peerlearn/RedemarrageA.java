/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import modele.Etudiant;
import service.GestionTutorat;

public class RedemarrageA {
    public static void main(String[] args) {
        GestionTutorat g = new GestionTutorat();
        Etudiant e = g.inscrire("pahola@mail.fr", "Lago", "Pahola", "motdepasse1");
        g.changerMotDePasse(e, "motdepasse1", "nouveauMdp2026");
        System.out.println("Compte créé (id " + e.getId() + ") : mot de passe changé, une notification enregistrée.");
        System.out.println("Programme terminé. Lancez maintenant RedemarrageB.");
    }
}