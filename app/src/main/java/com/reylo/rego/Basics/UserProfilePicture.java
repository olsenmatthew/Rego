package com.reylo.rego.Basics;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reylo.rego.Main.ActivityTabbedMain;
import com.reylo.rego.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class UserProfilePicture extends AppCompatActivity implements View.OnClickListener {

    // declare ui components
    private TextView userProfilePictureActivityTextView;
    private ImageButton userProfilePictureButton;
    private ImageView userProfilePictureActivityNextArrow;
    private ProgressDialog progressDialog;

    // declare and instantiate strings for first image
    private String imageNumberOne;
    private String OnePic = null;

    // declare firebase auth object
    private FirebaseAuth firebaseAuth;

    // declare storage object and references
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private StorageReference imageAddressInFirebase;

    // declare constant for best pic
    private static final int BEST_PIC = 1;

    //declare photo uri and set to null
    private Uri resultUri = null;

    // declare database reference
    private DatabaseReference databaseReference;

    // declare firebase user reference
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_picture);

        // get authentication instance and create user object
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // if user is null, finish activity and return to login
        if (user == null) {

            finish();
            startActivity(new Intent(this, LoginActivity.class));

        }

        // get firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // instantiate/connect ui components
        userProfilePictureActivityTextView = (TextView) findViewById(R.id.userProfilePictureActivityTextView);
        userProfilePictureButton = (ImageButton) findViewById(R.id.userProfilePictureButton);
        userProfilePictureActivityNextArrow = (ImageView) findViewById(R.id.userProfilePictureActivityNextArrow);

        // create new progress dialog
        progressDialog = (ProgressDialog) new ProgressDialog(UserProfilePicture.this);

        // init database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // init storage and references
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReferenceFromUrl("gs://rego-reylo.appspot.com/");

        // get image address in firebase
        imageAddressInFirebase = storageReference.child("Users").child(user.getUid())
                .child(String.valueOf(1));

        // set onclick listeners for profile picture button and next arrow
        userProfilePictureButton.setOnClickListener(this);
        userProfilePictureActivityNextArrow.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        // ask for permissions if not granted
        // create intent to access gallery and choose photo
        if (view == userProfilePictureButton) {

            int permissionResult = ContextCompat.checkSelfPermission(UserProfilePicture.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionResult == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

            }

            if (permissionResult == PackageManager.PERMISSION_GRANTED) {

                Intent imageIntent = new Intent(Intent.ACTION_PICK);
                imageIntent.setType("image/*");
                startActivityForResult(imageIntent, BEST_PIC);

            }

        }

        // if image is accepted, finish and go to next activity
        if (view == userProfilePictureActivityNextArrow && imageNumberOne != null) {

            finish();
            startActivity(new Intent(this, ActivityTabbedMain.class));

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // after user have chosen a photo from the gallery
        // user uri info and start cropping the image down to size
        // square ratio only
        if (requestCode == BEST_PIC && resultCode == RESULT_OK) {

            Uri mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }

        // after crop request, if success, store image to firebase
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                resultUri = result.getUri();
                storingImageOneInFirebase();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }

        }

    }

    // show progress dialog while saving image to firebase
    // on result, dismiss dialog
    // if success/failure, alert user
    private void storingImageOneInFirebase() {

        progressDialog.show();
        progressDialog.setMessage("Uploading to profile...");

        if (resultUri != null && imageAddressInFirebase != null) {

            imageAddressInFirebase.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    OnePic = taskSnapshot.getDownloadUrl().toString();

                    Toast.makeText(UserProfilePicture.this, "Upload was successful", Toast.LENGTH_LONG).show();
                    userProfilePictureButton.setImageURI(resultUri);

                    final UserProfilePictureInfo userProfilePictureInfo = new UserProfilePictureInfo(OnePic);
                    databaseReference.child("Users").child(user.getUid()).child("OnePic").setValue(userProfilePictureInfo);

                    imageNumberOne = taskSnapshot.getDownloadUrl().toString();
                    Picasso.with(UserProfilePicture.this).load(imageNumberOne).fit().centerCrop().into(userProfilePictureButton);
                    progressDialog.dismiss();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(UserProfilePicture.this, "Upload was not successful, please try again", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();

                }
            });

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

}
