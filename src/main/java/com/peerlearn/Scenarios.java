package com.peerlearn;

import dao.DemandeDAO;
import dao.SeanceDAO;
import dao.UtilisateurDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import modele.Demande;
import modele.Etudiant;
import modele.Exercice;
import modele.Matiere;
import modele.SeanceTutorat;
import modele.StatutDemande;
import modele.StatutSeance;
import modele.Utilisateur;
import service.GestionTutorat;
import service.RegleGestionException;
import util.ConnexionBD;

public class Scenarios {
    private static int ok = 0;
    private static int echecs = 0;

    private static void attendreRefus(String titre, Runnable action) {
        try {
            action.run();
            echecs++;
            System.out.println("ECHEC : " + titre + " aurait dû être refusé");
        } catch (RegleGestionException e) {
            ok++;
            System.out.println("OK    : " + titre + " -> " + e.getMessage());
        }
    }

    private static void verifier(String titre, boolean condition) {
        if (condition) {
            ok++;
        } else {
            echecs++;
        }
        System.out.println((condition ? "OK    : " : "ECHEC : ") + titre);
    }

    private static void reinitialiser() {
        String[] requetes = {
            "DELETE FROM notifications",
            "DELETE FROM rendus",
            "DELETE FROM exercices",
            "DELETE FROM evaluations_tuteur",
            "DELETE FROM messages",
            "DELETE FROM rapports_suivi",
            "DELETE FROM presences",
            "DELETE FROM seances_tutorat",
            "DELETE FROM demandes",
            "DELETE FROM utilisateurs WHERE role = 'ETUDIANT'",
            "DELETE FROM matieres WHERE nom = 'Chimie test'"
        };
        try (Connection c = ConnexionBD.getConnexion(); Statement st = c.createStatement()) {
            for (String requete : requetes) {
                st.executeUpdate(requete);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de remettre la base à zéro : " + e.getMessage());
        }
    }

    private static Matiere matiere(GestionTutorat g, Utilisateur admin, String nom) {
        for (Matiere m : g.listerMatieres(admin)) {
            if (m.getNom().equals(nom)) {
                return m;
            }
        }
        throw new RuntimeException("Matière absente de la base : " + nom + " (exécutez donnees_test.sql)");
    }

    public static void main(String[] args) {
        reinitialiser();
        GestionTutorat g = new GestionTutorat();
        UtilisateurDAO utilisateurs = new UtilisateurDAO();
        DemandeDAO demandes = new DemandeDAO();
        SeanceDAO seancesDAO = new SeanceDAO();
        Utilisateur admin = utilisateurs.chercherParEmail("admin@mail.fr");
        Matiere algo = matiere(g, admin, "Algorithmique");
        Matiere maths = matiere(g, admin, "Mathématiques");

        // R1 : inscription
        attendreRefus("Inscription hors liste du campus (R1)",
                () -> g.inscrire("inconnu@mail.fr", "X", "Y", "mdp"));
        Etudiant pahola = g.inscrire("pahola@mail.fr", "Lago", "Pahola", "m1");
        verifier("Inscription d'un L2 : compte créé, pas tuteur",
                pahola.getNiveau().toString().equals("L2") && !pahola.isEstTuteurActif());

        // Tuteurs
        attendreRefus("Un L2 devient tuteur",
                () -> g.definirProfilTuteur(pahola, algo, DayOfWeek.TUESDAY, LocalTime.of(18, 0)));
        Etudiant yohan = g.inscrire("yohan@mail.fr", "Sokegbe", "Yohan", "m2");
        attendreRefus("Un L3 active son statut sans profil complet", () -> g.activerTuteur(yohan));
        g.definirProfilTuteur(yohan, algo, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(yohan);
        verifier("Un L3 active son statut avec un profil complet",
                ((Etudiant) utilisateurs.chercherParId(yohan.getId())).isEstTuteurActif());

        Etudiant alice = g.inscrire("alice@mail.fr", "Martin", "Alice", "m3");
        g.definirProfilTuteur(alice, maths, DayOfWeek.TUESDAY, LocalTime.of(18, 0));
        g.activerTuteur(alice);

        // R5, R6, R4
        Etudiant ines = g.inscrire("ines@mail.fr", "Girard", "Inès", "m4");
        g.envoyerDemande(ines, yohan);
        attendreRefus("2e demande au même tuteur (R5)", () -> g.envoyerDemande(ines, yohan));
        attendreRefus("Deux tuteurs au même jour et à la même heure (R6)",
                () -> g.envoyerDemande(ines, alice));
        g.definirProfilTuteur(alice, maths, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0));
        Demande dAlice = g.envoyerDemande(ines, alice);
        verifier("Deux tuteurs de matières et d'horaires différents (R4)",
                dAlice.getStatut() == StatutDemande.EN_ATTENTE);

        // R3 : capacité
        Etudiant hugo = g.inscrire("hugo@mail.fr", "Roux", "Hugo", "m5");
        Etudiant chloe = g.inscrire("chloe@mail.fr", "Petit", "Chloé", "m6");
        Etudiant dylan = g.inscrire("dylan@mail.fr", "Leroy", "Dylan", "m7");
        Demande d1 = g.envoyerDemande(pahola, yohan);
        Demande d2 = g.envoyerDemande(hugo, yohan);
        Demande d3 = g.envoyerDemande(chloe, yohan);
        Demande d4 = g.envoyerDemande(dylan, yohan);
        g.validerDemande(yohan, d1);
        g.validerDemande(yohan, d2);
        g.validerDemande(yohan, d3);
        attendreRefus("Un tuteur accepte une 4e demande (R3)", () -> g.validerDemande(yohan, d4));

        // Messagerie (R7)
        attendreRefus("Message dans une demande non acceptée (R7)",
                () -> g.envoyerMessage(dylan, d4, "Bonjour"));
        g.envoyerMessage(pahola, d1, "Bonjour, quand commence-t-on ?");
        g.envoyerMessage(yohan, d1, "Mardi à 18 h.");
        verifier("Conversation de 2 messages", g.consulterConversation(pahola, d1).size() == 2);
        attendreRefus("Un tiers écrit dans la conversation",
                () -> g.envoyerMessage(hugo, d1, "Je m'incruste"));
        attendreRefus("Un tiers lit la conversation", () -> g.consulterConversation(hugo, d1));
        attendreRefus("Message vide", () -> g.envoyerMessage(pahola, d1, "   "));

        // Présence, absence, rapports et R8
        SeanceTutorat seance = g.enregistrerSeance(yohan, LocalDate.now(), 60);
        g.enregistrerPresence(yohan, seance, hugo);
        g.enregistrerAbsence(yohan, seance, chloe);
        attendreRefus("Rapport pour un tutoré absent",
                () -> g.redigerRapport(yohan, seance, chloe, 4, "Test"));
        attendreRefus("Présence d'un étudiant non suivi",
                () -> g.enregistrerPresence(yohan, seance, ines));
        attendreRefus("Un tutoré déjà pointé absent est pointé présent",
                () -> g.enregistrerPresence(yohan, seance, chloe));
        attendreRefus("Note de 6 dans un rapport",
                () -> g.redigerRapport(yohan, seance, hugo, 6, "Test"));
        g.redigerRapport(yohan, seance, hugo, 4, "Observation confidentielle");
        verifier("Le tutoré voit sa note (R8)",
                g.consulterProgression(hugo).size() == 1 && g.consulterProgression(hugo).get(0) == 4);
        attendreRefus("Un tutoré lit les rapports complets (R8)", () -> g.consulterRapports(hugo));
        verifier("L'administration connaît l'absence de Chloé",
                g.mesNotifications(admin).stream().anyMatch(m -> m.contains("Absence")));
        verifier("Statistique d'absences : 1 pour Petit",
                Integer.valueOf(1).equals(g.absencesParTutore(admin).get("Petit")));

        // Évaluation du tuteur
        attendreRefus("Évaluer un tuteur sans avoir assisté à une séance",
                () -> g.evaluerTuteur(pahola, d1, 5, "Super"));
        attendreRefus("Évaluer le suivi d'un autre étudiant", () -> g.evaluerTuteur(hugo, d1, 4, "Test"));
        attendreRefus("Note de 7 pour un tuteur", () -> g.evaluerTuteur(hugo, d2, 7, "Test"));
        g.evaluerTuteur(hugo, d2, 4, "Explications claires.");
        verifier("Yohan voit sa moyenne : 4.0", Double.valueOf(4.0).equals(g.consulterMaMoyenne(yohan)));
        verifier("Pas d'évaluation : pas de moyenne", g.consulterMaMoyenne(alice) == null);
        attendreRefus("Évaluer deux fois le même suivi", () -> g.evaluerTuteur(hugo, d2, 5, "Encore"));
        attendreRefus("Un tutoré lit toutes les évaluations", () -> g.consulterEvaluations(hugo));
        verifier("L'administrateur voit 1 évaluation", g.consulterEvaluations(admin).size() == 1);

        // Statistiques supplémentaires
        g.refuserDemande(yohan, d4);
        verifier("Taux d'acceptation : 3 acceptées sur 4 traitées",
                Math.abs(g.tauxAcceptation(admin) - 0.75) < 1e-9);
        verifier("Évaluation moyenne de Sokegbe : 4.0",
                Double.valueOf(4.0).equals(g.moyenneEvaluationsParTuteur(admin).get("Sokegbe")));
        attendreRefus("Un tutoré lit le taux d'acceptation", () -> g.tauxAcceptation(hugo));

        // Exercices et rendus
        attendreRefus("Publier un exercice sans être tuteur", () -> g.publierExercice(hugo, "TD1", "td1.pdf"));
        attendreRefus("Exercice au format interdit", () -> g.publierExercice(yohan, "TD1", "td1.exe"));
        attendreRefus("Exercice sans titre", () -> g.publierExercice(yohan, "   ", "td1.pdf"));
        Exercice td1 = g.publierExercice(yohan, "TD1 : les boucles", "exercices/td1.pdf");
        verifier("Hugo voit l'exercice", g.consulterExercices(hugo).size() == 1);
        verifier("Dylan (non suivi) ne le voit pas", g.consulterExercices(dylan).isEmpty());
        attendreRefus("Rendu d'un tutoré non suivi", () -> g.deposerRendu(dylan, td1, "rendu.pdf"));
        attendreRefus("Rendu au format interdit", () -> g.deposerRendu(hugo, td1, "rendu.zip"));
        g.deposerRendu(hugo, td1, "rendus/hugo_td1.docx");
        attendreRefus("Deuxième rendu du même exercice", () -> g.deposerRendu(hugo, td1, "rendus/v2.pdf"));
        verifier("Yohan voit 1 rendu", g.consulterRendus(yohan, td1).size() == 1);
        attendreRefus("Un tutoré lit les rendus", () -> g.consulterRendus(hugo, td1));

        // Report et annulation de séance
        SeanceTutorat seance2 = g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(1), 60);
        SeanceTutorat seance3 = g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(2), 60);
        attendreRefus("Report vers une semaine déjà occupée (R2)",
                () -> g.reporterSeance(yohan, seance2, LocalDate.now().plusWeeks(2)));
        attendreRefus("Report dans le passé",
                () -> g.reporterSeance(yohan, seance2, LocalDate.now().minusDays(1)));
        attendreRefus("Un autre étudiant reporte la séance",
                () -> g.reporterSeance(hugo, seance2, LocalDate.now().plusWeeks(5)));
        g.reporterSeance(yohan, seance2, LocalDate.now().plusWeeks(3));
        verifier("Séance reportée",
                seancesDAO.chercherParId(seance2.getId()).getDateSeance().equals(LocalDate.now().plusWeeks(3)));
        verifier("Hugo est prévenu du report",
                g.mesNotifications(hugo).stream().anyMatch(m -> m.contains("reportée")));
        g.annulerSeance(yohan, seance3);
        verifier("Séance annulée",
                seancesDAO.chercherParId(seance3.getId()).getStatut() == StatutSeance.ANNULEE);
        attendreRefus("Annuler deux fois", () -> g.annulerSeance(yohan, seance3));
        attendreRefus("Annuler une séance déjà pointée", () -> g.annulerSeance(yohan, seance));
        attendreRefus("Pointer une séance annulée", () -> g.enregistrerPresence(yohan, seance3, hugo));
        g.enregistrerSeance(yohan, LocalDate.now().plusWeeks(2), 60);
        verifier("La semaine d'une séance annulée est libérée (R2)", true);
        verifier("Statistique : 3 séances, l'annulée n'est pas comptée", g.nombreSeances(admin) == 3);

        // Listes et matières
        attendreRefus("Un tutoré crée une matière", () -> g.ajouterMatiere(hugo, "Chimie test"));
        g.ajouterMatiere(admin, "Chimie test");
        attendreRefus("Matière en double", () -> g.ajouterMatiere(admin, "chimie test"));
        verifier("7 matières listées", g.listerMatieres(hugo).size() == 7);
        verifier("Yohan a reçu 5 demandes", g.demandesRecues(yohan).size() == 5);
        verifier("Hugo a envoyé 1 demande", g.demandesEnvoyees(hugo).size() == 1);
        verifier("Inès a envoyé 2 demandes", g.demandesEnvoyees(ines).size() == 2);
        verifier("Yohan a 3 tutorés", g.mesTutores(yohan).size() == 3);
        verifier("Yohan a 4 séances, dont 1 annulée", g.mesSeances(yohan).size() == 4);
        verifier("Hugo a des notifications", !g.mesNotifications(hugo).isEmpty());
        attendreRefus("L'administrateur consulte ses tutorés", () -> g.mesTutores(admin));

        // Contrat Accompagnant
        attendreRefus("Un autre tuteur accepte la demande", () -> g.validerDemande(yohan, dAlice));
        attendreRefus("Accepter une demande déjà acceptée", () -> g.validerDemande(yohan, d1));
        attendreRefus("Un tuteur refuse la demande d'un autre", () -> g.refuserDemande(yohan, dAlice));
        attendreRefus("Etudiant.validerDemande appelée directement sur la demande d'un autre",
                () -> yohan.validerDemande(dAlice));
        attendreRefus("Etudiant.refuserDemande appelée directement sur la demande d'un autre",
                () -> yohan.refuserDemande(dAlice));

        
        // Changement de mot de passe
attendreRefus("Ancien mot de passe incorrect",
        () -> g.changerMotDePasse(pahola, "faux", "nouveauMdp1"));
attendreRefus("Nouveau mot de passe identique à l'ancien",
        () -> g.changerMotDePasse(pahola, "m1", "m1"));
attendreRefus("Nouveau mot de passe trop court",
        () -> g.changerMotDePasse(pahola, "m1", "court"));
g.changerMotDePasse(pahola, "m1", "nouveauMdp1");
verifier("Connexion avec le nouveau mot de passe",
        g.connecter("pahola@mail.fr", "nouveauMdp1").getId() == pahola.getId());
attendreRefus("L'ancien mot de passe ne fonctionne plus",
        () -> g.connecter("pahola@mail.fr", "m1"));
verifier("L'utilisateur est prévenu du changement",
        g.mesNotifications(pahola).stream().anyMatch(m -> m.contains("mot de passe")));
        // Rôles
        attendreRefus("Un tutoré appelle une méthode réservée à l'administration",
                () -> g.nombreSeances(hugo));

        // Retrait : toujours en dernier
        g.retirerTuteur(admin, yohan);
        verifier("L'administrateur retire un tuteur : ses suivis passent à TERMINEE",
                demandes.chercherParId(d1.getId()).getStatut() == StatutDemande.TERMINEE
                && demandes.chercherParId(d2.getId()).getStatut() == StatutDemande.TERMINEE);
        attendreRefus("Message après la fin du suivi", () -> g.envoyerMessage(pahola, d1, "Merci pour tout"));

        reinitialiser();
        System.out.println("---- Résultat : " + ok + " OK, " + echecs + " ECHEC ----");
    }
}