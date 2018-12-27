package com.reylo.rego.Basics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.reylo.rego.R;

public class UserRegistration extends AppCompatActivity implements View.OnClickListener {

    // declare ui components
    private EditText userRegistrationEmailEditText;
    private EditText userRegistrationPasswordEditText;
    private Button userRegistrationCreateAccountButton;
    private TextView userRegistrationAlreadyRegisteredTextView;
    private ProgressDialog progressDialog;

    // declare firebase auth and user objects
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    // declare database references
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        userRegistrationEmailEditText = (EditText) findViewById(R.id.userRegistrationEmailEditText);
        userRegistrationPasswordEditText = (EditText) findViewById(R.id.userRegistrationPasswordEditText);
        userRegistrationCreateAccountButton = (Button) findViewById(R.id.userRegistrationCreateAccountButton);
        userRegistrationAlreadyRegisteredTextView = (TextView) findViewById(R.id.userRegistrationAlreadyRegisteredTextView);

        progressDialog = new ProgressDialog(UserRegistration.this);

        userRegistrationCreateAccountButton.setOnClickListener(this);

        userRegistrationAlreadyRegisteredTextView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            finish();
            startActivity(new Intent(getApplicationContext(), UserFirstName.class));

        }

    }


    private void registerUser() {

        final String email = userRegistrationEmailEditText.getText().toString().trim();
        final String password = userRegistrationPasswordEditText.getText().toString().trim();

        // if text fields are not filled in, alert user
        // if text fields are filled in, attempt to register user
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

            //display progress dialog while trying to create new account
            progressDialog.setMessage("Creating Account");
            progressDialog.show();

            // try to create new user with email and password input
            // on success, finish activity and move to next activity
            // else alert user that registration failed
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            progressDialog.dismiss();
                            if (task.isSuccessful()) {

                                finish();
                                startActivity(new Intent(getApplicationContext(), UserFirstName.class));

                                Toast.makeText(UserRegistration.this, "Registered Successfully", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(UserRegistration.this, "Could not register user. Please try again", Toast.LENGTH_LONG).show();

                            }

                        }
                    });

        }

    }


    @Override
    public void onClick(View view) {

        // use details to register user
        if (view == userRegistrationCreateAccountButton && view != userRegistrationAlreadyRegisteredTextView) {

            registerUser();

        }

        // change activities to login user instead
        if (view == userRegistrationAlreadyRegisteredTextView && view != userRegistrationCreateAccountButton) {

            startActivity(new Intent(this, LoginActivity.class));

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        PlayServicesUtils.checkGooglePlaySevices(this);

    }

}
