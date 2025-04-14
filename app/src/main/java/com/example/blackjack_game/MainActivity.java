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
    private Button loginButton;
    private TextView aText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aText = findViewById(R.id.aText);

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

    private class SocketClient extends AsyncTask<String, Void, String> {
        private static final String SERVER_IP = "192.168.56.1";
        private static final int SERVER_PORT = 5555;
        private PrintWriter output;
        private BufferedReader input;
        private Socket socket;

        @Override
        protected String doInBackground(String... params) {
            try {
                String email = params[0];
                String password = params[1];

                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                JSONObject json = new JSONObject();
                json.put("username", email);
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
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
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
                                if (finalMessage.equals("start_game")) {
                                    aText.setText("HEY OH!");
                                    Toast.makeText(MainActivity.this, "La partie commence !", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Pour les autres messages
                                    aText.setText(finalMessage);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aText.setText("Erreur de connexion au serveur");
                        }
                    });
                }
            }
        }
    }
}
