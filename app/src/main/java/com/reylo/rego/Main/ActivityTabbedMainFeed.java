package com.reylo.rego.Main;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.reylo.rego.Utils.Calculations;
import com.reylo.rego.Location.DiscoverMapActivity;
import com.reylo.rego.R;
import com.reylo.rego.SwipeCards.arrayAdapter;
import com.reylo.rego.SwipeCards.cards;

import java.util.ArrayList;
import java.util.List;

public class ActivityTabbedMainFeed extends Fragment {

    private com.reylo.rego.SwipeCards.arrayAdapter arrayAdapterObject;

    //declare firebase auth / user objects
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // store uid
    private String thisUserID;
    private String userId;

    // declare database references
    private DatabaseReference usersDb;
    private DatabaseReference usersRef;

    //declare row items ( each item is a card with user data on each)
    private List<cards> rowItems;

    // store previous card for undo swipe
    private cards previousCard = null;

    // declare fling container for swiping each of the cards
    private SwipeFlingAdapterView cardContainer;

    // declare buttons
    private ImageView swipeLeftButton;
    private ImageView swipeRightButton;
    private ImageView feedMapButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_activity_tabbed_main_feed, container, false);

        // init
        user = FirebaseAuth.getInstance().getCurrentUser();

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // init mAuth and uid
        mAuth = FirebaseAuth.getInstance();
        thisUserID = mAuth.getCurrentUser().getUid();

        // listen for crossed paths
        // listen if crossed usersDb reference and data exists
        // if users crossed paths, set crossed paths reference for this user and the other user to true
        ifInLocation();

        // init swipe card container and adapter
        rowItems = new ArrayList<cards>();
        arrayAdapterObject = new arrayAdapter(getContext(), R.layout.item, rowItems);
        cardContainer = (SwipeFlingAdapterView) rootView.findViewById(R.id.frame);
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
                arrayAdapterObject.notifyDataSetChanged();

            }

            @Override
            public void onRightCardExit(Object dataObject) {

                //TODO: ON FIRST TIME, CREATE POP UP TO EXPLAIN WHAT HAPPENED
                //TODO: ADD CONNECTION LISTENER
                cards obj = (cards) dataObject;
                userId = obj.getUserId();
                usersRef.child(userId).child("Match").child("Yes").child(thisUserID).setValue(true);
                matchConnection(userId);
                arrayAdapterObject.notifyDataSetChanged();

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

        //swipeLeftButton
        swipeLeftButton = (ImageView) rootView.findViewById(R.id.activity_tabbed_main_feed_swipe_left_button);
        swipeLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: ON FIRST TIME, CREATE POP UP TO EXPLAIN WHAT HAPPENED
                if (!rowItems.isEmpty()) {
                    cardContainer.getTopCardListener().selectLeft();
                }

            }
        });

        // swipe left on left click
        swipeLeftButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                undoSwipe();
                return true;

            }
        });

        //swipeRightButton
        swipeRightButton = (ImageView) rootView.findViewById(R.id.activity_tabbed_main_feed_swipe_right_button);

        // swipe right on right click
        swipeRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: ON FIRST TIME, CREATE POP UP TO EXPLAIN WHAT HAPPENED
                if (!rowItems.isEmpty()) {
                    cardContainer.getTopCardListener().selectRight();
                }

            }
        });

        // undo swipes on long click
        swipeRightButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                undoSwipe();
                return true;

            }
        });

        //feedMapButton, on click, go to discovery map activity
        feedMapButton = (ImageView) rootView.findViewById(R.id.activity_tabbed_main_feed_crossed_paths_button);
        feedMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().finish();
                Intent intent = new Intent(getActivity(), DiscoverMapActivity.class);
                startActivity(intent);

            }
        });

        displayNoPeopleAround();

        return rootView;

    }

    // on both users swiping right on each other
    // connect both users and alert user that a connection has been made
    private void matchConnection(String userId) {

        //TODO: LOW PRIORITY: MAKE THIS INTO A BACKGROUND SERVICE OR GET CONNECTIONS WITHOUT BEING ON THE SCREEN IMMEDIATELY
        DatabaseReference rightSwipers = usersRef.child(thisUserID).child("Match").child("Yes").child(userId);
        rightSwipers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    Toast.makeText(getContext(), "Connected", Toast.LENGTH_LONG).show();

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
        usersDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    if (dataSnapshot.getValue() != null) {

                        final String trueCP;
                        trueCP = dataSnapshot.getValue().toString();

                        if (trueCP.equals("true")) {

                            FirebaseDatabase.getInstance().getReference().child("Users").child(dataSnapshot.getKey()).child("CrossedPaths").child(user.getUid()).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("CrossedPaths").child(dataSnapshot.getKey()).setValue(true);

                        } else if (trueCP.equals("false")) {

                            //TODO: ADD FUNCTIONALITY IF THIS IS EQUAL TO FALSE

                        }
                        loadCards();

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

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

    // undoes the result of a user swiping either right or left on another user
    // also undoes any connections made with the other user
    public void undoSwipe() {

        if (previousCard != null) {

            if (!usersRef.child(userId).child("Match").child("No").child(thisUserID).equals(null)){

                usersRef.child(userId).child("Match").child("No").child(thisUserID).removeValue();

            }

            if (!usersRef.child(userId).child("Match").child("Yes").child(thisUserID).equals(null)){

                usersRef.child(userId).child("Match").child("Yes").child(thisUserID).removeValue();
                undoConnection();

            }

            cardContainer.removeAllViewsInLayout();
            rowItems.add(0, previousCard);
            arrayAdapterObject.notifyDataSetChanged();
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

                if (dataSnapshot.exists()){

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersRef.child(dataSnapshot.getKey()).child("Match").child("Connected").child(thisUserID).removeValue();
                    usersRef.child(thisUserID).child("Match").child("Connected").child(dataSnapshot.getKey()).removeValue();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}