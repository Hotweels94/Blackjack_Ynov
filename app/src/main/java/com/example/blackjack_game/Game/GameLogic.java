package com.example.blackjack_game.Game;

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

    public void startNewRound(double betAmount) {
        player.placeBet(betAmount);
        player.clearHand();
        dealer.clear();

        player.getHand().addCard(deck.dealCard());
        dealer.addCard(deck.dealCard());
        player.getHand().addCard(deck.dealCard());
        dealer.addCard(deck.dealCard());
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
}