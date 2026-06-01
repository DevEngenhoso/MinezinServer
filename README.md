# MinezinServer

Plugin de Minecraft desenvolvido originalmente para um servidor entre amigos.

A ideia inicial do projeto era adicionar pequenas funcionalidades personalizadas ao servidor, deixando a gameplay mais divertida, caótica e com a cara do nosso grupo. O plugin não nasceu com a intenção de ser um sistema genérico ou comercial, mas sim como um conjunto de recursos feitos sob medida para melhorar a experiência dentro do nosso próprio servidor.

## Funcionalidades

### Ranking de mortes

O plugin possui um sistema de ranking de mortes exibido na sidebar do servidor.

Esse ranking mostra os jogadores online com mais mortes, ordenando automaticamente do maior para o menor número de mortes. A atualização acontece de forma automática durante o jogo, permitindo que todos acompanhem quem está morrendo mais no servidor.

Atualmente, o ranking exibe até os 3 jogadores online com mais mortes.

### Efeito ao morrer

Sempre que um jogador morre, o plugin exibe um título para todos os jogadores online com a mensagem:

> F no chat

Além da mensagem, o plugin também toca um som assustador aleatório e gera partículas ao redor dos jogadores, criando um efeito visual e sonoro para destacar a morte.

A intenção dessa funcionalidade é transformar cada morte em um pequeno evento dentro do servidor, deixando o momento mais engraçado e marcante para todo mundo.

## Objetivo do plugin

O MinezinServer foi criado para adicionar personalidade ao servidor, trazendo recursos simples, mas divertidos, que combinam com a dinâmica de um servidor privado entre amigos.

A proposta é manter o plugin leve, direto e focado em funcionalidades que realmente façam sentido para o servidor.

## Tecnologias utilizadas

- Java
- Bukkit / Spigot / Paper API
- Minecraft Server Plugin

## Instalação

1. Compile o projeto e gere o arquivo `.jar`.
2. Coloque o `.jar` na pasta `plugins` do servidor.
3. Inicie ou reinicie o servidor.
4. O plugin será carregado automaticamente.

## Status do projeto

O plugin está em desenvolvimento e pode receber novas funcionalidades conforme surgirem ideias para o servidor.
