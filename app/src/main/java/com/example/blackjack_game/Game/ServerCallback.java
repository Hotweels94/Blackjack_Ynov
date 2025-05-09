package com.example.blackjack_game.Game;

import org.json.JSONObject;

public interface ServerCallback {
    void onResponse(JSONObject response);
    void onError(Exception e);
}
