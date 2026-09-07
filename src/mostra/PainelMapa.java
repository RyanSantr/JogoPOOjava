package mostra;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PainelMapa extends JPanel {
    private static final Color AZUL = new Color(25, 103, 245);
    private static final Color TRILHA = new Color(225, 236, 255);
    private static final Color TRILHA_ATUAL = new Color(202, 220, 255);
    private static final Color LINHA = new Color(211, 218, 228);
    private static final Color BONUS = new Color(255, 246, 196);

    private final BufferedImage[][] spritesAndando = new BufferedImage[4][3];
    private final BufferedImage[][] spritesIdle = new BufferedImage[4][2];
    private final BufferedImage[][] spritesErro = new BufferedImage[4][2];
    private final BufferedImage[][] spritesFeliz = new BufferedImage[4][2];
    private final BufferedImage[][] spritesDancando = new BufferedImage[4][2];
    private final List<Posicao> caminhoPercorrido = new ArrayList<>();
    private final List<Confete> confetes = new ArrayList<>();
    private final Random sorteio = new Random();
    private final Timer timerIdle;

    private Missao missao;
    private Robo robo;
    private Posicao casaBonusVisivel;
    private double xVisual;
    private double yVisual;
    private double saltoReacao;
    private double anguloRobo;
    private Direcao direcaoVisual;
    private ReacaoRobo reacao = ReacaoRobo.NORMAL;
    private int quadroAnimacao;
    private int quadroIdle;
    private double progressoColeta;
    private Timer timerMovimento;
    private boolean movendo;
    private String fala;
    private long falaAte;

    public PainelMapa(Missao missao, Robo robo) {
        this.missao = missao;
        this.robo = robo;
        xVisual = robo.getPosicao().getX();
        yVisual = robo.getPosicao().getY();
        direcaoVisual = robo.getDirecao();
        carregarSprites2D();
        caminhoPercorrido.add(robo.getPosicao());

        timerIdle = new Timer(360, evento -> {
            if (!movendo) {
                quadroIdle = (quadroIdle + 1) % 2;
                if (fala != null && System.currentTimeMillis() > falaAte) {
                    fala = null;
                }
                repaint();
            }
        });
        timerIdle.start();

        setPreferredSize(new Dimension(700, 470));
        setMinimumSize(new Dimension(480, 350));
        setBackground(Color.WHITE);
    }

    public void resetar(Missao novaMissao, Robo novoRobo) {
        pararAnimacao();
        missao = novaMissao;
        robo = novoRobo;
        xVisual = robo.getPosicao().getX();
        yVisual = robo.getPosicao().getY();
        direcaoVisual = robo.getDirecao();
        reacao = ReacaoRobo.NORMAL;
        quadroAnimacao = 0;
        quadroIdle = 0;
        progressoColeta = 0;
        saltoReacao = 0;
        anguloRobo = 0;
        fala = null;
        confetes.clear();
        caminhoPercorrido.clear();
        caminhoPercorrido.add(robo.getPosicao());
        repaint();
    }

    public void atualizarModelo(Robo novoRobo) {
        robo = novoRobo;
        repaint();
    }

    public void setCasaBonus(Posicao casaBonus) {
        casaBonusVisivel = casaBonus;
        repaint();
    }

    public boolean passouPor(Posicao posicao) {
        return indiceNoCaminho(posicao) >= 0;
    }

    public void mostrarFala(String texto, int duracao) {
        fala = texto;
        falaAte = System.currentTimeMillis() + duracao;
        repaint();
    }

    public void animarMovimento(
            Posicao origem,
            Posicao destino,
            Direcao direcao,
            Runnable aoTerminar) {
        pararAnimacao();
        movendo = true;
        reacao = ReacaoRobo.NORMAL;
        direcaoVisual = direcao;
        quadroAnimacao = 0;
        long inicio = System.currentTimeMillis();
        int duracao = 460;

        timerMovimento = new Timer(38, evento -> {
            double progresso = Math.min(1.0, (System.currentTimeMillis() - inicio) / (double) duracao);
            double suave = progresso * progresso * (3 - 2 * progresso);
            xVisual = origem.getX() + (destino.getX() - origem.getX()) * suave;
            yVisual = origem.getY() + (destino.getY() - origem.getY()) * suave;
            quadroAnimacao = Math.min(2, (int) (progresso * 6) % 3);
            repaint();

            if (progresso >= 1.0) {
                pararAnimacao();
                xVisual = destino.getX();
                yVisual = destino.getY();
                quadroAnimacao = 0;
                adicionarAoCaminho(destino);
                repaint();
                aoTerminar.run();
            }
        });
        timerMovimento.start();
    }

    public void animarColeta(Runnable aoTerminar) {
        pararAnimacao();
        reacao = ReacaoRobo.FELIZ;
        mostrarFala("Energia!", 850);
        long inicio = System.currentTimeMillis();
        int duracao = 520;

        timerMovimento = new Timer(38, evento -> {
            progressoColeta = Math.min(1.0, (System.currentTimeMillis() - inicio) / (double) duracao);
            repaint();

            if (progressoColeta >= 1.0) {
                pararAnimacao();
                reacao = ReacaoRobo.NORMAL;
                repaint();
                aoTerminar.run();
            }
        });
        timerMovimento.start();
    }

    public void animarColisao(Direcao direcao, Runnable aoTerminar) {
        pararAnimacao();
        reacao = ReacaoRobo.ERRO;
        direcaoVisual = direcao;
        mostrarFala("Opa!", 700);
        double origemX = robo.getPosicao().getX();
        double origemY = robo.getPosicao().getY();
        Posicao tentativa = robo.getPosicao().mover(direcao);
        double passoX = tentativa.getX() - origemX;
        double passoY = tentativa.getY() - origemY;
        long inicio = System.currentTimeMillis();
        int duracao = 480;

        timerMovimento = new Timer(30, evento -> {
            double progresso = Math.min(1.0, (System.currentTimeMillis() - inicio) / (double) duracao);
            double impacto = Math.sin(progresso * Math.PI) * 0.18;
            xVisual = origemX + passoX * impacto;
            yVisual = origemY + passoY * impacto;
            anguloRobo = Math.sin(progresso * Math.PI * 5) * 0.06;
            repaint();

            if (progresso >= 1.0) {
                pararAnimacao();
                xVisual = origemX;
                yVisual = origemY;
                anguloRobo = 0;
                reacao = ReacaoRobo.NORMAL;
                repaint();
                aoTerminar.run();
            }
        });
        timerMovimento.start();
    }

    public void animarVitoria(Runnable aoTerminar) {
        animarCelebracao(ReacaoRobo.FELIZ, "Consegui!", 1750, aoTerminar);
    }

    public void animarDanca(Runnable aoTerminar) {
        animarCelebracao(ReacaoRobo.DANCANDO, "Código secreto!", 2300, aoTerminar);
    }

    private void animarCelebracao(
            ReacaoRobo novaReacao,
            String texto,
            int duracao,
            Runnable aoTerminar) {
        pararAnimacao();
        reacao = novaReacao;
        mostrarFala(texto, duracao);
        criarConfetes();
        long inicio = System.currentTimeMillis();

        timerMovimento = new Timer(32, evento -> {
            double progresso = Math.min(1.0, (System.currentTimeMillis() - inicio) / (double) duracao);
            saltoReacao = Math.abs(Math.sin(progresso * Math.PI * 6)) * 0.16;
            anguloRobo = Math.sin(progresso * Math.PI * 8) * 0.13;
            atualizarConfetes();
            repaint();

            if (progresso >= 1.0) {
                pararAnimacao();
                saltoReacao = 0;
                anguloRobo = 0;
                reacao = ReacaoRobo.NORMAL;
                confetes.clear();
                repaint();
                aoTerminar.run();
            }
        });
        timerMovimento.start();
    }

    public void pararAnimacao() {
        if (timerMovimento != null && timerMovimento.isRunning()) {
            timerMovimento.stop();
        }
        movendo = false;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margem = 18;
        int larguraCelula = (getWidth() - margem * 2) / Missao.COLUNAS;
        int alturaCelula = (getHeight() - margem * 2) / Missao.LINHAS;
        int tamanhoCelula = Math.max(1, Math.min(larguraCelula, alturaCelula));
        int inicioX = (getWidth() - tamanhoCelula * Missao.COLUNAS) / 2;
        int inicioY = (getHeight() - tamanhoCelula * Missao.LINHAS) / 2;

        desenharGrade(g, inicioX, inicioY, tamanhoCelula);
        desenharEnergia(g, inicioX, inicioY, tamanhoCelula);
        desenharRobo(g, inicioX, inicioY, tamanhoCelula);
        desenharConfetes(g);
        desenharFala(g, inicioX, inicioY, tamanhoCelula);
        g.dispose();
    }

    private void desenharGrade(Graphics2D g, int inicioX, int inicioY, int tamanho) {
        g.setStroke(new BasicStroke(1f));

        for (int y = 0; y < Missao.LINHAS; y++) {
            for (int x = 0; x < Missao.COLUNAS; x++) {
                int px = inicioX + x * tamanho;
                int py = inicioY + y * tamanho;
                Posicao posicao = new Posicao(x, y);

                if (missao.temParede(posicao)) {
                    g.setColor(new Color(22, 27, 34));
                    g.fillRoundRect(px + 5, py + 5, tamanho - 10, tamanho - 10, 8, 8);
                } else {
                    int indiceTrilha = indiceNoCaminho(posicao);
                    if (indiceTrilha >= 0) {
                        boolean ultima = indiceTrilha == caminhoPercorrido.size() - 1;
                        g.setColor(ultima ? TRILHA_ATUAL : TRILHA);
                    } else if (casaBonusVisivel != null && casaBonusVisivel.mesmaPosicao(posicao)) {
                        g.setColor(BONUS);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    g.fillRect(px + 1, py + 1, tamanho - 1, tamanho - 1);

                    if (indiceTrilha >= 0) {
                        g.setColor(AZUL);
                        g.fillRect(px + 1, py + tamanho - 5, tamanho - 1, 4);
                    }
                    if (casaBonusVisivel != null && casaBonusVisivel.mesmaPosicao(posicao)) {
                        desenharEstrelaBonus(g, px, py, tamanho, indiceTrilha >= 0);
                    }
                }

                g.setColor(LINHA);
                g.drawRect(px, py, tamanho, tamanho);
            }
        }
    }

    private void desenharEstrelaBonus(Graphics2D g, int x, int y, int tamanho, boolean visitada) {
        String estrela = "★";
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(16, tamanho / 4)));
        g.setColor(visitada ? new Color(22, 145, 86) : new Color(229, 168, 0));
        FontMetrics medidas = g.getFontMetrics();
        g.drawString(estrela, x + tamanho - medidas.stringWidth(estrela) - 9, y + medidas.getAscent() + 6);
    }

    private void desenharEnergia(Graphics2D g, int inicioX, int inicioY, int tamanho) {
        if (robo.pegouItem() && progressoColeta >= 1.0) {
            return;
        }

        Posicao objetivo = missao.getObjetivo();
        int centroX = inicioX + objetivo.getX() * tamanho + tamanho / 2;
        int centroY = inicioY + objetivo.getY() * tamanho + tamanho / 2;
        double escala = 1.0 - progressoColeta * 0.7;
        int largura = Math.max(10, (int) (tamanho * 0.25 * escala));
        int altura = Math.max(16, (int) (tamanho * 0.43 * escala));

        if (progressoColeta > 0) {
            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) Math.max(0.15, 1.0 - progressoColeta)));
        }

        int x = centroX - largura / 2;
        int y = centroY - altura / 2;
        g.setColor(new Color(15, 23, 35));
        g.fillRect(x - 4, y + 4, largura + 8, altura - 8);
        g.setColor(AZUL);
        g.fillRect(x, y, largura, altura);
        g.setColor(new Color(126, 199, 255));
        g.fillRect(x + largura / 4, y + 5, Math.max(2, largura / 4), altura - 10);
        g.setColor(new Color(15, 23, 35));
        g.fillRect(centroX - largura / 4, y - 5, largura / 2, 5);
        g.fillRect(centroX - largura / 3, y + altura, largura * 2 / 3, 5);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void desenharRobo(Graphics2D g, int inicioX, int inicioY, int tamanho) {
        int centroX = inicioX + (int) Math.round((xVisual + 0.5) * tamanho);
        int centroY = inicioY + (int) Math.round((yVisual + 0.5 - saltoReacao) * tamanho);
        int saltoColeta = (int) Math.round(Math.sin(progressoColeta * Math.PI) * tamanho * 0.10);
        centroY -= saltoColeta;

        int linha = indiceDirecao(direcaoVisual);
        BufferedImage sprite = escolherSprite(linha);
        int tamanhoSprite = Math.min(104, (int) (tamanho * 0.74));
        int destinoX = centroX - tamanhoSprite / 2;
        int destinoY = centroY - tamanhoSprite / 2;

        Graphics2D roboG = (Graphics2D) g.create();
        roboG.rotate(anguloRobo, centroX, centroY);
        roboG.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        roboG.drawImage(sprite, destinoX, destinoY, tamanhoSprite, tamanhoSprite, null);
        roboG.dispose();
    }

    private BufferedImage escolherSprite(int linha) {
        if (movendo) {
            return spritesAndando[linha][quadroAnimacao];
        }
        if (reacao == ReacaoRobo.ERRO) {
            return spritesErro[linha][quadroIdle];
        }
        if (reacao == ReacaoRobo.FELIZ) {
            return spritesFeliz[linha][quadroIdle];
        }
        if (reacao == ReacaoRobo.DANCANDO) {
            return spritesDancando[linha][quadroIdle];
        }
        return spritesIdle[linha][quadroIdle];
    }

    private void desenharFala(Graphics2D g, int inicioX, int inicioY, int tamanho) {
        if (fala == null) {
            return;
        }

        int centroX = inicioX + (int) Math.round((xVisual + 0.5) * tamanho);
        int centroY = inicioY + (int) Math.round((yVisual + 0.5 - saltoReacao) * tamanho);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        FontMetrics medidas = g.getFontMetrics();
        int largura = medidas.stringWidth(fala) + 22;
        int altura = 30;
        int x = Math.max(6, Math.min(getWidth() - largura - 6, centroX - largura / 2));
        int y = Math.max(5, centroY - tamanho / 2 - 38);

        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, largura, altura, 8, 8);
        g.setColor(new Color(18, 22, 28));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, largura, altura, 8, 8);
        int pontaX = Math.max(x + 10, Math.min(x + largura - 10, centroX));
        int[] pontosX = {pontaX - 5, pontaX + 5, pontaX};
        int[] pontosY = {y + altura, y + altura, y + altura + 7};
        g.setColor(Color.WHITE);
        g.fillPolygon(pontosX, pontosY, 3);
        g.setColor(new Color(18, 22, 28));
        g.drawLine(pontaX - 5, y + altura, pontaX, y + altura + 7);
        g.drawLine(pontaX, y + altura + 7, pontaX + 5, y + altura);
        g.drawString(fala, x + 11, y + 20);
    }

    private void criarConfetes() {
        confetes.clear();
        Color[] cores = {
            AZUL,
            new Color(22, 163, 74),
            new Color(250, 190, 40),
            new Color(239, 68, 68),
            new Color(18, 22, 28)
        };
        for (int i = 0; i < 42; i++) {
            confetes.add(new Confete(
                    sorteio.nextDouble(),
                    -sorteio.nextDouble() * Math.max(150, getHeight()),
                    3.5 + sorteio.nextDouble() * 5,
                    cores[sorteio.nextInt(cores.length)]));
        }
    }

    private void atualizarConfetes() {
        for (Confete confete : confetes) {
            confete.y += confete.velocidade;
            if (confete.y > getHeight()) {
                confete.y = -12;
            }
        }
    }

    private void desenharConfetes(Graphics2D g) {
        for (int i = 0; i < confetes.size(); i++) {
            Confete confete = confetes.get(i);
            int x = (int) (confete.x * getWidth());
            int y = (int) confete.y;
            g.setColor(confete.cor);
            if (i % 2 == 0) {
                g.fillRect(x, y, 5, 10);
            } else {
                g.fillOval(x, y, 7, 7);
            }
        }
    }

    private void carregarSprites2D() {
        for (Direcao direcao : Direcao.values()) {
            int linha = indiceDirecao(direcao);
            for (int quadro = 0; quadro < 3; quadro++) {
                spritesAndando[linha][quadro] = SpriteRobo.criar(direcao, quadro, true);
            }
            for (int quadro = 0; quadro < 2; quadro++) {
                spritesIdle[linha][quadro] = SpriteRobo.criar(direcao, quadro, false);
                spritesErro[linha][quadro] = SpriteRobo.criar(direcao, quadro, false, ReacaoRobo.ERRO);
                spritesFeliz[linha][quadro] = SpriteRobo.criar(direcao, quadro, false, ReacaoRobo.FELIZ);
                spritesDancando[linha][quadro] = SpriteRobo.criar(
                        direcao, quadro, false, ReacaoRobo.DANCANDO);
            }
        }
    }

    private int indiceDirecao(Direcao direcao) {
        if (direcao == Direcao.NORTE) {
            return 0;
        }
        if (direcao == Direcao.SUL) {
            return 1;
        }
        if (direcao == Direcao.OESTE) {
            return 2;
        }
        return 3;
    }

    private void adicionarAoCaminho(Posicao posicao) {
        caminhoPercorrido.add(posicao);
    }

    private int indiceNoCaminho(Posicao posicao) {
        for (int i = caminhoPercorrido.size() - 1; i >= 0; i--) {
            if (caminhoPercorrido.get(i).mesmaPosicao(posicao)) {
                return i;
            }
        }
        return -1;
    }

    private static class Confete {
        private final double x;
        private double y;
        private final double velocidade;
        private final Color cor;

        private Confete(double x, double y, double velocidade, Color cor) {
            this.x = x;
            this.y = y;
            this.velocidade = velocidade;
            this.cor = cor;
        }
    }
}
