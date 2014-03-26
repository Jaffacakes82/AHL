package com.airhockeylive.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Joe on 26/02/14.
 */
enum GameState
{
    NONE, OPEN, STARTED, FINISHED
}

public class Game implements Serializable
{
    public UUID id;

    public Player player1;

    public Player player2;

    public int player1Score;

    public int player2Score;

    public GameState state;

    public Player winner;

    public Game()
    {
    }

    public static List<Game> FetchOpenGames()
    {
        Client client = new Client();
        List<Game> openGamesList = new ArrayList<Game>();
        JSONArray openGames = client.FetchGames();

        if (openGames.length() > 0)
        {
            for (int i = 0; i < openGames.length(); i++)
            {
                try
                {
                    Game game = new Game();
                    JSONObject gameData = openGames.getJSONObject(i);
                    game.id = UUID.fromString(gameData.getString(Constants.GAME_ID));
                    game.player1 = client.GetPlayer(gameData.getString(Constants.GAME_PLAYER1));
                    game.state = GameState.valueOf(gameData.getString(Constants.GAME_STATE));
                    openGamesList.add(game);
                }
                catch (JSONException ex)
                {
                    // Do something
                }
            }
        }

        return openGamesList;
    }
}
