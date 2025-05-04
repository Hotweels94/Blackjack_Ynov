package com.example.blackjack_game.Game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Player {
    private String name;
    private double balance;
    private double currentBet;
    private Hand hand;

    public Player(String name, double initialBalance) {
        this.name = name;
        this.balance = initialBalance;
        this.hand = new Hand();
    }

    public void placeBet(double amount) {
        if (amount > balance) {
            throw new IllegalArgumentException("Montant de la mise dépasse le solde actuel");
        }
        currentBet = amount;
        balance -= amount;
    }

    public void win(double amount) {
        balance += amount;
    }

    public void lose() {
        // Parid éja déduit lors de la mise
    }

    public void push() {
        balance += currentBet;
    }

    public void blackjack() {
        balance += currentBet * 1.5;
    }

    public Hand getHand() {
        return hand;
    }

    public void clearHand() {
        hand.clear();
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public double getCurrentBet() {
        return currentBet;
    }

    public boolean canSplit() {
        return hand.calculateScore() == 21 && hand.cards.size() == 2 && 
               hand.cards.get(0).getValue() == hand.cards.get(1).getValue();
    }

    public boolean canDoubleDown() {
        return hand.cards.size() == 2 && balance >= currentBet;
    }

    public JSONObject getHandInJson() throws JSONException {
        JSONArray cardArray = new JSONArray();
        for (Card card : hand.getCards()) {
            JSONObject cardJson = new JSONObject();
            cardJson.put("rank", card.getRank());
            cardJson.put("suit", card.getSuit());
            cardArray.put(cardJson);
        }
        JSONObject result = new JSONObject();
        result.put("type", "hand");
        result.put("cards", cardArray);
        result.put("username", getName());
        return result;
    }
}