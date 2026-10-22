/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;
import java.time.LocalTime;
import java.time.DayOfWeek;
import modele.Demande;

/**
 *
 * @author Glamor Store
 */
public interface Accompagnant {
    public void proposerCreneau(DayOfWeek jour, LocalTime heure);
    public void validerDemande(Demande demande);
    public void refuserDemande(Demande demande);
    
}
