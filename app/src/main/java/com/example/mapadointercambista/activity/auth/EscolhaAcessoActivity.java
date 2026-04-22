package com.example.mapadointercambista.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.util.TransitionHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class EscolhaAcessoActivity extends AppCompatActivity {

    private static final int TIPO_USUARIO = 1;
    private static final int TIPO_AGENCIA = 2;

    private int tipoSelecionado = TIPO_USUARIO;

    private MaterialCardView cardOpcaoUsuario;
    private MaterialCardView cardOpcaoAgencia;
    private MaterialButton botaoEntrar;
    private View textoCriarConta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_escolha_acesso);

        aplicarModoImersivo();

        cardOpcaoUsuario = findViewById(R.id.cardOpcaoUsuario);
        cardOpcaoAgencia = findViewById(R.id.cardOpcaoAgencia);
        botaoEntrar = findViewById(R.id.botaoEntrarEscolhaAcesso);
        textoCriarConta = findViewById(R.id.textoCriarContaEscolhaAcesso);

        findViewById(R.id.botaoVoltarEscolhaAcesso).setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        cardOpcaoUsuario.setOnClickListener(v -> {
            tipoSelecionado = TIPO_USUARIO;
            atualizarSelecao();
        });

        cardOpcaoAgencia.setOnClickListener(v -> {
            tipoSelecionado = TIPO_AGENCIA;
            atualizarSelecao();
        });

        botaoEntrar.setOnClickListener(v -> abrirTelaEntrar());
        textoCriarConta.setOnClickListener(v -> abrirTelaCadastro());

        atualizarSelecao();
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

    private void atualizarSelecao() {
        if (tipoSelecionado == TIPO_USUARIO) {
            cardOpcaoUsuario.setCardBackgroundColor(getColor(R.color.card_background));
            cardOpcaoUsuario.setStrokeColor(getColor(R.color.brand_blue));
            cardOpcaoUsuario.setStrokeWidth(dpToPx(2));

            cardOpcaoAgencia.setCardBackgroundColor(getColor(R.color.card_background));
            cardOpcaoAgencia.setStrokeColor(getColor(R.color.divider_color));
            cardOpcaoAgencia.setStrokeWidth(dpToPx(1));
        } else {
            cardOpcaoAgencia.setCardBackgroundColor(getColor(R.color.card_background));
            cardOpcaoAgencia.setStrokeColor(getColor(R.color.brand_blue));
            cardOpcaoAgencia.setStrokeWidth(dpToPx(2));

            cardOpcaoUsuario.setCardBackgroundColor(getColor(R.color.card_background));
            cardOpcaoUsuario.setStrokeColor(getColor(R.color.divider_color));
            cardOpcaoUsuario.setStrokeWidth(dpToPx(1));
        }
    }

    private void abrirTelaEntrar() {
        Intent intent;

        if (tipoSelecionado == TIPO_AGENCIA) {
            intent = new Intent(this, LoginAgenciaActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        TransitionHelper.slideForward(this);
    }

    private void abrirTelaCadastro() {
        Intent intent;

        if (tipoSelecionado == TIPO_AGENCIA) {
            intent = new Intent(this, CadastroAgenciaActivity.class);
        } else {
            intent = new Intent(this, CadastroActivity.class);
        }

        startActivity(intent);
        TransitionHelper.slideForward(this);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}