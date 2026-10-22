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

public class Rendu {
    private int id;
    private Exercice exercice;
    private Etudiant tutore;
    private TypeFichier type;
    private String chemin;
    private LocalDate dateRendu;

    public Rendu(Exercice exercice, Etudiant tutore, String chemin) {
        if (exercice == null || tutore == null) {
            throw new RegleGestionException("Un rendu doit avoir un exercice et un tutoré.");
        }
        this.exercice = exercice;
        this.tutore = tutore;
        this.type = TypeFichier.depuisChemin(chemin);
        this.chemin = chemin.trim();
        this.dateRendu = LocalDate.now();
    }
    public Rendu(int id, Exercice exercice, Etudiant tutore, TypeFichier type,
             String chemin, LocalDate dateRendu) {
    this.id = id;
    this.exercice = exercice;
    this.tutore = tutore;
    this.type = type;
    this.chemin = chemin;
    this.dateRendu = dateRendu;
}

public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getTutore() {
        return tutore;
    }

    public TypeFichier getType() {
        return type;
    }

    public String getChemin() {
        return chemin;
    }

    public LocalDate getDateRendu() {
        return dateRendu;
    }
}