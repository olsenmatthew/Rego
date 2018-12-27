package com.reylo.rego.Basics;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.reylo.rego.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UserBirthday extends AppCompatActivity implements View.OnClickListener{

    // declare ui components later connected to corresponding components in activity
    private EditText userBirthdayDateEditText;
    private ImageView userBirthdayActivityNextArrow;

    //declare dialog to pick dates
    private DatePickerDialog.OnDateSetListener userBirthdayDatePickerDialog;

    // declare firebase auth object
    private FirebaseAuth firebaseAuth;

    //declare database reference
    private DatabaseReference databaseReference;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_birthday);

        // get user
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // if user is not logged in, return back to login activity
        if(user == null) {

            finish();
            startActivity(new Intent(this, LoginActivity.class));

        }

        //get database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // add value event listener to user's birthday location
        // if value exists, move to the next activity
        Query query = databaseReference.child("Users").child(user.getUid()).child("Birthday");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    finish();
                    startActivity(new Intent(UserBirthday.this, UserGender.class));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // connect ui components to corresponding components in activity_user_birthday layout
        userBirthdayDateEditText = (EditText) findViewById(R.id.userBirthdayDateEditText);
        userBirthdayActivityNextArrow = (ImageView) findViewById(R.id.userBirthdayActivityNextArrow);

        //create a text watcher to auto-correct invalid dates (not needed if user clicks to user the dialog)
        TextWatcher tw = new TextWatcher() {

            private String current = "";
            private String mmddyyyy = "mmddyyyy";
            private Calendar cal = Calendar.getInstance();

            //when the text changes, monitor for inaccuracies
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // if birthday text is not ""
                if (!s.toString().equals(current)) {

                    // remove all non digits or slashes
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");


                    int cleanLength = clean.length();
                    int sel = cleanLength;
                    for (int i = 2; i <= cleanLength && i < 6; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){

                        clean = clean + mmddyyyy.substring(clean.length());

                    } else {

                        int mon  = Integer.parseInt(clean.substring(0,2));
                        int day  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        Calendar calendar = Calendar.getInstance();
                        int userBirthdayYearMaxYear = (calendar.get(Calendar.YEAR) - 18 );
                        int userBirthdayYearMinYear = (calendar.get(Calendar.YEAR) - 100 );

                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon-1);
                        year = (year<userBirthdayYearMinYear)?userBirthdayYearMinYear:(year>userBirthdayYearMaxYear)?userBirthdayYearMaxYear:year;
                        cal.set(Calendar.YEAR, year);

                        day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal.getActualMaximum(Calendar.DATE) : day;
                        clean = String.format("%02d%02d%02d", mon, day, year);

                        int userBirthdayYearMaxMonth = (calendar.get(Calendar.MONTH) + 1 );
                        int userBirthdayYearMaxDayOfMonth = (calendar.get(Calendar.DAY_OF_MONTH) - 0 );

                        // ensure that the user is atleast 18 years old, then autocorrect data and send message
                        if (year == userBirthdayYearMaxYear && mon > userBirthdayYearMaxMonth) {

                            Toast.makeText(UserBirthday.this, "You must be 18 years of age or older", Toast.LENGTH_LONG).show();

                            Date todayMax = new Date();
                            Calendar calendarMax = Calendar.getInstance();
                            calendarMax.setTime(todayMax);
                            calendarMax.add(Calendar.YEAR, -18 );

                            calendarMax.set(Calendar.YEAR - 18,  userBirthdayYearMaxMonth, userBirthdayYearMaxDayOfMonth);


                            day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;

                            mon = mon < 1 ? 1 : mon > userBirthdayYearMaxMonth ? userBirthdayYearMaxMonth : mon;
                            cal.set(Calendar.MONTH, mon-1);
                            year = (year<userBirthdayYearMinYear)?userBirthdayYearMinYear:(year>userBirthdayYearMaxYear)?userBirthdayYearMaxYear:year;
                            day = day < 1 ? 1 : day > userBirthdayYearMaxDayOfMonth ? userBirthdayYearMaxDayOfMonth : day;

                            clean = String.format("%02d%02d%02d", mon, day, year);

                        }

                        // put a max age to the user and correct if the data entered make the user older than the max
                        if ((year == userBirthdayYearMaxYear && mon < userBirthdayYearMaxMonth && day > userBirthdayYearMaxDayOfMonth)
                                || (year == userBirthdayYearMaxYear && mon <= userBirthdayYearMaxMonth && day <= userBirthdayYearMaxDayOfMonth)) {

                            Date todayMax = new Date();
                            Calendar calendarMax = Calendar.getInstance();
                            calendarMax.setTime(todayMax);
                            calendarMax.add(Calendar.YEAR, -18 );

                            calendarMax.set(Calendar.YEAR - 18,  userBirthdayYearMaxMonth, userBirthdayYearMaxDayOfMonth);


                            day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;

                            mon = mon < 1 ? 1 : mon > userBirthdayYearMaxMonth ? userBirthdayYearMaxMonth : mon;
                            cal.set(Calendar.MONTH, mon-1);
                            year = (year<userBirthdayYearMinYear)?userBirthdayYearMinYear:(year>userBirthdayYearMaxYear)?userBirthdayYearMaxYear:year;
                            day = day < 1 ? 1 : day > userBirthdayYearMaxDayOfMonth ? userBirthdayYearMaxDayOfMonth : day;

                            clean = String.format("%02d%02d%02d", mon, day, year);

                        }

                    }

                    //final format of the string to date format
                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    userBirthdayDateEditText.setText(current);
                    userBirthdayDateEditText.setSelection(sel < current.length() ? sel : current.length());

                }

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // add the text watcher to text changed listener
        userBirthdayDateEditText.addTextChangedListener(tw);

        // set onclick listeners
        userBirthdayDateEditText.setOnClickListener(this);
        userBirthdayActivityNextArrow.setOnClickListener(this);

        // on click of date edit text, create date picker dialog
        userBirthdayDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                final int userBirthdayYear = (calendar.get(Calendar.YEAR) - 18 );
                final int userBirthdayMonth = calendar.get(Calendar.MONTH);
                final int userBirthdayDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(UserBirthday.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        userBirthdayDatePickerDialog, userBirthdayYear, userBirthdayMonth, userBirthdayDayOfMonth);

                Date todayMax = new Date();
                Calendar calendarMax = Calendar.getInstance();
                calendarMax.setTime(todayMax);
                calendarMax.add( Calendar.YEAR, -18 );
                long maxDate = calendarMax.getTime().getTime();

                Date todayMin = new Date();
                Calendar calendarMin = Calendar.getInstance();
                calendarMin.setTime(todayMin);
                calendarMin.add( Calendar.YEAR, -100 );
                long minDate = calendarMin.getTime().getTime();

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.getDatePicker().setMaxDate(maxDate);
                dialog.getDatePicker().setMinDate(minDate);

                dialog.show();

            }
        });

        // set min dates and max dates for date picker dialog to ensure an acceptable age is input
        // uses gregorian calendar
        userBirthdayDatePickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int userBirthdayYear, int userBirthdayMonth, int userBirthdayDayOfMonth) {

                Calendar userAge = new GregorianCalendar(userBirthdayYear,userBirthdayMonth,userBirthdayDayOfMonth);
                Calendar minAdultAge = new GregorianCalendar();
                minAdultAge.add(Calendar.YEAR, -18);

                if (minAdultAge.before(userAge)) {

                    Toast.makeText(UserBirthday.this, "You must be 18 years of age or older", Toast.LENGTH_LONG).show();

                }

                if (minAdultAge.after(userAge)) {

                    userBirthdayMonth = 1 + userBirthdayMonth;
                    String userBirthdayMonthDoubleDigits = ("0" + userBirthdayMonth);
                    String userBirthdayDayOfMonthDoubleDigits = ("0" + userBirthdayDayOfMonth);

                    if (userBirthdayMonth >= 1 && userBirthdayMonth <= 9) {

                        String date = (userBirthdayMonthDoubleDigits + "/" + userBirthdayDayOfMonth + "/" + userBirthdayYear);
                        userBirthdayDateEditText.setText(date);

                    }

                    if (userBirthdayMonth >= 10 && userBirthdayMonth <= 12) {

                        String date = (userBirthdayMonth + "/" + userBirthdayDayOfMonth + "/" + userBirthdayYear);
                        userBirthdayDateEditText.setText(date);

                    }

                    if ((userBirthdayMonth >= 10 && userBirthdayMonth <= 12)
                            && (userBirthdayDayOfMonth >= 1 && userBirthdayDayOfMonth <= 9)) {

                        String date = (userBirthdayMonth + "/" + userBirthdayDayOfMonthDoubleDigits + "/" + userBirthdayYear);
                        userBirthdayDateEditText.setText(date);

                    }

                    if ((userBirthdayMonth >= 1 && userBirthdayMonth <= 9)
                            && (userBirthdayDayOfMonth >= 1 && userBirthdayDayOfMonth <= 9)) {

                        String date = (userBirthdayMonthDoubleDigits + "/" + userBirthdayDayOfMonthDoubleDigits + "/" + userBirthdayYear);
                        userBirthdayDateEditText.setText(date);

                    }

                }

            }

        };

    }

    // clean up date and then save date to database
    // if date is invalid, alert user with a toast message
    private void saveUserBirthday() {

        if (userBirthdayDateEditText.getText().toString().contains("/00/")){

            userBirthdayDateEditText.getText().replace(3, 6, "/01/");

        }

        String birthday = userBirthdayDateEditText.getText().toString();

        if (birthday.length() < 1) {

            Toast.makeText(this, "Please enter your birthday", Toast.LENGTH_LONG).show();

        } else if (birthday.length() >= 2 && birthday.length() <= 9){

            Toast.makeText(this, "Invalid Date", Toast.LENGTH_LONG).show();

        } else if (birthday.length() == 10){

            final UserBirthdayInfo userBirthdayInfo = new UserBirthdayInfo(birthday);

            FirebaseUser user = firebaseAuth.getCurrentUser();

            databaseReference.child("Users").child(user.getUid()).child("Birthday").setValue(userBirthdayInfo);

            Toast.makeText(this, "Birthday Saved", Toast.LENGTH_LONG).show();

        } else if (birthday.length() >= 11){

            Toast.makeText(this, "Invalid Date", Toast.LENGTH_LONG).show();

        }

    }

    @Override
    public void onClick(View view) {

        // user clicked next arrow
        // get birthday text, check if it is valid
        // valid dates contain no letters and are 10 characters in length
        if (view == userBirthdayActivityNextArrow) {

            String birthday = userBirthdayDateEditText.getText().toString();
            birthday = birthday.toLowerCase();

            if (birthday.contains("m") || birthday.contains("d") || birthday.contains("y")) {

                Toast.makeText(this, "Your birthday may not contain any letters", Toast.LENGTH_LONG).show();

            } else if (birthday.length() < 1) {

                Toast.makeText(this, "Please enter your birthday", Toast.LENGTH_LONG).show();

            } else if (birthday.length() >= 2 && birthday.length() < 10){

                Toast.makeText(this, "Invalid Date", Toast.LENGTH_LONG).show();

            } else if (birthday.length() >= 11){

                Toast.makeText(this, "Invalid Date", Toast.LENGTH_LONG).show();

            } else if (birthday.length() == 10){

                saveUserBirthday();
                finish();

            }

        }

    }

}
