package com.peerlearn.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Choix et ouverture des fichiers d'exercices (PDF, DOC, DOCX). */
public final class Fichiers {

    private Fichiers() { }

    /** Ouvre un sélecteur de fichier ; renvoie le chemin complet, ou null si l'utilisateur annule. */
    public static String choisir(Component parent) {
        JFileChooser selecteur = new JFileChooser();
        selecteur.setDialogTitle("Choisir un document");
        selecteur.setAcceptAllFileFilterUsed(false);
        selecteur.setFileFilter(new FileNameExtensionFilter("Documents (PDF, DOC, DOCX)", "pdf", "doc", "docx"));
        if (selecteur.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return selecteur.getSelectedFile().getAbsolutePath();
    }

    /** Ouvre le fichier avec l'application du système. Les erreurs sont affichées en français. */
    public static void ouvrir(Component parent, String chemin) {
        File f = new File(chemin);
        if (!f.exists()) {
            Taches.erreur(parent, "Fichier introuvable : " + chemin
                    + "\nIl se trouve sur un autre ordinateur ou a été déplacé.");
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Taches.erreur(parent, "L'ouverture de fichiers n'est pas disponible sur ce système.\nChemin : " + chemin);
            return;
        }
        Taches.executer(parent, () -> { Desktop.getDesktop().open(f); return null; }, r -> { });
    }

    public static String nom(String chemin) {
        return chemin == null ? "" : new File(chemin).getName();
    }
}
