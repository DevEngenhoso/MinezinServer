package com.engenhoso.serverplugin.commands.fairycommands;

import com.engenhoso.serverplugin.modules.fairy.core.Fairy;
import com.engenhoso.serverplugin.modules.fairy.core.FairyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class RenomearFadaCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player jogador)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        Fairy fada = FairyManager.getFada(jogador);
        if (fada == null) {
            jogador.sendMessage(ChatColor.RED + "Você ainda não possui uma fada.");
            return true;
        }

        if (args.length == 0) {
            jogador.sendMessage(ChatColor.YELLOW + "Uso correto: /renomearfada <novo_nome>");
            return true;
        }

        String novoNome = String.join(" ", args).trim();

        fada.getInventario().setNomeFada(novoNome);
        fada.getInventario().salvarInventario(jogador.getUniqueId());

        // Recarrega a fada para atualizar nome visível
        FairyManager.removerFada(jogador);
        FairyManager.criarOuSubstituirFada(jogador); // Agora sem tentar capturar retorno
        Fairy novaFada = FairyManager.getFada(jogador);
        novaFada.getInventario().carregarInventario(jogador.getUniqueId());

        jogador.sendMessage(ChatColor.GREEN + "Sua fada agora se chama " + ChatColor.AQUA + novoNome + ChatColor.GREEN + "!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
