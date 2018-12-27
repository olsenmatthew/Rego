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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reylo.rego.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ActivityTabbedMainMatches extends Fragment {

    //declaring views
    private Button peopleButton;
    private Button interestsButton;
    private TextView profileRecyclerTextView;
    private SearchView connectionsSearchView;

    private RecyclerView messagesRecyclerView;
    private RecyclerView.LayoutManager messagesLayoutManager;
    private RecyclerView.Adapter messagesAdapter;

    private RecyclerView profileRecyclerView;
    private RecyclerView.Adapter profileAdapter;
    private RecyclerView.LayoutManager profileLayoutManager;

    private FirebaseAuth mAuth;
    private String thisUserID;
    private DatabaseReference usersDb;

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

        messagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.activity_tabbed_main_matches_messages_recycler);
        messagesRecyclerView.setNestedScrollingEnabled(false);
        messagesRecyclerView.setHasFixedSize(true);
        messagesLayoutManager = new LinearLayoutManager(getContext());
        messagesRecyclerView.setLayoutManager(messagesLayoutManager);
        messagesAdapter = new MatchesMessagesAdapter(getDataSetMessages(), getContext());
        messagesRecyclerView.setAdapter(messagesAdapter);

        profileRecyclerView = (RecyclerView) rootView.findViewById(R.id.activity_tabbed_main_matches_profile_recycler);
        profileRecyclerView.setNestedScrollingEnabled(false);
        profileRecyclerView.setHasFixedSize(true);
        profileLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        profileRecyclerView.setLayoutManager(profileLayoutManager);
        profileAdapter = new MatchesProfileAdapter(getDataSetProfiles(), getContext());
        profileRecyclerView.setAdapter(profileAdapter);


        return rootView;
    }

    private void getConnectedUserId() {

        DatabaseReference connectedDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(thisUserID)
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

    private void RetrieveConnectionInfo(String key) {

        DatabaseReference otherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
        otherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //making sure that the user is not equal to itself
                if (dataSnapshot.exists() && !dataSnapshot.toString().equals(thisUserID)){

                    //this is the other users id
                    String otherUserId = dataSnapshot.getKey();

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


                        matchesObject = new MatchesObject(otherUserId, firstName, profilePicURL);
                        resultMessages.add(matchesObject);
                        messagesAdapter.notifyDataSetChanged();

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





}