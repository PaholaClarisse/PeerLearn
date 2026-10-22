/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;

import java.time.LocalDate;
import service.RegleGestionException;

/**
 *
 * @author Glamor Store
 */
public class Demande {
    private int id;
    private Etudiant tutore;
    private Etudiant tuteur;
    private Matiere matiere;
    private LocalDate dateDemande;
    private StatutDemande statut;

    public Demande(Etudiant tutore, Etudiant tuteur, Matiere matiere) {
        if (tutore == null || tuteur == null || matiere == null) {
            throw new RegleGestionException("Une demande doit avoir un tutoré, un tuteur et une matière.");
        }
        if (tutore == tuteur) {
            throw new RegleGestionException("Un étudiant ne peut pas se demander à lui-même.");
        }
        this.tutore = tutore;
        this.tuteur = tuteur;
        this.matiere = matiere;
        this.dateDemande = LocalDate.now();
        this.statut = StatutDemande.EN_ATTENTE;
    }
    public Demande(int id, Etudiant tutore, Etudiant tuteur, Matiere matiere,
               LocalDate dateDemande, StatutDemande statut) {
    this.id = id;
    this.tutore = tutore;
    this.tuteur = tuteur;
    this.matiere = matiere;
    this.dateDemande = dateDemande;
    this.statut = statut;
}

public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}

    public Etudiant getTutore() {
        return tutore;
    }

    public Etudiant getTuteur() {
        return tuteur;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public LocalDate getDateDemande() {
        return dateDemande;
    }

    public StatutDemande getStatut() {
        return statut;
    }
    public void accepter() {
    verifierStatut(StatutDemande.EN_ATTENTE, "Seule une demande en attente peut être acceptée.");
    statut = StatutDemande.ACCEPTEE;
}

public void refuser() {
    verifierStatut(StatutDemande.EN_ATTENTE, "Seule une demande en attente peut être refusée.");
    statut = StatutDemande.REFUSEE;
}

public void annuler() {
    verifierStatut(StatutDemande.EN_ATTENTE, "Seule une demande en attente peut être annulée.");
    statut = StatutDemande.ANNULEE;
}

public void terminer() {
    verifierStatut(StatutDemande.ACCEPTEE, "Seule une demande acceptée peut être terminée.");
    statut = StatutDemande.TERMINEE;
}

private void verifierStatut(StatutDemande attendu, String message) {
    if (statut != attendu) {
        throw new RegleGestionException(message);
    }
}
@Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    return id != 0 && id == ((Demande) o).id;
}

@Override
public int hashCode() {
    return id != 0 ? Integer.hashCode(id) : System.identityHashCode(this);
}
}

