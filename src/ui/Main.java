package ui;

import entity.Abonnement;
import entity.AbonnementAvecEngagement;
import entity.AbonnementSansEngagement;
import entity.Paiement;
import enums.StatutAbonnement;
import enums.StatutPaiement;
import service.AbonnementService;
import service.PaiementService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    // Utilisation des services validés précédemment
    private static final AbonnementService abonnementService = new AbonnementService();
    private static final PaiementService paiementService = new PaiementService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        boolean running = true;
        // Chargement initial pour créer les tables si nécessaire (bonne pratique en début de main)
        try {
            // Tentative de connexion pour vérifier la DB
            dao.DatabaseConnection.getConnection();
            System.out.println("Base de données connectée avec succès.");
        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE DE CONNEXION DB: " + e.getMessage());
            System.err.println("Veuillez vérifier votre fichier db.properties et que le serveur PostgreSQL/MySQL est démarré.");
            return;
        }

        while (running) {
            printMenu();
            int choice = readInt("Choix > ");
            try {
                switch (choice) {
                    case 1: createAbonnement(); break;
                    case 2: modifyAbonnement(); break;
                    case 3: deleteAbonnement(); break;
                    case 4: listAbonnements(); break;
                    case 5: showPaiementsOfAbonnement(); break;
                    case 6: enregistrerPaiement(); break;
                    case 7: modifierPaiement(); break;
                    case 8: supprimerPaiement(); break;
                    case 9: paiementsManquantsAvecTotal(); break;
                    case 10: afficherSommePayee(); break;
                    case 11: afficher5DerniersPaiements(); break;
                    case 0: running = false; System.out.println("Au revoir !"); break;
                    default: System.out.println("Choix invalide."); break;
                }
            } catch (Exception e) {
                // Gestion générale des exceptions, y compris celles venant des services/DAO
                System.err.println("\n Une erreur inattendue est survenue : " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1  - Créer un abonnement (avec/sans engagement)");
        System.out.println("2  - Modifier un abonnement");
        System.out.println("3  - Supprimer/Résiliation d'un abonnement"); // Ajout de la notion de Résiliation
        System.out.println("4  - Consulter la liste des abonnements");
        System.out.println("----------------------------------------");
        System.out.println("5  - Afficher les paiements d'un abonnement");
        System.out.println("6  - Enregistrer un paiement (marquer comme payé)");
        System.out.println("7  - Modifier un paiement (date, type)");
        System.out.println("8  - Supprimer un paiement");
        System.out.println("----------------------------------------");
        System.out.println("9  - Montant total impayé (abos avec engagement)");
        System.out.println("10 - Afficher la somme payée d'un abonnement");
        System.out.println("11 - Afficher les 5 derniers paiements");
        System.out.println("0  - Quitter");
    }

    // ---------- Abonnements ----------
    private static void createAbonnement() {
        System.out.println("\n-- Créer un abonnement --");
        String nom = readString("Nom du service : ");
        double montant = readDouble("Montant mensuel : ");
        LocalDate debut = readDate("Date début (YYYY-MM-DD) : ");

        System.out.println("Type (1=AVEC_ENGAGEMENT, 2=SANS_ENGAGEMENT) : ");
        int t = readInt("> ");

        Abonnement abo;
        if (t == 1) {
            int duree = readInt("Durée engagement (mois) : ");
            LocalDate fin = debut.plusMonths(duree);
            abo = new AbonnementAvecEngagement(nom, montant, debut, fin, StatutAbonnement.ACTIVE, duree);
        } else if (t == 2) {
            // Pour l'abonnement sans engagement, la date de fin initiale n'est pas cruciale
            LocalDate fin = debut.plusYears(100);
            abo = new AbonnementSansEngagement(nom, montant, debut, fin, StatutAbonnement.ACTIVE);
        } else {
            System.err.println("Choix de type invalide. Annulation.");
            return;
        }

        // L'ID est généré dans le constructeur de l'entité, pas besoin de le faire ici.
        // abo.setId(UUID.randomUUID().toString()); // LIGNE SUPPRIMÉE

        // Correction de l'appel de méthode
        abonnementService.creerAbonnement(abo);

        System.out.println("\n Abonnement ajouté avec succès !");
        System.out.println(prettyPrintAbonnement(abo));
    }

    private static void modifyAbonnement() {
        System.out.println("\n-- Modifier un abonnement --");
        String id = readString("ID de l'abonnement à modifier : ");
        Abonnement existing = abonnementService.chercherParId(id);
        if (existing == null) {
            System.out.println(" Abonnement introuvable.");
            return;
        }

        // Simplification des entrées : seul le montant et le nom devraient pouvoir être modifiés facilement
        System.out.println("Abonnement actuel : " + prettyPrintAbonnement(existing));
        String nom = readString("Nouveau nom (Laissez vide pour garder '" + existing.getNomService() + "') : ");
        double montant = readDoubleOrDefault("Nouveau montant (" + existing.getMontantMensuel() + ") : ", existing.getMontantMensuel());

        existing.setNomService(nom.isEmpty() ? existing.getNomService() : nom);
        existing.setMontantMensuel(montant);

        // Option de Résiliation
        System.out.println("Voulez-vous résilier l'abonnement ? (o/n)");
        if (readString("> ").equalsIgnoreCase("o")) {
            LocalDate dateFin = readDate("Date de fin effective de la résiliation (YYYY-MM-DD) : ");
            abonnementService.resilierAbonnement(id, dateFin);
        } else {
            abonnementService.mettreAJour(existing);
            System.out.println(" Abonnement mis à jour avec succès !");
        }
    }

    private static void deleteAbonnement() {
        System.out.println("\n-- Supprimer un abonnement --");
        String id = readString("ID : ");
        Abonnement existing = abonnementService.chercherParId(id);
        if (existing == null) {
            System.out.println(" Abonnement introuvable.");
            return;
        }

        System.out.println("Êtes-vous sûr de vouloir SUPPRIMER (o/n) ? Cela supprimera les paiements associés. ");
        if (readString("> ").equalsIgnoreCase("o")) {
            abonnementService.supprimer(id);
            System.out.println("Abonnement et ses paiements associés supprimés.");
        } else {
            System.out.println("Annulation de la suppression.");
        }
    }

    private static void listAbonnements() {
        System.out.println("\n-- Liste des abonnements --");
        List<Abonnement> abonnements = abonnementService.listerAbonnements();
        if (abonnements.isEmpty()) {
            System.out.println("Aucun abonnement enregistré.");
        } else {
            // Utilisation du Stream API pour l'affichage (lambda)
            abonnements.forEach(a -> System.out.println(prettyPrintAbonnement(a)));
        }
    }

    private static String prettyPrintAbonnement(Abonnement a) {
        String duree = (a instanceof AbonnementAvecEngagement) ?
                ((AbonnementAvecEngagement)a).getDureeEngagementMois() + " mois" : "Sans engagement";
        String dateFinStr = (a.getDateFin() != null) ? a.getDateFin().format(DATE_FORMAT) : "N/A";

        return String.format("[%s] %s — %.2f€/mois — Début: %s | Fin: %s | Statut: %s | Type: %s",
                a.getId(), a.getNomService(), a.getMontantMensuel(),
                a.getDateDebut().format(DATE_FORMAT), dateFinStr,
                a.getStatut().name(), duree);
    }

    // ---------- Paiements ----------
    private static void showPaiementsOfAbonnement() {
        System.out.println("\n-- Paiements d'un abonnement --");
        String id = readString("ID abonnement : ");
        List<Paiement> list = paiementService.listerPaiementsParAbonnement(id);
        if (list.isEmpty()) {
            System.out.println("Aucun paiement trouvé pour cet abonnement.");
        } else {
            // Utilisation du Stream API pour l'affichage (lambda)
            list.forEach(p -> System.out.println(prettyPrintPaiement(p)));
        }
    }

    private static void enregistrerPaiement() {
        System.out.println("\n-- Enregistrer un paiement (Marquer une échéance comme payée) --");
        String idAbo = readString("ID abonnement : ");
        List<Paiement> impayes = paiementService.listerPaiementsParAbonnement(idAbo).stream()
                .filter(p -> p.getStatut() == StatutPaiement.NON_PAYE || p.getStatut() == StatutPaiement.EN_RETARD)
                .sorted(Comparator.comparing(Paiement::getDateEcheance)) // Afficher le plus ancien en premier
                .collect(Collectors.toList());

        if (impayes.isEmpty()) {
            System.out.println("Aucune échéance non payée ou en retard trouvée pour cet abonnement.");
            return;
        }

        System.out.println("\nÉchéances à payer (entrez l'ID du Paiement) :");
        impayes.forEach(p -> System.out.println(prettyPrintPaiement(p)));

        String idPaiement = readString("ID du Paiement à enregistrer : ");
        Optional<Paiement> optPaiement = impayes.stream()
                .filter(p -> p.getIdPaiement().equals(idPaiement))
                .findFirst();

        if (optPaiement.isPresent()) {
            Paiement p = optPaiement.get();
            // L'enregistrement est géré dans le Service (date de paiement = now, statut = PAYE)
            paiementService.enregistrerPaiement(p);
        } else {
            System.err.println("ID Paiement invalide ou n'est pas dans la liste des impayés.");
        }
    }

    private static void modifierPaiement() {
        System.out.println("\n-- Modifier paiement --");
        String idPaiement = readString("ID paiement : ");

        // CORRECTION : Le service n'a pas de findById, on utilise listAll puis filter (méthode non optimale mais fonctionnelle)
        Optional<Paiement> opt = paiementService.listerPaiements().stream()
                .filter(x -> idPaiement.equals(x.getIdPaiement()))
                .findFirst();

        if (!opt.isPresent()) {
            System.out.println(" Paiement introuvable.");
            return;
        }

        Paiement p = opt.get();
        System.out.println("Paiement actuel : " + prettyPrintPaiement(p));

        LocalDate newDatePaiement = readDateOrDefault("Nouvelle date paiement (Laissez vide pour garder / N/A pour NON_PAYE) (" + (p.getDatePaiement() != null ? p.getDatePaiement().format(DATE_FORMAT) : "N/A") + ") : ", p.getDatePaiement());
        String type = readString("Nouveau type paiement (Laissez vide pour garder '" + p.getTypePaiement() + "') : ");

        p.setDatePaiement(newDatePaiement);
        p.setTypePaiement(type.isEmpty() ? p.getTypePaiement() : type);

        // Logique de mise à jour du statut
        if (p.getDatePaiement() == null) {
            p.setStatut(StatutPaiement.NON_PAYE);
        } else if (p.getDatePaiement().isAfter(p.getDateEcheance())) {
            p.setStatut(StatutPaiement.EN_RETARD);
        } else {
            p.setStatut(StatutPaiement.PAYE);
        }

        // Correction de l'appel de méthode
        paiementService.mettreAJourPaiement(p);
        System.out.println(" Paiement mis à jour avec succès !");
    }

    private static void supprimerPaiement() {
        System.out.println("\n-- Supprimer paiement --");
        String id = readString("ID paiement : ");
        paiementService.supprimerPaiement(id);
        System.out.println(" Paiement supprimé.");
    }

    // ---------- calculs ----------
    private static void paiementsManquantsAvecTotal() {
        System.out.println("\n-- Montant total impayé (abonnements avec engagement) --");
        String id = readString("ID abonnement avec engagement : ");
        Abonnement a = abonnementService.chercherParId(id);

        if (a == null || !(a instanceof AbonnementAvecEngagement)) {
            System.out.println(" Abonnement introuvable ou n'est pas avec engagement.");
            return;
        }

        // Utilisation du Service (meilleure pratique)
        double totalImpayes = paiementService.calculerMontantImpaye(id);

        System.out.println("\nAnalyse pour : " + a.getNomService());
        System.out.println("Nombre d'échéances impayées (DAO/Service) : " + (int)(totalImpayes / a.getMontantMensuel()));
        System.out.println(" Montant total impayé : " + String.format("%.2f", totalImpayes) + "€");
    }

    private static void afficherSommePayee() {
        System.out.println("\n-- Somme payée d'un abonnement --");
        String id = readString("ID abonnement : ");
        Abonnement a = abonnementService.chercherParId(id);
        if (a == null) { System.out.println(" Abonnement introuvable."); return; }

        // Somme basée sur les paiements marqués PAYE ou EN_RETARD (considérés comme payés mais en retard)
        double somme = paiementService.listerPaiementsParAbonnement(id).stream()
                .filter(p -> p.getStatut() == StatutPaiement.PAYE || p.getStatut() == StatutPaiement.EN_RETARD)
                .mapToDouble(p -> a.getMontantMensuel()) // Utilise le montant mensuel de l'abonnement
                .sum(); // P.F.: Collectors.summingDouble est plus puissant mais sum() suffit ici

        System.out.println("Somme payée pour " + a.getNomService() + " : " + String.format("%.2f", somme) + "€");
    }

    private static void afficher5DerniersPaiements() {
        System.out.println("\n-- 5 derniers paiements (tous abonnements) --");
        // Utilisation du DAO/Service (findLastPayments) est plus efficace que de lister tout et trier ici
        List<Paiement> derniers = paiementService.listerCinqDerniersPaiements();

        if (derniers.isEmpty()) {
            System.out.println("Aucun paiement trouvé.");
        } else {
            System.out.println("Les 5 plus récents :");
            // Utilisation du Stream API pour l'affichage (lambda)
            derniers.forEach(p -> System.out.println(prettyPrintPaiement(p)));
        }
    }

    // ---------- Helpers (Lectures clavier) ----------
    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.next(); System.out.print("Entier attendu. " + prompt);
        }
        int v = scanner.nextInt();
        scanner.nextLine(); // consommer le retour chariot
        return v;
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            scanner.next(); System.out.print("Nombre attendu. " + prompt);
        }
        double v = scanner.nextDouble();
        scanner.nextLine();
        return v;
    }

    private static double readDoubleOrDefault(String prompt, double def) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return def;
        try { return Double.parseDouble(line); } catch (NumberFormatException e) {
            System.out.println("Format invalide, valeur par défaut utilisée (" + def + ").");
            return def;
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static LocalDate readDate(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String s = scanner.nextLine().trim();
                return LocalDate.parse(s, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.print("Format invalide (Ex: 2025-09-30). " + prompt);
            }
        }
    }

    private static LocalDate readDateOrDefault(String prompt, LocalDate def) {
        System.out.print(prompt);
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return def;

        // Si l'utilisateur tape "N/A" ou "null" pour une date (ex: date paiement), on retourne null
        if (s.equalsIgnoreCase("N/A") || s.equalsIgnoreCase("null")) return null;

        try {
            return LocalDate.parse(s, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            System.out.println("Format invalide, valeur par défaut utilisée (" + (def != null ? def.format(DATE_FORMAT) : "N/A") + ").");
            return def;
        }
    }

    private static String prettyPrintPaiement(Paiement p) {
        String datePaiementStr = (p.getDatePaiement() != null) ? p.getDatePaiement().format(DATE_FORMAT) : "N/A";

        return String.format("[%s] Abo=%s | Échéance=%s | Paiement=%s | Type=%s | Statut=%s",
                p.getIdPaiement(),
                p.getIdAbonnement(),
                p.getDateEcheance().format(DATE_FORMAT),
                datePaiementStr,
                p.getTypePaiement(),
                p.getStatut().name()
        );
    }
}