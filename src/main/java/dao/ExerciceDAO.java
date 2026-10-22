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
import modele.TypeFichier;
import service.RegleGestionException;
import util.ConnexionBD;

public class ExerciceDAO {
    private final UtilisateurDAO utilisateurs = new UtilisateurDAO();

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Cet exercice fait référence à un tuteur inexistant.");
        }
        if (e.getErrorCode() == 1451) {
            return new RegleGestionException("Cet exercice a des rendus et ne peut pas être supprimé.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private Exercice convertir(ResultSet rs) throws SQLException {
        Etudiant tuteur = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_tuteur"));
        return new Exercice(rs.getInt("id"), tuteur, rs.getString("titre"),
                TypeFichier.valueOf(rs.getString("type")), rs.getString("chemin"),
                rs.getDate("date_publication").toLocalDate());
    }

    public Exercice ajouter(Exercice x) {
        String sql = "INSERT INTO exercices (id_tuteur, titre, type, chemin, date_publication) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, x.getTuteur().getId());
            ps.setString(2, x.getTitre());
            ps.setString(3, x.getType().name());
            ps.setString(4, x.getChemin());
            ps.setDate(5, Date.valueOf(x.getDatePublication()));
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                x.setId(cles.getInt(1));
            }
            return x;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public Exercice chercherParId(int id) {
        String sql = "SELECT * FROM exercices WHERE id = ?";
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

    public ArrayList<Exercice> listerParTuteur(int idTuteur) {
        String sql = "SELECT * FROM exercices WHERE id_tuteur = ? ORDER BY date_publication, id";
        ArrayList<Exercice> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTuteur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(convertir(rs));
                }
            }
            return resultat;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public ArrayList<Exercice> lister() {
        String sql = "SELECT * FROM exercices ORDER BY date_publication, id";
        ArrayList<Exercice> resultat = new ArrayList<>();
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

    public boolean supprimer(int id) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM exercices WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
}
