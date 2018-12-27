package com.reylo.rego.Basics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.reylo.rego.R;

public class MoreGenderOptions extends ListFragment implements AdapterView.OnItemClickListener {

    // declare firebase auth object
    private FirebaseAuth firebaseAuth;

    // declare database reference
    private DatabaseReference databaseReference;

    // declare firebase user object
    private FirebaseUser user;

    // set gender to blank (can't be saved to db as "blank")
    public String gender = "blank";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more_gender_options, container, false);

        // set fb auth object
        firebaseAuth = FirebaseAuth.getInstance();

        // get user from firebase auth object
        user = firebaseAuth.getCurrentUser();

        // get reference to projects db
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // toast message to ask user to select gender if it's invalid
        if (gender.length() < 1 && (databaseReference.child("Users").child(user.getUid()).child("Gender") == null)) {

            Toast.makeText(getActivity(), "Please select your preference", Toast.LENGTH_LONG).show();

        }

        return view;
    }

    // save user's gender to database if valid, else ask them to select their gender
    private void saveUserGender() {

        if (gender.length() < 1 || gender.contentEquals("blank")) {

            Toast.makeText(getActivity(), "Please Select Your Gender", Toast.LENGTH_LONG).show();

        } else if (gender.length() > 1 || gender.contentEquals("Female")
                || gender.contentEquals("Male")
                || gender.contentEquals("blank")) {

            final UserGenderInfo userGenderInfo = new UserGenderInfo(gender);

            FirebaseUser user = firebaseAuth.getCurrentUser();

            databaseReference.child("Users").child(user.getUid()).child("Gender").setValue(userGenderInfo);

        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.more_gender_options, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // get all extra genders
        String[] genderArray = new String[35];
        genderArray = new String[]{"Agender", "Androgyne", "Androgyny", "Bigender", "Cis Female", "Cis Male", "Cisgender",
                "Demiboy", "Demigender", "Demigirl", "Female to Male", "Gender Non-Conforming", "Gender Questioning",
                "Gender Variant", "Genderfluid", "Genderqueer", "Intergender", "Intersex", "Male to Female", "Neither",
                "Neutrois", "Non-Binary", "None Gender", "Omnigender", "Pangender", "Poligender", "Third Gender", "Trans Man",
                "Trans Person", "Trans Woman", "Transgender Man", "Transgender Woman", "Transsexual", "Trigender", "", "Other"};

        // set gender by position of click and gender array
        gender = genderArray[position];

        // if valid gender, go to next activity
        // else ask user to select a gender with toast message
        if (gender.contentEquals(genderArray[position]) && !gender.contentEquals("blank") && gender.length() >= 1) {

            Toast.makeText(getActivity(), gender.toString() + "Saved", Toast.LENGTH_LONG).show();
            saveUserGender();
            getActivity().finish();
            startActivity(new Intent(getActivity(), UserProfilePicture.class));

        } else {

            Toast.makeText(getActivity(), "Please Select Your Gender", Toast.LENGTH_LONG).show();

        }

    }

}