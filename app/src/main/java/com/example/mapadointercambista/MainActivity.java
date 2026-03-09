package com.example.mapadointercambista;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;


public class MainActivity extends AppCompatActivity {
    Handler handler = new Handler();
    Runnable runnable;
    ViewPager2 carrossel;

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
        carrossel = findViewById(R.id.carrossel);

        int[] imagens = {
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        };

        CarrosselAdapter adapter = new CarrosselAdapter(imagens);
        carrossel.setAdapter(adapter);

        ImageView setaEsquerda = findViewById(R.id.setaEsquerda);
        ImageView setaDireita = findViewById(R.id.setaDireita);

        setaDireita.setOnClickListener(v -> {

            int current = carrossel.getCurrentItem();
            int total = carrossel.getAdapter().getItemCount();

            if(current == total - 1){
                carrossel.setCurrentItem(0);
            }else{
                carrossel.setCurrentItem(current + 1);
            }

        });

        setaEsquerda.setOnClickListener(v -> {

            int current = carrossel.getCurrentItem();
            int total = carrossel.getAdapter().getItemCount();

            if(current == 0){
                carrossel.setCurrentItem(total - 1);
            }else{
                carrossel.setCurrentItem(current - 1);
            }

        });

        iniciarAutoSlide();
    }

    private void iniciarAutoSlide(){

        runnable = new Runnable() {
            @Override
            public void run() {

                int current = carrossel.getCurrentItem();
                int total = carrossel.getAdapter().getItemCount();

                if(current == total - 1){

                    // volta para o início sem animação
                    carrossel.setCurrentItem(0, false);

                }else{

                    // avança normal
                    carrossel.setCurrentItem(current + 1, true);

                }

                handler.postDelayed(this, 4000);
            }
        };

        handler.postDelayed(runnable, 4000);
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
        handler.postDelayed(runnable, 4000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("Ciclo de vida", "onPause()chamado");
        handler.removeCallbacks(runnable);
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