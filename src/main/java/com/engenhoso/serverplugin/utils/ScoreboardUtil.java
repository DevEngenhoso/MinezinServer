package com.engenhoso.serverplugin.utils;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class ScoreboardUtil {

    public static void esconderNumerosDaDireita(Objective objetivo) {
        objetivo.numberFormat(NumberFormat.blank());
    }

    public static void esconderNumeroDaLinha(Score score) {
        score.numberFormat(NumberFormat.blank());
    }
}