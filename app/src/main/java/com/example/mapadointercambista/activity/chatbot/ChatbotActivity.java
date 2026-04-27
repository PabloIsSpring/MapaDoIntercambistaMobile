package com.example.mapadointercambista.activity.chatbot;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.chatbot.ChatbotAdapter;
import com.example.mapadointercambista.model.chatbot.ChatbotKnowledgeBase;
import com.example.mapadointercambista.model.chatbot.ChatbotMessage;
import com.example.mapadointercambista.model.chatbot.ChatbotResponse;
import com.example.mapadointercambista.util.AnimationUtils;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private final List<ChatbotMessage> mensagens = new ArrayList<>();

    private RecyclerView listaMensagens;
    private EditText inputMensagem;
    private FloatingActionButton botaoEnviar;
    private LinearLayout containerPerguntasRapidas;
    private static final int MAX_PERGUNTA = 180;
    private String ultimaPergunta = "";
    private ChatbotAdapter adapter;
    private ChatbotKnowledgeBase knowledgeBase;
    private static final String PREF_CHATBOT = "pref_chatbot";
    private static final String KEY_HISTORICO = "historico_chatbot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatbot);

        aplicarModoImersivo();

        knowledgeBase = new ChatbotKnowledgeBase();

        listaMensagens = findViewById(R.id.listaMensagensChatbot);
        inputMensagem = findViewById(R.id.inputMensagemChatbot);
        botaoEnviar = findViewById(R.id.botaoEnviarChatbot);
        containerPerguntasRapidas = findViewById(R.id.containerPerguntasRapidas);

        findViewById(R.id.botaoVoltarChatbot).setOnClickListener(v -> finish());

        configurarLista();
        configurarPerguntasRapidas();
        configurarAcoes();

        carregarHistorico();

        if (mensagens.isEmpty()) {
            adicionarMensagemBot("Olá! Sou o assistente de suporte do Mapa do Intercambista. Posso ajudar com login, cadastro, perfil, fórum, destinos, agência e suporte.");
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

    private void configurarLista() {
        adapter = new ChatbotAdapter(mensagens);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);

        listaMensagens.setLayoutManager(manager);
        listaMensagens.setAdapter(adapter);
        listaMensagens.setItemAnimator(null);
        listaMensagens.setItemViewCacheSize(20);
    }

    private void configurarPerguntasRapidas() {
        containerPerguntasRapidas.removeAllViews();

        for (String pergunta : knowledgeBase.perguntasRapidas()) {
            TextView chip = new TextView(this);
            chip.setText(pergunta);
            chip.setTextSize(13);
            chip.setTextColor(getColor(R.color.brand_blue));
            chip.setBackgroundResource(R.drawable.bg_filter_chip);
            chip.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, dpToPx(8), dpToPx(8));
            chip.setLayoutParams(params);

            AnimationUtils.applyPressAnimation(chip);

            chip.setOnClickListener(v -> enviarPergunta(pergunta));

            containerPerguntasRapidas.addView(chip);
        }
    }

    private void configurarAcoes() {
        AnimationUtils.applyPressAnimation(botaoEnviar);

        findViewById(R.id.botaoLimparChatbot).setOnClickListener(v -> limparConversa());

        botaoEnviar.setOnClickListener(v -> enviarPerguntaDigitada());

        inputMensagem.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                enviarPerguntaDigitada();
                return true;
            }
            return false;
        });
    }

    private void enviarPerguntaDigitada() {
        String pergunta = inputMensagem.getText().toString();
        pergunta = InputSecurityUtils.sanitizeUserText(pergunta);

        if (pergunta.trim().isEmpty()) {
            return;
        }

        if (pergunta.length() > MAX_PERGUNTA) {
            pergunta = pergunta.substring(0, MAX_PERGUNTA);
        }

        if (pergunta.equalsIgnoreCase(ultimaPergunta)) {
            adicionarMensagemBot("Você já perguntou isso agora há pouco 🤔 Tente reformular a pergunta para eu te ajudar melhor.");
            return;
        }

        pergunta = pergunta.replaceAll("(?i)<script.*?>.*?</script>", "");
        pergunta = pergunta.replaceAll("(?i)javascript:", "");

        ultimaPergunta = pergunta;

        inputMensagem.setText("");
        inputMensagem.clearFocus();

        enviarPergunta(pergunta);
    }

    private void enviarPergunta(String pergunta) {
        adicionarMensagemUsuario(pergunta);

        mensagens.add(new ChatbotMessage("...", false, true));
        int posicaoDigitando = mensagens.size() - 1;
        adapter.notifyItemInserted(posicaoDigitando);
        rolarParaFim();

        listaMensagens.postDelayed(() -> {
            if (!mensagens.isEmpty()) {
                int lastIndex = mensagens.size() - 1;

                if (mensagens.get(lastIndex).isDigitando()) {
                    mensagens.remove(lastIndex);
                    adapter.notifyItemRemoved(lastIndex);
                }
            }

            ChatbotResponse resposta = knowledgeBase.responderDetalhado(pergunta);
            adicionarMensagemBot(resposta.getResposta());
            atualizarPerguntasRapidas(resposta.getSugestoes());
        }, 650);
    }

    private void atualizarPerguntasRapidas(String[] sugestoes) {
        if (sugestoes == null || sugestoes.length == 0) {
            configurarPerguntasRapidas();
            return;
        }

        containerPerguntasRapidas.removeAllViews();

        for (String pergunta : sugestoes) {
            TextView chip = new TextView(this);
            chip.setText(pergunta);
            chip.setTextSize(13);
            chip.setTextColor(getColor(R.color.brand_blue));
            chip.setBackgroundResource(R.drawable.bg_filter_chip);
            chip.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, dpToPx(8), dpToPx(8));
            chip.setLayoutParams(params);

            AnimationUtils.applyPressAnimation(chip);
            chip.setOnClickListener(v -> enviarPergunta(pergunta));

            containerPerguntasRapidas.addView(chip);
        }
    }

    private void adicionarMensagemUsuario(String texto) {
        mensagens.add(new ChatbotMessage(texto, true));
        adapter.notifyItemInserted(mensagens.size() - 1);
        salvarHistorico();
        rolarParaFim();
    }

    private void adicionarMensagemBot(String texto) {
        mensagens.add(new ChatbotMessage(texto, false));
        adapter.notifyItemInserted(mensagens.size() - 1);
        salvarHistorico();
        rolarParaFim();
    }

    private void rolarParaFim() {
        listaMensagens.post(() -> {
            if (!mensagens.isEmpty()) {
                listaMensagens.scrollToPosition(mensagens.size() - 1);
            }
        });
    }

    private void limparConversa() {
        mensagens.clear();
        adapter.notifyDataSetChanged();

        getSharedPreferences(PREF_CHATBOT, MODE_PRIVATE)
                .edit()
                .remove(KEY_HISTORICO)
                .apply();

        adicionarMensagemBot("Conversa limpa. Como posso ajudar agora?");
        configurarPerguntasRapidas();
    }

    private void salvarHistorico() {
        StringBuilder builder = new StringBuilder();

        for (ChatbotMessage mensagem : mensagens) {
            if (mensagem.isDigitando()) continue;

            builder.append(mensagem.isUsuario() ? "U|" : "B|")
                    .append(mensagem.getTexto().replace("\n", "\\n"))
                    .append("\n");
        }

        getSharedPreferences(PREF_CHATBOT, MODE_PRIVATE)
                .edit()
                .putString(KEY_HISTORICO, builder.toString())
                .apply();
    }

    private void carregarHistorico() {
        SharedPreferences prefs = getSharedPreferences(PREF_CHATBOT, MODE_PRIVATE);
        String historico = prefs.getString(KEY_HISTORICO, "");

        if (historico == null || historico.trim().isEmpty()) {
            return;
        }

        mensagens.clear();

        String[] linhas = historico.split("\n");

        for (String linha : linhas) {
            if (linha.length() < 3) continue;

            boolean usuario = linha.startsWith("U|");
            String texto = linha.substring(2).replace("\\n", "\n");

            mensagens.add(new ChatbotMessage(texto, usuario));
        }

        adapter.notifyDataSetChanged();
        rolarParaFim();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }
}