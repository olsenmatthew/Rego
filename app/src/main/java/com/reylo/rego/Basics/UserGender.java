package com.reylo.rego.Basics;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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

public class UserGender extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    //declare ui components corresponding with activity's layout
    private Button userGenderActivityFemaleButton;
    private Button userGenderActivityMaleButton;
    private TextView userGenderMoreOptionsTextView;
    private ImageView userGenderActivityNextArrow;
    private FrameLayout user_gender_activity_container;
    private FrameLayout fragment_container;

    //declare firebase authentication and user objects
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    //declare database reference
    private DatabaseReference databaseReference;

    // set gender default to empty string
    public String gender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_gender);

        //get authentication instance and create user object
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // if user is null, finish activity and return to login
        if(user == null) {

            finish();
            startActivity(new Intent(this, LoginActivity.class));

        }

        // connect ui components
        user_gender_activity_container = (FrameLayout) findViewById(R.id.user_gender_activity_container);
        fragment_container = (FrameLayout) findViewById(R.id.fragment_container);
        user_gender_activity_container.setVisibility(View.VISIBLE);
        fragment_container.setVisibility(View.GONE);
        userGenderActivityFemaleButton = (Button) findViewById(R.id.userGenderActivityFemaleButton);
        userGenderActivityMaleButton = (Button) findViewById(R.id.userGenderActivityMaleButton);
        userGenderMoreOptionsTextView = (TextView) findViewById(R.id.userGenderMoreOptionsTextView);
        userGenderActivityNextArrow = (ImageView) findViewById(R.id.userGenderActivityNextArrow);

        //get firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // listen for valid gender in database
        // valid gender, finish activity and move onto next activity
        Query query = databaseReference.child("Users").child(user.getUid()).child("Gender");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    finish();
                    startActivity(new Intent(UserGender.this, UserProfilePicture.class));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // set backgrounds for buttons
        userGenderActivityFemaleButton.setBackgroundResource(R.drawable.button_rounder);
        userGenderActivityMaleButton.setBackgroundResource(R.drawable.button_rounder);

        // set click listeners for buttons and text views
        userGenderActivityNextArrow.setOnClickListener(this);
        userGenderActivityFemaleButton.setOnClickListener(this);
        userGenderActivityMaleButton.setOnClickListener(this);
        userGenderMoreOptionsTextView.setOnClickListener(this);

    }



    private void saveUserGender() {

        if (gender.length() < 1 && (databaseReference.child("Users").child(user.getUid()).child("Gender") == null)) {

            Toast.makeText(this, "Please Select Your Gender", Toast.LENGTH_LONG).show();

        } else if (gender.length() > 1 || gender.contentEquals("Female")
                || gender.contentEquals("Male")){

            final UserGenderInfo userGenderInfo = new UserGenderInfo(gender);

            FirebaseUser user = firebaseAuth.getCurrentUser();

            databaseReference.child("Users").child(user.getUid()).child("Gender").setValue(userGenderInfo);

        }

    }

    @Override
    public void onClick(View view) {

        // set gender content equal to female and change button style
        if (view == userGenderActivityFemaleButton) {

            gender = "Female";
            if (gender.contentEquals("Female")) {

                userGenderActivityMaleButton.setBackgroundResource(R.drawable.button_rounder);
                userGenderActivityMaleButton.setTextColor(Color.parseColor("#c17606"));
                userGenderActivityFemaleButton.setBackgroundResource(R.drawable.button_rounder_clicked_female);
                userGenderActivityFemaleButton.setTextColor(Color.parseColor("#d472bc"));

                Toast.makeText(this, "Female Saved", Toast.LENGTH_LONG).show();
            }

        }

        // set gender content equal to male and change button style
        if (view == userGenderActivityMaleButton) {

            gender = "Male";
            if (gender.contentEquals("Male")) {

                userGenderActivityFemaleButton.setBackgroundResource(R.drawable.button_rounder);
                userGenderActivityFemaleButton.setTextColor(Color.parseColor("#c17606"));
                userGenderActivityMaleButton.setBackgroundResource(R.drawable.button_rounder_clicked_male);
                userGenderActivityMaleButton.setTextColor(Color.parseColor("#72bcd4"));


                Toast.makeText(this, "Male Saved", Toast.LENGTH_LONG).show();

            }

        }

        // bring up more genders view
        if (view == userGenderMoreOptionsTextView) {

            user_gender_activity_container.setVisibility(View.GONE);
            fragment_container.setVisibility(View.VISIBLE);

        }

        // if not valid, ask user to select gender with toast message
        // save gender if valid and move onto next activity
        if (view == userGenderActivityNextArrow) {

            if (gender.length() < 1 && (databaseReference.child("Users").child(user.getUid()).child("Gender") == null)) {

                Toast.makeText(this, "Please Select Your Gender", Toast.LENGTH_LONG).show();

            } else {

                saveUserGender();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
