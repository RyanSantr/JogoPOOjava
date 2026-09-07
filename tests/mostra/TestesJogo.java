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
        testarDesafios();

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
                verificar(missao.getCasaBonus() != null, "todo mapa deve ter uma casa bônus");
                verificar(!missao.temParede(missao.getCasaBonus()), "a casa bônus deve estar livre");
                verificar(!missao.foraDoMapa(missao.getCasaBonus()), "a casa bônus deve ficar dentro do mapa");
                verificar(missao.calcularMenorCaminhoViaBonus() <= Missao.MAXIMO_MOVIMENTOS,
                        "a rota da casa bônus deve caber no limite de movimentos");

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

    private static void testarDesafios() {
        Missao missao = Missao.criarMissoes().get(0);
        Robo robo = missao.criarRobo();
        int meta = missao.calcularMenorCaminho() + 1;

        verificar(Desafio.LIVRE.foiCumprido(missao, robo, 10, false),
                "a missão normal não deve exigir uma regra extra");
        verificar(Desafio.CAMINHO_CURTO.foiCumprido(missao, robo, meta, false),
                "o código com a quantidade ideal deve cumprir o desafio curto");
        verificar(!Desafio.CAMINHO_CURTO.foiCumprido(missao, robo, meta + 1, false),
                "um código longo não deve cumprir o desafio curto");
        verificar(Desafio.BATERIA.foiCumprido(missao, robo, meta, false),
                "o robô carregado deve cumprir o desafio de bateria");

        for (int i = 0; i < 9; i++) {
            robo.mover(Direcao.LESTE);
        }
        verificar(!Desafio.BATERIA.foiCumprido(missao, robo, meta, false),
                "o desafio deve falhar abaixo de 30% de bateria");
        verificar(Desafio.CASA_BONUS.foiCumprido(missao, robo, meta, true),
                "passar pela estrela deve cumprir a rota alternativa");
        verificar(!Desafio.CASA_BONUS.foiCumprido(missao, robo, meta, false),
                "ignorar a estrela não deve cumprir a rota alternativa");
        verificar(Desafio.CASA_BONUS.getMetaComandos(missao)
                        == missao.calcularMenorCaminhoViaBonus() + 1,
                "a meta da rota alternativa deve incluir o comando pegarItem()");
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
