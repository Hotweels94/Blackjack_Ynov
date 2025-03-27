package com.example.blackjack_game.Game;

public class Dealer extends Hand {
    public String getPartialHand() {
        if (cards.isEmpty()) {
            return "Aucune cartes";
        }
        return cards.get(0).toString() + ", [Cartes cachées]"; /* affichage test */
    }
}