package com.peerlearn.ui;

import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Exécute un appel au contrôleur (donc à MySQL) hors du thread Swing,
 * puis rend le résultat sur le thread Swing. Sans cela, l'interface gèle
 * pendant la requête.
 */
public final class Taches {

    private Taches() { }

    public static <T> void executer(Component parent, Callable<T> travail, Consumer<T> succes) {
        executer(parent, travail, succes, () -> { });
    }

    /** "fin" s'exécute toujours, que l'appel réussisse ou échoue (réactiver un bouton, par exemple). */
    public static <T> void executer(Component parent, Callable<T> travail, Consumer<T> succes, Runnable fin) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return travail.call();
            }

            @Override
            protected void done() {
                try {
                    succes.accept(get());
                } catch (ExecutionException e) {
                    // RegleGestionException : message en français, prêt à afficher
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    erreur(parent, cause.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    fin.run();
                }
            }
        }.execute();
    }

    /** Comme executer, mais sans boîte d'erreur : pour les rafraîchissements automatiques en arrière-plan. */
    public static <T> void executerSilencieux(Callable<T> travail, Consumer<T> succes) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return travail.call();
            }

            @Override
            protected void done() {
                try {
                    succes.accept(get());
                } catch (ExecutionException | InterruptedException e) {
                    // on réessaiera au prochain tour ; l'utilisateur n'est pas dérangé
                }
            }
        }.execute();
    }

    public static void erreur(Component parent, String message) {
        Dialogue.erreur(parent, message);
    }

    public static void info(Component parent, String message) {
        Dialogue.info(parent, message);
    }
}
