package com.example.mapadointercambista.model.forum;

import java.util.ArrayList;
import java.util.List;

public class ForumRepository {

    public static List<PostForum> criarPostsIniciais() {
        List<PostForum> posts = new ArrayList<>();

        long agora = System.currentTimeMillis();

        PostForum post1 = new PostForum(
                "Associação MarkitoLivre",
                "markito@forum.com",
                "",
                "Gostei hein, top demais",
                agora - (2 * 60 * 60 * 1000L)
        );

        RespostaForum resposta1 = new RespostaForum(
                "Carlos",
                "carlos@forum.com",
                "",
                "Também achei muito bom.",
                agora - (60 * 60 * 1000L),
                0,
                true,
                true
        );

        RespostaForum resposta2 = new RespostaForum(
                "Amanda",
                "amanda@forum.com",
                "",
                "Concordo com você!",
                agora - (50 * 60 * 1000L),
                1,
                true,
                true
        );

        RespostaForum resposta3 = new RespostaForum(
                "Marcos",
                "marcos@forum.com",
                "",
                "Eu também testei e curti bastante.",
                agora - (35 * 60 * 1000L),
                2,
                true,
                false
        );

        post1.getUsuariosLike().add("a@a.com");
        post1.getUsuariosLike().add("b@b.com");
        post1.getUsuariosLike().add("c@c.com");
        post1.getUsuariosLike().add("d@d.com");
        post1.getUsuariosLike().add("e@e.com");

        post1.getUsuariosDislike().add("x1@x.com");
        post1.getUsuariosDislike().add("x2@x.com");

        resposta1.getUsuariosLike().add("l1@x.com");
        resposta1.getUsuariosLike().add("l2@x.com");
        resposta1.getUsuariosDislike().add("d1@x.com");

        post1.getRespostas().add(resposta1);
        post1.getRespostas().add(resposta2);
        post1.getRespostas().add(resposta3);

        PostForum post2 = new PostForum(
                "Juninho Mandelão",
                "juninho@forum.com",
                "",
                "Show de bola esse aplicativo!",
                agora - (5 * 60 * 60 * 1000L)
        );

        RespostaForum respostaPost2 = new RespostaForum(
                "Fernanda",
                "fernanda@forum.com",
                "",
                "Também gostei demais!",
                agora - (2 * 60 * 60 * 1000L),
                0,
                true,
                false
        );
        post2.getRespostas().add(respostaPost2);

        PostForum post3 = new PostForum(
                "XD",
                "xd@forum.com",
                "",
                "Aplicativo ficou muito bom!",
                agora - (8 * 60 * 60 * 1000L)
        );

        RespostaForum respostaPost3 = new RespostaForum(
                "Lucas",
                "lucas@forum.com",
                "",
                "Ficou show mesmo.",
                agora - (3 * 60 * 60 * 1000L),
                0,
                true,
                false
        );
        post3.getRespostas().add(respostaPost3);

        posts.add(post1);
        posts.add(post2);
        posts.add(post3);

        return posts;
    }
}