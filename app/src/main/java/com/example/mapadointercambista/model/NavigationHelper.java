package com.example.mapadointercambista.model;

import android.app.Activity;
import android.content.Intent;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.ContaActivity;
import com.example.mapadointercambista.activity.DestinosActivity;
import com.example.mapadointercambista.activity.ForumActivity;
import com.example.mapadointercambista.activity.LoginActivity;
import com.example.mapadointercambista.activity.MainActivity;
import com.example.mapadointercambista.model.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    public static void configurarBottomNavigation(Activity activity,
                                                  BottomNavigationView bottomNav,
                                                  int itemSelecionado) {

        SessionManager sessionManager = new SessionManager(activity);

        bottomNav.setSelectedItemId(itemSelecionado);
        atualizarIconePerfil(bottomNav, sessionManager);

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
                if (sessionManager.estaLogado()) {
                    intent = new Intent(activity, ContaActivity.class);
                } else {
                    intent = new Intent(activity, LoginActivity.class);
                }
            }

            if (intent != null) {
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            }

            return false;
        });
    }

    private static void atualizarIconePerfil(BottomNavigationView bottomNav,
                                             SessionManager sessionManager) {
        if (sessionManager.estaLogado()) {
            bottomNav.getMenu().findItem(R.id.nav_perfil).setIcon(R.drawable.ic_user);
        } else {
            bottomNav.getMenu().findItem(R.id.nav_perfil).setIcon(R.drawable.ic_user);
        }
    }
}