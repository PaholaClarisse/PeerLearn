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
import java.sql.Statement;
import java.util.ArrayList;
import modele.Matiere;
import service.RegleGestionException;
import util.ConnexionBD;

public class MatiereDAO {

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Cette matière existe déjà.");
        }
        if (e.getErrorCode() == 1451) {
            return new RegleGestionException("Cette matière est utilisée et ne peut pas être supprimée.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private Matiere convertir(ResultSet rs) throws SQLException {
        return new Matiere(rs.getInt("id"), rs.getString("nom"));
    }

    public Matiere ajouter(String nom) {
        String sql = "INSERT INTO matieres (nom) VALUES (?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                return new Matiere(cles.getInt(1), nom);
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public Matiere chercherParId(int id) {
        String sql = "SELECT id, nom FROM matieres WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs) : null;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public ArrayList<Matiere> lister() {
        String sql = "SELECT id, nom FROM matieres ORDER BY nom";
        ArrayList<Matiere> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add(convertir(rs));
            }
            return resultat;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public void modifier(Matiere matiere) {
        String sql = "UPDATE matieres SET nom = ? WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, matiere.getNom());
            ps.setInt(2, matiere.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM matieres WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
}
