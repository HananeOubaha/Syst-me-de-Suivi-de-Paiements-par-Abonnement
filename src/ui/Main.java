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
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AbonnementService abonnementService = new AbonnementService();
    private static final PaiementService paiementService = new PaiementService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        boolean running = true;
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
                    case 12: genererRapportsFinanciers(); break;
                    case 0: running = false; System.out.println("Au revoir !"); break;
                    default: System.out.println("Choix invalide."); break;
                }
            } catch (Exception e) {
                System.err.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1  - Créer un abonnement (avec/sans engagement)");
        System.out.println("2  - Modifier un abonnement");
        System.out.println("3  - Supprimer un abonnement");
        System.out.println("4  - Consulter la liste des abonnements");
        System.out.println("5  - Afficher les paiements d'un abonnement");
        System.out.println("6  - Enregistrer un paiement");
        System.out.println("7  - Modifier un paiement");
        System.out.println("8  - Supprimer un paiement");
        System.out.println("9  - Consulter les paiements manqués (avec montant total impayé pour abonnements avec engagement)");
        System.out.println("10 - Afficher la somme payée d'un abonnement");
        System.out.println("11 - Afficher les 5 derniers paiements");
        System.out.println("12 - Générer rapports financiers (mensuel, annuel, impayés)");
        System.out.println("0  - Quitter");
    }

    // ---------- Abonnements ----------
    private static void createAbonnement() {
        System.out.println("\n-- Créer un abonnement --");
        String nom = readString("Nom du service : ");
        double montant = readDouble();
        LocalDate debut = readDate("Date début (YYYY-MM-DD) : ");
        // pour débutant on peut laisser dateFin optionnelle -> calculer automatiquement 1 an si avec engagement
        System.out.println("Type (1=AVEC_ENGAGEMENT, 2=SANS_ENGAGEMENT) : ");
        int t = readInt("> ");
        Abonnement abo;
        if (t == 1) {
            int duree = readInt("Durée engagement (mois) : ");
            LocalDate fin = debut.plusMonths(duree);
            abo = new AbonnementAvecEngagement(nom, montant, debut, fin, StatutAbonnement.ACTIVE, duree);
        } else {
            LocalDate fin = debut.plusYears(1); // par défaut 1 an
            abo = new AbonnementSansEngagement(nom, montant, debut, fin, StatutAbonnement.ACTIVE);
        }

        // Génération ID unique
        abo.setId(UUID.randomUUID().toString());

        abonnementService.ajouterAbonnement(abo);

        // Affichage des détails après création
        System.out.println("\n Abonnement ajouté avec succès !");
        System.out.println(prettyPrintAbonnement(abo));
    }

    private static void modifyAbonnement() {
        System.out.println("\n-- Modifier un abonnement --");
        String id = readString("ID de l'abonnement à modifier : ");
        Abonnement existing = abonnementService.chercherParId(id);
        if (existing == null) {
            System.out.println("Abonnement introuvable.");
            return;
        }
        String nom = readString("Nouveau nom (" + existing.getNomService() + ") : ");
        double montant = readDoubleOrDefault("Nouveau montant (" + existing.getMontantMensuel() + ") : ", existing.getMontantMensuel());
        LocalDate debut = readDateOrDefault("Nouvelle date début (" + existing.getDateDebut() + ") : ", existing.getDateDebut());
        LocalDate fin = readDateOrDefault("Nouvelle date fin (" + existing.getDateFin() + ") : ", existing.getDateFin());
        existing.setNomService(nom.isEmpty() ? existing.getNomService() : nom);
        existing.setMontantMensuel(montant);
        existing.setDateDebut(debut);
        existing.setDateFin(fin);
        abonnementService.mettreAJour(existing);
        System.out.println(" Abonnement modifié avec succès !");
    }

    private static void deleteAbonnement() {
        System.out.println("\n-- Supprimer un abonnement --");
        String id = readString("ID : ");
        abonnementService.supprimer(id);
        System.out.println(" Abonnement supprimé avec succès !");
    }

    private static void listAbonnements() {
        System.out.println("\n-- Liste des abonnements --");
        abonnementService.listerAbonnements()
                .forEach(a -> System.out.println(prettyPrintAbonnement(a)));
    }

    private static String prettyPrintAbonnement(Abonnement a) {
        return String.format("[%s] %s — %.2f€/mois — %s -> %s — %s",
                a.getId(), a.getNomService(), a.getMontantMensuel(),
                a.getDateDebut().format(DATE_FORMAT), a.getDateFin().format(DATE_FORMAT),
                a.getStatut().name());
    }

    // ---------- Paiements ----------
    private static void showPaiementsOfAbonnement() {
        System.out.println("\n-- Paiements d'un abonnement --");
        String id = readString("ID abonnement : ");
        List<Paiement> list = paiementService.listerPaiementsParAbonnement(id);
        if (list.isEmpty()) {
            System.out.println("Aucun paiement trouvé.");
        } else {
            list.forEach(p -> System.out.println(prettyPrintPaiement(p)));
        }
    }

    private static void enregistrerPaiement() {
        System.out.println("\n-- Enregistrer un paiement --");
        String idAbo = readString("ID abonnement : ");
        Abonnement abo = abonnementService.chercherParId(idAbo);
        if (abo == null) {
            System.out.println("Abonnement introuvable.");
            return;
        }
        LocalDate dateEcheance = readDate("Date échéance (YYYY-MM-DD) : ");
        String typePaiement = readString("Type paiement (ex: CB, VIREMENT) : ");
        // Pour simplifier montant = abonnement.getMontantMensuel()
        LocalDate datePaiement = null;
        System.out.println("Le paiement est-il effectué maintenant ? (o/n)");
        if (readString("> ").equalsIgnoreCase("o")) {
            datePaiement = LocalDate.now();
        }
        StatutPaiement statut = (datePaiement == null) ? StatutPaiement.NON_PAYE : StatutPaiement.PAYE;
        // si datePaiement après échéance -> EN_RETARD
        if (datePaiement != null && datePaiement.isAfter(dateEcheance)) {
            statut = StatutPaiement.EN_RETARD;
        }
        Paiement p = new Paiement(idAbo, dateEcheance, datePaiement, typePaiement, statut);
        paiementService.ajouterPaiement(p);
    }

    private static void modifierPaiement() {
        System.out.println("\n-- Modifier paiement --");
        String idPaiement = readString("ID paiement : ");
        // tentative de récupération via listAll and filter (si service n'a pas findById)
        Optional<Paiement> opt = paiementService.listerPaiements().stream()
                .filter(x -> idPaiement.equals(x.getIdPaiement()))
                .findFirst();
        if (!opt.isPresent()) {
            System.out.println("Paiement introuvable.");
            return;
        }
        Paiement p = opt.get();
        LocalDate datePaiement = readDateOrDefault("Nouvelle date paiement (" + p.getDatePaiement() + ") : ", p.getDatePaiement());
        String type = readString("Nouveau type paiement (" + p.getTypePaiement() + ") : ");
        p.setDatePaiement(datePaiement);
        p.setTypePaiement(type.isEmpty() ? p.getTypePaiement() : type);
        // maj statut
        if (p.getDatePaiement() == null) p.setStatut(StatutPaiement.NON_PAYE);
        else if (p.getDatePaiement().isAfter(p.getDateEcheance())) p.setStatut(StatutPaiement.EN_RETARD);
        else p.setStatut(StatutPaiement.PAYE);
        paiementService.modifierPaiement(p);
    }

    private static void supprimerPaiement() {
        System.out.println("\n-- Supprimer paiement --");
        String id = readString("ID paiement : ");
        paiementService.supprimerPaiement(id);
    }

    // ---------- Rapports & calculs (Streams) ----------
    private static void paiementsManquantsAvecTotal() {
        System.out.println("\n-- Paiements manquants (abonnements avec engagement) --");
        // Récupérer tous les abonnements avec engagement
        List<Abonnement> avecEngagement = abonnementService.listerAbonnements().stream()
                .filter(a -> a instanceof AbonnementAvecEngagement)
                .collect(Collectors.toList());

        double totalImpayes = 0.0;
        for (Abonnement a : avecEngagement) {
            List<Paiement> impayes = paiementService.listerPaiementsParAbonnement(a.getId()).stream()
                    .filter(p -> p.getStatut() == StatutPaiement.NON_PAYE || p.getStatut() == StatutPaiement.EN_RETARD)
                    .collect(Collectors.toList());
            if (!impayes.isEmpty()) {
                double montantParAbo = a.getMontantMensuel() * impayes.size();
                totalImpayes += montantParAbo;
                System.out.println(prettyPrintAbonnement(a) + " -> impayés: " + impayes.size() + " (≈ " + String.format("%.2f", montantParAbo) + "€)");
            }
        }
        System.out.println("Montant total impayé (pour abonnements avec engagement) : " + String.format("%.2f", totalImpayes) + "€");
    }

    private static void afficherSommePayee() {
        System.out.println("\n-- Somme payée d'un abonnement --");
        String id = readString("ID abonnement : ");
        Abonnement a = abonnementService.chercherParId(id);
        if (a == null) { System.out.println("Abonnement introuvable."); return; }

        // Somme basée sur les paiements marqués PAYE ou EN_RETARD (considérés comme payés mais en retard)
        double somme = paiementService.listerPaiementsParAbonnement(id).stream()
                .filter(p -> p.getStatut() == StatutPaiement.PAYE || p.getStatut() == StatutPaiement.EN_RETARD)
                .mapToDouble(p -> a.getMontantMensuel()) // montant du paiement = montant mensuel de l'abonnement
                .sum();

        System.out.println("Somme payée pour " + a.getNomService() + " : " + String.format("%.2f", somme) + "€");
    }

    private static void afficher5DerniersPaiements() {
        System.out.println("\n-- 5 derniers paiements (tous abonnements) --");
        List<Paiement> derniers = paiementService.listerPaiements().stream()
                .sorted(Comparator.comparing(Paiement::getDatePaiement, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());
        derniers.forEach(p -> System.out.println(prettyPrintPaiement(p)));
    }

    private static void genererRapportsFinanciers() {
        System.out.println("\n-- Générer rapports financiers --");
        System.out.println("1 - Rapport mensuel (pour un mois)");
        System.out.println("2 - Rapport annuel (pour une année)");
        System.out.println("3 - Rapport impayés");
        int c = readInt("> ");
        if (c == 1) {
            int year = readInt("Année (ex: 2025) : ");
            int month = readInt("Mois (1-12) : ");
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            rapportParPeriode(start, end);
        } else if (c == 2) {
            int year = readInt("Année (ex: 2025) : ");
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            rapportParPeriode(start, end);
        } else if (c == 3) {
            List<Paiement> impayes = paiementService.listerPaiements().stream()
                    .filter(p -> p.getStatut() == StatutPaiement.NON_PAYE || p.getStatut() == StatutPaiement.EN_RETARD)
                    .collect(Collectors.toList());
            System.out.println("Paiements impayés / en retard : " + impayes.size());
            impayes.forEach(p -> System.out.println(prettyPrintPaiement(p)));
        } else {
            System.out.println("Choix invalide.");
        }
    }

    private static void rapportParPeriode(LocalDate start, LocalDate end) {
        // On récupère tous les paiements dont datePaiement est dans la période
        List<Paiement> allPaiements = paiementService.listerPaiements();
        List<Paiement> inRange = allPaiements.stream()
                .filter(p -> p.getDatePaiement() != null && ( !p.getDatePaiement().isBefore(start) && !p.getDatePaiement().isAfter(end) ))
                .collect(Collectors.toList());

        double total = inRange.stream()
                .mapToDouble(p -> {
                    Abonnement a = abonnementService.chercherParId(p.getIdAbonnement());
                    return a != null ? a.getMontantMensuel() : 0.0;
                })
                .sum();

        System.out.println("Rapport de " + start + " à " + end + " :");
        System.out.println("Nombre paiements validés dans la période : " + inRange.size());
        System.out.println("Total perçu estimé : " + String.format("%.2f", total) + "€");
        // Regroupement par abonnement (top recettes)
        Map<String, Double> byAbo = inRange.stream()
                .collect(Collectors.groupingBy(Paiement::getIdAbonnement,
                        Collectors.summingDouble(p -> {
                            Abonnement a = abonnementService.chercherParId(p.getIdAbonnement());
                            return a != null ? a.getMontantMensuel() : 0.0;
                        })));
        System.out.println("Top abonnements (par montant) :");
        byAbo.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> {
                    Abonnement a = abonnementService.chercherParId(e.getKey());
                    String name = a != null ? a.getNomService() : e.getKey();
                    System.out.println(name + " -> " + String.format("%.2f", e.getValue()) + "€");
                });
    }

    // ---------- Helpers ----------
    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.next(); System.out.print("Entier attendu. " + prompt);
        }
        int v = scanner.nextInt();
        scanner.nextLine();
        return v;
    }

    private static double readDouble() {
        System.out.print("Montant mensuel : ");
        while (!scanner.hasNextDouble()) {
            scanner.next(); System.out.print("Nombre attendu. " + "Montant mensuel : ");
        }
        double v = scanner.nextDouble();
        scanner.nextLine();
        return v;
    }

    private static double readDoubleOrDefault(String prompt, double def) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return def;
        try { return Double.parseDouble(line); } catch (NumberFormatException e) { return def; }
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
            } catch (Exception e) {
                System.out.print("Format invalide. " + prompt);
            }
        }
    }

    private static LocalDate readDateOrDefault(String prompt, LocalDate def) {
        System.out.print(prompt);
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return def;
        try {
            return LocalDate.parse(s, DATE_FORMAT);
        } catch (Exception e) {
            System.out.println("Format invalide, valeur par défaut utilisée.");
            return def;
        }
    }

    private static String prettyPrintPaiement(Paiement p) {
        return String.format("[%s] abo=%s | échéance=%s | paiement=%s | type=%s | statut=%s",
                p.getIdPaiement(),
                p.getIdAbonnement(),
                p.getDateEcheance(),
                p.getDatePaiement(),
                p.getTypePaiement(),
                p.getStatut().name()
        );
    }
}
