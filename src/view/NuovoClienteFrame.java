package view;

import javax.swing.*;

import db.DatabaseUtils;

import java.awt.*;
import java.sql.Connection;

public class NuovoClienteFrame extends JFrame {

    public NuovoClienteFrame(Connection conn, String codiceFiscale) {
        setTitle("Registrazione Nuovo Cliente");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextField nomeField = new JTextField(20);
        JTextField cognomeField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField telefonoField = new JTextField(20);
        JButton registraButton = new JButton("Registra");

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Cognome:"));
        panel.add(cognomeField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Telefono:"));
        panel.add(telefonoField);
        panel.add(new JLabel(""));
        panel.add(registraButton);

        add(panel);

        registraButton.addActionListener(e -> {
            String nome = nomeField.getText().trim();
            String cognome = cognomeField.getText().trim();
            String email = emailField.getText().trim();
            String telefono = telefonoField.getText().trim();

            if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tutti i campi sono obbligatori.");
                return;
            }

            boolean success = DatabaseUtils.inserisciNuovoCliente(conn, codiceFiscale, nome, cognome, email, telefono);

            if (success) {
                JOptionPane.showMessageDialog(this, "Cliente registrato con successo!");
                new ClientePage(codiceFiscale, conn).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Errore durante la registrazione.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
