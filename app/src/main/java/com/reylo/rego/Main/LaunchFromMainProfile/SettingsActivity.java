package com.reylo.rego.Main.LaunchFromMainProfile;

/*

This activity is unfinished
new features will soon be added to complete the full functionality

 */

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reylo.rego.Basics.LoginActivity;
import com.reylo.rego.Basics.UserRegistration;
import com.reylo.rego.Main.ActivityTabbedMain;
import com.reylo.rego.R;

import java.util.HashMap;
import java.util.Map;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class SettingsActivity extends AppCompatActivity {


    // declare firebase objects
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // declare db references
    private DatabaseReference mUserDatabase;

    // declare ui components
    // contains gender interest options
//    private RadioRealButtonGroup mRadioGroup;
    //logout and delete account buttons
    private Button mLogOutButton;
    private Button mDeleteAccountButton;

    private String interest = "Male", userId;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_settings);

        //mRadioGroup = findViewById(R.id.radioRealButtonGroup);
        mLogOutButton = findViewById(R.id.edit_settings_log_out);
        mDeleteAccountButton = findViewById(R.id.edit_settings_delete_account);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        //getUserInfo();

        // on click, log out user and return to login activity
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        // on click, delete the user account and go to register activity
        mDeleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount();
            }
        });

    }

//   private void getUserInfo() {
//
//        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//
//                    if(dataSnapshot.child("interest").getValue()!=null) {
//
//                        interest = dataSnapshot.child("interest").getValue().toString();
//
//                    }
//
//                    if(interest.equals("Male")) {
//
//                        mRadioGroup.setPosition(0);
//
//                    } else if(interest.equals("Female")) {
//
//                        mRadioGroup.setPosition(1);
//
//                    } else {
//
//                        mRadioGroup.setPosition(2);
//
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//    }
//
//    private void saveUserInformation() {
//
//        switch(mRadioGroup.getPosition()) {
//            case 0:
//                interest = "Male";
//                break;
//            case 1:
//                interest = "Female";
//                break;
//            case 2:
//                interest = "Both";
//                break;
//        }
//
//        Map userInfo = new HashMap();
//        userInfo.put("interest", interest);
//        mUserDatabase.updateChildren(userInfo);
//
//    }

    //log out the user and return to login activity
    private void logOut(){

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

    //delete the user and return to the create account activity
    private void deleteAccount() {

        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()) {

                    Toast.makeText(SettingsActivity.this, "Account Deleted", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SettingsActivity.this, UserRegistration.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(SettingsActivity.this, "Please try again", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //saveUserInformation();
        finish();
        return false;

    }

    // on back button, return to main tabbed activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode== KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent(this, ActivityTabbedMain.class);
            finish();
            startActivity(intent);

        }

        return super.onKeyDown(keyCode, event);

    }

}
