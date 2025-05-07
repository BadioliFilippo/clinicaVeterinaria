package db;

import java.sql.*;

public class DatabaseUtils {

    /**
     * Controlla se un codice fiscale esiste nella tabella CLIENTE
     *
     * @param conn Connessione al database
     * @param codiceFiscale  Codice fiscale da cercare
     * @return true se esiste, false altrimenti
     */
    public static boolean isCFCliente(Connection conn, String codiceFiscale) {
        String query = "SELECT 1 FROM CLIENTE WHERE codice_fiscale = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codiceFiscale);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true se almeno una riga trovata
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // oppure lancia un'eccezione se preferisci
        }
    }

    /**
    * Controlla se un codice fiscale esiste nella tabella VETERINARIO
    *
    * @param conn Connessione al database
    * @param codiceFiscale Codice fiscale da cercare
    * @return true se esiste, false altrimenti
    */
    public static boolean isCFVeterinario(Connection conn, String codiceFiscale) {
        String query = "SELECT 1 FROM VETERINARIO WHERE codice_fiscale = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codiceFiscale);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true se trova almeno una riga
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
