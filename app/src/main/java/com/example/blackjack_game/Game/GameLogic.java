package com.example.blackjack_game.Game;

import static com.example.blackjack_game.Game.Dealer.*;

import android.os.AsyncTask;
import android.util.Log;

import com.example.blackjack_game.ConnectionManager;
import com.example.blackjack_game.PageConnection;
import com.example.blackjack_game.Game.Dealer;
import com.example.blackjack_game.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.Socket;
import java.util.List;

public class GameLogic {
    private Deck deck;
    private Player player;
    private Dealer dealer;


    public GameLogic(String playerName, double initialBalance) {
        deck = new Deck();
        player = new Player(playerName, initialBalance);
        dealer = new Dealer();
    }

    public void startNewRound(double betAmount) throws JSONException {
        player.placeBet(betAmount);
        player.clearHand();
        dealer.clear();

        player.getHand().addCard(deck.dealCard());
        player.getHand().addCard(deck.dealCard());


        JSONObject playerHandInJson = player.getHandInJson();
        ConnectionManager.SendData(playerHandInJson);
        int millis = 100;

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        dealer.getDealerHandFromServer(new ServerCallback() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                dealer.addCardFromJson(response);
                if (gameStateListener != null) {
                    gameStateListener.onDealerHandUpdated();
                    Log.d("GameLogic", "Main dealer REEL : " + dealer.getCards().toString());
                    Log.d("GameLogic", "Main Joueur REEL : " + player.getHand().toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void playerHit() {
        player.getHand().addCard(deck.dealCard());
    }

    public void playerStand() {
        dealerPlay();
    }

    public void playerDoubleDown() {
        player.placeBet(player.getCurrentBet());
        player.getHand().addCard(deck.dealCard());
        dealerPlay();
    }

    private void dealerPlay() {
        while (dealer.calculateScore() < 17) {
            dealer.addCard(deck.dealCard());
        }
    }

    public void determineWinner() {
        int playerScore = player.getHand().calculateScore();
        int dealerScore = dealer.calculateScore();

        if (player.getHand().isBlackjack()) {
            player.blackjack();
        } else if (player.getHand().isBusted()) {
            player.lose();
        } else if (dealer.isBusted()) {
            player.win(player.getCurrentBet() * 2);
        } else if (playerScore > dealerScore) {
            player.win(player.getCurrentBet() * 2);
        } else if (playerScore == dealerScore) {
            player.push();
        } else {
            player.lose();
        }
    }

    public boolean isRoundOver() {
        return player.getHand().isBusted() || 
               dealer.calculateScore() >= 17 ||
               (player.getHand().isBlackjack() && !dealer.isBlackjack());
    }

    public Player getPlayer() {
        return player;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public String getDealerHandForDisplay(boolean showAll) {
        if (showAll) {
            return dealer.toString();
        } else {
            return dealer.getPartialHand();
        }
    }

    public String getPlayerHandForDisplay() {
        return player.getHand().toString();
    }

    public List<Card> getDealerCards() {
        return dealer.getCards();
    }

    public interface GameStateListener {
        void onDealerHandUpdated();
    }

    private GameStateListener gameStateListener;

    public void setGameStateListener(GameStateListener listener) {
        this.gameStateListener = listener;
    }
}