package mostra;

public enum Desafio {
    LIVRE("Missão normal"),
    CAMINHO_CURTO("Código curto"),
    BATERIA("Poupe bateria"),
    CASA_BONUS("Rota alternativa");

    private final String nome;

    Desafio(String nome) {
        this.nome = nome;
    }

    public String getDescricao(Missao missao) {
        return switch (this) {
            case LIVRE -> "Chegue até a energia e use pegarItem().";
            case CAMINHO_CURTO -> "Use no máximo "
                    + (missao.calcularMenorCaminho() + 1) + " comandos.";
            case BATERIA -> "Termine a missão com pelo menos 30% de bateria.";
            case CASA_BONUS -> "Passe pela casa com estrela antes de pegar a energia.";
        };
    }

    public boolean foiCumprido(
            Missao missao,
            Robo robo,
            int quantidadeComandos,
            boolean passouNaCasaBonus) {
        return switch (this) {
            case LIVRE -> true;
            case CAMINHO_CURTO -> quantidadeComandos <= missao.calcularMenorCaminho() + 1;
            case BATERIA -> robo.getBateria() >= 30;
            case CASA_BONUS -> passouNaCasaBonus;
        };
    }

    public int getMetaComandos(Missao missao) {
        if (this == CASA_BONUS) {
            return missao.calcularMenorCaminhoViaBonus() + 1;
        }
        return missao.calcularMenorCaminho() + 1;
    }

    @Override
    public String toString() {
        return nome;
    }
}
