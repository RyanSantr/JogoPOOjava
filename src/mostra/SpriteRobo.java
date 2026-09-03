package mostra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class SpriteRobo {
    public static final int TAMANHO = 32;

    private static final Color CONTORNO = new Color(15, 23, 35);
    private static final Color BRANCO = new Color(245, 248, 252);
    private static final Color SOMBRA = new Color(174, 188, 205);
    private static final Color AZUL = new Color(25, 103, 245);
    private static final Color AZUL_CLARO = new Color(91, 190, 255);
    private static final Color AZUL_ESCURO = new Color(8, 44, 96);

    private SpriteRobo() {
    }

    public static BufferedImage criar(Direcao direcao, int quadro, boolean andando) {
        BufferedImage sprite = new BufferedImage(TAMANHO, TAMANHO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sprite.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int fase = Math.floorMod(quadro, andando ? 3 : 2);
        int deslocamentoY = andando && fase == 1 ? -1 : 0;
        desenharSombra(g, fase, andando);

        AffineTransform transformacaoOriginal = g.getTransform();
        g.translate(0, deslocamentoY);
        if (direcao == Direcao.NORTE) {
            desenharCostas(g, fase, andando);
        } else if (direcao == Direcao.SUL) {
            desenharFrente(g, fase, andando);
        } else {
            desenharLado(g, fase, andando, direcao == Direcao.LESTE);
        }
        g.setTransform(transformacaoOriginal);
        g.dispose();
        return sprite;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage folha = criarFolhaDeSprites(4);
        Path destino = Path.of("assets", "sprites", "robo-sprites-2d.png");
        Files.createDirectories(destino.getParent());
        ImageIO.write(folha, "png", destino.toFile());
        System.out.println("Sprites gerados em " + destino.toAbsolutePath());
    }

    private static BufferedImage criarFolhaDeSprites(int escala) {
        Direcao[] linhas = {Direcao.NORTE, Direcao.SUL, Direcao.OESTE, Direcao.LESTE};
        int celula = TAMANHO * escala;
        BufferedImage folha = new BufferedImage(celula * 3, celula * 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = folha.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        for (int linha = 0; linha < linhas.length; linha++) {
            for (int quadro = 0; quadro < 3; quadro++) {
                BufferedImage sprite = criar(linhas[linha], quadro, true);
                g.drawImage(sprite, quadro * celula, linha * celula, celula, celula, null);
            }
        }
        g.dispose();
        return folha;
    }

    private static void desenharSombra(Graphics2D g, int fase, boolean andando) {
        int largura = andando && fase == 1 ? 12 : 14;
        g.setColor(new Color(15, 23, 35, 55));
        g.fillRect(16 - largura / 2, 29, largura, 2);
    }

    private static void desenharFrente(Graphics2D g, int fase, boolean andando) {
        desenharAntena(g, fase);

        g.setColor(CONTORNO);
        g.fillRect(5, 8, 3, 8);
        g.fillRect(24, 8, 3, 8);
        g.fillRect(7, 5, 18, 13);
        g.setColor(BRANCO);
        g.fillRect(8, 6, 16, 11);
        g.setColor(SOMBRA);
        g.fillRect(8, 15, 16, 2);

        g.setColor(AZUL_ESCURO);
        g.fillRect(9, 8, 14, 6);
        g.setColor(AZUL_CLARO);
        g.fillRect(11, 10, 3, 3);
        g.fillRect(18, 10, 3, 3);
        g.setColor(Color.WHITE);
        g.fillRect(12, 10, 1, 1);
        g.fillRect(19, 10, 1, 1);

        desenharCorpoFrontal(g);
        desenharBracos(g, fase, andando);
        desenharPernasFrontais(g, fase, andando);
    }

    private static void desenharCostas(Graphics2D g, int fase, boolean andando) {
        desenharAntena(g, fase);

        g.setColor(CONTORNO);
        g.fillRect(5, 8, 3, 8);
        g.fillRect(24, 8, 3, 8);
        g.fillRect(7, 5, 18, 13);
        g.setColor(BRANCO);
        g.fillRect(8, 6, 16, 11);
        g.setColor(SOMBRA);
        g.fillRect(8, 15, 16, 2);
        g.setColor(AZUL_ESCURO);
        g.fillRect(10, 8, 12, 6);
        g.setColor(AZUL);
        g.fillRect(11, 9, 10, 2);
        g.setColor(AZUL_CLARO);
        g.fillRect(12, 9, 5, 1);
        g.setColor(CONTORNO);
        g.fillRect(12, 12, 2, 1);
        g.fillRect(15, 12, 2, 1);
        g.fillRect(18, 12, 2, 1);

        g.setColor(CONTORNO);
        g.fillRect(9, 17, 14, 11);
        g.setColor(BRANCO);
        g.fillRect(10, 18, 12, 9);
        g.setColor(SOMBRA);
        g.fillRect(10, 25, 12, 2);
        g.setColor(AZUL_ESCURO);
        g.fillRect(12, 19, 8, 6);
        g.setColor(AZUL);
        g.fillRect(13, 20, 6, 3);
        g.setColor(AZUL_CLARO);
        g.fillRect(14, 20, 2, 1);

        desenharBracos(g, fase, andando);
        desenharPernasFrontais(g, fase, andando);
    }

    private static void desenharLado(Graphics2D g, int fase, boolean andando, boolean direita) {
        AffineTransform original = g.getTransform();
        if (!direita) {
            g.translate(TAMANHO, 0);
            g.scale(-1, 1);
        }

        desenharAntena(g, fase);
        g.setColor(CONTORNO);
        g.fillRect(8, 6, 16, 12);
        g.fillRect(23, 9, 3, 7);
        g.setColor(BRANCO);
        g.fillRect(9, 7, 14, 10);
        g.setColor(SOMBRA);
        g.fillRect(9, 15, 14, 2);
        g.setColor(AZUL_ESCURO);
        g.fillRect(17, 8, 6, 6);
        g.setColor(AZUL_CLARO);
        g.fillRect(20, 10, 2, 3);
        g.setColor(Color.WHITE);
        g.fillRect(21, 10, 1, 1);

        g.setColor(CONTORNO);
        g.fillRect(10, 17, 13, 11);
        g.setColor(BRANCO);
        g.fillRect(11, 18, 11, 9);
        g.setColor(SOMBRA);
        g.fillRect(11, 25, 11, 2);
        g.setColor(AZUL);
        g.fillRect(17, 20, 4, 4);
        g.setColor(CONTORNO);
        g.fillRect(7, 19, 4, 6);
        g.setColor(AZUL_ESCURO);
        g.fillRect(8, 20, 2, 4);
        g.setColor(CONTORNO);
        g.fillRect(21, 19 + (andando && fase == 2 ? 1 : 0), 4, 7);
        g.setColor(BRANCO);
        g.fillRect(22, 20 + (andando && fase == 2 ? 1 : 0), 2, 4);

        int pernaFrenteX = andando && fase == 1 ? 19 : 18;
        int pernaTrasX = andando && fase == 2 ? 10 : 11;
        g.setColor(CONTORNO);
        g.fillRect(pernaTrasX, 27, 5, 4);
        g.fillRect(pernaFrenteX, 27, 5, 4);
        g.setColor(AZUL_ESCURO);
        g.fillRect(pernaTrasX + 1, 28, 4, 2);
        g.fillRect(pernaFrenteX + 1, 28, 4, 2);
        g.setColor(AZUL_CLARO);
        g.fillRect(pernaFrenteX + 3, 28, 2, 1);
        g.setTransform(original);
    }

    private static void desenharAntena(Graphics2D g, int fase) {
        g.setColor(CONTORNO);
        g.fillRect(15, 2, 2, 4);
        g.fillRect(13, 0, 6, 3);
        g.setColor(fase % 2 == 0 ? AZUL : AZUL_CLARO);
        g.fillRect(14, 0, 4, 2);
        g.setColor(Color.WHITE);
        g.fillRect(15, 0, 1, 1);
    }

    private static void desenharCorpoFrontal(Graphics2D g) {
        g.setColor(CONTORNO);
        g.fillRect(9, 17, 14, 11);
        g.setColor(BRANCO);
        g.fillRect(10, 18, 12, 9);
        g.setColor(SOMBRA);
        g.fillRect(10, 25, 12, 2);
        g.setColor(AZUL_ESCURO);
        g.fillRect(12, 20, 8, 5);
        g.setColor(AZUL);
        g.fillRect(13, 21, 6, 3);
        g.setColor(AZUL_CLARO);
        g.fillRect(14, 21, 2, 1);
    }

    private static void desenharBracos(Graphics2D g, int fase, boolean andando) {
        int esquerdaY = andando && fase == 2 ? 20 : 19;
        int direitaY = andando && fase == 1 ? 20 : 19;
        g.setColor(CONTORNO);
        g.fillRect(5, esquerdaY, 4, 7);
        g.fillRect(23, direitaY, 4, 7);
        g.setColor(BRANCO);
        g.fillRect(6, esquerdaY + 1, 2, 4);
        g.fillRect(24, direitaY + 1, 2, 4);
        g.setColor(AZUL_ESCURO);
        g.fillRect(6, esquerdaY + 5, 2, 2);
        g.fillRect(24, direitaY + 5, 2, 2);
    }

    private static void desenharPernasFrontais(Graphics2D g, int fase, boolean andando) {
        int esquerdaY = andando && fase == 1 ? 28 : 27;
        int direitaY = andando && fase == 2 ? 28 : 27;
        g.setColor(CONTORNO);
        g.fillRect(10, esquerdaY, 5, 4);
        g.fillRect(17, direitaY, 5, 4);
        g.setColor(AZUL_ESCURO);
        g.fillRect(11, esquerdaY + 1, 4, 2);
        g.fillRect(18, direitaY + 1, 4, 2);
        g.setColor(AZUL_CLARO);
        g.fillRect(12, esquerdaY + 1, 2, 1);
        g.fillRect(19, direitaY + 1, 2, 1);
    }
}
