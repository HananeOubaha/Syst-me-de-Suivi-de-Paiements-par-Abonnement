package entity;

import java.time.LocalDate;

public class AbonnementAvecEngagement extends Abonnement {
    private int dureeEngagementMois;

    // CONstructeur
    public AbonnementAvecEngagement(String nomService, double montantMensuel, LocalDate dateDebut, LocalDate dateFin, StatutAbonnement statut, int dureeEngagementMois) {
        super(nomService, montantMensuel, dateDebut, dateFin, statut);
        this.dureeEngagementMois = dureeEngagementMois;
    }

    // Getters / setters
    public int getDureeEngagementMois() {
        return dureeEngagementMois;
    }
    public void setDureeEngagementMois(int dureeEngagementMois) {
        this.dureeEngagementMois = dureeEngagementMois;
    }
}
