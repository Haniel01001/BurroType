import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BurroType extends JFrame {

    enum EstadoTela { // ponto 1
        MENU,
        JOGANDO,
        NOVO_RECORDE,
        GAME_OVER
    }

    static class Palavra {
        String texto;
        double x, y;

        public Palavra(String texto, double x, double y) {
            this.texto = texto;
            this.x = x;
            this.y = y;
        }

        public void mover(double velocidade) {
            y += velocidade;
        }
    }

    static class Particula {
        double x, y, dx, dy;
        int vida, vidaMaxima;
        Color cor;

        public Particula(double x, double y, Color cor) {
            this.x = x;
            this.y = y;
            this.cor = cor;
            Random r = new Random();
            double angulo = r.nextDouble() * 2 * Math.PI;
            double velocidade = r.nextDouble() * 3 + 1;
            this.dx = Math.cos(angulo) * velocidade;
            this.dy = Math.sin(angulo) * velocidade;
            this.vidaMaxima = 20 + r.nextInt(20);
            this.vida = this.vidaMaxima;
        }

        public void atualizar() {
            x += dx;
            y += dy;
            vida--;
        }
    }

    static class CelulaFundo {
        int x, y, velocidade, tamanho;

        public CelulaFundo(int x, int y, int velocidade, int tamanho) {
            this.x = x;
            this.y = y;
            this.velocidade = velocidade;
            this.tamanho = tamanho;
        }

        public void mover(int alturaTela) {
            y += velocidade;
            if (y > alturaTela)
                y = -10;
        }
    }

    static class Recorde {
        String nome;
        int pontos;
        int nivel;
        int combo;

        public Recorde(String nome, int pontos, int nivel, int combo) {
            this.nome = nome;
            this.pontos = pontos;
            this.nivel = nivel;
            this.combo = combo;
        }
    }

    private List<Palavra> palavrasEmTela = new ArrayList<>();
    private List<Particula> particulas = new ArrayList<>();
    private List<CelulaFundo> celulasFundo = new ArrayList<>();
    private List<String> bancoDePalavras = new ArrayList<>();
    private List<Recorde> topRecordes = new ArrayList<>();
    private Random random = new Random();

    private EstadoTela estadoAtual = EstadoTela.MENU;
    private Palavra alvoAtual = null;

    private int pontuacao = 0;
    private int metaAtual = 10;
    private double velocidadeAtual = 2.0;
    private double incrementoVelocidade = 0.2;
    private int multiplicadorCombos = 1;
    private int acertosConsecutivos = 0;
    private int maiorComboPartida = 1;

    private int nivel = 1;
    private int maiorNivel = 1;

    private boolean temSave = false;
    private String palavraInvasora = "";
    private String nomeJogadorAtual = "";
    private String arquivoEstado = "estado_jogo.txt";

    private Timer timerJogo;
    private Timer timerSpawn;

    public BurroType() {
        setTitle("BurroType - Trabalho 2 Prog2");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        carregarPalavrasDoArquivo("palavras.txt");
        carregarEstadoDoArquivo();
        inicializarFundoBiologico();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (estadoAtual == EstadoTela.JOGANDO) {
                    temSave = true;
                }
                salvarEstadoNoArquivo();
                dispose();
                System.exit(0);
            }
        });

        JPanel painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // 1. Fundo Biológico
                g2d.setColor(new Color(50, 15, 25));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // 2. Células de fundo
                g2d.setColor(new Color(255, 100, 150, 40));
                for (CelulaFundo c : celulasFundo) {
                    g2d.fillOval(c.x, c.y, c.tamanho, c.tamanho);
                }

                int centroX = getWidth() / 2;
                int posYCerebro = 620;

                // 3. CÉREBRO
                g2d.setColor(new Color(150, 70, 100));
                g2d.fill(new RoundRectangle2D.Double(centroX - 8, posYCerebro + 20, 16, 25, 10, 10));
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(100, 40, 60));
                g2d.draw(new RoundRectangle2D.Double(centroX - 8, posYCerebro + 20, 16, 25, 10, 10));

                g2d.setColor(new Color(210, 110, 150));
                g2d.fillOval(centroX - 25, posYCerebro + 15, 22, 18);
                g2d.fillOval(centroX + 3, posYCerebro + 15, 22, 18);
                g2d.setColor(new Color(160, 60, 100));
                g2d.drawOval(centroX - 25, posYCerebro + 15, 22, 18);
                g2d.drawOval(centroX + 3, posYCerebro + 15, 22, 18);

                Area cerebroArea = new Area();
                cerebroArea.add(new Area(new Ellipse2D.Double(centroX - 40, posYCerebro - 5, 40, 35)));
                cerebroArea.add(new Area(new Ellipse2D.Double(centroX, posYCerebro - 5, 40, 35)));
                cerebroArea.add(new Area(new Ellipse2D.Double(centroX - 30, posYCerebro - 20, 32, 32)));
                cerebroArea.add(new Area(new Ellipse2D.Double(centroX - 2, posYCerebro - 20, 32, 32)));
                cerebroArea.add(new Area(new Ellipse2D.Double(centroX - 16, posYCerebro - 28, 32, 28)));

                g2d.setColor(new Color(255, 130, 180));
                g2d.fill(cerebroArea);
                g2d.setColor(new Color(180, 60, 110));
                g2d.draw(cerebroArea);

                g2d.drawLine(centroX, posYCerebro - 25, centroX, posYCerebro + 28);
                g2d.drawArc(centroX - 32, posYCerebro - 12, 20, 15, 45, 180);
                g2d.drawArc(centroX + 12, posYCerebro - 12, 20, 15, -45, 180);
                g2d.drawArc(centroX - 28, posYCerebro + 8, 15, 12, 90, 180);
                g2d.drawArc(centroX + 13, posYCerebro + 8, 15, 12, -90, 180);
                g2d.drawArc(centroX - 15, posYCerebro - 15, 12, 10, 30, 150);
                g2d.drawArc(centroX + 3, posYCerebro - 15, 12, 10, 0, 150);

                // 4. Escudo Verde
                int alturaEscudo = 570;
                g2d.setColor(new Color(0, 255, 50, 35));
                g2d.fillOval(-100, alturaEscudo, getWidth() + 200, 150);
                g2d.setColor(new Color(50, 255, 100, 200));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(-100, alturaEscudo, getWidth() + 200, 150);

                // 5. Laser
                if (estadoAtual == EstadoTela.JOGANDO && alvoAtual != null) {
                    g2d.setColor(new Color(255, 0, 255, 120));
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawLine(centroX, posYCerebro - 25, (int) alvoAtual.x + 20, (int) alvoAtual.y - 10);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawLine(centroX, posYCerebro - 25, (int) alvoAtual.x + 20, (int) alvoAtual.y - 10);
                }

                // 6. Partículas
                for (Particula p : particulas) {
                    int alpha = (int) ((p.vida / (double) p.vidaMaxima) * 255);
                    g2d.setColor(new Color(p.cor.getRed(), p.cor.getGreen(), p.cor.getBlue(), Math.max(0, alpha)));
                    g2d.fillOval((int) p.x, (int) p.y, 4, 4);
                }

                // 7. Palavras
                if (estadoAtual == EstadoTela.JOGANDO) {
                    g2d.setFont(new Font("Consolas", Font.BOLD, 22));
                    for (Palavra p : palavrasEmTela) {
                        if (p == alvoAtual) {
                            g2d.setColor(Color.YELLOW);
                        } else {
                            g2d.setColor(new Color(200, 255, 200));
                        }
                        g2d.drawString(p.texto, (int) p.x, (int) p.y);
                    }
                }

                // 8. HUD
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), 45);
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Verdana", Font.BOLD, 13));
                g2d.drawString("SCORE: " + pontuacao + "/" + metaAtual, 12, 28);
                g2d.setColor(Color.GREEN);
                g2d.drawString("NV: " + nivel, 190, 28);
                g2d.setColor(Color.ORANGE);
                g2d.drawString(String.format("VEL: %.1fx", velocidadeAtual), 265, 28);
                g2d.setColor(Color.MAGENTA);
                g2d.drawString("COMBO: x" + multiplicadorCombos, 415, 28);

                // 9. TELA INICIAL (MENU)
                if (estadoAtual == EstadoTela.MENU) {
                    g2d.setColor(new Color(0, 0, 0, 185));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    FontMetrics fm;

                    g2d.setColor(new Color(255, 130, 180));
                    g2d.setFont(new Font("Verdana", Font.BOLD, 34));
                    String tituloMenu = "BURROTYPE";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(tituloMenu, (getWidth() - fm.stringWidth(tituloMenu)) / 2, 95);

                    g2d.setColor(Color.GREEN);
                    g2d.setFont(new Font("Verdana", Font.BOLD, 14));
                    String nivelMaxTexto = "MAIOR NIVEL ALCANCADO: " + maiorNivel;
                    fm = g2d.getFontMetrics();
                    g2d.drawString(nivelMaxTexto, (getWidth() - fm.stringWidth(nivelMaxTexto)) / 2, 130);

                    // Tabela Top 10
                    g2d.setColor(Color.CYAN);
                    g2d.setFont(new Font("Verdana", Font.BOLD, 15));
                    String tituloRanking = "TOP 10 RECORDES";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(tituloRanking, (getWidth() - fm.stringWidth(tituloRanking)) / 2, 165);

                    g2d.setFont(new Font("Consolas", Font.BOLD, 13));
                    if (topRecordes.isEmpty()) {
                        g2d.setColor(Color.GRAY);
                        String vazio = "Nenhum recorde salvo ainda.";
                        fm = g2d.getFontMetrics();
                        g2d.drawString(vazio, (getWidth() - fm.stringWidth(vazio)) / 2, 195);
                    } else {
                        int yRanking = 192;
                        for (int i = 0; i < topRecordes.size() && i < 10; i++) {
                            Recorde r = topRecordes.get(i);
                            g2d.setColor(i == 0 ? Color.YELLOW : Color.WHITE);
                            String linha = (i + 1) + ". " + r.nome + " - " + r.pontos + " pts (Nv " + r.nivel + ", x"
                                    + r.combo + ")";
                            fm = g2d.getFontMetrics();
                            g2d.drawString(linha, (getWidth() - fm.stringWidth(linha)) / 2, yRanking);
                            yRanking += 20;
                        }
                    }

                    // Comandos do menu
                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Consolas", Font.BOLD, 15));
                    String cmdNovo = "[N] Novo Jogo";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(cmdNovo, (getWidth() - fm.stringWidth(cmdNovo)) / 2, 435);

                    if (temSave) {
                        g2d.setColor(new Color(50, 255, 120));
                        String cmdCont = "[C] Continuar Partida Salva";
                        fm = g2d.getFontMetrics();
                        g2d.drawString(cmdCont, (getWidth() - fm.stringWidth(cmdCont)) / 2, 465);
                    } else {
                        g2d.setColor(Color.GRAY);
                        String cmdSemSave = "[C] Nenhuma partida salva";
                        fm = g2d.getFontMetrics();
                        g2d.drawString(cmdSemSave, (getWidth() - fm.stringWidth(cmdSemSave)) / 2, 465);
                    }

                    g2d.setColor(Color.ORANGE);
                    g2d.setFont(new Font("Consolas", Font.PLAIN, 13));
                    String cmdSair = "[ESC] Sair";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(cmdSair, (getWidth() - fm.stringWidth(cmdSair)) / 2, 505);
                }

                // 10. TELA PARA DIGITAR NOME NO TOP 10 (ESTILO ARCADE)
                if (estadoAtual == EstadoTela.NOVO_RECORDE) {
                    g2d.setColor(new Color(0, 0, 0, 215));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    FontMetrics fm;

                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Verdana", Font.BOLD, 26));
                    String parabens = "ENTROU NO TOP 10!";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(parabens, (getWidth() - fm.stringWidth(parabens)) / 2, 220);

                    g2d.setColor(Color.CYAN);
                    g2d.setFont(new Font("Consolas", Font.BOLD, 16));
                    String resumo = "SCORE: " + pontuacao + "  |  NIVEL: " + nivel + "  |  COMBO MAX: x"
                            + maiorComboPartida;
                    fm = g2d.getFontMetrics();
                    g2d.drawString(resumo, (getWidth() - fm.stringWidth(resumo)) / 2, 265);

                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Consolas", Font.PLAIN, 15));
                    String pedirNome = "Digite seu nome e aperte [ENTER]:";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(pedirNome, (getWidth() - fm.stringWidth(pedirNome)) / 2, 325);

                    g2d.setColor(new Color(50, 255, 120));
                    g2d.setFont(new Font("Verdana", Font.BOLD, 24));
                    String campoNome = nomeJogadorAtual.isEmpty() ? "_" : nomeJogadorAtual + "_";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(campoNome, (getWidth() - fm.stringWidth(campoNome)) / 2, 375);

                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.setFont(new Font("Consolas", Font.PLAIN, 13));
                    String dicaNome = "[BACKSPACE] apaga   |   [ENTER] salva no ranking";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(dicaNome, (getWidth() - fm.stringWidth(dicaNome)) / 2, 430);
                }

                // 11. TELA DE GAME OVER
                if (estadoAtual == EstadoTela.GAME_OVER) {
                    g2d.setColor(new Color(0, 0, 0, 210));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    FontMetrics fm;

                    g2d.setColor(new Color(255, 50, 80));
                    g2d.setFont(new Font("Verdana", Font.BOLD, 28));
                    String titulo = "BARREIRA MENTAL ROMPIDA!";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(titulo, (getWidth() - fm.stringWidth(titulo)) / 2, 210);

                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
                    String msg1 = "A palavra '" + palavraInvasora + "' invadiu suas defesas.";
                    String msg2 = "Voce deixou o conhecimento entrar no cerebro.";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(msg1, (getWidth() - fm.stringWidth(msg1)) / 2, 255);
                    g2d.drawString(msg2, (getWidth() - fm.stringWidth(msg2)) / 2, 282);

                    g2d.setColor(Color.CYAN);
                    g2d.setFont(new Font("Verdana", Font.BOLD, 20));
                    String scoreMsg = "SCORE FINAL: " + pontuacao + "  |  NIVEL: " + nivel;
                    fm = g2d.getFontMetrics();
                    g2d.drawString(scoreMsg, (getWidth() - fm.stringWidth(scoreMsg)) / 2, 340);

                    if (!topRecordes.isEmpty()) {
                        g2d.setColor(Color.YELLOW);
                        g2d.setFont(new Font("Consolas", Font.BOLD, 15));
                        Recorde top1 = topRecordes.get(0);
                        String bestMsg = "1o LUGAR: " + top1.nome + " - " + top1.pontos + " pts (Nv " + top1.nivel
                                + ")";
                        fm = g2d.getFontMetrics();
                        g2d.drawString(bestMsg, (getWidth() - fm.stringWidth(bestMsg)) / 2, 380);
                    }

                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Consolas", Font.BOLD, 14));
                    String inst1 = "Pressione [ENTER] ou [N] para novo jogo";
                    String inst2 = "[M] para voltar ao menu ou [ESC] para sair";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(inst1, (getWidth() - fm.stringWidth(inst1)) / 2, 455);
                    g2d.drawString(inst2, (getWidth() - fm.stringWidth(inst2)) / 2, 482);
                }
            }
        };

        addKeyListener(new KeyAdapter() { // ponto 2 - 1
            @Override
            public void keyPressed(KeyEvent e) {
                int codigo = e.getKeyCode();

                if (estadoAtual == EstadoTela.MENU) {
                    if (codigo == KeyEvent.VK_N || codigo == KeyEvent.VK_ENTER) {
                        iniciarNovoJogo();
                    } else if (codigo == KeyEvent.VK_C && temSave) {
                        continuarJogoSalvo();
                    } else if (codigo == KeyEvent.VK_ESCAPE) {
                        salvarEstadoNoArquivo();
                        System.exit(0);
                    }
                    painel.repaint();
                    return;
                }

                if (estadoAtual == EstadoTela.JOGANDO) {
                    if (codigo == KeyEvent.VK_ESCAPE) {
                        timerJogo.stop();
                        timerSpawn.stop();
                        temSave = true;
                        salvarEstadoNoArquivo();
                        estadoAtual = EstadoTela.MENU;
                        painel.repaint();
                    }
                    return;
                }

                if (estadoAtual == EstadoTela.NOVO_RECORDE) {
                    if (codigo == KeyEvent.VK_BACK_SPACE && !nomeJogadorAtual.isEmpty()) {
                        nomeJogadorAtual = nomeJogadorAtual.substring(0, nomeJogadorAtual.length() - 1);
                        painel.repaint();
                    } else if (codigo == KeyEvent.VK_ENTER) {
                        confirmarRecordeComNome();
                        painel.repaint();
                    }
                    return;
                }

                if (estadoAtual == EstadoTela.GAME_OVER) {
                    if (codigo == KeyEvent.VK_ENTER || codigo == KeyEvent.VK_N) {
                        iniciarNovoJogo();
                    } else if (codigo == KeyEvent.VK_M) {
                        estadoAtual = EstadoTela.MENU;
                        painel.repaint();
                    } else if (codigo == KeyEvent.VK_ESCAPE) {
                        salvarEstadoNoArquivo();
                        System.exit(0);
                    }
                    painel.repaint();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (estadoAtual == EstadoTela.NOVO_RECORDE) {
                    char c = e.getKeyChar();
                    if (Character.isLetterOrDigit(c) && nomeJogadorAtual.length() < 10) {
                        nomeJogadorAtual += Character.toUpperCase(c);
                        painel.repaint();
                    }
                    return;
                }

                if (estadoAtual != EstadoTela.JOGANDO)
                    return;

                char letra = Character.toUpperCase(e.getKeyChar());
                if (!Character.isLetterOrDigit(letra))
                    return;

                boolean erro = true;

                if (alvoAtual == null) {
                    for (Palavra p : palavrasEmTela) {
                        if (!p.texto.isEmpty() && p.texto.charAt(0) == letra) {
                            alvoAtual = p;
                            processaLetra();
                            erro = false;
                            break;
                        }
                    }
                } else {
                    if (!alvoAtual.texto.isEmpty() && alvoAtual.texto.charAt(0) == letra) {
                        processaLetra();
                        erro = false;
                    }
                }

                if (erro)
                    quebraCombo();
                painel.repaint();
            }
        });

        setContentPane(painel);
        setFocusable(true);
        requestFocusInWindow();

        timerJogo = new Timer(16, e -> { // ponto 2 - 2
            atualizarLogica();
            painel.repaint();
        });

        timerSpawn = new Timer(2000, e -> { // ponto 2 - 3
            if (estadoAtual == EstadoTela.JOGANDO && palavrasEmTela.size() < 5) {
                gerarNovaPalavra();
            }
        });
    }

    private void atualizarLogica() {
        for (CelulaFundo c : celulasFundo)
            c.mover(getHeight());

        Iterator<Particula> itP = particulas.iterator();
        while (itP.hasNext()) {
            Particula p = itP.next();
            p.atualizar();
            if (p.vida <= 0)
                itP.remove();
        }

        if (estadoAtual != EstadoTela.JOGANDO)
            return;

        Iterator<Palavra> itPalavras = palavrasEmTela.iterator();
        while (itPalavras.hasNext()) {
            Palavra p = itPalavras.next();
            p.mover(velocidadeAtual / 2.0);

            // Colisão com a barreira mental
            if (p.y >= 570) { // ponto 2 - 4
                palavraInvasora = p.texto;
                timerJogo.stop();
                timerSpawn.stop();
                temSave = false;

                if (entraNoTop10(pontuacao)) {
                    nomeJogadorAtual = "";
                    estadoAtual = EstadoTela.NOVO_RECORDE;
                } else {
                    estadoAtual = EstadoTela.GAME_OVER;
                    salvarEstadoNoArquivo();
                }
                repaint();
                return;
            }
        }
    }

    private boolean entraNoTop10(int pontos) {
        if (pontos <= 0)
            return false;
        if (topRecordes.size() < 10)
            return true;
        return pontos > topRecordes.get(topRecordes.size() - 1).pontos;
    }

    private void confirmarRecordeComNome() {
        String nomeFinal = nomeJogadorAtual.trim();
        if (nomeFinal.isEmpty()) {
            nomeFinal = "JOGADOR";
        }
        topRecordes.add(new Recorde(nomeFinal, pontuacao, nivel, maiorComboPartida));
        ordenarEManterTop10();
        salvarEstadoNoArquivo();
        estadoAtual = EstadoTela.GAME_OVER;
    }

    private void processaLetra() { // ponto 3 - 1
        acertosConsecutivos++;
        if (acertosConsecutivos > 10)
            multiplicadorCombos = 2;
        if (acertosConsecutivos > 25)
            multiplicadorCombos = 3;
        if (multiplicadorCombos > maiorComboPartida)
            maiorComboPartida = multiplicadorCombos;

        criarExplosao(alvoAtual.x + 10, alvoAtual.y, 3, Color.YELLOW);

        alvoAtual.texto = alvoAtual.texto.substring(1);

        if (alvoAtual.texto.isEmpty()) {
            criarExplosao(alvoAtual.x + 20, alvoAtual.y, 25, new Color(50, 255, 100));
            palavrasEmTela.remove(alvoAtual);
            alvoAtual = null;
            pontuacao += (1 * multiplicadorCombos);

            gerarNovaPalavra();

            if (pontuacao >= metaAtual) { // ponto 3 - 2
                nivel++;
                if (nivel > maiorNivel) {
                    maiorNivel = nivel;
                }
                velocidadeAtual += incrementoVelocidade;
                incrementoVelocidade *= 2;
                metaAtual *= 2;
                temSave = true;
                salvarEstadoNoArquivo();
            }
        }
    }

    private void quebraCombo() {
        acertosConsecutivos = 0;
        multiplicadorCombos = 1;
    }

    private void criarExplosao(double x, double y, int qtd, Color cor) {
        for (int i = 0; i < qtd; i++) {
            particulas.add(new Particula(x, y, cor));
        }
    }

    private void inicializarFundoBiologico() {
        for (int i = 0; i < 40; i++) {
            int tamanho = random.nextInt(4) + 2;
            celulasFundo.add(new CelulaFundo(random.nextInt(600), random.nextInt(700), random.nextInt(3) + 1, tamanho));
        }
    }

    private void iniciarNovoJogo() {
        estadoAtual = EstadoTela.JOGANDO;
        palavrasEmTela.clear();
        particulas.clear();
        alvoAtual = null;
        pontuacao = 0;
        metaAtual = 10;
        velocidadeAtual = 2.0;
        incrementoVelocidade = 0.2;
        nivel = 1;
        maiorComboPartida = 1;
        nomeJogadorAtual = "";
        quebraCombo();

        gerarNovaPalavra();
        gerarNovaPalavra();

        timerJogo.start();
        timerSpawn.start();
        repaint();
    }

    private void continuarJogoSalvo() {
        if (!temSave) {
            iniciarNovoJogo();
            return;
        }
        estadoAtual = EstadoTela.JOGANDO;
        particulas.clear();
        alvoAtual = null;

        if (palavrasEmTela.isEmpty()) {
            gerarNovaPalavra();
        }

        timerJogo.start();
        timerSpawn.start();
        repaint();
    }

    private void ordenarEManterTop10() {
        for (int i = 0; i < topRecordes.size(); i++) {
            for (int j = i + 1; j < topRecordes.size(); j++) {
                if (topRecordes.get(j).pontos > topRecordes.get(i).pontos) {
                    Recorde temp = topRecordes.get(i);
                    topRecordes.set(i, topRecordes.get(j));
                    topRecordes.set(j, temp);
                }
            }
        }

        while (topRecordes.size() > 10) {
            topRecordes.remove(topRecordes.size() - 1);
        }
    }

    private void salvarEstadoNoArquivo() { // ponto 4 - 1
        List<String> linhas = new ArrayList<>();
        linhas.add("MAIOR_NIVEL=" + maiorNivel);
        linhas.add("TEM_SAVE=" + temSave);
        linhas.add("PONTUACAO=" + pontuacao);
        linhas.add("META_ATUAL=" + metaAtual);
        linhas.add("VELOCIDADE_ATUAL=" + velocidadeAtual);
        linhas.add("INCREMENTO_VELOCIDADE=" + incrementoVelocidade);
        linhas.add("MULTIPLICADOR_COMBOS=" + multiplicadorCombos);
        linhas.add("ACERTOS_CONSECUTIVOS=" + acertosConsecutivos);
        linhas.add("MAIOR_COMBO_PARTIDA=" + maiorComboPartida);
        linhas.add("NIVEL=" + nivel);

        for (Palavra p : palavrasEmTela) {
            linhas.add("PALAVRA_SALVA=" + p.texto + ";" + p.x + ";" + p.y);
        }

        for (Recorde r : topRecordes) {
            linhas.add("RECORDE=" + r.nome + ";" + r.pontos + ";" + r.nivel + ";" + r.combo);
        }

        try {
            File file = new File(arquivoEstado);
            Files.write(file.toPath(), linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Erro ao salvar estado do jogo.");
        }
    }

    private void carregarEstadoDoArquivo() { // ponto 4 - 2
        File file = new File(arquivoEstado);
        if (!file.exists()) {
            file = new File("src/" + arquivoEstado);
        }
        if (!file.exists()) {
            return;
        }

        try {
            List<String> linhas = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            topRecordes.clear();
            palavrasEmTela.clear();

            for (String linha : linhas) {
                if (linha.startsWith("MAIOR_NIVEL=")) {
                    maiorNivel = Integer.parseInt(linha.substring("MAIOR_NIVEL=".length()).trim());
                } else if (linha.startsWith("TEM_SAVE=")) {
                    temSave = Boolean.parseBoolean(linha.substring("TEM_SAVE=".length()).trim());
                } else if (linha.startsWith("PONTUACAO=")) {
                    pontuacao = Integer.parseInt(linha.substring("PONTUACAO=".length()).trim());
                } else if (linha.startsWith("META_ATUAL=")) {
                    metaAtual = Integer.parseInt(linha.substring("META_ATUAL=".length()).trim());
                } else if (linha.startsWith("VELOCIDADE_ATUAL=")) {
                    velocidadeAtual = Double.parseDouble(linha.substring("VELOCIDADE_ATUAL=".length()).trim());
                } else if (linha.startsWith("INCREMENTO_VELOCIDADE=")) {
                    incrementoVelocidade = Double
                            .parseDouble(linha.substring("INCREMENTO_VELOCIDADE=".length()).trim());
                } else if (linha.startsWith("MULTIPLICADOR_COMBOS=")) {
                    multiplicadorCombos = Integer.parseInt(linha.substring("MULTIPLICADOR_COMBOS=".length()).trim());
                } else if (linha.startsWith("ACERTOS_CONSECUTIVOS=")) {
                    acertosConsecutivos = Integer.parseInt(linha.substring("ACERTOS_CONSECUTIVOS=".length()).trim());
                } else if (linha.startsWith("MAIOR_COMBO_PARTIDA=")) {
                    maiorComboPartida = Integer.parseInt(linha.substring("MAIOR_COMBO_PARTIDA=".length()).trim());
                } else if (linha.startsWith("NIVEL=")) {
                    nivel = Integer.parseInt(linha.substring("NIVEL=".length()).trim());
                } else if (linha.startsWith("PALAVRA_SALVA=")) {
                    String dados = linha.substring("PALAVRA_SALVA=".length()).trim();
                    String[] partes = dados.split(";");
                    if (partes.length == 3) {
                        String txt = partes[0];
                        double px = Double.parseDouble(partes[1]);
                        double py = Double.parseDouble(partes[2]);
                        palavrasEmTela.add(new Palavra(txt, px, py));
                    }
                } else if (linha.startsWith("RECORDE=")) {
                    String dados = linha.substring("RECORDE=".length()).trim();
                    String[] partes = dados.split(";");
                    if (partes.length == 4) {
                        String nome = partes[0];
                        int p = Integer.parseInt(partes[1]);
                        int n = Integer.parseInt(partes[2]);
                        int c = Integer.parseInt(partes[3]);
                        topRecordes.add(new Recorde(nome, p, n, c));
                    }
                }
            }

            if (maiorNivel < nivel) {
                maiorNivel = nivel;
            }
            ordenarEManterTop10();
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao carregar estado do jogo.");
        }
    }

    private void carregarPalavrasDoArquivo(String nomeArquivo) {
        File file = new File(nomeArquivo);
        if (!file.exists())
            file = new File("src/" + nomeArquivo);

        if (file.exists()) {
            try {
                for (String linha : Files.readAllLines(file.toPath())) {
                    String palavraLimpa = linha.trim().toUpperCase();
                    if (!palavraLimpa.isEmpty())
                        bancoDePalavras.add(palavraLimpa);
                }
            } catch (IOException e) {
                System.out.println("Erro na leitura.");
            }
        }
        if (bancoDePalavras.isEmpty()) {
            bancoDePalavras.addAll(Arrays.asList("JAVA", "INTERFACE", "POLIMORFISMO", "ARRAY", "CONSTRUTOR"));
        }
    }

    private void gerarNovaPalavra() {
        if (bancoDePalavras.isEmpty())
            return;
        String texto = bancoDePalavras.get(random.nextInt(bancoDePalavras.size()));
        palavrasEmTela.add(new Palavra(texto, random.nextInt(400) + 50, -20));
    }

    public static void main(String[] args) {
        new BurroType().setVisible(true);
    }
}