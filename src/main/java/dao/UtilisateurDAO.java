/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Glamor Store
 */
import java.sql.Statement;
import java.sql.Types;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import modele.Administrateur;
import modele.Etudiant;
import modele.Matiere;
import modele.Niveau;
import modele.Utilisateur;
import service.RegleGestionException;
import util.ConnexionBD;

public class UtilisateurDAO {

    private static final String SELECT_BASE =
            "SELECT u.*, m.nom AS nom_matiere "
            + "FROM utilisateurs u LEFT JOIN matieres m ON u.id_matiere = m.id ";

    private RegleGestionException erreur(SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new RegleGestionException("Un compte existe déjà avec cette adresse.");
        }
        if (e.getErrorCode() == 1451) {
    return new RegleGestionException("Ce compte est utilisé ailleurs (demandes, séances...) et ne peut pas être supprimé.");
}
        return new RegleGestionException("Erreur d'accès à la base de données.");
    }

    private Utilisateur convertir(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String email = rs.getString("email");
        String mdp = rs.getString("mot_de_passe_hash");

        if ("ADMIN".equals(rs.getString("role"))) {
            return new Administrateur(id, nom, prenom, email, mdp);
        }

        Etudiant e = new Etudiant(id, nom, prenom, email, mdp,
                Niveau.valueOf(rs.getString("niveau")));

        int idMatiere = rs.getInt("id_matiere");
        Matiere matiere = rs.wasNull() ? null : new Matiere(idMatiere, rs.getString("nom_matiere"));

        String jourTexte = rs.getString("jour_semaine");
        DayOfWeek jour = (jourTexte == null) ? null : DayOfWeek.valueOf(jourTexte);

        Time heureSql = rs.getTime("heure_debut");
        LocalTime heure = (heureSql == null) ? null : heureSql.toLocalTime();

        e.restaurerDepuisBase(rs.getBoolean("est_tuteur_actif"),
                rs.getBoolean("retire"), matiere, jour, heure);
        return e;
    }

    public Utilisateur chercherParId(int id) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE u.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs) : null;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public Utilisateur chercherParEmail(String email) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE u.email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? convertir(rs) : null;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    public ArrayList<Utilisateur> lister() {
        ArrayList<Utilisateur> resultat = new ArrayList<>();
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "ORDER BY u.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultat.add(convertir(rs));
            }
            return resultat;
        } catch (SQLException e) {
            throw erreur(e);
        }
    }
    
    public Etudiant ajouterEtudiant(String nom, String prenom, String email, String mdp, Niveau niveau) {
    String sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe_hash, role, niveau) "
            + "VALUES (?, ?, ?, ?, 'ETUDIANT', ?)";
    try (Connection c = ConnexionBD.getConnexion();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, nom);
        ps.setString(2, prenom);
        ps.setString(3, email);
        ps.setString(4, mdp);
        ps.setString(5, niveau.name());
        ps.executeUpdate();
        try (ResultSet cles = ps.getGeneratedKeys()) {
            cles.next();
            return new Etudiant(cles.getInt(1), nom, prenom, email, mdp, niveau);
        }
    } catch (SQLException e) {
        throw erreur(e);
    }
}

public void modifierProfil(Connection c, Etudiant e) throws SQLException {
    String sql = "UPDATE utilisateurs SET est_tuteur_actif = ?, retire = ?, id_matiere = ?, "
            + "jour_semaine = ?, heure_debut = ?, capacite_max = ? WHERE id = ?";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setBoolean(1, e.isEstTuteurActif());
        ps.setBoolean(2, e.isRetire());
        if (e.getMatiere() == null) {
            ps.setNull(3, Types.INTEGER);
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(3, e.getMatiere().getId());
            ps.setInt(6, e.getCapaciteMax());
        }
        if (e.getJour() == null) {
            ps.setNull(4, Types.VARCHAR);
        } else {
            ps.setString(4, e.getJour().name());
        }
        if (e.getHeure() == null) {
            ps.setNull(5, Types.TIME);
        } else {
            ps.setTime(5, Time.valueOf(e.getHeure()));
        }
        ps.setInt(7, e.getId());
        ps.executeUpdate();
    }
}

public void modifierProfil(Etudiant e) {
    try (Connection c = ConnexionBD.getConnexion()) {
        modifierProfil(c, e);
    } catch (SQLException ex) {
        throw erreur(ex);
    }
}

public boolean supprimer(int id) {
    try (Connection c = ConnexionBD.getConnexion()) {
        c.setAutoCommit(false);
        try (PreparedStatement p1 = c.prepareStatement("DELETE FROM notifications WHERE id_utilisateur = ?");
             PreparedStatement p2 = c.prepareStatement("DELETE FROM utilisateurs WHERE id = ?")) {
            p1.setInt(1, id);
            p1.executeUpdate();
            p2.setInt(1, id);
            boolean supprime = p2.executeUpdate() > 0;
            c.commit();
            return supprime;
        } catch (SQLException e) {
            c.rollback();
            throw e;
        }
    } catch (SQLException e) {
        throw erreur(e);
    }
}
public void modifierNiveau(int id, Niveau niveau) {
    String sql = "UPDATE utilisateurs SET niveau = ? WHERE id = ? AND role = 'ETUDIANT'";
    try (Connection c = ConnexionBD.getConnexion();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, niveau.name());
        ps.setInt(2, id);
        ps.executeUpdate();
    } catch (SQLException e) {
        throw erreur(e);
    }
}

public void modifierMotDePasse(int id, String hash) {
    String sql = "UPDATE utilisateurs SET mot_de_passe_hash = ? WHERE id = ?";
    try (Connection c = ConnexionBD.getConnexion();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, hash);
        ps.setInt(2, id);
        ps.executeUpdate();
    } catch (SQLException e) {
        throw erreur(e);
    }
}
}