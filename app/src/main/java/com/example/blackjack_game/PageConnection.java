// app/src/main/java/com/example/blackjack_game/PageConnection.java
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
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class PageConnection extends AppCompatActivity {

    private static final String TAG = "PageConnection";

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView aText, connectionText, registerLink;

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
        registerLink = findViewById(R.id.registerLink);

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

                Log.d(TAG, "Bouton Se connecter cliqué");
                Log.d(TAG, "Pseudo: " + email);
                Log.d(TAG, "Mot de passe: " + password);

                // Vérification des champs non vides
                if (!email.isEmpty() && !password.isEmpty()) {
                    new LoginTask().execute(email, password);
                } else {
                    aText.setText("Veuillez remplir tous les champs !");
                }
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PageConnection.this, RegisterActivity.class);
                Log.d("PageConnection", "Avant startActivity");
                startActivity(intent);
                Log.d("PageConnection", "Après startActivity");
                finish();
                Log.d("PageConnection", "Après finish");            
            }
        });
    }

    public class LoginTask extends AsyncTask<String, Void, String> {
        private static final String SERVER_IP = "192.168.228.118";
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

                Log.d(TAG, "Envoi des données de connexion au serveur");
                Log.d(TAG, "Pseudo: " + username);
                Log.d(TAG, "Mot de passe: " + password);

                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                ConnectionManager.setSocket(socket);
                ConnectionManager.setOut(output);
                ConnectionManager.setIn(input);

                JSONObject json = new JSONObject();
                json.put("type", "login");
                json.put("pseudo", username);
                json.put("password", password);

                output.println(json.toString());

                String response = input.readLine();
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return "Erreur de connexion";
            }
        }

        @Override
        protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Toast.makeText(PageConnection.this, result, Toast.LENGTH_SHORT).show();
        connectionText.setText("Waiting for players...");

        new Thread(new IncomingReader()).start();
    }

        private class IncomingReader implements Runnable {
            @Override
            public void run() {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        Log.d("IncomingReader", "Message reçu: " + message);
                        final String finalMessage = message.trim();
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalMessage.equals("start_game")) {
                                    Log.d("PageConnection", "Redirection vers MainActivity");
                                    Intent intent = new Intent(PageConnection.this, MainActivity.class);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                    finish(); // Ferme la page de connexion
                                } else {
                                    try {
                                        JSONObject json = new JSONObject(finalMessage);
                                        if (json.has("type") && json.getString("type").equals("dealer_hand")) {
                                            Log.d("PageConnection", "Main du croupier reçue, redirection...");
                                            Intent intent = new Intent(PageConnection.this, MainActivity.class);
                                            intent.putExtra("username", username);
                                            intent.putExtra("dealer_hand", finalMessage);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("PageConnection", "Erreur parsing JSON", e);
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("IncomingReader", "Erreur", e);
                    runOnUiThread(() -> connectionText.setText("Erreur de connexion"));
                }
            }
        }
    }
}
