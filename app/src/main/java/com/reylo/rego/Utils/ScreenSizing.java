package com.reylo.rego.Utils;

import android.content.res.Resources;

public class ScreenSizing {

    public static int getScreenWidth() {

        return Resources.getSystem().getDisplayMetrics().widthPixels;

    }

    public static int getScreenHeight() {

        return Resources.getSystem().getDisplayMetrics().heightPixels;

    }

    public static int dpToPx(int dp) {

        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);

    }

    public static int pxToDp(int px) {

        return (int) (px / Resources.getSystem().getDisplayMetrics().density);

    }

}
