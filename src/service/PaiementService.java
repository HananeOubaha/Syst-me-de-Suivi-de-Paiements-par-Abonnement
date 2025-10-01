package service;

import dao.AbonnementDAO;
import dao.PaiementDAO;
import entity.Abonnement;
import entity.AbonnementAvecEngagement;
import entity.Paiement;
import enums.StatutPaiement;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.objects.NativeArray.forEach;

public class PaiementService {
    private final PaiementDAO paiementDAO;
    private final AbonnementDAO abonnementDAO;

    // Constructeur
    public PaiementService() {
        this.paiementDAO = new PaiementDAO();
        this.abonnementDAO = new AbonnementDAO();
    }

    // --- CRUD de base ---
    public void enregistrerPaiement(Paiement paiement) {
        // Logique métier : Un paiement enregistré est PAYE
        paiement.setDatePaiement(LocalDate.now());
        paiement.setStatut(StatutPaiement.PAYE);
        paiementDAO.update(paiement); // Supposons qu'on met à jour un paiement existant (échéance)
        System.out.println(" Paiement de l'échéance du " + paiement.getDateEcheance() + " enregistré.");
    }

    public void mettreAJourPaiement(Paiement paiement) {
        paiementDAO.update(paiement);
    }

    public void supprimerPaiement(String idPaiement) {
        paiementDAO.delete(idPaiement);
    }

    public List<Paiement> listerPaiements() {
        return paiementDAO.findAll();
    }

    public List<Paiement> listerPaiementsParAbonnement(String idAbonnement) {
        return paiementDAO.findByAbonnement(idAbonnement);
    }

    public List<Paiement> listerCinqDerniersPaiements() {
        return paiementDAO.findLastPayments(5);
    }

    // Détection des impayés (avec montant total)

    public void paiementsNonpayerAvecTotal(Paiement paiement ,String idAbonnement){


        List<Paiement> paiments =paiementDAO.findByAbonnement(idAbonnement);

        paiments.stream()
            .filter(p -> p.getStatut().equals(StatutPaiement.NON_PAYE))
                .map(p -> p.)
            .forEach(System.out:: println);




    }
    }