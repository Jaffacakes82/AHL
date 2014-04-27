/****************************************************************************
* Client.java
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.UUID;

/* The Client class handles all requests made the web server. */
public class Client
{
    /* Class field declarations */
    private URI registerUri;

    private URI loginUri;

    private URI gamesListUri;

    private URI getPlayerUri;

    private URI updateGameUri;

    private URI getGameUri;

    private URI createGameUri;

    private URI startGameUri;

    private String defaultHost = "http://192.168.0.3";

    private int defaultPort = 3000;

    private String registerQuery = "/register";

    private String loginQuery = "/login";

    private String getGamesQuery = "/gameslist";

    private String getPlayerQuery = "/getplayer";

    private String updateGameQuery = "/updategame";

    private String getGameQuery = "/getgame";

    private String createGameQuery = "/creategame";

    private String startGameQuery = "/startgame";

    public Client()
    {
        /* Build all of the request URI's in the constructor */
        registerUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, registerQuery));
        loginUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, loginQuery));
        getPlayerUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, getPlayerQuery));
        gamesListUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, getGamesQuery));
        updateGameUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, updateGameQuery));
        getGameUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, getGameQuery));
        createGameUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, createGameQuery));
        startGameUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, startGameQuery));
    }

    /************************************************
     * StartGame()
     * @param gameToStart - The game object to start
     * @return - a value to determine whether the game
     * was successfully started.
     */
    public boolean StartGame(Game gameToStart)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject gameData = new JSONObject();
        String responseString;

        try
        {
            /* Build the JSON object to post to the server */
            gameData.put(Constants.GAME_ID, gameToStart.id);
            gameData.put(Constants.GAME_PLAYER1, gameToStart.player1.id);
            gameData.put(Constants.GAME_PLAYER2, gameToStart.player2.id);
            gameData.put(Constants.GAME_STATE, gameToStart.state.toString());
            gameData.put(Constants.GAME_SCOREP1, gameToStart.player1Score);
            gameData.put(Constants.GAME_SCOREP2, gameToStart.player2Score);
        }
        catch (JSONException e)
        {
            return false;
        }

        try
        {
            /* Build the post request */
            HttpPost request = new HttpPost(this.startGameUri);
            StringEntity jsonString = new StringEntity(gameData.toString());

            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
        }
        catch (Exception ex)
        {
            return false;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        if (responseString.equals("true"))
        {
            return true; // If we got true back from the server.
        }
        else
        {
            return false;
        }
    }

    /******************************************************************
     * CreateGame()
     * @param gameToBeCreated - The game object to create a record for
     * @return - a boolean to determine success.
     */
    public boolean CreateGame(Game gameToBeCreated)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject gameData = new JSONObject();
        String responseString;

        try
        {
            /* Build JSON object for posting. */
            gameData.put(Constants.GAME_ID, gameToBeCreated.id);
            gameData.put(Constants.GAME_PLAYER1, gameToBeCreated.player1.id);
            gameData.put(Constants.GAME_STATE, gameToBeCreated.state.toString());
        }
        catch (JSONException e)
        {
            return false;
        }

        try
        {
            /* Make the request and get a response */
            HttpPost request = new HttpPost(this.createGameUri);
            StringEntity jsonString = new StringEntity(gameData.toString());

            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
        }
        catch (Exception ex)
        {
            return false;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        if (responseString.equals("true"))
        {
            return true; // Response from server.
        }
        else
        {
            return false;
        }
    }

    /*************************************************
     * UpdateGame()
     * @param currentGame - The game object to update.
     * @return - boolean to determine success.
     */
    public boolean UpdateGame(Game currentGame)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject gameData = new JSONObject();
        String responseString;

        try
        {
            // Build JSON.
            gameData.put(Constants.GAME_ID, currentGame.id);
            gameData.put(Constants.GAME_PLAYER1, currentGame.player1.id);
            gameData.put(Constants.GAME_PLAYER2, currentGame.player2.id);
            gameData.put(Constants.GAME_STATE, currentGame.state.toString());

            // If the game has started, then we add the scores.
            if (currentGame.state == GameState.STARTED)
            {
                gameData.put(Constants.GAME_SCOREP1, currentGame.player1Score);
                gameData.put(Constants.GAME_SCOREP2, currentGame.player2Score);
            }

            // If we have a winner, add that to the JSON to.
            if (currentGame.winner != null)
            {
                gameData.put(Constants.GAME_WINNER, currentGame.winner.id);
            }
        }
        catch (JSONException e)
        {
            return false;
        }

        try
        {
            /* Post the request, get a response. */
            HttpPost request = new HttpPost(this.updateGameUri);
            StringEntity jsonString = new StringEntity(gameData.toString());

            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
        }
        catch (Exception ex)
        {
            return false;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        if (responseString.equals("true"))
        {
            return true; //Success!
        }
        else
        {
            return false;
        }
    }

    /**********************************************************
     * FetchGames()
     * @param playerId - The ID of the player to get games for
     * @return - Array of JSON objects containing game data.
     */
    public JSONArray FetchGames(UUID playerId)
    {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(gamesListUri);
        HttpResponse response;
        JSONArray gameData;
        String responseString;
        JSONObject playerData = new JSONObject();

        try
        {
            playerData.put(Constants.PLAYER_ID, playerId);
        }
        catch (JSONException e)
        {
            return null;
        }

        try
        {
            /* Post our request and parse the response into a JSON array. */
            StringEntity jsonString = new StringEntity(playerData.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
            gameData = new JSONArray(responseString);
        }
        catch (Exception ex)
        {
            return null;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        return gameData;
    }

    /******************************************************************************
     * Register()
     * @param playerToRegister - The player object to create a record for in the DB
     * @return - success flag
     */
    public boolean Register(Player playerToRegister)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject registerData = new JSONObject();
        String responseString;

        try
        {
            /* Build our JSON object */
            registerData.put("id", playerToRegister.id);
            registerData.put("username", playerToRegister.username);
            registerData.put("password", playerToRegister.password);
            registerData.put("name", playerToRegister.name);
            registerData.put("twitter", playerToRegister.twitter);

        }
        catch (JSONException e)
        {
            return false;
        }

        try
        {
            /* Post it off, get response */
            HttpPost request = new HttpPost(this.registerUri);
            StringEntity jsonString = new StringEntity(registerData.toString());

            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
        }
        catch (Exception ex)
        {
            return false;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        if (responseString.equals("true"))
        {
            return true; // User registered!
        }
        else
        {
            return false;
        }
    }

    /*************************************
     * GetGame()
     * @param id - The ID of the game to fetch
     * @return - A JSONObject containing the game information.
     */
    public JSONObject GetGame(String id)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject gameIdData = new JSONObject();
        String responseString;
        JSONArray gameDataArray;
        JSONObject gameData;

        try
        {
            gameIdData.put(Constants.GAME_ID, id);
        }
        catch (JSONException e)
        {
            System.out.print(e);
        }

        try
        {
            /* Build the POST request, and get the response. */
            HttpPost request = new HttpPost(this.getGameUri);
            StringEntity jsonString = new StringEntity(gameIdData.toString());

            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
            gameDataArray = new JSONArray(responseString);

            /* If the array contains a single JSON object, fetch it and return it */
            if (gameDataArray.length() == 1)
            {
                gameData = gameDataArray.getJSONObject(0);
                return gameData;
            }
        }
        catch (Exception ex)
        {
            System.out.print(ex);
            return null;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        return null;
    }

    /********************************************
     * GetPlayer()
     * @param id - ID of the player to get.
     * @return - Return a populated player object.
     */
    public Player GetPlayer(String id)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject playerIdData = new JSONObject();
        String responseString;
        JSONArray playerData;

        try
        {
            playerIdData.put(Constants.PLAYER_ID, id);
        }
        catch (JSONException ex)
        {
            System.out.print(ex);
        }

        try
        {
            /* Build Post request */
            HttpPost request = new HttpPost(this.getPlayerUri);
            StringEntity jsonString = new StringEntity(playerIdData.toString());

            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
            playerData = new JSONArray(responseString);
        }
        catch (Exception ex)
        {
            return null;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        /* If the JSON array returned from the server has one object, fetch the contents and return
        a player object
         */
        if (playerData.length() == 1)
        {
            try
            {
                JSONObject playerJsonData = playerData.getJSONObject(0);

                UUID playerId = UUID.fromString(playerJsonData.getString(Constants.PLAYER_ID));
                String playerUsername = playerJsonData.getString(Constants.PLAYER_USERNAME);
                String playerPassword = playerJsonData.getString(Constants.PLAYER_PASSWORD);
                String playerName = playerJsonData.getString(Constants.PLAYER_NAME);
                String playerTwitter = playerJsonData.getString(Constants.PLAYER_TWITTER);

                return new Player(playerId, playerUsername, playerPassword, playerName, playerTwitter);
            }
            catch (JSONException e)
            {
                return null;
            }
        }
        else
        {
            return null;
        }

    }

    /*******************************************************
     * Login()
     * @param username - The username to send to the server to get a player.
     * @param password - The users password to authenticate with the sever.
     * @return - JSONObject containing player information.
     */
    public JSONObject Login(String username, String password)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject loginData = new JSONObject();
        String responseString;
        JSONArray userData;

        try
        {
            loginData.put("username", username);
            loginData.put("password", password);
        }
        catch (JSONException e)
        {
            System.out.print(e);
            return null;
        }

        try
        {
            HttpPost request = new HttpPost(this.loginUri);
            StringEntity jsonString = new StringEntity(loginData.toString());

            request.addHeader("content-type", "application/json");
            request.setEntity(jsonString);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseString = EntityUtils.toString(responseEntity);
            userData = new JSONArray(responseString);
        }
        catch (Exception ex)
        {
            return null;
        }
        finally
        {
            client.getConnectionManager().shutdown();
        }

        if (userData.length() == 1)
        {
            try
            {
                return userData.getJSONObject(0);
            }
            catch (JSONException e)
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
