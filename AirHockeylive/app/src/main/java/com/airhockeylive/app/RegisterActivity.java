/****************************************************************************
 * RegisterActivity.java
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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends Activity
{
    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mRegisterTask = null;

    // Values for email and password at the time of the login attempt.
    private String username;
    private String password;
    private String confirmPassword;
    private String name;
    private String twitter;

    // UI references.
    private EditText usernameView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private EditText nameView;
    private EditText twitterView;
    private View registerFormView;
    private View registerStatusView;
    private TextView registerStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        // Set up the register form.
        usernameView = (EditText) findViewById(R.id.username);

        passwordView = (EditText) findViewById(R.id.password);

        confirmPasswordView = (EditText) findViewById(R.id.confirmPassword);

        nameView = (EditText) findViewById(R.id.name);

        twitterView = (EditText) findViewById(R.id.twitter);

        registerFormView = findViewById(R.id.register_form);
        registerStatusView = findViewById(R.id.register_status);
        registerStatusMessageView = (TextView) findViewById(R.id.register_status_message);

        findViewById(R.id.register_user_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptRegister();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_go_login:
                Intent goLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                RegisterActivity.this.startActivity(goLogin);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    public void attemptRegister()
    {
        if (mRegisterTask != null)
        {
            return;
        }

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        username = usernameView.getText().toString().trim();
        password = passwordView.getText().toString().trim();
        confirmPassword = confirmPasswordView.getText().toString().trim();
        name = nameView.getText().toString().trim();
        twitter = twitterView.getText().toString().trim();

        if (!twitter.startsWith("@"))
        {
            twitter = "@" + twitter;
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password))
        {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }
        else if (password.length() < 4)
        {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        //Check passwords match
        if (!password.equals(confirmPassword))
        {
            confirmPasswordView.setError(getString(R.string.error_password_mismatch));
            focusView = confirmPasswordView;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username))
        {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            registerStatusMessageView.setText(R.string.register_progress);
            showProgress(true);
            mRegisterTask = new UserRegisterTask();
            mRegisterTask.execute((Void) null);
        }
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

            registerStatusView.setVisibility(View.VISIBLE);
            registerStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            registerStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            registerFormView.setVisibility(View.VISIBLE);
            registerFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            registerStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            boolean registered = false;

            try
            {
                Player playerToRegister = new Player(null, username, password, name, twitter);
                Client client = new Client();
                registered = client.Register(playerToRegister);
            }
            catch (Exception e)
            {
                return false;
            }

            return registered;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mRegisterTask = null;
            showProgress(false);

            if (success)
            {
                Intent goToLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                RegisterActivity.this.startActivity(goToLogin);
                finish();
            }
            else
            {
                passwordView.setError(getString(R.string.error_registering));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled()
        {
            mRegisterTask = null;
            showProgress(false);
        }
    }
}
