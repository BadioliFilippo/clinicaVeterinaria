package db;

import java.sql.*;

import javax.swing.JOptionPane;

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

    public static int getNextAvailableID(Connection conn, String tableName) {
    int id = 0;
    String idQuery = String.format("""
        SELECT COALESCE(
            (SELECT MIN(t1.id_%s + 1)
            FROM %s t1
            WHERE NOT EXISTS (
                SELECT 1 FROM %s t2 WHERE t2.id_%s = t1.id_%s + 1
            )),
            1
        ) AS nuovo_id
    """, tableName, tableName, tableName, tableName, tableName);

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(idQuery)) {
        if (rs.next()) {
            id = rs.getInt("nuovo_id");
        }
    } catch (SQLException e) {
        e.printStackTrace(); // stampa su console per debug
        JOptionPane.showMessageDialog(null, "Errore nel recupero dell'ID: " + e.getMessage());
    }

    return id;
}


}
