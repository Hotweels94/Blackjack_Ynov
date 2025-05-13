package com.example.blackjack_game;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PageConnection extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView aText, connectionText;

    public static String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        PageConnection.username = username;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        aText = findViewById(R.id.aText);
        connectionText = findViewById(R.id.connexionText);

        // Initialisation des vues
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // Définir l'action au clic sur le bouton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                // Vérification des champs non vides
                if (!email.isEmpty() && !password.isEmpty()) {
                    new SocketClient().execute(email, password);
                } else {
                    aText.setText("Veuillez remplir tous les champs !");
                }
            }
        });
    }

    public class SocketClient extends AsyncTask<String, Void, String> {
        private static final String SERVER_IP = "10.0.0.19";
        private static final int SERVER_PORT = 5555;
        private PrintWriter output;
        private BufferedReader input;
        private Socket socket;
        public String username;


        @Override
        protected String doInBackground(String... params) {
            try {
                username = params[0];
                setUsername(username);
                String password = params[1];

                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                ConnectionManager.setSocket(socket);
                ConnectionManager.setOut(output);
                ConnectionManager.setIn(input);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);

                output.println(json.toString());

                // Lance le thread d'écoute
                new Thread(new IncomingReader()).start();

                // Ne lis PAS la réponse ici, laisse IncomingReader s'en occuper
                return "Connexion établie";

            } catch (Exception e) {
                e.printStackTrace();
                return "Erreur de connexion";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Affiche la réponse dans un TextView ou autre
            Toast.makeText(PageConnection.this, result, Toast.LENGTH_SHORT).show();
            connectionText.setText("Waiting for players...");
        }

        private class IncomingReader implements Runnable {
            @Override
            public void run() {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        final String finalMessage = message;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalMessage.trim().equals("start_game")) {
                                    Intent intent = new Intent(PageConnection.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    /* Pour les autres messages */
                                    Log.d("PageConnection", finalMessage);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectionText.setText("Erreur de connexion au serveur");
                        }
                    });
                }
            }
        }
    }
}