package com.reylo.rego.Main.Matches;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.reylo.rego.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ActivityTabbedMainMatches extends Fragment {

    //declaring ui components
    private Button peopleButton;
    private Button interestsButton;
    private TextView profileRecyclerTextView;
    private SearchView connectionsSearchView;

    // declare messages recycler views
    private RecyclerView messagesRecyclerView;
    private RecyclerView.LayoutManager messagesLayoutManager;
    private RecyclerView.Adapter messagesAdapter;

    // declare profile recycler views
    private RecyclerView profileRecyclerView;
    private RecyclerView.Adapter profileAdapter;
    private RecyclerView.LayoutManager profileLayoutManager;

    // declare firebase objects
    private FirebaseAuth mAuth;

    // declare string to hold uid
    private String thisUserID;

    // declare database references
    private DatabaseReference usersDb;
    private DatabaseReference chatDb;
    private DatabaseReference theseUsersChatId;

    private MatchesObject matchesObject;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_tabbed_main_matches, container, false);

        //Firebase references
        mAuth = FirebaseAuth.getInstance();
        thisUserID = mAuth.getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        //referencing views
        peopleButton = (Button) rootView.findViewById(R.id.activity_tabbed_main_feed_people_button);
        interestsButton = (Button) rootView.findViewById(R.id.activity_tabbed_main_feed_interests_button);
        connectionsSearchView = (SearchView) rootView.findViewById(R.id.activity_tabbed_main_matches_search_view);
        profileRecyclerTextView = (TextView) rootView.findViewById(R.id.activity_tabbed_main_matches_profile_recycler_text_view);

        // connect and set message recycler view components
        messagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.activity_tabbed_main_matches_messages_recycler);
        messagesRecyclerView.setNestedScrollingEnabled(false);
        messagesRecyclerView.setHasFixedSize(true);
        messagesLayoutManager = new LinearLayoutManager(getContext());
        messagesRecyclerView.setLayoutManager(messagesLayoutManager);
        messagesAdapter = new MatchesMessagesAdapter(getDataSetMessages(), getContext());
        messagesRecyclerView.setAdapter(messagesAdapter);

        // connect and set profile picture matches view components
        profileRecyclerView = (RecyclerView) rootView.findViewById(R.id.activity_tabbed_main_matches_profile_recycler);
        profileRecyclerView.setNestedScrollingEnabled(false);
        profileRecyclerView.setHasFixedSize(true);
        profileLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        profileRecyclerView.setLayoutManager(profileLayoutManager);
        profileAdapter = new MatchesProfileAdapter(getDataSetProfiles(), getContext());
        profileRecyclerView.setAdapter(profileAdapter);


        return rootView;
    }

    // get uid of matched/connected user
    private void getConnectedUserId() {

        DatabaseReference connectedDatabaseReference = usersDb.child(thisUserID)
                .child("Match").child("Connected");
        connectedDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(DataSnapshot connected : dataSnapshot.getChildren()){
                        RetrieveConnectionInfo(connected.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // get all connections from database
    // make sure that each connection is a user with valid data
    // then add items to recycler view if valid
    private void RetrieveConnectionInfo(String key) {

        DatabaseReference otherUserDB = usersDb.child(key);
        otherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //making sure that the user is not equal to itself
                if (dataSnapshot.exists() && !dataSnapshot.toString().equals(thisUserID)){

                    //this is the other users id
                    final String otherUserId = dataSnapshot.getKey();

                    //getting other users first name
                    String firstName = "";
                    if (dataSnapshot.child("First Name").child("firstName").getValue()!= null){
                        firstName = dataSnapshot.child("First Name").child("firstName").getValue().toString();
                    }

                    //getting other users profile pic
                    String profilePicURL = "";
                    if (dataSnapshot.child("OnePic").child("OnePic").getValue()!= null){
                        profilePicURL = dataSnapshot.child("OnePic").child("OnePic").getValue().toString();
                    }



                    if (dataSnapshot.exists() && !dataSnapshot.toString().equals(thisUserID)) {

                        theseUsersChatId = FirebaseDatabase.getInstance().getReference().child("Users").child(thisUserID).child("Match").child("Connected").child(otherUserId).child("ChatId");
                        DatabaseReference theseUsersChatIdReference = theseUsersChatId;

                        matchesObject = new MatchesObject(otherUserId, firstName, profilePicURL);

                        final String finalFirstName = firstName;
                        final String finalProfilePicURL = profilePicURL;
                        theseUsersChatIdReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                chatDb =  FirebaseDatabase.getInstance().getReference().child("Chat");
                                DatabaseReference chatDbReference = chatDb.child(dataSnapshot.getValue().toString());
                                Query lastMessageQuery = chatDbReference.orderByKey().limitToLast(1);

                                lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            String lastMessageContent = "";
                                            long timestampMillis = 0;
                                            for (DataSnapshot child: dataSnapshot.getChildren()) {
                                                if (child.child("messageText").getValue() != null) {
                                                    lastMessageContent = child.child("messageText").getValue().toString();
                                                }
                                                if (child.child("messageTimestamp").getValue() != null) {
                                                    timestampMillis = Long.parseLong(child.child("messageTimestamp").getValue().toString());
                                                }
                                            }

                                            String lastMessageTimestamp = createTimestamp(timestampMillis);

                                            matchesObject = new MatchesObject(otherUserId, finalFirstName, finalProfilePicURL, lastMessageContent, lastMessageTimestamp);

                                            resultMessages.add(matchesObject);
                                            messagesAdapter.notifyDataSetChanged();

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        resultProfiles.add(matchesObject);
                        profileAdapter.notifyDataSetChanged();

                        if (!resultProfiles.isEmpty()) {
                            profileRecyclerTextView.setVisibility(View.VISIBLE);
                        } else {
                            profileRecyclerTextView.setVisibility(View.GONE);
                        }

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<MatchesObject> resultMessages = new ArrayList<MatchesObject>();
    private List<MatchesObject> getDataSetMessages() {

        return resultMessages;

    }

    private ArrayList<MatchesObject> resultProfiles = new ArrayList<MatchesObject>();
    private List<MatchesObject> getDataSetProfiles() {

        return resultProfiles;

    }

    // clear messages and profiles on resume in case they have been updated
    @Override
    public void onResume() {

        super.onResume();
        if (profileRecyclerView != null
                && messagesRecyclerView != null
                && messagesLayoutManager != null
                && messagesAdapter != null
                && profileAdapter != null
                && profileLayoutManager != null) {

            resultMessages.clear();
            resultProfiles.clear();
            getConnectedUserId();

        }

    }

    // clear messages and profiles on cases where is visible to the user in case they have been updated
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (profileRecyclerView != null
                && messagesRecyclerView != null
                && messagesLayoutManager != null
                && messagesAdapter != null
                && profileAdapter != null
                && profileLayoutManager != null
                && isVisibleToUser) {

            resultMessages.clear();
            resultProfiles.clear();
            getConnectedUserId();

        }

    }

    //Creating a timestamp from milliseconds (local to each user's time zone)
    private String createTimestamp(long millis) {

        //Setting bubbleTimestamp
        String time = null;

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int minutes = calendar.get(Calendar.MINUTE);
        int hours = calendar.get(Calendar.HOUR);
        int hoursOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String firstOrLastTwelve = null;
        if (hoursOfDay < 12) {
            firstOrLastTwelve = " AM";
        } else {
            firstOrLastTwelve = " PM";
        }
        if (hours == 0) {
            hours = 12;
        }
        time = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);

        if (time == null) {
            time = "";
        }

        return (time + firstOrLastTwelve);

    }

}