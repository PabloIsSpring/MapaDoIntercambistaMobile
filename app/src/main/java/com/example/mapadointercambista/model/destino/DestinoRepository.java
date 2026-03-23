package com.example.mapadointercambista.model.destino;

import com.example.mapadointercambista.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DestinoRepository {

    public static List<Destino> getDestinos() {
        List<Destino> lista = new ArrayList<>();

        List<String> agenciasPadrao = Arrays.asList(
                "Explore Abroad",
                "Journey Hub",
                "Gateway Exchange",
                "Nenhuma agência"
        );

        List<AvaliacaoDestino> avaliacoesOxford = new ArrayList<>();
        avaliacoesOxford.add(new AvaliacaoDestino(
                "SpringPablos",
                "spring@teste.com",
                "",
                "Muito bom o intercâmbio",
                5.0f,
                "Explore Abroad",
                System.currentTimeMillis() - (8 * 60 * 60 * 1000L)
        ));

        lista.add(new Destino(
                "destino_oxford",
                "Inglaterra - Oxford",
                R.drawable.inglaterra,
                "Inglaterra",
                "Inglês",
                "Libra Esterlina",
                "Oxford é uma cidade histórica e acadêmica, conhecida por abrigar uma das universidades mais prestigiadas do mundo. Com arquitetura clássica, bibliotecas renomadas e um ambiente universitário único, é um excelente destino para intercâmbio.",
                agenciasPadrao,
                avaliacoesOxford
        ));

        List<AvaliacaoDestino> avaliacoesKyoto = new ArrayList<>();
        avaliacoesKyoto.add(new AvaliacaoDestino(
                "SpringPablos",
                "spring2@teste.com",
                "",
                "Kyoto é linda, organizada e cheia de cultura. Vale muito a pena.",
                5.0f,
                "Journey Hub",
                System.currentTimeMillis() - (8 * 60 * 60 * 1000L)
        ));

        lista.add(new Destino(
                "destino_kyoto",
                "Japão - Kyoto",
                R.drawable.japao,
                "Japão",
                "Japonês",
                "Iene",
                "Kyoto é famosa por seus templos, cultura tradicional e experiências únicas de intercâmbio.",
                agenciasPadrao,
                avaliacoesKyoto
        ));

        lista.add(new Destino(
                "destino_berlim",
                "Alemanha - Berlim",
                R.drawable.alemanha,
                "Alemanha",
                "Alemão",
                "Euro",
                "Berlim é uma cidade moderna, multicultural e cheia de oportunidades para estudantes internacionais.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        lista.add(new Destino(
                "destino_toronto",
                "Canadá - Toronto",
                R.drawable.toronto,
                "Canadá",
                "Inglês",
                "Dólar Canadense",
                "Toronto é um dos destinos mais procurados por intercambistas, com ótima qualidade de vida e ensino.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        lista.add(new Destino(
                "destino_barcelona",
                "Espanha - Barcelona",
                R.drawable.barcelona,
                "Espanha",
                "Espanhol",
                "Euro",
                "Barcelona mistura cultura, praia e educação de qualidade em um ambiente vibrante.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        lista.add(new Destino(
                "destino_sydney",
                "Austrália - Sydney",
                R.drawable.boston,
                "Austrália",
                "Inglês",
                "Dólar Australiano",
                "Sydney é conhecida por sua qualidade de vida, belas paisagens e excelentes instituições.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        lista.add(new Destino(
                "destino_lisboa",
                "Portugal - Lisboa",
                R.drawable.boston,
                "Portugal",
                "Português",
                "Euro",
                "Lisboa é um ótimo destino para intercâmbio, com custo relativamente acessível e muita história.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        return lista;
    }
}