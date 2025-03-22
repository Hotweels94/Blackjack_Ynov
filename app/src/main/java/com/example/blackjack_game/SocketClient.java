package com.example.blackjack_game;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient extends AsyncTask<String, Void, String> {
    private static final String SERVER_IP = "192.168.1.168"; // Adresse IP PC
    private static final int SERVER_PORT = 5555;

    @Override
    protected String doInBackground(String... params) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            // Envoi des données au serveur
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String jsonData = "{\"username\":\"\",\"balance\":1000}";
            out.println(jsonData);

            // Lecture de la réponse du serveur
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            Log.d("SOCKET", "Réponse du serveur: " + response);

            socket.close();
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

