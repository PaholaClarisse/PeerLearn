package com.peerlearn.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import modele.Utilisateur;
import service.GestionTutorat;

/** Écran de connexion : panneau de marque à gauche, formulaire à droite. */
public class PanneauConnexion extends JPanel {

    private final Fenetre fenetre;
    private final GestionTutorat service;
    private final JTextField champEmail = new JTextField(24);
    private final JPasswordField champMdp = new JPasswordField(24);
    private final Bouton boutonConnexion = new Bouton("Se connecter").principal();
    private final Bouton boutonInscription = new Bouton("Créer un compte");

    public PanneauConnexion(Fenetre fenetre, GestionTutorat service) {
        this.fenetre = fenetre;
        this.service = service;
        setLayout(new GridLayout(1, 2));

        add(new PanneauMarque());
        add(formulaire());

        // Propriétés reconnues par FlatLaf si présent ; sans effet sinon
        champEmail.putClientProperty("JTextField.placeholderText", "prenom.nom@univ-tours.fr");
        champMdp.putClientProperty("JPasswordField.showRevealButton", Boolean.TRUE);

        boutonConnexion.addActionListener(e -> connecter());
        champEmail.addActionListener(e -> champMdp.requestFocusInWindow()); // Entrée : champ suivant
        champMdp.addActionListener(e -> connecter());
        boutonInscription.addActionListener(e -> fenetre.afficherInscription());
    }

    public void preremplir(String email) {
        champEmail.setText(email);
        champMdp.setText("");
        champMdp.requestFocusInWindow();
    }

    private JPanel formulaire() {
        JPanel fond = new JPanel(new GridBagLayout());
        fond.setBackground(Color.WHITE);

        JPanel colonne = new JPanel(new GridBagLayout());
        colonne.setOpaque(false);
        colonne.setPreferredSize(new Dimension(340, 380));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        int y = 0;
        JLabel titre = new JLabel("Bon retour");
        titre.setFont(Theme.police(28, Font.BOLD));
        titre.setForeground(Theme.TEXTE);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
        colonne.add(titre, c);

        c.gridy = y++; c.insets = new Insets(0, 0, 28, 0);
        colonne.add(Style.aide("Connectez-vous avec votre adresse de l'université."), c);

        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0);
        colonne.add(etiquette("Adresse e-mail"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 16, 0);
        colonne.add(champEmail, c);

        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0);
        colonne.add(etiquette("Mot de passe"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 24, 0);
        colonne.add(champMdp, c);

        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0);
        colonne.add(boutonConnexion, c);

        JPanel ou = new JPanel(new java.awt.BorderLayout(10, 0));
        ou.setOpaque(false);
        JLabel question = Style.aide("Pas encore de compte ?");
        ou.add(question, java.awt.BorderLayout.WEST);
        ou.add(boutonInscription, java.awt.BorderLayout.EAST);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0);
        colonne.add(ou, c);

        fond.add(colonne);
        return fond;
    }

    private static JLabel etiquette(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Theme.police(12, Font.BOLD));
        l.setForeground(Theme.TEXTE_DISCRET);
        return l;
    }

    private void connecter() {
        String email = champEmail.getText().trim();
        String mdp = new String(champMdp.getPassword());
        boutonConnexion.setEnabled(false);
        Taches.executer(this, () -> service.connecter(email, mdp), (Utilisateur u) -> {
            champMdp.setText("");
            fenetre.afficherAccueil(u);
        }, () -> boutonConnexion.setEnabled(true));
    }
}
