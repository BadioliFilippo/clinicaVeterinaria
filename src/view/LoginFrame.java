package view;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import db.DatabaseUtils;

public class LoginFrame extends JFrame {

    private JTextField codiceFiscaleField;
    private JButton clienteButton;
    private JButton veterinarioButton;
    private Connection conn;

    public LoginFrame(Connection conn) {
        this.conn = conn;
        setTitle("Login Clinica Veterinaria");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        codiceFiscaleField = new JTextField(20);
        clienteButton = new JButton("Login Cliente");
        veterinarioButton = new JButton("Login Veterinario");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Codice Fiscale:"));
        panel.add(codiceFiscaleField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clienteButton);
        buttonPanel.add(veterinarioButton);
        panel.add(buttonPanel);

        add(panel);

        clienteButton.addActionListener(e -> {
            String codiceFiscale = codiceFiscaleField.getText().trim();
            if (codiceFiscale.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci il codice fiscale.");
                return;
            }
            JOptionPane.showMessageDialog(this, "Login effettuato con successo!");
            this.dispose(); // chiude il frame di login
            ClientePage clientePage = new ClientePage(codiceFiscale, conn); 
            clientePage.setVisible(true);
        });
        
        veterinarioButton.addActionListener(e -> handleLogin("veterinario"));

        setVisible(true);
    }

    private void handleLogin(String tipo) {
        String cf = codiceFiscaleField.getText().trim();

        if (cf.equalsIgnoreCase("admin")) {
            openAdminPage();
        } else if (tipo.equals("cliente")) {
            openClientePage(cf);
        } else if (tipo.equals("veterinario")) {
            openVeterinarioPage(cf);
        } else {
            JOptionPane.showMessageDialog(this, "Errore login.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openClientePage(String cf) {
        if(DatabaseUtils.isCFCliente(conn, cf)) {
            JOptionPane.showMessageDialog(this, "Login Cliente effettuato con CF: " + cf);
            new ClientePage(cf, conn);
        } 
        else {
            JOptionPane.showMessageDialog(this, "CF non nel database", "ERRORE", 0);
        }
    }

    private void openVeterinarioPage(String cf) {
        if(DatabaseUtils.isCFVeterinario(conn, cf)) {
            JOptionPane.showMessageDialog(this, "Login Cliente effettuato con CF: " + cf);
            new VeterinarioPage(conn, cf);
        }
        else {
            JOptionPane.showMessageDialog(this, "CF non nel database", "ERRORE", 0);
        }
    }

    private void openAdminPage() {
        JOptionPane.showMessageDialog(this, "Accesso Admin");
        new AdminPage(conn); // da implementare
    }
}

