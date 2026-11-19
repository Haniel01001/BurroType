# 🧠 BurroType: A Fuga do Conhecimento

**BurroType** é um jogo arcade de digitação em 2D (inspirado no clássico *ZType*), desenvolvido inteiramente em **Java Swing**.

No jogo, você controla um cérebro altamente teimoso que construiu uma barreira mental para se proteger da pior ameaça de todas: o conhecimento. Sua missão é digitar rapidamente as palavras que caem do topo da tela para disparar lasers neurais e destruí-las antes que ultrapassem o seu escudo e deixem você mais inteligente!

---

## 🎮 Funcionalidades

- **Mecânica Clássica de Digitação:** Trave o alvo na palavra pela primeira letra e destrua-a digitando as letras seguintes na ordem correta.
- **Sistema de Níveis e Dificuldade Progressiva:** Ao atingir a meta de pontuação, o jogador avança de nível (`NV`), dobrando a meta e aumentando a velocidade de queda das palavras. O jogo também registra o **maior nível alcançado**.
- **Sistema de Combos:** Digite sem errar para acumular acertos consecutivos e multiplicar seus pontos (`x2`, `x3`). Um erro quebra o combo.
- **Ranking Top 10 Estilo Arcade:** Ao encerrar uma partida com pontuação suficiente para entrar no Top 10, o jogador pode digitar seu nome para salvar sua marca (pontos, nível e maior combo) no ranking exibido no menu inicial.
- **Manutenção de Estado entre Execuções:** O jogo salva e carrega automaticamente o progresso em arquivo externo (`estado_jogo.txt`), preservando o ranking Top 10, o maior nível alcançado e o estado da partida em andamento (pontuação, nível, velocidade, combos e posição das palavras na tela).
- **Telas e Navegação por Teclado:** Fluxo completo com Menu Inicial (`[N]` Novo Jogo, `[C]` Continuar Partida Salva, `[ESC]` Sair/Salvar), tela de Novo Recorde (`[ENTER]`) e tela de Game Over (`[ENTER]`/`[N]`, `[M]`, `[ESC]`).
- **Gráficos Nativos e Efeitos (Java 2D):**
  - Cérebro, escudo mental e cenário biológico desenhados 100% via código (`Graphics2D`, `Area`, formas geométricas), sem imagens externas.
  - Explosões de partículas ao acertar letras e destruir palavras, laser neural e células animadas no fundo.
- **Banco de Palavras Externo:** As palavras são carregadas de `palavras.txt`, facilitando a adição ou edição de termos.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java (JDK 8 ou superior)
- **Biblioteca Gráfica:** Java Swing / AWT (`Graphics2D`)
- **Persistência em Arquivo:** `java.nio.file.Files` (`palavras.txt` e `estado_jogo.txt`)
- **Arquitetura:** Aplicação de arquivo único (`BurroType.java`) com máquina de estados (`MENU`, `JOGANDO`, `NOVO_RECORDE`, `GAME_OVER`), game loop baseado em `javax.swing.Timer` e eventos de teclado (`KeyAdapter`).

---

## 🚀 Como Executar o Jogo

### Pré-requisitos
- Certifique-se de ter o **Java Development Kit (JDK)** instalado na sua máquina.

### Passos

1. Clone este repositório:
   ```bash
   git clone [https://github.com/Haniel01001/BurroType.git](https://github.com/Haniel01001/BurroType.git)
