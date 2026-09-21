package DAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class PainelRelatorioServicos extends JPanel {

    // Cores usadas nessa tela, seguindo o mesmo visual do sistema
    private final Color COR_TITULO        = new Color(0x80CBCB);
    private final Color COR_VIDRO_CARDS   = new Color(20, 43, 66, 170);
    private final Color AZUL_ATUALIZAR    = new Color(60, 130, 180);
    private final Color ROXO_RELATORIO    = new Color(120, 90, 180);
    private final Color COR_HEADER_TAB    = new Color(30, 90, 110);
    private final Color COR_LINHA_PAR     = new Color(20, 55, 80, 180);
    private final Color COR_LINHA_IMPAR   = new Color(12, 38, 58, 160);
    private final Color COR_SEL           = new Color(80, 180, 180, 120);

    private final Color COR_DINHEIRO      = new Color(110, 210, 140);
    private final Color COR_QTD           = new Color(80, 190, 230);
    private final Color COR_TICKET        = new Color(255, 202, 40);
    private final Color COR_ANO           = new Color(171, 120, 220);

    private static final Color[] PALETA_BARRAS = {
        new Color(0, 188, 212), new Color(38, 166, 154), new Color(102, 187, 106),
        new Color(174, 213, 129), new Color(255, 202, 40), new Color(255, 152, 0),
        new Color(239, 83, 80), new Color(236, 64, 122), new Color(171, 71, 188),
        new Color(92, 107, 192), new Color(41, 182, 246), new Color(0, 230, 118)
    };

    private Font exo2SemiBold;

    private JLabel lblValorPeriodo;
    private JLabel lblQtdServicos;
    private JLabel lblTicketMedio;
    private JLabel lblValorAno;

    private JComboBox<String> comboPeriodo;
    private CampoArredondado txtPesquisa;
    private DefaultTableModel modeloTabela;
    private JTable tabela;
    private TableRowSorter<DefaultTableModel> filtroTabela;

    private GraficoBarras graficoBarras;
    private GraficoRosca graficoRosca;
    private javax.swing.Timer timerAtualizacaoAutomatica;

    private double[] dadosMensais = new double[12];
    private String[] rotulosMensais = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
    private Map<String, Double> dadosOrigem = new LinkedHashMap<>();

    public PainelRelatorioServicos() {
        carregarFonteExo();

        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setOpaque(false);

        container.add(criarCardTitulo(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOpaque(false);
        split.setDividerSize(5);
        split.setDividerLocation(395);
        split.setResizeWeight(0.50);
        split.setBorder(null);

        split.setTopComponent(criarPainelSuperior());
        split.setBottomComponent(criarPainelTabela());

        container.add(split, BorderLayout.CENTER);
        add(container, BorderLayout.CENTER);

        // Quando a tela aparecer de novo, ela busca os dados mais recentes.
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                atualizarTela();
            }
        });

        // Atualiza sozinho enquanto a tela estiver aberta.
        // Assim o usuário não precisa apertar botão para ver uma OS concluída no relatório.
        timerAtualizacaoAutomatica = new javax.swing.Timer(4000, e -> {
            if (isShowing()) {
                atualizarTela();
            }
        });
        timerAtualizacaoAutomatica.start();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                atualizarTela();
            }
        });

        atualizarTela();
    }

    // Esse metodo publico deixa a TelaPrincipal atualizar o relatorio ao clicar no menu.
    public void atualizarTela() {
        carregarDadosRelatorio();
        filtrarTabela();
    }

    private JPanel criarCardTitulo() {
        JPanel card = criarCardVidro();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitulo = new JLabel("RELATÓRIO DE SERVIÇOS");
        if (exo2SemiBold != null) lblTitulo.setFont(exo2SemiBold.deriveFont(28f));
        else lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(COR_TITULO);

        JLabel lblSub = new JLabel("  Serviços concluídos, valores arrecadados e resumo por período");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(160, 210, 210));

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        esq.setOpaque(false);
        esq.add(lblTitulo);
        esq.add(lblSub);

        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dir.setOpaque(false);

        JLabel lblPeriodo = criarLabel("PERÍODO:");
        comboPeriodo = new JComboBox<>(new String[]{"Todos", "Semana atual", "Mês atual", "Ano atual"});
        comboPeriodo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboPeriodo.setPreferredSize(new Dimension(150, 34));
        comboPeriodo.addActionListener(e -> atualizarTela());

        JButton btnImprimir = criarBotaoAcao("Imprimir", ROXO_RELATORIO, Color.WHITE);
        TooltipUtils.aplicarTooltipPadrao(btnImprimir);
        btnImprimir.setPreferredSize(new Dimension(110, 34));
        btnImprimir.addActionListener(e -> acaoImprimir());

        dir.add(lblPeriodo);
        dir.add(comboPeriodo);
        dir.add(btnImprimir);

        card.add(esq, BorderLayout.WEST);
        card.add(dir, BorderLayout.EAST);
        return card;
    }

    private JPanel criarPainelSuperior() {
        JPanel sup = new JPanel(new BorderLayout(0, 12));
        sup.setOpaque(false);

        sup.add(criarPainelKPIs(), BorderLayout.NORTH);

        JPanel painelGraficos = new JPanel(new GridLayout(1, 2, 14, 0));
        painelGraficos.setOpaque(false);

        graficoBarras = new GraficoBarras();
        graficoRosca = new GraficoRosca();

        painelGraficos.add(criarCardComTitulo("Faturamento mensal dos serviços concluídos", graficoBarras));
        painelGraficos.add(criarCardComTitulo("Origem dos serviços concluídos", graficoRosca));

        sup.add(painelGraficos, BorderLayout.CENTER);
        return sup;
    }

    private JPanel criarPainelKPIs() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setOpaque(false);

        lblValorPeriodo = new JLabel("R$ 0,00");
        lblQtdServicos = new JLabel("0");
        lblTicketMedio = new JLabel("R$ 0,00");
        lblValorAno = new JLabel("R$ 0,00");

        p.add(criarCardKPI("Valor arrecadado no período", lblValorPeriodo, COR_DINHEIRO));
        p.add(criarCardKPI("Serviços concluídos", lblQtdServicos, COR_QTD));
        p.add(criarCardKPI("Ticket médio", lblTicketMedio, COR_TICKET));
        p.add(criarCardKPI("Arrecadado no ano", lblValorAno, COR_ANO));
        return p;
    }

    private JPanel criarCardKPI(String titulo, JLabel lblValor, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 38, 60, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(corDestaque.getRed(), corDestaque.getGreen(), corDestaque.getBlue(), 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.setColor(corDestaque);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 12));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitulo.setForeground(new Color(170, 210, 210));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(corDestaque);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarCardComTitulo(String titulo, JComponent conteudo) {
        JPanel card = criarCardVidro();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(COR_TITULO);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));

        card.add(lbl, BorderLayout.NORTH);
        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarPainelTabela() {
        JPanel card = criarCardVidro();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblSecao = new JLabel("Detalhamento dos serviços concluídos");
        lblSecao.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSecao.setForeground(COR_TITULO);

        JPanel pesqPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pesqPanel.setOpaque(false);

        JLabel lblPesquisa = criarLabel("BUSCAR:");
        lblPesquisa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtPesquisa = new CampoArredondado(15);
        LimiteCampos.aplicar(txtPesquisa, 100);
        txtPesquisa.setPreferredSize(new Dimension(240, 33));
        txtPesquisa.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filtrarTabela(); }
        });

        pesqPanel.add(lblPesquisa);
        pesqPanel.add(txtPesquisa);

        barra.add(lblSecao, BorderLayout.WEST);
        barra.add(pesqPanel, BorderLayout.EAST);
        card.add(barra, BorderLayout.NORTH);

        String[] colunas = {"ID OS", "Cliente", "Equipamento", "Técnico", "Origem", "Abertura", "Conclusão", "Valor", "Descrição"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tabela = new JTable(modeloTabela) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) c.setBackground(COR_SEL);
                else c.setBackground(row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR);
                c.setForeground(Color.WHITE);
                return c;
            }
        };

        filtroTabela = new TableRowSorter<>(modeloTabela);
        tabela.setRowSorter(filtroTabela);
        for (int i = 0; i < modeloTabela.getColumnCount(); i++) filtroTabela.setSortable(i, false);

        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabela.setRowHeight(38);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(0, 2));
        tabela.setOpaque(false);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);

        personalizarCabecalhoTabela();

        DefaultTableCellRenderer cellCenter = new DefaultTableCellRenderer();
        cellCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(cellCenter);
        }

        tabela.getColumnModel().getColumn(0).setPreferredWidth(55);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(170);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(220);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void carregarDadosRelatorio() {
        if (modeloTabela == null) return;
        modeloTabela.setRowCount(0);

        double totalPeriodo = 0;
        int qtdPeriodo = 0;
        double totalAno = calcularTotalAnoAtual();
        dadosMensais = carregarDadosMensaisAnoAtual();
        dadosOrigem.clear();

        Periodo periodo = pegarPeriodoSelecionado();

        String sql =
            "SELECT s.idSERVICO, c.nome AS cliente, " +
            "       TRIM(CONCAT_WS(' ', p.tipo, p.marca, p.modelo)) AS equipamento, " +
            "       COALESCE(t.nome, 'Não informado') AS tecnico, " +
            "       s.data_abertura, s.prazo_entrega, s.data_conclusao, s.status_os, " +
            "       COALESCE(r.valor_total_orcamento, 0) AS valor_servico, " +
            "       COALESCE(r.defeito_relatado, '') AS descricao, " +
            "       CASE WHEN r.idRELATORIO_ORCAMENTO IS NULL THEN 'Manual' " +
            "            WHEN r.status_orcamento = 'Servico Manual' THEN 'Manual' " +
            "            ELSE 'Orçamento' END AS origem " +
            "FROM servico s " +
            "JOIN cliente c ON c.idCliente = s.CLIENTE_idCliente AND c.ativo = 1 " +
            "LEFT JOIN produto p ON p.idPRODUTO = s.PRODUTO_idPRODUTO " +
            "LEFT JOIN tecnico t ON t.idTECNICO = s.TECNICO_idTECNICO " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "WHERE LOWER(TRIM(s.status_os)) LIKE 'conclu%' ";

        if (periodo.usarFiltro) {
            sql += "AND s.data_conclusao BETWEEN ? AND ? ";
        }

        sql += "ORDER BY s.data_conclusao DESC, s.idSERVICO DESC";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (periodo.usarFiltro) {
                ps.setDate(1, new java.sql.Date(periodo.inicio.getTime()));
                ps.setDate(2, new java.sql.Date(periodo.fim.getTime()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                while (rs.next()) {
                    BigDecimal valorBD = rs.getBigDecimal("valor_servico");
                    double valor = valorBD == null ? 0 : valorBD.doubleValue();
                    totalPeriodo += valor;
                    qtdPeriodo++;

                    String origem = rs.getString("origem");
                    if (origem == null || origem.trim().isEmpty()) origem = "Manual";
                    dadosOrigem.put(origem, dadosOrigem.getOrDefault(origem, 0.0) + 1.0);

                    String equipamento = rs.getString("equipamento");
                    if (equipamento == null || equipamento.trim().isEmpty()) equipamento = "Sem equipamento";

                    java.sql.Date abertura = rs.getDate("data_abertura");
                    java.sql.Date conclusao = rs.getDate("data_conclusao");

                    modeloTabela.addRow(new Object[]{
                        rs.getInt("idSERVICO"),
                        rs.getString("cliente"),
                        equipamento,
                        rs.getString("tecnico"),
                        origem,
                        abertura == null ? "" : sdf.format(abertura),
                        conclusao == null ? "" : sdf.format(conclusao),
                        formatarMoeda(valor),
                        rs.getString("descricao")
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar relatório: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }

        double ticketMedio = qtdPeriodo == 0 ? 0 : totalPeriodo / qtdPeriodo;

        lblValorPeriodo.setText(formatarMoeda(totalPeriodo));
        lblQtdServicos.setText(String.valueOf(qtdPeriodo));
        lblTicketMedio.setText(formatarMoeda(ticketMedio));
        lblValorAno.setText(formatarMoeda(totalAno));

        graficoBarras.setDados(dadosMensais, rotulosMensais, PALETA_BARRAS);
        graficoRosca.setDados(dadosOrigem);
    }

    private double calcularTotalAnoAtual() {
        double total = 0;
        Calendar cal = Calendar.getInstance();
        int ano = cal.get(Calendar.YEAR);

        String sql =
            "SELECT COALESCE(SUM(COALESCE(r.valor_total_orcamento, 0)), 0) AS total " +
            "FROM servico s " +
            "JOIN cliente c ON c.idCliente = s.CLIENTE_idCliente AND c.ativo = 1 " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "WHERE LOWER(TRIM(s.status_os)) LIKE 'conclu%' " +
            "AND YEAR(s.data_conclusao) = ?";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ano);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getDouble("total");
            }
        } catch (Exception e) {
            System.out.println("Erro ao calcular total do ano: " + e.getMessage());
        }
        return total;
    }

    private double[] carregarDadosMensaisAnoAtual() {
        double[] meses = new double[12];
        Calendar cal = Calendar.getInstance();
        int ano = cal.get(Calendar.YEAR);

        String sql =
            "SELECT MONTH(s.data_conclusao) AS mes, " +
            "       COALESCE(SUM(COALESCE(r.valor_total_orcamento, 0)), 0) AS total " +
            "FROM servico s " +
            "JOIN cliente c ON c.idCliente = s.CLIENTE_idCliente AND c.ativo = 1 " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "WHERE LOWER(TRIM(s.status_os)) LIKE 'conclu%' " +
            "AND YEAR(s.data_conclusao) = ? " +
            "GROUP BY MONTH(s.data_conclusao)";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ano);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    if (mes >= 1 && mes <= 12) meses[mes - 1] = rs.getDouble("total");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar grafico mensal: " + e.getMessage());
        }
        return meses;
    }

    private Periodo pegarPeriodoSelecionado() {
        String opcao = comboPeriodo == null ? "Todos" : String.valueOf(comboPeriodo.getSelectedItem());
        Calendar inicio = Calendar.getInstance();
        Calendar fim = Calendar.getInstance();
        zerarHora(inicio);
        finalizarDia(fim);

        if ("Semana atual".equals(opcao)) {
            inicio.set(Calendar.DAY_OF_WEEK, inicio.getFirstDayOfWeek());
            zerarHora(inicio);
            return new Periodo(true, inicio.getTime(), fim.getTime());
        }

        if ("Mês atual".equals(opcao)) {
            inicio.set(Calendar.DAY_OF_MONTH, 1);
            zerarHora(inicio);
            return new Periodo(true, inicio.getTime(), fim.getTime());
        }

        if ("Ano atual".equals(opcao)) {
            inicio.set(Calendar.DAY_OF_YEAR, 1);
            zerarHora(inicio);
            return new Periodo(true, inicio.getTime(), fim.getTime());
        }

        return new Periodo(false, null, null);
    }

    private void zerarHora(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void finalizarDia(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }

    class Periodo {
        boolean usarFiltro;
        Date inicio;
        Date fim;
        Periodo(boolean usarFiltro, Date inicio, Date fim) {
            this.usarFiltro = usarFiltro;
            this.inicio = inicio;
            this.fim = fim;
        }
    }

    private String formatarMoeda(double valor) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private void filtrarTabela() {
        if (filtroTabela == null || txtPesquisa == null) return;
        String texto = txtPesquisa.getText().trim();
        if (texto.isEmpty()) {
            filtroTabela.setRowFilter(null);
            return;
        }
        filtroTabela.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }

    private void acaoImprimir() {
        try {
            boolean ok = tabela.print(JTable.PrintMode.FIT_WIDTH,
                new java.text.MessageFormat("Relatório de Serviços Concluídos"),
                new java.text.MessageFormat("Página {0}"));
            if (ok) JOptionPane.showMessageDialog(this, "Impressão finalizada com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao processar impressão: " + e.getMessage());
        }
    }

    private void personalizarCabecalhoTabela() {
        JTableHeader header = tabela.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(COR_HEADER_TAB);
        header.setForeground(COR_TITULO);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 180, 180, 80)));
        header.setOpaque(true);
        header.setDefaultRenderer((t, val, sel, focus, row, col) -> {
            JLabel label = new JLabel(val == null ? "" : val.toString());
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(COR_TITULO);
            label.setBackground(COR_HEADER_TAB);
            label.setBorder(new EmptyBorder(0, 8, 0, 8));
            return label;
        });
    }

    class GraficoBarras extends JPanel {
        private double[] valores;
        private String[] rotulos;
        private Color[] cores;

        GraficoBarras() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 220));
        }

        void setDados(double[] valores, String[] rotulos, Color[] cores) {
            this.valores = valores == null ? new double[12] : valores.clone();
            this.rotulos = rotulos;
            this.cores = cores;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (valores == null || valores.length == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padLeft = 58, padRight = 12, padTop = 24, padBottom = 38;
            int areaW = w - padLeft - padRight;
            int areaH = h - padTop - padBottom;

            double maxVal = 0;
            for (double v : valores) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int nLinhas = 4;
            for (int i = 0; i <= nLinhas; i++) {
                int y = padTop + (int) (areaH * (double) i / nLinhas);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(padLeft, y, padLeft + areaW, y);

                double val = maxVal * (nLinhas - i) / nLinhas;
                String lbl = val >= 1000 ? String.format("%.0fk", val / 1000) : String.format("%.0f", val);
                g2.setColor(new Color(160, 210, 210));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, padLeft - fm.stringWidth(lbl) - 4, y + 4);
            }

            int n = valores.length;
            int barW = Math.max(6, (areaW / n) - 8);
            int gap = Math.max(2, (areaW - barW * n) / (n + 1));

            for (int i = 0; i < n; i++) {
                if (valores[i] == 0) {
                    desenharRotuloMes(g2, i, padLeft, padTop, areaH, barW, gap);
                    continue;
                }

                int barH = (int) (areaH * valores[i] / maxVal);
                int x = padLeft + gap + i * (barW + gap);
                int y = padTop + areaH - barH;
                Color cor = (cores != null && i < cores.length) ? cores[i] : new Color(0, 188, 212);

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(x + 2, y + 2, barW, barH, 6, 6);

                GradientPaint gp = new GradientPaint(x, y, cor, x, y + barH, cor.darker().darker());
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(x + 1, y, barW - 2, Math.min(8, barH / 3), 6, 6);

                desenharRotuloMes(g2, i, padLeft, padTop, areaH, barW, gap);

                if (barH > 20) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    FontMetrics fm = g2.getFontMetrics();
                    String val = valores[i] >= 1000 ? String.format("%.0fk", valores[i] / 1000) : String.format("%.0f", valores[i]);
                    g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 4);
                }
            }

            g2.setColor(new Color(0, 200, 200, 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(padLeft, padTop + areaH, padLeft + areaW, padTop + areaH);
            g2.dispose();
        }

        private void desenharRotuloMes(Graphics2D g2, int i, int padLeft, int padTop, int areaH, int barW, int gap) {
            int x = padLeft + gap + i * (barW + gap);
            g2.setColor(new Color(160, 210, 210));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            FontMetrics fm = g2.getFontMetrics();
            String rot = (rotulos != null && i < rotulos.length) ? rotulos[i] : String.valueOf(i + 1);
            g2.drawString(rot, x + (barW - fm.stringWidth(rot)) / 2, padTop + areaH + 14);
        }
    }

    class GraficoRosca extends JPanel {
        private Map<String, Double> dados;
        private static final Color[] CORES_ROSCA = {
            new Color(110, 210, 140), new Color(80, 190, 230),
            new Color(255, 202, 40), new Color(255, 100, 100),
            new Color(171, 71, 188)
        };

        GraficoRosca() { setOpaque(false); }

        void setDados(Map<String, Double> dados) {
            this.dados = dados;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dados == null || dados.isEmpty()) {
                desenharSemDados(g);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int legendaW = 150;
            int diam = Math.min(w - legendaW - 20, h - 20);
            diam = Math.max(diam, 60);
            int x = (w - diam - legendaW) / 2;
            int y = (h - diam) / 2;

            double total = 0;
            for (Double v : dados.values()) total += v;
            if (total == 0) { g2.dispose(); return; }

            double angulo = -90;
            int i = 0;
            String[] keys = dados.keySet().toArray(new String[0]);

            for (String key : keys) {
                double pct = dados.get(key) / total;
                double graus = pct * 360;
                Color cor = CORES_ROSCA[i % CORES_ROSCA.length];

                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new Arc2D.Double(x + 2, y + 2, diam, diam, angulo, graus, Arc2D.PIE));
                g2.setColor(cor);
                g2.fill(new Arc2D.Double(x, y, diam, diam, angulo, graus, Arc2D.PIE));
                g2.setColor(new Color(10, 25, 45));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Arc2D.Double(x, y, diam, diam, angulo, graus, Arc2D.PIE));

                angulo += graus;
                i++;
            }

            int espessura = Math.max(20, diam / 4);
            int buraco = diam - espessura * 2;
            int bx = x + espessura, by = y + espessura;

            g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(bx + buraco / 2f, by + buraco / 2f),
                buraco / 2f,
                new float[]{0f, 1f},
                new Color[]{new Color(18, 45, 70), new Color(12, 30, 52)}
            ));
            g2.fillOval(bx, by, buraco, buraco);

            g2.setColor(COR_TITULO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            String totalStr = String.valueOf((int) total);
            g2.drawString(totalStr, bx + (buraco - fm.stringWidth(totalStr)) / 2, by + buraco / 2 + 6);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(160, 210, 210));
            fm = g2.getFontMetrics();
            String subStr = "serviços";
            g2.drawString(subStr, bx + (buraco - fm.stringWidth(subStr)) / 2, by + buraco / 2 + 20);

            int lx = x + diam + 16;
            int ly = y + 10;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            i = 0;
            for (String key : keys) {
                Color cor = CORES_ROSCA[i % CORES_ROSCA.length];
                double pct = dados.get(key) / total * 100;
                g2.setColor(cor);
                g2.fillRoundRect(lx, ly + i * 24, 11, 11, 4, 4);
                g2.setColor(new Color(200, 230, 230));
                g2.drawString(String.format("%s (%.0f%%)", key, pct), lx + 15, ly + i * 24 + 10);
                i++;
            }

            g2.dispose();
        }

        private void desenharSemDados(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(160, 210, 210));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String msg = "Nenhum serviço concluído no período";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            g2.dispose();
        }
    }

    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    private JButton criarBotaoAcao(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 18);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return b;
    }

    private void carregarFonteExo() {
        try {
            InputStream is = getClass().getResourceAsStream("/imagens/Exo2-SemiBold.ttf");
            if (is != null) exo2SemiBold = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            exo2SemiBold = new Font("SansSerif", Font.BOLD, 14);
        }
    }

    private JPanel criarCardVidro() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_VIDRO_CARDS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    class BotaoArredondado extends JButton {
        private Color corFundo, corTexto;
        private boolean hover;
        private int arco;

        public BotaoArredondado(String texto, Color corFundo, Color corTexto, int arco) {
            super(texto);
            this.corFundo = corFundo;
            this.corTexto = corTexto;
            this.arco = arco;
            setForeground(corTexto);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color corAtual = hover ? clarearCor(corFundo, 25) : corFundo;
            g2.setColor(new Color(corAtual.getRed(), corAtual.getGreen(), corAtual.getBlue(), hover ? 235 : 210));
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
            g2.setColor(new Color(0, 200, 200, 170));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
            g2.setFont(getFont());
            g2.setColor(corTexto);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }

        private Color clarearCor(Color c, int v) {
            return new Color(Math.min(255, c.getRed() + v), Math.min(255, c.getGreen() + v), Math.min(255, c.getBlue() + v));
        }
    }

    class CampoArredondado extends JTextField {
        private Color corFundo = Color.WHITE, corBorda = new Color(0, 200, 200, 150);
        private int arco = 14;

        public CampoArredondado(int cols) { super(cols); configurar(); }
        public CampoArredondado() { super(); configurar(); }

        private void configurar() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setBackground(corFundo);
            setSelectionColor(new Color(80, 180, 180));
            setSelectedTextColor(Color.WHITE);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corFundo);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
            g2.dispose();
        }
    }
}
