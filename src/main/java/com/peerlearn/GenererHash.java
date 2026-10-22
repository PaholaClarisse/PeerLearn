/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;

/**
 *
 * @author Glamor Store
 */
import java.util.Scanner;
import util.MotDePasse;

public class GenererHash {
    public static void main(String[] args) {
        System.out.print("Mot de passe à hacher : ");
        String mdp = new Scanner(System.in).nextLine();
        System.out.println("Hachage à enregistrer :");
        System.out.println(MotDePasse.hacher(mdp));
    }
}