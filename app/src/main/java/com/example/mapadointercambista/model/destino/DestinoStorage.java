package com.example.mapadointercambista.model.destino;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DestinoStorage {

    private static final String PREF_NAME = "destino_storage";
    private static final String KEY_DESTINOS = "destinos";

    private final SharedPreferences prefs;
    private final Gson gson;

    public DestinoStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void salvarDestinos(List<Destino> destinos) {
        prefs.edit().putString(KEY_DESTINOS, gson.toJson(destinos)).apply();
    }

    public List<Destino> carregarDestinos() {
        String json = prefs.getString(KEY_DESTINOS, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Destino>>() {}.getType();
        List<Destino> destinos = gson.fromJson(json, type);

        return destinos != null ? destinos : new ArrayList<>();
    }

    public Destino buscarDestinoPorId(String destinoId) {
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                return destino;
            }
        }

        return null;
    }

    public boolean adicionarAvaliacao(String destinoId, AvaliacaoDestino novaAvaliacao) {
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                List<AvaliacaoDestino> avaliacoes = destino.getListaAvaliacoes();
                if (avaliacoes == null) {
                    avaliacoes = new ArrayList<>();
                }

                avaliacoes.add(0, novaAvaliacao);
                destino.setListaAvaliacoes(avaliacoes);
                destino.recalcularResumoAvaliacoes();
                salvarDestinos(destinos);
                return true;
            }
        }

        return false;
    }

    public boolean editarAvaliacao(String destinoId, String avaliacaoId, String novaMensagem,
                                   float novaNota, String novaAgencia) {
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                for (AvaliacaoDestino avaliacao : destino.getListaAvaliacoes()) {
                    if (avaliacaoId != null && avaliacaoId.equals(avaliacao.getId())) {
                        avaliacao.setMensagem(novaMensagem);
                        avaliacao.setNotaEstrelas(novaNota);
                        avaliacao.setAgenciaEscolhida(novaAgencia);
                        destino.recalcularResumoAvaliacoes();
                        salvarDestinos(destinos);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean excluirAvaliacao(String destinoId, String avaliacaoId) {
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                List<AvaliacaoDestino> avaliacoes = destino.getListaAvaliacoes();

                for (int i = 0; i < avaliacoes.size(); i++) {
                    if (avaliacaoId != null && avaliacaoId.equals(avaliacoes.get(i).getId())) {
                        avaliacoes.remove(i);
                        destino.setListaAvaliacoes(avaliacoes);
                        destino.recalcularResumoAvaliacoes();
                        salvarDestinos(destinos);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean toggleLikeAvaliacao(String destinoId, String avaliacaoId, String emailUsuario) {
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                for (AvaliacaoDestino avaliacao : destino.getListaAvaliacoes()) {
                    if (avaliacaoId != null && avaliacaoId.equals(avaliacao.getId())) {
                        List<String> likes = avaliacao.getUsuariosLike();
                        List<String> dislikes = avaliacao.getUsuariosDislike();

                        if (likes.contains(emailUsuario)) {
                            likes.remove(emailUsuario);
                        } else {
                            likes.add(emailUsuario);
                            dislikes.remove(emailUsuario);
                        }

                        salvarDestinos(destinos);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean toggleDislikeAvaliacao(String destinoId, String avaliacaoId, String emailUsuario) {
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                for (AvaliacaoDestino avaliacao : destino.getListaAvaliacoes()) {
                    if (avaliacaoId != null && avaliacaoId.equals(avaliacao.getId())) {
                        List<String> likes = avaliacao.getUsuariosLike();
                        List<String> dislikes = avaliacao.getUsuariosDislike();

                        if (dislikes.contains(emailUsuario)) {
                            dislikes.remove(emailUsuario);
                        } else {
                            dislikes.add(emailUsuario);
                            likes.remove(emailUsuario);
                        }

                        salvarDestinos(destinos);
                        return true;
                    }
                }
            }
        }

        return false;
    }
}