package com.peerlearn.ui;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import modele.StatutDemande;
import modele.Utilisateur;

/** Textes en français pour l'affichage, et petits outils communs aux écrans. */
public final class Libelles {

    private Libelles() { }

    public static String statut(StatutDemande s) {
        return switch (s) {
            case EN_ATTENTE -> "En attente";
            case ACCEPTEE -> "Acceptée";
            case REFUSEE -> "Refusée";
            case ANNULEE -> "Annulée";
            case TERMINEE -> "Terminée";
        };
    }

    public static String jour(DayOfWeek j) {
        if (j == null) return "";
        String t = j.getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    public static String nom(Utilisateur e) {
        return e == null ? "" : e.getPrenom() + " " + e.getNom();
    }

    /** Modèle de tableau dont les cellules ne sont pas modifiables. */
    public static class ModeleTable extends DefaultTableModel {
        public ModeleTable(String... colonnes) {
            super(colonnes, 0);
        }

        @Override
        public boolean isCellEditable(int ligne, int colonne) {
            return false;
        }
    }
}
