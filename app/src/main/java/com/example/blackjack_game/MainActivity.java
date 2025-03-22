package com.example.blackjack_game;

import android.os.AsyncTask;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private TextView responseText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues
        emailInput = findViewById(R.id.emailInput);  // Récupère l'Email input
        passwordInput = findViewById(R.id.passwordInput); // Récupère le mot de passe input
        loginButton = findViewById(R.id.loginButton);    // Récupère le bouton de connexion

        // Définir l'action au clic sur le bouton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                // Vérification des champs non vides
                if (!email.isEmpty() && !password.isEmpty()) {
                    new SocketClient().execute(email, password); // Envoie email et mot de passe au serveur
                } else {
                    responseText.setText("Veuillez remplir tous les champs !");
                }
            }
        });
    }

    // Classe AsyncTask pour gérer la connexion au serveur via socket
    private class SocketClient extends AsyncTask<String, Void, String> {
        private static final String SERVER_IP = "192.168.1.168"; // Adresse IP du serveur
        private static final int SERVER_PORT = 5555;

        @Override
        protected String doInBackground(String... params) {
            try {
                // Création du socket pour se connecter au serveur
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);

                // Création d'un objet JSON avec l'email et le mot de passe
                JSONObject json = new JSONObject();
                json.put("email", params[0]);  // Email
                json.put("password", params[1]);  // Mot de passe

                // Envoi des données au serveur
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(json.toString());

                // Lecture de la réponse du serveur
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                socket.close();  // Fermeture de la connexion
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return "Erreur de connexion au serveur";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Affiche la réponse du serveur dans le TextView
            if (result.equals("OK")) {
                responseText.setText("Connexion réussie");
            } else {
                responseText.setText("Erreur : " + result);
                // Optionnel: Toast pour afficher un message en haut de l'écran
                Toast.makeText(MainActivity.this, "Erreur : " + result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
