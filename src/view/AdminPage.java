package view;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import db.DatabaseUtils;

public class AdminPage extends JFrame {

    private Connection conn;

    public AdminPage(Connection conn) {
        this.conn = conn;
        setTitle("Pannello Admin");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton aggiungiVeterinarioButton = new JButton("Aggiungi Veterinario");
        JButton listaVeterinari = new JButton("Lista Veterinari");
        JButton gestisciVisiteButton = new JButton("Conferma/Rifiuta Visite");
        JButton aggiungiFarmacoButton = new JButton("Aggiungi Farmaco");
        JButton clientiFrequentiButton = new JButton("Clienti più Frequenti");
        JButton specieVisitateButton = new JButton("Specie più Visitate");

        panel.add(aggiungiVeterinarioButton);
        panel.add(listaVeterinari);
        panel.add(gestisciVisiteButton);
        panel.add(aggiungiFarmacoButton);
        panel.add(clientiFrequentiButton);
        panel.add(specieVisitateButton);

        add(panel);

        aggiungiVeterinarioButton.addActionListener(e -> aggiungiVeterinario());
        listaVeterinari.addActionListener(e -> mostraListaVeterinari());
        gestisciVisiteButton.addActionListener(e -> gestisciVisite());
        aggiungiFarmacoButton.addActionListener(e -> aggiungiFarmaco());
        clientiFrequentiButton.addActionListener(e -> mostraClientiFrequenti());
        specieVisitateButton.addActionListener(e -> mostraSpecieVisitate());

        setVisible(true);
    }

    private void aggiungiVeterinario() {
        JTextField cfField = new JTextField();
        JTextField nomeField = new JTextField();
        JTextField cognomeField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] fields = {
            "Codice Fiscale:", cfField,
            "Nome:", nomeField,
            "Cognome:", cognomeField,
            "Email:", emailField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Nuovo Veterinario", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO veterinario (codice_fiscale, nome, cognome, mail) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, cfField.getText().trim());
                stmt.setString(2, nomeField.getText().trim());
                stmt.setString(3, cognomeField.getText().trim());
                stmt.setString(4, emailField.getText().trim());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Veterinario aggiunto con successo.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        }
    }

    private void mostraListaVeterinari() {
            String query = "SELECT codice_fiscale, nome, cognome, mail FROM veterinario";

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        // Colonne della tabella
        String[] columnNames = {"Codice Fiscale", "Nome", "Cognome", "Email"};

        // Dati dinamici
        java.util.List<String[]> data = new java.util.ArrayList<>();

        while (rs.next()) {
            data.add(new String[] {
                rs.getString("codice_fiscale"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("mail")
            });
        }

        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun veterinario presente.");
            return;
        }

        // Conversione lista in array per JTable
        String[][] rowData = data.toArray(new String[0][]);

        JTable table = new JTable(rowData, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        // Imposta dimensione e visualizza in un dialog
        JDialog dialog = new JDialog(this, "Elenco Veterinari", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.add(scrollPane);
        dialog.setSize(600, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Errore SQL: " + ex.getMessage());
    }
    }

    private void gestisciVisite() {
        String sqlSelect = "SELECT id_prenotazione, id_cliente, data, paziente FROM prenotazione WHERE confermata IS NULL";

        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlSelect)) {

            while (rs.next()) {
                int idPrenotazione = rs.getInt("id_prenotazione");
                String cfCliente = rs.getString("id_cliente");
                String data = rs.getString("data");
                int idAnimale = rs.getInt("paziente");

                JTextField cfVetField = new JTextField();

                Object[] message = {
                    "Visita #" + idPrenotazione + " - Cliente: " + cfCliente + " - Data: " + data,
                    "Inserisci il CF del veterinario:", cfVetField
                };

                int scelta = JOptionPane.showConfirmDialog(this, message, "Gestione Visita", JOptionPane.YES_NO_CANCEL_OPTION);

                if (scelta == JOptionPane.YES_OPTION) {
                    String codiceFiscaleVeterinario = cfVetField.getText().trim();
                    if (codiceFiscaleVeterinario.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "CF del veterinario non può essere vuoto.");
                        continue; // passa alla prossima visita
                    }

                    // Conferma prenotazione
                    try (PreparedStatement psConferma = conn.prepareStatement("UPDATE prenotazione SET confermata = TRUE WHERE id_prenotazione = ?")) {
                        psConferma.setInt(1, idPrenotazione);
                        psConferma.executeUpdate();
                    }

                    // Inserisci nuova visita
                    String insertVisita = "INSERT INTO visita (id_visita, id_prenotazione, id_animale, id_veterinario, data) VALUES (?, ?, ?, ?, ?)";
                    int id_visita = DatabaseUtils.getNextAvailableID(conn, "visita");
                    try (PreparedStatement psInsert = conn.prepareStatement(insertVisita)) {
                        psInsert.setInt(1, id_visita);
                        psInsert.setInt(2, idPrenotazione);
                        psInsert.setInt(3, idAnimale);
                        psInsert.setString(4, codiceFiscaleVeterinario);
                        psInsert.setString(5, data);
                        psInsert.executeUpdate();
                    }

                    // Aggiornamento visite clienti
                    String updateCliente = "UPDATE cliente SET n_visite = n_visite + 1 WHERE codice_fiscale = (SELECT id_cliente FROM animale WHERE id_animale = ?)";
                    try (PreparedStatement psUpdateCliente = conn.prepareStatement(updateCliente)) {
                        psUpdateCliente.setInt(1, idAnimale);
                    }

                    // Aggiornamento visite specie
                    String updateSpecie = "UPDATE specie SET n_visite = n_visite + 1 WHERE id_specie = (SELECT id_specie FROM animale WHERE id_animale = ?)";
                    try (PreparedStatement psUpdateSpecie = conn.prepareStatement(updateSpecie)) {
                        psUpdateSpecie.setInt(1, idAnimale);
                    }


                    JOptionPane.showMessageDialog(this, "Visita confermata e registrata.");

                } else if (scelta == JOptionPane.NO_OPTION) {
                    try (PreparedStatement psRifiuto = conn.prepareStatement("UPDATE prenotazione SET confermata = FALSE WHERE id_prenotazione = ?")) {
                        psRifiuto.setInt(1, idPrenotazione);
                        psRifiuto.executeUpdate();
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Nessuna visita da confermare!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore SQL: " + ex.getMessage());
            }
    }



    private void aggiungiFarmaco() {
        JTextField nomeField = new JTextField();
        JTextField dosaggioField = new JTextField();
        JTextField somministrazioneField = new JTextField();
        Object[] fields = {
            "Nome Farmaco:", nomeField,
            "Dosaggio:", dosaggioField,
            "Somministrazione:", somministrazioneField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Nuovo Farmaco", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO farmaco (id_farmaco, nome, dosaggio, somministrazione) VALUES (?, ?, ?, ?)")) {
                int id_farmaco = DatabaseUtils.getNextAvailableID(conn, "farmaco");
                stmt.setInt(1, id_farmaco);
                stmt.setString(2, nomeField.getText().trim());
                stmt.setString(3, dosaggioField.getText().trim());
                stmt.setString(4, somministrazioneField.getText().trim());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Farmaco aggiunto con successo.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        }
    }

    private void mostraClientiFrequenti() {
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nome, cognome, n_visite FROM cliente ORDER BY n_visite DESC LIMIT 5")) {

            StringBuilder sb = new StringBuilder("Clienti più frequenti:\n");
            while (rs.next()) {
                sb.append(rs.getString("nome"))
                  .append(" ")
                  .append(rs.getString("cognome"))
                  .append(" - Visite: ")
                  .append(rs.getInt("n_visite"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }

    private void mostraSpecieVisitate() {
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nome, n_visite FROM specie ORDER BY n_visite DESC LIMIT 5")) {

            StringBuilder sb = new StringBuilder("Specie più visitate:\n");
            while (rs.next()) {
                sb.append(rs.getString("nome"))
                  .append(" - Visite: ")
                  .append(rs.getInt("n_visite"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
        }
    }
}
