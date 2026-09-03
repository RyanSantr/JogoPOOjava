package mostra;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JogoRoboFrame janela = new JogoRoboFrame();
            janela.setVisible(true);
        });
    }
}
