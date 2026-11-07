package com.peerlearn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import modele.Etudiant;
import modele.Utilisateur;
import service.GestionTutorat;

/** Compte de l'utilisateur connecté : identité et changement de mot de passe (tous les rôles). */
public class PanneauCompte extends JPanel {

    private final GestionTutorat service;
    private final Utilisateur connecte;
    private final JPasswordField ancien = new JPasswordField(22);
    private final JPasswordField nouveau = new JPasswordField(22);
    private final JPasswordField confirmation = new JPasswordField(22);
    private final Bouton boutonModifier = new Bouton("Modifier le mot de passe").principal();

    public PanneauCompte(GestionTutorat service, Utilisateur connecte) {
        this.service = service;
        this.connecte = connecte;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel carte = new JPanel(new GridBagLayout());
        carte.setBackground(Color.WHITE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDURE),
                BorderFactory.createEmptyBorder(20, 24, 24, 24)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        int ligne = 0;
        c.gridy = ligne++;
        carte.add(Style.section("Mes informations"), c);
        c.gridy = ligne++;
        carte.add(info("Nom", connecte instanceof Etudiant ? connecte.getPrenom() + " " + connecte.getNom() : "Administrateur"), c);
        c.gridy = ligne++;
        carte.add(info("Adresse", connecte.getMail()), c);
        c.gridy = ligne++;
        String role = connecte instanceof Etudiant e ? "Étudiant, niveau " + e.getNiveau() : "Administration";
        carte.add(info("Rôle", role), c);

        c.gridy = ligne++;
        c.insets = new Insets(22, 0, 5, 0);
        carte.add(Style.section("Changer mon mot de passe"), c);
        c.insets = new Insets(5, 0, 2, 0);
        c.gridy = ligne++;
        carte.add(etiquette("Mot de passe actuel"), c);
        c.gridy = ligne++;
        carte.add(ancien, c);
        c.gridy = ligne++;
        carte.add(etiquette("Nouveau mot de passe (8 caractères minimum)"), c);
        c.gridy = ligne++;
        carte.add(nouveau, c);
        c.gridy = ligne++;
        carte.add(etiquette("Confirmer le nouveau mot de passe"), c);
        c.gridy = ligne++;
        carte.add(confirmation, c);
        c.gridy = ligne++;
        c.insets = new Insets(16, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        carte.add(boutonModifier, c);

        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        carte.setPreferredSize(new java.awt.Dimension(460, carte.getPreferredSize().height));
        JPanel gauche = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        gauche.setOpaque(false);
        gauche.add(carte);
        haut.add(gauche, BorderLayout.NORTH);
        add(haut, BorderLayout.CENTER);

        boutonModifier.addActionListener(e -> modifier());
        confirmation.addActionListener(e -> modifier());
    }

    private void modifier() {
        String a = new String(ancien.getPassword());
        String n = new String(nouveau.getPassword());
        String conf = new String(confirmation.getPassword());
        if (!n.equals(conf)) {
            Taches.erreur(this, "La confirmation ne correspond pas au nouveau mot de passe.");
            return;
        }
        Taches.executer(this, () -> { service.changerMotDePasse(connecte, a, n); return null; }, r -> {
            ancien.setText("");
            nouveau.setText("");
            confirmation.setText("");
            Taches.info(this, "Votre mot de passe a été modifié.");
        });
    }

    private static JLabel etiquette(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(Theme.police(12, Font.BOLD));
        l.setForeground(Theme.TEXTE_DISCRET);
        return l;
    }

    private static JPanel info(String libelle, String valeur) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        JLabel l = etiquette(libelle);
        l.setPreferredSize(new java.awt.Dimension(80, 20));
        JLabel v = new JLabel(valeur);
        v.setFont(Theme.police(13));
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
    }
}
