package service;

import dao.AbonnementDAO;
import entity.Abonnement;
import java.util.List;

public class AbonnementService {
    private AbonnementDAO abonnementDAO = new AbonnementDAO();

    public void ajouterAbonnement(Abonnement abo) {
        abonnementDAO.create(abo);
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
}
