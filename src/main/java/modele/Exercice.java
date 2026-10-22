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

public class Exercice {
    private int id;
    private Etudiant tuteur;
    private String titre;
    private TypeFichier type;
    private String chemin;
    private LocalDate datePublication;

    public Exercice(Etudiant tuteur, String titre, String chemin) {
        if (tuteur == null) {
            throw new RegleGestionException("Un exercice doit avoir un tuteur.");
        }
        if (titre == null || titre.trim().isEmpty()) {
            throw new RegleGestionException("Le titre de l'exercice est obligatoire.");
        }
        this.tuteur = tuteur;
        this.titre = titre.trim();
        this.type = TypeFichier.depuisChemin(chemin);
        this.chemin = chemin.trim();
        this.datePublication = LocalDate.now();
    }
    public Exercice(int id, Etudiant tuteur, String titre, TypeFichier type,
                String chemin, LocalDate datePublication) {
    this.id = id;
    this.tuteur = tuteur;
    this.titre = titre;
    this.type = type;
    this.chemin = chemin;
    this.datePublication = datePublication;
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

    public String getTitre() {
        return titre;
    }

    public TypeFichier getType() {
        return type;
    }

    public String getChemin() {
        return chemin;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }
    @Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    return id != 0 && id == ((Exercice) o).id;
}

@Override
public int hashCode() {
    return id != 0 ? Integer.hashCode(id) : System.identityHashCode(this);
}
}
