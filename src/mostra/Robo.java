package mostra;

public class Robo {
    private Posicao posicao;
    private Direcao direcao;
    private int bateria;
    private boolean pegouItem;

    public Robo() {
        this(new Posicao(0, 0), Direcao.SUL);
    }

    public Robo(Posicao posicaoInicial, Direcao direcaoInicial) {
        posicao = posicaoInicial;
        direcao = direcaoInicial;
        bateria = 100;
        pegouItem = false;
    }

    public Posicao getPosicao() {
        return posicao;
    }

    public Direcao getDirecao() {
        return direcao;
    }

    public int getBateria() {
        return bateria;
    }

    public boolean pegouItem() {
        return pegouItem;
    }

    public Posicao calcularProximaPosicao(Direcao novaDirecao) {
        return posicao.mover(novaDirecao);
    }

    public void mover(Direcao novaDirecao) {
        direcao = novaDirecao;
        posicao = calcularProximaPosicao(novaDirecao);
        gastarBateria(8);
    }

    public void cima() {
        mover(Direcao.NORTE);
    }

    public void baixo() {
        mover(Direcao.SUL);
    }

    public void esquerda() {
        mover(Direcao.OESTE);
    }

    public void direita() {
        mover(Direcao.LESTE);
    }

    public void pegarItem() {
        if (pegouItem) {
            return;
        }

        pegouItem = true;
        gastarBateria(2);
    }

    private void gastarBateria(int quantidade) {
        bateria = Math.max(0, bateria - quantidade);
    }
}
