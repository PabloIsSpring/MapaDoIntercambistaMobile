package com.example.mapadointercambista.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public final class AnimationUtils {

    private AnimationUtils() {
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void applyPressAnimation(View view) {
        if (view == null) return;

        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .alpha(0.96f)
                            .setDuration(90)
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(110)
                            .start();
                    break;
            }
            return false;
        });
    }

    public static void playBounce(View view) {
        if (view == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.88f, 1.08f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.88f, 1.08f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(240);
        set.setInterpolator(new OvershootInterpolator(1.1f));
        set.start();
    }

    public static void fadeIn(View view) {
        if (view == null) return;

        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(180)
                .start();
    }
}