package view;

import javax.swing.*;

import db.DatabaseUtils;

import java.awt.*;
import java.sql.*;

public class ClientePage extends JFrame {
    private final String idCliente;
    private final Connection conn;

    public ClientePage(String idCliente, Connection conn) {
        this.idCliente = idCliente;
        this.conn = conn;
        setTitle("Area Cliente");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Registra Animale", createRegistraAnimalePanel());
        tabs.addTab("Prenota Visita", createPrenotaVisitaPanel());
        tabs.addTab("Consulta Vaccini", createConsultaVacciniPanel());
        tabs.addTab("Consulta Diagnosi", createConsultaDiagnosiPanel());
        tabs.addTab("Consulta Prescrizioni", createConsultaPrescrizioniPanel());
        tabs.addTab("Elenco Animali", createElencoAnimaliPanel());
        tabs.addTab("Elimina Animale", createEliminaAnimalePanel());

        add(tabs);
    }

    private JPanel createRegistraAnimalePanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2));
        JTextField nomeField = new JTextField();
        JTextField specieField = new JTextField();
        JTextField razzaField = new JTextField();
        JTextField etaField = new JTextField();
        JTextField pesoField = new JTextField();
        JButton registraBtn = new JButton("Registra");

        panel.add(new JLabel("Nome:")); panel.add(nomeField);
        panel.add(new JLabel("Specie:")); panel.add(specieField);
        panel.add(new JLabel("Razza:")); panel.add(razzaField);
        panel.add(new JLabel("Età:")); panel.add(etaField);
        panel.add(new JLabel("Peso:")); panel.add(pesoField);
        panel.add(new JLabel("")); panel.add(registraBtn);

        registraBtn.addActionListener(e -> {
            try {
                int idSpecie = getOrCreateSpecie(specieField.getText());
                int nuovoIdAnimale = DatabaseUtils.getNextAvailableID(conn, "Animale");
                if (nuovoIdAnimale == 0) {
                    throw new IllegalStateException("Errore nell'assegnazione dell'ID animale");
                }
                String sql = "INSERT INTO Animale (id_animale, id_cliente, nome, id_specie, razza, eta, peso) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, nuovoIdAnimale);
                    stmt.setString(2, idCliente);
                    stmt.setString(3, nomeField.getText());
                    stmt.setInt(4, idSpecie);
                    stmt.setString(5, razzaField.getText());
                    stmt.setInt(6, Integer.parseInt(etaField.getText()));
                    stmt.setDouble(7, Double.parseDouble(pesoField.getText()));
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Animale registrato con successo.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });

        return panel;
    }

    private int getOrCreateSpecie(String nomeSpecie) throws SQLException {
        String select = "SELECT id_specie FROM Specie WHERE nome = ?";
        try (PreparedStatement stmt = conn.prepareStatement(select)) {
            stmt.setString(1, nomeSpecie);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_specie");
        }

        String insert = "INSERT INTO Specie (nome) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nomeSpecie);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Impossibile creare la specie.");
    }

    private JPanel createPrenotaVisitaPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField idAnimaleField = new JTextField();
        JTextField dataField = new JTextField();
        JButton prenotaBtn = new JButton("Prenota");

        panel.add(new JLabel("ID Animale:")); panel.add(idAnimaleField);
        panel.add(new JLabel("Data (YYYY-MM-DD):")); panel.add(dataField);
        panel.add(new JLabel("")); panel.add(prenotaBtn);

        prenotaBtn.addActionListener(e -> {
            try {
                int id_prenotazione = DatabaseUtils.getNextAvailableID(conn, "prenotazione");
                String sql = "INSERT INTO prenotazione (id_prenotazione, id_cliente, paziente, data) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id_prenotazione);
                    stmt.setString(2, idCliente);
                    stmt.setInt(3, Integer.parseInt(idAnimaleField.getText()));
                    stmt.setDate(4, Date.valueOf(dataField.getText()));
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Visita prenotata.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel createConsultaVacciniPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField idField = new JTextField();
        JButton queryBtn = new JButton("Consulta");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JPanel inputPanel = new JPanel(new GridLayout(1, 3));
        inputPanel.add(new JLabel("ID Animale:"));
        inputPanel.add(idField);
        inputPanel.add(queryBtn);

        queryBtn.addActionListener(e -> {
            try {
                String sql = "SELECT * FROM Vaccino WHERE id_animale = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(idField.getText()));
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    ResultSetMetaData meta = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            sb.append(meta.getColumnLabel(i)).append(": ").append(rs.getString(i)).append(" | ");
                        }
                        sb.append("\n");
                    }
                    resultArea.setText(sb.toString());
                }
            } catch (Exception ex) {
                resultArea.setText("Errore: " + ex.getMessage());
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConsultaDiagnosiPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField idField = new JTextField();
        JButton queryBtn = new JButton("Consulta");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JPanel inputPanel = new JPanel(new GridLayout(1, 3));
        inputPanel.add(new JLabel("ID Animale:"));
        inputPanel.add(idField);
        inputPanel.add(queryBtn);

        queryBtn.addActionListener(e -> {
            try {
                String sql = "SELECT * FROM Diagnosi WHERE id_animale = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(idField.getText()));
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    ResultSetMetaData meta = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            sb.append(meta.getColumnLabel(i)).append(": ").append(rs.getString(i)).append(" | ");
                        }
                        sb.append("\n");
                    }
                    resultArea.setText(sb.toString());
                }
            } catch (Exception ex) {
                resultArea.setText("Errore: " + ex.getMessage());
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConsultaPrescrizioniPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField idField = new JTextField();
        JButton queryBtn = new JButton("Consulta");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JPanel inputPanel = new JPanel(new GridLayout(1, 3));
        inputPanel.add(new JLabel("ID Animale:"));
        inputPanel.add(idField);
        inputPanel.add(queryBtn);

        queryBtn.addActionListener(e -> {
            try {
                String sql = """
                    SELECT f.*
                    FROM prescrizione p
                    JOIN farmaco f ON p.id_farmaco = f.id_farmaco
                    JOIN diagnosi d ON p.id_diagnosi = d.id_diagnosi
                    WHERE d.id_animale = ?
                """;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(idField.getText()));
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    ResultSetMetaData meta = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            sb.append(meta.getColumnLabel(i)).append(": ").append(rs.getString(i)).append(" | ");
                        }
                        sb.append("\n");
                    }
                    resultArea.setText(sb.toString());
                }
            } catch (Exception ex) {
                resultArea.setText("Errore: " + ex.getMessage());
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createElencoAnimaliPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton aggiornaBtn = new JButton("Aggiorna");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        aggiornaBtn.addActionListener(e -> {
            try {
                String sql = """
                        SELECT a.id_animale, a.nome, s.nome AS specie, a.razza, a.eta, a.peso
                        FROM Animale a
                        JOIN Specie s ON a.id_specie = s.id_specie
                        WHERE id_cliente = ?
                        """;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, idCliente);
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        sb.append("ID: ").append(rs.getInt("id_animale")).append(" | ")
                                .append("Nome: ").append(rs.getString("nome")).append(" | ")
                                .append("Specie: ").append(rs.getString("specie")).append(" | ")
                                .append("Razza: ").append(rs.getString("razza")).append(" | ")
                                .append("Età: ").append(rs.getInt("eta")).append(" | ")
                                .append("Peso: ").append(rs.getDouble("peso")).append("\n");
                    }
                    resultArea.setText(sb.toString());
                }
            } catch (Exception ex) {
                resultArea.setText("Errore: " + ex.getMessage());
            }
        });

        panel.add(aggiornaBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEliminaAnimalePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField idAnimaleField = new JTextField();
        JButton eliminaBtn = new JButton("Elimina");

        panel.add(new JLabel("ID Animale:")); panel.add(idAnimaleField);
        panel.add(new JLabel("")); panel.add(eliminaBtn);

        eliminaBtn.addActionListener(e -> {
            try {
                String sql = "DELETE FROM Animale WHERE id_animale = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(idAnimaleField.getText()));
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Animale eliminato con successo.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage());
            }
        });

        return panel;
    }
}

