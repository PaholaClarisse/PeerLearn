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
import modele.Demande;
import modele.EvaluationTuteur;
import service.RegleGestionException;
import util.ConnexionBD;

public class EvaluationDAO {
    private final DemandeDAO demandes = new DemandeDAO();

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Vous avez déjà évalué ce tuteur pour ce suivi.");
        }
        if (e.getErrorCode() == 3819) {
            return new RegleGestionException("La note doit être comprise entre 0 et 5.");
        }
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Cette évaluation fait référence à une demande inexistante.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private EvaluationTuteur convertir(ResultSet rs) throws SQLException {
        Demande demande = demandes.chercherParId(rs.getInt("id_demande"));
        return new EvaluationTuteur(rs.getInt("id"), demande, rs.getInt("note"),
                rs.getString("commentaire"), rs.getDate("date_evaluation").toLocalDate());
    }

    public EvaluationTuteur ajouter(EvaluationTuteur e) {
        String sql = "INSERT INTO evaluations_tuteur (id_demande, note, commentaire, date_evaluation) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getDemande().getId());
            ps.setInt(2, e.getNote());
            ps.setString(3, e.getCommentaire());
            ps.setDate(4, Date.valueOf(e.getDateEvaluation()));
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                e.setId(cles.getInt(1));
            }
            return e;
        } catch (SQLException ex) {
            throw erreur(ex);
        }
    }

    public EvaluationTuteur chercherParId(int id) {
        String sql = "SELECT * FROM evaluations_tuteur WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs) : null;
            }
        } catch (SQLException ex) {
            throw erreur(ex);
        }
    }

    public EvaluationTuteur chercherParDemande(int idDemande) {
        String sql = "SELECT * FROM evaluations_tuteur WHERE id_demande = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idDemande);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs) : null;
            }
        } catch (SQLException ex) {
            throw erreur(ex);
        }
    }

    public ArrayList<EvaluationTuteur> lister() {
        String sql = "SELECT * FROM evaluations_tuteur ORDER BY date_evaluation, id";
        ArrayList<EvaluationTuteur> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add(convertir(rs));
            }
            return resultat;
        } catch (SQLException ex) {
            throw erreur(ex);
        }
    }

    public boolean supprimer(int id) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM evaluations_tuteur WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw erreur(ex);
        }
    }
}
