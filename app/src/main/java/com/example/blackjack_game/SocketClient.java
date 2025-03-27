package com.example.blackjack_game;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class SocketClient extends AsyncTask<String, Void, String> {
    private static final String SERVER_IP = getHostIpAddress();
    private static final int SERVER_PORT = 5555;
    private Context context;
    private SocketClientListener listener;

    private static String getHostIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1"; // Fallback si aucune IP trouvée
        }
    }

    public SocketClient(Context context, SocketClientListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            JSONObject json = new JSONObject();
            json.put("email", params[0]);
            json.put("password", params[1]);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(json.toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            socket.close();
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de connexion au serveur";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            listener.onResponseReceived(result);
        }

        if (!"OK".equals(result)) {
            Toast.makeText(context, "Erreur : " + result, Toast.LENGTH_SHORT).show();
        }
    }

    public interface SocketClientListener {
        void onResponseReceived(String response);
    }
}

