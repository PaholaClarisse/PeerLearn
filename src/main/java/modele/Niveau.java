/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package modele;

/**
 *
 * @author Glamor Store
 */
public enum Niveau {
    L1,L2,L3,M1,M2;
    public boolean peutTutorer(){
       return this != L1 && this != L2;
    }
    
}
