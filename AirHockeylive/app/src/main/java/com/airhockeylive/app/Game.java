/****************************************************************************
 * Game.java
 * Author: Joseph Ellis
 * Student Number: 10007329

 This file is part of Air Hockey - Live!

 Air Hockey - Live! is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Air Hockey - Live! is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Air Hockey - Live!  If not, see <http://www.gnu.org/licenses/>.
 ***************************************************************************/
package com.airhockeylive.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/* The enum used throughout when referring to the state of a game. */
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

    public static List<Game> FetchOpenGames(UUID playerId)
    {
        Client client = new Client();
        List<Game> openGamesList = new ArrayList<Game>();
        JSONArray openGames = client.FetchGames(playerId);

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

    //Build the game object from the json object that is returned from the server
    public static Game GetGame(UUID id)
    {
        Client client = new Client();
        Game game = new Game();
        JSONObject gameData = client.GetGame(id.toString());

        try
        {
            game.id = UUID.fromString(gameData.getString(Constants.GAME_ID));
            game.player1 = client.GetPlayer(gameData.getString(Constants.GAME_PLAYER1));

            if (!gameData.isNull(Constants.GAME_PLAYER2))
            {
                game.player2 = client.GetPlayer(gameData.getString(Constants.GAME_PLAYER2));
            }

            game.state = GameState.valueOf(gameData.getString(Constants.GAME_STATE));

            if (!gameData.isNull(Constants.GAME_SCOREP1))
            {
                game.player1Score = gameData.getInt(Constants.GAME_SCOREP1);
            }

            if (!gameData.isNull(Constants.GAME_SCOREP2))
            {
                game.player2Score = gameData.getInt(Constants.GAME_SCOREP2);
            }
        }
        catch (JSONException e)
        {
            // TODO: do something useful
        }

        return game;
    }

    public Game CreateGame(Player creator)
    {
        Client client = new Client();
        this.id = UUID.randomUUID();
        this.player1 = creator;
        this.state = GameState.OPEN;

        client.CreateGame(this);

        return this;
    }
}
