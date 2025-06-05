package com.engenhoso.serverplugin.fairy;

import com.engenhoso.serverplugin.MinezinServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FairyTalkManager {

    private static final List<String> MENSAGENS_GERAIS = List.of(
            "Você já tomou água hoje? Eu não, mas você deveria.",
            "Esses biomas são bonitos, mas nada supera você minerando desajeitado.",
            "Sabe o que eu mais gosto em você? Quando você não me ignora.",
            "Poderia me dar uma flor? Prometo não roubar… muito.",
            "Se eu tivesse uma moeda pra cada vez que você quase morreu… eu teria uma mochila cheia.",
            "Você me escuta? ...Ou só ignora fadas mesmo?",
            "Quantas vezes você já se perdeu hoje? Aposto que mais de três.",
            "Cuidado onde pisa! Eu sou pequena, lembra?",
            "Você sabia que minha magia funciona melhor com abraços?",
            "Você está andando em círculos ou eu que estou tonta?",
            "Esse lugar me dá arrepios. Vamos sair logo daqui.",
            "Se eu tivesse asas maiores, te levaria voando!",
            "Só entre nós: seu estilo de combate é… criativo.",
            "Você me escuta? ...Ou só ignora fadas mesmo?",
            "Você sempre carrega tanta tralha assim?",
            "Se eu sumir, procure em alguma floricultura.",
            "Você dormiu bem? Porque eu te observei a noite toda.",
            "Tem dias que acho que você me escuta... mas só acho.",
            "Olha o sol! Tá lindo... agora volta a minerar.",
            "Você sabia que se olhar pra lua... não acontece nada?",
            "Não vá dormir sem me contar uma história.",
            "Sabe aquele barulho? Não fui eu.",
            "Você devia me dar mais atenção. E comida imaginária.",
            "Eu sou sua consciência mágica. Faça o certo… ou o engraçado.",
            "Fadas não comem... mas adoram cheirar biscoitos.",
            "Você já se perdeu hoje? Tá na hora.",
            "Se fadas tivessem notas, você seria... satisfatório.",
            "Nunca vi ninguém se perder tão rápido.",
            "Você está indo bem! Mentira, mas quero te animar.",
            "Sou pequena, mas percebo seus vacilos.",
            "Você é o herói mais confuso que já escolhi seguir.",
            "Queria poder usar uma espada... por você.",
            "Se fadas tivessem coração, o meu batia por pão.",
            "Você é meu humano favorito. Não conte pros outros.",
            "Essa picareta vai quebrar em 3... 2... 1...",
            "Tenho um segredo pra te contar: você é legal.",
            "Você é a aventura da minha vida... e o perigo também.",
            "Se eu ganhasse um diamante pra cada erro seu… eu seria rica.",
            "Essa sua mania de explorar tudo... contagiante!",
            "Você é como redstone: incompreendido e explosivo.",
            "Dizem que fadas brilham... mas hoje é você que está brilhando. De suor.",
            "Você sabia que os aldeões me acham assustadora? Hehe.",
            "Todo mundo fala que fadas são sábias. Eu sou só falante.",
            "Você coloca blocos como se estivesse brincando de LEGO bêbado.",
            "Sua mochila parece o fim do baú do fim.",
            "Promete que não vai me deixar em cima de um cacto?",
            "Você é meu preferido. Às vezes.",
            "Fadas não mentem. Só exageram.",
            "Se eu tivesse um bloco por pensamento estranho seu…",
            "Você devia montar um show. 'O acrobata das quedas'."
    );


    private static final Random random = new Random();
    private static final HashMap<UUID, Long> ultimoDialogo = new HashMap<>();

    private static boolean podeFalar(Player jogador) {
        long agora = System.currentTimeMillis();
        long ultimo = ultimoDialogo.getOrDefault(jogador.getUniqueId(), 0L);
        if (agora - ultimo >= 5) {
            ultimoDialogo.put(jogador.getUniqueId(), agora);
            return true;
        }
        return false;
    }

    public static void iniciarTarefaFalada() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player jogador : Bukkit.getOnlinePlayers()) {
                    if (!FairyManager.temFada(jogador)) continue;

                    Fairy fada = FairyManager.getFada(jogador);
                    if (fada != null && random.nextDouble() < 0.5) {
                        String mensagem = MENSAGENS_GERAIS.get(random.nextInt(MENSAGENS_GERAIS.size()));
                        if (podeFalar(jogador)) {
                            jogador.sendMessage("§d[✧ Fada ✧] §f" + mensagem);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(MinezinServer.getInstance(), 0L, 20L * 120); // a cada 2 minutos
    }
}
