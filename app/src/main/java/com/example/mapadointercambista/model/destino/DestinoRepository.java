package com.example.mapadointercambista.model.destino;

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

        // ================= OXFORD =================
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
                "inglaterra",
                "Inglaterra",
                "Europa", // ✅ NOVO CAMPO
                "Inglês",
                "Libra Esterlina",
                "Oxford é uma cidade histórica e acadêmica, conhecida por abrigar uma das universidades mais prestigiadas do mundo.",
                agenciasPadrao,
                avaliacoesOxford
        ));

        // ================= KYOTO =================
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
                "japao",
                "Japão",
                "Ásia", // ✅ NOVO
                "Japonês",
                "Iene",
                "Kyoto é famosa por seus templos e cultura tradicional.",
                agenciasPadrao,
                avaliacoesKyoto
        ));

        // ================= BERLIM =================
        lista.add(new Destino(
                "destino_berlim",
                "Alemanha - Berlim",
                "alemanha",
                "Alemanha",
                "Europa", // ✅
                "Alemão",
                "Euro",
                "Berlim é uma cidade moderna e multicultural.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        // ================= TORONTO =================
        lista.add(new Destino(
                "destino_toronto",
                "Canadá - Toronto",
                "toronto",
                "Canadá",
                "América do Norte", // ✅
                "Inglês",
                "Dólar Canadense",
                "Toronto é um dos destinos mais procurados por intercambistas.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        // ================= BARCELONA =================
        lista.add(new Destino(
                "destino_barcelona",
                "Espanha - Barcelona",
                "barcelona",
                "Espanha",
                "Europa", // ✅
                "Espanhol",
                "Euro",
                "Barcelona mistura cultura, praia e educação.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        // ================= SYDNEY =================
        lista.add(new Destino(
                "destino_sydney",
                "Austrália - Sydney",
                "australia", // ⚠️ CORRIGIDO (antes tava "boston")
                "Austrália",
                "Oceania", // ✅
                "Inglês",
                "Dólar Australiano",
                "Sydney é conhecida por sua qualidade de vida.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        // ================= LISBOA =================
        lista.add(new Destino(
                "destino_lisboa",
                "Portugal - Lisboa",
                "lisboa", // ⚠️ CORRIGIDO (antes tava "boston")
                "Portugal",
                "Europa", // ✅
                "Português",
                "Euro",
                "Lisboa é um ótimo destino com custo acessível.",
                agenciasPadrao,
                new ArrayList<>()
        ));

        return lista;
    }
}