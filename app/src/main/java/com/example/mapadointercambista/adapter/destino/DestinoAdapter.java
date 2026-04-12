package com.example.mapadointercambista.adapter.destino;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.auth.LoginActivity;
import com.example.mapadointercambista.activity.destinos.DetalheDestinoActivity;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.FavoritosStorage;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.AnimationUtils;
import com.example.mapadointercambista.util.ImageUtils;

import java.util.List;
import java.util.Locale;

public class DestinoAdapter extends RecyclerView.Adapter<DestinoAdapter.ViewHolder> {

    private final Context context;
    private final List<Destino> lista;
    private final SparseArray<Integer> imageCache = new SparseArray<>();
    private final SessionManager sessionManager;
    private final FavoritosStorage favoritosStorage;

    public DestinoAdapter(Context context, List<Destino> lista) {
        this.context = context;
        this.lista = lista;
        this.sessionManager = new SessionManager(context);
        this.favoritosStorage = new FavoritosStorage(context);
        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagem;
        ImageView iconeFavorito;
        TextView nome;
        TextView subinfo;
        RatingBar rating;
        TextView reviews;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagem = itemView.findViewById(R.id.imagemDestino);
            iconeFavorito = itemView.findViewById(R.id.iconeFavoritoDestino);
            nome = itemView.findViewById(R.id.textoDestino);
            subinfo = itemView.findViewById(R.id.textoSubinfoDestino);
            rating = itemView.findViewById(R.id.ratingDestino);
            reviews = itemView.findViewById(R.id.textoReviews);
        }
    }

    @Override
    public long getItemId(int position) {
        String id = lista.get(position).getId();
        return id != null ? id.hashCode() : position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destino, parent, false);

        AnimationUtils.applyPressAnimation(view);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destino destino = lista.get(position);

        holder.nome.setText(destino.getNome());
        holder.subinfo.setText(montarSubinfo(destino));
        holder.rating.setRating(destino.getNota());
        holder.reviews.setText(String.format(
                Locale.getDefault(),
                "%.1f (%d avaliações)",
                destino.getNota(),
                destino.getAvaliacoes()
        ));

        String imagemNome = destino.getImagemNome();
        int cacheKey = imagemNome != null ? imagemNome.hashCode() : 0;

        Integer drawableId = imageCache.get(cacheKey);
        if (drawableId == null) {
            int resolvedId = ImageUtils.getDrawableId(context, imagemNome);

            if (resolvedId == 0) {
                resolvedId = R.drawable.ic_world;
            }

            imageCache.put(cacheKey, resolvedId);
            drawableId = resolvedId;
        }

        holder.imagem.setImageResource(drawableId);

        boolean logado = sessionManager.estaLogado();
        String emailUsuario = sessionManager.getEmailUsuario();
        boolean favorito = logado && favoritosStorage.isFavorito(emailUsuario, destino.getId());

        atualizarIconeFavorito(holder, favorito);

        holder.iconeFavorito.setContentDescription(
                favorito ? "Remover dos favoritos" : "Adicionar aos favoritos"
        );

        holder.iconeFavorito.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(context, "Entre em sua conta para favoritar", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, LoginActivity.class));
                return;
            }

            boolean agoraFavorito = favoritosStorage.toggleFavorito(
                    sessionManager.getEmailUsuario(),
                    destino.getId()
            );

            atualizarIconeFavorito(holder, agoraFavorito);
            holder.iconeFavorito.setContentDescription(
                    agoraFavorito ? "Remover dos favoritos" : "Adicionar aos favoritos"
            );
            AnimationUtils.playBounce(holder.iconeFavorito);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalheDestinoActivity.class);
            intent.putExtra("destino", destino);
            context.startActivity(intent);
        });
    }

    private void atualizarIconeFavorito(@NonNull ViewHolder holder, boolean favorito) {
        holder.iconeFavorito.setColorFilter(ContextCompat.getColor(
                context,
                favorito ? R.color.favorite_active : R.color.white
        ));
        holder.iconeFavorito.setAlpha(favorito ? 1f : 0.92f);
    }

    private String montarSubinfo(Destino destino) {
        String pais = safe(destino.getPais());
        String idioma = safe(destino.getIdioma());

        if (!pais.isEmpty() && !idioma.isEmpty()) {
            return pais + " • " + idioma;
        }

        if (!pais.isEmpty()) {
            return pais;
        }

        if (!idioma.isEmpty()) {
            return idioma;
        }

        return "Destino internacional";
    }

    private String safe(String valor) {
        return valor == null ? "" : valor.trim();
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }
}