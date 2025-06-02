
package com.engenhoso.serverplugin.fairy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FairyMessageFileCreator {

    public static void criarSeNaoExistir(File pastaPlugin) {
        File yml = new File(pastaPlugin, "fairy_messages.yml");
        if (yml.exists()) return;

        try (FileWriter writer = new FileWriter(yml)) {
            writer.write("FADA_ELOGIA:\n");
            writer.write("- Você está incrível hoje!\n");
            writer.write("- Se eu tivesse que escolher um herói, escolheria você.\n");
            writer.write("- Você me inspira a ser uma fada melhor.\n");
            writer.write("FADA_FLERTA:\n");
            writer.write("- Se eu tivesse um coração... acho que bateria mais forte agora.\n");
            writer.write("- Entre todas as criaturas... eu escolhi você.\n");
            writer.write("- Sabe que você é meio encantador, né?\n");
            writer.write("FADA_IRONIZA:\n");
            writer.write("- Ótimo plano... morrer pra lava.\n");
            writer.write("- Sério que você caiu de novo?\n");
            writer.write("- Talento puro, né? SQN.\n");
            writer.write("FADA_SENTE_SAUDADES:\n");
            writer.write("- Você me deixou sozinha... fiquei com saudades!\n");
            writer.write("- Por onde andou? Senti sua falta!\n");
            writer.write("- Achei que tinha me esquecido... mas fico feliz que voltou.\n");
            writer.write("PLAYER_ADVANCEMENT:\n");
            writer.write("- Uma conquista! Isso sim é digno de contos de fada!\n");
            writer.write("- Uau! Que avanço incrível!\n");
            writer.write("- Você está progredindo tão rápido, fico orgulhosa!\n");
            writer.write("PLAYER_CRAFT_ITEM:\n");
            writer.write("- Você é um artesão nato! Posso ser seu mascote oficial?\n");
            writer.write("- Mais um item? Você não para mesmo!\n");
            writer.write("- Um dia você ainda vai construir um castelo com essas mãos.\n");
            writer.write("PLAYER_DIES:\n");
            writer.write("- Você morreu de novo? Eu já perdi a conta.\n");
            writer.write("- Não fique triste... foi só mais uma morte.\n");
            writer.write("- Quer que eu te carregue da próxima vez?\n");
            writer.write("PLAYER_EAT_ITEM:\n");
            writer.write("- Hmm… parecia gostoso. Será que tem versão para fadas?\n");
            writer.write("- Comendo de novo? Eu gosto de ver você bem alimentado!\n");
            writer.write("- Você come como um aventureiro de verdade!\n");
            writer.write("PLAYER_ENTER_END:\n");
            writer.write("- Esse lugar me dá arrepios...\n");
            writer.write("- Você não vai me deixar sozinha aqui, vai?\n");
            writer.write("- Se o Dragão aparecer, grita meu nome!\n");
            writer.write("PLAYER_ENTER_NETHER:\n");
            writer.write("- Ugh... esse lugar fede enxofre.\n");
            writer.write("- Se você morrer aqui, não me culpe!\n");
            writer.write("- Tenha cuidado! O Nether não é lugar pra fadas...\n");
            writer.write("PLAYER_INACTIVE:\n");
            writer.write("- Você tá virando estátua? Eu já quase te desenhei aqui.\n");
            writer.write("- Acorda! A aventura não vai se viver sozinha.\n");
            writer.write("- Se você estiver dormindo... ronca baixinho, por favor.\n");
            writer.write("PLAYER_KILL_MOB:\n");
            writer.write("- Você luta como um verdadeiro herói!\n");
            writer.write("- Mais um monstro derrotado! Impressionante.\n");
            writer.write("- Tá colecionando almas agora?\n");
            writer.write("PLAYER_NEAR_FLOWERS:\n");
            writer.write("- Essas flores são lindas... mas você ganha!\n");
            writer.write("- Será que tem pó mágico nelas?\n");
            writer.write("- Fadas gostam de flores. Só dizendo...\n");
            writer.write("PLAYER_RESPAWN:\n");
            writer.write("- Achou que morrendo ia fugir de mim? Hihihi!\n");
            writer.write("- Eu disse que estaríamos juntos... mesmo depois da morte.\n");
            writer.write("- Bem-vindo de volta ao mundo dos vivos!\n");
            writer.write("PLAYER_RETURN_OVERWORLD:\n");
            writer.write("- Ahhh... nada como estar em casa de novo.\n");
            writer.write("- O Overworld sentiu sua falta... e eu também!\n");
            writer.write("- Eu sabia que você voltaria inteiro.\n");
            writer.write("PLAYER_TAKE_DAMAGE:\n");
            writer.write("- Cuidado! Nem minhas asas curam pancada!\n");
            writer.write("- Você sempre se machuca assim?\n");
            writer.write("- Tenta desviar da próxima vez, tá bom?\n");
            writer.write("PLAYER_USE_TOTEM:\n");
            writer.write("- Você quase foi dessa pra melhor!\n");
            writer.write("- Ufa! O Totem salvou você por pouco.\n");
            writer.write("- Talvez você devesse tomar mais cuidado...\n");
            writer.write("TIME_LONG_SESSION:\n");
            writer.write("- Você já está jogando há um tempinho...\n");
            writer.write("- Pega uma água, dá uma esticada... eu espero!\n");
            writer.write("- Quer que eu cante pra passar o tempo?\n");
            writer.write("TIME_STORM:\n");
            writer.write("- Está trovejando lá fora... segura minha mão!\n");
            writer.write("- Fadas não gostam de trovões!\n");
            writer.write("- Acho melhor se abrigar, está perigoso.\n");
            writer.flush();
        } catch (IOException e) {
            System.out.println("[Fada] Não foi possível criar fairy_messages.yml: " + e.getMessage());
        }
    }
}
