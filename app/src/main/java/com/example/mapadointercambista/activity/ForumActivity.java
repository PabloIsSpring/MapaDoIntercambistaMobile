package com.example.mapadointercambista.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.PostForumAdapter;
import com.example.mapadointercambista.model.ForumStorage;
import com.example.mapadointercambista.model.NavigationHelper;
import com.example.mapadointercambista.model.PostForum;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ForumActivity extends AppCompatActivity {

    RecyclerView listaTodosForuns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        listaTodosForuns = findViewById(R.id.listaTodosForuns);
        listaTodosForuns.setLayoutManager(new LinearLayoutManager(this));

        ForumStorage forumStorage = new ForumStorage(this);
        List<PostForum> posts = forumStorage.carregarPosts();

        if (posts.isEmpty()) {
            posts = new ArrayList<>();

            posts.add(new PostForum(
                    "Associação MarkitoLivre",
                    "Gostei hein, top demais",
                    R.drawable.logo,
                    21,
                    3,
                    "há 2h",
                    8
            ));

            posts.add(new PostForum(
                    "Juninho Mandelão",
                    "Show de bola esse aplicativo!",
                    R.drawable.logo,
                    5,
                    0,
                    "há 5h",
                    3
            ));

            posts.add(new PostForum(
                    "XD",
                    "Aplicativo ficou muito bom!",
                    R.drawable.logo,
                    15,
                    1,
                    "há 8h",
                    6
            ));

            forumStorage.salvarPosts(posts);
        }

        PostForumAdapter adapter = new PostForumAdapter(posts);
        listaTodosForuns.setAdapter(adapter);

        findViewById(R.id.botaoNovoForum).setOnClickListener(v ->
                Toast.makeText(this, "Funcionalidade de novo post virá depois", Toast.LENGTH_SHORT).show()
        );

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }
}