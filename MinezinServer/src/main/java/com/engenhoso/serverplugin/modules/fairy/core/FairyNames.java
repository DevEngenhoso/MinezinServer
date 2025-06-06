package com.engenhoso.serverplugin.modules.fairy.core;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FairyNames {

    private static final List<String> NOMES = Arrays.asList(
            "Alura", "Sylfae", "Lumira", "Nixielle", "Aerwyn", "Elvara", "Faelynn", "Miraline", "Ylliria", "Azura",
            "Thalia", "Liora", "Ailune", "Calindra", "Elyra", "Saphira", "Iridessa", "Olwyn", "Lyselle", "Serenya",
            "Nuala", "Kaelith", "Maelin", "Felyra", "Zareen", "Elaria", "Orielle", "Ilythia", "Nyssara", "Lumae",
            "Faenya", "Vaelra", "Awen", "Orlina", "Sylrie", "Aderyn", "Brisella", "Caelira", "Daenira", "Thalindra",
            "Ysara", "Lunessa", "Marwyn", "Elowen", "Irielle", "Celinae", "Quenyra", "Virelle", "Soraya", "Zynara",
            "Firae", "Gliselle", "Yselda", "Amelith", "Nolira", "Syleira", "Velwyn", "Melindra", "Halyra", "Evanya",
            "Isyl", "Zephyra", "Arilith", "Lyrelle", "Caeryn", "Elindra", "Tharelle", "Sorenya", "Vaenya", "Maeril",
            "Nyara", "Lysenna", "Vaelith", "Olyssae", "Elarith", "Cyrene", "Talyssa", "Firiel", "Lorwyn", "Zynessa",
            "Elara", "Mirelle", "Isara", "Nevarra", "Lyssa", "Elluin", "Virelia", "Sereniel", "Aelyra", "Orindra",
            "Faeriel", "Olyndra", "Aysha", "Vaeriel", "Nymara", "Sirael", "Virewyn", "Elandra", "Myrrha", "Azelya"
    );

    private static final Random random = new Random();

    public static String gerarNomeAleatorio() {
        return NOMES.get(random.nextInt(NOMES.size()));
    }
}
