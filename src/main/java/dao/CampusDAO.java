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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modele.Niveau;
import service.RegleGestionException;
import util.ConnexionBD;

public class CampusDAO {

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Cette adresse figure déjà dans la liste de l'université.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    public Niveau chercherNiveau(String email) {
        String sql = "SELECT niveau FROM etudiant_campus WHERE email = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Niveau.valueOf(rs.getString("niveau")) : null;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean existe(String email) {
        return chercherNiveau(email) != null;
    }

    public ArrayList<String> listerEmails() {
        String sql = "SELECT email FROM etudiant_campus ORDER BY email";
        ArrayList<String> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add(rs.getString("email"));
            }
            return resultat;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public void ajouter(String email, Niveau niveau) {
        String sql = "INSERT INTO etudiant_campus (email, niveau) VALUES (?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, niveau.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public void modifierNiveau(String email, Niveau niveau) {
        String sql = "UPDATE etudiant_campus SET niveau = ? WHERE email = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, niveau.name());
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean supprimer(String email) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM etudiant_campus WHERE email = ?")) {
            ps.setString(1, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
}