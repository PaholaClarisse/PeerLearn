package com.peerlearn;

import java.sql.Connection;
import java.sql.SQLException;
import service.RegleGestionException;
import util.ConnexionBD;

public class PeerLearn {
    public static void main(String[] args) {
        try (Connection c = ConnexionBD.getConnexion()) {
            System.out.println("PeerLearn : connexion à la base " + c.getCatalog() + " réussie.");
            System.out.println("Interface graphique : à venir (Swing).");
        } catch (RegleGestionException | SQLException e) {
            System.out.println("Échec : " + e.getMessage());
        }
    }
}