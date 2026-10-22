/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;
import service.RegleGestionException;
/**
 *
 * @author Glamor Store
 */
public class Matiere {
    private int id;
    private String nom;
    
    public Matiere(int id, String nom){
        if (nom == null || nom.isBlank()) {
            throw new RegleGestionException("Le nom de la matière est obligatoire.");
        }
        this.id = id;
        this.nom = nom.trim();
    }    

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }
     @Override
    public String toString() {
        return nom;
    }
   @Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    return getId() != 0 && getId() == ((Matiere) o).getId();
}

@Override
public int hashCode() {
    return getId() != 0 ? Integer.hashCode(getId()) : System.identityHashCode(this);
} 
}
