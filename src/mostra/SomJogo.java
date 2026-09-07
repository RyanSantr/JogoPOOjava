package mostra;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SomJogo {
    private static final float TAXA_AMOSTRAGEM = 8_000;
    private volatile boolean ligado = true;

    public boolean alternar() {
        ligado = !ligado;
        if (ligado) {
            tocarSequencia(new int[] {660}, new int[] {80});
        }
        return ligado;
    }

    public void tocarPasso(Direcao direcao) {
        int frequencia = switch (direcao) {
            case NORTE -> 620;
            case SUL -> 520;
            case LESTE -> 580;
            case OESTE -> 550;
        };
        tocarSequencia(new int[] {frequencia}, new int[] {55});
    }

    public void tocarErro() {
        tocarSequencia(new int[] {190, 140}, new int[] {110, 180});
    }

    public void tocarColeta() {
        tocarSequencia(new int[] {620, 780, 980}, new int[] {80, 80, 150});
    }

    public void tocarVitoria() {
        tocarSequencia(new int[] {523, 659, 784, 1047}, new int[] {100, 100, 110, 260});
    }

    public void tocarDanca() {
        tocarSequencia(
                new int[] {440, 660, 550, 740, 440, 880},
                new int[] {120, 120, 120, 120, 120, 260});
    }

    private void tocarSequencia(int[] frequencias, int[] duracoes) {
        if (!ligado) {
            return;
        }

        Thread som = new Thread(() -> {
            for (int i = 0; i < frequencias.length && ligado; i++) {
                tocarTom(frequencias[i], duracoes[i]);
            }
        }, "som-do-jogo");
        som.setDaemon(true);
        som.start();
    }

    private void tocarTom(int frequencia, int duracao) {
        AudioFormat formato = new AudioFormat(TAXA_AMOSTRAGEM, 8, 1, true, false);
        int quantidade = Math.round(TAXA_AMOSTRAGEM * duracao / 1000f);
        byte[] dados = new byte[quantidade];

        for (int i = 0; i < dados.length; i++) {
            double onda = Math.sin(2 * Math.PI * frequencia * i / TAXA_AMOSTRAGEM);
            dados[i] = (byte) (onda * 32);
        }

        try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
            linha.open(formato);
            linha.start();
            linha.write(dados, 0, dados.length);
            linha.drain();
        } catch (Exception ignorada) {
            // O jogo continua normalmente em computadores sem saída de áudio.
        }
    }
}
