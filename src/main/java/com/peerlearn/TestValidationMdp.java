/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peerlearn;
import util.ValidationMdp;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author Glamor Store
 */
public class TestValidationMdp {
    public static void tester(String mdp){
        List<String> erreurs = ValidationMdp.valider(mdp);
        System.out.println("Test de :" +mdp);
        System.out.println("nombres d'erreurs : " +erreurs.size());
        for(String e : erreurs){
            System.out.println("-" +e);
        }
                    System.out.println();
    }
    public static void main(String[] args){ 
       tester("");
       tester("abc");
       tester("agfhjsdlh");
       tester("AFGHKLDU");
       tester("AGB");
       tester("fsgjkjkdf");
       tester("magne33Pa@");
       tester("magne33PSHH");
       tester(null);
    }
}
