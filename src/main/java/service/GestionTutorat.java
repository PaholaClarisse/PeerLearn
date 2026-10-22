/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author Glamor Store
 */
import java.util.Objects;
import java.util.HashSet;
import modele.Message;
import dao.RapportDAO;
import dao.SeanceDAO;
import dao.EvaluationDAO;
import dao.ExerciceDAO;
import dao.MessageDAO;
import dao.NotificationDAO;
import dao.RenduDAO;
import modele.Notification;
import java.sql.Connection;
import java.sql.SQLException;
import util.ConnexionBD;
import dao.CampusDAO;
import dao.UtilisateurDAO;
import util.MotDePasse;
import modele.EvaluationTuteur;
import modele.StatutSeance;
import modele.Exercice;
import modele.Rendu;
import java.util.ArrayList;
import java.util.HashMap;
import modele.Demande;
import modele.Matiere;
import modele.Niveau;
import modele.RapportSuivi;
import modele.SeanceTutorat;
import modele.Utilisateur;
import modele.Administrateur;
import modele.Etudiant;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;
import modele.StatutDemande;
import dao.DemandeDAO;
import dao.MatiereDAO;
import java.time.temporal.TemporalAdjusters;

public class GestionTutorat {
    private final MessageDAO messageDAO = new MessageDAO();
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();
    private final ExerciceDAO exerciceDAO = new ExerciceDAO();
    private final RenduDAO renduDAO = new RenduDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final RapportDAO rapportDAO = new RapportDAO();
    private final DemandeDAO demandeDAO = new DemandeDAO();
    private final MatiereDAO matiereDAO = new MatiereDAO();
    private final UtilisateurDAO utilisateursDAO = new UtilisateurDAO();
    private final CampusDAO campusDAO = new CampusDAO();
  

    public GestionTutorat() {
}
    
    public Etudiant inscrire(String email, String nom, String prenom, String mdp) {
    Niveau niveau = campusDAO.chercherNiveau(email);
    if (niveau == null) {
        throw new RegleGestionException("Cette adresse n'est pas dans la liste de l'université.");
    }
    if (nom == null || nom.trim().isEmpty() || prenom == null || prenom.trim().isEmpty()) {
        throw new RegleGestionException("Le nom et le prénom sont obligatoires.");
    }
    if (utilisateursDAO.chercherParEmail(email) != null) {
        throw new RegleGestionException("Un compte existe déjà avec cette adresse.");
    }
    return utilisateursDAO.ajouterEtudiant(nom.trim(), prenom.trim(), email,
            MotDePasse.hacher(mdp), niveau);
}

public Utilisateur connecter(String email, String mdp) {
    Utilisateur u = utilisateursDAO.chercherParEmail(email);
    if (u == null || !MotDePasse.verifier(mdp, u.getMdp())) {
        throw new RegleGestionException("Adresse ou mot de passe incorrect.");
    }
    if (u instanceof Etudiant) {
        Niveau niveauCampus = campusDAO.chercherNiveau(email);
        if (niveauCampus != null && niveauCampus != ((Etudiant) u).getNiveau()) {
            utilisateursDAO.modifierNiveau(u.getId(), niveauCampus);
            u = utilisateursDAO.chercherParId(u.getId());
        }
    }
    return u;
}
    private Etudiant exigerEtudiant(Utilisateur connecte) {
    if (!(connecte instanceof Etudiant)) {
        throw new RegleGestionException("Cette action est réservée aux étudiants.");
    }
    return chargerEtudiant(connecte.getId());
}

private Etudiant chargerEtudiant(int id) {
    Utilisateur u = utilisateursDAO.chercherParId(id);
    if (!(u instanceof Etudiant)) {
        throw new RegleGestionException("Compte introuvable.");
    }
    Etudiant e = (Etudiant) u;
    e.restaurerTutores(demandeDAO.listerTutoresAcceptes(id));
    return e;
}
private Demande chargerDemande(Demande d) {
    if (d == null) {
        throw new RegleGestionException("Demande invalide.");
    }
    Demande relue = demandeDAO.chercherParId(d.getId());
    if (relue == null) {
        throw new RegleGestionException("Cette demande n'existe plus.");
    }
    return relue;
}
private void notifier(Utilisateur destinataire, String message) {
    notificationDAO.ajouter(destinataire.getId(), message);
}

public void definirProfilTuteur(Utilisateur connecte, Matiere matiere, DayOfWeek jour, LocalTime heure) {
    Etudiant e = exigerEtudiant(connecte);
    e.choisirMatiere(matiere);
    e.proposerCreneau(jour, heure);
    utilisateursDAO.modifierProfil(e);
}

public void activerTuteur(Utilisateur connecte) {
    Etudiant e = exigerEtudiant(connecte);
    e.activerTuteur();
    utilisateursDAO.modifierProfil(e);
}

public void mettreEnPause(Utilisateur connecte) {
    Etudiant e = exigerEtudiant(connecte);
    e.mettreEnPause();
    utilisateursDAO.modifierProfil(e);
}

public ArrayList<Etudiant> rechercherTuteurs(Utilisateur connecte, Matiere matiere) {
    Etudiant demandeur = exigerEtudiant(connecte);
    ArrayList<Etudiant> resultat = new ArrayList<>();
    for (Utilisateur u : utilisateursDAO.lister()) {
        if (u instanceof Etudiant) {
            Etudiant t = (Etudiant) u;
            if (!Objects.equals(t, demandeur)
                    && t.isEstTuteurActif()
                    && Objects.equals(t.getMatiere(), matiere)) {
                t.restaurerTutores(demandeDAO.listerTutoresAcceptes(t.getId()));
                if (t.getTutores().size() < t.getCapaciteMax()) {
                    resultat.add(t);
                }
            }
        }
    }
    return resultat;
}


private boolean estActive(Demande d) {
    return Objects.equals(d.getStatut(),StatutDemande.EN_ATTENTE)
        || Objects.equals(d.getStatut(),StatutDemande.ACCEPTEE);
}
public Demande envoyerDemande(Utilisateur connecte, Etudiant tuteurChoisi) {
    Etudiant tutore = exigerEtudiant(connecte);
    if (tuteurChoisi == null) {
        throw new RegleGestionException("Ce tuteur n'est pas disponible.");
    }
    Etudiant tuteur = chargerEtudiant(tuteurChoisi.getId());
    if (!tuteur.isEstTuteurActif()) {
        throw new RegleGestionException("Ce tuteur n'est pas disponible.");
    }

    for (Demande d : demandeDAO.listerParTutore(tutore.getId())) {
        if (!estActive(d)) {
            continue;
        }
        // R5 : une seule demande active par tuteur
        if (Objects.equals(d.getTuteur(), tuteur)) {
            throw new RegleGestionException("Vous avez déjà une demande active auprès de ce tuteur.");
        }
        // R4 : un seul tuteur par matière
        if (Objects.equals(d.getMatiere(), tuteur.getMatiere())) {
            throw new RegleGestionException("Vous avez déjà un tuteur pour cette matière.");
        }
        // R6 : pas de chevauchement d'horaire
        if (Objects.equals(d.getTuteur().getJour(), tuteur.getJour())
                && Objects.equals(d.getTuteur().getHeure(), tuteur.getHeure())) {
            throw new RegleGestionException("Cet horaire chevauche celui d'un de vos autres tuteurs.");
        }
    }

    Demande demande = demandeDAO.ajouter(new Demande(tutore, tuteur, tuteur.getMatiere()));
   notifier(tuteur,tutore.getNom() + " vous a envoyé une demande de tutorat en "
            + tuteur.getMatiere().getNom() + ".");
    return demande;
}

public void annulerDemande(Utilisateur connecte, Demande demande) {
    Etudiant tutore = exigerEtudiant(connecte);
    Demande d = chargerDemande(demande);
    if (!Objects.equals(d.getTutore(), tutore)) {
        throw new RegleGestionException("Vous ne pouvez annuler que vos propres demandes.");
    }
    d.annuler();
    demandeDAO.modifierStatut(d);
    notifier(d.getTuteur(),tutore.getNom() + " a annulé sa demande.");
}

public void refuserDemande(Utilisateur connecte, Demande demande) {
    Etudiant tuteur = exigerEtudiant(connecte);
    Demande d = chargerDemande(demande);
    tuteur.refuserDemande(d);
    demandeDAO.modifierStatut(d);
    notifier(d.getTutore(), "Votre demande auprès de " + tuteur.getNom() + " a été refusée.");
}

public void terminerSuivi(Utilisateur connecte, Demande demande) {
    Etudiant tutore = exigerEtudiant(connecte);
    Demande d = chargerDemande(demande);
    if (!Objects.equals(d.getTutore(), tutore)) {
        throw new RegleGestionException("Vous ne pouvez terminer que vos propres suivis.");
    }
    d.terminer();
    demandeDAO.modifierStatut(d);
}

public ArrayList<Demande> demandesRecues(Utilisateur connecte) {
    return demandeDAO.listerParTuteur(exigerEtudiant(connecte).getId());
}

public ArrayList<Demande> demandesEnvoyees(Utilisateur connecte) {
    return demandeDAO.listerParTutore(exigerEtudiant(connecte).getId());
}

public ArrayList<Etudiant> mesTutores(Utilisateur connecte) {
    return exigerEtudiant(connecte).getTutores();
}


public void validerDemande(Utilisateur connecte, Demande demande) {
    exigerEtudiant(connecte);
    try (Connection c = ConnexionBD.getConnexion()) {
        c.setAutoCommit(false);
        try {
            demandeDAO.verrouillerTuteur(c, connecte.getId());

            Etudiant tuteur = chargerEtudiant(connecte.getId());
            Demande d = chargerDemande(demande);
            if (!Objects.equals(d.getTuteur(), tuteur)) {
                throw new RegleGestionException("Cette demande ne vous est pas adressée.");
            }

            // R6 : revérifier l'horaire à l'acceptation
            if (d.getStatut() == StatutDemande.EN_ATTENTE) {
                for (Demande autre : demandeDAO.listerParTutore(d.getTutore().getId())) {
                    if (autre.getStatut() == StatutDemande.ACCEPTEE
                            && Objects.equals(autre.getTuteur().getJour(), tuteur.getJour())
                            && Objects.equals(autre.getTuteur().getHeure(), tuteur.getHeure())) {
                        throw new RegleGestionException("Le tutoré a déjà un suivi accepté à cet horaire.");
                    }
                }
            }

            // R3, statut et destinataire : vérifiés par Etudiant
            tuteur.validerDemande(d);
            demandeDAO.modifierStatut(c, d);
            c.commit();

            notifier(d.getTutore(),"Votre demande auprès de " + tuteur.getNom() + " a été acceptée.");
        } catch (RuntimeException | SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    } catch (SQLException e) {
        throw new RegleGestionException("Erreur d'accès à la base de données.");
    }
}

public SeanceTutorat enregistrerSeance(Utilisateur connecte, LocalDate date, int dureeMinutes) {
    Etudiant tuteur = exigerEtudiant(connecte);
    if (!tuteur.isEstTuteurActif()) {
        throw new RegleGestionException("Seuls les tuteurs actifs peuvent enregistrer une séance.");
    }
    if (date == null) {
        throw new RegleGestionException("La date de la séance est obligatoire.");
    }
    if (tuteurOccupeCetteSemaine(tuteur, date, null)) {
        throw new RegleGestionException("Une séance est déjà enregistrée cette semaine.");
    }
    return seanceDAO.ajouter(new SeanceTutorat(tuteur, date, dureeMinutes));
}
private Administrateur exigerAdministrateur(Utilisateur connecte) {
    if (!(connecte instanceof Administrateur)) {
        throw new RegleGestionException("Cette action est réservée à l'administration.");
    }
    return (Administrateur) connecte;
}
private SeanceTutorat chargerSeance(SeanceTutorat s) {
    if (s == null) {
        throw new RegleGestionException("Séance invalide.");
    }
    SeanceTutorat relue = seanceDAO.chercherParId(s.getId());
    if (relue == null) {
        throw new RegleGestionException("Cette séance n'existe plus.");
    }
    return relue;
}

public RapportSuivi redigerRapport(Utilisateur connecte, SeanceTutorat seance,
                                   Etudiant tutore, int note, String observations) {
    Etudiant tuteur = exigerEtudiant(connecte);
    SeanceTutorat s = chargerSeance(seance);
    if (!Objects.equals(s.getTuteur(), tuteur)) {
        throw new RegleGestionException("Vous ne pouvez rédiger un rapport que pour vos propres séances.");
    }
    if (!tuteur.getTutores().contains(tutore)) {
        throw new RegleGestionException("Cet étudiant n'est pas suivi par vous.");
    }
    if (!s.estPresent(tutore)) {
        throw new RegleGestionException("Ce tutoré n'était pas présent à cette séance.");
    }
    for (RapportSuivi r : rapportDAO.listerParTutore(tutore.getId())) {
        if (Objects.equals(r.getSeance(), s)) {
            throw new RegleGestionException("Un rapport existe déjà pour cette séance et ce tutoré.");
        }
    }
    RapportSuivi rapport = rapportDAO.ajouter(new RapportSuivi(s, tutore, note, observations));
    for (Utilisateur u : utilisateursDAO.lister()) {
        if (u instanceof Administrateur) {
            notifier(u,"Nouveau rapport : " + tuteur.getNom() + " a évalué "
                    + tutore.getNom() + " (note " + note + ").");
        }
    }
    notifier(tutore,"Votre tuteur " + tuteur.getNom() + " a rédigé un rapport : note " + note + ".");
    return rapport;
}

public ArrayList<Integer> consulterProgression(Utilisateur connecte) {
    Etudiant tutore = exigerEtudiant(connecte);
    ArrayList<Integer> notes = new ArrayList<>();
    for (RapportSuivi r : rapportDAO.listerParTutore(tutore.getId())) {
        notes.add(r.getNote());
    }
    return notes;
}

public ArrayList<RapportSuivi> consulterRapports(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    return rapportDAO.lister();
}
public void retirerTuteur(Utilisateur connecte, Etudiant tuteurChoisi) {
    exigerAdministrateur(connecte);
    if (tuteurChoisi == null) {
        throw new RegleGestionException("Tuteur invalide.");
    }
    Etudiant tuteur = chargerEtudiant(tuteurChoisi.getId());
    if (tuteur.isRetire()) {
        throw new RegleGestionException("Ce tuteur est déjà retiré.");
    }
    ArrayList<Etudiant> aPrevenir = new ArrayList<>();
    try (Connection c = ConnexionBD.getConnexion()) {
        c.setAutoCommit(false);
        try {
            for (Demande d : demandeDAO.listerParTuteur(tuteur.getId())) {
                if (d.getStatut() == StatutDemande.ACCEPTEE) {
                    d.terminer();
                    demandeDAO.modifierStatut(c, d);
                    aPrevenir.add(d.getTutore());
                } else if (d.getStatut() == StatutDemande.EN_ATTENTE) {
                    d.refuser();
                    demandeDAO.modifierStatut(c, d);
                }
            }
            tuteur.retirer();
            utilisateursDAO.modifierProfil(c, tuteur);
            c.commit();
        } catch (RuntimeException | SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    } catch (SQLException e) {
        throw new RegleGestionException("Erreur d'accès à la base de données.");
    }
    for (Etudiant t : aPrevenir) {
        notifier(t, "Votre tuteur " + tuteur.getNom() + " a été retiré : votre suivi est terminé.");
    }
}

public int nombreTuteursActifs(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    int n = 0;
    for (Utilisateur u : utilisateursDAO.lister()) {
        if (u instanceof Etudiant && ((Etudiant) u).isEstTuteurActif()) {
            n++;
        }
    }
    return n;
}

public int nombreTutoresSuivis(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    HashSet<Etudiant> suivis = new HashSet<>();
    for (Demande d : demandeDAO.lister()) {
        if (d.getStatut() == StatutDemande.ACCEPTEE) {
            suivis.add(d.getTutore());
        }
    }
    return suivis.size();
}

public int nombreSeances(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    int n = 0;
    for (SeanceTutorat s : seanceDAO.lister()) {
        if (s.getStatut() == StatutSeance.PLANIFIEE) {
            n++;
        }
    }
    return n;
}


public HashMap<String, Integer> demandesParMatiere(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    HashMap<String, Integer> resultat = new HashMap<>();
    for (Demande d : demandeDAO.lister()) {
        String nom = d.getMatiere().getNom();
        resultat.put(nom, resultat.getOrDefault(nom, 0) + 1);
    }
    return resultat;
}
public HashMap<String, Double> moyenneNotesParTuteur(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    HashMap<String, Integer> sommes = new HashMap<>();
    HashMap<String, Integer> comptes = new HashMap<>();
    for (RapportSuivi r : rapportDAO.lister()) {
        String tuteur = r.getSeance().getTuteur().getNom();
        sommes.put(tuteur, sommes.getOrDefault(tuteur, 0) + r.getNote());
        comptes.put(tuteur, comptes.getOrDefault(tuteur, 0) + 1);
    }
    HashMap<String, Double> moyennes = new HashMap<>();
    for (String tuteur : sommes.keySet()) {
        moyennes.put(tuteur, (double) sommes.get(tuteur) / comptes.get(tuteur));
    }
    return moyennes;
}

public void enregistrerPresence(Utilisateur connecte, SeanceTutorat seance, Etudiant tutore) {
    Etudiant tuteur = exigerEtudiant(connecte);
    SeanceTutorat s = chargerSeance(seance);
    if (!Objects.equals(s.getTuteur(), tuteur)) {
        throw new RegleGestionException("Vous ne pouvez gérer que les présences de vos propres séances.");
    }
    if (!tuteur.getTutores().contains(tutore)) {
        throw new RegleGestionException("Cet étudiant n'est pas suivi par vous.");
    }
    s.marquerPresent(tutore);
    seanceDAO.enregistrerPointage(s, tutore, true);
}

public void enregistrerAbsence(Utilisateur connecte, SeanceTutorat seance, Etudiant tutore) {
    Etudiant tuteur = exigerEtudiant(connecte);
    SeanceTutorat s = chargerSeance(seance);
    if (!Objects.equals(s.getTuteur(), tuteur)) {
        throw new RegleGestionException("Vous ne pouvez gérer que les présences de vos propres séances.");
    }
    if (!tuteur.getTutores().contains(tutore)) {
        throw new RegleGestionException("Cet étudiant n'est pas suivi par vous.");
    }
    s.marquerAbsent(tutore);
    seanceDAO.enregistrerPointage(s, tutore, false);
    for (Utilisateur u : utilisateursDAO.lister()) {
        if (u instanceof Administrateur) {
            notifier(u,"Absence : " + tutore.getNom() + " n'était pas présent à la séance de "
                    + tuteur.getNom() + ".");
        }
    }
    notifier(tutore,"Votre absence à la séance de " + tuteur.getNom() + " a été enregistrée.");
}
public HashMap<String, Integer> absencesParTutore(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    HashMap<String, Integer> resultat = new HashMap<>();
    for (SeanceTutorat s : seanceDAO.lister()) {
        for (Etudiant e : s.getAbsents()) {
            resultat.put(e.getNom(), resultat.getOrDefault(e.getNom(), 0) + 1);
        }
    }
    return resultat;
}

private void exigerParticipant(Etudiant e, Demande demande) {
    if (!Objects.equals(demande.getTutore(), e) && !Objects.equals(demande.getTuteur(),e)) {
        throw new RegleGestionException("Vous ne participez pas à cette conversation.");
    }
}
private Exercice chargerExercice(Exercice x) {
    if (x == null) {
        throw new RegleGestionException("Exercice invalide.");
    }
    Exercice relu = exerciceDAO.chercherParId(x.getId());
    if (relu == null) {
        throw new RegleGestionException("Cet exercice n'existe plus.");
    }
    return relu;
}
public Message envoyerMessage(Utilisateur connecte, Demande demande, String contenu) {
    Etudiant expediteur = exigerEtudiant(connecte);
    Demande d = chargerDemande(demande);
    exigerParticipant(expediteur, d);
    if (d.getStatut() != StatutDemande.ACCEPTEE) {
        throw new RegleGestionException("La messagerie n'est ouverte que pour une demande acceptée.");
    }
    Message message = messageDAO.ajouter(new Message(d, expediteur, contenu));
    Etudiant destinataire = Objects.equals(d.getTutore(), expediteur) ? d.getTuteur() : d.getTutore();
    notifier(destinataire, "Nouveau message de " + expediteur.getNom() + ".");
    return message;
}

public ArrayList<Message> consulterConversation(Utilisateur connecte, Demande demande) {
    Etudiant lecteur = exigerEtudiant(connecte);
    Demande d = chargerDemande(demande);
    exigerParticipant(lecteur, d);
    return messageDAO.listerParDemande(d);
}

private LocalDate lundiDe(LocalDate date) {
    return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
}

private boolean tuteurOccupeCetteSemaine(Etudiant tuteur, LocalDate date, SeanceTutorat sauf) {
    for (SeanceTutorat s : seanceDAO.listerParTuteur(tuteur.getId())) {
        if (!Objects.equals(s, sauf)
                && s.getStatut() == StatutSeance.PLANIFIEE
                && lundiDe(s.getDateSeance()).equals(lundiDe(date))) {
            return true;
        }
    }
    return false;
}

public void annulerSeance(Utilisateur connecte, SeanceTutorat seance) {
    Etudiant tuteur = exigerEtudiant(connecte);
    SeanceTutorat s = chargerSeance(seance);
    if (!Objects.equals(s.getTuteur(), tuteur)) {
        throw new RegleGestionException("Cette séance n'est pas la vôtre.");
    }
    s.annuler();
    seanceDAO.modifier(s);
    for (Etudiant t : tuteur.getTutores()) {
        notifier(t,"La séance de " + tuteur.getNom() + " du " + s.getDateSeance() + " est annulée.");
    }
}


public void reporterSeance(Utilisateur connecte, SeanceTutorat seance, LocalDate nouvelleDate) {
    Etudiant tuteur = exigerEtudiant(connecte);
    SeanceTutorat s = chargerSeance(seance);
    if (!Objects.equals(s.getTuteur(), tuteur)) {
        throw new RegleGestionException("Cette séance n'est pas la vôtre.");
    }
    s.verifierModifiable();
    if (nouvelleDate == null) {
        throw new RegleGestionException("La nouvelle date est obligatoire.");
    }
    if (nouvelleDate.isBefore(LocalDate.now())) {
        throw new RegleGestionException("La nouvelle date doit être aujourd'hui ou plus tard.");
    }
    if (tuteurOccupeCetteSemaine(tuteur, nouvelleDate, s)) {
        throw new RegleGestionException("Vous avez déjà une séance cette semaine-là.");
    }
    LocalDate ancienne = s.getDateSeance();
    s.reporter(nouvelleDate);
    seanceDAO.modifier(s);
    for (Etudiant t : tuteur.getTutores()) {
        notifier(t,"La séance de " + tuteur.getNom() + " du " + ancienne
                + " est reportée au " + nouvelleDate + ".");
    }
}
private ArrayList<EvaluationTuteur> evaluations = new ArrayList<>();

public EvaluationTuteur evaluerTuteur(Utilisateur connecte, Demande demande, int note, String commentaire) {
    Etudiant tutore = exigerEtudiant(connecte);
    Demande d = chargerDemande(demande);
    if (!Objects.equals(d.getTutore(), tutore)) {
        throw new RegleGestionException("Vous ne pouvez évaluer que vos propres tuteurs.");
    }
    if (d.getStatut() != StatutDemande.ACCEPTEE && d.getStatut() != StatutDemande.TERMINEE) {
        throw new RegleGestionException("Seul un suivi accepté ou terminé peut être évalué.");
    }
    boolean aAssiste = false;
    for (SeanceTutorat s : seanceDAO.listerParTuteur(d.getTuteur().getId())) {
        if (s.estPresent(tutore)) {
            aAssiste = true;
        }
    }
    if (!aAssiste) {
        throw new RegleGestionException("Vous devez avoir assisté à au moins une séance de ce tuteur.");
    }
    if (evaluationDAO.chercherParDemande(d.getId()) != null) {
        throw new RegleGestionException("Vous avez déjà évalué ce tuteur pour ce suivi.");
    }
    EvaluationTuteur evaluation = evaluationDAO.ajouter(new EvaluationTuteur(d, note, commentaire));
    notifier(d.getTuteur(), "Vous avez reçu une nouvelle évaluation.");
    return evaluation;
}
public Double consulterMaMoyenne(Utilisateur connecte) {
    Etudiant tuteur = exigerEtudiant(connecte);
    int somme = 0;
    int nombre = 0;
    for (EvaluationTuteur e : evaluationDAO.lister()) {
        if (Objects.equals(e.getDemande().getTuteur(), tuteur)) {
            somme += e.getNote();
            nombre++;
        }
    }
    if (nombre == 0) {
        return null;
    }
    return (double) somme / nombre;
}
public ArrayList<EvaluationTuteur> consulterEvaluations(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    return evaluationDAO.lister();
}

public Double tauxAcceptation(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    int acceptees = 0;
    int refusees = 0;
    for (Demande d : demandeDAO.lister()) {
        if (d.getStatut() == StatutDemande.ACCEPTEE || d.getStatut() == StatutDemande.TERMINEE) {
            acceptees++;
        } else if (d.getStatut() == StatutDemande.REFUSEE) {
            refusees++;
        }
    }
    if (acceptees + refusees == 0) {
        return null;
    }
    return (double) acceptees / (acceptees + refusees);
}

public HashMap<String, Double> moyenneEvaluationsParTuteur(Utilisateur connecte) {
    exigerAdministrateur(connecte);
    HashMap<String, Integer> sommes = new HashMap<>();
    HashMap<String, Integer> comptes = new HashMap<>();
    for (EvaluationTuteur e : evaluationDAO.lister()) {
        String tuteur = e.getDemande().getTuteur().getNom();
        sommes.put(tuteur, sommes.getOrDefault(tuteur, 0) + e.getNote());
        comptes.put(tuteur, comptes.getOrDefault(tuteur, 0) + 1);
    }
    HashMap<String, Double> moyennes = new HashMap<>();
    for (String tuteur : sommes.keySet()) {
        moyennes.put(tuteur, (double) sommes.get(tuteur) / comptes.get(tuteur));
    }
    return moyennes;
}

public Exercice publierExercice(Utilisateur connecte, String titre, String chemin) {
    Etudiant tuteur = exigerEtudiant(connecte);
    if (!tuteur.isEstTuteurActif()) {
        throw new RegleGestionException("Seuls les tuteurs actifs peuvent publier un exercice.");
    }
    Exercice exercice = exerciceDAO.ajouter(new Exercice(tuteur, titre, chemin));
    for (Etudiant t : tuteur.getTutores()) {
        notifier(t, tuteur.getNom() + " a publié un exercice : " + exercice.getTitre() + ".");
    }
    return exercice;
}

public ArrayList<Exercice> consulterExercices(Utilisateur connecte) {
    Etudiant tutore = exigerEtudiant(connecte);
    ArrayList<Exercice> resultat = new ArrayList<>();
    for (Demande d : demandeDAO.listerParTutore(tutore.getId())) {
        if (d.getStatut() == StatutDemande.ACCEPTEE) {
            resultat.addAll(exerciceDAO.listerParTuteur(d.getTuteur().getId()));
        }
    }
    return resultat;
}

public Rendu deposerRendu(Utilisateur connecte, Exercice exercice, String chemin) {
    Etudiant tutore = exigerEtudiant(connecte);
    Exercice x = chargerExercice(exercice);
    if (!demandeDAO.listerTutoresAcceptes(x.getTuteur().getId()).contains(tutore)) {
        throw new RegleGestionException("Cet exercice ne vous est pas destiné.");
    }
    if (renduDAO.chercherParExerciceEtTutore(x, tutore.getId()) != null) {
        throw new RegleGestionException("Vous avez déjà rendu cet exercice.");
    }
    Rendu rendu = renduDAO.ajouter(new Rendu(x, tutore, chemin));
    notifier(x.getTuteur(), tutore.getNom() + " a rendu l'exercice : " + x.getTitre() + ".");
    return rendu;
}

public ArrayList<Rendu> consulterRendus(Utilisateur connecte, Exercice exercice) {
    Etudiant tuteur = exigerEtudiant(connecte);
    Exercice x = chargerExercice(exercice);
    if (!Objects.equals(x.getTuteur(), tuteur)) {
        throw new RegleGestionException("Cet exercice n'est pas le vôtre.");
    }
    return renduDAO.listerParExercice(x);
} 

public Matiere ajouterMatiere(Utilisateur connecte, String nom) {
    exigerAdministrateur(connecte);
    if (nom == null || nom.trim().isEmpty()) {
        throw new RegleGestionException("Le nom de la matière est obligatoire.");
    }
    return matiereDAO.ajouter(nom.trim());
}

public ArrayList<Matiere> listerMatieres(Utilisateur connecte) {
    if (connecte == null) {
        throw new RegleGestionException("Vous devez être connecté.");
    }
    return matiereDAO.lister();
}

public ArrayList<SeanceTutorat> mesSeances(Utilisateur connecte) {
    return seanceDAO.listerParTuteur(exigerEtudiant(connecte).getId());
}

public ArrayList<String> mesNotifications(Utilisateur connecte) {
    if (connecte == null) {
        throw new RegleGestionException("Vous devez être connecté.");
    }
    ArrayList<String> resultat = new ArrayList<>();
    for (Notification n : notificationDAO.listerParUtilisateur(connecte.getId())) {
        resultat.add(n.getMessage());
    }
    return resultat;
}

public int nombreNotificationsNonLues(Utilisateur connecte) {
    if (connecte == null) {
        throw new RegleGestionException("Vous devez être connecté.");
    }
    return notificationDAO.compterNonLues(connecte.getId());
}

public void marquerNotificationsLues(Utilisateur connecte) {
    if (connecte == null) {
        throw new RegleGestionException("Vous devez être connecté.");
    }
    notificationDAO.marquerToutesLues(connecte.getId());
}
public void changerMotDePasse(Utilisateur connecte, String ancien, String nouveau) {
    if (connecte == null) {
        throw new RegleGestionException("Vous devez être connecté.");
    }
    Utilisateur u = utilisateursDAO.chercherParId(connecte.getId());
    if (u == null || !MotDePasse.verifier(ancien, u.getMdp())) {
        throw new RegleGestionException("L'ancien mot de passe est incorrect.");
    }
    if (nouveau != null && MotDePasse.verifier(nouveau, u.getMdp())) {
        throw new RegleGestionException("Le nouveau mot de passe doit être différent de l'ancien.");
    }
    if (nouveau == null || nouveau.length() < 8) {
        throw new RegleGestionException("Le nouveau mot de passe doit contenir au moins 8 caractères.");
    }
    utilisateursDAO.modifierMotDePasse(u.getId(), MotDePasse.hacher(nouveau));
    notifier(u, "Votre mot de passe a été modifié.");
}
}

