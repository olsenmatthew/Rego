package com.reylo.rego.Utils;

import static android.text.TextUtils.isEmpty;


// removes LineFeedChars and CarriageReturnChars
public class ChompString {

    public static final char LineFeedChar = '\n';
    public static final char CarriageReturnChar = '\r';

    public static String Chomp (String input) {

        if (isEmpty(input) || input.equals("")) {

            return input;

        }

        if (input.length() == 1) {

            char firstChar = input.charAt(0);

            if (firstChar == LineFeedChar || firstChar == CarriageReturnChar) {

                return "";

            }

            return input;

        }

        int lastIndex = (input.length() - 1);

        char lastChar = input.charAt(lastIndex);

        if (lastChar == LineFeedChar) {

            if (input.charAt(lastIndex - 1) == CarriageReturnChar) {

                lastIndex--;

            }

        } else if (lastChar != CarriageReturnChar) {

            lastIndex++;

        }

        return input.substring(0, lastIndex);

    }

}
