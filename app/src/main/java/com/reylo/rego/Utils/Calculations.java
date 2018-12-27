package com.reylo.rego.Utils;

import java.util.Calendar;

// Contains functions which perform calculations
public class Calculations {

    // compute the age from the date
    public static int AgeFromDate(int year, int month, int day) {

        int uAge = 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        uAge = (int) age;

        return uAge;

    }

}
