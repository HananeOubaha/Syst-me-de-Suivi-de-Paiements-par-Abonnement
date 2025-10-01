package service;

import dao.AbonnementDAO;
import dao.PaiementDAO;
import entity.Abonnement;
import entity.Paiement;
import enums.StatutAbonnement;
import enums.StatutPaiement;
import static utilitaire.ValidationUtils.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbonnementService {
    private final AbonnementDAO abonnementDAO;
    private final PaiementDAO paiementDAO;

    // Constructeur
    public AbonnementService() {
        this.abonnementDAO = new AbonnementDAO();
        this.paiementDAO = new PaiementDAO();
    }

    //  CRUD de base
    public void creerAbonnement(Abonnement abo) {
        abonnementDAO.create(abo);
        if (abo.getStatut() == StatutAbonnement.ACTIVE) {
            genererEcheancesFutures(abo);
        }
    }

    public List<Abonnement> listerAbonnements() {
        return abonnementDAO.findAll();
    }

    public Abonnement chercherParId(String id) {
        return abonnementDAO.findById(id);
    }

    public void mettreAJour(Abonnement abo) {
        abonnementDAO.update(abo);
    }

    public void supprimer(String id) {
        abonnementDAO.delete(id);
    }

    //  Résiliation (Modification du Statut)
    public void resilierAbonnement(String idAbonnement, LocalDate dateFin) {
        Abonnement abo = chercherParId(idAbonnement);
        if (abo != null) {
            abo.setStatut(StatutAbonnement.RESILIE);
            abo.setDateFin(dateFin);
            abonnementDAO.update(abo);
            System.out.println(" Abonnement " + abo.getNomService() + " résilié.");
        } else {
            System.err.println(" Abonnement introuvable pour la résiliation.");
        }
    }

    //  Génération d'échéances
    public void genererEcheancesFutures(Abonnement abonnement) {
        LocalDate debut = abonnement.getDateDebut();
        LocalDate fin = abonnement.getDateFin() != null ? abonnement.getDateFin() : debut.plusYears(10); // Générer 10 ans max si pas de fin

        long monthsBetween = ChronoUnit.MONTHS.between(debut, fin);

        List<Paiement> echeances = IntStream.range(0, (int) monthsBetween + 1) // Crée un Stream d'entiers (indices de mois)
                .mapToObj(i -> {
                    LocalDate dateEcheance = debut.plusMonths(i);

                    // Ne génère l'échéance que si elle est dans la période d'activité
                    if (dateEcheance.isAfter(debut) || dateEcheance.isEqual(debut)) {
                        // Les échéances futures sont 'NON_PAYE' ou 'EN_RETARD' si la date est passée
                        StatutPaiement statut = dateEcheance.isBefore(LocalDate.now()) ? StatutPaiement.NON_PAYE : StatutPaiement.NON_PAYE;

                        return new Paiement(
                                abonnement.getId(),
                                dateEcheance,
                                null, // Date de paiement est NULL pour l'instant
                                "Prélèvement",
                                statut
                        );
                    }
                    return null;
                })
                // FILTRAGE Supprime les objets nuls (ceux en dehors de la période)
                .filter(p -> p != null && paiementDAO.findByAbonnement(abonnement.getId()).stream()
                        .noneMatch(existingP -> existingP.getDateEcheance().isEqual(p.getDateEcheance()))) // Évite la duplication
                .collect(Collectors.toList());

        // Enregistrement des nouvelles échéances
        echeances.forEach(paiementDAO::create);

        System.out.println( echeances.size() + " nouvelles échéances générées pour " + abonnement.getNomService() + ".");
    }
}