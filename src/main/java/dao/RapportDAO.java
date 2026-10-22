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
import modele.RapportSuivi;
import modele.SeanceTutorat;
import service.RegleGestionException;
import util.ConnexionBD;

public class RapportDAO {
    private final SeanceDAO seances = new SeanceDAO();
    private final UtilisateurDAO utilisateurs = new UtilisateurDAO();

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Un rapport existe déjà pour cette séance et ce tutoré.");
        }
        if (e.getErrorCode() == 3819) {
            return new RegleGestionException("La note doit être comprise entre 0 et 5.");
        }
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Ce rapport fait référence à une séance ou un tutoré inexistant.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private RapportSuivi convertir(ResultSet rs) throws SQLException {
        SeanceTutorat seance = seances.chercherParId(rs.getInt("id_seance"));
        Etudiant tutore = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_tutore"));
        return new RapportSuivi(rs.getInt("id"), seance, tutore, rs.getInt("note"),
                rs.getString("observations"), rs.getDate("date_rapport").toLocalDate());
    }

    public RapportSuivi ajouter(RapportSuivi r) {
        String sql = "INSERT INTO rapports_suivi (id_seance, id_tutore, note, observations, date_rapport) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getSeance().getId());
            ps.setInt(2, r.getTutore().getId());
            ps.setInt(3, r.getNote());
            ps.setString(4, r.getObservations());
            ps.setDate(5, Date.valueOf(r.getDateRapport()));
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

    public RapportSuivi chercherParId(int id) {
        String sql = "SELECT * FROM rapports_suivi WHERE id = ?";
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

    public ArrayList<RapportSuivi> listerParTutore(int idTutore) {
        String sql = "SELECT * FROM rapports_suivi WHERE id_tutore = ? ORDER BY date_rapport, id";
        ArrayList<RapportSuivi> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTutore);
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

    public ArrayList<RapportSuivi> lister() {
        String sql = "SELECT * FROM rapports_suivi ORDER BY date_rapport, id";
        ArrayList<RapportSuivi> resultat = new ArrayList<>();
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
             PreparedStatement ps = c.prepareStatement("DELETE FROM rapports_suivi WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
}
