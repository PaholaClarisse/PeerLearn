/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;

/**
 *
 * @author Glamor Store
 */
import java.time.LocalDateTime;
import service.RegleGestionException;
public class Profil {
    private int id;
    private int idUser;
    private String description;
    private String filiere;
    private String photo;
    private LocalDateTime dateModification;
    
    public Profil(int idUser,String description, String filiere, String photo){
          setDescription(description);
          setFiliere(filiere);
         this.idUser = idUser;
         setPhoto(photo);
         this.dateModification = null;
     }
    public final void setPhoto(String photo){
          if(photo != null && !photo.trim().isEmpty()){
            TypePhoto.depuisCheminPhoto(photo);
            this.photo = photo.trim();
         }else{
             this.photo = null;
         }
    }
    public final void setFiliere(String filiere){
             if (filiere == null || filiere.trim().isEmpty()) {
            throw new RegleGestionException("La filiere est obligatoire.");
        }
             this.filiere = filiere.trim();
    }
    public final void setDescription(String description) {
    if (description == null || description.trim().isEmpty()) {
        throw new RegleGestionException("La description de ton profil est obligatoire.");
    }
    this.description = description.trim();
}
       public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getDescription() {
        return description;
    }

    public String getFiliere() {
        return filiere;
    }

    public String getPhoto() {
        return photo;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }
    
@Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    return id != 0 && id == ((Profil) o).id;
}

@Override
public int hashCode() {
    return id != 0 ? Integer.hashCode(id) : System.identityHashCode(this);
}
}
    

