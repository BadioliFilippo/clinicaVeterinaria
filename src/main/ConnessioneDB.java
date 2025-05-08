package main;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import view.LoginFrame;

public class ConnessioneDB {
    private static Connection conn;

    public static void main(String[] args) {
        // Thread che mantiene attiva la connessione
        Thread dbThread = new Thread(() -> {
            String hostname = "";
            String url, user, password;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if(hostname.equals("ae0f-19a4-4cce-be83-2aa9bfeed0daDESKTOP-LK6JOS9")) {
                url = "jdbc:mysql://37.116.164.208:3306/clinicaveterinaria";
                user = "utente_remoto";
                password = "!A2b3c4d";
            } else {
                url = "jdbc:mysql://localhost:3306/clinicaveterinaria";
                user = "root";
                password = "!A2b3c4d";
            }
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

