package com.engenhoso.serverplugin.features.players;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerProfileService {

    private final JavaPlugin plugin;
    private final PlayerProfileRepository repository;

    private final Map<UUID, PlayerProfile> cache = new ConcurrentHashMap<>();

    public PlayerProfileService(JavaPlugin plugin, PlayerProfileRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public PlayerProfile carregarOuCriar(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        try {
            PlayerProfile profile = repository.findOrCreate(uuid, name);
            cache.put(uuid, profile);

            plugin.getLogger().info("[PlayerProfile] Perfil carregado/criado para " + name + ".");

            return profile;
        } catch (SQLException exception) {
            plugin.getLogger().severe("[PlayerProfile] Falha ao carregar/criar perfil de " + name + ".");
            exception.printStackTrace();

            throw new IllegalStateException("Não foi possível carregar/criar o perfil do jogador.", exception);
        }
    }

    public void salvar(PlayerProfile profile) {
        try {
            repository.save(profile);
            plugin.getLogger().info("[PlayerProfile] Perfil salvo para " + profile.getName() + ".");
        } catch (SQLException exception) {
            plugin.getLogger().severe("[PlayerProfile] Falha ao salvar perfil de " + profile.getName() + ".");
            exception.printStackTrace();
        }
    }

    public void salvarERemover(UUID uuid) {
        PlayerProfile profile = cache.remove(uuid);

        if (profile == null) {
            return;
        }

        salvar(profile);
    }

    public void salvarTodos() {
        Collection<PlayerProfile> profiles = cache.values();

        for (PlayerProfile profile : profiles) {
            salvar(profile);
        }

        cache.clear();
    }

    public Optional<PlayerProfile> getProfile(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    public Optional<PlayerProfile> getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public boolean hasProfile(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public PlayerProfile getOrLoad(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), uuid -> carregarOuCriar(player));
    }

    public Optional<String> obterClasse(UUID uuid) {
        return getProfile(uuid)
                .map(PlayerProfile::getClasse)
                .filter(classe -> classe != null && !classe.isBlank());
    }

    public boolean definirClasse(Player player, String classe) {
        PlayerProfile profile = getOrLoad(player);

        if (profile.hasClasse()) {
            return false;
        }

        profile.setClasse(classe);
        profile.setPrologoConcluido(true);

        salvar(profile);
        return true;
    }

    public boolean resetarClasse(Player player) {
        PlayerProfile profile = getOrLoad(player);

        if (!profile.hasClasse()) {
            return false;
        }

        profile.setClasse(null);
        profile.setPrologoConcluido(false);

        salvar(profile);
        return true;
    }

    public boolean resetarClassePorIdentificador(String identificador) {
        try {
            Optional<PlayerProfile> optionalProfile = buscarPorIdentificador(identificador);

            if (optionalProfile.isEmpty()) {
                return false;
            }

            PlayerProfile profile = optionalProfile.get();

            if (!profile.hasClasse()) {
                return false;
            }

            profile.setClasse(null);
            profile.setPrologoConcluido(false);

            PlayerProfile cachedProfile = cache.get(profile.getUuid());

            if (cachedProfile != null) {
                cachedProfile.setClasse(null);
                cachedProfile.setPrologoConcluido(false);
                salvar(cachedProfile);
            } else {
                repository.save(profile);
            }

            plugin.getLogger().info("[PlayerProfile] Classe resetada para " + profile.getName() + ".");
            return true;
        } catch (SQLException exception) {
            plugin.getLogger().severe("[PlayerProfile] Falha ao resetar classe de " + identificador + ".");
            exception.printStackTrace();
            return false;
        }
    }

    public Optional<PlayerProfile> buscarPerfilPorIdentificador(String identificador) {
        try {
            return buscarPorIdentificador(identificador);
        } catch (SQLException exception) {
            plugin.getLogger().severe("[PlayerProfile] Falha ao buscar perfil: " + identificador);
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<PlayerProfile> buscarPorIdentificador(String identificador) throws SQLException {
        try {
            UUID uuid = UUID.fromString(identificador);

            PlayerProfile cachedProfile = cache.get(uuid);

            if (cachedProfile != null) {
                return Optional.of(cachedProfile);
            }

            return repository.findByUuid(uuid);
        } catch (IllegalArgumentException ignored) {
            Optional<PlayerProfile> cachedByName = buscarNoCachePorNome(identificador);

            if (cachedByName.isPresent()) {
                return cachedByName;
            }

            return repository.findByName(identificador);
        }
    }

    private Optional<PlayerProfile> buscarNoCachePorNome(String name) {
        return cache.values()
                .stream()
                .filter(profile -> profile.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}