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

    private static List<Destino> cacheDestinos;

    private final SharedPreferences prefs;
    private final Gson gson;

    public DestinoStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public synchronized void salvarDestinos(List<Destino> destinos) {
        cacheDestinos = destinos != null ? new ArrayList<>(destinos) : new ArrayList<>();
        prefs.edit().putString(KEY_DESTINOS, gson.toJson(cacheDestinos)).apply();
    }

    public synchronized List<Destino> carregarDestinos() {
        if (cacheDestinos != null && !cacheDestinos.isEmpty()) {
            return new ArrayList<>(cacheDestinos);
        }

        String json = prefs.getString(KEY_DESTINOS, null);

        if (json == null || json.trim().isEmpty()) {
            List<Destino> iniciais = DestinoRepository.getDestinos();
            cacheDestinos = iniciais != null ? new ArrayList<>(iniciais) : new ArrayList<>();

            boolean alterado = garantirAvaliacoesPadrao(cacheDestinos);
            salvarDestinos(cacheDestinos);

            return new ArrayList<>(cacheDestinos);
        }

        Type type = new TypeToken<List<Destino>>() {}.getType();
        List<Destino> destinos = gson.fromJson(json, type);
        cacheDestinos = destinos != null ? destinos : new ArrayList<>();

        boolean alterado = garantirAvaliacoesPadrao(cacheDestinos);
        if (alterado) {
            salvarDestinos(cacheDestinos);
        }

        return new ArrayList<>(cacheDestinos);
    }

    private boolean garantirAvaliacoesPadrao(List<Destino> destinos) {
        if (destinos == null || destinos.isEmpty()) {
            return false;
        }

        boolean alterado = false;

        for (Destino destino : destinos) {
            if (destino == null) continue;

            List<AvaliacaoDestino> avaliacoes = destino.getListaAvaliacoes();
            if (avaliacoes == null) {
                avaliacoes = new ArrayList<>();
            }

            int faltantes = 3 - avaliacoes.size();
            if (faltantes > 0) {
                avaliacoes.addAll(criarAvaliacoesPadrao(destino, faltantes));
                destino.setListaAvaliacoes(avaliacoes);
                alterado = true;
            }
        }

        return alterado;
    }

    private List<AvaliacaoDestino> criarAvaliacoesPadrao(Destino destino, int quantidade) {
        List<AvaliacaoDestino> lista = new ArrayList<>();
        long agora = System.currentTimeMillis();

        String nomeDestino = destino.getNome() != null ? destino.getNome() : "destino";
        List<String> agencias = destino.getAgencias();
        String agencia1 = (agencias != null && !agencias.isEmpty()) ? agencias.get(0) : "Agência Internacional";
        String agencia2 = (agencias != null && agencias.size() > 1) ? agencias.get(1) : agencia1;
        String agencia3 = (agencias != null && agencias.size() > 2) ? agencias.get(2) : agencia1;

        if (quantidade >= 1) {
            lista.add(new AvaliacaoDestino(
                    "Camila Ribeiro",
                    "camila.ribeiro@email.com",
                    "",
                    "Tive uma experiência muito boa em " + nomeDestino + ". Gostei da organização, do suporte e da adaptação inicial.",
                    5.0f,
                    agencia1,
                    agora - (3 * 24 * 60 * 60 * 1000L)
            ));
        }

        if (quantidade >= 2) {
            lista.add(new AvaliacaoDestino(
                    "Bruno Santos",
                    "bruno.santos@email.com",
                    "",
                    "O destino é excelente e o processo foi tranquilo. Só achei importante ir com planejamento financeiro.",
                    4.5f,
                    agencia2,
                    agora - (5 * 24 * 60 * 60 * 1000L)
            ));
        }

        if (quantidade >= 3) {
            lista.add(new AvaliacaoDestino(
                    "Patrícia Lima",
                    "patricia.lima@email.com",
                    "",
                    "Gostei bastante da experiência cultural e das oportunidades. Recomendaria para quem quer evoluir no idioma.",
                    4.0f,
                    agencia3,
                    agora - (7 * 24 * 60 * 60 * 1000L)
            ));
        }

        return lista;
    }

    public synchronized Destino buscarDestinoPorId(String destinoId) {
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                return destino;
            }
        }

        return null;
    }

    public synchronized boolean adicionarAvaliacao(String destinoId, AvaliacaoDestino novaAvaliacao) {
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

    public synchronized boolean editarAvaliacao(String destinoId, String avaliacaoId, String novaMensagem,
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

    public synchronized boolean excluirAvaliacao(String destinoId, String avaliacaoId) {
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

    public synchronized boolean toggleLikeAvaliacao(String destinoId, String avaliacaoId, String emailUsuario) {
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            return false;
        }

        String emailNormalizado = emailUsuario.trim().toLowerCase();
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                for (AvaliacaoDestino avaliacao : destino.getListaAvaliacoes()) {
                    if (avaliacaoId != null && avaliacaoId.equals(avaliacao.getId())) {
                        List<String> likes = avaliacao.getUsuariosLike() != null
                                ? new ArrayList<>(avaliacao.getUsuariosLike()) : new ArrayList<>();
                        List<String> dislikes = avaliacao.getUsuariosDislike() != null
                                ? new ArrayList<>(avaliacao.getUsuariosDislike()) : new ArrayList<>();

                        normalizarListaEmails(likes);
                        normalizarListaEmails(dislikes);

                        if (likes.contains(emailNormalizado)) {
                            likes.remove(emailNormalizado);
                        } else {
                            likes.add(emailNormalizado);
                            dislikes.remove(emailNormalizado);
                        }

                        avaliacao.setUsuariosLike(likes);
                        avaliacao.setUsuariosDislike(dislikes);
                        salvarDestinos(destinos);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public synchronized boolean toggleDislikeAvaliacao(String destinoId, String avaliacaoId, String emailUsuario) {
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            return false;
        }

        String emailNormalizado = emailUsuario.trim().toLowerCase();
        List<Destino> destinos = carregarDestinos();

        for (Destino destino : destinos) {
            if (destinoId != null && destinoId.equals(destino.getId())) {
                for (AvaliacaoDestino avaliacao : destino.getListaAvaliacoes()) {
                    if (avaliacaoId != null && avaliacaoId.equals(avaliacao.getId())) {
                        List<String> likes = avaliacao.getUsuariosLike() != null
                                ? new ArrayList<>(avaliacao.getUsuariosLike()) : new ArrayList<>();
                        List<String> dislikes = avaliacao.getUsuariosDislike() != null
                                ? new ArrayList<>(avaliacao.getUsuariosDislike()) : new ArrayList<>();

                        normalizarListaEmails(likes);
                        normalizarListaEmails(dislikes);

                        if (dislikes.contains(emailNormalizado)) {
                            dislikes.remove(emailNormalizado);
                        } else {
                            dislikes.add(emailNormalizado);
                            likes.remove(emailNormalizado);
                        }

                        avaliacao.setUsuariosLike(likes);
                        avaliacao.setUsuariosDislike(dislikes);
                        salvarDestinos(destinos);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void normalizarListaEmails(List<String> lista) {
        if (lista == null) return;

        for (int i = 0; i < lista.size(); i++) {
            String valor = lista.get(i);
            lista.set(i, valor == null ? "" : valor.trim().toLowerCase());
        }

        lista.removeIf(item -> item == null || item.trim().isEmpty());
    }
}