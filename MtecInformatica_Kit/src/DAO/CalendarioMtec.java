package DAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class CalendarioMtec extends JDialog {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private final JTextField campoDestino;
    private final JLabel lblMesAno = new JLabel("", SwingConstants.CENTER);
    private final JPanel painelDias = new JPanel(new GridLayout(6, 7, 4, 4));
    private YearMonth mesAtual;
    private LocalDate dataSelecionada;

    private final Color corFundo = new Color(20, 43, 66);
    private final Color corTopo = new Color(15, 35, 55);
    private final Color corTexto = Color.WHITE;
    private final Color corDestaque = new Color(80, 180, 180);
    private final Color corHoje = new Color(0, 210, 210);
    private final Color corDia = new Color(27, 58, 84);

    private CalendarioMtec(Window owner, JTextField campoDestino) {
        super(owner instanceof Frame ? (Frame) owner : null, "Selecionar data", ModalityType.APPLICATION_MODAL);
        this.campoDestino = campoDestino;
        this.dataSelecionada = lerDataCampo(campoDestino);
        this.mesAtual = YearMonth.from(dataSelecionada);

        setResizable(false);
        setContentPane(criarConteudo());
        pack();
        setLocationRelativeTo(owner);
        desenharDias();
    }

    public static void abrir(Component parent, JTextField campoDestino) {
        if (campoDestino == null || !campoDestino.isEnabled() || !campoDestino.isEditable()) {
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(parent);
        new CalendarioMtec(owner, campoDestino).setVisible(true);
    }

    private JPanel criarConteudo() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(corFundo);
        painel.setBorder(new EmptyBorder(16, 16, 14, 16));

        painel.add(criarTopo(), BorderLayout.NORTH);
        painel.add(criarCentro(), BorderLayout.CENTER);
        painel.add(criarRodape(), BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setOpaque(false);

        JButton btnAnterior = criarBotaoTopo("<");
        btnAnterior.setToolTipText("Ir para o mês anterior.");
        JButton btnProximo = criarBotaoTopo(">");
        btnProximo.setToolTipText("Ir para o próximo mês.");

        lblMesAno.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMesAno.setForeground(corTexto);

        btnAnterior.addActionListener(e -> {
            mesAtual = mesAtual.minusMonths(1);
            desenharDias();
        });

        btnProximo.addActionListener(e -> {
            mesAtual = mesAtual.plusMonths(1);
            desenharDias();
        });

        topo.add(btnAnterior, BorderLayout.WEST);
        topo.add(lblMesAno, BorderLayout.CENTER);
        topo.add(btnProximo, BorderLayout.EAST);

        return topo;
    }

    private JPanel criarCentro() {
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);

        JPanel semana = new JPanel(new GridLayout(1, 7, 4, 0));
        semana.setOpaque(false);

        String[] dias = {"DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB"};
        for (String dia : dias) {
            JLabel lblDia = new JLabel(dia, SwingConstants.CENTER);
            lblDia.setForeground(new Color(160, 220, 220));
            lblDia.setFont(new Font("Segoe UI", Font.BOLD, 12));
            semana.add(lblDia);
        }

        painelDias.setOpaque(false);
        painelDias.setPreferredSize(new Dimension(322, 204));

        centro.add(semana, BorderLayout.NORTH);
        centro.add(painelDias, BorderLayout.CENTER);

        return centro;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rodape.setOpaque(false);

        JButton btnHoje = criarBotaoRodape("Hoje", corDestaque);
        btnHoje.setToolTipText("Selecionar a data de hoje.");
        JButton btnLimpar = criarBotaoRodape("Limpar", new Color(75, 95, 115));
        btnLimpar.setToolTipText("Limpar a data selecionada.");
        JButton btnCancelar = criarBotaoRodape("Cancelar", new Color(180, 30, 30));
        btnCancelar.setToolTipText("Fechar o calendário.");

        btnHoje.addActionListener(e -> confirmar(LocalDate.now()));
        btnLimpar.addActionListener(e -> {
            campoDestino.setText("");
            dispose();
        });
        btnCancelar.addActionListener(e -> dispose());

        rodape.add(btnHoje);
        rodape.add(btnLimpar);
        rodape.add(btnCancelar);

        return rodape;
    }

    private void desenharDias() {
        painelDias.removeAll();

        String mes = mesAtual.getMonth().getDisplayName(TextStyle.FULL, LOCALE_BR);
        mes = mes.substring(0, 1).toUpperCase(LOCALE_BR) + mes.substring(1);
        lblMesAno.setText(mes + " " + mesAtual.getYear());

        LocalDate primeiroDia = mesAtual.atDay(1);
        int primeiroIndice = primeiroDia.getDayOfWeek().getValue() % 7;
        int totalDias = mesAtual.lengthOfMonth();

        YearMonth mesAnterior = mesAtual.minusMonths(1);
        int totalAnterior = mesAnterior.lengthOfMonth();

        for (int i = 0; i < 42; i++) {
            LocalDate data;
            boolean foraDoMes = false;

            if (i < primeiroIndice) {
                data = mesAnterior.atDay(totalAnterior - primeiroIndice + i + 1);
                foraDoMes = true;
            } else if (i >= primeiroIndice + totalDias) {
                data = mesAtual.plusMonths(1).atDay(i - primeiroIndice - totalDias + 1);
                foraDoMes = true;
            } else {
                data = mesAtual.atDay(i - primeiroIndice + 1);
            }

            painelDias.add(criarBotaoDia(data, foraDoMes));
        }

        painelDias.revalidate();
        painelDias.repaint();
    }

    private JButton criarBotaoDia(LocalDate data, boolean foraDoMes) {
        JButton btn = new JButton(String.valueOf(data.getDayOfMonth()));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setOpaque(true);

        if (data.equals(dataSelecionada)) {
            btn.setBackground(corDestaque);
            btn.setForeground(Color.WHITE);
        } else if (data.equals(LocalDate.now())) {
            btn.setBackground(corTopo);
            btn.setForeground(corHoje);
            btn.setBorder(BorderFactory.createLineBorder(corHoje, 1));
        } else {
            btn.setBackground(foraDoMes ? new Color(18, 38, 58) : corDia);
            btn.setForeground(foraDoMes ? new Color(120, 145, 165) : corTexto);
        }

        btn.setToolTipText(data.format(FORMATO_DATA));
        btn.addActionListener(e -> confirmar(data));
        return btn;
    }

    private JButton criarBotaoTopo(String texto) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(42, 34));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(corTopo);
        btn.setForeground(corTexto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setOpaque(true);
        TooltipUtils.aplicarTooltipPadrao(btn);
        return btn;
    }

    private JButton criarBotaoRodape(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(86, 34));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(true);
        TooltipUtils.aplicarTooltipPadrao(btn);
        return btn;
    }

    private void confirmar(LocalDate data) {
        campoDestino.setText(data.format(FORMATO_DATA));
        dispose();
    }

    private LocalDate lerDataCampo(JTextField campo) {
        try {
            String texto = campo.getText() == null ? "" : campo.getText().trim();
            if (!texto.isEmpty()) {
                return LocalDate.parse(texto, FORMATO_DATA);
            }
        } catch (DateTimeParseException e) {
            // Se a data digitada estiver invalida, abre no dia atual.
        }

        return LocalDate.now();
    }
}
