package com.airhockeylive.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
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

public class GameListActivity extends Activity
{
    private List<Game> openGames;
    private GameListTask mGameListTask = null;
    private View gamesListStatusView;
    private View gamesListView;
    private TextView gamesListStatusMessageView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        gamesListView = findViewById(R.id.gamesTable);
        gamesListStatusView = findViewById(R.id.gamelist_status);
        gamesListStatusMessageView = (TextView) findViewById(R.id.gamelist_status_message);

        getListOfGames();
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

    public void getListOfGames()
    {
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

        if (openGames.size() > 0)
        {
            setContentView(R.layout.activity_gamelist);

            TableLayout mainTable = (TableLayout)findViewById(R.id.gamesTable);

            for (int i = 0; i < openGames.size(); i++)
            {
                TableRow row = new TableRow(this);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT);
                rowParams.setMargins(20,20,20,20);
                row.setLayoutParams(rowParams);

                TextView ownerView = new TextView(this);
                ownerView.setText(openGames.get(i).player1.username);

                TextView statusView = new TextView(this);
                statusView.setText(openGames.get(i).state.toString());

                Button playButton = new Button(this);
                playButton.setText("Play");

                row.addView(ownerView);
                row.addView(statusView);
                row.addView(playButton);

                mainTable.addView(row);
            }
        }
        else
        {
            setContentView(R.layout.nogames);
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
                finish();
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
