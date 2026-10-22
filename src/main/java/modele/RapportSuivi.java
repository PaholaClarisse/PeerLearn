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
public class RapportSuivi {
    private int id;
    private SeanceTutorat seance;
    private Etudiant tutore;
    private int note;
    private String observations;
    private LocalDate dateRapport;

    public RapportSuivi(SeanceTutorat seance, Etudiant tutore, int note, String observations) {
        if (seance == null || tutore == null) {
            throw new RegleGestionException("Un rapport doit avoir une séance et un tutoré.");
        }
        if (note < 0 || note > 5) {
            throw new RegleGestionException("La note doit être comprise entre 0 et 5.");
        }
        if (observations == null) {
            observations = "";
        }
        this.seance = seance;
        this.tutore = tutore;
        this.note = note;
        this.observations = observations;
        this.dateRapport = LocalDate.now();
    }
    public RapportSuivi(int id, SeanceTutorat seance, Etudiant tutore, int note,
                    String observations, LocalDate dateRapport) {
    this.id = id;
    this.seance = seance;
    this.tutore = tutore;
    this.note = note;
    this.observations = observations;
    this.dateRapport = dateRapport;
}

public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}

    public SeanceTutorat getSeance() {
        return seance;
    }

    public Etudiant getTutore() {
        return tutore;
    }

    public int getNote() {
        return note;
    }

    public String getObservations() {
        return observations;
    }

    public LocalDate getDateRapport() {
        return dateRapport;
    }
}

