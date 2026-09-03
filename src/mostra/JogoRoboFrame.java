package mostra;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class JogoRoboFrame extends JFrame {
    private static final Color AZUL = new Color(25, 103, 245);
    private static final Color PRETO = new Color(18, 22, 28);
    private static final Color CINZA = new Color(105, 113, 125);
    private static final Color CINZA_CLARO = new Color(222, 226, 232);
    private static final int LIMITE_COMANDOS = 10;

    private final List<Missao> missoes;
    private final List<Comando> comandosEscolhidos;
    private final List<JButton> botoesEdicao;
    private final JComboBox<String> seletorMissao;
    private final JLabel tituloMissao;
    private final JLabel descricaoMissao;
    private final JLabel statusRobo;
    private final JLabel contadorComandos;
    private final JTextArea areaCodigo;
    private final PainelMapa painelMapa;

    private Missao missaoAtual;
    private Robo robo;
    private int indiceExecucao;
    private boolean executando;

    public JogoRoboFrame() {
        super("POO com Java - Missão Robô");
        missoes = Missao.criarMissoes();
        comandosEscolhidos = new ArrayList<>();
        botoesEdicao = new ArrayList<>();
        missaoAtual = missoes.get(0);
        robo = missaoAtual.criarRobo();

        seletorMissao = criarSeletorMissao();
        tituloMissao = criarLabel("", 25, Font.BOLD, PRETO);
        descricaoMissao = criarLabel("", 15, Font.PLAIN, CINZA);
        statusRobo = criarLabel("", 14, Font.BOLD, PRETO);
        contadorComandos = criarLabel("", 13, Font.PLAIN, CINZA);
        areaCodigo = criarAreaCodigo();
        painelMapa = new PainelMapa(missaoAtual, robo);

        configurarJanela();
        montarTela();
        atualizarTela();
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 620));
    }

    private void montarTela() {
        JPanel raiz = new JPanel(new BorderLayout(16, 16));
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        raiz.setBackground(new Color(244, 247, 251));
        setContentPane(raiz);

        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarAreaCentral(), BorderLayout.CENTER);

        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        int largura = Math.min(1220, tela.width - 70);
        int altura = Math.min(740, tela.height - 90);
        setSize(Math.max(1000, largura), Math.max(620, altura));
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout(20, 0));
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CINZA_CLARO),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(tituloMissao);
        textos.add(Box.createVerticalStrut(4));
        textos.add(descricaoMissao);
        textos.add(Box.createVerticalStrut(14));

        JButton novoMapa = criarBotaoSecundario("Novo mapa", this::gerarNovoMapa);
        JButton dica = criarBotaoSecundario("Dica", this::mostrarDica);
        botoesEdicao.add(novoMapa);
        botoesEdicao.add(dica);

        JPanel informacoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        informacoes.setOpaque(false);
        informacoes.add(statusRobo);
        informacoes.add(novoMapa);
        informacoes.add(dica);
        informacoes.add(seletorMissao);

        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(informacoes, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarAreaCentral() {
        JPanel central = new JPanel(new BorderLayout(16, 0));
        central.setOpaque(false);
        central.add(criarAreaMapa(), BorderLayout.CENTER);
        central.add(criarPainelCodigo(), BorderLayout.EAST);
        return central;
    }

    private JPanel criarAreaMapa() {
        JPanel areaMapa = new JPanel(new BorderLayout(0, 8));
        areaMapa.setBackground(Color.WHITE);
        areaMapa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CINZA_CLARO),
                new EmptyBorder(14, 14, 14, 14)));
        areaMapa.add(criarLabel("MAPA DA MISSÃO", 13, Font.BOLD, PRETO), BorderLayout.NORTH);
        areaMapa.add(painelMapa, BorderLayout.CENTER);
        areaMapa.add(criarLabel("O caminho percorrido fica marcado em azul.", 13, Font.PLAIN, CINZA),
                BorderLayout.SOUTH);
        return areaMapa;
    }

    private JPanel criarPainelCodigo() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setPreferredSize(new Dimension(400, 560));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CINZA_CLARO),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel titulo = criarLabel("COMPLETE O CÓDIGO", 19, Font.BOLD, PRETO);
        JLabel instrucao = criarLabel("Clique nas fitas para montar a sequência.", 14, Font.PLAIN, CINZA);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(5));
        painel.add(instrucao);
        painel.add(Box.createVerticalStrut(12));

        JScrollPane rolagemCodigo = new JScrollPane(areaCodigo);
        rolagemCodigo.setAlignmentX(Component.LEFT_ALIGNMENT);
        rolagemCodigo.setPreferredSize(new Dimension(385, 245));
        rolagemCodigo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        rolagemCodigo.setBorder(BorderFactory.createLineBorder(CINZA_CLARO));
        rolagemCodigo.getViewport().setBackground(new Color(248, 249, 251));
        painel.add(rolagemCodigo);
        painel.add(Box.createVerticalStrut(6));
        painel.add(contadorComandos);
        painel.add(Box.createVerticalStrut(16));

        painel.add(criarLabel("FITAS DE COMANDO", 13, Font.BOLD, PRETO));
        painel.add(Box.createVerticalStrut(8));

        JPanel fitas = new JPanel(new GridLayout(3, 2, 8, 8));
        fitas.setOpaque(false);
        fitas.setAlignmentX(Component.LEFT_ALIGNMENT);
        fitas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        fitas.add(criarFita(Comando.CIMA));
        fitas.add(criarFita(Comando.BAIXO));
        fitas.add(criarFita(Comando.ESQUERDA));
        fitas.add(criarFita(Comando.DIREITA));
        fitas.add(criarFita(Comando.PEGAR_ITEM));
        JButton desfazer = criarBotaoSecundario("Desfazer", this::desfazerUltimo);
        botoesEdicao.add(desfazer);
        fitas.add(desfazer);
        painel.add(fitas);
        painel.add(Box.createVerticalGlue());

        JButton executar = criarBotaoPrimario("EXECUTAR CODIGO", this::executarCodigo);
        JButton limpar = criarBotaoSecundario("Limpar", this::limparCodigo);
        botoesEdicao.add(executar);
        botoesEdicao.add(limpar);
        limpar.setAlignmentX(Component.LEFT_ALIGNMENT);
        limpar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        painel.add(executar);
        painel.add(Box.createVerticalStrut(8));
        painel.add(limpar);
        return painel;
    }

    private JComboBox<String> criarSeletorMissao() {
        JComboBox<String> combo = new JComboBox<>();
        for (Missao missao : missoes) {
            combo.addItem(missao.getNome());
        }
        combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.addActionListener(this::trocarMissao);
        return combo;
    }

    private JTextArea criarAreaCodigo() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        area.setBackground(new Color(248, 249, 251));
        area.setForeground(PRETO);
        area.setCaretColor(AZUL);
        area.setBorder(new EmptyBorder(14, 14, 14, 14));
        return area;
    }

    private JButton criarFita(Comando comando) {
        JButton botao = new JButton(comando.getCodigoJava());
        botao.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        botao.setHorizontalAlignment(JButton.LEFT);
        configurarBotao(botao, Color.WHITE, PRETO, CINZA_CLARO);
        botao.addActionListener(evento -> adicionarComando(comando));
        botoesEdicao.add(botao);
        return botao;
    }

    private JButton criarBotaoPrimario(String texto, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        configurarBotao(botao, AZUL, Color.WHITE, AZUL);
        botao.addActionListener(acao);
        botao.setAlignmentX(Component.LEFT_ALIGNMENT);
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        return botao;
    }

    private JButton criarBotaoSecundario(String texto, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        configurarBotao(botao, Color.WHITE, PRETO, CINZA_CLARO);
        botao.addActionListener(acao);
        return botao;
    }

    private void configurarBotao(JButton botao, Color fundo, Color texto, Color borda) {
        botao.setFocusPainted(false);
        botao.setBackground(fundo);
        botao.setForeground(texto);
        botao.setOpaque(true);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borda),
                new EmptyBorder(9, 11, 9, 11)));
        botao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private JLabel criarLabel(String texto, int tamanho, int estilo, Color cor) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Font.SANS_SERIF, estilo, tamanho));
        label.setForeground(cor);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void trocarMissao(ActionEvent evento) {
        if (executando) {
            return;
        }

        int indice = seletorMissao.getSelectedIndex();
        if (indice >= 0 && indice < missoes.size()) {
            missaoAtual = missoes.get(indice);
            missaoAtual.gerarNovoMapa();
            limparCodigo(null);
        }
    }

    private void gerarNovoMapa(ActionEvent evento) {
        if (executando) {
            return;
        }
        missaoAtual.gerarNovoMapa();
        limparCodigo(null);
    }

    private void mostrarDica(ActionEvent evento) {
        JOptionPane.showMessageDialog(
                this,
                missaoAtual.getDica(),
                "Dica da missão",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void adicionarComando(Comando comando) {
        if (executando) {
            return;
        }
        if (comandosEscolhidos.size() >= LIMITE_COMANDOS) {
            JOptionPane.showMessageDialog(this, "O código chegou ao limite de 10 comandos.");
            return;
        }

        comandosEscolhidos.add(comando);
        atualizarCodigo();
    }

    private void desfazerUltimo(ActionEvent evento) {
        if (!executando && !comandosEscolhidos.isEmpty()) {
            comandosEscolhidos.remove(comandosEscolhidos.size() - 1);
            atualizarCodigo();
        }
    }

    private void limparCodigo(ActionEvent evento) {
        if (executando) {
            return;
        }

        comandosEscolhidos.clear();
        robo = missaoAtual.criarRobo();
        painelMapa.resetar(missaoAtual, robo);
        atualizarTela();
    }

    private void executarCodigo(ActionEvent evento) {
        if (comandosEscolhidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos uma fita ao código.");
            return;
        }

        executando = true;
        alterarEdicao(false);
        robo = missaoAtual.criarRobo();
        painelMapa.resetar(missaoAtual, robo);
        indiceExecucao = 0;
        atualizarStatus("Preparando a execução...");

        Timer esperaInicial = new Timer(250, e -> {
            ((Timer) e.getSource()).stop();
            executarProximoComando();
        });
        esperaInicial.setRepeats(false);
        esperaInicial.start();
    }

    private void executarProximoComando() {
        if (indiceExecucao >= comandosEscolhidos.size()) {
            finalizarExecucao();
            return;
        }

        Comando comando = comandosEscolhidos.get(indiceExecucao);
        atualizarStatus("Executando: " + comando.getCodigoJava());

        if (comando.ehMovimento()) {
            executarMovimento(comando);
        } else {
            executarColeta();
        }
    }

    private void executarMovimento(Comando comando) {
        Posicao origem = robo.getPosicao();
        Posicao destino = robo.calcularProximaPosicao(comando.getDirecao());

        if (missaoAtual.foraDoMapa(destino)) {
            interromperComErro("O robô tentou sair do mapa.");
            return;
        }
        if (missaoAtual.temParede(destino)) {
            interromperComErro("O robô encontrou uma parede. Revise a ordem das fitas.");
            return;
        }

        robo.mover(comando.getDirecao());
        painelMapa.atualizarModelo(robo);
        painelMapa.animarMovimento(origem, destino, comando.getDirecao(), () -> {
            indiceExecucao++;
            atualizarStatus(null);
            executarProximoComando();
        });
    }

    private void executarColeta() {
        if (robo.pegouItem()) {
            interromperComErro("O item já foi coletado. Remova o comando repetido.");
            return;
        }

        if (!robo.getPosicao().mesmaPosicao(missaoAtual.getObjetivo())) {
            interromperComErro("pegarItem() foi chamado longe da energia.");
            return;
        }

        robo.pegarItem();
        painelMapa.atualizarModelo(robo);
        painelMapa.animarColeta(() -> {
            indiceExecucao++;
            atualizarStatus(null);
            executarProximoComando();
        });
    }

    private void interromperComErro(String mensagem) {
        executando = false;
        alterarEdicao(true);
        atualizarStatus("Execução interrompida");
        JOptionPane.showMessageDialog(this, mensagem, "A lógica precisa de ajuste", JOptionPane.WARNING_MESSAGE);
    }

    private void finalizarExecucao() {
        executando = false;
        alterarEdicao(true);
        boolean chegou = robo.getPosicao().mesmaPosicao(missaoAtual.getObjetivo());
        boolean concluiu = chegou && robo.pegouItem();
        atualizarStatus(concluiu ? "Missão concluída" : "Código finalizado");

        if (concluiu) {
            JOptionPane.showMessageDialog(this,
                    "Missão concluída! Seu caminho funcionou.",
                    "Muito bem",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "O código terminou, mas ainda falta chegar na energia e usar pegarItem().",
                    "Continue tentando",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void alterarEdicao(boolean habilitada) {
        for (JButton botao : botoesEdicao) {
            botao.setEnabled(habilitada);
        }
        seletorMissao.setEnabled(habilitada);
    }

    private void atualizarTela() {
        tituloMissao.setText(missaoAtual.getNome().toUpperCase());
        descricaoMissao.setText(missaoAtual.getDescricao());
        atualizarStatus(null);
        atualizarCodigo();
    }

    private void atualizarStatus(String mensagem) {
        if (mensagem != null) {
            statusRobo.setText(mensagem + "  |  Bateria: " + robo.getBateria() + "%");
            return;
        }

        Posicao posicao = robo.getPosicao();
        statusRobo.setText("Bateria: " + robo.getBateria() + "%  |  Coluna: "
                + (posicao.getX() + 1) + "  |  Linha: " + (posicao.getY() + 1));
    }

    private void atualizarCodigo() {
        StringBuilder codigo = new StringBuilder();
        codigo.append("public void completarMissao() {\n");
        codigo.append("    Robo robo = new Robo();\n\n");

        for (Comando comando : comandosEscolhidos) {
            codigo.append("    ").append(comando.getCodigoJava()).append("\n");
        }

        if (comandosEscolhidos.size() < LIMITE_COMANDOS) {
            codigo.append("    // clique em uma fita\n");
        }

        codigo.append("}");
        areaCodigo.setText(codigo.toString());
        areaCodigo.setCaretPosition(0);
        contadorComandos.setText(comandosEscolhidos.size() + " de " + LIMITE_COMANDOS + " comandos");
    }
}
