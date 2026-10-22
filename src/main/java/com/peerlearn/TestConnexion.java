/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import service.RegleGestionException;
import util.ConnexionBD;

public class TestConnexion {
    public static void main(String[] args) {
        try (Connection c = ConnexionBD.getConnexion();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM matieres");
             ResultSet rs = ps.executeQuery()) {
            System.out.println("Connexion réussie à la base : " + c.getCatalog());
            rs.next();
            System.out.println("Nombre de matières : " + rs.getInt(1) + " (attendu : 6)");
        } catch (RegleGestionException | SQLException e) {
            System.out.println("Échec : " + e.getMessage());
        }
    }
}