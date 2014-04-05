/****************************************************************************
 * GameActivity.java
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends ActionBarActivity
{
    private Player loggedInPlayer;
    private Game currentGame;
    private View gameView;
    private TextView player1;
    private TextView player2;
    private Button readyButton;
    private TextView player1Score;
    private TextView player2Score;
    private TextView winnerMsg;

    private TextView gameStatusMessageView;
    private View gameStatusView;

    private GetGameTask mGetGameTask = null;
    private UpdateGameTask mUpdateGameTask = null;
    private StartGameTask mStartGameTask = null;
    private GetScoreTask mGetScoreTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loggedInPlayer = (Player) getIntent().getSerializableExtra(Constants.PLAYER_OBJECT);
        currentGame = (Game) getIntent().getSerializableExtra(Constants.GAME_OBJECT);

        gameView = findViewById(R.id.game);
        player1 = (TextView) findViewById(R.id.player1);
        player2 = (TextView) findViewById(R.id.player2);
        readyButton = (Button) findViewById(R.id.readyBtn);
        player1Score = (TextView) findViewById(R.id.player1Score);
        player2Score = (TextView) findViewById(R.id.player2Score);
        winnerMsg = (TextView) findViewById(R.id.winnerTextView);
        winnerMsg.setVisibility(winnerMsg.GONE);

        gameStatusMessageView = (TextView) findViewById(R.id.game_status_message);
        gameStatusView = findViewById(R.id.game_status);

        displayGame();

        readyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentGame.state = GameState.STARTED;
                currentGame.player1Score = 0;
                currentGame.player2Score = 0;

                gameStatusMessageView.setText(R.string.start_game_progress);
                showProgress(true);
                mStartGameTask = new StartGameTask();
                mStartGameTask.execute((Void) null);
            }
        });
    }

    public void getScore()
    {
        mGetScoreTask = new GetScoreTask();
        mGetScoreTask.execute((Void) null);
    }

    public void updateScoreUI()
    {
        player1Score.setText(String.valueOf(currentGame.player1Score));
        player2Score.setText(String.valueOf(currentGame.player2Score));

        if (currentGame.player1Score < Constants.MAX_SCORE && currentGame.player2Score < Constants.MAX_SCORE)
        {
            getScore();
        }
        else if (currentGame.player1Score == Constants.MAX_SCORE)
        {
            currentGame.winner = currentGame.player1;
            String message = "The winner is: " + currentGame.player1.username + "!";
            winnerMsg.setText(message);
            winnerMsg.setVisibility(winnerMsg.VISIBLE);
        }
        else if (currentGame.player2Score == Constants.MAX_SCORE)
        {
            currentGame.winner = currentGame.player2;
            String message = "The winner is: " + currentGame.player2.username + "!";
            winnerMsg.setText(message);
            winnerMsg.setVisibility(winnerMsg.VISIBLE);
        }
    }

    public void displayGame()
    {
        if (currentGame.player1.id.compareTo(loggedInPlayer.id) == 0)
        {
            player1.setText(loggedInPlayer.username);

            if (currentGame.player2 == null)
            {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                gameStatusMessageView.setText(R.string.search_players_progress);
                showProgress(true);
                mGetGameTask = new GetGameTask();
                mGetGameTask.execute((Void) null);
            }
            else if (currentGame.state != GameState.STARTED)
            {
                player2.setText(currentGame.player2.username);
                readyButton.setVisibility(readyButton.VISIBLE);
            }
        }
        else
        {
            if (currentGame.player2 == null)
            {
                currentGame.player2 = loggedInPlayer;

                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                gameStatusMessageView.setText(R.string.update_game_progress);
                showProgress(true);
                mUpdateGameTask = new UpdateGameTask();
                mUpdateGameTask.execute((Void) null);
            }

            player1.setText(currentGame.player1.username);
            player2.setText(currentGame.player2.username);
        }

        if (currentGame.state == GameState.OPEN)
        {
            player1Score.setVisibility(player1Score.GONE);
            player2Score.setVisibility(player2Score.GONE);
        }
        else
        {
            player1Score.setText(String.valueOf(currentGame.player1Score));
            player2Score.setText(String.valueOf(currentGame.player2Score));
            player1Score.setVisibility(player1Score.VISIBLE);
            player2Score.setVisibility(player2Score.VISIBLE);
            readyButton.setVisibility(readyButton.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            gameStatusView.setVisibility(View.VISIBLE);
            gameStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            gameStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            gameView.setVisibility(View.VISIBLE);
            gameView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            gameView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            gameStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            gameView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class GetGameTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                while (currentGame.player2 == null || currentGame.state == GameState.OPEN)
                {
                    currentGame = Game.GetGame(currentGame.id);
                }
            }
            catch (Exception e)
            {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mGetGameTask = null;
            showProgress(false);

            if (success)
            {
                displayGame();
                getScore();
            }
            else
            {
            }
        }

        @Override
        protected void onCancelled()
        {
            mGetGameTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UpdateGameTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                Client client = new Client();
                client.UpdateGame(currentGame);
            }
            catch (Exception e)
            {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mUpdateGameTask = null;
            showProgress(false);

            if (success)
            {
                displayGame();
            }
            else
            {
            }
        }

        @Override
        protected void onCancelled()
        {
            mUpdateGameTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class StartGameTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                Client client = new Client();
                client.StartGame(currentGame);
            }
            catch (Exception e)
            {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mUpdateGameTask = null;
            showProgress(false);

            if (success)
            {
                displayGame();
                getScore();
            }
            else
            {
            }
        }

        @Override
        protected void onCancelled()
        {
            mUpdateGameTask = null;
            showProgress(false);
        }
    }

    public class GetScoreTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                currentGame = Game.GetGame(currentGame.id);
            }
            catch (Exception e)
            {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mGetGameTask = null;

            if (success)
            {
                updateScoreUI();
            }
            else
            {
            }
        }

        @Override
        protected void onCancelled()
        {
            mGetGameTask = null;
            showProgress(false);
        }
    }
}