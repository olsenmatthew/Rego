package com.reylo.rego.Basics;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.reylo.rego.R;

public class UserFirstName extends AppCompatActivity implements View.OnClickListener{

    // declare ui components for corresponding layout
    private EditText userFirstNameActivityEditText;
    private ImageView userFirstNameActivityNextArrow;

    // declare firebase authentication object
    private FirebaseAuth firebaseAuth;

    // declare database references
    private DatabaseReference databaseReference;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get authentication instance and create user object
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // if user is null, finish activity and return to login
        if(user == null) {

            finish();
            startActivity(new Intent(this, LoginActivity.class));

        }

        // init db reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // listen for value of user's first name
        // if first name exists, finish activity and go to user's birthday activity
        Query query = databaseReference.child("Users").child(user.getUid()).child("First Name");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    finish();
                    startActivity(new Intent(UserFirstName.this, UserBirthday.class));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setContentView(R.layout.activity_user_first_name);

        // connect ui components to corresponding components on activity's layout
        userFirstNameActivityEditText = (EditText) findViewById(R.id.userFirstNameActivityEditText);
        userFirstNameActivityNextArrow = (ImageView) findViewById(R.id.userFirstNameActivityNextArrow);

        // listen for clicks on next arrow
        userFirstNameActivityNextArrow.setOnClickListener(this);

    }

    // if first name is valid, save first name to database, otherwise ask user to enter name
    private void saveUserFirstName() {

        String firstName = userFirstNameActivityEditText.getText().toString().trim();

        if(databaseReference.child("Users").child(user.getUid()).child("First Name") == null) {

            Toast.makeText(this, "Please Enter Your First Name", Toast.LENGTH_LONG).show();

        } else if (firstName.length() > 1 && (databaseReference.child("Users").child(user.getUid()).child("First Name") != null)){

            UserFirstNameInfo userFirstNameInfo = new UserFirstNameInfo(firstName);

            FirebaseUser user = firebaseAuth.getCurrentUser();

            databaseReference.child("Users").child(user.getUid()).child("First Name").setValue(userFirstNameInfo);

            Toast.makeText(this, "First Name Saved", Toast.LENGTH_LONG).show();

        }

    }


    @Override
    public void onClick(View view) {

        // if invalid name, ask user to enter first name
        // on user clicking to next activity, save name and finish activity if valid name
        if (view == userFirstNameActivityNextArrow) {

            if (userFirstNameActivityEditText.getText().toString().length() < 1 && (databaseReference.child("Users").child(user.getUid()).child("First Name") == null)) {

                Toast.makeText(this, "Please Enter Your First Name", Toast.LENGTH_LONG).show();

            } else {

                saveUserFirstName();
                finish();

            }

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode== KeyEvent.KEYCODE_BACK) {

            logOut();

        }
        return super.onKeyDown(keyCode, event);
    }

    //log out the user and return to login activity
    private void logOut(){

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

}
