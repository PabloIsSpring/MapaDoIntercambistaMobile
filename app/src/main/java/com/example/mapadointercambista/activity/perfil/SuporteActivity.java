package com.example.mapadointercambista.activity.perfil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.util.TransitionHelper;

public class SuporteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suporte);

        aplicarModoImersivo();

        findViewById(R.id.botaoVoltarSuporte).setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        findViewById(R.id.itemEmailSuporte).setOnClickListener(v -> abrirEmailSuporte());
        findViewById(R.id.itemFaqSuporte).setOnClickListener(v ->
                Toast.makeText(this, "Central de ajuda completa será expandida em breve.", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.itemReportarProblemaSuporte).setOnClickListener(v ->
                Toast.makeText(this, "Canal de reporte será integrado em breve.", Toast.LENGTH_SHORT).show()
        );
    }

    private void abrirEmailSuporte() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:suporte@mapadointercambista.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Suporte - Mapa do Intercambista");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o aplicativo de e-mail.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            aplicarModoImersivo();
        }
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
}