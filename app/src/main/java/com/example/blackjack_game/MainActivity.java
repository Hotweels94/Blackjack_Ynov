package com.example.blackjack_game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.blackjack_game.Game.Card;
import com.example.blackjack_game.Game.GameLogic;
import android.widget.EditText;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity {
    private GameLogic game;
    private TextView dealerHandTitle, playerHandTitle, balanceText;
    private LinearLayout dealerCardsLayout, playerCardsLayout;
    private Button hitButton, standButton, doubleButton, seeHandsButton, continueButton, quitButton;
    private int currentRound = 1;
    private View resultOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        game = new GameLogic("Joueur", 3000.0);
        
        setupButtons();
        setupResultButtons();
        
        game.setGameStateListener(() -> runOnUiThread(this::updateGameState));
        showBetDialog();
    }

    private void initViews() {
        dealerHandTitle = findViewById(R.id.dealerHandTitle);
        playerHandTitle = findViewById(R.id.playerHandTitle);
        balanceText = findViewById(R.id.balanceText);
        dealerCardsLayout = findViewById(R.id.dealerCardsLayout);
        playerCardsLayout = findViewById(R.id.playerCardsLayout);
        hitButton = findViewById(R.id.hitButton);
        standButton = findViewById(R.id.standButton);
        doubleButton = findViewById(R.id.doubleButton);
        seeHandsButton = findViewById(R.id.see_hands);
        resultOverlay = findViewById(R.id.resultOverlay);
        continueButton = findViewById(R.id.continueButton);
        quitButton = findViewById(R.id.quitButton);
    }

    private void setupButtons() {
        hitButton.setOnClickListener(v -> {
            game.playerHit();
            updateGameState();
            if (game.getPlayer().getHand().isBusted()) {
                endRound();
            }
        });

        standButton.setOnClickListener(v -> {
            game.playerStand();
            endRound();
        });

        doubleButton.setOnClickListener(v -> {
            if (game.getPlayer().canDoubleDown()) {
                game.playerDoubleDown();
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
        game.determineWinner();
        updateGameState();
        showGameResult();
        currentRound++;
        updateRoundTitle();
        setGameButtonsEnabled(false);
    }

private void showGameResult() {
    String result = determineResultText();
    TextView resultText = findViewById(R.id.resultText);
    resultText.setText(result);
    
    if (game.getPlayer().getBalance() <= 0) {
        continueButton.setVisibility(View.GONE); // Cache  l'opt "Continuer" si solde = 0
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
        }else if (game.getPlayer().getHand().isBlackjack()) {
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
    });

    quitButton.setOnClickListener(v -> {
        finish();
    });
}
}