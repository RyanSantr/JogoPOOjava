package mostra;

public enum Direcao {
    NORTE(0, -1, "Norte"),
    LESTE(1, 0, "Leste"),
    SUL(0, 1, "Sul"),
    OESTE(-1, 0, "Oeste");

    private final int movimentoX;
    private final int movimentoY;
    private final String nome;

    Direcao(int movimentoX, int movimentoY, String nome) {
        this.movimentoX = movimentoX;
        this.movimentoY = movimentoY;
        this.nome = nome;
    }

    public int getMovimentoX() {
        return movimentoX;
    }

    public int getMovimentoY() {
        return movimentoY;
    }

    public String getNome() {
        return nome;
    }

}
