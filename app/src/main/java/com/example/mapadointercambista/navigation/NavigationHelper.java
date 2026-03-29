package com.example.mapadointercambista.navigation;

import android.app.Activity;
import android.content.Intent;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.auth.LoginActivity;
import com.example.mapadointercambista.activity.destinos.DestinosActivity;
import com.example.mapadointercambista.activity.forum.ForumActivity;
import com.example.mapadointercambista.activity.main.MainActivity;
import com.example.mapadointercambista.activity.perfil.ContaActivity;
import com.example.mapadointercambista.model.user.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    public static void configurarBottomNavigation(Activity activity,
                                                  BottomNavigationView bottomNav,
                                                  int itemSelecionado) {

        SessionManager sessionManager = new SessionManager(activity);

        bottomNav.setSelectedItemId(itemSelecionado);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == itemSelecionado) {
                return true;
            }

            Intent intent = null;
            boolean deveFinalizarTelaAtual = true;

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
                    deveFinalizarTelaAtual = false;
                }
            }

            if (intent != null) {
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);

                if (deveFinalizarTelaAtual) {
                    activity.finish();
                }

                return true;
            }

            return false;
        });
    }
}