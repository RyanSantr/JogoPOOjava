package mostra;

public enum Comando {
    CIMA("robo.cima();", Direcao.NORTE),
    BAIXO("robo.baixo();", Direcao.SUL),
    ESQUERDA("robo.esquerda();", Direcao.OESTE),
    DIREITA("robo.direita();", Direcao.LESTE),
    PEGAR_ITEM("robo.pegarItem();", null);

    private final String codigoJava;
    private final Direcao direcao;

    Comando(String codigoJava, Direcao direcao) {
        this.codigoJava = codigoJava;
        this.direcao = direcao;
    }

    public String getCodigoJava() {
        return codigoJava;
    }

    public boolean ehMovimento() {
        return direcao != null;
    }

    public Direcao getDirecao() {
        return direcao;
    }
}
