/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;
import java.util.ArrayList;
import interfaces.Notifiable;

/**
 *
 * @author Glamor Store
 */
public abstract class Utilisateur implements Notifiable{
    private int id;
    private String nom;
    private String prenom;
    private String mail;
    private String mdp;
    
    public Utilisateur( int id,String nom, String prenom,String mail, String mdp){
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.mdp = mdp;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getMail() {
        return mail;
    }

    public String getMdp() {
        return mdp;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }
    public abstract String getRole();
    private ArrayList<String> notifications = new ArrayList<>();

@Override
public void envoyerNotification(String message) {
    notifications.add(message);
}

public ArrayList<String> getNotifications() {
    return new ArrayList<>(notifications);
}
@Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
    return getId() != 0 && getId() == ((Utilisateur) o).getId();
}

@Override
public int hashCode() {
    return getId() != 0 ? Integer.hashCode(getId()) : System.identityHashCode(this);
}
}
