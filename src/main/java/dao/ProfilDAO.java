/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Glamor Store
 */
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import modele.Utilisateur;
import modele.Profil;
import modele.TypePhoto;
import service.RegleGestionException;
import util.ConnexionBD;
public class ProfilDAO {
    
    
    private RegleGestionException erreur(SQLException e) {
        e.printStackTrace();
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Un profil existe déjà pour cet utilisateur..");
        }
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Le compte utilisateur associé est introuvable");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }
    private Profil mapper(ResultSet rs) throws SQLException {
    Profil p = new Profil(
        rs.getInt("id_user"),
        rs.getString("description"),
        rs.getString("filiere"),
        rs.getString("photo")
    );
    p.setId(rs.getInt("id"));

    Timestamp date = rs.getTimestamp("date_maj");
    if (date != null) {
        p.setDateModification(date.toLocalDateTime());
    }
    return p;
}
    public Profil trouverParIdUser(int idUser){
        String sql = "select * from profil where id_user=?";
        try (Connection c = ConnexionBD.getConnexion();
            PreparedStatement ps = c.prepareStatement(sql)){
            ps.setInt(1, idUser);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next() ? mapper(rs) : null;
            }
        }catch (SQLException e) {
            throw erreur(e);
        }
        
        }
    public void creer(Profil p) {
    String sql = "INSERT INTO profil (id_user, description, filiere, photo) VALUES (?, ?, ?, ?)";
    try (Connection c = ConnexionBD.getConnexion();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, p.getIdUser());
        ps.setString(2, p.getDescription());
        ps.setString(3, p.getFiliere());
        ps.setString(4, p.getPhoto());          // si null, la base reçoit NULL

        ps.executeUpdate();

        try (ResultSet cles = ps.getGeneratedKeys()) {
            if (cles.next()) {
                p.setId(cles.getInt(1));        // l'id attribué par AUTO_INCREMENT
            }
        }
    } catch (SQLException e) {
        throw erreur(e);
    }
}
    public void modifier(Profil p) {
    String sql = "UPDATE profil SET description = ?, filiere = ?, photo = ? WHERE id_user = ?";
    try (Connection c = ConnexionBD.getConnexion();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, p.getDescription());
        ps.setString(2, p.getFiliere());
        ps.setString(3, p.getPhoto());      // si null, la base reçoit NULL
        ps.setInt(4, p.getIdUser());        // le WHERE

        int lignes = ps.executeUpdate();
        if (lignes == 0) {
            throw new RegleGestionException("Aucun profil à modifier pour cet utilisateur.");
        }
    } catch (SQLException e) {
        throw erreur(e);
    }
}
    }      


