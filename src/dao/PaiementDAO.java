package dao;

import entity.Paiement;
import enums.StatutPaiement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaiementDAO {

    // --- CREATE ---
    public void create(Paiement paiement) {
        String sql = "INSERT INTO paiement (id_paiement, id_abonnement, date_echeance, date_paiement, type_paiement, statut) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paiement.getIdPaiement());
            stmt.setString(2, paiement.getIdAbonnement());
            stmt.setDate(3, Date.valueOf(paiement.getDateEcheance()));
            // Gère le cas où datePaiement est null (paiement non effectué)
            stmt.setDate(4, Optional.ofNullable(paiement.getDatePaiement()).map(Date::valueOf).orElse(null));
            stmt.setString(5, paiement.getTypePaiement());
            stmt.setString(6, paiement.getStatut().name());

            stmt.executeUpdate();
            System.out.println(" Paiement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println(" Erreur lors de l'insertion : " + e.getMessage());
        }
    }

    // --- UPDATE ---
    public void update(Paiement paiement) {
        String sql = "UPDATE paiement SET id_abonnement=?, date_echeance=?, date_paiement=?, type_paiement=?, statut=? WHERE id_paiement=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, paiement.getIdAbonnement());
            stmt.setDate(2, Date.valueOf(paiement.getDateEcheance()));
            stmt.setDate(3, Optional.ofNullable(paiement.getDatePaiement()).map(Date::valueOf).orElse(null));
            stmt.setString(4, paiement.getTypePaiement());
            stmt.setString(5, paiement.getStatut().name());
            stmt.setString(6, paiement.getIdPaiement());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" Paiement mis à jour avec succès !");
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    // --- DELETE ---
    public void delete(String idPaiement) {
        String sql = "DELETE FROM paiement WHERE id_paiement=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idPaiement);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" Paiement supprimé avec succès !");
            }
        } catch (SQLException e) {
            System.err.println(" Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // --- READ ALL ---
    public List<Paiement> findAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la récupération : " + e.getMessage());
        }

        return paiements;
    }

    // --- READ BY ABONNEMENT ---
    public List<Paiement> findByAbonnement(String idAbonnement) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE id_abonnement = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idAbonnement);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la recherche par abonnement : " + e.getMessage());
        }

        return paiements;
    }

    //   Trouver les impayés pour un abonnement
    public List<Paiement> findUnpaidByAbonnement(String idAbonnement) {
        List<Paiement> paiements = new ArrayList<>();
        // Les statuts doivent correspondre à ceux de votre ENUM
        String sql = "SELECT * FROM paiement WHERE id_abonnement = ? AND statut IN ('NON_PAYE', 'EN_RETARD')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idAbonnement);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la recherche des impayés : " + e.getMessage());
        }

        return paiements;
    }

    //  REQUÊTE MÉTIER (BDD) : Trouver les N derniers paiements
    public List<Paiement> findLastPayments(int limit) {
        List<Paiement> paiements = new ArrayList<>();
        // Utilise ORDER BY et LIMIT (Standard SQL supporté par PostgreSQL et MySQL)
        String sql = "SELECT * FROM paiement ORDER BY date_echeance DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Erreur lors de la recherche des derniers paiements : " + e.getMessage());
        }

        // Pas besoin de stream ici, car le tri est fait efficacement par la BDD.
        return paiements;
    }

    // --- UTILITY METHOD: Mapper un ResultSet à un objet Paiement ---
    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Date sqlDatePaiement = rs.getDate("date_paiement");
        LocalDate datePaiement = (sqlDatePaiement != null) ? sqlDatePaiement.toLocalDate() : null;

        return new Paiement(
                rs.getString("id_paiement"),
                rs.getString("id_abonnement"),
                rs.getDate("date_echeance").toLocalDate(),
                datePaiement,
                rs.getString("type_paiement"),
                StatutPaiement.valueOf(rs.getString("statut"))
        );
    }
}