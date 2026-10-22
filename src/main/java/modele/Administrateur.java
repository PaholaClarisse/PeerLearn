/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;

/**
 *
 * @author Glamor Store
 */
public class Administrateur extends Utilisateur {
     
    public Administrateur( int id,String nom, String prenom,String mail, String mdp){
         super(id, nom, prenom, mail,mdp);
     }
    
    @Override public String getRole(){
        return "ADMIN";
    }
    
}
