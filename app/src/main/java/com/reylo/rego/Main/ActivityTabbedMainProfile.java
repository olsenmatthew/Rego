package com.reylo.rego.Main;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reylo.rego.Main.LaunchFromMainProfile.EditInfoActivity;

import com.reylo.rego.Main.LaunchFromMainProfile.SettingsActivity;
import com.reylo.rego.R;

public class ActivityTabbedMainProfile extends Fragment {

    private ImageView activityTabbedMainProfileCircularPicutre;
    private ImageView activityTabbedMainProfileSettingsIcon;
    private ImageView getActivityTabbedMainProfileEditIcon;
    private TextView activityTabbedMainProfileNameTextView;
    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_tabbed_main_profile, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        //Main Profile Picture
        // Connect the image view and then crop image into circle, then get profile picture from database
        activityTabbedMainProfileCircularPicutre = rootView.findViewById(R.id.activity_tabbed_main_profile_circular_picture);
        Glide.clear(activityTabbedMainProfileCircularPicutre);
        FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("OnePic").child("OnePic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String profilePicURL = dataSnapshot.getValue().toString();
                Glide.with(getContext())
                        .load(profilePicURL)
                        .centerCrop()
                        .transform(new CircleTransform(getContext()))
                        .into(activityTabbedMainProfileCircularPicutre);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        activityTabbedMainProfileCircularPicutre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: SEND TO VIEW PROFILE AS OTHER PEOPLE
            }
        });

        //Connect text view for name, then set name with name from database
        activityTabbedMainProfileNameTextView = (TextView) rootView.findViewById(R.id.activity_tabbed_main_profile_name_text_view);
        FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("First Name").child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    activityTabbedMainProfileNameTextView.setText(dataSnapshot.getValue().toString());

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // on settings button clicked, go to edit settings activity
        activityTabbedMainProfileSettingsIcon = (ImageView) rootView.findViewById(R.id.activity_tabbed_main_profile_settings_button);
        activityTabbedMainProfileSettingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().finish();
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);

            }
        });

        // on edit icon clicked, go to edit info activity
        getActivityTabbedMainProfileEditIcon = (ImageView) rootView.findViewById(R.id.activity_tabbed_main_profile_edit_button);
        getActivityTabbedMainProfileEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getActivity().finish();
                Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                startActivity(intent);

            }
        });

        return rootView;

    }

    // used to transform image into a circular format
    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {

            return circleCrop(pool, toTransform);

        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {

            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {

                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;

        }

        @Override public String getId() {

            return getClass().getName();

        }

    }



}