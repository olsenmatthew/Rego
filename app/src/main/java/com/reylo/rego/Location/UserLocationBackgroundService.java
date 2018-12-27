package com.reylo.rego.Location;


import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.reylo.rego.Common.Common;

public class UserLocationBackgroundService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // declare location objects
    private Location userLocation;
    private LocationRequest mLocationRequest;

    // init otherpersons string to empty
    private String otherPersons = "";

    // declare local binder
    private IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public UserLocationBackgroundService getServerInstance() {
            return UserLocationBackgroundService.this;
        }
    }

    //declare google api client objects
    private GoogleApiClient googleApiClient;
    private GoogleApiClient mGoogleApiClient;

    // declare wake lock acquired later to keep service running
    private PowerManager.WakeLock mWakeLock;

    // declare firebase auth / user objects
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    // declare database references
    private DatabaseReference otherPeople;
    private DatabaseReference mPastPlaces;

    private boolean mInProgress;

    // declare location callbacks
    private LocationCallback mLocationCallback;

    private Boolean servicesAvailable = false;

    //declare geofire objects
    private GeoFire geoFire;
    private GeoFire geoEntryPoints;
    private GeoFire geoEntryPointsOtherUser;

    // declare location variables
    private double latitude;
    private double longitude;
    private LatLng userLatLng;

    @Override
    public void onCreate() {
        super.onCreate();

        //get authentication instance and create user object
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // init database references
        otherPeople = FirebaseDatabase.getInstance().getReference(Common.people_location);
        mPastPlaces = FirebaseDatabase.getInstance().getReference(Common.people_info).child(user.getUid()).child("PastPlacesID");

        // location tracking isn't occuring, therefore mInProgress is false
        mInProgress = false;

        //create location request and set (priority/intervals/displacements)
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(Constants.DISPLACEMENT);

        servicesAvailable = isGooglePlayServicesAvailable();

        // init geofire object
        geoFire = new GeoFire(otherPeople);

        setUpLocationClientIfNeeded();

        // get user's location and query around their current location
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                for (final Location location : locationResult.getLocations()) {

                    //get user's longitude and latitude
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    // geofirekey is uid
                    final String geoFireKey = user.getUid();

                    // set this user's location given the latitude and longitude
                    geoFire.setLocation(geoFireKey, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                            // query around the user's location for other users
                            GeoQuery userZone = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), .1f);
                            userZone.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {

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
                                    Log.e("Error", "" + error);
                                }

                            });


                        }

                    });

                }

            }

        };

    }

    //build and connect google api client
    protected synchronized void buildGoogleApiClient() {

        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    // return true or false on whether google play services are available
    private boolean isGooglePlayServicesAvailable() {

        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {

            return true;

        } else {

            return false;

        }

    }

    // create now wakelock with powermanager
    // acquire wakelock with a 5 min timeout
    // set up api client if needed
    // connect google api client if not connected
    // return start sticky
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (this.mWakeLock == null) {

            if (powerManager != null) {

                this.mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");

            }

        }

        if (this.mWakeLock != null && !this.mWakeLock.isHeld()) {

            this.mWakeLock.acquire(5 * 60 * 1000L /*5 minutes*/);

        }

        if (mGoogleApiClient != null) {

            if (!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress) {

                return START_STICKY;

            }

        }

        setUpLocationClientIfNeeded();

        if (!mGoogleApiClient.isConnected() || (!mGoogleApiClient.isConnecting() && !mInProgress)) {

            mInProgress = true;
            mGoogleApiClient.connect();

        }

        return START_STICKY;

    }

    // if google api client is null, build the google api client
    private void setUpLocationClientIfNeeded() {

        if (mGoogleApiClient == null) {

            buildGoogleApiClient();

        }

    }

    // if user's longitude and latitude are valid, set latitude and longitude
    // then use geofire to set the location and load all people/places
    @Override
    public void onLocationChanged(Location location) {

        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("debug", msg);

        if (Double.toString(location.getLatitude()) != null
                && Double.toString(location.getLatitude()).length() > 0
                && Double.toString(location.getLongitude()) != null
                && Double.toString(location.getLongitude()).length() > 0) {

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            final String geoFireKey = user.getUid();

            geoFire.setLocation(geoFireKey, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                    loadAllOtherPersons();
                    loadAllOtherPlaces();

                }
            });

        }

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

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // if location permissions are not granted, end function
    // else request location updates, load all other persons/places
    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        loadAllOtherPersons();
        loadAllOtherPlaces();

    }

    // location updating is not in progress
    // google api client is now null
    @Override
    public void onConnectionSuspended(int i) {

        mInProgress = false;
        mGoogleApiClient = null;

    }

    //log no resolution result
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        mInProgress = false;

        if (!connectionResult.hasResolution()) {

            String noResolution = "No connection solution";
            Log.d(noResolution, "No resolution for connection result is currently available");

        }

    }

    public class LocationReceiver extends BroadcastReceiver {

        private String TAG = this.getClass().getSimpleName();

        private LocationResult mLocationResult;

        @Override
        public void onReceive(Context context, Intent intent) {

            if(LocationResult.hasResult(intent)) {

                this.mLocationResult = LocationResult.extractResult(intent);
                Log.i(TAG, "Location Received: " + this.mLocationResult.toString());

            }

        }

    }

    // store constants useed for location requests
    public final class Constants {

        private static final int MILLISECONDS_PER_SECOND = 1000;
        private static final int UPDATE_INTERVAL_IN_SECONDS = 150;
        public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
        private static final int FASTEST_INTERVAL_IN_SECONDS = 60;
        public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
        public static final float DISPLACEMENT = 200f;

        private Constants() {

            throw new AssertionError();

        }

    }

    // if locations permissions are not granted, end function prematurely
    // else get last location of user
    // query at this user's location to get other user's locations and check if paths have crossed between users
    // if yes, register crossed paths to database
    private void loadAllOtherPersons() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        if (mGoogleApiClient != null) {

            userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (userLocation != null) {

                DatabaseReference otherUsersLocation = FirebaseDatabase.getInstance().getReference(Common.people_location);
                GeoFire geoFireLoadOtherPersons = new GeoFire(otherUsersLocation);
                GeoQuery geoQueryLoadOtherPersons = geoFireLoadOtherPersons.queryAtLocation(new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude()), 0.1f);

                geoQueryLoadOtherPersons.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(final String key, final GeoLocation location) {

                        if (!user.getUid().equals(key)){

                            FirebaseDatabase.getInstance().getReference(Common.people_info).child(user.getUid()).child("CrossedPaths").child(key).setValue(true);

                            geoEntryPoints = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.people_info).child(user.getUid()).child("EntryPoints"));
                            geoEntryPoints.setLocation(key, new GeoLocation(location.latitude, location.longitude), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                    geoEntryPointsOtherUser = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.people_info).child(key).child("EntryPoints"));

                                    if (!user.getUid().equals(key)) {

                                        geoEntryPointsOtherUser.setLocation(user.getUid(), new GeoLocation(location.latitude, location.longitude), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {

                                            }
                                        });

                                    }

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

        }

    }

    // if locations permissions are not granted, end function prematurely
    // query google places with this user's location
    // for each place that this user crosses paths with, test if it meets the criteria
    // if yes, save this place's data to the database under the user's past places
    private void loadAllOtherPlaces(){

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(getApplicationContext());

            //TODO: ADD A PLACE FILTER LATER TO CUSTOMIZE THE USERS SEARCH CRITERIA
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);

            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull final Task<PlaceLikelihoodBufferResponse> task) {

                    if (task.isSuccessful()) {

                        mPastPlaces.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                Place currentPlace = null;

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {

                                    currentPlace = placeLikelihood.getPlace();
                                    final String cpID = currentPlace.getId();

                                    if (!dataSnapshot.hasChild(cpID)) {

                                        if (passesFiltrationCriteria(currentPlace)) {

                                            mPastPlaces.child(cpID).setValue(true);
                                            mPastPlaces.child(cpID).child("PlaceName").setValue(currentPlace.getName());
                                            mPastPlaces.child(cpID).child("Address").setValue(currentPlace.getAddress());
                                            mPastPlaces.child(cpID).child("Rating").setValue(currentPlace.getRating());
                                            mPastPlaces.child(cpID).child("Likelihood").setValue(placeLikelihood.getLikelihood());
                                            mPastPlaces.child(cpID).child("Type").setValue(currentPlace.getPlaceTypes());

                                        }


                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }
            });



    }

    // currently hardcoded filtration criteria for google places data
    //TODO: IMPORT ALL THE CONSTRAINTS FROM USER
    //TODO: CREATE AN ARRAY OF ALL DESIRED TYPES AND LOOP THROUGH DESIRED TYPES
    private boolean passesFiltrationCriteria(Place currentPlace) {

        boolean contains_type = false;
        boolean within_budget = false;
        boolean satisfactory_rating = false;

        for (int i = 0; i < 100; i++) {

            if (currentPlace.getPlaceTypes().contains(i)) {

                contains_type = true;
                break;

            }

        }

        if (!contains_type) {

            return false;

        }

        if (currentPlace.getRating() >= 3.0) {

            satisfactory_rating =  true;

        }

        if (!satisfactory_rating) {

            return false;

        }

        if (currentPlace.getPriceLevel() <= 3) {

            within_budget = true;

        }

        if (!within_budget) {

            return false ;

        }

        return true;

    }

    @Override
    public void onDestroy() {

        this.mInProgress = false;

        if (this.servicesAvailable && this.mGoogleApiClient != null) {

            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            this.mGoogleApiClient = null;

        }

        if (this.mWakeLock != null) {

            this.mWakeLock.release();
            this.mWakeLock = null;

        }

        super.onDestroy();

    }

}

