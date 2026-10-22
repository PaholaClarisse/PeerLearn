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
import java.sql.Timestamp;
import java.util.ArrayList;
import modele.Demande;
import modele.Etudiant;
import modele.Message;
import service.RegleGestionException;
import util.ConnexionBD;

public class MessageDAO {
    private final DemandeDAO demandes = new DemandeDAO();
    private final UtilisateurDAO utilisateurs = new UtilisateurDAO();

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Ce message fait référence à une demande ou un utilisateur inexistant.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private Message convertir(ResultSet rs, Demande demande) throws SQLException {
        Etudiant expediteur = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_expediteur"));
        return new Message(rs.getInt("id"), demande, expediteur, rs.getString("contenu"),
                rs.getTimestamp("date_envoi").toLocalDateTime());
    }

    public Message ajouter(Message m) {
        String sql = "INSERT INTO messages (id_demande, id_expediteur, contenu, date_envoi) VALUES (?, ?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getDemande().getId());
            ps.setInt(2, m.getExpediteur().getId());
            ps.setString(3, m.getContenu());
            ps.setTimestamp(4, Timestamp.valueOf(m.getDateEnvoi()));
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                m.setId(cles.getInt(1));
            }
            return m;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public Message chercherParId(int id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Demande demande = demandes.chercherParId(rs.getInt("id_demande"));
                return convertir(rs, demande);
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public ArrayList<Message> listerParDemande(Demande demande) {
        String sql = "SELECT * FROM messages WHERE id_demande = ? ORDER BY date_envoi, id";
        ArrayList<Message> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, demande.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultat.add(convertir(rs, demande));
                }
            }
            return resultat;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean supprimer(int id) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM messages WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
}
     