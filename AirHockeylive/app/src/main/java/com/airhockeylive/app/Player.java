/****************************************************************************
 * Player.java
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

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
