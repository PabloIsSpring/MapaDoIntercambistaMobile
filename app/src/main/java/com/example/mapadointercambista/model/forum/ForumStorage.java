package com.example.mapadointercambista.model.forum;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ForumStorage {

    private static final String PREF_NAME = "forum_storage";
    private static final String KEY_POSTS = "posts";

    private final SharedPreferences prefs;
    private final Gson gson;

    public ForumStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void salvarPosts(List<PostForum> posts) {
        String json = gson.toJson(posts);
        prefs.edit().putString(KEY_POSTS, json).apply();
    }

    public List<PostForum> carregarPosts() {
        String json = prefs.getString(KEY_POSTS, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<PostForum>>() {}.getType();
        List<PostForum> posts = gson.fromJson(json, type);

        return posts != null ? posts : new ArrayList<>();
    }

    public void limparPosts() {
        prefs.edit().remove(KEY_POSTS).apply();
    }

    public void adicionarPost(PostForum novoPost) {
        List<PostForum> posts = carregarPosts();
        posts.add(0, novoPost);
        salvarPosts(posts);
    }

    public boolean editarPost(String postId, String novaMensagem) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                post.setMensagem(novaMensagem);
                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public boolean excluirPost(String postId) {
        List<PostForum> posts = carregarPosts();

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                posts.remove(i);
                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public boolean adicionarResposta(String postId, RespostaForum novaResposta) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<RespostaForum> respostas = post.getRespostas();
                if (respostas == null) {
                    respostas = new ArrayList<>();
                }
                respostas.add(novaResposta);
                post.setRespostas(respostas);
                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public boolean adicionarRespostaFilha(String postId, String respostaPaiId, RespostaForum novaResposta) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (int i = 0; i < respostas.size(); i++) {
                    RespostaForum respostaAtual = respostas.get(i);

                    if (respostaAtual.getId().equals(respostaPaiId)) {
                        RespostaForum respostaFilha = new RespostaForum(
                                novaResposta.getAutorNome(),
                                novaResposta.getAutorEmail(),
                                novaResposta.getAutorFotoUri(),
                                novaResposta.getMensagem(),
                                novaResposta.getCriadoEm(),
                                respostaAtual.getNivel() + 1,
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

    public boolean editarResposta(String postId, String respostaId, String novaMensagem) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (RespostaForum resposta : respostas) {
                    if (resposta.getId().equals(respostaId)) {
                        resposta.setMensagem(novaMensagem);
                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean excluirResposta(String postId, String respostaId) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (int i = 0; i < respostas.size(); i++) {
                    RespostaForum resposta = respostas.get(i);

                    if (resposta.getId().equals(respostaId)) {
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

    public boolean toggleLikePost(String postId, String emailUsuario) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<String> likes = post.getUsuariosLike();
                List<String> dislikes = post.getUsuariosDislike();

                if (likes.contains(emailUsuario)) {
                    likes.remove(emailUsuario);
                } else {
                    likes.add(emailUsuario);
                    dislikes.remove(emailUsuario);
                }

                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public boolean toggleDislikePost(String postId, String emailUsuario) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<String> likes = post.getUsuariosLike();
                List<String> dislikes = post.getUsuariosDislike();

                if (dislikes.contains(emailUsuario)) {
                    dislikes.remove(emailUsuario);
                } else {
                    dislikes.add(emailUsuario);
                    likes.remove(emailUsuario);
                }

                salvarPosts(posts);
                return true;
            }
        }

        return false;
    }

    public boolean toggleLikeResposta(String postId, String respostaId, String emailUsuario) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (RespostaForum resposta : respostas) {
                    if (resposta.getId().equals(respostaId)) {
                        List<String> likes = resposta.getUsuariosLike();
                        List<String> dislikes = resposta.getUsuariosDislike();

                        if (likes.contains(emailUsuario)) {
                            likes.remove(emailUsuario);
                        } else {
                            likes.add(emailUsuario);
                            dislikes.remove(emailUsuario);
                        }

                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean toggleDislikeResposta(String postId, String respostaId, String emailUsuario) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                List<RespostaForum> respostas = post.getRespostas();

                if (respostas == null) {
                    return false;
                }

                for (RespostaForum resposta : respostas) {
                    if (resposta.getId().equals(respostaId)) {
                        List<String> likes = resposta.getUsuariosLike();
                        List<String> dislikes = resposta.getUsuariosDislike();

                        if (dislikes.contains(emailUsuario)) {
                            dislikes.remove(emailUsuario);
                        } else {
                            dislikes.add(emailUsuario);
                            likes.remove(emailUsuario);
                        }

                        salvarPosts(posts);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public PostForum buscarPostPorId(String postId) {
        List<PostForum> posts = carregarPosts();

        for (PostForum post : posts) {
            if (post.getId().equals(postId)) {
                return post;
            }
        }

        return null;
    }
}