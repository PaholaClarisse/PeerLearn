package com.peerlearn.ui;

import javax.swing.JTabbedPane;

/** Un onglet qui recharge ses données quand l'utilisateur l'ouvre. */
public interface Actualisable {

    void actualiser();

    /**
     * Appelle actualiser() sur l'onglet sélectionné, à chaque changement d'onglet,
     * et une première fois pour l'onglet affiché à l'ouverture.
     */
    static void brancher(JTabbedPane onglets) {
        onglets.addChangeListener(e -> {
            if (onglets.getSelectedComponent() instanceof Actualisable a) {
                a.actualiser();
            }
        });
        if (onglets.getSelectedComponent() instanceof Actualisable a) {
            a.actualiser();
        }
    }
}
