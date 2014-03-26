package com.airhockeylive.app;

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
    private TextView versus;
    private Button readyButton;
    private TextView player1Score;
    private TextView player2Score;

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
        versus = (TextView) findViewById(R.id.versusText);
        readyButton = (Button) findViewById(R.id.readyBtn);
        player1Score = (TextView) findViewById(R.id.player1Score);
        player2Score = (TextView) findViewById(R.id.player2Score);

        if (currentGame.state == GameState.OPEN)
        {
            player1Score.setVisibility(player1Score.GONE);
            player2Score.setVisibility(player2Score.GONE);
        }

        if (currentGame.player1.id.compareTo(loggedInPlayer.id) == 0)
        {
            player1.setText(loggedInPlayer.username);

            if (currentGame.player2 == null)
            {
                player2.setText("???");
                readyButton.setVisibility(readyButton.GONE);
            }
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

}
