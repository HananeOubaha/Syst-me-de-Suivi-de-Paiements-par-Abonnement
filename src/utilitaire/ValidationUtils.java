package utilitaire;

import exceptions.ValidationException;

public class ValidationUtils {

    // Vérifier qu'une chaîne n'est pas vide
    public static void validerNomService(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException(" Le nom du service ne peut pas être vide !");
        }
    }

    // Vérifier que le montant est positif
    public static void validerMontant(double montant) {
        if (montant <= 0) {
            throw new ValidationException(" Le montant doit être supérieur à 0 !");
        }
    }

    // Vérifier la durée d'engagement
    public static void validerDureeEngagement(int duree, boolean avecEngagement) {
        if (avecEngagement && duree <= 0) {
            throw new ValidationException(" La durée d'engagement doit être supérieure à 0 !");
        }
    }
}
