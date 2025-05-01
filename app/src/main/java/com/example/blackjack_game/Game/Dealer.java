package com.example.blackjack_game.Game;
import java.util.List;

public class Dealer extends Hand {
    public String getPartialHand() {
        if (cards.isEmpty()) {
            return "Aucune cartes";
        }
        return cards.get(0).toString() + ", [Cartes cachées]";
    }

    public List<Card> getCards() {
        return cards;
    }
}