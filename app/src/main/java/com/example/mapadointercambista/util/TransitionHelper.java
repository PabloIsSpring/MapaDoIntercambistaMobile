package com.example.mapadointercambista.util;

import android.app.Activity;

import com.example.mapadointercambista.R;

public final class TransitionHelper {

    private TransitionHelper() {
    }

    public static void fade(Activity activity) {
        if (activity == null) return;
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void slideForward(Activity activity) {
        if (activity == null) return;
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void slideBack(Activity activity) {
        if (activity == null) return;
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}