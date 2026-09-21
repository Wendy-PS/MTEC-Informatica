package DAO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PainelDashboard extends JPanel {

    private static final Color COR_CARD = new Color(15, 42, 66, 216);
    private static final Color COR_CARD_2 = new Color(18, 56, 84, 194);
    private static final Color COR_CIANO = new Color(0, 213, 224);
    private static final Color COR_CIANO_SUAVE = new Color(128, 203, 203);
    private static final Color COR_TEXTO = Color.WHITE;
    private static final Color COR_TEXTO_SUAVE = new Color(184, 218, 222);
    private static final Color COR_GRID = new Color(130, 220, 225, 38);
    private static final Color COR_VERDE = new Color(91, 214, 151);
    private static final Color COR_AMARELO = new Color(255, 203, 74);
    private static final Color COR_ROXO = new Color(171, 123, 255);
    private static final Color COR_VERMELHO = new Color(255, 111, 105);
    private static final Color COR_AZUL = new Color(77, 166, 255);

    private static final Color[] PALETA = {
        COR_CIANO, COR_VERDE, COR_AMARELO, COR_ROXO, COR_VERMELHO,
        new Color(0, 160, 220), new Color(255, 149, 84), new Color(70, 220, 190)
    };

    private Font exo2SemiBold;
    private JComboBox<String> comboPeriodo;
    private JLabel lblAtualizacao;
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private Timer timerAtualizacao;
    private CardLayout cardDashboards;
    private JPanel painelDashboards;
    private DashboardPage paginaAtual = DashboardPage.GERAL;
    private final List<DashboardNavButton> botoesDashboard = new ArrayList<>();

    private MetricCard cardOsAbertas;
    private MetricCard cardConcluidas;
    private MetricCard cardClientes;
    private MetricCard cardReceita;
    private MetricCard cardTicket;
    private LineChartPanel graficoServicosMes;
    private DonutChartPanel graficoStatusOs;
    private BarChartPanel graficoOrcamentos;
    private HorizontalBarChartPanel graficoTecnicos;

    private MetricCard cardClientesTotal;
    private MetricCard cardClientesAtivos;
    private MetricCard cardClientesInativos;
    private MetricCard cardClientesProdutos;
    private MetricCard cardClientesServicos;
    private DonutChartPanel graficoClientesStatus;
    private HorizontalBarChartPanel graficoProdutosPorCliente;
    private HorizontalBarChartPanel graficoServicosPorCliente;
    private BarChartPanel graficoOrcamentosPorCliente;

    private MetricCard cardProdutosTotal;
    private MetricCard cardProdutosTipos;
    private MetricCard cardProdutosMarcas;
    private MetricCard cardProdutosClientes;
    private MetricCard cardProdutosSemServico;
    private DonutChartPanel graficoProdutosTipo;
    private BarChartPanel graficoProdutosMarca;
    private HorizontalBarChartPanel graficoProdutosCliente;
    private BarChartPanel graficoOsPorTipoProduto;

    private MetricCard cardTecnicosTotal;
    private MetricCard cardTecnicosAtivos;
    private MetricCard cardTecnicosInativos;
    private MetricCard cardTecnicosEspecialidades;
    private MetricCard cardTecnicosOs;
    private DonutChartPanel graficoTecnicosStatus;
    private DonutChartPanel graficoTecnicosEspecialidade;
    private BarChartPanel graficoOsPorTecnico;
    private HorizontalBarChartPanel graficoReceitaPorTecnico;

    private MetricCard cardOrcamentosTotal;
    private MetricCard cardOrcamentosAprovados;
    private MetricCard cardOrcamentosManuais;
    private MetricCard cardOrcamentosValor;
    private MetricCard cardOrcamentosTicket;
    private DonutChartPanel graficoOrcStatus;
    private LineChartPanel graficoOrcMes;
    private BarChartPanel graficoValorPorStatus;
    private HorizontalBarChartPanel graficoOrcPorCliente;

    private MetricCard cardServicosTotal;
    private MetricCard cardServicosAbertos;
    private MetricCard cardServicosConcluidos;
    private MetricCard cardServicosVencidos;
    private MetricCard cardServicosFaturamento;
    private LineChartPanel graficoServMesDetalhado;
    private DonutChartPanel graficoServStatusDetalhado;
    private HorizontalBarChartPanel graficoServPorTecnicoDetalhado;
    private HorizontalBarChartPanel graficoServPorClienteDetalhado;

    public PainelDashboard() {
        carregarFonteExo();
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(14, 0, 0, 0));

        montarTela();
        selecionarDashboard(DashboardPage.GERAL);
        atualizarDashboard();
        iniciarAtualizacaoAutomatica();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                atualizarDashboard();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                atualizarDashboard();
            }
        });
    }

    private void montarTela() {
        JPanel container = criarPainelPrincipal();
        container.setLayout(new BorderLayout(0, 12));
        container.setBorder(new EmptyBorder(16, 18, 16, 18));

        container.add(criarCabecalho(), BorderLayout.NORTH);

        cardDashboards = new CardLayout();
        painelDashboards = new JPanel(cardDashboards);
        painelDashboards.setOpaque(false);
        painelDashboards.add(criarDashboardGeral(), DashboardPage.GERAL.name());
        painelDashboards.add(criarDashboardClientes(), DashboardPage.CLIENTES.name());
        painelDashboards.add(criarDashboardProdutos(), DashboardPage.PRODUTOS.name());
        painelDashboards.add(criarDashboardTecnicos(), DashboardPage.TECNICOS.name());
        painelDashboards.add(criarDashboardOrcamentos(), DashboardPage.ORCAMENTOS.name());
        painelDashboards.add(criarDashboardServicos(), DashboardPage.SERVICOS.name());

        container.add(painelDashboards, BorderLayout.CENTER);
        add(container, BorderLayout.CENTER);
    }

    private JPanel criarCabecalho() {
        JPanel header = criarCardVidro(20, 215);
        header.setLayout(new BorderLayout(0, 10));
        header.setBorder(new EmptyBorder(10, 16, 11, 14));
        header.setPreferredSize(new Dimension(0, 112));

        JPanel topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);

        JPanel textos = new JPanel(new BorderLayout(0, 1));
        textos.setOpaque(false);

        lblTitulo = new JLabel();
        lblTitulo.setForeground(COR_CIANO);
        lblTitulo.setFont(fonteTitulo(26f));

        lblSubtitulo = new JLabel();
        lblSubtitulo.setForeground(COR_TEXTO_SUAVE);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        textos.add(lblTitulo, BorderLayout.NORTH);
        textos.add(lblSubtitulo, BorderLayout.CENTER);

        JPanel acoes = new JPanel(new GridBagLayout());
        acoes.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 6, 0, 0);
        g.gridy = 0;

        lblAtualizacao = new JLabel("Atualizando...");
        lblAtualizacao.setForeground(new Color(162, 210, 214));
        lblAtualizacao.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        comboPeriodo = new JComboBox<>(new String[]{"Mes atual", "Ano atual", "Todos"});
        comboPeriodo.setFocusable(false);
        comboPeriodo.setPreferredSize(new Dimension(122, 32));
        comboPeriodo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        comboPeriodo.addActionListener(e -> atualizarDashboard());

        g.gridx = 0;
        acoes.add(lblAtualizacao, g);
        g.gridx = 1;
        acoes.add(comboPeriodo, g);

        topo.add(textos, BorderLayout.CENTER);
        topo.add(acoes, BorderLayout.EAST);

        JPanel navegacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navegacao.setOpaque(false);
        for (DashboardPage page : DashboardPage.values()) {
            DashboardNavButton btn = new DashboardNavButton(page);
            btn.addActionListener(e -> selecionarDashboard(page));
            botoesDashboard.add(btn);
            navegacao.add(btn);
        }

        header.add(topo, BorderLayout.CENTER);
        header.add(navegacao, BorderLayout.SOUTH);
        return header;
    }

    private JPanel criarDashboardGeral() {
        JPanel pagina = criarPaginaBase();
        pagina.add(criarCardsResumo(
            cardOsAbertas = new MetricCard("OS abertas", "0", "em andamento", COR_CIANO),
            cardConcluidas = new MetricCard("Concluídas", "0", "no período", COR_VERDE),
            cardClientes = new MetricCard("Clientes ativos", "0", "base atual", COR_AZUL),
            cardReceita = new MetricCard("Faturamento", "R$ 0,00", "no período", COR_AMARELO),
            cardTicket = new MetricCard("Ticket médio", "R$ 0,00", "por OS concluída", COR_ROXO)
        ), BorderLayout.NORTH);

        graficoServicosMes = new LineChartPanel("Chamados por mês", "aberturas no ano");
        graficoStatusOs = new DonutChartPanel("Status das OS", "distribuição atual");
        graficoOrcamentos = new BarChartPanel("Orçamentos por status", "quantidade");
        graficoTecnicos = new HorizontalBarChartPanel("OS por técnico", "top técnicos");
        pagina.add(criarAreaGraficos(graficoServicosMes, graficoStatusOs, graficoOrcamentos, graficoTecnicos), BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarDashboardClientes() {
        JPanel pagina = criarPaginaBase();
        pagina.add(criarCardsResumo(
            cardClientesTotal = new MetricCard("Total clientes", "0", "ativos + inativos", COR_CIANO),
            cardClientesAtivos = new MetricCard("Clientes ativos", "0", "em atendimento", COR_VERDE),
            cardClientesInativos = new MetricCard("Clientes inativos", "0", "arquivo morto", COR_VERMELHO),
            cardClientesProdutos = new MetricCard("Equipamentos", "0", "vinculados", COR_AZUL),
            cardClientesServicos = new MetricCard("Clientes com OS", "0", "com histórico", COR_ROXO)
        ), BorderLayout.NORTH);

        graficoClientesStatus = new DonutChartPanel("Clientes por status", "ativos e inativos");
        graficoProdutosPorCliente = new HorizontalBarChartPanel("Equipamentos por cliente", "maiores carteiras");
        graficoServicosPorCliente = new HorizontalBarChartPanel("OS por cliente", "mais atendidos");
        graficoOrcamentosPorCliente = new BarChartPanel("Orçamentos por cliente", "quantidade");
        pagina.add(criarAreaGraficos(graficoClientesStatus, graficoProdutosPorCliente, graficoServicosPorCliente, graficoOrcamentosPorCliente), BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarDashboardProdutos() {
        JPanel pagina = criarPaginaBase();
        pagina.add(criarCardsResumo(
            cardProdutosTotal = new MetricCard("Equipamentos", "0", "cadastrados", COR_CIANO),
            cardProdutosTipos = new MetricCard("Tipos", "0", "categorias", COR_VERDE),
            cardProdutosMarcas = new MetricCard("Marcas", "0", "fabricantes", COR_AMARELO),
            cardProdutosClientes = new MetricCard("Clientes", "0", "com equipamento", COR_AZUL),
            cardProdutosSemServico = new MetricCard("Sem OS", "0", "sem atendimento", COR_ROXO)
        ), BorderLayout.NORTH);

        graficoProdutosTipo = new DonutChartPanel("Equipamentos por tipo", "distribuição");
        graficoProdutosMarca = new BarChartPanel("Equipamentos por marca", "top marcas");
        graficoProdutosCliente = new HorizontalBarChartPanel("Equipamentos por cliente", "top clientes");
        graficoOsPorTipoProduto = new BarChartPanel("OS por tipo de equipamento", "demanda");
        pagina.add(criarAreaGraficos(graficoProdutosTipo, graficoProdutosMarca, graficoProdutosCliente, graficoOsPorTipoProduto), BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarDashboardTecnicos() {
        JPanel pagina = criarPaginaBase();
        pagina.add(criarCardsResumo(
            cardTecnicosTotal = new MetricCard("Técnicos", "0", "cadastrados", COR_CIANO),
            cardTecnicosAtivos = new MetricCard("Ativos", "0", "disponíveis", COR_VERDE),
            cardTecnicosInativos = new MetricCard("Inativos", "0", "fora da operação", COR_VERMELHO),
            cardTecnicosEspecialidades = new MetricCard("Especialidades", "0", "áreas", COR_AMARELO),
            cardTecnicosOs = new MetricCard("OS atribuídas", "0", "com técnico", COR_AZUL)
        ), BorderLayout.NORTH);

        graficoTecnicosStatus = new DonutChartPanel("Técnicos por status", "situação atual");
        graficoTecnicosEspecialidade = new DonutChartPanel("Especialidades", "distribuição");
        graficoOsPorTecnico = new BarChartPanel("OS por técnico", "quantidade");
        graficoReceitaPorTecnico = new HorizontalBarChartPanel("Faturamento por técnico", "OS concluídas");
        pagina.add(criarAreaGraficos(graficoTecnicosStatus, graficoTecnicosEspecialidade, graficoOsPorTecnico, graficoReceitaPorTecnico), BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarDashboardOrcamentos() {
        JPanel pagina = criarPaginaBase();
        pagina.add(criarCardsResumo(
            cardOrcamentosTotal = new MetricCard("Orçamentos", "0", "no período", COR_CIANO),
            cardOrcamentosAprovados = new MetricCard("Aprovados", "0", "convertidos", COR_VERDE),
            cardOrcamentosManuais = new MetricCard("Manuais", "0", "serviços diretos", COR_AZUL),
            cardOrcamentosValor = new MetricCard("Valor total", "R$ 0,00", "no período", COR_AMARELO),
            cardOrcamentosTicket = new MetricCard("Ticket médio", "R$ 0,00", "por orçamento", COR_ROXO)
        ), BorderLayout.NORTH);

        graficoOrcStatus = new DonutChartPanel("Orçamentos por status", "distribuição");
        graficoOrcMes = new LineChartPanel("Orçamentos por mês", "aberturas no ano");
        graficoValorPorStatus = new BarChartPanel("Valor por status", "soma em reais");
        graficoOrcPorCliente = new HorizontalBarChartPanel("Orçamentos por cliente", "top clientes");
        pagina.add(criarAreaGraficos(graficoOrcStatus, graficoOrcMes, graficoValorPorStatus, graficoOrcPorCliente), BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarDashboardServicos() {
        JPanel pagina = criarPaginaBase();
        pagina.add(criarCardsResumo(
            cardServicosTotal = new MetricCard("Ordens de serviço", "0", "no período", COR_CIANO),
            cardServicosAbertos = new MetricCard("Em andamento", "0", "pendentes", COR_AMARELO),
            cardServicosConcluidos = new MetricCard("Concluídas", "0", "no período", COR_VERDE),
            cardServicosVencidos = new MetricCard("Vencidas", "0", "fora do prazo", COR_VERMELHO),
            cardServicosFaturamento = new MetricCard("Faturamento", "R$ 0,00", "concluídas", COR_ROXO)
        ), BorderLayout.NORTH);

        graficoServMesDetalhado = new LineChartPanel("Serviços por mês", "aberturas no ano");
        graficoServStatusDetalhado = new DonutChartPanel("Status das OS", "distribuição");
        graficoServPorTecnicoDetalhado = new HorizontalBarChartPanel("Serviços por técnico", "top técnicos");
        graficoServPorClienteDetalhado = new HorizontalBarChartPanel("Serviços por cliente", "top clientes");
        pagina.add(criarAreaGraficos(graficoServMesDetalhado, graficoServStatusDetalhado, graficoServPorTecnicoDetalhado, graficoServPorClienteDetalhado), BorderLayout.CENTER);
        return pagina;
    }

    private JPanel criarPaginaBase() {
        JPanel pagina = new JPanel(new BorderLayout(0, 12));
        pagina.setOpaque(false);
        return pagina;
    }

    private JPanel criarCardsResumo(MetricCard... cards) {
        JPanel painel = new JPanel(new GridLayout(1, cards.length, 12, 0));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(0, 96));
        for (MetricCard card : cards) {
            painel.add(card);
        }
        return painel;
    }

    private JPanel criarAreaGraficos(Component primeiro, Component segundo, Component terceiro, Component quarto) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 0.55;
        g.insets = new Insets(0, 0, 12, 12);

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0.62;
        grid.add(primeiro, g);

        g.gridx = 1;
        g.weightx = 0.38;
        g.insets = new Insets(0, 0, 12, 0);
        grid.add(segundo, g);

        g.gridy = 1;
        g.gridx = 0;
        g.weighty = 0.45;
        g.weightx = 0.50;
        g.insets = new Insets(0, 0, 0, 12);
        grid.add(terceiro, g);

        g.gridx = 1;
        g.weightx = 0.50;
        g.insets = new Insets(0, 0, 0, 0);
        grid.add(quarto, g);

        return grid;
    }

    private void selecionarDashboard(DashboardPage page) {
        paginaAtual = page;
        if (cardDashboards != null) {
            cardDashboards.show(painelDashboards, page.name());
        }
        if (lblTitulo != null) lblTitulo.setText(page.titulo);
        if (lblSubtitulo != null) lblSubtitulo.setText(page.subtitulo);
        for (DashboardNavButton btn : botoesDashboard) {
            btn.setSelecionado(btn.getPage() == page);
        }
        atualizarDashboard();
    }

    public void atualizarDashboard() {
        DashboardData dados = carregarDados();

        cardOsAbertas.setValores(String.valueOf(dados.servicosAbertos), "aguardando ou em andamento");
        cardConcluidas.setValores(String.valueOf(dados.servicosConcluidos), detalhePeriodo());
        cardClientes.setValores(String.valueOf(dados.clientesAtivos), dados.produtosTotal + " equipamentos cadastrados");
        cardReceita.setValores(formatarMoeda(dados.faturamentoServicos), detalhePeriodo());
        cardTicket.setValores(formatarMoeda(dados.ticketServico), "média das concluídas");
        graficoServicosMes.setDados(dados.servicosPorMes);
        graficoStatusOs.setDados(dados.statusOs);
        graficoOrcamentos.setDados(dados.orcamentosPorStatus);
        graficoTecnicos.setDados(dados.servicosPorTecnico);

        cardClientesTotal.setValores(String.valueOf(dados.clientesTotal), "base completa");
        cardClientesAtivos.setValores(String.valueOf(dados.clientesAtivos), "podem receber OS");
        cardClientesInativos.setValores(String.valueOf(dados.clientesInativos), "arquivados");
        cardClientesProdutos.setValores(String.valueOf(dados.produtosTotal), "equipamentos cadastrados");
        cardClientesServicos.setValores(String.valueOf(dados.clientesComServico), "com histórico");
        graficoClientesStatus.setDados(dados.clientesPorStatus);
        graficoProdutosPorCliente.setDados(dados.produtosPorCliente);
        graficoServicosPorCliente.setDados(dados.servicosPorCliente);
        graficoOrcamentosPorCliente.setDados(dados.orcamentosPorCliente);

        cardProdutosTotal.setValores(String.valueOf(dados.produtosTotal), "equipamentos cadastrados");
        cardProdutosTipos.setValores(String.valueOf(dados.produtosTipos), "tipos diferentes");
        cardProdutosMarcas.setValores(String.valueOf(dados.produtosMarcas), "marcas diferentes");
        cardProdutosClientes.setValores(String.valueOf(dados.clientesComProduto), "clientes atendidos");
        cardProdutosSemServico.setValores(String.valueOf(dados.produtosSemServico), "sem OS vinculada");
        graficoProdutosTipo.setDados(dados.produtosPorTipo);
        graficoProdutosMarca.setDados(dados.produtosPorMarca);
        graficoProdutosCliente.setDados(dados.produtosPorCliente);
        graficoOsPorTipoProduto.setDados(dados.osPorTipoProduto);

        cardTecnicosTotal.setValores(String.valueOf(dados.tecnicosTotal), "equipe cadastrada");
        cardTecnicosAtivos.setValores(String.valueOf(dados.tecnicosAtivos), "status ativo");
        cardTecnicosInativos.setValores(String.valueOf(dados.tecnicosInativos), "status diferente");
        cardTecnicosEspecialidades.setValores(String.valueOf(dados.tecnicosEspecialidades), "áreas técnicas");
        cardTecnicosOs.setValores(String.valueOf(dados.osComTecnico), "ordens atribuídas");
        graficoTecnicosStatus.setDados(dados.tecnicosPorStatus);
        graficoTecnicosEspecialidade.setDados(dados.tecnicosPorEspecialidade);
        graficoOsPorTecnico.setDados(dados.servicosPorTecnico);
        graficoReceitaPorTecnico.setDados(dados.receitaPorTecnico);

        cardOrcamentosTotal.setValores(String.valueOf(dados.orcamentosTotal), detalhePeriodo());
        cardOrcamentosAprovados.setValores(String.valueOf(dados.orcamentosAprovados), "status aprovado");
        cardOrcamentosManuais.setValores(String.valueOf(dados.orcamentosManuais), "serviço manual");
        cardOrcamentosValor.setValores(formatarMoeda(dados.valorOrcamentos), detalhePeriodo());
        cardOrcamentosTicket.setValores(formatarMoeda(dados.ticketOrcamento), "média por orçamento");
        graficoOrcStatus.setDados(dados.orcamentosPorStatus);
        graficoOrcMes.setDados(dados.orcamentosPorMes);
        graficoValorPorStatus.setDados(dados.valorPorStatusOrcamento);
        graficoOrcPorCliente.setDados(dados.orcamentosPorCliente);

        cardServicosTotal.setValores(String.valueOf(dados.servicosTotalPeriodo), detalhePeriodo());
        cardServicosAbertos.setValores(String.valueOf(dados.servicosAbertos), "aguardando ou em andamento");
        cardServicosConcluidos.setValores(String.valueOf(dados.servicosConcluidos), detalhePeriodo());
        cardServicosVencidos.setValores(String.valueOf(dados.servicosVencidos), "prazo vencido");
        cardServicosFaturamento.setValores(formatarMoeda(dados.faturamentoServicos), "serviços concluídos");
        graficoServMesDetalhado.setDados(dados.servicosPorMes);
        graficoServStatusDetalhado.setDados(dados.statusOs);
        graficoServPorTecnicoDetalhado.setDados(dados.servicosPorTecnico);
        graficoServPorClienteDetalhado.setDados(dados.servicosPorCliente);

        if (lblAtualizacao != null) {
            lblAtualizacao.setText("Atualizado " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }
        repaint();
    }

    private DashboardData carregarDados() {
        DashboardData dados = new DashboardData();
        Periodo periodo = periodoSelecionado();

        dados.clientesTotal = contar("SELECT COUNT(*) FROM cliente");
        dados.clientesAtivos = contar("SELECT COUNT(*) FROM cliente WHERE COALESCE(ativo, 1) = 1");
        dados.clientesInativos = contar("SELECT COUNT(*) FROM cliente WHERE COALESCE(ativo, 1) = 0");
        dados.clientesComServico = contar("SELECT COUNT(DISTINCT CLIENTE_idCliente) FROM servico");
        dados.clientesComProduto = contar("SELECT COUNT(DISTINCT CLIENTE_idCliente) FROM produto");
        dados.clientesPorStatus = new ArrayList<>();
        dados.clientesPorStatus.add(new ChartItem("Ativos", dados.clientesAtivos, COR_VERDE));
        dados.clientesPorStatus.add(new ChartItem("Inativos", dados.clientesInativos, COR_VERMELHO));
        dados.produtosPorCliente = carregarLista(
            "SELECT c.nome AS rotulo, COUNT(p.idPRODUTO) AS total " +
            "FROM cliente c LEFT JOIN produto p ON p.CLIENTE_idCliente = c.idCliente " +
            "GROUP BY c.idCliente, c.nome ORDER BY total DESC, c.nome LIMIT 6",
            true
        );
        dados.servicosPorCliente = carregarLista(
            "SELECT c.nome AS rotulo, COUNT(s.idSERVICO) AS total " +
            "FROM cliente c LEFT JOIN servico s ON s.CLIENTE_idCliente = c.idCliente " +
            "GROUP BY c.idCliente, c.nome ORDER BY total DESC, c.nome LIMIT 6",
            true
        );
        dados.orcamentosPorCliente = carregarLista(
            "SELECT c.nome AS rotulo, COUNT(r.idRELATORIO_ORCAMENTO) AS total " +
            "FROM cliente c LEFT JOIN relatorio_orcamento r ON r.CLIENTE_idCliente = c.idCliente " +
            "GROUP BY c.idCliente, c.nome ORDER BY total DESC, c.nome LIMIT 6",
            true
        );

        dados.produtosTotal = contar("SELECT COUNT(*) FROM produto");
        dados.produtosTipos = contar("SELECT COUNT(DISTINCT COALESCE(NULLIF(TRIM(tipo), ''), 'Sem tipo')) FROM produto");
        dados.produtosMarcas = contar("SELECT COUNT(DISTINCT COALESCE(NULLIF(TRIM(marca), ''), 'Sem marca')) FROM produto");
        dados.produtosSemServico = contar(
            "SELECT COUNT(*) FROM produto p " +
            "LEFT JOIN servico s ON s.PRODUTO_idPRODUTO = p.idPRODUTO " +
            "WHERE s.idSERVICO IS NULL"
        );
        dados.produtosPorTipo = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(tipo), ''), 'Sem tipo') AS rotulo, COUNT(*) AS total " +
            "FROM produto GROUP BY COALESCE(NULLIF(TRIM(tipo), ''), 'Sem tipo') " +
            "ORDER BY total DESC",
            true
        );
        dados.produtosPorMarca = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(marca), ''), 'Sem marca') AS rotulo, COUNT(*) AS total " +
            "FROM produto GROUP BY COALESCE(NULLIF(TRIM(marca), ''), 'Sem marca') " +
            "ORDER BY total DESC LIMIT 8",
            true
        );
        dados.osPorTipoProduto = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(p.tipo), ''), 'Sem tipo') AS rotulo, COUNT(s.idSERVICO) AS total " +
            "FROM servico s LEFT JOIN produto p ON p.idPRODUTO = s.PRODUTO_idPRODUTO " +
            "GROUP BY COALESCE(NULLIF(TRIM(p.tipo), ''), 'Sem tipo') " +
            "ORDER BY total DESC LIMIT 8",
            true
        );

        dados.tecnicosTotal = contar("SELECT COUNT(*) FROM tecnico");
        dados.tecnicosAtivos = contar("SELECT COUNT(*) FROM tecnico WHERE LOWER(TRIM(status)) = 'ativo'");
        dados.tecnicosInativos = contar("SELECT COUNT(*) FROM tecnico WHERE LOWER(TRIM(COALESCE(status, ''))) <> 'ativo'");
        dados.tecnicosEspecialidades = contar("SELECT COUNT(DISTINCT COALESCE(NULLIF(TRIM(especialidade), ''), 'Não informada')) FROM tecnico");
        dados.osComTecnico = contar("SELECT COUNT(*) FROM servico WHERE TECNICO_idTECNICO IS NOT NULL");
        dados.tecnicosPorStatus = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(status), ''), 'Sem status') AS rotulo, COUNT(*) AS total " +
            "FROM tecnico GROUP BY COALESCE(NULLIF(TRIM(status), ''), 'Sem status') " +
            "ORDER BY total DESC",
            true
        );
        dados.tecnicosPorEspecialidade = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(especialidade), ''), 'Não informada') AS rotulo, COUNT(*) AS total " +
            "FROM tecnico GROUP BY COALESCE(NULLIF(TRIM(especialidade), ''), 'Não informada') " +
            "ORDER BY total DESC",
            true
        );
        dados.servicosPorTecnico = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(t.nome), ''), 'Sem técnico') AS rotulo, COUNT(s.idSERVICO) AS total " +
            "FROM servico s LEFT JOIN tecnico t ON t.idTECNICO = s.TECNICO_idTECNICO " +
            "GROUP BY COALESCE(NULLIF(TRIM(t.nome), ''), 'Sem técnico') " +
            "ORDER BY total DESC LIMIT 6",
            true
        );
        dados.receitaPorTecnico = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(t.nome), ''), 'Sem técnico') AS rotulo, " +
            "COALESCE(SUM(COALESCE(r.valor_total_orcamento, 0)), 0) AS total " +
            "FROM servico s " +
            "LEFT JOIN tecnico t ON t.idTECNICO = s.TECNICO_idTECNICO " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "WHERE LOWER(TRIM(s.status_os)) LIKE 'conclu%' " +
            "GROUP BY COALESCE(NULLIF(TRIM(t.nome), ''), 'Sem técnico') " +
            "ORDER BY total DESC LIMIT 6",
            true
        );

        dados.servicosAbertos = contar(
            "SELECT COUNT(*) FROM servico WHERE LOWER(TRIM(status_os)) NOT LIKE 'conclu%'"
        );
        dados.servicosTotalPeriodo = contar(
            "SELECT COUNT(*) FROM servico s WHERE 1 = 1 " + periodo.whereSql("s.data_abertura")
        );
        dados.servicosConcluidos = contar(
            "SELECT COUNT(*) FROM servico s " +
            "WHERE LOWER(TRIM(s.status_os)) LIKE 'conclu%' " + periodo.whereSql("s.data_conclusao")
        );
        dados.servicosVencidos = contar(
            "SELECT COUNT(*) FROM servico " +
            "WHERE LOWER(TRIM(status_os)) NOT LIKE 'conclu%' " +
            "AND prazo_entrega IS NOT NULL AND prazo_entrega < CURRENT_DATE"
        );
        dados.faturamentoServicos = somar(
            "SELECT COALESCE(SUM(COALESCE(r.valor_total_orcamento, 0)), 0) " +
            "FROM servico s " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "WHERE LOWER(TRIM(s.status_os)) LIKE 'conclu%' " + periodo.whereSql("s.data_conclusao")
        );
        if (dados.servicosConcluidos > 0) {
            dados.ticketServico = dados.faturamentoServicos.divide(
                new BigDecimal(dados.servicosConcluidos), 2, RoundingMode.HALF_UP);
        }
        dados.servicosPorMes = carregarPorMes("servico", "data_abertura");
        dados.statusOs = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(status_os), ''), 'Sem status') AS rotulo, COUNT(*) AS total " +
            "FROM servico GROUP BY COALESCE(NULLIF(TRIM(status_os), ''), 'Sem status') " +
            "ORDER BY total DESC",
            true
        );

        dados.orcamentosTotal = contar(
            "SELECT COUNT(*) FROM relatorio_orcamento r WHERE 1 = 1 " + periodo.whereSql("r.abertura")
        );
        dados.orcamentosAprovados = contar(
            "SELECT COUNT(*) FROM relatorio_orcamento r " +
            "WHERE LOWER(TRIM(COALESCE(r.status_orcamento, ''))) LIKE 'aprov%' " + periodo.whereSql("r.abertura")
        );
        dados.orcamentosManuais = contar(
            "SELECT COUNT(*) FROM relatorio_orcamento r " +
            "WHERE LOWER(TRIM(COALESCE(r.status_orcamento, ''))) LIKE 'servico manual%' " + periodo.whereSql("r.abertura")
        );
        dados.valorOrcamentos = somar(
            "SELECT COALESCE(SUM(COALESCE(r.valor_total_orcamento, 0)), 0) " +
            "FROM relatorio_orcamento r WHERE 1 = 1 " + periodo.whereSql("r.abertura")
        );
        if (dados.orcamentosTotal > 0) {
            dados.ticketOrcamento = dados.valorOrcamentos.divide(
                new BigDecimal(dados.orcamentosTotal), 2, RoundingMode.HALF_UP);
        }
        dados.orcamentosPorStatus = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(status_orcamento), ''), 'Sem status') AS rotulo, COUNT(*) AS total " +
            "FROM relatorio_orcamento GROUP BY COALESCE(NULLIF(TRIM(status_orcamento), ''), 'Sem status') " +
            "ORDER BY total DESC",
            true
        );
        dados.orcamentosPorMes = carregarPorMes("relatorio_orcamento", "abertura");
        dados.valorPorStatusOrcamento = carregarLista(
            "SELECT COALESCE(NULLIF(TRIM(status_orcamento), ''), 'Sem status') AS rotulo, " +
            "COALESCE(SUM(COALESCE(valor_total_orcamento, 0)), 0) AS total " +
            "FROM relatorio_orcamento GROUP BY COALESCE(NULLIF(TRIM(status_orcamento), ''), 'Sem status') " +
            "ORDER BY total DESC",
            true
        );

        return dados;
    }

    private List<ChartItem> carregarPorMes(String tabela, String coluna) {
        String[] meses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        double[] valores = new double[12];
        int ano = Calendar.getInstance().get(Calendar.YEAR);

        String sql =
            "SELECT MONTH(" + coluna + ") AS mes, COUNT(*) AS total " +
            "FROM " + tabela + " WHERE " + coluna + " IS NOT NULL AND YEAR(" + coluna + ") = ? " +
            "GROUP BY MONTH(" + coluna + ") ORDER BY mes";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ano);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    if (mes >= 1 && mes <= 12) valores[mes - 1] = rs.getDouble("total");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados por mês: " + e.getMessage());
        }

        List<ChartItem> itens = new ArrayList<>();
        for (int i = 0; i < meses.length; i++) {
            itens.add(new ChartItem(meses[i], valores[i], PALETA[i % PALETA.length]));
        }
        return itens;
    }

    private List<ChartItem> carregarLista(String sql, boolean limitar) {
        List<ChartItem> itens = new ArrayList<>();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int i = 0;
            while (rs.next()) {
                if (limitar && i >= 8) break;
                itens.add(new ChartItem(rs.getString("rotulo"), rs.getDouble("total"), PALETA[i % PALETA.length]));
                i++;
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do dashboard: " + e.getMessage());
        }

        if (itens.isEmpty()) {
            itens.add(new ChartItem("Sem dados", 0, COR_CIANO));
        }

        return itens;
    }

    private int contar(String sql) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("Erro ao contar dados do dashboard: " + e.getMessage());
        }
        return 0;
    }

    private BigDecimal somar(String sql) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal valor = rs.getBigDecimal(1);
                return valor == null ? BigDecimal.ZERO : valor;
            }
        } catch (Exception e) {
            System.err.println("Erro ao somar dados do dashboard: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private Periodo periodoSelecionado() {
        String opcao = comboPeriodo == null ? "Mes atual" : String.valueOf(comboPeriodo.getSelectedItem());
        if ("Ano atual".equals(opcao)) return Periodo.anoAtual();
        if ("Todos".equals(opcao)) return Periodo.todos();
        return Periodo.mesAtual();
    }

    private String detalhePeriodo() {
        return periodoSelecionado().rotulo;
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) valor = BigDecimal.ZERO;
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private void iniciarAtualizacaoAutomatica() {
        if (timerAtualizacao != null && timerAtualizacao.isRunning()) {
            timerAtualizacao.stop();
        }
        timerAtualizacao = new Timer(6000, e -> {
            if (isShowing()) atualizarDashboard();
        });
        timerAtualizacao.start();
    }

    private JPanel criarPainelPrincipal() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = preparar(g);
                int w = getWidth();
                int h = getHeight();

                Paint fundo = new LinearGradientPaint(
                    0, 0, w, h,
                    new float[]{0f, 0.52f, 1f},
                    new Color[]{
                        new Color(12, 33, 53, 226),
                        new Color(17, 70, 96, 196),
                        new Color(8, 24, 42, 225)
                    }
                );
                g2.setPaint(fundo);
                g2.fillRoundRect(0, 0, w, h, 24, 24);

                g2.setColor(new Color(0, 217, 230, 122));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 24, 24);

                desenharDetalheTecnologico(g2, w, h);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel criarCardVidro(int arco, int alpha) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = preparar(g);
                g2.setColor(new Color(COR_CARD.getRed(), COR_CARD.getGreen(), COR_CARD.getBlue(), alpha));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arco, arco);
                g2.setColor(new Color(0, 220, 225, 78));
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private void desenharDetalheTecnologico(Graphics2D g2, int w, int h) {
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(0, 220, 230, 22));
        int step = 52;
        for (int x = w / 2; x < w + step; x += step) {
            for (int y = 38; y < h; y += step) {
                int r = 18;
                Path2D hex = new Path2D.Double();
                for (int i = 0; i < 6; i++) {
                    double a = Math.toRadians(60 * i + 30);
                    double px = x + Math.cos(a) * r;
                    double py = y + Math.sin(a) * r;
                    if (i == 0) hex.moveTo(px, py);
                    else hex.lineTo(px, py);
                }
                hex.closePath();
                g2.draw(hex);
            }
        }

        RadialGradientPaint brilho = new RadialGradientPaint(
            new Point2D.Float(w * 0.86f, h * 0.15f),
            Math.max(w, h) * 0.42f,
            new float[]{0f, 1f},
            new Color[]{new Color(0, 220, 230, 45), new Color(0, 220, 230, 0)}
        );
        g2.setPaint(brilho);
        g2.fillRect(0, 0, w, h);
    }

    private Graphics2D preparar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
    }

    private Font fonteTitulo(float tamanho) {
        if (exo2SemiBold != null) {
            return exo2SemiBold.deriveFont(tamanho);
        }
        return new Font("Segoe UI", Font.BOLD, Math.round(tamanho));
    }

    private void carregarFonteExo() {
        try {
            InputStream is = getClass().getResourceAsStream("/imagens/Exo2-SemiBold.ttf");
            if (is != null) exo2SemiBold = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            exo2SemiBold = new Font("Segoe UI", Font.BOLD, 14);
        }
    }

    private enum DashboardPage {
        GERAL("GERAL", "DASHBOARD OPERACIONAL", "Visão geral de chamados, clientes, orçamentos e faturamento"),
        CLIENTES("CLIENTES", "DASHBOARD DE CLIENTES", "Total de clientes, ativos, inativos e relacionamento com OS"),
        PRODUTOS("PRODUTOS", "DASHBOARD DE PRODUTOS", "Equipamentos cadastrados, tipos, marcas e demanda"),
        TECNICOS("TÉCNICOS", "DASHBOARD DE TÉCNICOS", "Equipe técnica, especialidades, OS e faturamento"),
        ORCAMENTOS("ORÇAMENTOS", "DASHBOARD DE ORÇAMENTOS", "Status, valores, aprovações e volume por período"),
        SERVICOS("SERVIÇOS", "DASHBOARD DE SERVIÇOS", "Ordens de serviço, prazos, status e produtividade");

        final String botao;
        final String titulo;
        final String subtitulo;

        DashboardPage(String botao, String titulo, String subtitulo) {
            this.botao = botao;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
        }
    }

    private static class DashboardData {
        int clientesTotal;
        int clientesAtivos;
        int clientesInativos;
        int clientesComServico;
        int clientesComProduto;
        int produtosTotal;
        int produtosTipos;
        int produtosMarcas;
        int produtosSemServico;
        int tecnicosTotal;
        int tecnicosAtivos;
        int tecnicosInativos;
        int tecnicosEspecialidades;
        int osComTecnico;
        int servicosTotalPeriodo;
        int servicosAbertos;
        int servicosConcluidos;
        int servicosVencidos;
        int orcamentosTotal;
        int orcamentosAprovados;
        int orcamentosManuais;
        BigDecimal faturamentoServicos = BigDecimal.ZERO;
        BigDecimal ticketServico = BigDecimal.ZERO;
        BigDecimal valorOrcamentos = BigDecimal.ZERO;
        BigDecimal ticketOrcamento = BigDecimal.ZERO;
        List<ChartItem> clientesPorStatus = new ArrayList<>();
        List<ChartItem> produtosPorCliente = new ArrayList<>();
        List<ChartItem> servicosPorCliente = new ArrayList<>();
        List<ChartItem> orcamentosPorCliente = new ArrayList<>();
        List<ChartItem> produtosPorTipo = new ArrayList<>();
        List<ChartItem> produtosPorMarca = new ArrayList<>();
        List<ChartItem> osPorTipoProduto = new ArrayList<>();
        List<ChartItem> tecnicosPorStatus = new ArrayList<>();
        List<ChartItem> tecnicosPorEspecialidade = new ArrayList<>();
        List<ChartItem> servicosPorTecnico = new ArrayList<>();
        List<ChartItem> receitaPorTecnico = new ArrayList<>();
        List<ChartItem> servicosPorMes = new ArrayList<>();
        List<ChartItem> statusOs = new ArrayList<>();
        List<ChartItem> orcamentosPorStatus = new ArrayList<>();
        List<ChartItem> orcamentosPorMes = new ArrayList<>();
        List<ChartItem> valorPorStatusOrcamento = new ArrayList<>();
    }

    private static class Periodo {
        final String rotulo;
        final String sql;

        private Periodo(String rotulo, String sql) {
            this.rotulo = rotulo;
            this.sql = sql;
        }

        static Periodo mesAtual() {
            return new Periodo("mês atual", " AND {coluna} IS NOT NULL AND YEAR({coluna}) = YEAR(CURRENT_DATE) AND MONTH({coluna}) = MONTH(CURRENT_DATE) ");
        }

        static Periodo anoAtual() {
            return new Periodo("ano atual", " AND {coluna} IS NOT NULL AND YEAR({coluna}) = YEAR(CURRENT_DATE) ");
        }

        static Periodo todos() {
            return new Periodo("todos os registros", "");
        }

        String whereSql(String coluna) {
            return sql.replace("{coluna}", coluna);
        }
    }

    private static class ChartItem {
        final String label;
        final double value;
        final Color color;

        ChartItem(String label, double value, Color color) {
            this.label = label == null || label.trim().isEmpty() ? "Sem dados" : label.trim();
            this.value = value;
            this.color = color;
        }
    }

    private class DashboardNavButton extends JButton {
        private final DashboardPage page;
        private boolean selecionado;
        private boolean hover;

        DashboardNavButton(DashboardPage page) {
            super(page.botao);
            this.page = page;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setHorizontalAlignment(SwingConstants.CENTER);
            setPreferredSize(new Dimension(122, 31));
            setToolTipText("Abrir dashboard de " + page.botao.toLowerCase(Locale.ROOT));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        DashboardPage getPage() {
            return page;
        }

        void setSelecionado(boolean selecionado) {
            this.selecionado = selecionado;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = preparar(g);
            Color fundo = selecionado
                ? new Color(0, 181, 198, 230)
                : hover ? new Color(0, 130, 155, 190) : new Color(10, 39, 61, 184);
            g2.setColor(fundo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(selecionado ? new Color(170, 255, 255, 170) : new Color(0, 220, 225, 72));
            g2.setStroke(new BasicStroke(1.1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class MetricCard extends JPanel {
        private final JLabel titulo = new JLabel();
        private final JLabel valor = new JLabel();
        private final JLabel detalhe = new JLabel();
        private final Color destaque;

        MetricCard(String tituloTexto, String valorTexto, String detalheTexto, Color destaque) {
            this.destaque = destaque;
            setOpaque(false);
            setLayout(new BorderLayout(0, 2));
            setBorder(new EmptyBorder(13, 15, 11, 13));

            titulo.setText(tituloTexto.toUpperCase(Locale.ROOT));
            titulo.setForeground(COR_TEXTO_SUAVE);
            titulo.setFont(new Font("Segoe UI", Font.BOLD, 11));

            valor.setText(valorTexto);
            valor.setForeground(COR_TEXTO);
            valor.setFont(fonteTitulo(25f));

            detalhe.setText(detalheTexto);
            detalhe.setForeground(new Color(170, 214, 218));
            detalhe.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            add(titulo, BorderLayout.NORTH);
            add(valor, BorderLayout.CENTER);
            add(detalhe, BorderLayout.SOUTH);
        }

        void setValores(String novoValor, String novoDetalhe) {
            valor.setText(novoValor);
            detalhe.setText(novoDetalhe);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = preparar(g);
            int w = getWidth();
            int h = getHeight();

            g2.setPaint(new GradientPaint(0, 0, COR_CARD_2, w, h, new Color(9, 27, 45, 224)));
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            g2.setColor(new Color(destaque.getRed(), destaque.getGreen(), destaque.getBlue(), 72));
            g2.fillRoundRect(0, 0, 6, h, 6, 6);

            g2.setColor(new Color(destaque.getRed(), destaque.getGreen(), destaque.getBlue(), 116));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            g2.setColor(new Color(destaque.getRed(), destaque.getGreen(), destaque.getBlue(), 32));
            g2.fillOval(w - 54, -26, 92, 92);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private abstract class ChartCard extends JPanel {
        private final String titulo;
        private final String subtitulo;
        List<ChartItem> dados = new ArrayList<>();

        ChartCard(String titulo, String subtitulo) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(38, 16, 14, 16));
        }

        void setDados(List<ChartItem> novosDados) {
            dados = novosDados == null ? new ArrayList<>() : novosDados;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = preparar(g);
            int w = getWidth();
            int h = getHeight();

            g2.setPaint(new GradientPaint(0, 0, new Color(15, 44, 68, 218), w, h, new Color(8, 26, 45, 226)));
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.setColor(new Color(0, 220, 225, 82));
            g2.setStroke(new BasicStroke(1.1f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 20, 20);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.setColor(COR_CIANO_SUAVE);
            g2.drawString(titulo, 16, 22);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(168, 210, 216));
            g2.drawString(subtitulo, 16, 36);

            paintChart(g2, new Insets(46, 16, 14, 16));
            g2.dispose();
            super.paintComponent(g);
        }

        abstract void paintChart(Graphics2D g2, Insets areaInsets);

        double maxValor() {
            double max = 0;
            for (ChartItem item : dados) {
                if (item.value > max) max = item.value;
            }
            return max <= 0 ? 1 : max;
        }

        String valorCurto(double valor) {
            if (valor >= 1000000) return String.format(Locale.US, "%.1fM", valor / 1000000d);
            if (valor >= 1000) return String.format(Locale.US, "%.1fk", valor / 1000d);
            return String.valueOf((int) Math.round(valor));
        }

        String limitar(String texto, Graphics2D g2, int largura) {
            if (texto == null) return "";
            FontMetrics fm = g2.getFontMetrics();
            if (fm.stringWidth(texto) <= largura) return texto;
            String r = texto;
            while (r.length() > 2 && fm.stringWidth(r + "...") > largura) {
                r = r.substring(0, r.length() - 1);
            }
            return r + "...";
        }

        void desenharSemDados(Graphics2D g2) {
            g2.setColor(COR_TEXTO_SUAVE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String msg = "Sem dados para exibir";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
        }
    }

    private class LineChartPanel extends ChartCard {
        LineChartPanel(String titulo, String subtitulo) {
            super(titulo, subtitulo);
        }

        @Override
        void paintChart(Graphics2D g2, Insets in) {
            int x = in.left + 24;
            int y = in.top + 8;
            int w = getWidth() - in.left - in.right - 36;
            int h = getHeight() - in.top - in.bottom - 18;
            if (w <= 20 || h <= 20) return;

            double max = maxValor();
            g2.setStroke(new BasicStroke(1f));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i <= 4; i++) {
                int gy = y + (int) (h * i / 4.0);
                g2.setColor(COR_GRID);
                g2.drawLine(x, gy, x + w, gy);
                double valor = max * (4 - i) / 4.0;
                g2.setColor(new Color(165, 210, 215));
                g2.drawString(valorCurto(valor), x - 24, gy + 4);
            }

            if (dados.isEmpty()) return;
            int n = dados.size();
            double passo = n <= 1 ? w : w / (double) (n - 1);

            Path2D area = new Path2D.Double();
            Path2D linha = new Path2D.Double();
            for (int i = 0; i < n; i++) {
                ChartItem item = dados.get(i);
                int px = x + (int) Math.round(i * passo);
                int py = y + h - (int) Math.round((item.value / max) * h);
                if (i == 0) {
                    linha.moveTo(px, py);
                    area.moveTo(px, y + h);
                    area.lineTo(px, py);
                } else {
                    linha.lineTo(px, py);
                    area.lineTo(px, py);
                }
            }
            area.lineTo(x + w, y + h);
            area.closePath();

            g2.setPaint(new GradientPaint(0, y, new Color(0, 213, 224, 86), 0, y + h, new Color(0, 213, 224, 0)));
            g2.fill(area);

            g2.setColor(COR_CIANO);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linha);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < n; i++) {
                ChartItem item = dados.get(i);
                int px = x + (int) Math.round(i * passo);
                int py = y + h - (int) Math.round((item.value / max) * h);
                g2.setColor(new Color(0, 25, 35));
                g2.fillOval(px - 5, py - 5, 10, 10);
                g2.setColor(COR_CIANO);
                g2.fillOval(px - 3, py - 3, 6, 6);

                g2.setColor(new Color(185, 223, 226));
                FontMetrics fm = g2.getFontMetrics();
                String label = item.label;
                int lx = px - fm.stringWidth(label) / 2;
                g2.drawString(label, lx, y + h + 16);
            }
        }
    }

    private class DonutChartPanel extends ChartCard {
        DonutChartPanel(String titulo, String subtitulo) {
            super(titulo, subtitulo);
        }

        @Override
        void paintChart(Graphics2D g2, Insets in) {
            double total = 0;
            for (ChartItem item : dados) total += item.value;
            if (total <= 0) {
                desenharSemDados(g2);
                return;
            }

            int areaX = in.left;
            int areaY = in.top;
            int areaW = getWidth() - in.left - in.right;
            int areaH = getHeight() - in.top - in.bottom;
            int legendW = Math.min(150, areaW / 2);
            int diam = Math.min(areaW - legendW - 12, areaH - 10);
            diam = Math.max(64, diam);
            int x = areaX + 2;
            int y = areaY + (areaH - diam) / 2;

            double angulo = -90;
            for (ChartItem item : dados) {
                double graus = item.value / total * 360;
                g2.setColor(item.color);
                g2.fill(new Arc2D.Double(x, y, diam, diam, angulo, graus, Arc2D.PIE));
                g2.setColor(new Color(8, 25, 40));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Arc2D.Double(x, y, diam, diam, angulo, graus, Arc2D.PIE));
                angulo += graus;
            }

            int buraco = (int) (diam * 0.58);
            int bx = x + (diam - buraco) / 2;
            int by = y + (diam - buraco) / 2;
            g2.setPaint(new RadialGradientPaint(
                new Point(bx + buraco / 2, by + buraco / 2),
                buraco / 2f,
                new float[]{0f, 1f},
                new Color[]{new Color(18, 55, 78), new Color(8, 25, 42)}
            ));
            g2.fillOval(bx, by, buraco, buraco);

            g2.setColor(COR_TEXTO);
            g2.setFont(fonteTitulo(22f));
            FontMetrics fm = g2.getFontMetrics();
            String totalStr = String.valueOf((int) Math.round(total));
            g2.drawString(totalStr, bx + (buraco - fm.stringWidth(totalStr)) / 2, by + buraco / 2 + 3);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fm = g2.getFontMetrics();
            String sub = "registros";
            g2.setColor(COR_TEXTO_SUAVE);
            g2.drawString(sub, bx + (buraco - fm.stringWidth(sub)) / 2, by + buraco / 2 + 18);

            int lx = x + diam + 14;
            int ly = areaY + Math.max(8, (areaH - dados.size() * 22) / 2);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i < dados.size(); i++) {
                ChartItem item = dados.get(i);
                double pct = item.value / total * 100;
                int rowY = ly + i * 22;
                g2.setColor(item.color);
                g2.fillRoundRect(lx, rowY, 10, 10, 4, 4);
                g2.setColor(COR_TEXTO_SUAVE);
                String texto = limitar(item.label, g2, Math.max(44, getWidth() - lx - 54));
                g2.drawString(texto, lx + 15, rowY + 9);
                g2.setColor(Color.WHITE);
                g2.drawString(String.format(Locale.US, "%.0f%%", pct), getWidth() - in.right - 32, rowY + 9);
            }
        }
    }

    private class BarChartPanel extends ChartCard {
        BarChartPanel(String titulo, String subtitulo) {
            super(titulo, subtitulo);
        }

        @Override
        void paintChart(Graphics2D g2, Insets in) {
            int x = in.left + 32;
            int y = in.top + 10;
            int w = getWidth() - in.left - in.right - 42;
            int h = getHeight() - in.top - in.bottom - 20;
            if (w <= 20 || h <= 20 || dados.isEmpty()) return;

            double max = maxValor();
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i <= 3; i++) {
                int gy = y + (int) (h * i / 3.0);
                g2.setColor(COR_GRID);
                g2.drawLine(x, gy, x + w, gy);
                g2.setColor(COR_TEXTO_SUAVE);
                g2.drawString(valorCurto(max * (3 - i) / 3.0), x - 28, gy + 4);
            }

            int n = Math.min(dados.size(), 8);
            int gap = Math.max(8, w / Math.max(1, n) / 5);
            int barW = Math.max(12, (w - gap * (n + 1)) / Math.max(1, n));
            for (int i = 0; i < n; i++) {
                ChartItem item = dados.get(i);
                int bh = (int) Math.round((item.value / max) * h);
                int bx = x + gap + i * (barW + gap);
                int by = y + h - bh;

                g2.setPaint(new GradientPaint(bx, by, item.color, bx, y + h, item.color.darker().darker()));
                g2.fillRoundRect(bx, by, barW, Math.max(2, bh), 10, 10);

                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillRoundRect(bx + 2, by + 1, Math.max(2, barW - 4), Math.min(8, Math.max(2, bh / 3)), 8, 8);

                g2.setColor(COR_TEXTO);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String valor = valorCurto(item.value);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valor, bx + (barW - fm.stringWidth(valor)) / 2, by - 4);

                g2.setColor(COR_TEXTO_SUAVE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                fm = g2.getFontMetrics();
                String label = limitar(item.label, g2, barW + gap);
                g2.drawString(label, bx + (barW - fm.stringWidth(label)) / 2, y + h + 16);
            }
        }
    }

    private class HorizontalBarChartPanel extends ChartCard {
        HorizontalBarChartPanel(String titulo, String subtitulo) {
            super(titulo, subtitulo);
        }

        @Override
        void paintChart(Graphics2D g2, Insets in) {
            int x = in.left + 86;
            int y = in.top + 8;
            int w = getWidth() - in.left - in.right - 104;
            int h = getHeight() - in.top - in.bottom - 12;
            if (w <= 20 || h <= 20 || dados.isEmpty()) return;

            double max = maxValor();
            int n = Math.min(dados.size(), 6);
            int rowH = Math.max(20, h / Math.max(1, n));

            for (int i = 0; i < n; i++) {
                ChartItem item = dados.get(i);
                int cy = y + i * rowH + rowH / 2;
                int barH = Math.max(9, Math.min(18, rowH - 8));
                int bw = (int) Math.round((item.value / max) * w);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(COR_TEXTO_SUAVE);
                String label = limitar(item.label, g2, 76);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, x - fm.stringWidth(label) - 10, cy + 4);

                g2.setColor(new Color(255, 255, 255, 24));
                g2.fillRoundRect(x, cy - barH / 2, w, barH, 12, 12);

                g2.setPaint(new GradientPaint(x, cy, item.color, x + bw, cy, item.color.brighter()));
                g2.fillRoundRect(x, cy - barH / 2, Math.max(3, bw), barH, 12, 12);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(valorCurto(item.value), x + Math.max(5, bw) + 7, cy + 4);
            }
        }
    }
}
