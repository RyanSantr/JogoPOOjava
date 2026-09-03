package mostra;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class TestesJogo {
    private static int verificacoes;

    public static void main(String[] args) {
        testarMovimentosDoRobo();
        testarColetaUnica();
        testarMapasAleatorios();

        System.out.println("Tudo certo: " + verificacoes + " verificações passaram.");
    }

    private static void testarMovimentosDoRobo() {
        Robo robo = new Robo(new Posicao(2, 2), Direcao.SUL);

        robo.cima();
        verificar(robo.getPosicao().mesmaPosicao(new Posicao(2, 1)), "cima() deve diminuir Y");

        robo.direita();
        verificar(robo.getPosicao().mesmaPosicao(new Posicao(3, 1)), "direita() deve aumentar X");

        robo.baixo();
        verificar(robo.getPosicao().mesmaPosicao(new Posicao(3, 2)), "baixo() deve aumentar Y");

        robo.esquerda();
        verificar(robo.getPosicao().mesmaPosicao(new Posicao(2, 2)), "esquerda() deve diminuir X");
        verificar(robo.getBateria() == 68, "quatro movimentos devem gastar 32% de bateria");
    }

    private static void testarColetaUnica() {
        Robo robo = new Robo();
        robo.pegarItem();
        robo.pegarItem();

        verificar(robo.pegouItem(), "o item deve ficar marcado como coletado");
        verificar(robo.getBateria() == 98, "coletar o mesmo item duas vezes não deve gastar bateria novamente");
    }

    private static void testarMapasAleatorios() {
        for (Missao missao : Missao.criarMissoes()) {
            for (int rodada = 0; rodada < 200; rodada++) {
                missao.gerarNovoMapa();
                int distancia = missao.calcularMenorCaminho();

                verificar(distancia >= 0, "todo mapa deve possuir solução");
                verificar(distancia <= Missao.MAXIMO_MOVIMENTOS,
                        "a solução deve caber no limite de comandos");

                Robo robo = missao.criarRobo();
                verificar(!missao.temParede(robo.getPosicao()), "o robô não pode nascer em uma parede");
                verificar(!missao.temParede(missao.getObjetivo()), "o objetivo não pode conter uma parede");

                List<Direcao> caminho = encontrarCaminho(missao, robo.getPosicao());
                verificar(caminho.size() == distancia, "a distância calculada deve ser a do menor caminho");

                for (Direcao direcao : caminho) {
                    robo.mover(direcao);
                }
                verificar(robo.getPosicao().mesmaPosicao(missao.getObjetivo()),
                        "o caminho encontrado deve terminar no objetivo");
            }
        }
    }

    private static List<Direcao> encontrarCaminho(Missao missao, Posicao inicio) {
        Queue<Passo> fila = new ArrayDeque<>();
        boolean[][] visitada = new boolean[Missao.LINHAS][Missao.COLUNAS];
        fila.add(new Passo(inicio, Collections.emptyList()));
        visitada[inicio.getY()][inicio.getX()] = true;

        while (!fila.isEmpty()) {
            Passo atual = fila.remove();
            if (atual.posicao.mesmaPosicao(missao.getObjetivo())) {
                return atual.caminho;
            }

            for (Direcao direcao : Direcao.values()) {
                Posicao proxima = atual.posicao.mover(direcao);
                if (missao.foraDoMapa(proxima)
                        || missao.temParede(proxima)
                        || visitada[proxima.getY()][proxima.getX()]) {
                    continue;
                }

                visitada[proxima.getY()][proxima.getX()] = true;
                List<Direcao> novoCaminho = new ArrayList<>(atual.caminho);
                novoCaminho.add(direcao);
                fila.add(new Passo(proxima, novoCaminho));
            }
        }

        throw new AssertionError("O mapa deveria possuir um caminho.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        verificacoes++;
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static class Passo {
        private final Posicao posicao;
        private final List<Direcao> caminho;

        private Passo(Posicao posicao, List<Direcao> caminho) {
            this.posicao = posicao;
            this.caminho = caminho;
        }
    }
}
