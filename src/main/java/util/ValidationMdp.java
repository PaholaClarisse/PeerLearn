/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Glamor Store
 */
public class ValidationMdp {
    private static final String SPECIAUX = "!@#$%^&*()_+-=<>?";
    public static List<String> valider(String mdp){
        List<String> erreurs = new ArrayList<>();
        boolean aMinuscule = false;
        boolean aMajuscule = false;
        boolean aChiffre = false;
        boolean aSpecial = false;
       
            if(mdp == null){
               erreurs.add("Le mot de passe est obligatoire");
               return erreurs;
            }
            if(mdp.length() < 8){
                erreurs.add("Le mot de passe doit contenir au moins 8 caractères");
            } 
            for(char c: mdp.toCharArray()){
                if(Character.isLowerCase(c)){
                    aMinuscule = true;
                }
                if(Character.isUpperCase(c)){
                    aMajuscule = true;
                }
                if(Character.isDigit(c)){
                    aChiffre = true;
                }
                if(SPECIAUX.indexOf(c)>=0){
                    aSpecial = true;
                }
             }
            
            if(!aMinuscule){
            erreurs.add("le mot de passe doit contenir au moins une minuscule.");
            }
            if(!aMajuscule){
                erreurs.add("le mot de passe doit contenir au moins une majuscule");
            }
            if(!aChiffre){
                erreurs.add("ce mot de passe doit contenir au moins un chiffre");
            }
            if(!aSpecial){
                erreurs.add("ce mot de passe doit contenir au moins un caractere special");
            }
        return erreurs;
    }
}
