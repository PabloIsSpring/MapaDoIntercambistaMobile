package com.example.mapadointercambista.activity.perfil;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.util.TransitionHelper;

public class ConfiguracoesActivity extends AppCompatActivity {

    private static final String PREF_CONFIG = "configuracoes_app";
    private static final String KEY_IDIOMA = "idioma";
    private static final String KEY_NOTIFICACOES = "notificacoes";
    private static final String KEY_CIDADE = "cidade";
    private static final String KEY_ACESSIBILIDADE = "acessibilidade";
    private static final String KEY_PERFIL_VISIVEL = "perfil_visivel";
    private static final String KEY_MOSTRAR_EMAIL = "mostrar_email";
    private static final String KEY_RELOGIN_APP = "relogin_app";
    private static final String KEY_BLOQUEAR_SCREENSHOT = "bloquear_screenshot";

    private TextView textoValorIdioma;
    private TextView textoValorNotificacoes;
    private TextView textoValorLocalizacao;
    private TextView textoValorAcessibilidade;
    private TextView textoValorPrivacidade;
    private TextView textoValorSeguranca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracoes);

        aplicarModoImersivo();
        inicializarViews();
        configurarAcoes();
        atualizarResumoConfiguracoes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
        atualizarResumoConfiguracoes();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            aplicarModoImersivo();
        }
    }

    private void inicializarViews() {
        textoValorIdioma = findViewById(R.id.textoValorIdiomaConfiguracoes);
        textoValorNotificacoes = findViewById(R.id.textoValorNotificacoesConfiguracoes);
        textoValorLocalizacao = findViewById(R.id.textoValorLocalizacaoConfiguracoes);
        textoValorAcessibilidade = findViewById(R.id.textoValorAcessibilidadeConfiguracoes);
        textoValorPrivacidade = findViewById(R.id.textoValorPrivacidadeConfiguracoes);
        textoValorSeguranca = findViewById(R.id.textoValorSegurancaConfiguracoes);
    }

    private void configurarAcoes() {
        findViewById(R.id.botaoVoltarConfiguracoes).setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        findViewById(R.id.itemIdiomaConfiguracoes).setOnClickListener(v -> abrirDialogIdioma());
        findViewById(R.id.itemNotificacoesConfiguracoes).setOnClickListener(v -> abrirDialogNotificacoes());
        findViewById(R.id.itemLocalizacaoConfiguracoes).setOnClickListener(v -> abrirDialogCidade());
        findViewById(R.id.itemAcessibilidadeConfiguracoes).setOnClickListener(v -> abrirDialogAcessibilidade());
        findViewById(R.id.itemPrivacidadeConfiguracoes).setOnClickListener(v -> abrirDialogPrivacidade());
        findViewById(R.id.itemSegurancaConfiguracoes).setOnClickListener(v -> abrirDialogSeguranca());

        findViewById(R.id.itemTermosConfiguracoes).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, TermosUsoActivity.class));
            TransitionHelper.slideForward(this);
        });

        findViewById(R.id.itemSuporteConfiguracoes).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, SuporteActivity.class));
            TransitionHelper.slideForward(this);
        });
    }

    private void atualizarResumoConfiguracoes() {
        textoValorIdioma.setText(getIdiomaSelecionadoLabel());
        textoValorNotificacoes.setText(getNotificacoesSelecionadasLabel());
        textoValorLocalizacao.setText(getCidadeSelecionadaLabel());
        textoValorAcessibilidade.setText(getAcessibilidadeSelecionadaLabel());
        textoValorPrivacidade.setText(getPrivacidadeSelecionadaLabel());
        textoValorSeguranca.setText(getSegurancaSelecionadaLabel());
    }

    private void abrirDialogIdioma() {
        View view = getLayoutInflater().inflate(R.layout.dialog_opcoes_radio, null);

        TextView titulo = view.findViewById(R.id.textoTituloDialogOpcoes);
        RadioButton radioOpcao1 = view.findViewById(R.id.radioOpcao1Dialog);
        RadioButton radioOpcao2 = view.findViewById(R.id.radioOpcao2Dialog);
        RadioButton radioOpcao3 = view.findViewById(R.id.radioOpcao3Dialog);

        titulo.setText("Escolha o idioma");
        radioOpcao1.setText("Português - Brasil");
        radioOpcao2.setText("English");
        radioOpcao3.setText("Español");

        String idiomaAtual = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getString(KEY_IDIOMA, "pt-BR");

        if ("en".equals(idiomaAtual)) {
            radioOpcao2.setChecked(true);
        } else if ("es".equals(idiomaAtual)) {
            radioOpcao3.setChecked(true);
        } else {
            radioOpcao1.setChecked(true);
        }

        View.OnClickListener selecionarUnico = v -> {
            radioOpcao1.setChecked(v == radioOpcao1);
            radioOpcao2.setChecked(v == radioOpcao2);
            radioOpcao3.setChecked(v == radioOpcao3);
        };

        radioOpcao1.setOnClickListener(selecionarUnico);
        radioOpcao2.setOnClickListener(selecionarUnico);
        radioOpcao3.setOnClickListener(selecionarUnico);

        new AlertDialog.Builder(this)
                .setTitle("Idioma")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String valorSelecionado = "pt-BR";

                    if (radioOpcao2.isChecked()) {
                        valorSelecionado = "en";
                    } else if (radioOpcao3.isChecked()) {
                        valorSelecionado = "es";
                    }

                    getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_IDIOMA, valorSelecionado)
                            .apply();

                    atualizarResumoConfiguracoes();
                    Toast.makeText(this, "Idioma salvo com sucesso.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void abrirDialogNotificacoes() {
        View view = getLayoutInflater().inflate(R.layout.dialog_opcoes_radio, null);

        TextView titulo = view.findViewById(R.id.textoTituloDialogOpcoes);
        RadioButton radioOpcao1 = view.findViewById(R.id.radioOpcao1Dialog);
        RadioButton radioOpcao2 = view.findViewById(R.id.radioOpcao2Dialog);
        RadioButton radioOpcao3 = view.findViewById(R.id.radioOpcao3Dialog);

        titulo.setText("Escolha o tipo de notificações");
        radioOpcao1.setText("Todas");
        radioOpcao2.setText("Somente importantes");
        radioOpcao3.setText("Nenhuma");

        String notificacaoAtual = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getString(KEY_NOTIFICACOES, "todas");

        if ("importantes".equals(notificacaoAtual)) {
            radioOpcao2.setChecked(true);
        } else if ("nenhuma".equals(notificacaoAtual)) {
            radioOpcao3.setChecked(true);
        } else {
            radioOpcao1.setChecked(true);
        }

        View.OnClickListener selecionarUnico = v -> {
            radioOpcao1.setChecked(v == radioOpcao1);
            radioOpcao2.setChecked(v == radioOpcao2);
            radioOpcao3.setChecked(v == radioOpcao3);
        };

        radioOpcao1.setOnClickListener(selecionarUnico);
        radioOpcao2.setOnClickListener(selecionarUnico);
        radioOpcao3.setOnClickListener(selecionarUnico);

        new AlertDialog.Builder(this)
                .setTitle("Notificações")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String valorSelecionado = "todas";

                    if (radioOpcao2.isChecked()) {
                        valorSelecionado = "importantes";
                    } else if (radioOpcao3.isChecked()) {
                        valorSelecionado = "nenhuma";
                    }

                    getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_NOTIFICACOES, valorSelecionado)
                            .apply();

                    atualizarResumoConfiguracoes();
                    Toast.makeText(this, "Preferência de notificações salva.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void abrirDialogCidade() {
        View view = getLayoutInflater().inflate(R.layout.dialog_input_simples, null);
        EditText input = view.findViewById(R.id.inputDialogSimples);

        input.setHint("Digite sua cidade");
        input.setText(getSharedPreferences(PREF_CONFIG, MODE_PRIVATE).getString(KEY_CIDADE, ""));

        new AlertDialog.Builder(this)
                .setTitle("Selecionar cidade")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Limpar", (dialog, which) -> {
                    getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                            .edit()
                            .remove(KEY_CIDADE)
                            .apply();

                    atualizarResumoConfiguracoes();
                    Toast.makeText(this, "Cidade removida.", Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String cidade = input.getText().toString().trim();

                    if (cidade.isEmpty()) {
                        Toast.makeText(this, "Digite uma cidade válida.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_CIDADE, cidade)
                            .apply();

                    atualizarResumoConfiguracoes();
                    Toast.makeText(this, "Cidade salva com sucesso.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void abrirDialogAcessibilidade() {
        View view = getLayoutInflater().inflate(R.layout.dialog_opcoes_radio, null);

        TextView titulo = view.findViewById(R.id.textoTituloDialogOpcoes);
        RadioButton radioOpcao1 = view.findViewById(R.id.radioOpcao1Dialog);
        RadioButton radioOpcao2 = view.findViewById(R.id.radioOpcao2Dialog);
        RadioButton radioOpcao3 = view.findViewById(R.id.radioOpcao3Dialog);

        titulo.setText("Escolha o modo de acessibilidade");
        radioOpcao1.setText("Padrão");
        radioOpcao2.setText("Texto ampliado");
        radioOpcao3.setText("Alto contraste");

        String acessibilidadeAtual = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getString(KEY_ACESSIBILIDADE, "padrao");

        if ("texto_ampliado".equals(acessibilidadeAtual)) {
            radioOpcao2.setChecked(true);
        } else if ("alto_contraste".equals(acessibilidadeAtual)) {
            radioOpcao3.setChecked(true);
        } else {
            radioOpcao1.setChecked(true);
        }

        View.OnClickListener selecionarUnico = v -> {
            radioOpcao1.setChecked(v == radioOpcao1);
            radioOpcao2.setChecked(v == radioOpcao2);
            radioOpcao3.setChecked(v == radioOpcao3);
        };

        radioOpcao1.setOnClickListener(selecionarUnico);
        radioOpcao2.setOnClickListener(selecionarUnico);
        radioOpcao3.setOnClickListener(selecionarUnico);

        new AlertDialog.Builder(this)
                .setTitle("Acessibilidade")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String valorSelecionado = "padrao";

                    if (radioOpcao2.isChecked()) {
                        valorSelecionado = "texto_ampliado";
                    } else if (radioOpcao3.isChecked()) {
                        valorSelecionado = "alto_contraste";
                    }

                    getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_ACESSIBILIDADE, valorSelecionado)
                            .apply();

                    atualizarResumoConfiguracoes();
                    Toast.makeText(this, "Preferência de acessibilidade salva.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void abrirDialogPrivacidade() {
        View view = getLayoutInflater().inflate(R.layout.dialog_opcoes_radio, null);

        TextView titulo = view.findViewById(R.id.textoTituloDialogOpcoes);
        RadioButton radioOpcao1 = view.findViewById(R.id.radioOpcao1Dialog);
        RadioButton radioOpcao2 = view.findViewById(R.id.radioOpcao2Dialog);
        RadioButton radioOpcao3 = view.findViewById(R.id.radioOpcao3Dialog);

        titulo.setText("Escolha a visibilidade do perfil");
        radioOpcao1.setText("Perfil visível e mostrar e-mail");
        radioOpcao2.setText("Perfil visível sem mostrar e-mail");
        radioOpcao3.setText("Perfil mais privado");

        boolean perfilVisivel = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_PERFIL_VISIVEL, true);
        boolean mostrarEmail = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_MOSTRAR_EMAIL, false);

        if (!perfilVisivel) {
            radioOpcao3.setChecked(true);
        } else if (mostrarEmail) {
            radioOpcao1.setChecked(true);
        } else {
            radioOpcao2.setChecked(true);
        }

        View.OnClickListener selecionarUnico = v -> {
            radioOpcao1.setChecked(v == radioOpcao1);
            radioOpcao2.setChecked(v == radioOpcao2);
            radioOpcao3.setChecked(v == radioOpcao3);
        };

        radioOpcao1.setOnClickListener(selecionarUnico);
        radioOpcao2.setOnClickListener(selecionarUnico);
        radioOpcao3.setOnClickListener(selecionarUnico);

        new AlertDialog.Builder(this)
                .setTitle("Privacidade")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    boolean novoPerfilVisivel = true;
                    boolean novoMostrarEmail = false;

                    if (radioOpcao1.isChecked()) {
                        novoPerfilVisivel = true;
                        novoMostrarEmail = true;
                    } else if (radioOpcao2.isChecked()) {
                        novoPerfilVisivel = true;
                        novoMostrarEmail = false;
                    } else if (radioOpcao3.isChecked()) {
                        novoPerfilVisivel = false;
                        novoMostrarEmail = false;
                    }

                    getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_PERFIL_VISIVEL, novoPerfilVisivel)
                            .putBoolean(KEY_MOSTRAR_EMAIL, novoMostrarEmail)
                            .apply();

                    atualizarResumoConfiguracoes();
                    Toast.makeText(this, "Preferências de privacidade salvas.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void abrirDialogSeguranca() {
        View view = getLayoutInflater().inflate(R.layout.dialog_opcoes_radio, null);

        TextView titulo = view.findViewById(R.id.textoTituloDialogOpcoes);
        RadioButton radioOpcao1 = view.findViewById(R.id.radioOpcao1Dialog);
        RadioButton radioOpcao2 = view.findViewById(R.id.radioOpcao2Dialog);
        RadioButton radioOpcao3 = view.findViewById(R.id.radioOpcao3Dialog);

        titulo.setText("Escolha o modo de segurança");
        radioOpcao1.setText("Padrão");
        radioOpcao2.setText("Reforçado");
        radioOpcao3.setText("Máximo");

        boolean relogin = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_RELOGIN_APP, false);
        boolean bloquearScreenshot = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_BLOQUEAR_SCREENSHOT, false);

        if (relogin && bloquearScreenshot) {
            radioOpcao3.setChecked(true);
        } else if (relogin) {
            radioOpcao2.setChecked(true);
        } else {
            radioOpcao1.setChecked(true);
        }

        View.OnClickListener selecionarUnico = v -> {
            radioOpcao1.setChecked(v == radioOpcao1);
            radioOpcao2.setChecked(v == radioOpcao2);
            radioOpcao3.setChecked(v == radioOpcao3);
        };

        radioOpcao1.setOnClickListener(selecionarUnico);
        radioOpcao2.setOnClickListener(selecionarUnico);
        radioOpcao3.setOnClickListener(selecionarUnico);

        new AlertDialog.Builder(this)
                .setTitle("Segurança")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    boolean novoRelogin = false;
                    boolean novoBloquearScreenshot = false;

                    if (radioOpcao2.isChecked()) {
                        novoRelogin = true;
                    } else if (radioOpcao3.isChecked()) {
                        novoRelogin = true;
                        novoBloquearScreenshot = true;
                    }

                    getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_RELOGIN_APP, novoRelogin)
                            .putBoolean(KEY_BLOQUEAR_SCREENSHOT, novoBloquearScreenshot)
                            .apply();

                    atualizarResumoConfiguracoes();
                    Toast.makeText(this, "Preferências de segurança salvas.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private String getIdiomaSelecionadoLabel() {
        String idioma = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getString(KEY_IDIOMA, "pt-BR");

        switch (idioma) {
            case "en":
                return "English";
            case "es":
                return "Español";
            case "pt-BR":
            default:
                return "Português - Brasil";
        }
    }

    private String getNotificacoesSelecionadasLabel() {
        String notificacoes = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getString(KEY_NOTIFICACOES, "todas");

        switch (notificacoes) {
            case "importantes":
                return "Somente importantes";
            case "nenhuma":
                return "Nenhuma";
            case "todas":
            default:
                return "Todas";
        }
    }

    private String getCidadeSelecionadaLabel() {
        String cidade = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getString(KEY_CIDADE, "");

        return cidade == null || cidade.trim().isEmpty() ? "Não definida" : cidade;
    }

    private String getAcessibilidadeSelecionadaLabel() {
        String acessibilidade = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getString(KEY_ACESSIBILIDADE, "padrao");

        switch (acessibilidade) {
            case "texto_ampliado":
                return "Texto ampliado";
            case "alto_contraste":
                return "Alto contraste";
            case "padrao":
            default:
                return "Padrão";
        }
    }

    private String getPrivacidadeSelecionadaLabel() {
        boolean perfilVisivel = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_PERFIL_VISIVEL, true);
        boolean mostrarEmail = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_MOSTRAR_EMAIL, false);

        if (!perfilVisivel) {
            return "Perfil mais privado";
        }

        if (mostrarEmail) {
            return "Perfil visível com e-mail";
        }

        return "Perfil visível sem e-mail";
    }

    private String getSegurancaSelecionadaLabel() {
        boolean relogin = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_RELOGIN_APP, false);
        boolean bloquearScreenshot = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
                .getBoolean(KEY_BLOQUEAR_SCREENSHOT, false);

        if (relogin && bloquearScreenshot) {
            return "Máximo";
        }

        if (relogin) {
            return "Reforçado";
        }

        return "Padrão";
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