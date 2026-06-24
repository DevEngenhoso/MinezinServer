package com.engenhoso.serverplugin.features.classes.habilidades;

import com.engenhoso.serverplugin.features.classes.ClasseTipo;
import org.bukkit.Material;

import java.util.*;

public class HabilidadeRegistry {

    private final Map<String, HabilidadeDefinicao> habilidades = new LinkedHashMap<>();

    public HabilidadeRegistry() {
        registrarPadroes();
    }

    private void registrarPadroes() {
        registrarTanque();
        registrarGuerreiro();
        registrarAtirador();
        registrarMago();
        registrarSacerdote();
    }

    private void registrarTanque() {
        registrar(
                "tanque_batida_defensiva",
                "Batida Defensiva",
                ClasseTipo.TANQUE,
                TipoHabilidade.PASSIVA,
                Material.SHIELD,
                "Fortalece o tanque em combate.",
                "Aumenta resistência a dano",
                "e resistência a controle por alguns segundos."
        );

        registrar(
                "tanque_aura_debilitante",
                "Aura Debilitante",
                ClasseTipo.TANQUE,
                TipoHabilidade.PASSIVA,
                Material.WITHER_ROSE,
                "Ao levar dano, cria uma aura debilitante.",
                "Inimigos próximos causam menos dano,",
                "ficam desacelerados e invisíveis são revelados."
        );

        registrar(
                "tanque_escudo_de_forca",
                "Escudo de Força",
                ClasseTipo.TANQUE,
                TipoHabilidade.PASSIVA,
                Material.LIGHT_BLUE_STAINED_GLASS,
                "Ao levar dano, cria um campo defensivo.",
                "Aumenta resistência a dano e cura recebida",
                "do tanque e de aliados próximos."
        );

        registrar(
                "tanque_investida_ardilosa",
                "Investida Ardilosa",
                ClasseTipo.TANQUE,
                TipoHabilidade.COMUM,
                Material.IRON_BOOTS,
                "Avança ou salta até o alvo.",
                "Causa dano físico em área",
                "e aplica controle nos inimigos atingidos."
        );

        registrar(
                "tanque_quebra_chao",
                "Quebra-chão",
                ClasseTipo.TANQUE,
                TipoHabilidade.COMUM,
                Material.CRACKED_STONE_BRICKS,
                "Golpeia o chão na posição alvo.",
                "Causa dano físico em área",
                "e lança inimigos para cima."
        );

        registrar(
                "tanque_vinganca",
                "Vingança",
                ClasseTipo.TANQUE,
                TipoHabilidade.COMUM,
                Material.ECHO_SHARD,
                "Dispara uma onda de choque.",
                "O primeiro inimigo atingido é amaldiçoado.",
                "Depois, puxa inimigos próximos e causa dano mágico."
        );

        registrar(
                "tanque_enfrentamento",
                "Enfrentamento",
                ClasseTipo.TANQUE,
                TipoHabilidade.COMUM,
                Material.IRON_CHESTPLATE,
                "Avança até a posição alvo.",
                "Empurra inimigos pelo caminho",
                "e atordoa os inimigos atingidos."
        );

        registrar(
                "tanque_anel_da_inercia",
                "Anel da Inércia",
                ClasseTipo.TANQUE,
                TipoHabilidade.COMUM,
                Material.IRON_CHAIN,
                "Cria um anel ao redor do tanque.",
                "Inimigos que atravessam o anel",
                "são fortemente desacelerados."
        );

        registrar(
                "tanque_geiser_de_poder",
                "Gêiser de Poder",
                ClasseTipo.TANQUE,
                TipoHabilidade.COMUM,
                Material.AMETHYST_CLUSTER,
                "Cria um gêiser na posição alvo.",
                "Lança inimigos ao ar, causa dano mágico",
                "e empurra aliados para longe do impacto."
        );

        registrar(
                "tanque_racha_terra",
                "Racha-terra",
                ClasseTipo.TANQUE,
                TipoHabilidade.COMUM,
                Material.DEEPSLATE,
                "Despedaça o chão ao redor do tanque.",
                "Causa dano físico, atordoa inimigos",
                "e força monstros a focarem o tanque."
        );

        registrar(
                "tanque_maldicao_do_encolhimento",
                "Maldição do Encolhimento",
                ClasseTipo.TANQUE,
                TipoHabilidade.ULTIMATE,
                Material.FERMENTED_SPIDER_EYE,
                "Canaliza um golpe amaldiçoado.",
                "Reduz a vida máxima dos inimigos",
                "e diminui o dano causado por eles."
        );

        registrar(
                "tanque_aurora_abencoada",
                "Aurora Abençoada",
                ClasseTipo.TANQUE,
                TipoHabilidade.ULTIMATE,
                Material.BEACON,
                "Cria uma aurora protetora.",
                "Concede velocidade, escudo e cura",
                "para aliados próximos."
        );

        registrar(
                "tanque_salto_profundo",
                "Salto Profundo",
                ClasseTipo.TANQUE,
                TipoHabilidade.ULTIMATE,
                Material.RABBIT_FOOT,
                "Salta até uma área alvo.",
                "Fica imune durante o salto.",
                "Ao cair, causa dano, lentidão e atordoamento."
        );

        registrar(
                "tanque_passos_gigantes",
                "Passos Gigantes",
                ClasseTipo.TANQUE,
                TipoHabilidade.ULTIMATE,
                Material.NETHERITE_CHESTPLATE,
                "O tanque cresce e fica gigante.",
                "Cria uma zona de pressão que desacelera inimigos",
                "e pode esmagar o chão a cada 3 segundos."
        );

        registrar(
                "tanque_massacre",
                "Massacre",
                ClasseTipo.TANQUE,
                TipoHabilidade.ULTIMATE,
                Material.ANVIL,
                "Avança girando o martelo.",
                "Puxa inimigos atravessados e causa dano repetido.",
                "Ao chegar, bate no chão e lança inimigos ao ar."
        );

        registrar(
                "tanque_hiperestatica",
                "Hiperestática",
                ClasseTipo.TANQUE,
                TipoHabilidade.ULTIMATE,
                Material.LIGHTNING_ROD,
                "Cria uma veia de energia no chão.",
                "Aliados recebem escudo.",
                "Inimigos sofrem dano, estática e atordoamento."
        );
    }

    private void registrarGuerreiro() {
        registrar(
                "guerreiro_tenacidade",
                "Tenacidade",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.PASSIVA,
                Material.SHIELD,
                "Quando fica com pouca vida,",
                "recebe resistência temporária",
                "para sobreviver melhor em combate."
        );

        registrar(
                "guerreiro_cutelo_heroico",
                "Cutelo Heroico",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.IRON_SWORD,
                "Balança a espada ao redor.",
                "Causa dano físico em área.",
                "Ao acertar inimigos, ganha velocidade."
        );

        registrar(
                "guerreiro_corte_separador",
                "Corte Separador",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.IRON_AXE,
                "Rasga o chão em linha reta.",
                "Causa dano físico",
                "e enraíza inimigos atingidos."
        );

        registrar(
                "guerreiro_golpe_fulgaz",
                "Golpe Fulgaz",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.DIAMOND_SWORD,
                "Entra em postura defensiva.",
                "Fica imune durante uma canalização curta.",
                "Ao final, reflete dano e golpeia ao redor."
        );

        registrar(
                "guerreiro_investida",
                "Investida",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.IRON_BOOTS,
                "Avança rapidamente contra o alvo.",
                "Fica imune a controles durante o avanço.",
                "Causa dano físico e atordoa no impacto."
        );

        registrar(
                "guerreiro_laminas_enfurecidas",
                "Lâminas Enfurecidas",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.BLAZE_ROD,
                "Cria lâminas girando ao redor.",
                "Causa dano periódico em inimigos próximos.",
                "Aumenta o dano conforme atinge alvos."
        );

        registrar(
                "guerreiro_furia_dilacerante",
                "Fúria Dilacerante",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.NETHERITE_AXE,
                "Executa cortes em cone.",
                "Aplica sangramento nos inimigos.",
                "Pode finalizar com salto e enraizamento."
        );

        registrar(
                "guerreiro_pico_de_adrenalina",
                "Pico de Adrenalina",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.SUGAR,
                "Aumenta dano, velocidade de movimento",
                "e velocidade de ataque por alguns segundos.",
                "O efeito termina se parar de causar dano."
        );

        registrar(
                "guerreiro_golpe_de_corrente",
                "Golpe de Corrente",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.IRON_CHAIN,
                "Avança até um inimigo.",
                "Pode saltar entre alvos próximos",
                "causando dano em sequência."
        );

        registrar(
                "guerreiro_adaga_sombria",
                "Adaga Sombria",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.COMUM,
                Material.FLINT,
                "Arremessa uma faca em linha reta.",
                "Atordoa o primeiro inimigo atingido",
                "e puxa o guerreiro para trás do alvo."
        );

        registrar(
                "guerreiro_golpe_crescente",
                "Golpe Crescente",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.ULTIMATE,
                Material.SCULK,
                "Lança uma onda demoníaca em linha reta.",
                "Perfura inimigos e causa dano mágico.",
                "Pode aplicar lentidão, silêncio ou dano extra."
        );

        registrar(
                "guerreiro_corrente_desalmada",
                "Corrente Desalmada",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.ULTIMATE,
                Material.SOUL_LANTERN,
                "Libera ondas mágicas ao redor.",
                "Causa dano mágico",
                "e reduz temporariamente a vida máxima dos inimigos."
        );

        registrar(
                "guerreiro_golpe_majestoso",
                "Golpe Majestoso",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.ULTIMATE,
                Material.NETHERITE_SWORD,
                "Lança inimigos ao ar.",
                "Depois desfere um golpe vertical poderoso",
                "em linha à frente e atrás."
        );

        registrar(
                "guerreiro_ultrapassando_limites",
                "Ultrapassando Limites",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.ULTIMATE,
                Material.NETHER_STAR,
                "Aumenta velocidade e dano.",
                "Depois libera golpes fortes",
                "em cone à frente."
        );

        registrar(
                "guerreiro_corte_laminar",
                "Corte Laminar",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.ULTIMATE,
                Material.PRISMARINE_SHARD,
                "Salta na direção alvo.",
                "Causa grande dano físico em cone",
                "e converte parte do dano em cura."
        );

        registrar(
                "guerreiro_desembainhar",
                "Desembainhar",
                ClasseTipo.GUERREIRO,
                TipoHabilidade.ULTIMATE,
                Material.SHEARS,
                "Prende o alvo no lugar.",
                "Canaliza cortes rápidos, interrompe habilidades",
                "e quebra escudos ativos."
        );
    }

    private void registrarAtirador() {
        registrar(
                "atirador_flechas_perfurantes",
                "Flechas Perfurantes",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.PASSIVA,
                Material.ARROW,
                "Cada flecha acertada reduz a defesa",
                "do inimigo em 2% por 3 segundos.",
                "Acumula até 4 vezes."
        );

        registrar(
                "atirador_reposicionar",
                "Reposicionar",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.PASSIVA,
                Material.RABBIT_FOOT,
                "Ao acertar uma flecha em um inimigo,",
                "recebe Velocidade I",
                "por 5 segundos."
        );

        registrar(
                "atirador_multidisparo",
                "Multidisparo",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.BOW,
                "Libera uma salva de flechas em cone.",
                "Causa dano físico",
                "e desacelera inimigos atingidos."
        );

        registrar(
                "atirador_tiro_letal",
                "Tiro Letal",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.SPECTRAL_ARROW,
                "Dispara uma flecha perfurante.",
                "Atravessa inimigos, causa dano físico",
                "e reduz resistências a dano."
        );

        registrar(
                "atirador_raio_de_luz",
                "Raio de Luz",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.GLOWSTONE_DUST,
                "Dispara uma flecha para o céu.",
                "Ela cai após um atraso, causando dano mágico",
                "e enraizando inimigos próximos."
        );

        registrar(
                "atirador_tiro_de_gelo",
                "Tiro de Gelo",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.BLUE_ICE,
                "Dispara uma flecha de gelo.",
                "Explode em área, desacelera inimigos",
                "e empurra o atirador para trás."
        );

        registrar(
                "atirador_flecha_magica",
                "Flecha Mágica",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.AMETHYST_SHARD,
                "Dispara uma flecha mágica de longo alcance.",
                "Causa alto dano mágico",
                "e pode causar dano adicional em área."
        );

        registrar(
                "atirador_furia_do_ceu",
                "Fúria do Céu",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.FEATHER,
                "Salta até uma posição alvo.",
                "Cria um ponto seguro no ar",
                "e permite disparar Flechas Celestes."
        );

        registrar(
                "atirador_flechas_explosivas",
                "Flechas Explosivas",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.FIREWORK_ROCKET,
                "Encanta as próximas flechas.",
                "Elas explodem no impacto",
                "e causam dano mágico em área."
        );

        registrar(
                "atirador_tiro_explosivo",
                "Tiro Explosivo",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.TNT,
                "Lança uma bomba pegajosa no alvo.",
                "Causa dano inicial",
                "e explode depois de alguns segundos."
        );

        registrar(
                "atirador_salva_explosiva",
                "Salva Explosiva",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.COMUM,
                Material.CROSSBOW,
                "Lança virotes explosivos em uma área.",
                "Causa dano físico",
                "e cria gases corrosivos."
        );

        registrar(
                "atirador_chuva_de_flechas",
                "Chuva de Flechas",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.TIPPED_ARROW,
                "Canaliza salvas de flechas vindas do céu.",
                "Causa dano físico repetido em área",
                "e desacelera inimigos."
        );

        registrar(
                "atirador_flecha_demoniaca",
                "Flecha Demoníaca",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.NETHERITE_SCRAP,
                "Dispara uma flecha demoníaca perfurante.",
                "O dano aumenta conforme a quantidade",
                "de inimigos atingidos."
        );

        registrar(
                "atirador_falcao_luminoso",
                "Falcão Luminoso",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.PHANTOM_MEMBRANE,
                "Libera um falcão luminoso em linha reta.",
                "Atravessa inimigos e causa dano mágico.",
                "Pode ser relançado algumas vezes."
        );

        registrar(
                "atirador_tempestade_furiosa",
                "Tempestade Furiosa",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.LIGHTNING_ROD,
                "Cria uma tempestade no impacto.",
                "Causa dano mágico contínuo em área",
                "e interrompe conjurações ao causar dano."
        );

        registrar(
                "atirador_tiro_de_precisao",
                "Tiro de Precisão",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.SPYGLASS,
                "Mira e dispara um tiro extremamente forte.",
                "Causa alto dano físico",
                "e reduz bastante as resistências do alvo."
        );

        registrar(
                "atirador_climax_mortifero",
                "Clímax Mortífero",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.TARGET,
                "Canaliza uma barreira ofensiva no alvo.",
                "Causa dano físico repetido",
                "que aumenta conforme os acertos."
        );

        registrar(
                "atirador_barreira_perversa",
                "Barreira Perversa",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.NETHER_BRICK_FENCE,
                "Canaliza salvas em cone à frente.",
                "Aumenta a defesa do atirador",
                "e causa dano mágico e verdadeiro."
        );

        registrar(
                "atirador_motor_divino",
                "Motor Divino",
                ClasseTipo.ATIRADOR,
                TipoHabilidade.ULTIMATE,
                Material.END_ROD,
                "Canaliza um raio longo de energia.",
                "Perfura inimigos em linha",
                "e causa dano mágico contínuo."
        );
    }

    private void registrarMago() {
        registrar(
                "mago_queimar",
                "Queimar",
                ClasseTipo.MAGO,
                TipoHabilidade.PASSIVA,
                Material.FLINT_AND_STEEL,
                "Ao causar dano em qualquer criatura,",
                "aplica fogo no alvo",
                "por alguns segundos."
        );

        registrar(
                "mago_ultimo_recurso",
                "Último Recurso",
                ClasseTipo.MAGO,
                TipoHabilidade.PASSIVA,
                Material.NETHER_STAR,
                "Usar uma ultimate enquanto todas",
                "as habilidades comuns estão em recarga",
                "faz a ultimate causar dano aumentado."
        );

        registrar(
                "mago_emissao_de_energia",
                "Emissão de Energia",
                ClasseTipo.MAGO,
                TipoHabilidade.PASSIVA,
                Material.HEART_OF_THE_SEA,
                "Ao receber dano, cria uma bolha de energia.",
                "Empurra inimigos que tocarem nela",
                "e concede imunidade contra movimento forçado."
        );

        registrar(
                "mago_chao_podre",
                "Chão Podre",
                ClasseTipo.MAGO,
                TipoHabilidade.PASSIVA,
                Material.MYCELIUM,
                "Ao conjurar uma ultimate,",
                "corrompe o chão sob seus pés.",
                "Inimigos na trilha têm cura e resistência reduzidas."
        );

        registrar(
                "mago_bomba_glacial",
                "Bomba Glacial",
                ClasseTipo.MAGO,
                TipoHabilidade.COMUM,
                Material.PACKED_ICE,
                "Cria uma bomba de gelo no alvo.",
                "Desacelera inimigos na área",
                "e explode após um curto tempo."
        );

        registrar(
                "mago_lamina_de_gelo",
                "Lâmina de Gelo",
                ClasseTipo.MAGO,
                TipoHabilidade.COMUM,
                Material.ICE,
                "Faz cair uma lâmina de gelo.",
                "Explode no impacto",
                "e causa dano mágico em área pequena."
        );

        registrar(
                "mago_explosao_congelante",
                "Explosão Congelante",
                ClasseTipo.MAGO,
                TipoHabilidade.COMUM,
                Material.SNOWBALL,
                "Teleporta o mago até a posição alvo.",
                "Causa uma explosão congelante,",
                "atordoa inimigos e concede breve imunidade."
        );

        registrar(
                "mago_onda_de_fogo",
                "Onda de Fogo",
                ClasseTipo.MAGO,
                TipoHabilidade.COMUM,
                Material.BLAZE_POWDER,
                "Libera um cone de fogo à frente.",
                "Incendeia inimigos, causa dano contínuo",
                "e empurra os alvos atingidos."
        );

        registrar(
                "mago_campo_ardente",
                "Campo Ardente",
                ClasseTipo.MAGO,
                TipoHabilidade.COMUM,
                Material.CAMPFIRE,
                "Lança uma orbe ardente na posição alvo.",
                "Causa dano inicial",
                "e deixa fogo no chão por alguns segundos."
        );

        registrar(
                "mago_dominio_do_monarca",
                "Domínio do Monarca",
                ClasseTipo.MAGO,
                TipoHabilidade.ULTIMATE,
                Material.SCULK_CATALYST,
                "Cria uma área sombria.",
                "Fortalece aliados sombrios",
                "e enfraquece inimigos dentro do domínio."
        );

        registrar(
                "mago_bubble_trouble",
                "Bubble Trouble",
                ClasseTipo.MAGO,
                TipoHabilidade.ULTIMATE,
                Material.SLIME_BALL,
                "Cria um campo minado mágico de bolhas.",
                "As bolhas explodem quando inimigos",
                "encostam ou passam perto."
        );

        registrar(
                "mago_cristal_de_gelo",
                "Cristal de Gelo",
                ClasseTipo.MAGO,
                TipoHabilidade.ULTIMATE,
                Material.BLUE_ICE,
                "Cria um cristal de gelo na posição alvo.",
                "Atordoa inimigos próximos",
                "e depois explode causando grande dano."
        );

        registrar(
                "mago_obelisco_glacial",
                "Obelisco Glacial",
                ClasseTipo.MAGO,
                TipoHabilidade.ULTIMATE,
                Material.PRISMARINE,
                "Invoca um obelisco glacial.",
                "Aumenta dano e recarga de aliados próximos.",
                "Pode ser detonado para causar dano e lentidão."
        );

        registrar(
                "mago_esfera_de_lava",
                "Esfera de Lava",
                ClasseTipo.MAGO,
                TipoHabilidade.ULTIMATE,
                Material.MAGMA_BLOCK,
                "Lança uma esfera de magma.",
                "Ela rola para frente, atravessa inimigos",
                "e causa dano mágico pesado."
        );

        registrar(
                "mago_artilharia_de_fogo",
                "Artilharia de Fogo",
                ClasseTipo.MAGO,
                TipoHabilidade.ULTIMATE,
                Material.FIRE_CHARGE,
                "Canaliza e dispara orbes de fogo.",
                "Cada impacto causa dano em área",
                "e deixa rastros ardentes."
        );

        registrar(
                "mago_tornado_de_fogo",
                "Tornado de Fogo",
                ClasseTipo.MAGO,
                TipoHabilidade.ULTIMATE,
                Material.BLAZE_ROD,
                "Cria um tornado flamejante em área.",
                "Causa dano mágico contínuo.",
                "Inimigos que saem continuam queimando."
        );
    }

    private void registrarSacerdote() {
        registrar(
                "sacerdote_fe_inabalavel",
                "Fé Inabalável",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.PASSIVA,
                Material.ENCHANTED_GOLDEN_APPLE,
                "Melhora o poder de cura",
                "ou reduz o dano recebido",
                "quando está próximo de aliados."
        );

        registrar(
                "sacerdote_luz_sagrada",
                "Luz Sagrada",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.COMUM,
                Material.GLOWSTONE_DUST,
                "Conjura uma luz sagrada na posição alvo.",
                "Cura aliados",
                "em uma área próxima."
        );

        registrar(
                "sacerdote_orbe_sagrada",
                "Orbe Sagrada",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.COMUM,
                Material.ENDER_PEARL,
                "Lança uma orbe sagrada.",
                "Ao impactar, explode",
                "e cura aliados próximos."
        );

        registrar(
                "sacerdote_punicao",
                "Punição",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.COMUM,
                Material.GOLDEN_SWORD,
                "Causa dano mágico em área.",
                "Inimigos atingidos recebem dano adicional",
                "na próxima vez que tomarem dano direto."
        );

        registrar(
                "sacerdote_pulsacao_sagrada",
                "Pulsação Sagrada",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.COMUM,
                Material.AMETHYST_SHARD,
                "Emite uma pulsação sagrada.",
                "Cura aliados próximos",
                "e pode afetar inimigos próximos."
        );

        registrar(
                "sacerdote_raio_sagrado",
                "Raio Sagrado",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.COMUM,
                Material.END_ROD,
                "Canaliza um raio de cura em um aliado.",
                "Restaura vida repetidamente",
                "e aumenta a cura durante a canalização."
        );

        registrar(
                "sacerdote_intervencao_divina",
                "Intervenção Divina",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.COMUM,
                Material.FEATHER,
                "Salta até a posição alvo.",
                "Fica imune durante o deslocamento.",
                "No impacto, cura aliados e lança inimigos ao ar."
        );

        registrar(
                "sacerdote_protecao_divina",
                "Proteção Divina",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.ULTIMATE,
                Material.SHIELD,
                "Aplica um escudo em um aliado.",
                "Pode ser reutilizada para explodir o escudo",
                "e curar aliados próximos."
        );

        registrar(
                "sacerdote_despertar",
                "Despertar",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.ULTIMATE,
                Material.TOTEM_OF_UNDYING,
                "Reanima um aliado nocauteado.",
                "Restaura parte da vida máxima",
                "e impede nova reanimação por um tempo."
        );

        registrar(
                "sacerdote_salvacao",
                "Salvação",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.ULTIMATE,
                Material.BEACON,
                "Cria uma área de salvação.",
                "Após carregar, cura muitos aliados,",
                "concede imunidade e purifica efeitos negativos."
        );

        registrar(
                "sacerdote_esfera_celestial",
                "Esfera Celestial",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.ULTIMATE,
                Material.END_CRYSTAL,
                "Invoca uma esfera saltitante.",
                "Ela cura aliados",
                "pulando entre alvos próximos."
        );

        registrar(
                "sacerdote_julgamento_sagrado",
                "Julgamento Sagrado",
                ClasseTipo.SACERDOTE,
                TipoHabilidade.ULTIMATE,
                Material.GOLDEN_AXE,
                "Cria uma área sagrada.",
                "Causa dano em inimigos",
                "e cura aliados ao mesmo tempo."
        );
    }

    private void registrar(
            String id,
            String nome,
            ClasseTipo classe,
            TipoHabilidade tipo,
            Material material,
            String... descricao
    ) {
        registrar(new HabilidadeDefinicao(
                id,
                nome,
                classe,
                tipo,
                material,
                Arrays.asList(descricao),
                true
        ));
    }

    public void registrar(HabilidadeDefinicao habilidade) {
        habilidades.put(habilidade.getId().toLowerCase(Locale.ROOT), habilidade);
    }

    public HabilidadeDefinicao get(String id) {
        if (id == null) {
            return null;
        }

        return habilidades.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<HabilidadeDefinicao> getTodas() {
        return habilidades.values();
    }

    public List<HabilidadeDefinicao> getPorClasse(ClasseTipo classe) {
        List<HabilidadeDefinicao> resultado = new ArrayList<>();

        for (HabilidadeDefinicao habilidade : habilidades.values()) {
            if (habilidade.getClasse() == classe) {
                resultado.add(habilidade);
            }
        }

        return resultado;
    }
}