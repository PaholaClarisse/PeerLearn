/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author Glamor Store
 */
import dao.ProfilDAO;
import modele.Profil;

public class ProfilService {
    private final ProfilDAO profilDAO = new ProfilDAO();

    public void enregistrerProfil(int idUser, String description, String filiere, String photo) {
        Profil existant = profilDAO.trouverParIdUser(idUser);

        if (existant == null) {
            // première complétion : création
            Profil nouveau = new Profil(idUser, description, filiere, photo);
            profilDAO.creer(nouveau);
        } else {
            // profil déjà créé : mise à jour
            existant.setDescription(description);
            existant.setFiliere(filiere);
            existant.setPhoto(photo);
            profilDAO.modifier(existant);
        }
    }

    public Profil obtenirProfil(int idUser) {
        return profilDAO.trouverParIdUser(idUser);
    }

}
