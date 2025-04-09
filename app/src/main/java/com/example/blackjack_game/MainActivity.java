package com.example.blackjack_game;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blackjack_game.Game.BlackjackConsole;

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
                    responseText.setText("Veuillez remplir tous les champs !");
                }
            }
        });
    }

    private class SocketClient extends AsyncTask<String, Void, String> {
        private static final String SERVER_IP = "192.168.56.1"; // Adresse IP du serveur
        private static final int SERVER_PORT = 5555;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        @Override
        protected String doInBackground(String... params) {
            try {
                // Création du socket pour se connecter au serveur
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);

                // Création d'un objet JSON avec l'email et le mot de passe
                JSONObject json = new JSONObject();
                json.put("email", params[0]);
                json.put("password", params[1]);

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(json.toString());
                String serverMessage;
                while((serverMessage = in.readLine()) != null) {
                    final String message = serverMessage;
                    runOnUiThread(() -> handleServerMessage(message));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("OK")) {
                responseText.setText("Connexion réussie");
            } else {
                responseText.setText("Erreur : " + result);
                Toast.makeText(MainActivity.this, "Erreur : " + result, Toast.LENGTH_SHORT).show();
            }
        }

        private void handleServerMessage(String message) {
            if (message.equals("start_game")) {
                responseText.setText("La partie commence !");
                
                BlackjackConsole.start_game();
            } else {
                responseText.setText("Message du serveur : " + message);
            }

        }
    }
}
