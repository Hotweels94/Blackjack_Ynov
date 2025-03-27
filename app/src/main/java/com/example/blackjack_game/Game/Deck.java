package com.example.blackjack_game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;
    private int currentIndex;

    public Deck() {
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        cards = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
        currentIndex = 0;
    }

    public Card dealCard() {
        if (currentIndex >= cards.size()) {
            shuffle();
        }
        return cards.get(currentIndex++);
    }

    public int remainingCards() {
        return cards.size() - currentIndex;
    }
}