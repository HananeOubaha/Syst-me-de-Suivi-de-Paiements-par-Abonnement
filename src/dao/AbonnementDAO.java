package dao;

import entity.Abonnement;
import entity.AbonnementAvecEngagement;
import entity.AbonnementSansEngagement;
import enums.StatutAbonnement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AbonnementDAO {

    // CREATE
    public void create(Abonnement abonnement) {
        String sql = "INSERT INTO abonnement (id, nom_service, montant_mensuel, date_debut, date_fin, statut, type_abonnement, duree_engagement_mois) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // The id is the first parameter. This line was missing.
            stmt.setString(1, abonnement.getId());
            stmt.setString(2, abonnement.getNomService());
            stmt.setDouble(3, abonnement.getMontantMensuel());
            stmt.setDate(4, Date.valueOf(abonnement.getDateDebut()));
            stmt.setDate(5, Date.valueOf(abonnement.getDateFin()));
            stmt.setString(6, abonnement.getStatut().name());

            if (abonnement instanceof AbonnementAvecEngagement) {
                stmt.setString(7, "AVEC_ENGAGEMENT");
                stmt.setInt(8, ((AbonnementAvecEngagement) abonnement).getDureeEngagementMois());
            } else {
                stmt.setString(7, "SANS_ENGAGEMENT");
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.executeUpdate();
            System.out.println("Abonnement ajouté avec succès !" );
        } catch (SQLException e) {
            System.err.println(" Erreur lors de l'insertion : " + e.getMessage());
        }
    }

    //  READ ALL
    public List<Abonnement> findAll() {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM abonnement";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String nomService = rs.getString("nom_service");
                double montant = rs.getDouble("montant_mensuel");
                LocalDate debut = rs.getDate("date_debut").toLocalDate();
                LocalDate fin = rs.getDate("date_fin").toLocalDate();
                StatutAbonnement statut = StatutAbonnement.valueOf(rs.getString("statut"));
                String type = rs.getString("type_abonnement");

                Abonnement abo;
                if ("AVEC_ENGAGEMENT".equals(type)) {
                    int duree = rs.getInt("duree_engagement_mois");
                    abo = new AbonnementAvecEngagement(nomService, montant, debut, fin, statut, duree);
                } else {
                    abo = new AbonnementSansEngagement(nomService, montant, debut, fin, statut);
                }
                abo.setId(id); // Set the ID after object creation
                abonnements.add(abo);
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la récupération des abonnements : " + e.getMessage());
        }

        return abonnements;
    }

    //  READ BY ID
    public Abonnement findById(String id) {
        Abonnement abo = null;
        String sql = "SELECT * FROM abonnement WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nomService = rs.getString("nom_service");
                double montant = rs.getDouble("montant_mensuel");
                LocalDate debut = rs.getDate("date_debut").toLocalDate();
                LocalDate fin = rs.getDate("date_fin").toLocalDate();
                StatutAbonnement statut = StatutAbonnement.valueOf(rs.getString("statut"));
                String type = rs.getString("type_abonnement");

                if ("AVEC_ENGAGEMENT".equals(type)) {
                    int duree = rs.getInt("duree_engagement_mois");
                    abo = new AbonnementAvecEngagement(nomService, montant, debut, fin, statut, duree);
                } else {
                    abo = new AbonnementSansEngagement(nomService, montant, debut, fin, statut);
                }
                abo.setId(id); // Set the ID after object creation
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la recherche par ID : " + e.getMessage());
        }

        return abo;
    }

    //  UPDATE
    public void update(Abonnement abonnement) {
        String sql = "UPDATE abonnement SET nom_service=?, montant_mensuel=?, date_debut=?, date_fin=?, statut=?, type_abonnement=?, duree_engagement_mois=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, abonnement.getNomService());
            stmt.setDouble(2, abonnement.getMontantMensuel());
            stmt.setDate(3, Date.valueOf(abonnement.getDateDebut()));
            stmt.setDate(4, Date.valueOf(abonnement.getDateFin()));
            stmt.setString(5, abonnement.getStatut().name());

            if (abonnement instanceof AbonnementAvecEngagement) {
                stmt.setString(6, "AVEC_ENGAGEMENT");
                stmt.setInt(7, ((AbonnementAvecEngagement) abonnement).getDureeEngagementMois());
            } else {
                stmt.setString(6, "SANS_ENGAGEMENT");
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, abonnement.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" Abonnement mis à jour avec succès !");
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    // DELETE
    public void delete(String id) {
        String sql = "DELETE FROM abonnement WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" Abonnement supprimé avec succès !");
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la suppression : " + e.getMessage());
        }
    }
}