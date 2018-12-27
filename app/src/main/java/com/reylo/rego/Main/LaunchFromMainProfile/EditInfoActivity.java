package com.reylo.rego.Main.LaunchFromMainProfile;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.reylo.rego.Basics.LoginActivity;
import com.reylo.rego.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class EditInfoActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private DatabaseReference mProfileDatabaseRef;

    //Declare UI
    private Toolbar editInfoToolbar;
    private ConstraintLayout editInfoPhotoGrid;
    private RoundedImageView editInfoProfilePicture1;
    private RoundedImageView editInfoProfilePicture2;
    private RoundedImageView editInfoProfilePicture3;
    private RoundedImageView editInfoProfilePicture4;
    private RoundedImageView editInfoProfilePicture5;
    private RoundedImageView editInfoProfilePicture6;
    private RoundedImageView editInfoProfileMarginFarLeft;
    private RoundedImageView editInfoProfileMarginMidLeft;
    private RoundedImageView editInfoProfileMarginMidRight;
    private RoundedImageView editInfoProfileMarginFarRight;
    private RoundedImageView editInfoProfileMarginMidHorizontal;
    private RoundedImageView editInfoProfileMarginTopHorizontal;
    private RoundedImageView editInfoProfilePictureButton1;
    private RoundedImageView editInfoProfilePictureButton2;
    private RoundedImageView editInfoProfilePictureButton3;
    private RoundedImageView editInfoProfilePictureButton4;
    private RoundedImageView editInfoProfilePictureButton5;
    private RoundedImageView editInfoProfilePictureButton6;
    private EditText editInfoProfileName;
    private EditText editInfoProfileAboutMe;
    private EditText editInfoProfileJobTitle;
    private EditText editInfoProfileCompany;
    private EditText editInfoProfileSchool;
    private RadioRealButtonGroup editInfoRealRadioGender;
    private RadioRealButton editInfoRealRadioButtonMale;
    private RadioRealButton editInfoRealRadioButtonFemale;
    private RadioRealButton editInfoRealRadioButtonGenderMoreOptions;
    private RadioRealButtonGroup editInfoRealRadioRelationshipStatus;
    private RadioRealButton editInfoRealRadioSingle;
    private RadioRealButton editInfoRealRadioInARelationship;
    private RadioRealButton editInfoRealRadioRelationshipMoreOptions;
    private EditText editInfoProfileWebsite;

    //Profile Data
    private String mProfilePicURL1;
    private String mProfilePicURL2;
    private String mProfilePicURL3;
    private String mProfilePicURL4;
    private String mProfilePicURL5;
    private String mProfilePicURL6;
    private String mFirstName;
    private String mAboutMe;
    private String mJobTitle;
    private String mCompany;
    private String mSchool;
    private String mGender;
    private String mRelationshipStatus;
    private String mWebsite;
    private boolean cGender = false;
    private boolean cRelationshipStatus = false;
    private boolean fOnePic;
    private boolean fTwoPic;
    private boolean fThreePic;
    private boolean fFourPic;
    private boolean fFivePic;
    private boolean fSixPic;

    private final int PERMISSION_TO_USE_CAMERA = 1;
    static private final int GALLERY_REQUEST_CODE = 1882;

    private StorageReference photoStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if(user == null) {

            finish();
            startActivity(new Intent(this, LoginActivity.class));

        }

        editInfoToolbar = (Toolbar) findViewById(R.id.edit_info_tool_bar);
        editInfoToolbar.isClickable();
        editInfoToolbar.setTitle("Edit Profile");
        setSupportActionBar(editInfoToolbar);

        //Instantiate UI
        editInfoPhotoGrid = (ConstraintLayout) findViewById(R.id.edit_info_profile_photo_grid);
        editInfoProfilePicture1 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_one);
        editInfoProfilePicture2 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_two);
        editInfoProfilePicture3 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_three);
        editInfoProfilePicture4 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_four);
        editInfoProfilePicture5 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_five);
        editInfoProfilePicture6 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_six);
        editInfoProfileMarginFarLeft = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_left_padding_invisible);
        editInfoProfileMarginMidLeft = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_mid_left_padding_invisible);
        editInfoProfileMarginMidRight = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_mid_right_padding_invisible);
        editInfoProfileMarginFarRight = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_right_padding_invisible);
        editInfoProfileMarginMidHorizontal = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_mid_horizontal_padding_invisible);
        editInfoProfileMarginTopHorizontal = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_top_horizontal_padding_invisible);
        editInfoProfilePictureButton1 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_one_add_button);
        editInfoProfilePictureButton2 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_two_add_button);
        editInfoProfilePictureButton3 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_three_add_button);
        editInfoProfilePictureButton4 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_four_add_button);
        editInfoProfilePictureButton5 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_five_add_button);
        editInfoProfilePictureButton6 = (RoundedImageView) findViewById(R.id.edit_info_profile_picture_six_add_button);
        editInfoProfileName = (EditText) findViewById(R.id.edit_info_profile_name);
        editInfoProfileAboutMe = (EditText) findViewById(R.id.edit_info_profile_about_me);
        editInfoProfileJobTitle = (EditText) findViewById(R.id.edit_info_profile_job_title);
        editInfoProfileCompany = (EditText) findViewById(R.id.edit_info_profile_company);
        editInfoProfileSchool = (EditText) findViewById(R.id.edit_info_profile_school);
        editInfoRealRadioGender = (RadioRealButtonGroup) findViewById(R.id.edit_info_radio_button_gender);
        editInfoRealRadioButtonMale = (RadioRealButton) findViewById(R.id.edit_info_radio_button_male);
        editInfoRealRadioButtonFemale = (RadioRealButton) findViewById(R.id.edit_info_radio_button_female);
        editInfoRealRadioButtonGenderMoreOptions = (RadioRealButton) findViewById(R.id.edit_info_radio_button_gender_more_options);
        editInfoRealRadioRelationshipStatus = (RadioRealButtonGroup) findViewById(R.id.edit_info_radio_button_relationship_status);
        editInfoRealRadioSingle = (RadioRealButton) findViewById(R.id.edit_info_radio_button_single);
        editInfoRealRadioInARelationship = (RadioRealButton) findViewById(R.id.edit_info_radio_button_in_a_relationship);
        editInfoRealRadioRelationshipMoreOptions = (RadioRealButton) findViewById(R.id.edit_info_radio_button_relationship_more_options);
        editInfoProfileWebsite = (EditText) findViewById(R.id.edit_info_profile_website);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowmanager != null) {

            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);

        }

        //Sizing metrics for Profiles photos
        int length = (int) (displayMetrics.widthPixels * 0.29);
        int buttonSize = (int) (displayMetrics.widthPixels * 0.05);
        int margin = (int) (displayMetrics.widthPixels * 0.0325);

        //Picture 1
        editInfoProfilePicture1.getLayoutParams().width = length;
        editInfoProfilePicture1.getLayoutParams().height = length;
        editInfoProfilePicture1.requestLayout();
        //Picture 2
        editInfoProfilePicture2.getLayoutParams().width = length;
        editInfoProfilePicture2.getLayoutParams().height = length;
        editInfoProfilePicture2.requestLayout();
        //Picture 3
        editInfoProfilePicture3.getLayoutParams().width = length;
        editInfoProfilePicture3.getLayoutParams().height = length;
        editInfoProfilePicture3.requestLayout();
        //Picture 4
        editInfoProfilePicture4.getLayoutParams().width = length;
        editInfoProfilePicture4.getLayoutParams().height = length;
        editInfoProfilePicture4.requestLayout();
        //Picture 5
        editInfoProfilePicture5.getLayoutParams().width = length;
        editInfoProfilePicture5.getLayoutParams().height = length;
        editInfoProfilePicture5.requestLayout();
        //Picture 6
        editInfoProfilePicture6.getLayoutParams().width = length;
        editInfoProfilePicture6.getLayoutParams().height = length;
        editInfoProfilePicture6.requestLayout();

        //Margin Far Left
        editInfoProfileMarginFarLeft.getLayoutParams().width = margin;
        editInfoProfileMarginFarLeft.getLayoutParams().height = margin;
        //Margin Mid Left
        editInfoProfileMarginMidLeft.getLayoutParams().width = margin;
        editInfoProfileMarginMidLeft.getLayoutParams().height = margin;
        //Margin Mid Right
        editInfoProfileMarginMidRight.getLayoutParams().width = margin;
        editInfoProfileMarginMidRight.getLayoutParams().height = margin;
        //Margin Far Right
        editInfoProfileMarginFarRight.getLayoutParams().width = margin;
        editInfoProfileMarginFarRight.getLayoutParams().height = margin;
        //Margin Mid Horizontal
        editInfoProfileMarginMidHorizontal.getLayoutParams().width = margin;
        editInfoProfileMarginMidHorizontal.getLayoutParams().height = margin;
        //Margin Top Horizontal
        editInfoProfileMarginTopHorizontal.getLayoutParams().width = margin;
        editInfoProfileMarginTopHorizontal.getLayoutParams().height = margin;

        //Profile Pic Button 1
        editInfoProfilePictureButton1.getLayoutParams().width = buttonSize;
        editInfoProfilePictureButton1.getLayoutParams().height = buttonSize;
        //Profile Pic Button 2
        editInfoProfilePictureButton2.getLayoutParams().width = buttonSize;
        editInfoProfilePictureButton2.getLayoutParams().height = buttonSize;
        //Profile Pic Button 3
        editInfoProfilePictureButton3.getLayoutParams().width = buttonSize;
        editInfoProfilePictureButton3.getLayoutParams().height = buttonSize;
        //Profile Pic Button 4
        editInfoProfilePictureButton4.getLayoutParams().width = buttonSize;
        editInfoProfilePictureButton4.getLayoutParams().height = buttonSize;
        //Profile Pic Button 5
        editInfoProfilePictureButton5.getLayoutParams().width = buttonSize;
        editInfoProfilePictureButton5.getLayoutParams().height = buttonSize;
        //Profile Pic Button 6
        editInfoProfilePictureButton6.getLayoutParams().width = buttonSize;
        editInfoProfilePictureButton6.getLayoutParams().height = buttonSize;

        // init profile db reference for this user
        mProfileDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        // get the data for the profile and listen for changes, then handle each of the changes accordingly
        mProfileDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Get Profile Pic URL
                if (dataSnapshot.child("OnePic").child("OnePic").getValue() != null && !dataSnapshot.child("OnePic").child("OnePic").getValue().equals("remove_me")) {

                    mProfilePicURL1 = dataSnapshot.child("OnePic").child("OnePic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(mProfilePicURL1)
                            .centerCrop()
                            .into(editInfoProfilePicture1);

                    editInfoProfilePictureButton1.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_x_white_encircled));
                    fOnePic = true;

                } else {

                    editInfoProfilePictureButton1.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_plus_sign_white_encircled));
                    fOnePic = false;

                }

                //Get Profile pic 2
                if (dataSnapshot.child("TwoPic").child("TwoPic").getValue() != null) {

                    mProfilePicURL2 = dataSnapshot.child("TwoPic").child("TwoPic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(mProfilePicURL2)
                            .centerCrop()
                            .into(editInfoProfilePicture2);

                    editInfoProfilePictureButton2.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_x_white_encircled));
                    fTwoPic = true;

                } else {

                    editInfoProfilePictureButton2.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_plus_sign_white_encircled));
                    fTwoPic = false;

                }

                //Get Profile pic 3
                if (dataSnapshot.child("ThreePic").child("ThreePic").getValue() != null) {

                    mProfilePicURL3 = dataSnapshot.child("ThreePic").child("ThreePic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(mProfilePicURL3)
                            .centerCrop()
                            .into(editInfoProfilePicture3);

                    editInfoProfilePictureButton3.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_x_white_encircled));
                    fThreePic = true;


                } else {

                    editInfoProfilePictureButton3.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_plus_sign_white_encircled));
                    fThreePic = false;

                }

                //Get Profile pic 4
                if (dataSnapshot.child("FourPic").child("FourPic").getValue() != null) {

                    mProfilePicURL4 = dataSnapshot.child("FourPic").child("FourPic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(mProfilePicURL4)
                            .centerCrop()
                            .into(editInfoProfilePicture4);

                    editInfoProfilePictureButton4.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_x_white_encircled));
                    fFourPic = true;


                } else {

                    editInfoProfilePictureButton4.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_plus_sign_white_encircled));
                    fFourPic = false;

                }

                //Get Profile pic 5
                if (dataSnapshot.child("FivePic").child("FivePic").getValue() != null) {

                    mProfilePicURL5 = dataSnapshot.child("FivePic").child("FivePic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(mProfilePicURL5)
                            .centerCrop()
                            .into(editInfoProfilePicture5);

                    editInfoProfilePictureButton5.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_x_white_encircled));
                    fFivePic = true;


                } else {

                    editInfoProfilePictureButton5.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_plus_sign_white_encircled));
                    fFivePic = false;

                }

                //Get Profile pic 6
                if (dataSnapshot.child("SixPic").child("SixPic").getValue() != null) {

                    mProfilePicURL6 = dataSnapshot.child("SixPic").child("SixPic").getValue().toString();
                    Glide.with(getApplicationContext())
                            .load(mProfilePicURL6)
                            .centerCrop()
                            .into(editInfoProfilePicture6);

                    editInfoProfilePictureButton6.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_x_white_encircled));
                    fSixPic = true;

                } else {

                    editInfoProfilePictureButton6.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_plus_sign_white_encircled));
                    fSixPic = false;

                }

                //Get First Name
                if (dataSnapshot.child("First Name").child("firstName").getValue() != null) {

                    mFirstName = dataSnapshot.child("First Name").child("firstName").getValue().toString();
                    editInfoProfileName.setText(mFirstName);

                }

                //Get Gender
                if (dataSnapshot.child("AboutMe").child("AboutMe").getValue() != null) {

                    mAboutMe = dataSnapshot.child("AboutMe").child("AboutMe").getValue().toString();
                    editInfoProfileAboutMe.setText(mAboutMe);

                }

                //Get Job Title
                if (dataSnapshot.child("JobTitle").child("JobTitle").getValue() != null) {

                    mJobTitle = dataSnapshot.child("JobTitle").child("JobTitle").getValue().toString();
                    editInfoProfileJobTitle.setText(mJobTitle);

                }

                //Get Company
                if (dataSnapshot.child("Company").child("Company").getValue() != null) {

                    mCompany = dataSnapshot.child("Company").child("Company").getValue().toString();
                    editInfoProfileCompany.setText(mCompany);

                }

                //Get School
                if (dataSnapshot.child("School").child("School").getValue() != null) {

                    mSchool = dataSnapshot.child("School").child("School").getValue().toString();
                    editInfoProfileSchool.setText(mSchool);

                }

                //Get Gender
                if (dataSnapshot.child("Gender").child("gender").getValue() != null) {

                    mGender = dataSnapshot.child("Gender").child("gender").getValue().toString();

                    if (mGender.equals("Male")) {

                        editInfoRealRadioGender.setPosition(0);

                    } else if (mGender.equals("Female")) {

                        editInfoRealRadioGender.setPosition(1);

                    } else {

                        editInfoRealRadioGender.setPosition(2);
                        editInfoRealRadioButtonGenderMoreOptions.setText(mGender);

                    }

                }

                //Get Relationship Status
                if (dataSnapshot.child("RelationshipStatus").child("RelationshipStatus").getValue() != null) {

                    mRelationshipStatus = dataSnapshot.child("RelationshipStatus").child("RelationshipStatus").getValue().toString();

                    if (mRelationshipStatus.equals("Single")) {

                        editInfoRealRadioRelationshipStatus.setPosition(0);

                    } else if (mRelationshipStatus.equals("In a Relationship")) {

                        editInfoRealRadioRelationshipStatus.setPosition(1);

                    } else {

                        editInfoRealRadioRelationshipStatus.setPosition(2);
                        editInfoRealRadioButtonGenderMoreOptions.setText(mRelationshipStatus);

                    }

                }

                //Get Website
                if (dataSnapshot.child("Website").child("Website").getValue() != null) {

                    mWebsite = dataSnapshot.child("Website").child("Website").getValue().toString();
                    editInfoProfileWebsite.setText(mWebsite);

                }

                //Reorder each of the pictures if the one before it is null but and the latter is not null
                if (dataSnapshot.child("TwoPic").child("TwoPic").getValue() != null && (dataSnapshot.child("OnePic").child("OnePic").getValue() == null
                                                                                        || dataSnapshot.child("OnePic").child("OnePic").getValue().equals("remove_me"))) {

                    String nPic = dataSnapshot.child("TwoPic").child("TwoPic").getValue().toString();
                    mProfileDatabaseRef.child("OnePic").child("OnePic").setValue(nPic);
                    mProfileDatabaseRef.child("TwoPic").child("TwoPic").removeValue();

                }

                //if this picture's value is null, set a gray background
                if (dataSnapshot.child("TwoPic").child("TwoPic").getValue() == null) {

                    editInfoProfilePicture2.setImageResource(R.drawable.gray_background);

                }

                //Reorder each of the pictures if the one before it is null but and the latter is not null
                if (dataSnapshot.child("ThreePic").child("ThreePic").getValue() != null && dataSnapshot.child("TwoPic").child("TwoPic").getValue() == null) {

                    String nPic = dataSnapshot.child("ThreePic").child("ThreePic").getValue().toString();
                    mProfileDatabaseRef.child("TwoPic").child("TwoPic").setValue(nPic);
                    mProfileDatabaseRef.child("ThreePic").child("ThreePic").removeValue();
                    editInfoProfilePicture3.setImageResource(R.drawable.gray_background);

                }

                //if this picture's value is null, set a gray background
                if (dataSnapshot.child("ThreePic").child("ThreePic").getValue() == null) {

                    editInfoProfilePicture3.setImageResource(R.drawable.gray_background);

                }

                //Reorder each of the pictures if the one before it is null but and the latter is not null
                if (dataSnapshot.child("FourPic").child("FourPic").getValue() != null  && dataSnapshot.child("ThreePic").child("ThreePic").getValue() == null) {

                    String nPic = dataSnapshot.child("FourPic").child("FourPic").getValue().toString();
                    mProfileDatabaseRef.child("ThreePic").child("ThreePic").setValue(nPic);
                    mProfileDatabaseRef.child("FourPic").child("FourPic").removeValue();

                }

                //if this picture's value is null, set a gray background
                if (dataSnapshot.child("FourPic").child("FourPic").getValue() == null) {

                    editInfoProfilePicture4.setImageResource(R.drawable.gray_background);

                }

                //Reorder each of the pictures if the one before it is null but and the latter is not null
                if (dataSnapshot.child("FivePic").child("FivePic").getValue() != null  && dataSnapshot.child("FourPic").child("FourPic").getValue() == null) {

                    String nPic = dataSnapshot.child("FivePic").child("FivePic").getValue().toString();
                    mProfileDatabaseRef.child("FourPic").child("FourPic").setValue(nPic);
                    mProfileDatabaseRef.child("FivePic").child("FivePic").removeValue();

                }

                //if this picture's value is null, set a gray background
                if (dataSnapshot.child("FivePic").child("FivePic").getValue() == null) {

                    editInfoProfilePicture5.setImageResource(R.drawable.gray_background);

                }

                //Reorder each of the pictures if the one before it is null but and the latter is not null
                if (dataSnapshot.child("SixPic").child("SixPic").getValue() != null  && dataSnapshot.child("FivePic").child("FivePic").getValue() == null) {

                    String nPic = dataSnapshot.child("SixPic").child("SixPic").getValue().toString();
                    mProfileDatabaseRef.child("FivePic").child("FivePic").setValue(nPic);
                    mProfileDatabaseRef.child("SixPic").child("SixPic").removeValue();
                    editInfoProfilePicture6.setImageResource(R.drawable.gray_background);

                }

                //if this picture's value is null, set a gray background
                if (dataSnapshot.child("SixPic").child("SixPic").getValue() == null) {

                    editInfoProfilePicture6.setImageResource(R.drawable.gray_background);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //if changed, set changed gender status to true, this will be used as a test to send data to firebase later
        editInfoRealRadioGender.setOnPositionChangedListener(new RadioRealButtonGroup.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(RadioRealButton button, int currentPosition, int lastPosition) {

                cGender = true;

                if (currentPosition != 2) {

                    editInfoRealRadioButtonGenderMoreOptions.setText("More");

                }
            }
        });

        //if changed, set changed relationship status to true, this will be used as a test to send data to firebase later
        editInfoRealRadioRelationshipStatus.setOnPositionChangedListener(new RadioRealButtonGroup.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(RadioRealButton button, int currentPosition, int lastPosition) {

                cRelationshipStatus = true;

                if (currentPosition != 2) {

                    editInfoRealRadioRelationshipMoreOptions.setText("More");

                }

            }
        });

        //On click show pop up dialog to specify which extra gender option they have chosen
        editInfoRealRadioButtonGenderMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                genderDialog();

            }
        });

        //On click show pop up dialog to specify which extra relationship status option they have chosen
        editInfoRealRadioRelationshipMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                relationshipDialog();

            }
        });

        // request for camera permissions if not granted
        // then get image from gallery with intent
        editInfoProfilePictureButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else if (fOnePic && (fTwoPic || fThreePic || fFourPic || fFivePic || fSixPic)) {

                    mProfileDatabaseRef.child("OnePic").child("OnePic").setValue("remove_me");

                } else {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");

                    if (galleryIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

                    }

                }

            }
        });

        // request for camera permissions if not granted
        // then get image from gallery with intent
        editInfoProfilePictureButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else if (fTwoPic && (fOnePic || fThreePic || fFourPic || fFivePic || fSixPic)) {

                    mProfileDatabaseRef.child("TwoPic").child("TwoPic").removeValue();

                } else {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");

                    if (galleryIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

                    }

                }

            }
        });

        // request for camera permissions if not granted
        // then get image from gallery with intent
        editInfoProfilePictureButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else if (fThreePic && (fOnePic || fTwoPic || fFourPic || fFivePic || fSixPic)) {

                    mProfileDatabaseRef.child("ThreePic").child("ThreePic").removeValue();

                } else {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");

                    if (galleryIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

                    }

                }

            }
        });

        // request for camera permissions if not granted
        // then get image from gallery with intent
        editInfoProfilePictureButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else if (fFourPic && (fOnePic || fThreePic || fTwoPic || fFivePic || fSixPic)) {

                    mProfileDatabaseRef.child("FourPic").child("FourPic").removeValue();

                } else {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");

                    if (galleryIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

                    }

                }

            }
        });

        // request for camera permissions if not granted
        // then get image from gallery with intent
        editInfoProfilePictureButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else if (fFivePic && (fOnePic || fThreePic || fTwoPic || fFourPic || fSixPic)) {

                    mProfileDatabaseRef.child("FivePic").child("FivePic").removeValue();

                } else {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");

                    if (galleryIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

                    }

                }

            }
        });

        // request for camera permissions if not granted
        // then get image from gallery with intent
        editInfoProfilePictureButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else if (fSixPic && (fOnePic || fThreePic || fTwoPic || fFivePic || fFourPic)) {

                    mProfileDatabaseRef.child("SixPic").child("SixPic").removeValue();

                } else {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");

                    if (galleryIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

                    }

                }

            }
        });

    }



    // create dialog with various relationship options
    private void relationshipDialog() {

        final CharSequence options[] = new CharSequence[] {"Engaged", "Married", "In a civil union",
                                                            "In a domestic partnership", "In an open relationship",
                                                            "It's complicated", "Separated", "Divorced", "Widowed", "Rather Not Say", "Other"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("I am...");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choiceIndex) {

                editInfoRealRadioRelationshipMoreOptions.setText(options[choiceIndex].toString());
                editInfoRealRadioRelationshipStatus.setPosition(2);

            }
        });
        builder.show();

    }

    // create dialog with various gender options
    private void genderDialog() {

        final CharSequence options[] = new CharSequence[] {"Agender", "Androgyne", "Androgyny", "Bigender", "Cis Female",
                                                        "Cis Male", "Cisgender", "Demiboy", "Demigender", "Demigirl",
                                                        "Female to Male", "Gender Non-Conforming", "Gender Questioning",
                                                        "Gender Variant", "Genderfluid", "Genderqueer", "Intergender",
                                                        "Intersex", "Male to Female", "Neither", "Neutrois", "Non-Binary",
                                                        "None Gender", "Omnigender", "Pangender", "Poligender", "Third Gender",
                                                        "Trans Man", "Trans Person", "Trans Woman", "Transgender Man", "Transgender Woman",
                                                        "Transsexual", "Trigender", "Rather Not Say", "Other"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("I am...");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choiceIndex) {

                editInfoRealRadioButtonGenderMoreOptions.setText(options[choiceIndex].toString());
                editInfoRealRadioGender.setPosition(2);

            }
        });
        builder.show();

    }

    // return true or false as to whether the profile data was edited
    private boolean isEdited() {

        if (!editInfoProfileName.getText().toString().equals(mFirstName)
                || !editInfoProfileAboutMe.getText().toString().equals(mAboutMe)
                || !editInfoProfileJobTitle.getText().toString().equals(mJobTitle)
                || !editInfoProfileCompany.getText().toString().equals(mCompany)
                || !editInfoProfileSchool.getText().toString().equals(mSchool)
                || !editInfoProfileWebsite.getText().toString().equals(mWebsite)
                || cGender
                || cRelationshipStatus) {

            return true;

        }

        return false;

    }

    // save changes to all of the profile's mutable data if changed
    private void saveChanges() {

        if (!editInfoProfileName.getText().toString().equals(mFirstName)) {

            mFirstName = editInfoProfileName.getText().toString();
            mProfileDatabaseRef.child("First Name").child("firstName").setValue(mFirstName);

        }

        if (!editInfoProfileAboutMe.getText().toString().equals(mAboutMe)) {

            mAboutMe = editInfoProfileAboutMe.getText().toString();
            mProfileDatabaseRef.child("AboutMe").child("AboutMe").setValue(mAboutMe);

        }

        if (!editInfoProfileJobTitle.getText().toString().equals(mJobTitle)) {

            mJobTitle = editInfoProfileJobTitle.getText().toString();
            mProfileDatabaseRef.child("JobTitle").child("JobTitle").setValue(mJobTitle);

        }

        if (!editInfoProfileCompany.getText().toString().equals(mCompany)) {

            mCompany = editInfoProfileCompany.getText().toString();
            mProfileDatabaseRef.child("Company").child("Company").setValue(mCompany);

        }

        if (!editInfoProfileSchool.getText().toString().equals(mSchool)) {

            mSchool = editInfoProfileSchool.getText().toString();
            mProfileDatabaseRef.child("School").child("School").setValue(mSchool);

        }

        if (!editInfoProfileWebsite.getText().toString().equals(mWebsite)) {

            mWebsite = editInfoProfileWebsite.getText().toString();
            mWebsite = mWebsite.toLowerCase();
            mProfileDatabaseRef.child("Website").child("Website").setValue(mWebsite);

        }

        if (cGender) {

            if (editInfoRealRadioGender.getPosition() == 0) {

                mProfileDatabaseRef.child("Gender").child("gender").setValue("Male");

            } else if (editInfoRealRadioGender.getPosition() == 1) {

                mProfileDatabaseRef.child("Gender").child("gender").setValue("Female");

            } else {

                mProfileDatabaseRef.child("Gender").child("gender").setValue(editInfoRealRadioButtonGenderMoreOptions.getText().toString());

            }

        }

        if (cRelationshipStatus) {

            if (editInfoRealRadioRelationshipStatus.getPosition() == 0) {

                mProfileDatabaseRef.child("RelationshipStatus").child("RelationshipStatus").setValue("Single");

            } else if (editInfoRealRadioRelationshipStatus.getPosition() == 1) {

                mProfileDatabaseRef.child("RelationshipStatus").child("RelationshipStatus").setValue("In a Relationship");

            } else {

                mProfileDatabaseRef.child("RelationshipStatus").child("RelationshipStatus").setValue(editInfoRealRadioRelationshipMoreOptions.getText().toString());

            }

        }

    }

    //When this activity is destroyed, send all of the data to be saved on this users profile on fire base
    @Override
    protected void onDestroy() {

        if (isEdited()) {

            saveChanges();

        }
        //TODO: SAVE IMAGES ON SUCCESSFUL UPLOAD

        super.onDestroy();

    }

    // request for camera permisions if they are not granted, then return true or false on result
    private boolean requestAndReturnCameraPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            //Give user option to still opt-in the permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_TO_USE_CAMERA);

        }

        //If permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }

    }

    // if result if from gallery intent, go to crop activity for image next
    // if result if from crop activity, save image or error
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri uri = data.getData();

            CropImage.activity(uri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri uri = result.getUri();

                saveImageInOrder(uri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }

    //if camera permissions are not granted, ask for permission
    // save images in order by testing if each of the 6 profile pictures exists
    private void saveImageInOrder(Uri uri) {

        if (!requestAndReturnCameraPermissions()) {

            requestAndReturnCameraPermissions();

        } else if (!fOnePic) {

            //Save OnePic
            photoStorageReference = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("OnePic");
            photoStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String imageURL = taskSnapshot.getDownloadUrl().toString();
                    mProfileDatabaseRef.child("OnePic").child("OnePic").setValue(imageURL);

                }
            });

        } else if (!fTwoPic) {

            //Save TwoPic
            photoStorageReference = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("TwoPic");
            photoStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String imageURL = taskSnapshot.getDownloadUrl().toString();
                    mProfileDatabaseRef.child("TwoPic").child("TwoPic").setValue(imageURL);

                }
            });

        } else if (!fThreePic) {

            //Save ThreePic
            photoStorageReference = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("ThreePic");
            photoStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String imageURL = taskSnapshot.getDownloadUrl().toString();
                    mProfileDatabaseRef.child("ThreePic").child("ThreePic").setValue(imageURL);

                }
            });

        } else if (!fFourPic) {

            //Save FourPic
            photoStorageReference = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("FourPic");
            photoStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String imageURL = taskSnapshot.getDownloadUrl().toString();
                    mProfileDatabaseRef.child("FourPic").child("FourPic").setValue(imageURL);

                }
            });

        } else if (!fFivePic) {

            //Save FivePic
            photoStorageReference = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("FivePic");
            photoStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String imageURL = taskSnapshot.getDownloadUrl().toString();
                    mProfileDatabaseRef.child("FivePic").child("FivePic").setValue(imageURL);

                }
            });


        } else if (!fSixPic) {

            //Save SixPic
            photoStorageReference = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("SixPic");
            photoStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String imageURL = taskSnapshot.getDownloadUrl().toString();
                    mProfileDatabaseRef.child("SixPic").child("SixPic").setValue(imageURL);

                }
            });

        }

    }

}
