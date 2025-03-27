package com.example.blackjack_game.Game;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    protected List<Card> cards;

    public Hand() {
        cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void clear() {
        cards.clear();
    }

    public int calculateScore() {
        int score = 0;
        int aceCount = 0;

        for (Card card : cards) {
            score += card.getValue();
            if (card.getRank() == Card.Rank.ACE) {
                aceCount++;
            }
        }

        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }

        return score;
    }

    public boolean isBusted() {
        return calculateScore() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && calculateScore() == 21;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card.toString()).append(", ");
        }
        if (!cards.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append(" (Score: ").append(calculateScore()).append(")");
        return sb.toString();
    }

    public String getFirstCard() {
        if (cards.isEmpty()) {
            return "Acune cartes";
        }
        return cards.get(0).toString();
    }
}