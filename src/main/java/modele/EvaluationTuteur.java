/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;

/**
 *
 * @author Glamor Store
 */
import java.time.LocalDate;
import service.RegleGestionException;

public class EvaluationTuteur {
    private int id;
    private Demande demande;
    private int note;
    private String commentaire;
    private LocalDate dateEvaluation;

    public EvaluationTuteur(Demande demande, int note, String commentaire) {
        if (demande == null) {
            throw new RegleGestionException("Une évaluation doit porter sur une demande.");
        }
        if (note < 0 || note > 5) {
            throw new RegleGestionException("La note doit être comprise entre 0 et 5.");
        }
        this.demande = demande;
        this.note = note;
        this.commentaire = (commentaire == null) ? "" : commentaire.trim();
        this.dateEvaluation = LocalDate.now();
    }
public EvaluationTuteur(int id, Demande demande, int note, String commentaire, LocalDate dateEvaluation) {
    this.id = id;
    this.demande = demande;
    this.note = note;
    this.commentaire = commentaire;
    this.dateEvaluation = dateEvaluation;
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

    public int getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public LocalDate getDateEvaluation() {
        return dateEvaluation;
    }
}