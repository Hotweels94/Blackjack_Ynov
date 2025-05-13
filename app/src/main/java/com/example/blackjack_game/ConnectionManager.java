package com.example.blackjack_game;

import android.util.Log;

import com.example.blackjack_game.Game.ServerCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionManager {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    /* private static final String SERVER_IP = "192.168.56.1"; */
    private static final String SERVER_IP = "10.0.0.19";
    private static final int SERVER_PORT = 5555;

    public static void setSocket(Socket s) {
        socket = s;
    }

    public static Socket getSocket() {
        return socket;
    }

    public static void setOut(PrintWriter o) {
        out = o;
    }

    public static PrintWriter getOut() {
        return out;
    }

    public static void setIn(BufferedReader i) {
        in = i;
    }

    public static BufferedReader getIn() {
        return in;
    }


    public static void SendData(JSONObject data) {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                ConnectionManager.setSocket(socket);
                ConnectionManager.setOut(out);
                ConnectionManager.setIn(in);

                out.println(data.toString());
                out.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void requestDealerHand(ServerCallback callback) {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                ConnectionManager.setSocket(socket);
                ConnectionManager.setOut(out);
                ConnectionManager.setIn(in);

                JSONObject request = new JSONObject();
                request.put("type", "request_dealer_hand");

                out.println(request.toString());
                out.flush();

                // Lire la réponse ligne par ligne
                String responseLine = in.readLine(); // attend une ligne finie par '\n'
                Log.d("ConnectionManager", "Réponse reçue : " + responseLine);

                if (responseLine != null) {
                    JSONObject response = new JSONObject(responseLine);
                    callback.onResponse(response);
                } else {
                    callback.onError(new Exception("Aucune réponse reçue du serveur"));
                }

            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

}
