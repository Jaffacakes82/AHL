package com.airhockeylive.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class Game
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

        Game game = new Game();

        if (openGames.length() > 0)
        {
            for (int i = 0; i < openGames.length(); i++)
            {
                try
                {
                    JSONObject gameData = openGames.getJSONObject(i);

                    game.id = UUID.fromString(gameData.getString(Constants.GAME_ID));
                    game.player1 = client.GetPlayer()
                }
                catch (JSONException ex)
                {
                    // Do something
                }
            }
        }
    }
}