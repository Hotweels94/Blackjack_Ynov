package com.example.blackjack_game.Game;
import com.example.blackjack_game.ConnectionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public void getDealerHandFromServer(ServerCallback callback) {
        ConnectionManager.requestDealerHand(new ServerCallback() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("type") && response.getString("type").equals("dealer_hand")) {
                        JSONArray cardsJson = response.getJSONArray("cards");

                        // mise à jour des cartes du Dealer
                        Dealer.this.clear();
                        for (int i = 0; i < cardsJson.length(); i++) {
                            JSONObject card = cardsJson.getJSONObject(i);
                            Card.Rank rank = Card.Rank.valueOf(card.getString("rank"));
                            Card.Suit suit = Card.Suit.valueOf(card.getString("suit"));
                            Dealer.this.addCard(new Card(suit, rank));
                        }

                        callback.onResponse(response);
                    }
                } catch (JSONException e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void addCardFromJson(JSONObject cardJson) throws JSONException {
        JSONArray cardsArray = cardJson.getJSONArray("cards");

        // Crée un dealer pour ajouter les cartes
        Dealer dealer = new Dealer();

        for (int i = 0; i < cardsArray.length(); i++) {
            JSONObject cardJson2 = cardsArray.getJSONObject(i);
            String rankStr = cardJson2.getString("rank");
            String suitStr = cardJson2.getString("suit");

            // Convertir en enums
            Card.Rank rank = Card.Rank.valueOf(rankStr);
            Card.Suit suit = Card.Suit.valueOf(suitStr);

            // Créer la carte et l'ajouter au dealer
            Card card = new Card(suit, rank);
            dealer.addCard(card);
        }
    }
}