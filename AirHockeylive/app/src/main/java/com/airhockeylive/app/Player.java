package com.airhockeylive.app;

import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Joe on 26/02/14.
 */
public class Player implements Serializable
{
    public UUID id;

    public String username;

    public String password;

    public String name;

    public String twitter;

    public Player(UUID id, String username, String password, String name, String twitter)
    {
        if (id == null)
        {
            this.id = UUID.randomUUID();
        }
        else
        {
            this.id = id;
        }

        this.username = username;
        this.password = password;
        this.name = name;
        this.twitter = twitter;
    }

    public static Player Login(String username, String password)
    {
        Client client = new Client();

        JSONObject playerData = client.Login(username, password);

        if (playerData != null)
        {
            UUID id;
            String playerUsername;
            String playerPassword;
            String playerName;
            String playerTwitter;

            try
            {
                id = UUID.fromString(playerData.getString(Constants.PLAYER_ID));
                playerUsername = playerData.getString(Constants.PLAYER_USERNAME);
                playerPassword = playerData.getString(Constants.PLAYER_PASSWORD);
                playerName = playerData.getString(Constants.PLAYER_NAME);
                playerTwitter = playerData.getString(Constants.PLAYER_TWITTER);
            }
            catch (JSONException e)
            {
                return null;
            }

            return new Player(id, playerUsername, playerPassword, playerName, playerTwitter);
        }
        else
        {
            return null;
        }
    }
}
