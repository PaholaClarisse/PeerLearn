/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author Glamor Store
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import service.RegleGestionException;

public class ConnexionBD {
    private static final String FICHIER = "db.properties";
    private static Properties config;

    private static Properties lireConfig() {
        if (config == null) {
            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream(FICHIER)) {
                p.load(in);
            } catch (IOException e) {
                throw new RegleGestionException("Fichier de configuration introuvable : " + FICHIER);
            }
            config = p;
        }
        return config;
    }

    public static Connection getConnexion() {
        Properties p = lireConfig();
        try {
            return DriverManager.getConnection(
                    p.getProperty("db.url"),
                    p.getProperty("db.user"),
                    p.getProperty("db.password"));
        } catch (SQLException e) {
            if (e.getErrorCode() == 1045) {
                throw new RegleGestionException("Accès à la base refusé : utilisateur ou mot de passe incorrect.");
            }
            if (e.getErrorCode() == 1049) {
                throw new RegleGestionException("La base de données est introuvable : exécutez sql/schema.sql.");
            }
            throw new RegleGestionException("Impossible de joindre MySQL : vérifiez que le serveur est démarré.");
        }
    }
}