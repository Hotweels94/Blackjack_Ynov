package com.example.blackjack_game;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.blackjack_game.Game.Card;
import com.example.blackjack_game.Game.GameLogic;
import com.example.blackjack_game.Game.ServerCallback;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private GameLogic game;
    private TextView dealerHandTitle, playerHandTitle, balanceText, usernameText;
    private LinearLayout dealerCardsLayout, playerCardsLayout;
    private Button hitButton, standButton, doubleButton, seeHandsButton, continueButton, quitButton;
    private int currentRound = 1;
    private View resultOverlay;
    public static String username;
    private SharedPreferences prefs;
    private double userBalance = 1000.0; // Valeur par défaut

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupérer le nom d'utilisateur depuis l'intent ou PageConnection
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            username = PageConnection.username;
        }
        
        // Initialisation des préférences pour sauvegarder localement
        prefs = getSharedPreferences("BlackjackPrefs", MODE_PRIVATE);
        
        // Récupérer le solde de l'utilisateur depuis la DB ou les préférences
        getUserBalance();

        initViews();
        
        // Initialiser le jeu avec le solde récupéré
        game = new GameLogic(username, userBalance);
        
        setupButtons();
        setupResultButtons();

        // Afficher le nom d'utilisateur
        usernameText.setText(username);
        
        // Configurer l'écouteur pour les mises à jour du jeu
        game.setGameStateListener(() -> runOnUiThread(this::updateGameState));
        
        // Démarrer la partie avec la boîte de dialogue de mise
        showBetDialog();
    }

    private void getUserBalance() {
        // Essayer de récupérer depuis les préférences d'abord
        userBalance = prefs.getFloat(username + "_balance", 1000.0f);
        
        // On pourrait aussi faire une requête au serveur pour obtenir le solde à jour
        JSONObject requestBalance = new JSONObject();
        try {
            requestBalance.put("type", "get_balance");
            requestBalance.put("username", username);
            
            // Requête asynchrone (idéalement, attendre la réponse avant de continuer)
            ConnectionManager.SendData(requestBalance);
            
            // Note: Dans une implémentation réelle, vous attendriez la réponse du serveur
            // avant de continuer, en utilisant un callback ou une autre méthode
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Erreur lors de la demande du solde: " + e.getMessage());
        }
    }

    private void saveUserBalance() {
        // Sauvegarder localement
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(username + "_balance", (float)game.getPlayer().getBalance());
        editor.apply();
        
        // Envoyer au serveur
        JSONObject updateBalance = new JSONObject();
        try {
            updateBalance.put("type", "update_balance");
            updateBalance.put("username", username);
            updateBalance.put("balance", game.getPlayer().getBalance());
            ConnectionManager.SendData(updateBalance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        dealerHandTitle = findViewById(R.id.dealerHandTitle);
        playerHandTitle = findViewById(R.id.playerHandTitle);
        balanceText = findViewById(R.id.balanceText);
        usernameText = findViewById(R.id.usernameText);
        dealerCardsLayout = findViewById(R.id.dealerCardsLayout);
        playerCardsLayout = findViewById(R.id.playerCardsLayout);
        hitButton = findViewById(R.id.hitButton);
        standButton = findViewById(R.id.standButton);
        doubleButton = findViewById(R.id.doubleButton);
        seeHandsButton = findViewById(R.id.see_hands);
        resultOverlay = findViewById(R.id.resultOverlay);
        continueButton = findViewById(R.id.continueButton);
        quitButton = findViewById(R.id.quitButton);
        
        // Mettre à jour le titre avec le numéro de manche
        updateRoundTitle();
    }

    private void setupButtons() {
        hitButton.setOnClickListener(v -> {
            game.playerHit();
            updateGameState();
            
            // Notify server about the hit action
            try {
                JSONObject hitAction = new JSONObject();
                hitAction.put("type", "player_action");
                hitAction.put("action", "hit");
                hitAction.put("username", username);
                ConnectionManager.SendData(hitAction);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            if (game.getPlayer().getHand().isBusted()) {
                endRound();
            }
        });

        standButton.setOnClickListener(v -> {
            game.playerStand();
            
            // Notify server about the stand action
            try {
                JSONObject standAction = new JSONObject();
                standAction.put("type", "player_action");
                standAction.put("action", "stand");
                standAction.put("username", username);
                ConnectionManager.SendData(standAction);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            endRound();
        });

        doubleButton.setOnClickListener(v -> {
            if (game.getPlayer().canDoubleDown()) {
                game.playerDoubleDown();
                
                // Notify server about the double down action
                try {
                    JSONObject doubleAction = new JSONObject();
                    doubleAction.put("type", "player_action");
                    doubleAction.put("action", "double");
                    doubleAction.put("username", username);
                    ConnectionManager.SendData(doubleAction);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                endRound();
            } else {
                Toast.makeText(this, "Double down impossible", Toast.LENGTH_SHORT).show();
            }
        });

        seeHandsButton.setOnClickListener(v -> showOtherPlayersHands());
    }

    private void updateRoundTitle() {
        dealerHandTitle.setText(String.format("MANCHE %d - MAIN DU CROUPIER", currentRound));
    }

    private void showBetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Placez votre mise");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Montant entre 10 et " + game.getPlayer().getBalance());
        builder.setView(input);

        builder.setPositiveButton("Confirmer", (dialog, which) -> {
            try {
                double bet = Double.parseDouble(input.getText().toString());
                if (bet < 10 || bet > game.getPlayer().getBalance()) {
                    Toast.makeText(this, "Mise invalide", Toast.LENGTH_SHORT).show();
                    showBetDialog();
                } else {
                    startNewRound(bet);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show();
                showBetDialog();
            }
        });

        builder.setNegativeButton("Quitter", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }

    private void startNewRound(double bet) {
        try {
            // Envoyer l'information de mise au serveur
            JSONObject betInfo = new JSONObject();
            betInfo.put("type", "place_bet");
            betInfo.put("username", username);
            betInfo.put("amount", bet);
            ConnectionManager.SendData(betInfo);
            
            // Démarrer une nouvelle manche
            game.startNewRound(bet);
            updateGameState();
            setGameButtonsEnabled(true);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            showBetDialog();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateGameState() {
        runOnUiThread(() -> {
            // Màj solde
            balanceText.setText(String.format("%,d$", (int)game.getPlayer().getBalance()));

            // Màj les img des cartes
            updateCardImages();

            // Désactiver btn
            doubleButton.setEnabled(game.getPlayer().canDoubleDown());
        });
    }

    private void updateCardImages() {
        runOnUiThread(() -> {
            dealerCardsLayout.removeAllViews();
            playerCardsLayout.removeAllViews();

            // Cartes du croupier - toujours afficher au moins une carte
            boolean showDealerCards = !game.getDealer().getCards().isEmpty();
            
            if (showDealerCards) {
                for (int i = 0; i < game.getDealer().getCards().size(); i++) {
                    boolean isHidden = (i == 0 && !game.isRoundOver());
                    addCardToLayout(dealerCardsLayout, game.getDealer().getCards().get(i), isHidden);
                }
            }

            // Cartes du joueur
            for (Card card : game.getPlayer().getHand().getCards()) {
                addCardToLayout(playerCardsLayout, card, false);
            }
        });
    }

    private void addCardToLayout(LinearLayout layout, Card card, boolean hidden) {
        ImageView cardImage = createCardImageView(card, hidden);
        layout.addView(cardImage);

        // Ajouter space entre les cartes
        if (layout.getChildCount() > 1) {
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(10), 0));
            layout.addView(spacer, layout.getChildCount() - 1);
        }
    }

    private ImageView createCardImageView(Card card, boolean hidden) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(80),
                dpToPx(120)
        );
        params.setMargins(dpToPx(5), 0, dpToPx(5), 0);
        imageView.setLayoutParams(params);

        if (hidden) {
            imageView.setImageResource(R.drawable.card_back);
        } else {
            loadCardImage(imageView, card);
        }
        return imageView;
    }

    private void loadCardImage(ImageView imageView, Card card) {
        String imageName = getCardImageName(card);
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resId != 0) {
            imageView.setImageResource(resId);
        } else {
            imageView.setImageResource(R.drawable.card_back);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String getCardImageName(Card card) {
        // Convertit rang en string
        String rank;
        switch(card.getRank()) {
            case TWO: rank = "two"; break;
            case THREE: rank = "three"; break;
            case FOUR: rank = "four"; break;
            case FIVE: rank = "five"; break;
            case SIX: rank = "six"; break;
            case SEVEN: rank = "seven"; break;
            case EIGHT: rank = "eight"; break;
            case NINE: rank = "nine"; break;
            case TEN: rank = "ten"; break;
            case JACK: rank = "jack"; break;
            case QUEEN: rank = "queen"; break;
            case KING: rank = "king"; break;
            case ACE: rank = "ace"; break;
            default: rank = card.getRank().name().toLowerCase();
        }

        String suit = card.getSuit().name().toLowerCase();
        return rank + "_" + suit;
    }

    private void endRound() {
        // Déterminer le gagnant
        game.determineWinner();
        
        // Mettre à jour l'interface
        updateGameState();
        
        // Sauvegarder le solde mis à jour
        saveUserBalance();
        
        // Afficher le résultat
        showGameResult();
        
        // Préparer pour la manche suivante
        currentRound++;
        updateRoundTitle();
        setGameButtonsEnabled(false);
        
        // Envoyer les résultats de la manche au serveur
        JSONObject roundResult = new JSONObject();
        try {
            roundResult.put("type", "round_result");
            roundResult.put("username", username);
            roundResult.put("balance", game.getPlayer().getBalance());
            roundResult.put("result", determineResultText());
            ConnectionManager.SendData(roundResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showGameResult() {
        String result = determineResultText();
        TextView resultText = findViewById(R.id.resultText);
        resultText.setText(result);
        
        if (game.getPlayer().getBalance() <= 0) {
            continueButton.setVisibility(View.GONE); // Cache l'opt "Continuer" si solde = 0
            quitButton.setText("Game Over");
        } else {
            continueButton.setVisibility(View.VISIBLE);
            quitButton.setText("Quitter la partie");
        }
        
        resultOverlay.setVisibility(View.VISIBLE);
    }

    private String determineResultText() {
        if (game.getPlayer().getBalance() <= 0) {
            return "Game Over!\nVotre solde est épuisé.";
        } else if (game.getPlayer().getHand().isBlackjack()) {
            return "Blackjack!\nVous gagnez " + (int)(game.getPlayer().getCurrentBet() * 1.5) + "$ !";
        } else if (game.getPlayer().getHand().isBusted()) {
            return "Vous avez perdu!\nMise: " + (int)game.getPlayer().getCurrentBet() + "$";
        } else if (game.getDealer().isBusted()) {
            return "Croupier busted!\nVous gagnez " + (int)(game.getPlayer().getCurrentBet() * 2) + "$ !";
        } else {
            int playerScore = game.getPlayer().getHand().calculateScore();
            int dealerScore = game.getDealer().calculateScore();

            if (playerScore > dealerScore) {
                return "Vous gagnez!\n" + (int)(game.getPlayer().getCurrentBet() * 2) + "$ !";
            } else if (playerScore == dealerScore) {
                return "Égalité!\nRécupérez votre mise de " + (int)game.getPlayer().getCurrentBet() + "$";
            } else {
                return "Croupier gagne!\nMise: " + (int)game.getPlayer().getCurrentBet() + "$";
            }
        }
    }

    private void showOtherPlayersHands() {
        // Récupérer les mains des autres joueurs depuis le serveur
        JSONObject request = new JSONObject();
        try {
            request.put("type", "request_other_hands");
            request.put("username", username);
            ConnectionManager.SendData(request);
            
            // Temporairement, on affiche les données simulées
            showOtherHandsDialog();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void showOtherHandsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mains des autres joueurs");
        // Test autres joueurs (à remplacer données réelles)
        StringBuilder sb = new StringBuilder();
        sb.append("Joueur 2: 10♠ 8♦ (18)\n\n");
        sb.append("Joueur 3: A♣ K♥ (Blackjack)\n\n");
        sb.append("Joueur 4: 5♠ 5♥ 5♦ (15)\n\n");

        builder.setMessage(sb.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void setGameButtonsEnabled(boolean enabled) {
        hitButton.setEnabled(enabled);
        standButton.setEnabled(enabled);
        doubleButton.setEnabled(enabled && game.getPlayer().canDoubleDown());
    }

    private void setupResultButtons() {
        continueButton.setOnClickListener(v -> {
            resultOverlay.setVisibility(View.GONE);
            showBetDialog();
            
            // Informer le serveur de la continuation
            JSONObject continueUsername = new JSONObject();
            try {
                continueUsername.put("type", "continue");
                continueUsername.put("username", username);
                ConnectionManager.SendData(continueUsername);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        quitButton.setOnClickListener(v -> {
            // Informer le serveur que le joueur quitte
            JSONObject quitInfo = new JSONObject();
            try {
                quitInfo.put("type", "quit_game");
                quitInfo.put("username", username);
                ConnectionManager.SendData(quitInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Sauvegarder l'état du jeu en cas de mise en pause
        saveUserBalance();
    }
}