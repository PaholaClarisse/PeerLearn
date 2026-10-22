/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;
import java.time.LocalDate;
import service.RegleGestionException;
import java.util.ArrayList;
/**
 *
 * @author Glamor Store
 */
public class SeanceTutorat {
    private int id;
    private Etudiant tuteur;
    private LocalDate dateSeance;
    private int dureeMinutes;
    private StatutSeance statut = StatutSeance.PLANIFIEE;
    private ArrayList<Etudiant> absents = new ArrayList<>();


    public SeanceTutorat(Etudiant tuteur, LocalDate dateSeance, int dureeMinutes) {
        if (tuteur == null) {
            throw new RegleGestionException("Une séance doit avoir un tuteur.");
        }
        if (dateSeance == null) {
            throw new RegleGestionException("Une séance doit avoir une date.");
        }
        if (dureeMinutes <= 0) {
            throw new RegleGestionException("La durée d'une séance doit être positive.");
        }
        this.tuteur = tuteur;
        this.dateSeance = dateSeance;
        this.dureeMinutes = dureeMinutes;
    }
    
    public SeanceTutorat(int id, Etudiant tuteur, LocalDate dateSeance,
                     int dureeMinutes, StatutSeance statut) {
    this.id = id;
    this.tuteur = tuteur;
    this.dateSeance = dateSeance;
    this.dureeMinutes = dureeMinutes;
    this.statut = statut;
}

public void restaurerPointages(ArrayList<Etudiant> presents, ArrayList<Etudiant> absents) {
    this.presents = presents;
    this.absents = absents;
}

public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}
    public Etudiant getTuteur() {
        return tuteur;
    }

    public LocalDate getDateSeance() {
        return dateSeance;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }
    
    private ArrayList<Etudiant> presents = new ArrayList<>();

public void marquerPresent(Etudiant tutore) {
    if (statut == StatutSeance.ANNULEE) {
    throw new RegleGestionException("Cette séance est annulée.");
    }
    if (tutore == null) {
        throw new RegleGestionException("Tutoré invalide.");
    }
    if (presents.contains(tutore) || absents.contains(tutore)) {
    throw new RegleGestionException("Ce tutoré est déjà pointé pour cette séance.");
    }
    presents.add(tutore);
    }

public boolean estPresent(Etudiant tutore) {
    return presents.contains(tutore);
}

public ArrayList<Etudiant> getPresents() {
    return new ArrayList<>(presents);
}

public void marquerAbsent(Etudiant tutore) {
    if (statut == StatutSeance.ANNULEE) {
    throw new RegleGestionException("Cette séance est annulée.");
    }
    if (tutore == null) {
        throw new RegleGestionException("Tutoré invalide.");
    }
    if (presents.contains(tutore) || absents.contains(tutore)) {
        throw new RegleGestionException("Ce tutoré est déjà pointé pour cette séance.");
    }
    absents.add(tutore);
}

public ArrayList<Etudiant> getAbsents() {
    return new ArrayList<>(absents);
}


public StatutSeance getStatut() {
    return statut;
}

public void verifierModifiable() {
    if (statut == StatutSeance.ANNULEE) {
        throw new RegleGestionException("Cette séance est déjà annulée.");
    }
    if (!presents.isEmpty() || !absents.isEmpty()) {
        throw new RegleGestionException("Cette séance a déjà été pointée : elle ne peut plus être modifiée.");
    }
    if (dateSeance.isBefore(LocalDate.now())) {
        throw new RegleGestionException("Une séance passée ne peut plus être modifiée.");
    }
}

public void annuler() {
    verifierModifiable();
    statut = StatutSeance.ANNULEE;
}

public void reporter(LocalDate nouvelleDate) {
    verifierModifiable();
    if (nouvelleDate == null || nouvelleDate.isBefore(LocalDate.now())) {
        throw new RegleGestionException("La nouvelle date doit être aujourd'hui ou plus tard.");
    }
    dateSeance = nouvelleDate;
}
@Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    return id != 0 && id == ((SeanceTutorat) o).id;
}

@Override
public int hashCode() {
    return id != 0 ? Integer.hashCode(id) : System.identityHashCode(this);
}
}
