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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BurroType extends JFrame {

    static class Palavra {
        String texto;
        double x, y;
        
        public Palavra(String texto, double x, double y) {
            this.texto = texto;
            this.x = x;
            this.y = y;
        }
        public void mover(double velocidade) { y += velocidade; }
    }

    static class Particula {
        double x, y, dx, dy;
        int vida, vidaMaxima;
        Color cor;

        public Particula(double x, double y, Color cor) {
            this.x = x; this.y = y; this.cor = cor;
            Random r = new Random();
            double angulo = r.nextDouble() * 2 * Math.PI;
            double velocidade = r.nextDouble() * 3 + 1;
            this.dx = Math.cos(angulo) * velocidade;
            this.dy = Math.sin(angulo) * velocidade;
            this.vidaMaxima = 20 + r.nextInt(20);
            this.vida = this.vidaMaxima;
        }

        public void atualizar() {
            x += dx; y += dy; vida--;
        }
    }

    static class CelulaFundo {
        int x, y, velocidade, tamanho;
        public CelulaFundo(int x, int y, int velocidade, int tamanho) {
            this.x = x; this.y = y; this.velocidade = velocidade; this.tamanho = tamanho;
        }
        public void mover(int alturaTela) {
            y += velocidade;
            if (y > alturaTela) y = -10;
        }
    }

    private List<Palavra> palavrasEmTela = new ArrayList<>();
    private List<Particula> particulas = new ArrayList<>();
    private List<CelulaFundo> celulasFundo = new ArrayList<>();
    private List<String> bancoDePalavras = new ArrayList<>();
    private Random random = new Random();

    private Palavra alvoAtual = null;
    private int pontuacao = 0;
    private int metaAtual = 10;
    private double velocidadeAtual = 2.0;
    private double incrementoVelocidade = 0.2;
    private int multiplicadorCombos = 1;
    private int acertosConsecutivos = 0;

    private boolean isGameOver = false;
    private String palavraInvasora = "";

    private Timer timerJogo;
    private Timer timerSpawn;

    public BurroType() {
        setTitle("ZType Pro - Trabalho 1 Prog2");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        carregarPalavrasDoArquivo("palavras.txt");
        inicializarFundoBiologico();

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
                if (alvoAtual != null) {
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
                g2d.setFont(new Font("Consolas", Font.BOLD, 22));
                for (Palavra p : palavrasEmTela) {
                    if (p == alvoAtual) {
                        g2d.setColor(Color.YELLOW);
                    } else {
                        g2d.setColor(new Color(200, 255, 200));
                    }
                    g2d.drawString(p.texto, (int) p.x, (int) p.y);
                }

                // 8. HUD
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), 45);
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Verdana", Font.BOLD, 14));
                g2d.drawString("SCORE: " + pontuacao + " / META: " + metaAtual, 15, 28);
                g2d.setColor(Color.ORANGE);
                g2d.drawString(String.format("VEL: %.1fx", velocidadeAtual), 250, 28);
                g2d.setColor(Color.MAGENTA);
                g2d.drawString("COMBO: x" + multiplicadorCombos, 450, 28);

                // 9. TELA DE GAME OVER
              
                if (isGameOver) {
                    g2d.setColor(new Color(0, 0, 0, 210)); 
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    FontMetrics fm;
                    
                    // Título
                    g2d.setColor(new Color(255, 50, 80));
                    g2d.setFont(new Font("Verdana", Font.BOLD, 28));
                    String titulo = "BARREIRA MENTAL ROMPIDA!";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(titulo, (getWidth() - fm.stringWidth(titulo)) / 2, 250);

                    // Mensagem Temática
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
                    String msg1 = "A palavra '" + palavraInvasora + "' invadiu suas defesas.";
                    String msg2 = "Você deixou o conhecimento entrar no cérebro.";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(msg1, (getWidth() - fm.stringWidth(msg1)) / 2, 300);
                    g2d.drawString(msg2, (getWidth() - fm.stringWidth(msg2)) / 2, 330);

                    // Pontuação Final
                    g2d.setColor(Color.CYAN);
                    g2d.setFont(new Font("Verdana", Font.BOLD, 22));
                    String scoreMsg = "SCORE FINAL: " + pontuacao;
                    fm = g2d.getFontMetrics();
                    g2d.drawString(scoreMsg, (getWidth() - fm.stringWidth(scoreMsg)) / 2, 400);

                    // Instruções de reinício piscantes 
                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Consolas", Font.BOLD, 14));
                    String inst = "Pressione [ENTER] para tentar novamente ou [ESC] para sair";
                    fm = g2d.getFontMetrics();
                    g2d.drawString(inst, (getWidth() - fm.stringWidth(inst)) / 2, 500);
                }
            }
        };

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Comandos da tela de Game Over
                if (isGameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        iniciarJogo();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (isGameOver) return; // Trava a digitação se o jogo acabou

                char letra = Character.toUpperCase(e.getKeyChar());
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

                if (erro) quebraCombo();
                painel.repaint();
            }
        });

        setContentPane(painel);
        setFocusable(true);
        requestFocusInWindow(); 

        timerJogo = new Timer(16, e -> { 
            atualizarLogica();
            painel.repaint();
        });

        timerSpawn = new Timer(2000, e -> {
            if (palavrasEmTela.size() < 5) gerarNovaPalavra();
        });

        iniciarJogo();
    }

    private void atualizarLogica() {
        for (CelulaFundo c : celulasFundo) c.mover(getHeight());

        Iterator<Particula> itP = particulas.iterator();
        while (itP.hasNext()) {
            Particula p = itP.next();
            p.atualizar();
            if (p.vida <= 0) itP.remove();
        }

        Iterator<Palavra> itPalavras = palavrasEmTela.iterator();
        while (itPalavras.hasNext()) {
            Palavra p = itPalavras.next();
            p.mover(velocidadeAtual / 2.0);

            // Colisão com a barreira mental
            if (p.y >= 570) {
                isGameOver = true;
                palavraInvasora = p.texto; // Guarda a palavra para mostrar na tela de Game Over
                timerJogo.stop();
                timerSpawn.stop();
                repaint(); // Força o desenho da tela de Game Over
                return;
            }
        }
    }

    private void processaLetra() {
        acertosConsecutivos++;
        if (acertosConsecutivos > 10) multiplicadorCombos = 2;
        if (acertosConsecutivos > 25) multiplicadorCombos = 3;

        criarExplosao(alvoAtual.x + 10, alvoAtual.y, 3, Color.YELLOW);
        
        alvoAtual.texto = alvoAtual.texto.substring(1);

        if (alvoAtual.texto.isEmpty()) {
            criarExplosao(alvoAtual.x + 20, alvoAtual.y, 25, new Color(50, 255, 100)); 
            palavrasEmTela.remove(alvoAtual);
            alvoAtual = null;
            pontuacao += (1 * multiplicadorCombos);

            gerarNovaPalavra();

            if (pontuacao >= metaAtual) {
                velocidadeAtual += incrementoVelocidade;
                incrementoVelocidade *= 2;
                metaAtual *= 2;
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

    private void iniciarJogo() {
        isGameOver = false;
        palavrasEmTela.clear();
        particulas.clear();
        alvoAtual = null;
        pontuacao = 0;
        metaAtual = 10;
        velocidadeAtual = 2.0;
        incrementoVelocidade = 0.2;
        quebraCombo();

        gerarNovaPalavra();
        gerarNovaPalavra();

        timerJogo.start();
        timerSpawn.start();
        repaint();
    }

    private void carregarPalavrasDoArquivo(String nomeArquivo) {
        File file = new File(nomeArquivo);
        if (!file.exists()) file = new File("src/" + nomeArquivo);

        if (file.exists()) {
            try {
                for (String linha : Files.readAllLines(file.toPath())) {
                    String palavraLimpa = linha.trim().toUpperCase();
                    if (!palavraLimpa.isEmpty()) bancoDePalavras.add(palavraLimpa);
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
        if (bancoDePalavras.isEmpty()) return;
        String texto = bancoDePalavras.get(random.nextInt(bancoDePalavras.size()));
        palavrasEmTela.add(new Palavra(texto, random.nextInt(400) + 50, -20)); 
    }

    public static void main(String[] args) {
        new BurroType().setVisible(true);
    }
}