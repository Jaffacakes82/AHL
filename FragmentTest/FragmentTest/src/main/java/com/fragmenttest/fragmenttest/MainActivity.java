package com.fragmenttest.fragmenttest;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration config = getResources().getConfiguration();
        android.support.v4.app.FragmentManager fragz = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction tranz = fragz.beginTransaction();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            LM_Fragment landscapeFragment = new LM_Fragment();
            tranz.replace(android.R.id.content, landscapeFragment);
        }
        else
        {
            PM_Fragment portraitFragment = new PM_Fragment();
            tranz.replace(android.R.id.content, portraitFragment);
        }

        tranz.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
