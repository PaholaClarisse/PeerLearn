package com.peerlearn;

import com.peerlearn.ui.Fenetre;
import com.peerlearn.ui.Theme;
import javax.swing.SwingUtilities;
import service.GestionTutorat;

/** Point d'entrée de l'application (exec.mainClass du pom.xml). */
public class PeerLearn {
    public static void main(String[] args) {
        Theme.installer();
        SwingUtilities.invokeLater(() -> {
            GestionTutorat service = new GestionTutorat();
            new Fenetre(service).setVisible(true);
        });
    }
}
