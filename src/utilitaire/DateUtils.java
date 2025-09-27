package utilitaire;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Convertir une chaîne en LocalDate
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(" Date invalide, format attendu : yyyy-MM-dd");
        }
    }

    // Convertir une LocalDate en String formatée
    public static String formatDate(LocalDate date) {
        return date.format(FORMATTER);
    }

    // Vérifie si d1 est avant d2
    public static boolean estAvant(LocalDate d1, LocalDate d2) {
        return d1.isBefore(d2);
    }

    // Vérifie si d1 est après d2
    public static boolean estApres(LocalDate d1, LocalDate d2) {
        return d1.isAfter(d2);
    }

    // Calculer le nombre de jours entre deux dates
    public static long joursEntre(LocalDate d1, LocalDate d2) {
        return ChronoUnit.DAYS.between(d1, d2);
    }

    // Retourne la date actuelle
    public static LocalDate aujourdHui() {
        return LocalDate.now();
    }
}
