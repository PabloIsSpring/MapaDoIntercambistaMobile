package com.example.mapadointercambista.adapter.chatbot;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.model.chatbot.ChatbotMessage;

import java.util.List;

public class ChatbotAdapter extends RecyclerView.Adapter<ChatbotAdapter.ViewHolder> {

    private final List<ChatbotMessage> mensagens;

    public ChatbotAdapter(List<ChatbotMessage> mensagens) {
        this.mensagens = mensagens;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mensagem_chatbot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatbotMessage mensagem = mensagens.get(position);

        if (mensagem.isDigitando()) {
            holder.containerMensagem.setGravity(Gravity.START);
            holder.textoMensagem.setBackgroundResource(R.drawable.bg_chatbot_balao_bot);
            holder.textoMensagem.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
            holder.textoMensagem.setText("Digitando...");
            return;
        }

        holder.textoMensagem.setText(mensagem.getTexto());

        if (mensagem.isUsuario()) {
            holder.containerMensagem.setGravity(Gravity.END);
            holder.textoMensagem.setBackgroundResource(R.drawable.bg_chatbot_balao_usuario);
            holder.textoMensagem.setTextColor(holder.itemView.getContext().getColor(R.color.white));
        } else {
            holder.containerMensagem.setGravity(Gravity.START);
            holder.textoMensagem.setBackgroundResource(R.drawable.bg_chatbot_balao_bot);
            holder.textoMensagem.setTextColor(holder.itemView.getContext().getColor(R.color.text_primary));
        }
    }

    @Override
    public int getItemCount() {
        return mensagens != null ? mensagens.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerMensagem;
        TextView textoMensagem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            containerMensagem = itemView.findViewById(R.id.containerMensagem);
            textoMensagem = itemView.findViewById(R.id.textoMensagemChatbot);
        }
    }
}