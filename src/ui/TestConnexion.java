package ui;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnexion {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/Sys_Paiements";
        String user = "postgres";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connexion réussie à PostgreSQL !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
