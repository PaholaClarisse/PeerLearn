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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import modele.Etudiant;
import modele.Exercice;
import modele.Rendu;
import modele.TypeFichier;
import service.RegleGestionException;
import util.ConnexionBD;

public class RenduDAO {
    private final ExerciceDAO exercices = new ExerciceDAO();
    private final UtilisateurDAO utilisateurs = new UtilisateurDAO();

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Vous avez déjà rendu cet exercice.");
        }
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Ce rendu fait référence à un exercice ou un tutoré inexistant.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private Rendu convertir(ResultSet rs, Exercice exercice) throws SQLException {
        Etudiant tutore = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_tutore"));
        return new Rendu(rs.getInt("id"), exercice, tutore,
                TypeFichier.valueOf(rs.getString("type")), rs.getString("chemin"),
                rs.getDate("date_rendu").toLocalDate());
    }

    public Rendu ajouter(Rendu r) {
        String sql = "INSERT INTO rendus (id_exercice, id_tutore, type, chemin, date_rendu) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getExercice().getId());
            ps.setInt(2, r.getTutore().getId());
            ps.setString(3, r.getType().name());
            ps.setString(4, r.getChemin());
            ps.setDate(5, Date.valueOf(r.getDateRendu()));
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                r.setId(cles.getInt(1));
            }
            return r;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public Rendu chercherParId(int id) {
        String sql = "SELECT * FROM rendus WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Exercice exercice = exercices.chercherParId(rs.getInt("id_exercice"));
                return convertir(rs, exercice);
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public Rendu chercherParExerciceEtTutore(Exercice exercice, int idTutore) {
        String sql = "SELECT * FROM rendus WHERE id_exercice = ? AND id_tutore = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, exercice.getId());
            ps.setInt(2, idTutore);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs, exercice) : null;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public ArrayList<Rendu> listerParExercice(Exercice exercice) {
        String sql = "SELECT * FROM rendus WHERE id_exercice = ? ORDER BY date_rendu, id";
        ArrayList<Rendu> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, exercice.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(convertir(rs, exercice));
                }
            }
            return resultat;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean supprimer(int id) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM rendus WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
}
        
