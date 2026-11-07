package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import service.GestionTutorat;

/** Inscription : le niveau n'est jamais saisi, il est lu dans la liste du campus (R1). */
public class DialogueInscription extends JDialog {

    public DialogueInscription(JFrame parent, GestionTutorat service) {
        super(parent, "Créer un compte", true);
        JTextField email = new JTextField(24);
        JTextField prenom = new JTextField(24);
        JTextField nom = new JTextField(24);
        JPasswordField mdp = new JPasswordField(24);
        email.putClientProperty("JTextField.placeholderText", "prenom.nom@univ-tours.fr");
        mdp.putClientProperty("JPasswordField.showRevealButton", Boolean.TRUE);
        Bouton valider = new Bouton("Créer mon compte").principal();
        Bouton annuler = new Bouton("Annuler");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        int y = 0;
        JLabel titre = new JLabel("Créer un compte");
        titre.setFont(Theme.police(22, Font.BOLD));
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
        form.add(titre, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0);
        form.add(Style.aide("Votre adresse doit figurer dans la liste de l'université."), c);

        String[] etiquettes = {"Adresse institutionnelle", "Prénom", "Nom", "Mot de passe (8 caractères minimum)"};
        javax.swing.JComponent[] champs = {email, prenom, nom, mdp};
        for (int i = 0; i < champs.length; i++) {
            JLabel l = new JLabel(etiquettes[i]);
            l.setFont(Theme.police(12, Font.BOLD));
            l.setForeground(Theme.TEXTE_DISCRET);
            c.gridy = y++; c.insets = new Insets(0, 0, 4, 0);
            form.add(l, c);
            c.gridy = y++; c.insets = new Insets(0, 0, 12, 0);
            form.add(champs[i], c);
        }

        JPanel bas = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        bas.setBackground(Color.WHITE);
        bas.setBorder(BorderFactory.createEmptyBorder(4, 28, 22, 28));
        bas.add(annuler);
        bas.add(valider);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(bas, BorderLayout.SOUTH);
        getContentPane().setBackground(Color.WHITE);

        annuler.addActionListener(e -> dispose());
        valider.addActionListener(e -> {
            String m = new String(mdp.getPassword());
            Taches.executer(this,
                    () -> service.inscrire(email.getText().trim(), nom.getText().trim(), prenom.getText().trim(), m),
                    etudiant -> {
                        Taches.info(this, "Compte créé. Vous pouvez maintenant vous connecter.");
                        dispose();
                    });
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
}
