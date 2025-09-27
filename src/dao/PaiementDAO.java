package dao;

import entity.Paiement;
import enums.StatutPaiement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO {

    // 🔹 CREATE
    public void create(Paiement paiement) {
        String sql = "INSERT INTO paiement (id_Paiement, id_Abonnement, date_echeance, date_paiement, type_paiement, statut) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paiement.getIdPaiement());
            stmt.setString(2, paiement.getIdAbonnement());
            stmt.setDate(3, Date.valueOf(paiement.getDateEcheance()));
            stmt.setDate(4, paiement.getDatePaiement() != null ? Date.valueOf(paiement.getDatePaiement()) : null);
            stmt.setString(5, paiement.getTypePaiement());
            stmt.setString(6, paiement.getStatut().name());

            stmt.executeUpdate();
            System.out.println(" Paiement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion : " + e.getMessage());
        }
    }
    // UPDATE
    public void update(Paiement paiement) {
        String sql = "UPDATE paiement SET date_echeance=?, date_paiement=?, type_paiement=?, statut=? WHERE id_Paiement=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(paiement.getDateEcheance()));
            stmt.setDate(2, paiement.getDatePaiement() != null ? Date.valueOf(paiement.getDatePaiement()) : null);
            stmt.setString(3, paiement.getTypePaiement());
            stmt.setString(4, paiement.getStatut().name());
            stmt.setString(5, paiement.getIdPaiement());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // DELETE
    public void delete(String idPaiement) {
        String sql = "DELETE FROM paiement WHERE id_Paiement=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idPaiement);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // READ ALL
    public List<Paiement> findAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Paiement p = new Paiement(
                        rs.getString("id_Abonnement"),
                        rs.getDate("date_echeance").toLocalDate(),
                        rs.getDate("date_paiement") != null ? rs.getDate("date_paiement").toLocalDate() : null,
                        rs.getString("type_paiement"),
                        StatutPaiement.valueOf(rs.getString("statut"))
                );
                p.setIdPaiement(rs.getString("id_Paiement"));
                paiements.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération : " + e.getMessage());
        }

        return paiements;
    }

    //  READ BY ABONNEMENT
    public List<Paiement> findByAbonnement(String idAbonnement) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE id_Abonnement = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idAbonnement);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Paiement p = new Paiement(
                        rs.getString("id_Abonnement"),
                        rs.getDate("date_echeance").toLocalDate(),
                        rs.getDate("date_paiement") != null ? rs.getDate("date_paiement").toLocalDate() : null,
                        rs.getString("type_paiement"),
                        StatutPaiement.valueOf(rs.getString("statut"))
                );
                p.setIdPaiement(rs.getString("id_Paiement"));
                paiements.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par abonnement : " + e.getMessage());
        }

        return paiements;
    }
}
