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
import java.time.LocalDateTime;
import java.util.ArrayList;
import modele.Notification;
import service.RegleGestionException;
import util.ConnexionBD;

public class NotificationDAO {

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1406) {
            return new RegleGestionException("Le message de la notification est trop long (500 caractères maximum).");
        }
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Cette notification est destinée à un utilisateur inexistant.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    public Notification ajouter(int idUtilisateur, String message) {
        String sql = "INSERT INTO notifications (id_utilisateur, message) VALUES (?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUtilisateur);
            ps.setString(2, message);
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                return new Notification(cles.getInt(1), message, LocalDateTime.now(), false);
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public ArrayList<Notification> listerParUtilisateur(int idUtilisateur) {
        String sql = "SELECT id, message, date_envoi, lue FROM notifications "
                + "WHERE id_utilisateur = ? ORDER BY date_envoi, id";
        ArrayList<Notification> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(new Notification(rs.getInt("id"), rs.getString("message"),
                            rs.getTimestamp("date_envoi").toLocalDateTime(), rs.getBoolean("lue")));
                }
            }
            return resultat;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public int compterNonLues(int idUtilisateur) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE id_utilisateur = ? AND lue = FALSE";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public int marquerToutesLues(int idUtilisateur) {
        String sql = "UPDATE notifications SET lue = TRUE WHERE id_utilisateur = ? AND lue = FALSE";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean supprimer(int id) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM notifications WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public int supprimerParUtilisateur(int idUtilisateur) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM notifications WHERE id_utilisateur = ?")) {
            ps.setInt(1, idUtilisateur);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
}