package service;

import dao.PaiementDAO;
import entity.Paiement;

import java.util.List;

public class PaiementService {

    private PaiementDAO paiementDAO;

    public PaiementService() {
        this.paiementDAO = new PaiementDAO();
    }

    // Ajouter un paiement
    public void ajouterPaiement(Paiement paiement) {
        paiementDAO.create(paiement);
    }

    // Récupérer tous les paiements
    public List<Paiement> listerPaiements() {
        return paiementDAO.findAll();
    }

    // Récupérer les paiements d’un abonnement spécifique
    public List<Paiement> listerPaiementsParAbonnement(String abonnementId) {
        return paiementDAO.findByAbonnement(abonnementId);
    }
    public void modifierPaiement(Paiement paiement) {
        paiementDAO.update(paiement); // suppose que tu crées update() dans DAO
    }

    public void supprimerPaiement(String idPaiement) {
        paiementDAO.delete(idPaiement); // suppose que tu crées delete() dans DAO
    }

}
