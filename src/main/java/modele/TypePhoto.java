/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package modele;

/**
 *
 * @author Glamor Store
 */
import service.RegleGestionException;
public enum TypePhoto {
    PNG,JPG,JPEG;
    
    public static TypePhoto depuisCheminPhoto(String chemin){
         if (chemin == null || chemin.trim().isEmpty()) {
            throw new RegleGestionException("Le chemin du fichier est obligatoire.");
        }
          int point = chemin.lastIndexOf('.');
        if (point < 0) {
            throw new RegleGestionException("Le fichier doit avoir une extension PNG, JPG ou JPEG.");
        }
         String extension = chemin.substring(point + 1).trim().toUpperCase();
        for (TypePhoto t : values()) {
            if (t.name().equals(extension)) {
                return t;
            }
        }
        throw new RegleGestionException("Type de fichier non autorisé : seuls PNG, JPG ou JPEG sont acceptés.");
    }
}
