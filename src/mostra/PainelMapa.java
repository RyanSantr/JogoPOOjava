package mostra;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PainelMapa extends JPanel {
    private static final Color AZUL = new Color(25, 103, 245);
    private static final Color TRILHA = new Color(225, 236, 255);
    private static final Color TRILHA_ATUAL = new Color(202, 220, 255);
    private static final Color LINHA = new Color(211, 218, 228);

    private final BufferedImage[][] spritesAndando = new BufferedImage[4][3];
    private final BufferedImage[][] spritesIdle = new BufferedImage[4][2];
    private final List<Posicao> caminhoPercorrido = new ArrayList<>();
    private final Timer timerIdle;

    private Missao missao;
    private Robo robo;
    private double xVisual;
    private double yVisual;
    private Direcao direcaoVisual;
    private int quadroAnimacao;
    private int quadroIdle;
    private double progressoColeta;
    private Timer timerMovimento;
    private boolean movendo;

    public PainelMapa(Missao missao, Robo robo) {
        this.missao = missao;
        this.robo = robo;
        xVisual = robo.getPosicao().getX();
        yVisual = robo.getPosicao().getY();
        direcaoVisual = robo.getDirecao();
        carregarSprites2D();
        caminhoPercorrido.add(robo.getPosicao());

        timerIdle = new Timer(520, evento -> {
            if (!movendo) {
                quadroIdle = (quadroIdle + 1) % 2;
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
        quadroAnimacao = 0;
        quadroIdle = 0;
        progressoColeta = 0;
        caminhoPercorrido.clear();
        caminhoPercorrido.add(robo.getPosicao());
        repaint();
    }

    public void atualizarModelo(Robo novoRobo) {
        robo = novoRobo;
        repaint();
    }

    public void animarMovimento(
            Posicao origem,
            Posicao destino,
            Direcao direcao,
            Runnable aoTerminar) {
        pararAnimacao();
        movendo = true;
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
        long inicio = System.currentTimeMillis();
        int duracao = 520;

        timerMovimento = new Timer(38, evento -> {
            progressoColeta = Math.min(1.0, (System.currentTimeMillis() - inicio) / (double) duracao);
            repaint();

            if (progressoColeta >= 1.0) {
                pararAnimacao();
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
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    g.fillRect(px + 1, py + 1, tamanho - 1, tamanho - 1);

                    if (indiceTrilha >= 0) {
                        g.setColor(AZUL);
                        g.fillRect(px + 1, py + tamanho - 5, tamanho - 1, 4);
                    }
                }

                g.setColor(LINHA);
                g.drawRect(px, py, tamanho, tamanho);
            }
        }
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
        int centroY = inicioY + (int) Math.round((yVisual + 0.5) * tamanho);
        int salto = (int) Math.round(Math.sin(progressoColeta * Math.PI) * tamanho * 0.10);
        centroY -= salto;

        int linha = indiceDirecao(direcaoVisual);
        BufferedImage sprite = movendo
                ? spritesAndando[linha][quadroAnimacao]
                : spritesIdle[linha][quadroIdle];
        int tamanhoSprite = Math.min(104, (int) (tamanho * 0.74));
        int destinoX = centroX - tamanhoSprite / 2;
        int destinoY = centroY - tamanhoSprite / 2;

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(sprite, destinoX, destinoY, tamanhoSprite, tamanhoSprite, null);
    }

    private void carregarSprites2D() {
        for (Direcao direcao : Direcao.values()) {
            int linha = indiceDirecao(direcao);
            for (int quadro = 0; quadro < 3; quadro++) {
                spritesAndando[linha][quadro] = SpriteRobo.criar(direcao, quadro, true);
            }
            for (int quadro = 0; quadro < 2; quadro++) {
                spritesIdle[linha][quadro] = SpriteRobo.criar(direcao, quadro, false);
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
}
