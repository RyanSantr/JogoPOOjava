package mostra;

public class Posicao {
    private final int x;
    private final int y;

    public Posicao(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Posicao mover(Direcao direcao) {
        return new Posicao(x + direcao.getMovimentoX(), y + direcao.getMovimentoY());
    }

    public boolean mesmaPosicao(Posicao outra) {
        return x == outra.x && y == outra.y;
    }
}
