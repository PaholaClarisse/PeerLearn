/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modele;
import java.util.Objects;
import interfaces.Accompagnant;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import service.RegleGestionException;
/**
 *
 * @author Glamor Store
 */
public class Etudiant extends Utilisateur implements Accompagnant {
    private Niveau niveau;
    private boolean estTuteurActif;
    private int capaciteMax;
    private Matiere matiere;
    private boolean retire;
    private DayOfWeek jour;
    private LocalTime heure;
    private ArrayList<Etudiant> tutores;
    public Etudiant( int id,String nom, String prenom,String mail, String mdp, Niveau niveau){
         super(id, nom, prenom, mail,mdp);
         this.niveau = niveau;
         estTuteurActif = false;
         capaciteMax = 3;
         this.tutores = new ArrayList<Etudiant>();
     }
    
    @Override public String getRole(){
        return "ETUDIANT";
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public boolean isEstTuteurActif() {
        return estTuteurActif;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public DayOfWeek getJour() {
        return jour;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public ArrayList<Etudiant> getTutores() {
        return new ArrayList<>(tutores);
    }
    public void ajouterTutorer(Etudiant tutore){
        if(tutore== null || Objects.equals(tutore,this)){
             throw new RegleGestionException("Tutoré invalide.");
        }
        if (!estTuteurActif) {
        throw new RegleGestionException("Ce tuteur n'est pas actif.");
    }
        if (tutores.contains(tutore)) {
            throw new RegleGestionException("Cet étudiant est déjà suivi par ce tuteur.");
    }
    if (tutores.size() >= capaciteMax) {
        throw new RegleGestionException("Capacité maximale atteinte (" + capaciteMax + " tutorés).");
    }
  
    tutores.add(tutore);
    }    
    public void retirerTutore(Etudiant tutore) {
    tutores.remove(tutore);
}
    @Override public void proposerCreneau(DayOfWeek jour, LocalTime heure ){
        if (!niveau.peutTutorer()){
            throw new RegleGestionException("Seuls les etudiants de L3 ou plus peuvent devenir tuteur.");
        }
        if (!tutores.isEmpty()) {
           throw new RegleGestionException("Impossible de changer de créneau : ce tuteur a déjà des tutorés.");
        }
        this.jour = jour;
        this.heure = heure;
    }
   @Override
public void validerDemande(Demande demande) {
    if (demande==null || !Objects.equals(demande.getTuteur(),this)) {
        throw new RegleGestionException("Cette demande ne vous est pas adressée.");
    }
    if (!Objects.equals(demande.getStatut(),StatutDemande.EN_ATTENTE)) {
        throw new RegleGestionException("Seule une demande en attente peut être acceptée.");
    }
    ajouterTutorer(demande.getTutore());
    demande.accepter();
}

@Override
public void refuserDemande(Demande demande) {
    if (demande == null || !Objects.equals(demande.getTuteur(),this)) {
        throw new RegleGestionException("Cette demande ne vous est pas adressée.");
    }
    demande.refuser();
}
     public void choisirMatiere(Matiere matiere) {
    verifierNiveauTuteur();
    if (matiere == null) {
        throw new RegleGestionException("La matière est obligatoire.");
    }
    if (!tutores.isEmpty()) {
        throw new RegleGestionException("Impossible de changer de matière : ce tuteur a déjà des tutorés.");
    }
    this.matiere = matiere;
}

public void activerTuteur() {
    verifierNiveauTuteur();
    if (matiere == null) {
        throw new RegleGestionException("Choisissez une matière avant d'activer le profil tuteur.");
    }
    if (jour == null || heure == null) {
        throw new RegleGestionException("Proposez un créneau (jour et heure) avant d'activer le profil tuteur.");
    }
    if (retire) {
    throw new RegleGestionException("Ce tuteur a été retiré par l'administration.");
}
    estTuteurActif = true;
}

public void mettreEnPause() {
    estTuteurActif = false;
}

private void verifierNiveauTuteur() {
    if (!niveau.peutTutorer()) {
        throw new RegleGestionException("Seuls les étudiants de L3 ou plus peuvent devenir tuteur.");
    }
}
public boolean isRetire() {
    return retire;
}

public void retirer() {
    retire = true;
    estTuteurActif = false;
    tutores.clear();
}
public void restaurerDepuisBase(boolean estTuteurActif, boolean retire,
                                Matiere matiere, DayOfWeek jour, LocalTime heure) {
    this.estTuteurActif = estTuteurActif;
    this.retire = retire;
    this.matiere = matiere;
    this.jour = jour;
    this.heure = heure;
}
    public void restaurerTutores(ArrayList<Etudiant> liste) {
    this.tutores = new ArrayList<>(liste);
}
}
