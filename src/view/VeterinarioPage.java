package view;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class VeterinarioPage extends JFrame {

    private final Connection conn;
    private final String cfVeterinario;

    public VeterinarioPage(Connection conn, String cfVeterinario) {
        this.conn = conn;
        this.cfVeterinario = cfVeterinario;

        setTitle("Pannello Veterinario");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Diagnosi & Farmaci", creaPannelloDiagnosi());
        tabs.addTab("Vaccinazioni", creaPannelloVaccinazione());
        tabs.addTab("Storico Animale", creaPannelloStorico());

        add(tabs);
        setVisible(true);
    }

    private JPanel creaPannelloDiagnosi() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField idAnimaleField = new JTextField();
        JTextField descrizioneField = new JTextField();
        JTextField farmaciField = new JTextField();

        JButton inviaButton = new JButton("Registra");

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("ID Animale:"));
        panel.add(idAnimaleField);

        panel.add(new JLabel("Descrizione diagnosi:"));
        panel.add(descrizioneField);

        panel.add(new JLabel("Farmaci (separati da virgola):"));
        panel.add(farmaciField);

        panel.add(new JLabel()); // spacer
        panel.add(inviaButton);

        inviaButton.addActionListener(e -> {
            try {
                int idVisita = getVisitaCorrente(Integer.parseInt(idAnimaleField.getText().trim()));
                String descrizione = descrizioneField.getText().trim();
                String[] farmaci = farmaciField.getText().trim().split(",");

                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO diagnosi (id_visita, descrizione) VALUES (?, ?)")) {
                    ps.setInt(1, idVisita);
                    ps.setString(2, descrizione);
                    ps.executeUpdate();
                }

                for (String nomeFarmaco : farmaci) {
                    nomeFarmaco = nomeFarmaco.trim();
                    if (!nomeFarmaco.isEmpty()) {
                        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO prescrizione (id_visita, nome_farmaco) VALUES (?, ?)")) {
                            ps.setInt(1, idVisita);
                            ps.setString(2, nomeFarmaco);
                            ps.executeUpdate();
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, "Diagnosi e farmaci registrati.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel creaPannelloVaccinazione() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField idAnimaleField = new JTextField();
        JTextField nomeVaccinoField = new JTextField();
        JTextField dataField = new JTextField();

        JButton inviaButton = new JButton("Registra Vaccino");

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("ID Animale:"));
        panel.add(idAnimaleField);

        panel.add(new JLabel("Nome Vaccino:"));
        panel.add(nomeVaccinoField);

        panel.add(new JLabel("Data (YYYY-MM-DD):"));
        panel.add(dataField);

        panel.add(new JLabel());
        panel.add(inviaButton);

        inviaButton.addActionListener(e -> {
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO vaccinazione (id_animale, nome_vaccino, data, cf_veterinario) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, Integer.parseInt(idAnimaleField.getText().trim()));
                ps.setString(2, nomeVaccinoField.getText().trim());
                ps.setString(3, dataField.getText().trim());
                ps.setString(4, cfVeterinario);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Vaccinazione registrata.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel creaPannelloStorico() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new FlowLayout());

        JTextField idAnimaleField = new JTextField(10);
        JButton cercaButton = new JButton("Cerca");
        JTextArea outputArea = new JTextArea(15, 50);
        outputArea.setEditable(false);

        inputPanel.add(new JLabel("ID Animale:"));
        inputPanel.add(idAnimaleField);
        inputPanel.add(cercaButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        cercaButton.addActionListener(e -> {
            String idAnimale = idAnimaleField.getText().trim();
            if (idAnimale.isEmpty()) return;

            try (PreparedStatement psDiagnosi = conn.prepareStatement(
                    "SELECT d.descrizione, v.data FROM diagnosi d JOIN visita v ON d.id_visita = v.id_visita WHERE v.id_animale = ?");
                 PreparedStatement psVaccini = conn.prepareStatement(
                    "SELECT nome_vaccino, data FROM vaccinazione WHERE id_animale = ?")) {

                psDiagnosi.setInt(1, Integer.parseInt(idAnimale));
                psVaccini.setInt(1, Integer.parseInt(idAnimale));

                StringBuilder sb = new StringBuilder("Diagnosi:\n");
                ResultSet rsD = psDiagnosi.executeQuery();
                while (rsD.next()) {
                    sb.append(rsD.getString("data")).append(" - ").append(rsD.getString("descrizione")).append("\n");
                }

                sb.append("\nVaccinazioni:\n");
                ResultSet rsV = psVaccini.executeQuery();
                while (rsV.next()) {
                    sb.append(rsV.getString("data")).append(" - ").append(rsV.getString("nome_vaccino")).append("\n");
                }

                outputArea.setText(sb.toString());

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });

        return panel;
    }

    private int getVisitaCorrente(int idAnimale) throws SQLException {
        String query = "SELECT id_visita FROM visita WHERE id_animale = ? ORDER BY data DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idAnimale);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_visita");
            } else {
                throw new SQLException("Nessuna visita trovata per l'animale.");
            }
        }
    }
}
