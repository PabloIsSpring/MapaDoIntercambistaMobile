package com.example.mapadointercambista.model.forum;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mapadointercambista.util.ForumLimits;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ForumStorage {

    private static final String PREF_NAME = "forum_storage";
    private static final String KEY_POSTS = "posts";

    private static List<PostForum> cachePosts;

    private final SharedPreferences prefs;
    private final Gson gson;

    public ForumStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public synchronized void salvarPosts(List<PostForum> posts) {
        List<PostForum> normalizados = normalizarPosts(posts);
        cachePosts = new ArrayList<>(normalizados);
        prefs.edit().putString(KEY_POSTS, gson.toJson(cachePosts)).apply();
    }

    public synchronized List<PostForum> carregarPosts() {
        if (cachePosts != null) {
            return new ArrayList<>(cachePosts);
        }

        String json = prefs.getString(KEY_POSTS, null);
        if (json == null || json.trim().isEmpty()) {
            cachePosts = new ArrayList<>();
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<PostForum>>() {}.getType();
            List<PostForum> posts = gson.fromJson(json, type);
            cachePosts = normalizarPosts(posts);
            return new ArrayList<>(cachePosts);
        } catch (JsonSyntaxException | IllegalStateException e) {
            cachePosts = new ArrayList<>();
            prefs.edit().remove(KEY_POSTS).apply();
            return new ArrayList<>();
        }
    }

    public synchronized void limparPosts() {
        cachePosts = new ArrayList<>();
        prefs.edit().remove(KEY_POSTS).apply();
    }

    public synchronized boolean adicionarPost(PostForum novoPost) {
        if (!isPostValido(novoPost)) {
            return false;
        }

        List<PostForum> posts = carregarPosts();
        posts.add(0, sanitizarPost(novoPost));
        salvarPosts(posts);
        return true;
    }

    public synchronized boolean editarPost(String postId, String novoTitulo, String novaMensagem) {
        if (InputSecurityUtils.isNullOrBlank(postId)) {
            return false;
        }

        String titulo = InputSecurityUtils.sanitizeUserText(novoTitulo);
        String mensagem = InputSecurityUtils.sanitizeUserText(novaMensagem);

        if (InputSecurityUtils.isNullOrBlank(titulo)
                || InputSecurityUtils.isNullOrBlank(mensagem)
                || titulo.length() < ForumLimits.MIN_TITULO_POST
                || mensagem.length() < ForumLimits.MIN_TEXTO_POST
                || InputSecurityUtils.exceedsMaxLength(titulo, ForumLimits.MAX_TITULO_POST)
                || InputSecurityUtils.exceedsMaxLength(mensagem, ForumLimits.MAX_TEXTO_POST)
                || InputSecurityUtils.containsSuspiciousPattern(titulo)
                || InputSecurityUtils.containsSuspiciousPattern(mensagem)) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                post.setTitulo(titulo);
                post.setMensagem(mensagem);
                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public synchronized boolean excluirPost(String postId) {
        if (InputSecurityUtils.isNullOrBlank(postId)) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (int i = 0; i < posts.size(); i++) {
            if (postId.equals(posts.get(i).getId())) {
                posts.remove(i);
                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public synchronized boolean adicionarResposta(String postId, RespostaForum novaResposta) {
        if (InputSecurityUtils.isNullOrBlank(postId) || !isRespostaValida(novaResposta)) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<RespostaForum> respostas = post.getRespostas();
                if (respostas == null) {
                    respostas = new ArrayList<>();
                }

                if (respostas.size() >= ForumLimits.MAX_RESPOSTAS_POR_POST) {
                    return false;
                }

                respostas.add(sanitizarResposta(novaResposta));
                post.setRespostas(respostas);
                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public synchronized boolean adicionarRespostaFilha(String postId, String respostaPaiId, RespostaForum novaResposta) {
        if (InputSecurityUtils.isNullOrBlank(postId)
                || InputSecurityUtils.isNullOrBlank(respostaPaiId)
                || !isRespostaValida(novaResposta)) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null || respostas.isEmpty()) {
                    return false;
                }

                if (respostas.size() >= ForumLimits.MAX_RESPOSTAS_POR_POST) {
                    return false;
                }

                for (int i = 0; i < respostas.size(); i++) {
                    RespostaForum respostaAtual = respostas.get(i);

                    if (respostaPaiId.equals(respostaAtual.getId())) {
                        int novoNivel = respostaAtual.getNivel() + 1;
                        if (novoNivel > ForumLimits.MAX_NIVEL_RESPOSTA) {
                            return false;
                        }

                        RespostaForum base = sanitizarResposta(novaResposta);

                        RespostaForum respostaFilha = new RespostaForum(
                                base.getAutorNome(),
                                base.getAutorEmail(),
                                base.getAutorFotoUri(),
                                base.getMensagem(),
                                base.getCriadoEm(),
                                novoNivel,
                                false,
                                false
                        );

                        respostaAtual.setTemRespostas(true);

                        int posicaoInsercao = i + 1;
                        while (posicaoInsercao < respostas.size()
                                && respostas.get(posicaoInsercao).getNivel() > respostaAtual.getNivel()) {
                            posicaoInsercao++;
                        }

                        respostas.add(posicaoInsercao, respostaFilha);
                        post.setRespostas(respostas);
                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public synchronized boolean editarResposta(String postId, String respostaId, String novaMensagem) {
        if (InputSecurityUtils.isNullOrBlank(postId) || InputSecurityUtils.isNullOrBlank(respostaId)) {
            return false;
        }

        String mensagem = InputSecurityUtils.sanitizeUserText(novaMensagem);

        if (InputSecurityUtils.isNullOrBlank(mensagem)
                || mensagem.length() < ForumLimits.MIN_RESPOSTA
                || InputSecurityUtils.exceedsMaxLength(mensagem, ForumLimits.MAX_RESPOSTA)
                || InputSecurityUtils.containsSuspiciousPattern(mensagem)) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (RespostaForum resposta : respostas) {
                    if (respostaId.equals(resposta.getId())) {
                        resposta.setMensagem(mensagem);
                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public synchronized boolean excluirResposta(String postId, String respostaId) {
        if (InputSecurityUtils.isNullOrBlank(postId) || InputSecurityUtils.isNullOrBlank(respostaId)) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (int i = 0; i < respostas.size(); i++) {
                    RespostaForum resposta = respostas.get(i);

                    if (respostaId.equals(resposta.getId())) {
                        int nivelPai = resposta.getNivel();

                        respostas.remove(i);

                        while (i < respostas.size() && respostas.get(i).getNivel() > nivelPai) {
                            respostas.remove(i);
                        }

                        reajustarTemRespostas(respostas);
                        post.setRespostas(respostas);
                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public synchronized boolean toggleLikePost(String postId, String emailUsuario) {
        if (InputSecurityUtils.isNullOrBlank(postId) || InputSecurityUtils.isNullOrBlank(emailUsuario)) {
            return false;
        }

        String emailNormalizado = normalizarEmail(emailUsuario);
        if (emailNormalizado.isEmpty()) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<String> likes = garantirLista(post.getUsuariosLike());
                List<String> dislikes = garantirLista(post.getUsuariosDislike());

                if (likes.contains(emailNormalizado)) {
                    likes.remove(emailNormalizado);
                } else {
                    likes.add(emailNormalizado);
                    dislikes.remove(emailNormalizado);
                }

                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public synchronized boolean toggleDislikePost(String postId, String emailUsuario) {
        if (InputSecurityUtils.isNullOrBlank(postId) || InputSecurityUtils.isNullOrBlank(emailUsuario)) {
            return false;
        }

        String emailNormalizado = normalizarEmail(emailUsuario);
        if (emailNormalizado.isEmpty()) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<String> likes = garantirLista(post.getUsuariosLike());
                List<String> dislikes = garantirLista(post.getUsuariosDislike());

                if (dislikes.contains(emailNormalizado)) {
                    dislikes.remove(emailNormalizado);
                } else {
                    dislikes.add(emailNormalizado);
                    likes.remove(emailNormalizado);
                }

                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public synchronized boolean toggleLikeResposta(String postId, String respostaId, String emailUsuario) {
        if (InputSecurityUtils.isNullOrBlank(postId)
                || InputSecurityUtils.isNullOrBlank(respostaId)
                || InputSecurityUtils.isNullOrBlank(emailUsuario)) {
            return false;
        }

        String emailNormalizado = normalizarEmail(emailUsuario);
        if (emailNormalizado.isEmpty()) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (RespostaForum resposta : respostas) {
                    if (respostaId.equals(resposta.getId())) {
                        List<String> likes = garantirLista(resposta.getUsuariosLike());
                        List<String> dislikes = garantirLista(resposta.getUsuariosDislike());

                        if (likes.contains(emailNormalizado)) {
                            likes.remove(emailNormalizado);
                        } else {
                            likes.add(emailNormalizado);
                            dislikes.remove(emailNormalizado);
                        }

                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public synchronized boolean toggleDislikeResposta(String postId, String respostaId, String emailUsuario) {
        if (InputSecurityUtils.isNullOrBlank(postId)
                || InputSecurityUtils.isNullOrBlank(respostaId)
                || InputSecurityUtils.isNullOrBlank(emailUsuario)) {
            return false;
        }

        String emailNormalizado = normalizarEmail(emailUsuario);
        if (emailNormalizado.isEmpty()) {
            return false;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (RespostaForum resposta : respostas) {
                    if (respostaId.equals(resposta.getId())) {
                        List<String> likes = garantirLista(resposta.getUsuariosLike());
                        List<String> dislikes = garantirLista(resposta.getUsuariosDislike());

                        if (dislikes.contains(emailNormalizado)) {
                            dislikes.remove(emailNormalizado);
                        } else {
                            dislikes.add(emailNormalizado);
                            likes.remove(emailNormalizado);
                        }

                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public synchronized PostForum buscarPostPorId(String postId) {
        if (InputSecurityUtils.isNullOrBlank(postId)) {
            return null;
        }

        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                return post;
            }
        }

        return null;
    }

    private void reajustarTemRespostas(List<RespostaForum> respostas) {
        for (int i = 0; i < respostas.size(); i++) {
            RespostaForum atual = respostas.get(i);
            boolean temFilha = false;

            for (int j = i + 1; j < respostas.size(); j++) {
                if (respostas.get(j).getNivel() <= atual.getNivel()) {
                    break;
                }

                if (respostas.get(j).getNivel() == atual.getNivel() + 1) {
                    temFilha = true;
                    break;
                }
            }

            atual.setTemRespostas(temFilha);
        }
    }

    private List<PostForum> normalizarPosts(List<PostForum> posts) {
        List<PostForum> listaNormalizada = new ArrayList<>();

        if (posts == null) {
            return listaNormalizada;
        }

        for (PostForum post : posts) {
            if (post == null) {
                continue;
            }

            post.setAutorNome(limitar(
                    InputSecurityUtils.sanitizeUserText(post.getAutorNome()),
                    ForumLimits.MAX_NOME_AUTOR
            ));

            post.setAutorEmail(limitar(
                    normalizarEmail(post.getAutorEmail()),
                    ForumLimits.MAX_EMAIL
            ));

            post.setAutorFotoUri(limitar(post.getAutorFotoUri(), ForumLimits.MAX_FOTO_URI));
            post.setTitulo(InputSecurityUtils.sanitizeUserText(post.getTitulo()));
            post.setMensagem(InputSecurityUtils.sanitizeUserText(post.getMensagem()));

            if (post.getRespostas() == null) {
                post.setRespostas(new ArrayList<>());
            }

            for (RespostaForum resposta : post.getRespostas()) {
                if (resposta != null) {
                    resposta.setAutorNome(limitar(
                            InputSecurityUtils.sanitizeUserText(resposta.getAutorNome()),
                            ForumLimits.MAX_NOME_AUTOR
                    ));

                    resposta.setAutorEmail(limitar(
                            normalizarEmail(resposta.getAutorEmail()),
                            ForumLimits.MAX_EMAIL
                    ));

                    resposta.setAutorFotoUri(limitar(
                            resposta.getAutorFotoUri(),
                            ForumLimits.MAX_FOTO_URI
                    ));

                    resposta.setMensagem(InputSecurityUtils.sanitizeUserText(resposta.getMensagem()));
                    resposta.setNivel(Math.max(0, Math.min(resposta.getNivel(), ForumLimits.MAX_NIVEL_RESPOSTA)));
                }
            }

            listaNormalizada.add(post);
        }

        return listaNormalizada;
    }

    private boolean isPostValido(PostForum post) {
        if (post == null) {
            return false;
        }

        String titulo = InputSecurityUtils.sanitizeUserText(post.getTitulo());
        String mensagem = InputSecurityUtils.sanitizeUserText(post.getMensagem());

        return !InputSecurityUtils.isNullOrBlank(titulo)
                && !InputSecurityUtils.isNullOrBlank(mensagem)
                && titulo.length() >= ForumLimits.MIN_TITULO_POST
                && mensagem.length() >= ForumLimits.MIN_TEXTO_POST
                && !InputSecurityUtils.exceedsMaxLength(titulo, ForumLimits.MAX_TITULO_POST)
                && !InputSecurityUtils.exceedsMaxLength(mensagem, ForumLimits.MAX_TEXTO_POST)
                && !InputSecurityUtils.containsSuspiciousPattern(titulo)
                && !InputSecurityUtils.containsSuspiciousPattern(mensagem);
    }

    private boolean isRespostaValida(RespostaForum resposta) {
        if (resposta == null) {
            return false;
        }

        String mensagem = InputSecurityUtils.sanitizeUserText(resposta.getMensagem());

        return !InputSecurityUtils.isNullOrBlank(mensagem)
                && mensagem.length() >= ForumLimits.MIN_RESPOSTA
                && !InputSecurityUtils.exceedsMaxLength(mensagem, ForumLimits.MAX_RESPOSTA)
                && !InputSecurityUtils.containsSuspiciousPattern(mensagem);
    }

    private PostForum sanitizarPost(PostForum post) {
        return new PostForum(
                garantirId(post.getId()),
                limitar(post.getAutorNome(), ForumLimits.MAX_NOME_AUTOR),
                limitar(normalizarEmail(post.getAutorEmail()), ForumLimits.MAX_EMAIL),
                limitar(post.getAutorFotoUri(), ForumLimits.MAX_FOTO_URI),
                limitar(InputSecurityUtils.sanitizeUserText(post.getTitulo()), ForumLimits.MAX_TITULO_POST),
                limitar(InputSecurityUtils.sanitizeUserText(post.getMensagem()), ForumLimits.MAX_TEXTO_POST),
                post.getCriadoEm(),
                garantirLista(post.getUsuariosLike()),
                garantirLista(post.getUsuariosDislike()),
                post.getRespostas() != null ? post.getRespostas() : new ArrayList<>()
        );
    }

    private String garantirId(String id) {
        if (InputSecurityUtils.isNullOrBlank(id)) {
            return java.util.UUID.randomUUID().toString();
        }
        return id.trim();
    }
    private RespostaForum sanitizarResposta(RespostaForum resposta) {
        int nivelSeguro = Math.max(0, Math.min(resposta.getNivel(), ForumLimits.MAX_NIVEL_RESPOSTA));

        return new RespostaForum(
                garantirId(resposta.getId()),
                limitar(resposta.getAutorNome(), ForumLimits.MAX_NOME_AUTOR),
                limitar(normalizarEmail(resposta.getAutorEmail()), ForumLimits.MAX_EMAIL),
                limitar(resposta.getAutorFotoUri(), ForumLimits.MAX_FOTO_URI),
                limitar(InputSecurityUtils.sanitizeUserText(resposta.getMensagem()), ForumLimits.MAX_RESPOSTA),
                resposta.getCriadoEm(),
                nivelSeguro,
                resposta.isVisivel(),
                resposta.isTemRespostas(),
                garantirLista(resposta.getUsuariosLike()),
                garantirLista(resposta.getUsuariosDislike())
        );
    }

    private List<String> garantirLista(List<String> lista) {
        List<String> resultado = new ArrayList<>();

        if (lista == null) {
            return resultado;
        }

        for (String item : lista) {
            String valor = normalizarEmail(item);
            if (!valor.isEmpty() && !resultado.contains(valor)) {
                resultado.add(valor);
            }
        }

        return resultado;
    }

    private String limitar(String valor, int max) {
        if (valor == null) {
            return "";
        }
        return valor.length() > max ? valor.substring(0, max) : valor;
    }

    private String normalizarEmail(String email) {
        return InputSecurityUtils.sanitizeUserText(email).trim().toLowerCase();
    }
}