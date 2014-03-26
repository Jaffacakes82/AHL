package com.airhockeylive.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GameManagementActivity extends Activity
{

    private Player loggedInPlayer;
    private List<Game> openGames;
    private GameListTask mGameListTask = null;
    private View gamesListStatusView;
    private View gamesListView;
    private View tableHeadView;
    private View noGamesView;
    private TextView gamesListStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamemanagement);

        loggedInPlayer = (Player) getIntent().getSerializableExtra(Constants.PLAYER_OBJECT);

        gamesListView = findViewById(R.id.gamesTable);
        gamesListStatusView = findViewById(R.id.gamelist_status);
        noGamesView = findViewById(R.id.nogames);
        tableHeadView = findViewById(R.id.gamesTableHead);
        gamesListStatusMessageView = (TextView) findViewById(R.id.gamelist_status_message);

        noGamesView.setVisibility(noGamesView.GONE);

        if (mGameListTask != null)
        {
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        gamesListStatusMessageView.setText(R.string.games_list_progress);
        showProgress(true);
        mGameListTask = new GameListTask();
        mGameListTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_list, menu);
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

            gamesListStatusView.setVisibility(View.VISIBLE);
            gamesListStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            gamesListStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            gamesListView.setVisibility(View.VISIBLE);
            gamesListView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            gamesListView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            gamesListStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            gamesListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    View.OnClickListener getOnClickPlayGame(final Button button)
    {
        return new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int gameIndex = v.getId();

                Intent goToGame = new Intent(GameManagementActivity.this, GameActivity.class);
                goToGame.putExtra(Constants.GAME_OBJECT, openGames.get(gameIndex));
                goToGame.putExtra(Constants.PLAYER_OBJECT, loggedInPlayer);
                startActivity(goToGame);
            }
        };
    }

    public void displayGames()
    {
        TableLayout mainTable = (TableLayout) gamesListView;

        if (openGames.size() > 0)
        {
            for (int i = 0; i < openGames.size(); i++)
            {
                TableRow row = new TableRow(this);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT);

                TextView gameId = new TextView(this);
                gameId.setText(openGames.get(i).id.toString());
                gameId.setVisibility(gameId.GONE);

                TextView ownerView = new TextView(this);
                ownerView.setText(openGames.get(i).player1.username);
                ownerView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                TextView statusView = new TextView(this);
                statusView.setText(openGames.get(i).state.toString());
                statusView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                Button playButton = new Button(this);

                if (openGames.get(i).player1.id.compareTo(loggedInPlayer.id) == 0)
                {
                    playButton.setText("View");
                }
                else
                {
                    playButton.setText("Play");
                }
                playButton.setId(i);
                playButton.setOnClickListener(getOnClickPlayGame(playButton));
                playButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                row.addView(gameId);
                row.addView(ownerView);
                row.addView(statusView);
                row.addView(playButton);

                mainTable.addView(row);
            }
        }
        else
        {
            noGamesView.setVisibility(noGamesView.VISIBLE);
            tableHeadView.setVisibility(tableHeadView.GONE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class GameListTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                openGames = new ArrayList<Game>();
                openGames = Game.FetchOpenGames();
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
            mGameListTask = null;
            showProgress(false);

            if (success)
            {
                displayGames();
            }
            else
            {
            }
        }

        @Override
        protected void onCancelled()
        {
            mGameListTask = null;
            showProgress(false);
        }
    }
}
