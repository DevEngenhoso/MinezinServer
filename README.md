# MinezinServer

> **Um plugin RPG para Minecraft focado em classes, progressão, habilidades, mundos narrativos e presença épica dentro do servidor.**

**MinezinServer** é a fundação de um RPG dentro do Minecraft, construído sobre Paper, com sistemas próprios de classe, perfil persistente, habilidades, mundo inicial, progressão e estrutura modular preparada para capítulos, bosses, dungeons e eventos futuros.

O projeto nasce com uma proposta simples:

> transformar um servidor Minecraft em uma experiência de RPG com identidade própria.

Não é apenas um plugin de comandos.
É uma base para um mundo.

---

## Visão do Projeto

MinezinServer busca criar uma jornada onde cada jogador entra em um mundo desconhecido, escolhe sua classe, constrói sua identidade e progride dentro de uma estrutura RPG feita para crescer com o tempo.

A experiência começa no **Limiar**, um espaço especial onde o jogador realiza sua primeira escolha definitiva: sua classe.

A partir dessa escolha, o jogador passa a carregar uma identidade mecânica e narrativa dentro do servidor.

---

## Principais Sistemas

### Limiar

O **Limiar** é o ponto inicial da jornada.

Antes de entrar no mundo principal, o jogador é levado para um espaço separado onde deve escolher sua classe. Essa escolha define seu caminho inicial dentro do RPG.

O sistema inclui:

* mundo próprio para o Limiar;
* mundo survival separado;
* bloqueio de entrada no survival sem classe;
* totens/manequins de seleção;
* confirmação de escolha por interface;
* portal de acesso ao mundo principal;
* comandos administrativos para configuração.

---

### Classes

O plugin possui cinco classes principais:

| Classe        | Identidade                       |
| ------------- | -------------------------------- |
| **Tanque**    | resistência, proteção e controle |
| **Guerreiro** | combate corpo a corpo e pressão  |
| **Atirador**  | distância, precisão e mobilidade |
| **Mago**      | dano mágico, controle e explosão |
| **Sacerdote** | suporte, cura e proteção         |

A classe é persistente e define quais habilidades o jogador poderá usar.

---

### Sistema de Habilidades

O MinezinServer possui uma estrutura própria de habilidades, com separação por tipo e por classe.

Cada jogador pode montar um loadout composto por:

* **1 habilidade passiva**
* **4 habilidades comuns**
* **1 ultimate**

As habilidades são organizadas por classe, nível, tipo e desbloqueio.

O sistema inclui:

* catálogo de habilidades;
* menu de habilidades;
* loadout individual;
* persistência por jogador;
* níveis de habilidade;
* executor de habilidades;
* cooldowns;
* efeitos visuais;
* efeitos sonoros;
* controle de combate.

---

### Skillbar Temporária

Durante o combate, o jogador pode ativar uma hotbar especial de habilidades.

Essa hotbar substitui temporariamente a hotbar original do jogador, permitindo usar habilidades sem bagunçar o inventário real.

O sistema:

* salva a hotbar original;
* exibe habilidades equipadas;
* executa habilidades com clique direito;
* restaura a hotbar original;
* bloqueia drop, clique e drag dos itens temporários;
* remove itens falsos em logout, kick, morte e login;
* impede o uso de habilidades dentro do Limiar.

---

### Perfil Persistente

Cada jogador possui um perfil salvo no banco de dados.

O perfil armazena informações importantes para a progressão do RPG, como:

* UUID;
* nome;
* classe escolhida;
* nível;
* experiência;
* pontos de habilidade;
* progresso inicial;
* instância atual;
* localização de retorno;
* dados de party e capítulos futuros.

Esse sistema permite que a jornada do jogador continue mesmo após sair do servidor.

---

### Banco de Dados

O plugin possui integração com banco de dados usando MariaDB.

A estrutura atual inclui:

* configuração via `config.yml`;
* serviço de conexão;
* criação inicial de tabelas;
* repository/service para perfis;
* base preparada para expansão futura.

---

### Aura

O MinezinServer possui um sistema de aura especial, usado como recurso visual e conceitual.

A aura representa presença, identidade e poder dentro do servidor.

Também existe suporte para itens especiais e efeitos cosméticos ligados à identidade do projeto, como a **Skeleton Piercer**.

---

### Interface Holográfica

O projeto possui uma base compartilhada para interfaces holográficas usando entidades do Minecraft moderno.

Essa estrutura permite criar menus e interações visuais no mundo, sem depender apenas de inventários.

Ela usa:

* `TextDisplay`;
* `Interaction`;
* botões holográficos;
* linhas de texto;
* contexto de interação;
* ações customizadas.

Essa base será usada para interações narrativas, confirmação de escolhas, portais, totens e sistemas futuros.

---

## Comandos Principais

### Jogador

| Comando        | Descrição                  |
| -------------- | -------------------------- |
| `/habilidades` | Abre o menu de habilidades |
| `/skills`      | Alias para `/habilidades`  |
| `/toggle aura` | Liga ou desliga a aura     |

---

### Administração

| Comando | Descrição                                  |
| ------- | ------------------------------------------ |
| `/mz`   | Comando administrativo principal do plugin |

O comando `/mz` concentra ferramentas internas para configuração, debug e administração dos sistemas do RPG.

Entre suas funções estão:

* visualizar perfil de jogadores;
* alterar nível;
* alterar experiência;
* alterar pontos de habilidade;
* resetar classe;
* configurar pontos do Limiar;
* gerenciar totens;
* teleportar para mundos do plugin.

---

## Estrutura Técnica

O plugin é organizado em módulos, separando cada sistema em sua própria feature.

Estrutura geral:

```text
com.engenhoso.serverplugin
├── core
│   ├── command
│   ├── database
│   └── module
│
├── features
│   ├── admin
│   ├── aura
│   ├── classes
│   ├── deathtitle
│   ├── dimensionlock
│   ├── limiar
│   ├── players
│   └── scoreboard
│
└── shared
    └── hologram
```

A proposta é manter cada sistema isolado, testável e expansível.

---

## Tecnologias

* Java
* Paper API
* Maven
* MariaDB
* HikariCP
* Bukkit/Paper events
* PersistentDataContainer
* TextDisplay / Interaction entities

---

## Configuração

O plugin utiliza `config.yml` para configurações principais.

Exemplo conceitual:

```yml
database:
  host: localhost
  port: 3306
  database: minezinserver
  username: root
  password: senha
```

Antes de iniciar o servidor, configure corretamente o banco de dados.

---

## Status do Projeto

O MinezinServer está em desenvolvimento ativo.

Atualmente, o projeto já possui a fundação principal para:

* entrada do jogador pelo Limiar;
* escolha definitiva de classe;
* perfil persistente;
* sistema de habilidades;
* skillbar de combate;
* banco de dados;
* comandos administrativos;
* estrutura modular;
* interfaces holográficas;
* expansão futura de narrativa.

---

## Roadmap

Ideias e sistemas planejados para as próximas fases:

* progressão completa de jogador;
* balanceamento de classes;
* novas habilidades para todas as classes;
* sistema de party;
* dungeons instanciadas;
* bosses narrativos;
* capítulos de história;
* sistema de fama;
* presença de jogadores poderosos;
* world level;
* loot individual;
* rankings;
* eventos de servidor;
* integração maior com interfaces holográficas.

---

## Filosofia do Plugin

MinezinServer não busca apenas adicionar mecânicas isoladas.

A ideia é criar uma experiência onde cada sistema conversa com o outro:

* classe define identidade;
* habilidades definem estilo;
* progressão define crescimento;
* mundo define contexto;
* bosses definem propósito;
* presença define impacto.

O objetivo é fazer o jogador sentir que entrou em um RPG construído dentro do Minecraft, mas com personalidade própria.

---

## Desenvolvimento

Projeto desenvolvido por **DevEngenhoso**.

> Um plugin feito na base da persistência, café, ambição e necromancia de software.

---

## Aviso

Este projeto ainda está em desenvolvimento e pode passar por grandes mudanças estruturais.

Sistemas, comandos, balanceamento e mecânicas podem ser alterados conforme o RPG evolui.

---

## MinezinServer

**Escolha sua classe.
Entre no mundo.
Construa sua presença.**
