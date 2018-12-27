package com.reylo.rego.Location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.reylo.rego.Basics.UserFirstNameInfo;
import com.reylo.rego.Common.Common;
import com.reylo.rego.Utils.Calculations;
import com.reylo.rego.Main.ActivityTabbedMain;
import com.reylo.rego.R;
import com.reylo.rego.SwipeCards.arrayAdapter;
import com.reylo.rego.SwipeCards.cards;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.getActivity;

public class DiscoverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // declare google map object
    public GoogleMap mMap;

    //map and play services constants
    private static final int DISCOVERY_MAP_REQUEST_CODE = 311;
    private static final int DISCOVERY_MAP_PLAY_SERVICES_RESOLUTION_REQUEST = 402;

    // declare location objects
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location userLocation;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    // declare firebase auth
    private FirebaseAuth firebaseAuth;

    //declare firebase user object
    private FirebaseUser user;

    // database references
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;

    // declare geofire objects
    private GeoFire geoFire;

    //declare marker for current location
    private Marker markerCurrentLocation;
    private GeoFire geoEntryPoints;
    private GeoFire geoEntryPointsOtherUser;

    // constants (used for geolocation)
    private static int MILLISECONDS_TO_SECONDS = 1000;
    private static int UPDATE_INTERVAL_IN_SECONDS = 150;
    private static int UPDATE_INTERVAL = UPDATE_INTERVAL_IN_SECONDS * MILLISECONDS_TO_SECONDS;
    private static int FASTEST_INTERVAL_IN_SECONDS = 60;
    private static int FASTEST_INTERVAL = FASTEST_INTERVAL_IN_SECONDS * MILLISECONDS_TO_SECONDS;
    private static int DISPLACEMENT = 100;

    // declare vertical seek bar for zoom levels
    private VerticalSeekBar mVerticalSeekBar;


    //declarations for other users than this one
    private String otherPersons = "";
    private DatabaseReference otherPeople;

    //this users marker
    private Marker myMarker;

    //entry point markers
    private Marker entryPointMarkers;
    private Circle entryPointCircles;

    // declare views
    private View constraintLayout;
    private View backgroundRelativeLayout;

    //declare components for cards
    private SwipeFlingAdapterView cardContainer;
    private List<cards> rowItems;
    private cards previousCard = null;
    private com.reylo.rego.SwipeCards.arrayAdapter arrayAdapterObject;
    private String thisUserID;
    private String userId;
    private DatabaseReference usersDb;

    //Image Views Used as buttons at bottom
    private ImageView swipeLeftButton;
    private ImageView swipeRightButton;
    private ImageView feedMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_map);

        // create async map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // get userid
        thisUserID = firebaseAuth.getCurrentUser().getUid();

        // get user
        user = firebaseAuth.getCurrentUser();

        // get users database references
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // get database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // get locations database reference
        otherPeople = FirebaseDatabase.getInstance().getReference(Common.people_location);

        // init geofire object
        geoFire = new GeoFire(otherPeople);

        // listen for crossed paths
        // listen if crossed usersDb reference and data exists
        // if users crossed paths, set crossed paths reference for this user and the other user to true
        ifInLocation();

        // init swipe card container and adapter
        cardContainer = (SwipeFlingAdapterView) findViewById(R.id.discoveryCardFrame);
        rowItems = new ArrayList<cards>();
        arrayAdapterObject = new arrayAdapter(DiscoverMapActivity.this, R.layout.item, rowItems);
        cardContainer.setBackgroundResource(R.drawable.card_rounder);
        cardContainer.setAdapter(arrayAdapterObject);

        // set listener for left and right swipes
        // register yes or no for each user on each swipe
        cardContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                previousCard = rowItems.get(0);
                rowItems.remove(0);
                arrayAdapterObject.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {

                //TODO: ON FIRST TIME, CREATE POP UP TO EXPLAIN WHAT HAPPENED
                cards obj = (cards) dataObject;
                userId = obj.getUserId();
                usersRef.child(userId).child("Match").child("No").child(thisUserID).setValue(true);

            }

            @Override
            public void onRightCardExit(Object dataObject) {

                //TODO: ON FIRST TIME, CREATE POP UP TO EXPLAIN WHAT HAPPENED
                cards obj = (cards) dataObject;
                userId = obj.getUserId();
                usersRef.child(userId).child("Match").child("Yes").child(thisUserID).setValue(true);
                matchConnection(userId);

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

        // zoom in camera dependent on the vertical seek bar change
        mVerticalSeekBar = (VerticalSeekBar) findViewById(R.id.userVerticalSeekBarDiscoveryMap);
        mVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mMap.animateCamera(CameraUpdateFactory.zoomTo(progress), 1000, null);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // connect ui components
        constraintLayout = (View) findViewById(R.id.constraintLayout);
        backgroundRelativeLayout = (View) findViewById(R.id.backgroundRelativeLayout);

        // hide card and button views
        makeSwipeViewInvisible();

        // swipeLeftButton
        // swipe left on click of left button
        // undo swipe on long click of left button
        swipeLeftButton = (ImageView) findViewById(R.id.activity_tabbed_main_feed_swipe_left_button);
        swipeLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: ON FIRST TIME, CREATE POP UP TO EXPLAIN WHAT HAPPENED
                if (!rowItems.isEmpty()){
                    cardContainer.getTopCardListener().selectLeft();
                }

            }
        });
        swipeLeftButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                undoSwipe();
                return true;

            }
        });

        //swipeRightButton
        // swipe right on click of right button
        // undo swipe on long click of right button
        swipeRightButton = (ImageView) findViewById(R.id.activity_tabbed_main_feed_swipe_right_button);
        swipeRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: ON FIRST TIME, CREATE POP UP TO EXPLAIN WHAT HAPPENED
                if (!rowItems.isEmpty()){
                    cardContainer.getTopCardListener().selectRight();
                }

            }
        });
        swipeRightButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                undoSwipe();
                return true;

            }
        });

        // on click of feed map button, switch to activity tabbed main
        feedMapButton = (ImageView) findViewById(R.id.activity_tabbed_main_feed_crossed_paths_button);
        feedMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(DiscoverMapActivity.this, ActivityTabbedMain.class);
                startActivity(intent);

            }
        });

        // check if location permissions are granted, if not, ask for permissions
        // if available, build api client, create location request, then display location
        setUpLocation();

        // init fused location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // call background service to listen for location and update database
        startService(new Intent(this, UserLocationBackgroundService.class));

    }

    // on user's location permission answers, if yes, start building api client, creating location request, and displaying locaiton
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case DISCOVERY_MAP_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isPlayServicesAvailable()) {

                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }
                }
                break;

        }

    }

    // check if location permissions are granted, if not, ask for permissions
    // if available, build api client, create location request, then display location
    private void setUpLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            //request to access location
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, DISCOVERY_MAP_REQUEST_CODE);

        } else {

            if (isPlayServicesAvailable()) {

                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }

        }
    }

    // check if location permissions are granted, if not, end function early
    // get last location of user
    // set location with geofire, then move camera's view towards the user's current location
    // then load all other persons
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        userLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (userLocation != null) {

            final double latitude = userLocation.getLatitude();
            final double longitude = userLocation.getLongitude();
            final LatLng userLatLng = new LatLng(latitude, longitude);

            //making geofire key equal to user id
            FirebaseUser user = firebaseAuth.getCurrentUser();
            final String geoFireKey = user.getUid();

            geoFire.setLocation(geoFireKey, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (markerCurrentLocation != null) {
                        markerCurrentLocation.remove();
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13.0f));
                    loadAllOtherPersons();

                }
            });

            Log.d("TAG", String.format("Your location has changed : %f / %f", latitude, longitude));

        } else {

            Log.d("TAG", "Can't get location");

        }


    }

    // check if location permissions are granted, end early if not granted
    // get location of user, then query this user and all other user's separately
    private void loadAllOtherPersons() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        userLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        queryMyselfQuery();

        entryPointGlobalQuery();

    }

    // load all persons within user's radius
    // label your own pin and change color to distinguish it
    // add your own marker to the map
    private void queryMyselfQuery() {

        //Load all other persons within this users radius
        DatabaseReference otherUsersLocation = FirebaseDatabase.getInstance().getReference(Common.people_location);
        GeoFire geoFireLoadOtherPersons = new GeoFire(otherUsersLocation);
        GeoQuery geoQueryLoadOtherPersons = geoFireLoadOtherPersons.queryAtLocation(new GeoLocation(userLocation.getLatitude()
                , userLocation.getLongitude()), .1f);
        geoQueryLoadOtherPersons.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {

                //if the user found within the radius is myself, create a pin that is labeled "You"
                if (user.getUid().equals(key)
                        && FirebaseDatabase.getInstance().getReference(Common.people_info)
                        .child(key).child("Match").child("Yes").child(thisUserID) != null
                        && FirebaseDatabase.getInstance().getReference(Common.people_info)
                        .child(key).child("Match").child("No").child(thisUserID) != null) {

                    //Add myMarker to the map
                    myMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.latitude, location.longitude))
                            .flat(true)
                            .title("You")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    );

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if (marker.equals(myMarker) ){

                                constraintLayout.setVisibility(View.VISIBLE);
                                backgroundRelativeLayout.setVisibility(View.VISIBLE);

                            }
                            return false;
                        }
                    });

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {


            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    // query all entry points around click
    // use entry points to get users
    // then get all of user's data
    // then user user's data to create a new card
    // then add card to the array adapter
    private void entryPointGlobalQuery() {

        //TODO: OPTIMIZE THIS QUERY LATER
        final DatabaseReference entryPointsReference = FirebaseDatabase.getInstance().getReference(Common.people_info)
                .child(user.getUid()).child("EntryPoints");
        geoEntryPoints = new GeoFire(entryPointsReference);
        GeoQuery geoQueryEntryPoints = geoEntryPoints.queryAtLocation(new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude()), 6371f);
        geoQueryEntryPoints.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {

                if (!user.getUid().equals(key)) {

                    FirebaseDatabase.getInstance().getReference(Common.people_info)
                            .child(key).child("Match").child("Yes").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.exists()){

                                //Use key to get info from table of users
                                //TableUsers is when Users Register to get an account and input their information
                                FirebaseDatabase.getInstance().getReference(Common.people_info)
                                        .child(key)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {


                                                if (!user.getUid().equals(key)) {
                                                    UserFirstNameInfo userFirstNameInfo = dataSnapshot.child("First Name").getValue(UserFirstNameInfo.class);

                                                    //Add other users to the map
                                                    if (userFirstNameInfo != null) {
                                                        entryPointMarkers = mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(location.latitude, location.longitude))
                                                                .flat(true)
                                                                .title(userFirstNameInfo.getFirstName())
                                                        );
                                                    }

                                                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                        @Override
                                                        public boolean onMarkerClick(Marker marker) {

                                                            if (!marker.equals(myMarker) ){

                                                                constraintLayout.setVisibility(View.VISIBLE);
                                                                backgroundRelativeLayout.setVisibility(View.VISIBLE);

                                                                DatabaseReference entryPointsReference = FirebaseDatabase.getInstance().getReference(Common.people_info)
                                                                        .child(user.getUid()).child("EntryPoints");
                                                                geoEntryPoints = new GeoFire(entryPointsReference);
                                                                GeoQuery geoQueryEntryPoints = geoEntryPoints.queryAtLocation(new GeoLocation(marker.getPosition().latitude, marker.getPosition().longitude), .1f);
                                                                geoQueryEntryPoints.addGeoQueryEventListener(new GeoQueryEventListener() {
                                                                    @Override
                                                                    public void onKeyEntered(final String key, final GeoLocation location) {

                                                                        if (!user.getUid().equals(key)) {

                                                                            usersRef.addChildEventListener(new ChildEventListener() {
                                                                                @Override
                                                                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                                                                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals(key)) {

                                                                                        if (dataSnapshot.exists() && !dataSnapshot.child("Match").child("No").hasChild(thisUserID)
                                                                                                && !dataSnapshot.child("Match").child("Yes").hasChild(thisUserID)) {

                                                                                            String firstName = "default";
                                                                                            if (dataSnapshot.child("First Name").child("firstName").getValue() != null) {
                                                                                                firstName = dataSnapshot.child("First Name").child("firstName").getValue().toString();
                                                                                            }

                                                                                            String birthdate;
                                                                                            int age = 0;
                                                                                            if (dataSnapshot.child("Birthday").child("userBirthday").getValue() != null) {

                                                                                                birthdate = dataSnapshot.child("Birthday").child("userBirthday").getValue().toString();
                                                                                                String mmddyyyy[] = birthdate.split("/");
                                                                                                int month = Integer.parseInt(mmddyyyy[0]);
                                                                                                int day = Integer.parseInt(mmddyyyy[1]);
                                                                                                int year = Integer.parseInt(mmddyyyy[2]);
                                                                                                age = Calculations.AgeFromDate(year, month, day);

                                                                                            }

                                                                                            String OnePic = "default";
                                                                                            if (dataSnapshot.child("OnePic").child("OnePic").getValue() != null) {
                                                                                                if (!dataSnapshot.child("OnePic").child("OnePic").getValue().equals("default")
                                                                                                        && !dataSnapshot.child("OnePic").child("OnePic").getValue().equals("remove_me")) {
                                                                                                    OnePic = dataSnapshot.child("OnePic").child("OnePic").getValue().toString();
                                                                                                }
                                                                                            }

                                                                                            String TwoPic = "default";
                                                                                            if (dataSnapshot.child("TwoPic").child("TwoPic").getValue() != null) {
                                                                                                if (!dataSnapshot.child("TwoPic").child("TwoPic").getValue().equals("default")
                                                                                                        && !dataSnapshot.child("TwoPic").child("TwoPic").getValue().equals("remove_me")) {
                                                                                                    TwoPic = dataSnapshot.child("TwoPic").child("TwoPic").getValue().toString();
                                                                                                }
                                                                                            }

                                                                                            String ThreePic = "default";
                                                                                            if (dataSnapshot.child("ThreePic").child("ThreePic").getValue() != null) {
                                                                                                if (!dataSnapshot.child("ThreePic").child("ThreePic").getValue().equals("default")
                                                                                                        && !dataSnapshot.child("ThreePic").child("ThreePic").getValue().equals("remove_me")) {
                                                                                                    ThreePic = dataSnapshot.child("ThreePic").child("ThreePic").getValue().toString();
                                                                                                }
                                                                                            }

                                                                                            String FourPic = "default";
                                                                                            if (dataSnapshot.child("FourPic").child("FourPic").getValue() != null) {
                                                                                                if (!dataSnapshot.child("FourPic").child("FourPic").getValue().equals("default")
                                                                                                        && !dataSnapshot.child("FourPic").child("FourPic").getValue().equals("remove_me")) {
                                                                                                    FourPic = dataSnapshot.child("FourPic").child("FourPic").getValue().toString();
                                                                                                }
                                                                                            }
                                                                                            String FivePic = "default";
                                                                                            if (dataSnapshot.child("FivePic").child("FivePic").getValue() != null) {
                                                                                                if (!dataSnapshot.child("FivePic").child("FivePic").getValue().equals("default")
                                                                                                        && !dataSnapshot.child("FivePic").child("FivePic").getValue().equals("remove_me")) {
                                                                                                    FivePic = dataSnapshot.child("FivePic").child("FivePic").getValue().toString();
                                                                                                }
                                                                                            }

                                                                                            String SixPic = "default";
                                                                                            if (dataSnapshot.child("SixPic").child("SixPic").getValue() != null) {
                                                                                                if (!dataSnapshot.child("SixPic").child("SixPic").getValue().equals("default")
                                                                                                        && !dataSnapshot.child("SixPic").child("SixPic").getValue().equals("remove_me")) {
                                                                                                    SixPic = dataSnapshot.child("SixPic").child("SixPic").getValue().toString();
                                                                                                }
                                                                                            }

                                                                                            String schoolName = "default";
                                                                                            if (dataSnapshot.child("School").child("School").getValue() != null) {
                                                                                                schoolName = dataSnapshot.child("School").child("School").getValue().toString();
                                                                                            }

                                                                                            String jobTitle = "default";
                                                                                            if (dataSnapshot.child("JobTitle").child("JobTitle").getValue() != null) {
                                                                                                jobTitle = dataSnapshot.child("JobTitle").child("JobTitle").getValue().toString();
                                                                                            }


                                                                                            if (!dataSnapshot.getKey().equals(user.getUid())
                                                                                                    && !firstName.equals("default")
                                                                                                    && !OnePic.equals("default")
                                                                                                    && age >= 18
                                                                                                    ) {

                                                                                                cards item = new cards(dataSnapshot.getKey(), firstName, age,
                                                                                                        OnePic, TwoPic, ThreePic,
                                                                                                        FourPic, FivePic, SixPic,
                                                                                                        schoolName, jobTitle);

                                                                                                rowItems.add(item);
                                                                                                arrayAdapterObject.notifyDataSetChanged();

                                                                                            }


                                                                                        }

                                                                                    }

                                                                                }

                                                                                @Override
                                                                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                                                                }

                                                                                @Override
                                                                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                                                                }

                                                                                @Override
                                                                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onKeyExited(String key) {

                                                                    }

                                                                    @Override
                                                                    public void onKeyMoved(String key, GeoLocation location) {

                                                                    }

                                                                    @Override
                                                                    public void onGeoQueryReady() {

                                                                    }

                                                                    @Override
                                                                    public void onGeoQueryError(DatabaseError error) {

                                                                    }
                                                                });

                                                            }

                                                            return false;
                                                        }
                                                    });

                                                    mMap.addCircle(new CircleOptions()
                                                            .center(new LatLng(location.latitude, location.longitude))
                                                            .radius(200)//in meters
                                                            .fillColor(0x55a7cdf2)
                                                            .strokeColor(Color.parseColor("#a7cdf2"))
                                                            .strokeWidth(2.5f));


                                                }



                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    // init location request
    // set location request param
    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    // init googleApiClient and connect googleApiClient
    private void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();


    }

    //check that google play services is available
    // if not available toast message to alert the user location permissions are needed, then exit activity
    private boolean isPlayServicesAvailable() {

        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;

        } else {

            Toast.makeText(this, "Location permissions needed to use this function", Toast.LENGTH_LONG).show();
            finish();

            return false;
        }

    }

    // init mMap when ready
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

    }

    // update users location and display location
    @Override
    public void onLocationChanged(Location location) {

        userLocation = location;
        displayLocation();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // display location and start updating location
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();

    }

    // check if user has granted permission to access location
    // if not, end early
    // else get last location and display last location
    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {

                    displayLocation();

                }

            }

        });

    }

    // if suspended, reconnect googleApiClient
    @Override
    public void onConnectionSuspended(int i) {

        googleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //Methods for Cards

    // on both users swiping right on each other
    // connect both users and alert user that a connection has been made
    private void matchConnection(String userId) {

        //TODO: LOW PRIORITY: MAKE THIS INTO A BACKGROUND SERVICE OR GET CONNECTIONS WITHOUT BEING ON THE SCREEN IMMEDIATELY
        DatabaseReference rightSwipers = usersRef.child(thisUserID).child("Match").child("Yes").child(userId);
        rightSwipers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Toast.makeText(DiscoverMapActivity.this, "Connected", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersRef.child(dataSnapshot.getKey()).child("Match").child("Connected").child(thisUserID).child("ChatId").setValue(key);
                    usersRef.child(thisUserID).child("Match").child("Connected").child(dataSnapshot.getKey()).child("ChatId").setValue(key);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // undoes the result of a user swiping either right or left on another user
    // also undoes any connections made with the other user
    public void undoSwipe() {

        if (previousCard != null) {
            if (!usersRef.child(userId).child("Match").child("No").child(thisUserID).equals(null)) {
                usersRef.child(userId).child("Match").child("No").child(thisUserID).removeValue();
            }
            if (!usersRef.child(userId).child("Match").child("Yes").child(thisUserID).equals(null)) {
                usersRef.child(userId).child("Match").child("Yes").child(thisUserID).removeValue();
                undoConnection();
            }

            rowItems.add(0, previousCard);
            arrayAdapterObject.notifyDataSetChanged();
            cardContainer.removeAllViewsInLayout();
            previousCard = null;
        }

    }

    // undo swipe connection
    // un-does the result of both users swiping right
    private void undoConnection() {

        DatabaseReference rightSwipers = usersRef.child(thisUserID).child("Match").child("Yes").child(userId);
        rightSwipers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    usersRef.child(dataSnapshot.getKey()).child("Match").child("Connected").child(thisUserID).removeValue();
                    usersRef.child(thisUserID).child("Match").child("Connected").child(dataSnapshot.getKey()).removeValue();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    // listen to add cards
    // store image reference values as strings
    // calculate age from date
    // add card to adapter after curating information
    public void loadCards() {

        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.getValue() != null) {

                    if (dataSnapshot.exists() && !dataSnapshot.child("Match").child("No").hasChild(thisUserID)
                            && !dataSnapshot.child("Match").child("Yes").hasChild(thisUserID)) {

                        String firstName = "default";
                        if (dataSnapshot.child("First Name").child("firstName").getValue() != null) {
                            firstName = dataSnapshot.child("First Name").child("firstName").getValue().toString();
                        }

                        String birthdate;
                        int age = 0;
                        if (dataSnapshot.child("Birthday").child("userBirthday").getValue() != null) {

                            birthdate = dataSnapshot.child("Birthday").child("userBirthday").getValue().toString();
                            String mmddyyyy[] = birthdate.split("/");
                            int month = Integer.parseInt(mmddyyyy[0]);
                            int day = Integer.parseInt(mmddyyyy[1]);
                            int year = Integer.parseInt(mmddyyyy[2]);
                            age = Calculations.AgeFromDate(year, month, day);

                        }

                        String OnePic = "default";
                        if (dataSnapshot.child("OnePic").child("OnePic").getValue() != null) {
                            if (!dataSnapshot.child("OnePic").child("OnePic").getValue().equals("default")
                                    && !dataSnapshot.child("OnePic").child("OnePic").getValue().equals("remove_me")) {
                                OnePic = dataSnapshot.child("OnePic").child("OnePic").getValue().toString();
                            }
                        }

                        String TwoPic = "default";
                        if (dataSnapshot.child("TwoPic").child("TwoPic").getValue() != null) {
                            if (!dataSnapshot.child("TwoPic").child("TwoPic").getValue().equals("default")
                                    && !dataSnapshot.child("TwoPic").child("TwoPic").getValue().equals("remove_me")) {
                                TwoPic = dataSnapshot.child("TwoPic").child("TwoPic").getValue().toString();
                            }
                        }

                        String ThreePic = "default";
                        if (dataSnapshot.child("ThreePic").child("ThreePic").getValue() != null) {
                            if (!dataSnapshot.child("ThreePic").child("ThreePic").getValue().equals("default")
                                    && !dataSnapshot.child("ThreePic").child("ThreePic").getValue().equals("remove_me")) {
                                ThreePic = dataSnapshot.child("ThreePic").child("ThreePic").getValue().toString();
                            }
                        }

                        String FourPic = "default";
                        if (dataSnapshot.child("FourPic").child("FourPic").getValue() != null) {
                            if (!dataSnapshot.child("FourPic").child("FourPic").getValue().equals("default")
                                    && !dataSnapshot.child("FourPic").child("FourPic").getValue().equals("remove_me")) {
                                FourPic = dataSnapshot.child("FourPic").child("FourPic").getValue().toString();
                            }
                        }
                        String FivePic = "default";
                        if (dataSnapshot.child("FivePic").child("FivePic").getValue() != null) {
                            if (!dataSnapshot.child("FivePic").child("FivePic").getValue().equals("default")
                                    && !dataSnapshot.child("FivePic").child("FivePic").getValue().equals("remove_me")) {
                                FivePic = dataSnapshot.child("FivePic").child("FivePic").getValue().toString();
                            }
                        }

                        String SixPic = "default";
                        if (dataSnapshot.child("SixPic").child("SixPic").getValue() != null) {
                            if (!dataSnapshot.child("SixPic").child("SixPic").getValue().equals("default")
                                    && !dataSnapshot.child("SixPic").child("SixPic").getValue().equals("remove_me")) {
                                SixPic = dataSnapshot.child("SixPic").child("SixPic").getValue().toString();
                            }
                        }

                        String schoolName = "default";
                        if (dataSnapshot.child("School").child("School").getValue() != null) {
                            schoolName = dataSnapshot.child("School").child("School").getValue().toString();
                        }

                        String jobTitle = "default";
                        if (dataSnapshot.child("JobTitle").child("JobTitle").getValue() != null) {
                            jobTitle = dataSnapshot.child("JobTitle").child("JobTitle").getValue().toString();
                        }


                        if (!dataSnapshot.getKey().equals(user.getUid())
                                && !firstName.equals("default")
                                && !OnePic.equals("default")
                                && age >= 18
                                ) {

                            cards item = new cards(dataSnapshot.getKey(), firstName, age,
                                    OnePic, TwoPic, ThreePic,
                                    FourPic, FivePic, SixPic,
                                    schoolName, jobTitle);

                            rowItems.add(item);
                            arrayAdapterObject.notifyDataSetChanged();

                        }


                    }

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // if there are no cards to swipe on / no one is around, display toast message
    private void displayNoPeopleAround() {

        //TODO: DISPLAY THAT NO PEOPLE ARE AROUND IF THERE ARE NO CARDS LEFT TO SWIPE ON
//            Toast.makeText(getContext(), "There's no one new around you :(", Toast.LENGTH_LONG).show();

    }

    // listen for crossed paths
    // listen if crossed usersDb reference and data exists
    // if users crossed paths, set crossed paths reference for this user and the other user to true
    public void ifInLocation() {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        usersDb = usersDb.child(user.getUid());
        // listen if crossed usersDb reference and data exists
        usersDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.getValue() != null) {

                        final String trueCP = dataSnapshot.getValue().toString();
                        // set crossed paths reference for this user and the other user to true
                        if (trueCP.equals("true")) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(dataSnapshot.getKey()).child("CrossedPaths").child(user.getUid()).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("CrossedPaths").child(dataSnapshot.getKey()).setValue(true);
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    // hide the swipe cares and the corresponding buttons when called
    public void makeSwipeViewInvisible(){

        backgroundRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundRelativeLayout.setVisibility(View.GONE);
                constraintLayout.setVisibility(View.GONE);

                //TODO: OPTIMIZATION: UPDATE PINS ON MAP AS SWIPES OCCUR AND CALL MAKE SWIPEVIEW INVISIBLE ON ROW ITEMS
                rowItems.clear();
                arrayAdapterObject.notifyDataSetChanged();
                mMap.clear();
                loadAllOtherPersons();
            }
        });
    }

}