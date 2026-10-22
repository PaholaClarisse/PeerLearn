/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Glamor Store
 */
package modele;

import java.time.LocalDateTime;
import service.RegleGestionException;

public class Message {
    private int id;
    private Demande demande;
    private Etudiant expediteur;
    private String contenu;
    private LocalDateTime dateEnvoi;

    public Message(Demande demande, Etudiant expediteur, String contenu) {
        if (demande == null || expediteur == null) {
            throw new RegleGestionException("Un message doit avoir une demande et un expéditeur.");
        }
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new RegleGestionException("Le message ne peut pas être vide.");
        }
        this.demande = demande;
        this.expediteur = expediteur;
        this.contenu = contenu.trim();
        this.dateEnvoi = LocalDateTime.now();
    }
    public Message(int id, Demande demande, Etudiant expediteur, String contenu, LocalDateTime dateEnvoi) {
    this.id = id;
    this.demande = demande;
    this.expediteur = expediteur;
    this.contenu = contenu;
    this.dateEnvoi = dateEnvoi;
}

public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}

    public Demande getDemande() {
        return demande;
    }

    public Etudiant getExpediteur() {
        return expediteur;
    }

    public String getContenu() {
        return contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }
}