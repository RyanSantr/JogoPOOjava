package mostra;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Missao {
    static final int COLUNAS = 6;
    static final int LINHAS = 4;
    static final int MAXIMO_MOVIMENTOS = 9;

    private final String nome;
    private final String descricao;
    private final Posicao inicio;
    private final Direcao direcaoInicial;
    private final Posicao objetivo;
    private final int quantidadeParedes;
    private final Random sorteio = new Random();
    private List<Posicao> paredes = new ArrayList<>();

    public Missao(
            String nome,
            String descricao,
            Posicao inicio,
            Direcao direcaoInicial,
            Posicao objetivo,
            int quantidadeParedes) {
        this.nome = nome;
        this.descricao = descricao;
        this.inicio = inicio;
        this.direcaoInicial = direcaoInicial;
        this.objetivo = objetivo;
        this.quantidadeParedes = quantidadeParedes;
        gerarNovoMapa();
    }

    public static List<Missao> criarMissoes() {
        return List.of(
                new Missao(
                        "Missão 1: energia",
                        "Leve o robô até a energia azul.",
                        new Posicao(0, 3),
                        Direcao.SUL,
                        new Posicao(5, 0),
                        5),
                new Missao(
                        "Missão 2: recarga",
                        "Encontre um caminho seguro até a bateria azul.",
                        new Posicao(5, 3),
                        Direcao.OESTE,
                        new Posicao(0, 1),
                        6));
    }

    public void gerarNovoMapa() {
        List<Posicao> casasDisponiveis = new ArrayList<>();
        for (int y = 0; y < LINHAS; y++) {
            for (int x = 0; x < COLUNAS; x++) {
                Posicao posicao = new Posicao(x, y);
                if (!posicao.mesmaPosicao(inicio) && !posicao.mesmaPosicao(objetivo)) {
                    casasDisponiveis.add(posicao);
                }
            }
        }

        for (int tentativa = 0; tentativa < 300; tentativa++) {
            Collections.shuffle(casasDisponiveis, sorteio);
            paredes = new ArrayList<>(casasDisponiveis.subList(0, quantidadeParedes));
            int distancia = calcularMenorCaminho();
            if (distancia >= 0 && distancia <= MAXIMO_MOVIMENTOS) {
                return;
            }
        }

        paredes = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Posicao getObjetivo() {
        return objetivo;
    }

    public String getDica() {
        return "O menor caminho deste mapa usa " + calcularMenorCaminho()
                + " movimentos. Observe primeiro quais casas estão livres.";
    }

    public Robo criarRobo() {
        return new Robo(inicio, direcaoInicial);
    }

    public boolean temParede(Posicao posicao) {
        for (Posicao parede : paredes) {
            if (parede.mesmaPosicao(posicao)) {
                return true;
            }
        }
        return false;
    }

    public boolean foraDoMapa(Posicao posicao) {
        return posicao.getX() < 0
                || posicao.getX() >= COLUNAS
                || posicao.getY() < 0
                || posicao.getY() >= LINHAS;
    }

    public int calcularMenorCaminho() {
        int[][] distancias = new int[LINHAS][COLUNAS];
        for (int y = 0; y < LINHAS; y++) {
            for (int x = 0; x < COLUNAS; x++) {
                distancias[y][x] = -1;
            }
        }

        Queue<Posicao> fila = new ArrayDeque<>();
        fila.add(inicio);
        distancias[inicio.getY()][inicio.getX()] = 0;

        while (!fila.isEmpty()) {
            Posicao atual = fila.remove();
            if (atual.mesmaPosicao(objetivo)) {
                return distancias[atual.getY()][atual.getX()];
            }

            for (Direcao direcao : Direcao.values()) {
                Posicao proxima = atual.mover(direcao);
                if (foraDoMapa(proxima)
                        || temParede(proxima)
                        || distancias[proxima.getY()][proxima.getX()] >= 0) {
                    continue;
                }

                distancias[proxima.getY()][proxima.getX()]
                        = distancias[atual.getY()][atual.getX()] + 1;
                fila.add(proxima);
            }
        }

        return -1;
    }
}
