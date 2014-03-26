package com.airhockeylive.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Created by Joe on 25/02/14.
 */
public class Client
{
    private URI registerUri;

    private URI loginUri;

    private URI gamesListUri;

    private URI getPlayerUri;

    private String defaultHost = "http://192.168.0.2";

    private int defaultPort = 3000;

    private String registerQuery = "/register";

    private String loginQuery = "/login";

    private String getGamesQuery = "/gameslist";

    private String getPlayerQuery = "/getplayer";

    public Client(String host, int port)
    {
        registerUri = URI.create(String.format("%s:%d%s", host, port, registerQuery));
        loginUri = URI.create(String.format("%s:%d%s", host, port, loginQuery));
        getPlayerUri = URI.create(String.format("%s:%d%s", host, port, getPlayerQuery));
        gamesListUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, getGamesQuery));
    }

    public Client()
    {
        registerUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, registerQuery));
        loginUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, loginQuery));
        getPlayerUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, getPlayerQuery));
        gamesListUri = URI.create(String.format("%s:%d%s", defaultHost, defaultPort, getGamesQuery));
    }

    public JSONArray FetchGames()
    {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(gamesListUri);
        HttpResponse response;
        JSONArray gameData;
        String responseString;

        try
        {
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

    public boolean Register(Player playerToRegister)
    {
        HttpClient client = new DefaultHttpClient();
        JSONObject registerData = new JSONObject();
        String responseString;

        try
        {
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
            return true;
        }
        else
        {
            return false;
        }
    }

    //Fetch a player from the database from a given ID
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
            //
        }

        try
        {
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
