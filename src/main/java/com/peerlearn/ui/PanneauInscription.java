package com.peerlearn.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import service.GestionTutorat;

/**
 * Page d'inscription (même mise en page que la connexion).
 * Le niveau n'est jamais saisi : il est lu dans la liste du campus (R1).
 */
public class PanneauInscription extends JPanel {

    private final Fenetre fenetre;
    private final GestionTutorat service;
    private final JTextField email = new JTextField(24);
    private final JTextField prenom = new JTextField(24);
    private final JTextField nom = new JTextField(24);
    private final JPasswordField mdp = new JPasswordField(24);
    private final JPasswordField confirmation = new JPasswordField(24);
    private final Bouton valider = new Bouton("Créer mon compte").principal();
    private final Bouton retour = new Bouton("Retour à la connexion");

    public PanneauInscription(Fenetre fenetre, GestionTutorat service) {
        this.fenetre = fenetre;
        this.service = service;
        setLayout(new GridLayout(1, 2));
        add(new PanneauMarque());
        add(formulaire());

        email.putClientProperty("JTextField.placeholderText", "prenom.nom@univ-tours.fr");
        mdp.putClientProperty("JPasswordField.showRevealButton", Boolean.TRUE);
        confirmation.putClientProperty("JPasswordField.showRevealButton", Boolean.TRUE);

        valider.addActionListener(e -> inscrire());
        retour.addActionListener(e -> fenetre.afficherConnexion());
        confirmation.addActionListener(e -> inscrire());
    }

    /** Vide les champs : appelé à chaque affichage de la page. */
    public void reinitialiser() {
        for (JTextField c : new JTextField[] {email, prenom, nom, mdp, confirmation}) c.setText("");
        email.requestFocusInWindow();
    }

    private JPanel formulaire() {
        JPanel fond = new JPanel(new GridBagLayout());
        fond.setBackground(Color.WHITE);

        JPanel colonne = new JPanel(new GridBagLayout());
        colonne.setOpaque(false);
        colonne.setPreferredSize(new Dimension(340, 560));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        int y = 0;
        JLabel titre = new JLabel("Créer un compte");
        titre.setFont(Theme.police(28, Font.BOLD));
        titre.setForeground(Theme.TEXTE);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
        colonne.add(titre, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 22, 0);
        colonne.add(Style.aide("Votre adresse doit figurer dans la liste de l'université."), c);

        String[] noms = {"Adresse institutionnelle", "Prénom", "Nom", "Mot de passe (8 caractères minimum)", "Confirmer le mot de passe"};
        JComponent[] champs = {email, prenom, nom, mdp, confirmation};
        for (int i = 0; i < champs.length; i++) {
            JLabel l = new JLabel(noms[i]);
            l.setFont(Theme.police(12, Font.BOLD));
            l.setForeground(Theme.TEXTE_DISCRET);
            c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
            colonne.add(l, c);
            c.gridy = y++; c.insets = new Insets(0, 0, 12, 0);
            colonne.add(champs[i], c);
        }

        c.gridy = y++; c.insets = new Insets(12, 0, 14, 0);
        colonne.add(valider, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0);
        colonne.add(retour, c);

        fond.add(colonne);
        return fond;
    }

    private void inscrire() {
        String m = new String(mdp.getPassword());
        if (!m.equals(new String(confirmation.getPassword()))) {
            Taches.erreur(this, "Les deux mots de passe ne correspondent pas.");
            return;
        }
        String adresse = email.getText().trim();
        valider.setEnabled(false);
        Taches.executer(this,
                () -> service.inscrire(adresse, nom.getText().trim(), prenom.getText().trim(), m),
                etudiant -> {
                    Taches.info(this, "Compte créé. Vous pouvez maintenant vous connecter.");
                    fenetre.afficherConnexion(adresse);
                }, () -> valider.setEnabled(true));
    }
}
