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
import modele.SeanceTutorat;
import modele.StatutSeance;
import service.RegleGestionException;
import util.ConnexionBD;

public class SeanceDAO {
    private final UtilisateurDAO utilisateurs = new UtilisateurDAO();

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Ce tutoré est déjà pointé pour cette séance.");
        }
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Cette séance fait référence à un utilisateur inexistant.");
        }
        if (e.getErrorCode() == 3819) {
            return new RegleGestionException("La durée d'une séance doit être positive.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private SeanceTutorat convertir(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Etudiant tuteur = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_tuteur"));
        SeanceTutorat seance = new SeanceTutorat(id, tuteur,
                rs.getDate("date_seance").toLocalDate(),
                rs.getInt("duree_minutes"),
                StatutSeance.valueOf(rs.getString("statut")));
        chargerPointages(seance);
        return seance;
    }

    private void chargerPointages(SeanceTutorat seance) throws SQLException {
        ArrayList<Etudiant> presents = new ArrayList<>();
        ArrayList<Etudiant> absents = new ArrayList<>();
        String sql = "SELECT id_tutore, present FROM presences WHERE id_seance = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, seance.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Etudiant e = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_tutore"));
                    if (rs.getBoolean("present")) {
                        presents.add(e);
                    } else {
                        absents.add(e);
                    }
                }
            }
        }
        seance.restaurerPointages(presents, absents);
    }

    public SeanceTutorat ajouter(SeanceTutorat s) {
        String sql = "INSERT INTO seances_tutorat (id_tuteur, date_seance, duree_minutes, statut) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getTuteur().getId());
            ps.setDate(2, Date.valueOf(s.getDateSeance()));
            ps.setInt(3, s.getDureeMinutes());
            ps.setString(4, s.getStatut().name());
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                s.setId(cles.getInt(1));
            }
            return s;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public SeanceTutorat chercherParId(int id) {
        String sql = "SELECT * FROM seances_tutorat WHERE id = ?";
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

    public void modifier(SeanceTutorat s) {
        String sql = "UPDATE seances_tutorat SET date_seance = ?, duree_minutes = ?, statut = ? WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(s.getDateSeance()));
            ps.setInt(2, s.getDureeMinutes());
            ps.setString(3, s.getStatut().name());
            ps.setInt(4, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public void enregistrerPointage(SeanceTutorat s, Etudiant tutore, boolean present) {
        String sql = "INSERT INTO presences (id_seance, id_tutore, present) VALUES (?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getId());
            ps.setInt(2, tutore.getId());
            ps.setBoolean(3, present);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean supprimer(int id) {
        try (Connection c = ConnexionBD.getConnexion()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement("DELETE FROM presences WHERE id_seance = ?");
                 PreparedStatement p2 = c.prepareStatement("DELETE FROM seances_tutorat WHERE id = ?")) {
                p1.setInt(1, id);
                p1.executeUpdate();
                p2.setInt(1, id);
                boolean supprimee = p2.executeUpdate() > 0;
                c.commit();
                return supprimee;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
    
    public ArrayList<SeanceTutorat> listerParTuteur(int idTuteur) {
    String sql = "SELECT * FROM seances_tutorat WHERE id_tuteur = ? ORDER BY date_seance, id";
    ArrayList<SeanceTutorat> resultat = new ArrayList<>();
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

public ArrayList<SeanceTutorat> lister() {
    String sql = "SELECT * FROM seances_tutorat ORDER BY date_seance, id";
    ArrayList<SeanceTutorat> resultat = new ArrayList<>();
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
}
