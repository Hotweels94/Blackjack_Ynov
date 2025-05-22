// app/src/main/java/com/example/blackjack_game/RegisterActivity.java
package com.example.blackjack_game;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText pseudoInput, passwordInput, ipInput;
    private Button registerButton;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        pseudoInput = findViewById(R.id.pseudoInput);
        passwordInput = findViewById(R.id.passwordInput);
        ipInput = findViewById(R.id.ipInput);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pseudo = pseudoInput.getText().toString();
                String password = passwordInput.getText().toString();
                String serverIp = ipInput.getText().toString();

                Log.d(TAG, "Bouton S'inscrire cliqué");
                Log.d(TAG, "Pseudo: " + pseudo);
                Log.d(TAG, "Mot de passe: " + password);

                if (!pseudo.isEmpty() && !password.isEmpty() && !serverIp.isEmpty()) {
                    new RegisterTask(serverIp).execute(pseudo, password);
                } else {
                    Toast.makeText(RegisterActivity.this, "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, PageConnection.class);
                startActivity(intent);
            }
        });
    }

    public class RegisterTask extends AsyncTask<String, Void, String> {
        private final String serverIp;
        private static final String TAG = "RegisterTask";
        // private static final String SERVER_IP = "192.168.228.118";
        private static final int SERVER_PORT = 5555;
        private PrintWriter output;
        private BufferedReader input;
        private Socket socket;

        public RegisterTask(String serverIp) {
            this.serverIp = serverIp;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String pseudo = params[0];
                String password = params[1];

                Log.d(TAG, "Connexion au serveur: " + serverIp + ":" + SERVER_PORT);
                socket = new Socket(serverIp, SERVER_PORT);

                Log.d(TAG, "Envoi des données d'inscription au serveur");
                Log.d(TAG, "Pseudo: " + pseudo);
                Log.d(TAG, "Mot de passe: " + password);

                // socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                JSONObject json = new JSONObject();
                json.put("type", "register");
                json.put("pseudo", pseudo);
                json.put("password", password);

                output.println(json.toString());

                String response = input.readLine();
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return "Erreur de connexion à " + serverIp + ": " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
            if (result.equals("GOOD")) {
                Intent intent = new Intent(RegisterActivity.this, PageConnection.class);
                startActivity(intent);
            }
        }
    }
}
