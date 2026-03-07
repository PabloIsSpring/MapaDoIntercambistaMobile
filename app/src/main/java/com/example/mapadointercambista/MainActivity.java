package com.example.mapadointercambista;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(savedInstanceState != null){
            // Restaura os valores do estado salvo
            int v1 = savedInstanceState.getInt("valor1");
            int v2 = savedInstanceState.getInt("valor1");
        }
        else{
            // Provavelmente inicializa as variáveis com valores
        }

        Log.d("Ciclo de vida", "onCreate()chamado");

        // Carrossel
        ViewPager2 carrossel = findViewById(R.id.carrossel);

        int[] imagens = {
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        };

        CarrosselAdapter adapter = new CarrosselAdapter(imagens);
        carrossel.setAdapter(adapter);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("Ciclo de vida", "onStart()chamado");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d("Ciclo de vida", "onRestart()chamado");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("Ciclo de vida", "onResume()chamado");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("Ciclo de vida", "onPause()chamado");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("Ciclo de vida", "onStop()chamado");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("Ciclo de vida", "onDestroy()chamado");
    }

    @Override
    public void onSaveInstanceState(Bundle savedIntanceState){
        // Salva o estado atual do jogador
        savedIntanceState.putInt("valor1", 1000); // Forma de declarar variável
        savedIntanceState.putInt("valor2", 55);
        //Invoca a super classe, para que seja possível salvar o estado
        super.onSaveInstanceState(savedIntanceState);
    }
}