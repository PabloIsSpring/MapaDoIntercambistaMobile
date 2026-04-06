package com.example.mapadointercambista.navigation;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.destinos.DestinosActivity;
import com.example.mapadointercambista.activity.forum.ForumActivity;
import com.example.mapadointercambista.activity.main.MainActivity;
import com.example.mapadointercambista.activity.perfil.ContaActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    public static void configurarBottomNavigation(Activity activity,
                                                  BottomNavigationView bottomNav,
                                                  int itemSelecionado) {

        if (bottomNav == null) {
            return;
        }

        aplicarInsetsNoBottomNavigation(bottomNav);

        bottomNav.setSelectedItemId(itemSelecionado);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == itemSelecionado) {
                return true;
            }

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
            } else if (itemId == R.id.nav_forum) {
                intent = new Intent(activity, ForumActivity.class);
            } else if (itemId == R.id.nav_mundo) {
                intent = new Intent(activity, DestinosActivity.class);
            } else if (itemId == R.id.nav_perfil) {
                intent = new Intent(activity, ContaActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            }

            return false;
        });
    }

    private static void aplicarInsetsNoBottomNavigation(BottomNavigationView bottomNav) {
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int top = view.getPaddingTop();
            int left = view.getPaddingLeft();
            int right = view.getPaddingRight();
            int baseBottom = dpToPx(view, 6);

            view.setPadding(left, top, right, baseBottom + systemBars.bottom);
            return insets;
        });
    }

    private static int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}