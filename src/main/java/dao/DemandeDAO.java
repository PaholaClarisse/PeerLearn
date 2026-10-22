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
import modele.Demande;
import modele.Etudiant;
import modele.Matiere;
import modele.StatutDemande;
import service.RegleGestionException;
import util.ConnexionBD;
import java.util.ArrayList;

public class DemandeDAO {
    private final UtilisateurDAO utilisateurs = new UtilisateurDAO();
    private final MatiereDAO matieres = new MatiereDAO();

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1452) {
            return new RegleGestionException("Cette demande fait référence à un utilisateur ou une matière inexistant.");
        }
        if (e.getErrorCode() == 3819) {
            return new RegleGestionException("Un étudiant ne peut pas se demander à lui-même.");
        }
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private Demande convertir(ResultSet rs) throws SQLException {
        Etudiant tutore = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_tutore"));
        Etudiant tuteur = (Etudiant) utilisateurs.chercherParId(rs.getInt("id_tuteur"));
        Matiere matiere = matieres.chercherParId(rs.getInt("id_matiere"));
        return new Demande(rs.getInt("id"), tutore, tuteur, matiere,
                rs.getDate("date_demande").toLocalDate(),
                StatutDemande.valueOf(rs.getString("statut")));
    }

    public Demande ajouter(Demande d) {
        String sql = "INSERT INTO demandes (id_tutore, id_tuteur, id_matiere, date_demande, statut) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getTutore().getId());
            ps.setInt(2, d.getTuteur().getId());
            ps.setInt(3, d.getMatiere().getId());
            ps.setDate(4, Date.valueOf(d.getDateDemande()));
            ps.setString(5, d.getStatut().name());
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                d.setId(cles.getInt(1));
            }
            return d;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public Demande chercherParId(int id) {
        String sql = "SELECT * FROM demandes WHERE id = ?";
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

    public void modifierStatut(Demande d) {
        String sql = "UPDATE demandes SET statut = ? WHERE id = ?";
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getStatut().name());
            ps.setInt(2, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public boolean supprimer(int id) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM demandes WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
    public ArrayList<Demande> lister() {
    String sql = "SELECT * FROM demandes ORDER BY id";
    ArrayList<Demande> resultat = new ArrayList<>();
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

public ArrayList<Demande> listerParTuteur(int idTuteur) {
    String sql = "SELECT * FROM demandes WHERE id_tuteur = ? ORDER BY id";
    ArrayList<Demande> resultat = new ArrayList<>();
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

public ArrayList<Demande> listerParTutore(int idTutore) {
    String sql = "SELECT * FROM demandes WHERE id_tutore = ? ORDER BY id";
    ArrayList<Demande> resultat = new ArrayList<>();
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

public ArrayList<Etudiant> listerTutoresAcceptes(int idTuteur) {
    String sql = "SELECT id_tutore FROM demandes WHERE id_tuteur = ? AND statut = 'ACCEPTEE' ORDER BY id";
    ArrayList<Etudiant> resultat = new ArrayList<>();
    try (Connection c = ConnexionBD.getConnexion();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, idTuteur);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add((Etudiant) utilisateurs.chercherParId(rs.getInt("id_tutore")));
            }
        }
        return resultat;
    } catch (SQLException e) {
        throw erreur(e);
    }
}

public void verrouillerTuteur(Connection c, int idTuteur) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement(
            "SELECT id FROM utilisateurs WHERE id = ? FOR UPDATE")) {
        ps.setInt(1, idTuteur);
        ps.executeQuery().close();
    }
}

public void modifierStatut(Connection c, Demande d) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement(
            "UPDATE demandes SET statut = ? WHERE id = ?")) {
        ps.setString(1, d.getStatut().name());
        ps.setInt(2, d.getId());
        ps.executeUpdate();
    }
}
}
