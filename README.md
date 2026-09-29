# 🧠 BurroType: A Fuga do Conhecimento

**BurroType** é um jogo arcade de digitação em 2D (inspirado no clássico *ZType*), desenvolvido inteiramente em Java Swing. 

No jogo, você controla um cérebro altamente teimoso que construiu uma **barreira mental** para se proteger da pior ameaça de todas: o *conhecimento*. Sua missão é digitar rapidamente as palavras que caem do topo da tela para disparar lasers neurais e destruí-las antes que elas ultrapassem o seu escudo e te deixem mais inteligente!

---

## 🎮 Funcionalidades

* **Mecânica Clássica de Digitação:** Destrua as palavras digitando suas letras na ordem correta.
* **Dificuldade Progressiva:** Ao atingir a meta de pontuação, o jogo avança de nível, dobrando a meta e aumentando a velocidade de queda das palavras.
* **Sistema de Combos:** Digite sem errar para acumular acertos consecutivos e multiplicar seus pontos (x2, x3). Um erro quebra o seu combo.
* **Gráficos Nativos e Efeitos (Java 2D):** 
  * O cérebro, escudo e cenário biológico são desenhados 100% via código (`Graphics2D`, `Area`, formas geométricas), sem depender de imagens externas.
  * Efeitos visuais incluem explosões de partículas ao destruir palavras, lasers neon, e células de fundo animadas.
* **Interface Personalizada:** HUD moderno para acompanhamento de status e uma tela de *Game Over* customizada sobreposta ao jogo (Overlay).
* **Banco de Dados Dinâmico:** As palavras do jogo são lidas a partir de um arquivo externo (`palavras.txt`), permitindo fácil adição de novos termos.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java (JDK 8 ou superior)
* **Biblioteca Gráfica:** Java Swing / AWT (`Graphics2D`)
* **Arquitetura:** Aplicação de arquivo único (*Single-file*) focada em orientação a objetos simples (Entidades, Game Loop, Eventos de Teclado).

---

## 🚀 Como Executar o Jogo

### Pré-requisitos
Certifique-se de ter o [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) instalado na sua máquina.

### Passos
1. Clone este repositório:
   ```bash
   git clone [https://github.com/Haniel01001/BurroType.git](https://github.com/Haniel01001/BurroType.git)
   
## 👨‍💻 Desenvolvido por

* **Haniel Nasiniak de Souza** - [Haniel01001](https://github.com/Haniel01001)
* Disciplina: Programação 2
