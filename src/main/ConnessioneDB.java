package main;

import java.sql.*;
import view.LoginFrame;

public class ConnessioneDB {
    private static Connection conn;

    public static void main(String[] args) {
        // Thread che mantiene attiva la connessione
        Thread dbThread = new Thread(() -> {
            String url = "jdbc:mysql://localhost:3306/clinicaveterinaria";
            String user = "root";
            String password = "!A2b3c4d";

            try {
                conn = DriverManager.getConnection(url, user, password);
                System.out.println("Connessione riuscita al database!");

                // Avvia GUI solo dopo connessione
                new LoginFrame(conn);

                // Mantieni attivo finché l'app non viene chiusa
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait(); // si blocca finché non viene notificato
                }

            } catch (SQLException e) {
                System.err.println("Errore di connessione:");
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("Thread DB interrotto.");
            }
        });

        dbThread.start();

        // Hook per chiusura
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Connessione chiusa al termine dell'applicazione.");
                }
            } catch (SQLException e) {
                System.err.println("Errore nella chiusura della connessione:");
                e.printStackTrace();
            }
        }));
    }
}

