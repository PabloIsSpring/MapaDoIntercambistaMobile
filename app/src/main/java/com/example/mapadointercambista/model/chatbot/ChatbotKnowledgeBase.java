package com.example.mapadointercambista.model.chatbot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatbotKnowledgeBase {

    private final List<Intencao> intencoes = new ArrayList<>();
    private String ultimaIntencao = "";

    public ChatbotKnowledgeBase() {
        intencoes.add(new Intencao(
                "recuperar_senha",
                "Você pode usar a opção de recuperação de senha na tela de login para redefinir seu acesso. Caso ainda não funcione, entre em contato pelo e-mail suporte@mapadointercambista.com.",
                "esqueci senha", "recuperar senha", "trocar senha", "redefinir senha", "nao lembro senha", "senha errada"
        ));

        intencoes.add(new Intencao(
                "problema_login",
                "Verifique se o e-mail e a senha estão corretos. Se o problema continuar, tente redefinir sua senha ou verifique sua conexão com a internet.",
                "nao consigo login", "nao consigo entrar", "erro login", "login nao funciona", "problema entrar", "email senha incorretos"
        ));

        intencoes.add(new Intencao(
                "criar_conta",
                "Para criar uma conta, vá até Perfil, toque em Entrar ou Criar conta, escolha se você é usuário ou agência e preencha os dados solicitados.",
                "criar conta", "cadastro", "cadastrar", "nova conta", "registrar", "fazer conta"
        ));

        intencoes.add(new Intencao(
                "cadastro_agencia",
                "Para cadastrar uma agência, selecione a opção Agência na tela de acesso e informe razão social, nome fantasia, CNPJ, username, e-mail e senha.",
                "agencia", "cadastro agencia", "criar agencia", "cnpj", "razao social", "nome fantasia", "conta comercial"
        ));

        intencoes.add(new Intencao(
                "usar_sem_login",
                "Você pode navegar pelo app sem login. Porém, para postar, responder, curtir, descurtir ou favoritar destinos, é necessário entrar em uma conta.",
                "preciso estar logado", "sem login", "usar deslogado", "visitante", "usar sem conta", "navegar sem login"
        ));

        intencoes.add(new Intencao(
                "perfil",
                "Você pode acessar a tela de Perfil para visualizar seus dados, favoritos, configurações e opção de sair da conta.",
                "perfil", "minha conta", "dados pessoais", "alterar dados", "editar perfil", "informacoes pessoais"
        ));

        intencoes.add(new Intencao(
                "email_senha",
                "No momento, o e-mail da conta não pode ser alterado. A senha poderá ser alterada quando a funcionalidade de recuperação/alteração estiver disponível.",
                "mudar email", "alterar email", "trocar email", "mudar senha", "alterar senha", "trocar senha"
        ));

        intencoes.add(new Intencao(
                "logout",
                "Para sair da conta, acesse a tela de Perfil e toque em Sair da conta.",
                "sair conta", "logout", "deslogar", "encerrar sessao", "sair do app"
        ));

        intencoes.add(new Intencao(
                "seguranca",
                "O app utiliza validações, controle de sessão e token de autenticação para proteger o acesso. Mesmo assim, nunca compartilhe sua senha com terceiros.",
                "seguro", "seguranca", "dados seguros", "meus dados", "privacidade", "protecao"
        ));

        intencoes.add(new Intencao(
                "suporte",
                "Você pode falar com o suporte pelo e-mail suporte@mapadointercambista.com. Ao reportar um problema, envie o máximo de detalhes possível.",
                "suporte", "contato", "email suporte", "ajuda", "falar suporte", "atendimento"
        ));

        intencoes.add(new Intencao(
                "erro_app",
                "Tente reiniciar o aplicativo e verificar sua conexão. Se o erro continuar, envie um relato para suporte@mapadointercambista.com.",
                "erro", "bug", "travando", "nao funciona", "problema app", "aplicativo fechando", "falha"
        ));

        intencoes.add(new Intencao(
                "internet",
                "O aplicativo precisa de conexão com a internet para funcionar corretamente, principalmente para login, cadastro, suporte e recursos online.",
                "sem internet", "offline", "funciona sem internet", "conexao", "internet", "wifi", "dados moveis"
        ));

        intencoes.add(new Intencao(
                "intercambio",
                "Você pode encontrar informações sobre intercâmbio nas telas de Destinos, Fórum e nos conteúdos publicados por usuários e agências.",
                "intercambio", "destinos", "estudar fora", "pais", "viagem", "programa intercambio", "informacoes intercambio"
        ));

        intencoes.add(new Intencao(
                "forum",
                "No Fórum você pode ver relatos, dúvidas e experiências reais. Para publicar ou interagir, é necessário estar logado.",
                "forum", "postar", "publicacao", "comentario", "responder", "curtir", "descurtir"
        ));

        intencoes.add(new Intencao(
                "favoritos",
                "Para favoritar destinos, entre em sua conta e toque no ícone de favorito nos cards de destinos.",
                "favorito", "favoritos", "salvar destino", "destino salvo", "curtir destino"
        ));

        intencoes.add(new Intencao(
                "excluir_conta",
                "A exclusão de conta será implementada em breve. Por enquanto, solicite ajuda pelo e-mail suporte@mapadointercambista.com.",
                "excluir conta", "deletar conta", "apagar conta", "remover conta", "cancelar conta"
        ));

        intencoes.add(new Intencao(
                "frustracao_suporte",
                "Poxa, entendi. Como isso não funcionou, o ideal é acionar o suporte pelo e-mail suporte@mapadointercambista.com. Explique o que tentou fazer, em qual tela aconteceu e, se possível, envie um print do erro.",
                "nao funcionou", "deu errado", "continua erro", "nao resolveu", "nao deu certo", "mesmo erro", "continua travando", "ainda nao vai", "falhou"
        ));

        intencoes.add(new Intencao(
                "gratuito",
                "Sim, o uso do aplicativo é gratuito para usuários.",
                "gratuito", "pago", "cobra", "mensalidade", "preco", "valor"
        ));
    }

    public String responder(String pergunta) {
        if (pergunta == null || pergunta.trim().isEmpty()) {
            return "Digite sua dúvida ou escolha uma das perguntas rápidas acima.";
        }

        String texto = normalizar(pergunta);

        Intencao melhor = null;
        int melhorPontuacao = 0;

        for (Intencao intencao : intencoes) {
            int pontuacao = intencao.calcularPontuacao(texto);

            if (pontuacao > melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhor = intencao;
            }
        }

        if (melhor != null && melhorPontuacao >= 2) {
            ultimaIntencao = melhor.nome;
            return melhor.resposta + sugestoesPorIntencao(melhor.nome);
        }

        if (texto.length() <= 12 && !ultimaIntencao.isEmpty()) {
            return responderComContexto(texto);
        }

        return "Ainda não encontrei uma resposta exata para isso. Posso ajudar com: login, senha, cadastro, agência, perfil, fórum, favoritos, internet e suporte. Se preferir, fale com suporte@mapadointercambista.com.";
    }

    private String responderComContexto(String texto) {
        if (ultimaIntencao.equals("problema_login") || ultimaIntencao.equals("recuperar_senha")) {
            return "Como sua dúvida anterior foi sobre acesso, recomendo conferir se o e-mail está correto, tentar redefinir a senha e verificar sua conexão com a internet.";
        }

        if (ultimaIntencao.equals("cadastro_agencia")) {
            return "Sobre agência: o cadastro pede razão social, nome fantasia, CNPJ, username, e-mail e senha.";
        }

        if (ultimaIntencao.equals("forum")) {
            return "Sobre o fórum: você pode visualizar sem login, mas precisa estar logado para postar, responder, curtir ou descurtir.";
        }

        if (ultimaIntencao.equals("favoritos")) {
            return "Sobre favoritos: eles ficam vinculados à sua conta. Entre no app e toque no coração dos destinos que deseja salvar.";
        }

        return "Posso complementar sua dúvida, mas tente escrever um pouco mais de detalhes para eu entender melhor.";
    }

    private String sugestoesPorIntencao(String intencao) {
        switch (intencao) {
            case "problema_login":
            case "recuperar_senha":
                return "\n\nVocê também pode tentar perguntar: \"Como redefinir minha senha?\" ou \"Meu login não funciona\".";

            case "frustracao_suporte":
                return "\n\nComo isso não funcionou, recomendo falar com o suporte pelo e-mail suporte@mapadointercambista.com.";

            case "cadastro_agencia":
                return "\n\nVocê também pode perguntar: \"Quais dados preciso para cadastrar uma agência?\".";

            case "forum":
                return "\n\nVocê também pode perguntar: \"Preciso estar logado para postar?\".";

            case "favoritos":
                return "\n\nVocê também pode perguntar: \"Onde vejo meus favoritos?\".";

            case "suporte":
            case "erro_app":
                return "\n\nSe quiser, descreva o erro com mais detalhes, como a tela onde aconteceu e o que você estava tentando fazer.";

            default:
                return "";
        }
    }

    public ChatbotResponse responderDetalhado(String pergunta) {
        if (pergunta == null || pergunta.trim().isEmpty()) {
            return new ChatbotResponse(
                    "Digite sua dúvida ou escolha uma das perguntas rápidas acima.",
                    "vazio",
                    0,
                    perguntasRapidas()
            );
        }

        String texto = normalizar(pergunta);

        Intencao melhor = null;
        int melhorPontuacao = 0;

        for (Intencao intencao : intencoes) {
            int pontuacao = intencao.calcularPontuacao(texto);

            if (pontuacao > melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhor = intencao;
            }
        }

        if (melhor != null && melhorPontuacao >= 4) {
            ultimaIntencao = melhor.nome;

            return new ChatbotResponse(
                    melhor.resposta,
                    melhor.nome,
                    melhorPontuacao,
                    sugestoesArrayPorIntencao(melhor.nome)
            );
        }

        if (melhor != null && melhorPontuacao >= 2) {
            ultimaIntencao = melhor.nome;

            return new ChatbotResponse(
                    "Acho que sua dúvida é sobre " + nomeAmigavel(melhor.nome) + ". " + melhor.resposta,
                    melhor.nome,
                    melhorPontuacao,
                    sugestoesArrayPorIntencao(melhor.nome)
            );
        }

        if (texto.length() <= 12 && !ultimaIntencao.isEmpty()) {
            return new ChatbotResponse(
                    responderComContexto(texto),
                    ultimaIntencao,
                    1,
                    sugestoesArrayPorIntencao(ultimaIntencao)
            );
        }

        return new ChatbotResponse(
                "Ainda não encontrei uma resposta exata para isso. Posso ajudar com login, senha, cadastro, agência, perfil, fórum, favoritos, internet e suporte.",
                "fallback",
                0,
                perguntasRapidas()
        );
    }

    private String nomeAmigavel(String intencao) {
        switch (intencao) {
            case "problema_login":
                return "problemas de login";
            case "recuperar_senha":
                return "recuperação de senha";
            case "cadastro_agencia":
                return "cadastro de agência";
            case "criar_conta":
                return "criação de conta";
            case "usar_sem_login":
                return "uso sem login";
            case "forum":
                return "fórum";
            case "favoritos":
                return "favoritos";
            case "erro_app":
                return "erro no aplicativo";
            case "suporte":
                return "suporte";
            default:
                return "esse assunto";
        }
    }

    private String[] sugestoesArrayPorIntencao(String intencao) {
        switch (intencao) {
            case "problema_login":
            case "recuperar_senha":
                return new String[]{
                        "Esqueci minha senha",
                        "Não consigo fazer login",
                        "Meu e-mail está correto?",
                        "Falar com suporte"
                };

            case "cadastro_agencia":
                return new String[]{
                        "Como cadastrar agência?",
                        "Quais dados a agência precisa?",
                        "O que é CNPJ?",
                        "Falar com suporte"
                };



            case "criar_conta":
                return new String[]{
                        "Como criar uma conta?",
                        "Preciso estar logado?",
                        "Como cadastrar agência?",
                        "Não consigo fazer login"
                };

            case "forum":
                return new String[]{
                        "Como usar o fórum?",
                        "Preciso estar logado?",
                        "Como comentar?",
                        "Como curtir posts?"
                };

            case "favoritos":
                return new String[]{
                        "Como favoritar destinos?",
                        "Onde vejo favoritos?",
                        "Preciso estar logado?",
                        "Como buscar destinos?"
                };

            case "suporte":
            case "frustracao_suporte":
                return new String[]{
                        "Falar com suporte",
                        "Como reportar erro?",
                        "O app está travando",
                        "Não consigo fazer login"
                };
            case "erro_app":
                return new String[]{
                        "Falar com suporte",
                        "O app está travando",
                        "O app funciona sem internet?",
                        "Como reportar erro?"
                };

            default:
                return perguntasRapidas();
        }
    }

    public String[] perguntasRapidas() {
        return new String[]{
                "Não consigo fazer login",
                "Esqueci minha senha",
                "Como criar uma conta?",
                "Como cadastrar agência?",
                "Preciso estar logado?",
                "Como usar o fórum?",
                "Como favoritar destinos?",
                "Falar com suporte"
        };
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";

        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        return normalizado
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static class Intencao {
        private final String nome;
        private final String resposta;
        private final List<String> palavrasChave;

        Intencao(String nome, String resposta, String... palavrasChave) {
            this.nome = nome;
            this.resposta = resposta;
            this.palavrasChave = Arrays.asList(palavrasChave);
        }

        int calcularPontuacao(String texto) {
            int pontos = 0;

            for (String termo : palavrasChave) {
                String termoNormalizado = normalizar(termo);

                if (texto.contains(termoNormalizado)) {
                    pontos += termoNormalizado.contains(" ") ? 3 : 2;
                } else {
                    String[] partes = termoNormalizado.split(" ");
                    for (String parte : partes) {
                        if (parte.length() >= 4 && texto.contains(parte)) {
                            pontos++;
                        }
                    }
                }
            }

            return pontos;
        }
    }
}