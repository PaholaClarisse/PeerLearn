package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import modele.Utilisateur;
import service.GestionTutorat;

/** Onglet commun aux trois rôles : liste des notifications enregistrées en base. */
public class PanneauNotifications extends JPanel implements Actualisable {

    private final GestionTutorat service;
    private final Utilisateur connecte;
    private final DefaultListModel<String> modele = new DefaultListModel<>();
    private final JLabel vide = Style.aide("Aucune notification pour l'instant.");
    private Runnable apresLecture = () -> { };

    public PanneauNotifications(GestionTutorat service, Utilisateur connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));

        JList<String> liste = new JList<>(modele);
        liste.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        liste.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, "<html><body style='width:520px'>" + v + "</body></html>", i, sel, false);
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDURE),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)));
                c.setBackground(sel ? Theme.ACCENT_CLAIR : Color.WHITE);
                c.setForeground(Theme.TEXTE);
                c.setFont(Theme.police(13, Font.PLAIN));
                return c;
            }
        });
        add(Style.defiler(liste), BorderLayout.CENTER);

        JPanel bas = new JPanel(new BorderLayout());
        bas.setOpaque(false);
        bas.add(vide, BorderLayout.WEST);
        Bouton actualiser = new Bouton("Actualiser");
        actualiser.addActionListener(e -> charger());
        bas.add(actualiser, BorderLayout.EAST);
        add(bas, BorderLayout.SOUTH);
        // pas de chargement ici : la liste se charge (et se marque lue) à l'ouverture de l'onglet
    }

    /** Appelé quand les notifications viennent d'être marquées lues (pour remettre le compteur à jour). */
    public void setApresLecture(Runnable r) {
        this.apresLecture = r == null ? () -> { } : r;
    }

    @Override
    public void actualiser() {
        charger();
    }

    public void charger() {
        Taches.executer(this, () -> service.mesNotifications(connecte), (ArrayList<String> liste) -> {
            modele.clear();
            // les plus récentes sont-elles en premier ? On les affiche dans l'ordre reçu du contrôleur.
            liste.forEach(modele::addElement);
            vide.setVisible(liste.isEmpty());
            // Le contrat demande de marquer comme lues quand l'utilisateur ouvre sa liste
            Taches.executer(this, () -> { service.marquerNotificationsLues(connecte); return null; },
                    r -> apresLecture.run());
        });
    }
}
