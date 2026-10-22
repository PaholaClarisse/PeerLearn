/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author Glamor Store
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import service.RegleGestionException;

public class MotDePasse {
    private static final int ITERATIONS = 120000;
    private static final int TAILLE_SEL = 16;
    private static final int TAILLE_HASH_BITS = 256;

    private static byte[] calculer(char[] mdp, byte[] sel, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(mdp, sel, iterations, TAILLE_HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RegleGestionException("Erreur technique lors du hachage du mot de passe.");
        }
    }

    public static String hacher(String mdp) {
        if (mdp == null || mdp.isEmpty()) {
            throw new RegleGestionException("Le mot de passe est obligatoire.");
        }
        byte[] sel = new byte[TAILLE_SEL];
        new SecureRandom().nextBytes(sel);
        byte[] hash = calculer(mdp.toCharArray(), sel, ITERATIONS);
        return ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(sel) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifier(String mdp, String stocke) {
        if (mdp == null || stocke == null) {
            return false;
        }
        String[] parties = stocke.split("\\$");
        if (parties.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parties[0]);
            byte[] sel = Base64.getDecoder().decode(parties[1]);
            byte[] attendu = Base64.getDecoder().decode(parties[2]);
            byte[] calcule = calculer(mdp.toCharArray(), sel, iterations);
            return MessageDigest.isEqual(attendu, calcule);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}