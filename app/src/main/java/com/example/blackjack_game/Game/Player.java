package com.example.blackjack_game.Game;

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
}