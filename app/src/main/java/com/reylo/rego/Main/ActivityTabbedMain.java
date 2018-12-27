package com.reylo.rego.Main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.ResultReceiver;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.reylo.rego.Basics.LoginActivity;
import com.reylo.rego.Basics.UserBirthday;
import com.reylo.rego.Basics.UserFirstName;
import com.reylo.rego.Basics.UserGender;
import com.reylo.rego.Basics.UserProfilePicture;
import com.reylo.rego.Main.Matches.ActivityTabbedMainMatches;
import com.reylo.rego.R;
import com.reylo.rego.Location.UserLocationBackgroundService;

public class ActivityTabbedMain extends AppCompatActivity {

    // constants
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    // declare location provider
    private LocationManager locationManager;
    private String provider;

    // declare result receivers
    private ResultReceiver mResultReceiver;

    // declare location objects
    protected Location mLastLocation;

    // declare sections pager adapter
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // declare view pager
    private ViewPager mViewPager;

    // declare firebase objects
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    // declare database references
    private DatabaseReference databaseReference;

    // declare tab items
    private TabItem tabItemMultiLocation;
    private TabItem tabItemChatBubble;
    private TabItem tabItemProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();
        // init user object
        user = firebaseAuth.getCurrentUser();

        // return to login if user is not logged in
        if(user == null) {

            finish();
            startActivity(new Intent(this, LoginActivity.class));

        } else {

            // init database reference
            databaseReference = FirebaseDatabase.getInstance().getReference();

            // if user has first name date
            Query queryFirstName = databaseReference.child("Users").child(user.getUid()).child("First Name");
            queryFirstName.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        //and user has birthday data
                        Query queryBirthday = databaseReference.child("Users").child(user.getUid()).child("Birthday");
                        queryBirthday.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    //and user has gender data
                                    Query queryGender = databaseReference.child("Users").child(user.getUid()).child("Gender");
                                    queryGender.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {

                                                // and profile picture exists
                                                Query queryOnePic = databaseReference.child("Users").child(user.getUid()).child("OnePic");
                                                queryOnePic.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {

                                                            //set layout for activity
                                                            setContentView(R.layout.activity_tabbed_main);

                                                            // Create the adapter that will return a fragment for each of the three
                                                            // primary sections of the activity.
                                                            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                                                            // Set up the ViewPager with the sections adapter.
                                                            mViewPager = (ViewPager) findViewById(R.id.container);
                                                            mViewPager.setAdapter(mSectionsPagerAdapter);

                                                            // set tab layout
                                                            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

                                                            //add listeners for page changes and tab selections
                                                            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                                                            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

                                                            //requesting location services if it is not already given
                                                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                                            provider = locationManager.getBestProvider(new Criteria(), false);
                                                            checkLocationPermission();

                                                            //background location service
                                                            startService(new Intent(ActivityTabbedMain.this, UserLocationBackgroundService.class));

                                                        }

                                                        if (!dataSnapshot.exists()) {

                                                            finish();
                                                            startActivity(new Intent(ActivityTabbedMain.this, UserProfilePicture.class));

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                            if (!dataSnapshot.exists()) {

                                                finish();
                                                startActivity(new Intent(ActivityTabbedMain.this, UserGender.class));

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                                if (!dataSnapshot.exists()) {

                                    finish();
                                    startActivity(new Intent(ActivityTabbedMain.this, UserBirthday.class));

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    if (!dataSnapshot.exists()) {

                        finish();
                        startActivity(new Intent(ActivityTabbedMain.this, UserFirstName.class));

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // init tab items
            tabItemMultiLocation = (TabItem) findViewById(R.id.tabItemMultiLocation);
            tabItemChatBubble = (TabItem) findViewById(R.id.tabItemChatBubble);
            tabItemProfile = (TabItem) findViewById(R.id.tabItemProfile);

        }

    }

    // inflate options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_activity_tabbed_main, menu);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {

            return true;

        }

        return super.onOptionsItemSelected(item);

    }

    // switch tabs based on their corresponding xml files in each position
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            //switch for tabs in their corresponding xml files in each position
            switch (position) {
                case 0:
                    ActivityTabbedMainFeed activityTabbedMainFeed = new ActivityTabbedMainFeed();
                    return activityTabbedMainFeed;
                case 1:
                    ActivityTabbedMainMatches activityTabbedMainMatches = new ActivityTabbedMainMatches();
                    notifyDataSetChanged();
                    return activityTabbedMainMatches;
                case 2:
                    ActivityTabbedMainProfile activityTabbedMainProfile = new ActivityTabbedMainProfile();
                    return activityTabbedMainProfile;
                default:
                    return null;

            }

        }

        @Override
        public int getCount() {

            return 3;

        }

    }

    // check if location permissions are granted
    // if granted, return true
    // if not granted, ask permission, then return true or false if granted on user's response
    public boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(ActivityTabbedMain.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ActivityTabbedMain.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityTabbedMain.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityTabbedMain.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(ActivityTabbedMain.this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.positive_button_allow, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(ActivityTabbedMain.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {

                ActivityCompat.requestPermissions(ActivityTabbedMain.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            }

            return false;

        } else {

            return true;

        }

    }

    // on request permissions result, test if permission is granted
    // if permission is granted, start background service
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(ActivityTabbedMain.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(ActivityTabbedMain.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        startService(new Intent(this, UserLocationBackgroundService.class));

                    }

                }

                return;

            }

        }

    }

}
