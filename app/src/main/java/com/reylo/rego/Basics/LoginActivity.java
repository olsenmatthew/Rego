package com.reylo.rego.Basics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.reylo.rego.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    //declare ui elements
    private EditText loginActivityEmailEditText;
    private EditText loginActivityPasswordEditText;
    private Button loginActivityLoginButton;
    private TextView loginActivitySignUpHereTextView;
    private ProgressDialog progressDialog;

    // declare Firebase authentication objects
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // connect ui elements corresponding to components in activity_login layout
        loginActivityEmailEditText = (EditText) findViewById(R.id.loginActivityEmailEditText);
        loginActivityPasswordEditText = (EditText) findViewById(R.id.loginActivityPasswordEditText);
        loginActivityLoginButton = (Button) findViewById(R.id.loginActivityLoginButton);
        loginActivitySignUpHereTextView = (TextView) findViewById(R.id.loginActivitySignUpHereTextView);

        // create new progress dialog for when login info is being processed by database
        progressDialog = new ProgressDialog(LoginActivity.this);

        // set on click listener for login button (see onClick function below)
        loginActivityLoginButton.setOnClickListener(this);

        // set on click listener for login button (see onClick function below)
        loginActivitySignUpHereTextView.setOnClickListener(this);

        // get firebase login details
        firebaseAuth = FirebaseAuth.getInstance();


        if (firebaseAuth.getCurrentUser() != null) {

            finish();
            startActivity(new Intent(getApplicationContext(), UserFirstName.class));

        }

    }

    private void userLogin() {

        //get email and password from components
        String email = loginActivityEmailEditText.getText().toString().trim();
        String password = loginActivityPasswordEditText.getText().toString().trim();

        // if email / password is empty, alert user with toast
        // else attempt login with firebase auth object
        if (TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_LONG).show();
            return;

        } else if (TextUtils.isEmpty(password) && !TextUtils.isEmpty(email)) {

            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_LONG).show();
            return;

        } else if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please Enter Your Email & Password", Toast.LENGTH_LONG).show();
            return;

        } else {

            // show progress dialog while firebase authenticates user
            progressDialog.setMessage("Logging into Account");
            progressDialog.show();

            // authenticating user with email and password
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();

                    // if successful, proceed to UserFirstName activity
                    if(task.isSuccessful()) {

                        finish();
                        startActivity(new Intent(getApplicationContext(), UserFirstName.class));
                        Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_LONG).show();

                    } else {

                        //login failed, let user know
                        Toast.makeText(LoginActivity.this, "Could not login. Please try again", Toast.LENGTH_LONG).show();

                    }

                }

            });

        }

    }

    @Override
    public void onClick(View view) {

        // accept login details and continue login process
        if (view == loginActivityLoginButton && view != loginActivitySignUpHereTextView){

            userLogin();

        }

        // user wants to create a new account, go to UserRegistration
        if (view == loginActivitySignUpHereTextView && view != loginActivityLoginButton) {

            startActivity(new Intent(this, UserRegistration.class));

        }

    }

}
