// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Imports que a tela de servicos precisa para funcionar.
import Model.Servico;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URL;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

// Essa tela mostra e cadastra as ordens de servico do sistema.
// Aqui eu deixo o formulario e a tabela de OS.
// A tabela tambem mostra se a OS veio de um orcamento ou foi criada manualmente.
public class PainelServicos extends JPanel {

    // -- Cores que uso em toda a tela ------------------------------
    private final Color COR_TITULO        = new Color(0x80CBCB);        // azul claro dos títulos
    private final Color COR_VIDRO_LATERAL = new Color(15, 35, 55, 190); // fundo do menu lateral
    private final Color COR_VIDRO_CARDS   = new Color(20, 43, 66, 170); // fundo dos cards
    private final Color VERDE_MTEC        = new Color(133, 201, 196);   // verde dos botões positivos
    private final Color VERMELHO_MTEC     = new Color(200, 0, 0);       // vermelho dos botões de cancelar/excluir
    private final Color COR_HEADER_TAB    = new Color(30, 90, 110);      // cor sólida do cabeçalho da tabela
    private final Color COR_LINHA_PAR     = new Color(20, 55, 80, 180);  // linhas pares da tabela
    private final Color COR_LINHA_IMPAR   = new Color(12, 38, 58, 160);  // linhas ímpares da tabela
    private final Color COR_SEL           = new Color(80, 180, 180, 120); // cor quando seleciona uma linha

    // -- Variáveis globais da tela ---------------------------------
    private Font exo2SemiBold;               // fonte personalizada do sistema
    private JLayeredPane layeredPane;        // camadas da tela (fundo + interface em cima)
    private JPanel painelFundo;             // painel que desenha o background
    private JPanel painelInterface;         // painel que fica em cima do fundo com os componentes
    private DefaultTableModel modeloTabela; // modelo de dados da tabela de OS
    private JTable tabelaServicos; // tabela principal que mostra as ordens de serviço
    private TableRowSorter<DefaultTableModel> filtroTabela; // filtro usado na pesquisa da tabela
    private JTextField tfPesquisaOs; // campo de pesquisa das OS cadastradas

    // -- Campos do formulário - declaro aqui para acessar em qualquer método --
    private JTextField tfOs;                   // campo do número da OS (somente leitura)
    private JTextField tfClientePesquisa;      // campo para pesquisar cliente pelo nome, telefone ou CPF/CNPJ
    private JPopupMenu popupClientes;          // menu suspenso com resultados da busca de clientes
    private int idClienteSelecionado = -1;     // guarda o ID real do cliente selecionado
    private JComboBox<ItemCombo> comboEquipamento; // lista de equipamentos do cliente selecionado
    private JComboBox<ItemCombo> comboTecnico; // lista de técnicos do banco com ID escondido
    private JComboBox<String> comboStatus;     // lista de status da OS
    private JButton btnInserir;                 // botão de inserir OS
    private JButton btnAtualizar;               // botão de atualizar OS
    private JButton btnCancelar;                // botão vermelho do formulário
    private JTextField tfPrazo;                // campo do prazo com máscara de data
    private JTextField tfAbertura;             // campo da data de abertura com máscara
    private CampoArredondado tfMaoObra;        // campo de valor bruto do serviço
    private CampoArredondado tfTotal;          // valor final do serviço
    private AreaArredondada taDescricao;       // descrição do defeito/serviço
    private boolean formatandoMoeda = false;   // evita loop quando formato moeda no campo
    private boolean carregandoValorEdicao = false; // evita que o valor zere enquanto carrego uma OS para editar
    private BigDecimal valorOriginalEditando = BigDecimal.ZERO; // guarda o valor da OS enquanto estou editando
    private String statusOriginalEditando = ""; // guarda o status original para evitar alteração sem querer
    private boolean servicoEditandoManual = true; // uso para não alterar detalhes vindos de orçamento
    private int idOsEditando = -1;             // guarda o ID da OS quando estiver editando (-1 = nenhuma)

    // -- Classe usada no ComboBox de técnico -----------------------
    // Ela mostra um texto bonito na tela, mas guarda o ID real do banco por trás
    private static class ItemCombo {
        private int idBanco;          // ID verdadeiro que vem do banco
        private String textoExibido;  // texto que aparece para o usuário

        public ItemCombo(int idBanco, String textoExibido) {
            this.idBanco = idBanco;
            this.textoExibido = textoExibido;
        }

        public int getIdBanco() {
            return idBanco;
        }

        @Override
        public String toString() {
            return textoExibido;
        }
    }

    // -- Construtor: monta todo o painel quando a classe é criada ----
    public PainelServicos() {
        carregarFonteExo(); // carrega a fonte antes de qualquer coisa

        // Como esse painel vai entrar dentro da TelaPrincipal, ele não cria janela própria
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 0, 0, 0));

        // -- Container dos dois cards (formulário + tabela) ---------
        JPanel containerCards = new JPanel(new BorderLayout(0, 20));
        containerCards.setOpaque(false);

        // Card de cima: formulário de cadastro de OS
        JPanel cardCrud = criarCardVidro();
        cardCrud.setLayout(new GridBagLayout());
        montarLayoutCrud(cardCrud); // monto os campos do formulário dentro desse card

        // Card de baixo: tabela com todas as OS
        JPanel cardTabela = criarCardVidro();
        cardTabela.setLayout(new BorderLayout());
        cardTabela.setBorder(new EmptyBorder(10, 10, 10, 10));
        montarTabela(cardTabela); // monto a tabela dentro desse card

        containerCards.add(cardCrud, BorderLayout.NORTH);   // formulário no topo
        containerCards.add(cardTabela, BorderLayout.CENTER); // tabela embaixo ocupando o resto

        // Adiciono os cards dentro do painel de serviços
        add(containerCards, BorderLayout.CENTER);

        // Quando essa tela aparecer, eu recarrego os dados do banco.
        // Isso ajuda a OS criada pelo orcamento aparecer sem precisar fechar o sistema.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                atualizarTela();
            }
        });

        // Esse listener ajuda quando o painel fica dentro de outro painel com menu.
        // Quando a tela voltar a aparecer, eu atualizo de novo os combos e a tabela.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                atualizarTela();
            }
        });

        // Deixo a abertura preenchida com a data de hoje, igual na tela de Orçamentos
        preencherDataAberturaHoje();
    }

    // Preenche a data de abertura com a data atual
    // Uso isso para deixar Serviços e Orçamentos funcionando no mesmo padrão
    private void preencherDataAberturaHoje() {
        if (tfAbertura != null) {
            String hoje = new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
            tfAbertura.setText(hoje);
        }
    }

    // TABELA DE ORDENS DE SERVIÇO

    // Monta a tabela dentro do card e configura todo o visual dela
    private void montarTabela(JPanel card) {
        // Defino os nomes das colunas da tabela
        // As colunas ID Cliente e ID Técnico ficam ocultas, mas ajudam na hora de editar sem depender só do nome
        String[] colunas = {"ID", "Cliente", "Equipamento", "Técnico", "Abertura", "Prazo", "Status", "Origem", "Valor Total", "Ações", "ID Cliente", "ID Técnico", "ID Produto", "Descrição", "Valor"};

        // Crio o modelo da tabela - ele guarda os dados das linhas
        // So a coluna Acoes e editavel, pois tem os botoes Editar/Excluir
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 9; }
        };

        carregarDados(); // preencho a tabela com os dados do banco

        // Crio a tabela com cores alternadas nas linhas
        tabelaServicos = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(COR_SEL); // linha selecionada fica azul claro
                } else {
                    // linhas pares e ímpares com cores diferentes
                    c.setBackground(row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR);
                }
                c.setForeground(Color.WHITE); // texto sempre branco
                return c;
            }
        };

        // Crio o filtro da tabela
        // Ele permite pesquisar OS por ID, cliente, equipamento, datas e status
        filtroTabela = new TableRowSorter<>(modeloTabela);
        tabelaServicos.setRowSorter(filtroTabela);
        for (int i = 0; i < modeloTabela.getColumnCount(); i++) filtroTabela.setSortable(i, false);

        // Configurações visuais gerais da tabela
        tabelaServicos.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabelaServicos.setRowHeight(38);          // altura de cada linha
        tabelaServicos.setShowGrid(false);        // sem linhas de grade
        tabelaServicos.setIntercellSpacing(new Dimension(0, 2)); // espaço entre linhas
        tabelaServicos.setOpaque(false);          // transparente para o card aparecer
        tabelaServicos.setFillsViewportHeight(true); // ocupa todo o espaço disponível
        tabelaServicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // só uma linha selecionada por vez
        tabelaServicos.getTableHeader().setReorderingAllowed(false); // impede arrastar colunas

        // Configuro o visual do cabeçalho da tabela
        JTableHeader header = tabelaServicos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(COR_HEADER_TAB);
        header.setForeground(COR_TITULO);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setOpaque(true); // deixo o cabeçalho sólido para as linhas não aparecerem por trás

        // Renderer personalizado para centralizar o texto do cabeçalho
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                setBackground(COR_HEADER_TAB);
                setForeground(COR_TITULO);
                setFont(new Font("Segoe UI", Font.BOLD, 15));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setOpaque(true);
                return this;
            }
        };

        // Aplico o renderer em todas as colunas do cabeçalho
        for (int i = 0; i < tabelaServicos.getColumnCount(); i++) {
            tabelaServicos.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Centralizo o texto de todas as células, menos a coluna de Ações
        DefaultTableCellRenderer cellCenter = new DefaultTableCellRenderer();
        cellCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaServicos.getColumnCount(); i++) {
            if (i != 9) {
                tabelaServicos.getColumnModel().getColumn(i).setCellRenderer(cellCenter);
            }
        }

        // Coluna de Ações: uso renderer e editor personalizados com botões Editar/Excluir
        tabelaServicos.getColumnModel().getColumn(9).setCellRenderer(new AcoesCellRenderer());
        tabelaServicos.getColumnModel().getColumn(9).setCellEditor(new AcoesCellEditor(tabelaServicos));
        tabelaServicos.getColumnModel().getColumn(9).setPreferredWidth(165);
        tabelaServicos.getColumnModel().getColumn(9).setMinWidth(165);

        // Defino a largura preferida das colunas visíveis
        int[] larguras = {65, 130, 160, 130, 95, 95, 120, 90, 100};
        for (int i = 0; i < larguras.length; i++) {
            tabelaServicos.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Escondo os IDs e os detalhes que só uso na hora de editar
        // Eles continuam no modelo da tabela, mas não aparecem para o usuário
        ocultarColuna(tabelaServicos, 10);
        ocultarColuna(tabelaServicos, 11);
        ocultarColuna(tabelaServicos, 12);
        ocultarColuna(tabelaServicos, 13);
        ocultarColuna(tabelaServicos, 14);


        // Painel de pesquisa que fica acima da tabela
        // Ele filtra as OS enquanto o usuário digita
        JPanel painelPesquisa = criarPainelPesquisaTabela();
        card.add(painelPesquisa, BorderLayout.NORTH);

        // Coloco a tabela dentro de um scroll para funcionar quando tiver muitas linhas
        JScrollPane scroll = new JScrollPane(tabelaServicos);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16); // velocidade do scroll

        card.add(scroll, BorderLayout.CENTER);
    }


    // Cria o painel de pesquisa da tabela de OS
    // Esse campo filtra a lista conforme o usuário vai digitando
    private JPanel criarPainelPesquisaTabela() {
        JPanel painelPesquisa = new JPanel(new GridBagLayout());
        painelPesquisa.setOpaque(false);
        painelPesquisa.setBorder(new EmptyBorder(0, 0, 10, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblPesquisa = criarLabel("PESQUISAR:");
        lblPesquisa.setFont(new Font("Segoe UI", Font.BOLD, 16));

        tfPesquisaOs = new CampoArredondado();
        LimiteCampos.aplicar(tfPesquisaOs, 100);
        tfPesquisaOs.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfPesquisaOs.setPreferredSize(new Dimension(280, 36));
        tfPesquisaOs.setToolTipText("Pesquise por ID, cliente, equipamento, técnico, data ou status");

        // Toda vez que digita, filtro a tabela automaticamente
        tfPesquisaOs.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabelaOs();
            }
        });

        JButton btnLimparPesquisa = criarBtnAcao("Limpar", new Color(80, 180, 180), Color.WHITE);
        btnLimparPesquisa.setToolTipText("Limpar o campo de pesquisa.");
        btnLimparPesquisa.setPreferredSize(new Dimension(90, 32));
        btnLimparPesquisa.addActionListener(e -> {
            tfPesquisaOs.setText("");
            filtrarTabelaOs();
        });

        // Adiciono o texto da pesquisa na esquerda
        g.gridx = 0;
        g.weightx = 0;
        painelPesquisa.add(lblPesquisa, g);

        // Adiciono o campo ocupando o espaço até antes da coluna Ações
        g.gridx = 1;
        g.weightx = 1;
        painelPesquisa.add(tfPesquisaOs, g);

        // Área do botão Limpar alinhada com a coluna Ações da tabela
        // A margem da direita joga o botão para o centro da mesma reta do título "Ações"
        JPanel painelBotaoLimpar = new JPanel(new GridBagLayout());
        painelBotaoLimpar.setOpaque(false);
        painelBotaoLimpar.setPreferredSize(new Dimension(170, 36));
        painelBotaoLimpar.setBorder(new EmptyBorder(0, 0, 0, 42));
        painelBotaoLimpar.add(btnLimparPesquisa);

        g.gridx = 2;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 0, 0);
        painelPesquisa.add(painelBotaoLimpar, g);

        return painelPesquisa;
    }

    // Filtra a tabela de OS usando o texto digitado no campo de pesquisa
    // Não mexe no banco, apenas esconde/mostra linhas na tela
    private void filtrarTabelaOs() {
        if (filtroTabela == null || tfPesquisaOs == null) {
            return;
        }

        String texto = tfPesquisaOs.getText().trim();

        // Se o campo estiver vazio, mostro todas as OS novamente
        if (texto.isEmpty()) {
            filtroTabela.setRowFilter(null);
            return;
        }

        // Uso Pattern.quote para evitar erro se a pessoa digitar símbolos como +, (, ), [, etc.
        filtroTabela.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }

    // Oculta uma coluna da JTable sem remover os dados dela do modelo
    private void ocultarColuna(JTable tabela, int indice) {
        TableColumn coluna = tabela.getColumnModel().getColumn(indice);
        coluna.setMinWidth(0);
        coluna.setMaxWidth(0);
        coluna.setPreferredWidth(0);
        coluna.setResizable(false);
    }

    // Metodo publico para a TelaPrincipal pedir para atualizar esta tela.
    // Fiz assim para a OS criada pelo orcamento aparecer quando abrir Servicos.
    public void atualizarTela() {
        atualizarComboTecnicos();
        atualizarComboEquipamentosDoCliente();
        carregarDados();
        filtrarTabelaOs();
    }

    // Carrega as OS do banco e coloca na tabela.
    // Agora eu busco também valor e descrição para o serviço manual ficar completo.
    private void carregarDados() {
        modeloTabela.setRowCount(0); // limpo a tabela antes de recarregar

        String sql =
            "SELECT s.idSERVICO, " +
            "       c.nome AS nomeCliente, " +
            "       TRIM(CONCAT_WS(' ', p.tipo, p.marca, p.modelo)) AS equipamento, " +
            "       COALESCE(t.nome, 'Não informado') AS nomeTecnico, " +
            "       s.data_abertura, s.prazo_entrega, s.data_conclusao, s.status_os, " +
            "       COALESCE(r.valor_total_orcamento, 0) AS valor_servico, " +
            "       CASE " +
            "           WHEN r.idRELATORIO_ORCAMENTO IS NULL THEN 'Manual' " +
            "           WHEN r.status_orcamento = 'Servico Manual' THEN 'Manual' " +
            "           ELSE 'Orcamento' " +
            "       END AS origem_servico, " +
            "       s.CLIENTE_idCliente, s.TECNICO_idTECNICO, s.PRODUTO_idPRODUTO, " +
            "       COALESCE(r.defeito_relatado, '') AS descricao_servico, " +
            "       CASE " +
            "           WHEN COALESCE(r.mao_obra_orcada, 0) > 0 THEN r.mao_obra_orcada " +
            "           ELSE COALESCE(r.valor_total_orcamento, 0) " +
            "       END AS mao_obra_servico " +
            "FROM servico s " +
            "JOIN cliente c ON s.CLIENTE_idCliente = c.idCliente AND c.ativo = 1 " +
            "LEFT JOIN produto p ON s.PRODUTO_idPRODUTO = p.idPRODUTO " +
            "LEFT JOIN tecnico t ON s.TECNICO_idTECNICO = t.idTECNICO " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "ORDER BY s.idSERVICO DESC";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String abertura = rs.getDate("data_abertura") != null
                    ? new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("data_abertura")) : "";

                String prazo = rs.getDate("prazo_entrega") != null
                    ? new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("prazo_entrega")) : "";

                String equipamento = rs.getString("equipamento");
                if (equipamento == null || equipamento.trim().isEmpty()) {
                    equipamento = "Sem equipamento";
                }

                BigDecimal valor = rs.getBigDecimal("valor_servico");
                BigDecimal maoObra = rs.getBigDecimal("mao_obra_servico");

                modeloTabela.addRow(new Object[]{
                    rs.getInt("idSERVICO"),
                    rs.getString("nomeCliente"),
                    equipamento,
                    rs.getString("nomeTecnico"),
                    abertura,
                    prazo,
                    rs.getString("status_os"),
                    formatarOrigem(rs.getString("origem_servico")),
                    formatarMoeda(valor),
                    "",
                    rs.getInt("CLIENTE_idCliente"),
                    rs.getInt("TECNICO_idTECNICO"),
                    rs.getInt("PRODUTO_idPRODUTO"),
                    rs.getString("descricao_servico"),
                    formatarMoedaTexto(maoObra)
                });
            }

        } catch (Exception e) {
            // Se der erro, eu mostro no console para facilitar achar o problema.
            System.out.println("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private String formatarOrigem(String origem) {
        if (origem == null || origem.trim().isEmpty()) {
            return "Manual";
        }

        if (origem.equalsIgnoreCase("Orcamento")) {
            return "Orçamento";
        }

        return origem;
    }

    // Carrega os técnicos do banco em um mapa de ID -> Nome
    // Uso isso para mostrar o técnico responsável na tabela sem mexer no DAO
    private Map<Integer, String> carregarMapaTecnicos() {
        Map<Integer, String> mapa = new HashMap<>();

        String sql = "SELECT idTECNICO, nome FROM tecnico";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getInt("idTECNICO"), rs.getString("nome"));
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar técnicos para tabela: " + e.getMessage());
        }

        return mapa;
    }

    // BOTÕES EDITAR E EXCLUIR DA TABELA

    // Esse renderer mostra os botões Editar/Excluir em cada linha (visual apenas)
    class AcoesCellRenderer implements TableCellRenderer {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        AcoesCellRenderer() {
            painel.setOpaque(true);
            painel.add(criarBtnAcao("Editar",  new Color(80, 180, 180), Color.WHITE));
            painel.add(criarBtnAcao("Excluir", new Color(200, 60, 60),  Color.WHITE));
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            // Muda a cor de fundo dependendo se a linha está selecionada ou não
            painel.setBackground(sel ? COR_SEL : (row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR));
            return painel;
        }
    }

    // Esse editor ativa os botões quando o usuário clica na célula de Ações
    class AcoesCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel  painel  = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JButton btnEdit = criarBtnAcao("Editar",  new Color(80, 180, 180), Color.WHITE);
        private final JButton btnDel  = criarBtnAcao("Excluir", new Color(200, 60, 60),  Color.WHITE);
        private final JTable tabela; // guardo a tabela para converter a linha filtrada para a linha real
        private int linhaAtual; // guarda qual linha real do modelo foi clicada

        AcoesCellEditor(JTable tabela) {
            this.tabela = tabela;
            painel.setOpaque(true);
            painel.add(btnEdit);
            painel.add(btnDel);

            // Quando clica em Editar, chamo o método usando a linha real do modelo
            btnEdit.addActionListener(e -> { fireEditingStopped(); onEditar(linhaAtual);  });
            // Quando clica em Excluir, chamo o método usando a linha real do modelo
            btnDel.addActionListener(e  -> { fireEditingStopped(); onExcluir(linhaAtual); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            // Como a tabela agora tem filtro, preciso converter a linha visual para a linha real do modelo
            linhaAtual = tabela.convertRowIndexToModel(row);
            painel.setBackground(COR_SEL);
            return painel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    // Cria um botão pequeno e arredondado para usar dentro da tabela e na pesquisa
    private JButton criarBtnAcao(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 12);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(70, 26));
        return b;
    }

    // Ação do botão Editar - preenche o formulário com os dados da linha selecionada
    private void onEditar(int linha) {
        // Pego o ID da OS e deixo salvo para o botão ATUALIZAR saber qual registro alterar.
        idOsEditando = (int) modeloTabela.getValueAt(linha, 0);
        tfOs.setText(String.valueOf(idOsEditando));

        // Deixo esta trava ligada enquanto carrego os campos.
        // Assim o listener do campo de moeda não joga o total para zero no meio do processo.
        carregandoValorEdicao = true;

        // Primeiro pego o valor que está na tabela, igual é feito na tela de orçamento.
        // A coluna 14 guarda o valor bruto escondido e a coluna 8 guarda o valor que aparece na tabela.
        BigDecimal valorServico = converterValor(String.valueOf(modeloTabela.getValueAt(linha, 14)));
        if (valorServico.compareTo(BigDecimal.ZERO) == 0) {
            valorServico = converterValor(String.valueOf(modeloTabela.getValueAt(linha, 8)));
        }

        // Se ainda assim não tiver valor, busco no banco pelo ID da OS.
        // Isso ajuda quando a OS veio de orçamento ou quando a tabela foi carregada de outro jeito.
        if (valorServico.compareTo(BigDecimal.ZERO) == 0) {
            valorServico = buscarValorServicoParaEditar(idOsEditando);
        }

        // Guardo o valor original da OS para não perder ele durante a edição.
        valorOriginalEditando = valorServico == null ? BigDecimal.ZERO : valorServico;

        // Preencho o cliente usando o nome visível e guardo o ID real usando a coluna oculta.
        tfClientePesquisa.setText(String.valueOf(modeloTabela.getValueAt(linha, 1)));
        idClienteSelecionado = (int) modeloTabela.getValueAt(linha, 10);

        // Faço igual na tela de orçamento: no modo edição eu deixo o equipamento atual fixo no combo.
        // Isso evita o combo recarregar e mexer em outros campos quando clico em Editar.
        int idProduto = (int) modeloTabela.getValueAt(linha, 12);
        comboEquipamento.removeAllItems();
        comboEquipamento.addItem(new ItemCombo(idProduto, String.valueOf(modeloTabela.getValueAt(linha, 2))));
        comboEquipamento.setSelectedIndex(0);

        // Seleciono o técnico correto usando o ID salvo na coluna oculta.
        int idTecnico = (int) modeloTabela.getValueAt(linha, 11);
        selecionarComboPorId(comboTecnico, idTecnico);

        // Preencho os campos de data.
        tfAbertura.setText(String.valueOf(modeloTabela.getValueAt(linha, 4)));
        tfPrazo.setText(String.valueOf(modeloTabela.getValueAt(linha, 5)));

        // Seleciono o status correto no ComboBox.
        String status = String.valueOf(modeloTabela.getValueAt(linha, 6));
        statusOriginalEditando = status;
        comboStatus.setSelectedItem(status);

        // Se a OS veio de orçamento, deixo descrição e valor só para leitura.
        String origem = String.valueOf(modeloTabela.getValueAt(linha, 7));
        servicoEditandoManual = !origem.equalsIgnoreCase("Orçamento");

        taDescricao.setText(String.valueOf(modeloTabela.getValueAt(linha, 13)));

        // Aqui está a correção principal: coloco o mesmo valor em Valor e Total.
        aplicarValorNosCampos(valorServico);

        carregandoValorEdicao = false;
        configurarModoEdicao(true);

        // Repito uma vez no final da fila do Swing para garantir que nenhum evento atrasado
        // do combo ou do campo de moeda volte o valor para R$ 0,00.
        final BigDecimal valorFinal = valorServico;
        SwingUtilities.invokeLater(() -> aplicarValorNosCampos(valorFinal));

        JOptionPane.showMessageDialog(this,
            "OS nº " + idOsEditando + " carregada para edição!\nAltere os campos e clique em ATUALIZAR.",
            "Editar OS", JOptionPane.INFORMATION_MESSAGE);

        // Depois que fecha o aviso, eu aplico de novo.
        // Isso evita algum evento atrasado do Swing deixar o valor como R$ 0,00.
        aplicarValorNosCampos(valorFinal);
        SwingUtilities.invokeLater(() -> aplicarValorNosCampos(valorFinal));
    }

    // Coloca o valor nos dois campos sem deixar o cálculo automático interferir.
    private void aplicarValorNosCampos(BigDecimal valor) {
        if (valor == null) valor = BigDecimal.ZERO;

        boolean travaAntiga = carregandoValorEdicao;
        carregandoValorEdicao = true;
        try {
            String textoValor = formatarMoedaTexto(valor);
            if (tfMaoObra != null) {
                tfMaoObra.setText(textoValor);
                tfMaoObra.setCaretPosition(tfMaoObra.getText().length());
            }
            if (tfTotal != null) {
                tfTotal.setText(textoValor);
                tfTotal.setCaretPosition(tfTotal.getText().length());
            }
        } finally {
            carregandoValorEdicao = travaAntiga;
        }
    }

    // Antes de salvar, garanto que o valor não será perdido.
    // Se o valor estiver zerado, mas o total tiver valor, copio o total para o campo de valor.
    private void garantirValorAntesDeSalvar() {
        BigDecimal maoObra = converterValor(tfMaoObra == null ? "" : tfMaoObra.getText());
        BigDecimal total = converterValor(tfTotal == null ? "" : tfTotal.getText());

        // Se o valor ficou zerado, mas o total ainda tem valor, uso o total.
        if (maoObra.compareTo(BigDecimal.ZERO) == 0 && total.compareTo(BigDecimal.ZERO) > 0) {
            aplicarValorNosCampos(total);
            return;
        }

        // Se os dois campos ficaram zerados durante a edição, uso o valor original da OS.
        if (idOsEditando != -1
                && maoObra.compareTo(BigDecimal.ZERO) == 0
                && valorOriginalEditando != null
                && valorOriginalEditando.compareTo(BigDecimal.ZERO) > 0) {
            aplicarValorNosCampos(valorOriginalEditando);
        }
    }

    // Busca o valor da OS direto do banco para preencher a edição sem zerar.
    private BigDecimal buscarValorServicoParaEditar(int idOs) {
        String sql =
            "SELECT " +
            "   COALESCE(NULLIF(r.mao_obra_orcada, 0), NULLIF(r.valor_total_orcamento, 0), 0) AS valor_servico " +
            "FROM servico s " +
            "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
            "WHERE s.idSERVICO = ? " +
            "ORDER BY " +
            "   CASE WHEN r.status_orcamento = 'Servico Manual' THEN 0 ELSE 1 END, " +
            "   r.idRELATORIO_ORCAMENTO DESC " +
            "LIMIT 1";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idOs);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal valor = rs.getBigDecimal("valor_servico");
                    if (valor != null) {
                        return valor.setScale(2, RoundingMode.HALF_UP);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar valor da OS para edição: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    // Ação do botão Excluir - pede confirmação e deleta a OS do banco
    private void onExcluir(int linha) {
        int idOs = (int) modeloTabela.getValueAt(linha, 0); // pego o ID da OS
        String status = String.valueOf(modeloTabela.getValueAt(linha, 6));

        String mensagem;
        String titulo;

        if (ehStatusConcluido(status)) {
            mensagem = "A OS nº " + idOs + " está CONCLUÍDA e aparece no relatório de serviços.\n" +
                       "Se excluir, ela também sairá dos valores arrecadados.\n\n" +
                       "Deseja realmente excluir esta OS concluída?";
            titulo = "Atenção: OS concluída";
        } else {
            mensagem = "Deseja excluir a OS nº " + idOs + "?";
            titulo = "Confirmar exclusão";
        }

        int ok = JOptionPane.showConfirmDialog(this,
            mensagem,
            titulo, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                new ServicoDAO().excluir(idOs); // deleto do banco
                modeloTabela.removeRow(linha);  // removo visualmente da tabela
                JOptionPane.showMessageDialog(this,
                    "OS excluída com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao excluir OS: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // FORMULÁRIO DE CADASTRO DE OS (painel superior)

    // Monta todos os campos e botões do formulário usando GridBagLayout
    // Deixei esta parte parecida com a tela de Orçamentos para cadastrar serviço manual completo.
    private void montarLayoutCrud(JPanel p) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.anchor = GridBagConstraints.CENTER;
        g.fill = GridBagConstraints.NONE;

        JLabel titulo = new JLabel("PAINEL DE SERVIÇOS");
        if (exo2SemiBold != null) titulo.setFont(exo2SemiBold.deriveFont(28f));
        else titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(COR_TITULO);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 6;
        p.add(titulo, g);

        // Campo usado só internamente, o ID aparece apenas na tabela depois que a OS é gerada
        tfOs = criarTextField(5);
        tfOs.setVisible(false);

        // Linha 1: Cliente | Equipamento | Técnico
        g.gridwidth = 1;
        g.gridy = 1;

        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("CLIENTE:"), g);

        tfClientePesquisa = criarTextField(18);
        LimiteCampos.aplicar(tfClientePesquisa, 100);
        tfClientePesquisa.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfClientePesquisa.setPreferredSize(new Dimension(250, 42));
        tfClientePesquisa.setMinimumSize(new Dimension(250, 42));
        tfClientePesquisa.setToolTipText("Digite o nome, telefone ou CPF/CNPJ do cliente");

        popupClientes = new JPopupMenu();
        popupClientes.setFocusable(false);

        tfClientePesquisa.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                pesquisarClientesEnquantoDigita();
            }
        });

        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(tfClientePesquisa, g);

        g.gridx = 2; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("EQUIPAMENTO:"), g);

        comboEquipamento = new JComboBox<>();
        comboEquipamento.setEditable(false);
        comboEquipamento.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboEquipamento.setPreferredSize(new Dimension(250, 42));
        comboEquipamento.setMinimumSize(new Dimension(250, 42));
        comboEquipamento.addItem(new ItemCombo(0, "Selecione um cliente"));
        comboEquipamento.setEnabled(false);

        // Quando abrir a lista de equipamentos, busco de novo no banco.
        // Assim, se cadastrou um equipamento em outra tela, ele aparece aqui na hora.
        comboEquipamento.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                atualizarComboEquipamentosDoCliente();
            }

            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });

        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(comboEquipamento, g);

        g.gridx = 4; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("TÉCNICO:"), g);

        comboTecnico = new JComboBox<>();
        comboTecnico.setEditable(false);
        comboTecnico.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboTecnico.setBackground(Color.WHITE);
        comboTecnico.setForeground(Color.BLACK);
        comboTecnico.setPreferredSize(new Dimension(230, 42));
        comboTecnico.setMinimumSize(new Dimension(230, 42));
        carregarCombo(comboTecnico);

        comboTecnico.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                atualizarComboTecnicos();
            }

            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });

        g.gridx = 5; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(comboTecnico, g);

        // Linha 2: Status | Abertura | Prazo
        g.gridy = 2;

        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("STATUS:"), g);

        comboStatus = new JComboBox<>(new String[]{
            "Em Andamento",
            "Concluído",
            "Cancelado"
        });
        comboStatus.setEditable(false);
        comboStatus.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboStatus.setPreferredSize(new Dimension(220, 42));
        comboStatus.setMinimumSize(new Dimension(220, 42));
        comboStatus.setSelectedItem("Em Andamento");
        comboStatus.setEnabled(false);
        comboStatus.setToolTipText("No cadastro manual o serviço sempre começa em andamento.");

        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(comboStatus, g);

        g.gridx = 2; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("ABERTURA:"), g);

        tfAbertura = criarTextFieldData();
        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(criarPainelDataComBotao(tfAbertura), g);

        g.gridx = 4; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("PRAZO:"), g);

        tfPrazo = criarTextFieldData();
        g.gridx = 5; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(criarPainelDataComBotao(tfPrazo), g);

        // Linha 3: Valor | Total
        // O sistema não separa mais peças e serviço; por isso o usuário digita um valor bruto.
        g.gridy = 3;

        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("VALOR:"), g);

        tfMaoObra = (CampoArredondado) criarTextField(10);
        tfMaoObra.setText("R$ 0,00");
        configurarCampoMoeda(tfMaoObra);
        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(tfMaoObra, g);

        g.gridx = 2; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("TOTAL:"), g);

        tfTotal = (CampoArredondado) criarTextField(10);
        tfTotal.setEditable(false);
        tfTotal.setText("R$ 0,00");
        tfTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(tfTotal, g);

        // Linha 4: descrição do serviço
        g.gridy = 4;

        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.NORTHEAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("DESCRIÇÃO:"), g);

        taDescricao = new AreaArredondada(2, 20);
        LimiteCampos.aplicar(taDescricao, 200);
        JScrollPane scrollDesc = new JScrollPane(taDescricao);
        scrollDesc.setOpaque(false);
        scrollDesc.getViewport().setOpaque(false);
        scrollDesc.setBorder(null);
        scrollDesc.setPreferredSize(new Dimension(0, 80));

        g.gridx = 1; g.gridwidth = 5; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.BOTH;
        p.add(scrollDesc, g);

        // Linha 5: botões de ação
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.setBorder(new EmptyBorder(6, 0, 6, 0));

        btnInserir = criarBotaoAcao("INSERIR", VERDE_MTEC, Color.WHITE);
        btnInserir.addActionListener(e -> onInserir());
        TooltipUtils.aplicarTooltipPadrao(btnInserir);

        btnAtualizar = criarBotaoAcao("ATUALIZAR", new Color(60, 130, 180), Color.WHITE);
        btnAtualizar.addActionListener(e -> onAtualizar());
        TooltipUtils.aplicarTooltipPadrao(btnAtualizar);

        btnCancelar = criarBotaoAcao("APAGAR", VERMELHO_MTEC, Color.WHITE);
        btnCancelar.addActionListener(e -> onCancelar());
        TooltipUtils.aplicarTooltipPadrao(btnCancelar);

        painelBotoes.add(btnInserir);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnCancelar);

        configurarModoEdicao(false);

        g.gridy = 5;
        g.gridx = 0;
        g.gridwidth = 6;
        g.weightx = 1;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(14, 10, 16, 10);
        p.add(painelBotoes, g);

        calcularTotal();
    }

    // Controla o que pode ou não ser alterado quando estiver editando
    private void configurarModoEdicao(boolean editando) {
        if (btnInserir != null) btnInserir.setVisible(!editando);
        if (btnAtualizar != null) btnAtualizar.setVisible(editando);
        if (btnCancelar != null) btnCancelar.setText(editando ? "CANCELAR" : "APAGAR");

        if (tfClientePesquisa != null) {
            tfClientePesquisa.setEditable(!editando);
            tfClientePesquisa.setFocusable(!editando);
        }

        if (comboEquipamento != null) {
            comboEquipamento.setEnabled(!editando && idClienteSelecionado > 0);
        }

        if (comboStatus != null) {
            // No cadastro a OS manual sempre nasce em andamento.
            // Só libero o status quando o usuário está editando uma OS já existente.
            comboStatus.setEnabled(editando);
            if (!editando) {
                comboStatus.setSelectedItem("Em Andamento");
            }
        }

        if (tfAbertura != null) {
            tfAbertura.setEditable(!editando);
            tfAbertura.setEnabled(!editando);
            tfAbertura.setFocusable(!editando);
        }

        // Se veio de orçamento, eu deixo os detalhes só para leitura.
        // Assim eu não altero o orçamento original pela tela de Serviços.
        boolean podeEditarDetalhes = !editando || servicoEditandoManual;
        if (tfMaoObra != null) tfMaoObra.setEditable(podeEditarDetalhes);
        if (taDescricao != null) taDescricao.setEditable(podeEditarDetalhes);
        if (tfTotal != null) tfTotal.setEditable(false);
    }

    // AÇÕES DOS BOTÕES DO FORMULÁRIO

    // Chamado quando clica em INSERIR - valida e salva nova OS no banco
    private void onInserir() {
        try {
            // Verifico se os campos obrigatórios foram preenchidos
            // O cliente precisa ter sido escolhido na lista de resultados, não apenas digitado
            int idProdutoSelecionado = extrairId(comboEquipamento);
            int idTecnicoSelecionado = extrairId(comboTecnico);

            if (idClienteSelecionado <= 0 || idProdutoSelecionado <= 0 || idTecnicoSelecionado <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Selecione um Cliente válido, um Equipamento e um Técnico!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Converto e valido as datas antes de tentar salvar no banco
            // Assim evito aparecer erro em inglês vindo direto do MySQL
            java.sql.Date dataAbertura = converterData(tfAbertura.getText());
            java.sql.Date prazoEntrega = converterData(tfPrazo.getText());

            if (dataAbertura == null) {
                JOptionPane.showMessageDialog(this,
                    "Informe uma data de abertura válida no formato DD/MM/AAAA!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (prazoEntrega == null) {
                JOptionPane.showMessageDialog(this,
                    "Informe um prazo de entrega válido no formato DD/MM/AAAA!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (prazoEntrega.before(dataAbertura)) {
                JOptionPane.showMessageDialog(this,
                    "O prazo de entrega não pode ser antes da data de abertura!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                tfPrazo.requestFocus();
                return;
            }

            if (taDescricao.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Informe a descrição/defeito relatado do serviço!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                taDescricao.requestFocus();
                return;
            }

            calcularTotal();

            // Monto o objeto Servico com os dados do formulário
            Servico os = new Servico();
            os.setClienteIdCliente(idClienteSelecionado); // pego o ID do cliente selecionado na pesquisa
            os.setProdutoIdProduto(idProdutoSelecionado); // pego o ID do equipamento escolhido
            os.setTecnicoIdTecnico(idTecnicoSelecionado);
            os.setStatusOs("Em Andamento"); // serviço manual novo sempre começa neste status
            os.setDataAbertura(dataAbertura); // uso a data já validada
            os.setPrazoEntrega(prazoEntrega);

            // Salvo a OS e os detalhes manuais no banco
            adicionarServicoManual(os);

            JOptionPane.showMessageDialog(this,
                "OS inserida com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            onCancelar();     // limpo o formulário
            carregarDados();  // atualizo a tabela com a nova OS

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao inserir OS: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Chamado quando clica em ATUALIZAR - salva alterações de uma OS existente
    private void onAtualizar() {
        // Verifico se tem uma OS sendo editada
        if (idOsEditando == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma OS na tabela para editar!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Verifico se o cliente e o técnico são válidos antes de atualizar
            garantirValorAntesDeSalvar();

            int idProdutoSelecionado = extrairId(comboEquipamento);
            int idTecnicoSelecionado = extrairId(comboTecnico);

            if (idClienteSelecionado <= 0 || idProdutoSelecionado <= 0 || idTecnicoSelecionado <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Selecione um Cliente válido, um Equipamento e um Técnico!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Converto e valido as datas antes de tentar atualizar no banco
            // Assim evito aparecer erro em inglês vindo direto do MySQL
            java.sql.Date dataAbertura = converterData(tfAbertura.getText());
            java.sql.Date prazoEntrega = converterData(tfPrazo.getText());

            if (dataAbertura == null) {
                JOptionPane.showMessageDialog(this,
                    "Informe uma data de abertura válida no formato DD/MM/AAAA!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (prazoEntrega == null) {
                JOptionPane.showMessageDialog(this,
                    "Informe um prazo de entrega válido no formato DD/MM/AAAA!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (prazoEntrega.before(dataAbertura)) {
                JOptionPane.showMessageDialog(this,
                    "O prazo de entrega não pode ser antes da data de abertura!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                tfPrazo.requestFocus();
                return;
            }

            if (servicoEditandoManual && taDescricao.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Informe a descrição/defeito relatado do serviço!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                taDescricao.requestFocus();
                return;
            }

            garantirValorAntesDeSalvar();
            calcularTotal();

            String novoStatus = String.valueOf(comboStatus.getSelectedItem());
            if (ehStatusConcluido(statusOriginalEditando)) {
                BigDecimal valorAtual = converterValor(tfTotal.getText());
                if (valorOriginalEditando != null
                        && valorOriginalEditando.compareTo(BigDecimal.ZERO) > 0
                        && valorAtual.compareTo(valorOriginalEditando) != 0) {
                    int op = JOptionPane.showConfirmDialog(this,
                        "Atenção: esta OS já estava concluída.\n" +
                        "Alterar o valor dela também altera o relatório de serviços.\n\n" +
                        "Valor anterior: " + formatarMoedaTexto(valorOriginalEditando) + "\n" +
                        "Valor novo: " + formatarMoedaTexto(valorAtual) + "\n\n" +
                        "Deseja continuar?",
                        "Confirmar alteração de valor",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (op != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            }

            if (ehStatusConcluido(statusOriginalEditando) && !ehStatusConcluido(novoStatus)) {
                int op = JOptionPane.showConfirmDialog(this,
                    "Esta OS estava concluída e já aparecia no relatório.\n" +
                    "Ao mudar o status para '" + novoStatus + "', a data de conclusão será limpa\n" +
                    "e ela sairá do relatório até ser concluída novamente.\n\n" +
                    "Deseja continuar?",
                    "Confirmar mudança de status",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (op != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Monto o objeto com os novos dados do formulário
            Servico os = new Servico();
            os.setIdSERVICO(idOsEditando);
            os.setClienteIdCliente(idClienteSelecionado);
            os.setProdutoIdProduto(idProdutoSelecionado);
            os.setTecnicoIdTecnico(idTecnicoSelecionado);
            os.setStatusOs(novoStatus);
            os.setDataAbertura(dataAbertura);
            os.setPrazoEntrega(prazoEntrega);

            // Atualizo a OS e, se for manual, atualizo os detalhes também
            alterarServicoManual(os);

            JOptionPane.showMessageDialog(this,
                "OS atualizada com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            onCancelar();    // limpo o formulário
            carregarDados(); // atualizo a tabela

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao atualizar OS: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Chamado quando clica em CANCELAR - limpa todos os campos do formulário
    private void onCancelar() {
        tfOs.setText("");                 // limpo o campo de número da OS
        tfPrazo.setText("");              // limpo o prazo
        preencherDataAberturaHoje();      // volto a abertura para a data de hoje
        tfClientePesquisa.setText("");    // limpo o campo de pesquisa de cliente
        idClienteSelecionado = -1;        // removo o cliente selecionado

        // Escondo o popup de clientes, caso ele esteja aberto
        if (popupClientes != null) {
            popupClientes.setVisible(false);
        }

        limparComboEquipamentos();
        comboTecnico.setSelectedIndex(0);
        comboStatus.setSelectedItem("Em Andamento");

        // Limpo também os campos detalhados do serviço manual
        if (tfMaoObra != null) tfMaoObra.setText("R$ 0,00");
        if (tfTotal != null) tfTotal.setText("R$ 0,00");
        if (taDescricao != null) taDescricao.setText("");

        servicoEditandoManual = true;
        valorOriginalEditando = BigDecimal.ZERO;
        statusOriginalEditando = "";
        idOsEditando = -1; // indico que não está mais editando nenhuma OS
        configurarModoEdicao(false);
    }

    // Extrai o ID real do banco de um item do ComboBox de técnico
    // Mesmo que a tela mostre 1, 2, 3 em ordem, o sistema salva o ID verdadeiro no banco
    private int extrairId(JComboBox<ItemCombo> combo) {
        ItemCombo item = (ItemCombo) combo.getSelectedItem();
        if (item == null) return -1;
        return item.getIdBanco();
    }

    // Seleciona no ComboBox o técnico pelo ID real do banco
    // Isso é usado quando clico em Editar e preciso carregar o técnico correto da OS
    private void selecionarComboPorId(JComboBox<ItemCombo> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            ItemCombo item = combo.getItemAt(i);
            if (item != null && item.getIdBanco() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
    }

    // Converte uma data no formato DD/MM/AAAA para java.sql.Date
    private java.sql.Date converterData(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) {
                return null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false); // impede datas impossíveis, tipo 31/02/2026
            java.util.Date d = sdf.parse(texto.trim());
            return new java.sql.Date(d.getTime());
        } catch (Exception e) {
            return null; // se a data estiver vazia ou inválida, retorno null
        }
    }

    // Configura campo de dinheiro e recalcula o total quando o usuário digita
    private void configurarCampoMoeda(CampoArredondado campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroMoeda());

        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calcularTotal(); }
            @Override public void removeUpdate(DocumentEvent e) { calcularTotal(); }
            @Override public void changedUpdate(DocumentEvent e) { calcularTotal(); }
        });

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (campo.getText().trim().equals("R$ 0,00")) campo.setText("");
            }

            @Override public void focusLost(java.awt.event.FocusEvent e) {
                formatarMoedaNoCampo(campo);
                calcularTotal();
            }
        });
    }

    // O total do serviço manual fica igual ao valor bruto, igual na tela de orçamento.
    private void calcularTotal() {
        if (tfMaoObra == null || tfTotal == null) return;
        if (carregandoValorEdicao) return;

        BigDecimal maoObra = converterValor(tfMaoObra.getText());
        tfTotal.setText(formatarMoedaTexto(maoObra));
    }

    // Formata o campo de dinheiro quando sai dele
    private void formatarMoedaNoCampo(JTextField campo) {
        if (formatandoMoeda) return;

        try {
            formatandoMoeda = true;
            BigDecimal valor = converterValor(campo.getText());
            campo.setText(formatarMoedaTexto(valor));
            campo.setCaretPosition(campo.getText().length());
        } finally {
            formatandoMoeda = false;
        }
    }

    // Converte texto tipo R$ 10,50 para BigDecimal
    private BigDecimal converterValor(String texto) {
        try {
            String limpo = texto == null ? "" : texto.trim();
            if (limpo.isEmpty()) return BigDecimal.ZERO;

            // O NumberFormat pode colocar um espaço invisível entre R$ e o valor.
            // Por isso eu limpo tudo que não for número, vírgula, ponto ou sinal.
            limpo = limpo.replace("\u00A0", " ");
            limpo = limpo.replace("R$", "").trim();
            limpo = limpo.replaceAll("[^0-9,.-]", "");

            if (limpo.isEmpty() || limpo.equals("-") || limpo.equals(",") || limpo.equals(".")) {
                return BigDecimal.ZERO;
            }

            // Formato brasileiro: 1.234,56 vira 1234.56
            if (limpo.contains(",")) {
                limpo = limpo.replace(".", "").replace(",", ".");
                return new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP);
            }

            // Formato simples: 100 ou 100.00
            return new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // Deixa o valor no padrão brasileiro.
    // Eu troquei o espaço invisível que o Java coloca depois do R$ por um espaço normal.
    // Isso é importante porque o filtro do campo de moeda pode bloquear o texto e deixar R$ 0,00.
    private String formatarMoedaTexto(BigDecimal valor) {
        if (valor == null) valor = BigDecimal.ZERO;
        String texto = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
        return texto.replace(' ', ' ');
    }

    // Insere uma OS manual e também salva os detalhes iguais ao orçamento
    private void adicionarServicoManual(Servico os) throws SQLException {
        String sqlServico =
            "INSERT INTO servico (data_abertura, prazo_entrega, data_conclusao, status_os, " +
            "CLIENTE_idCliente, TECNICO_idTECNICO, PRODUTO_idPRODUTO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = new ConnectionFactory().getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlServico, Statement.RETURN_GENERATED_KEYS)) {
                validarClienteAtivo(con, os.getClienteIdCliente());

                ps.setDate(1, os.getDataAbertura());
                ps.setDate(2, os.getPrazoEntrega());
                ps.setDate(3, dataConclusaoParaStatus(os.getStatusOs()));
                ps.setString(4, os.getStatusOs());
                ps.setInt(5, os.getClienteIdCliente());
                ps.setInt(6, os.getTecnicoIdTecnico());
                ps.setInt(7, os.getProdutoIdProduto());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        os.setIdSERVICO(rs.getInt(1));
                    } else {
                        throw new SQLException("Não foi possível pegar o ID da OS criada.");
                    }
                }

                inserirDetalhesServicoManual(con, os);
                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Atualiza a OS. Se ela for manual, atualiza os detalhes também.
    private void alterarServicoManual(Servico os) throws SQLException {
        String sqlServico =
            "UPDATE servico SET data_abertura=?, prazo_entrega=?, status_os=?, " +
            "data_conclusao = CASE WHEN LOWER(TRIM(?)) LIKE 'conclu%' " +
            "THEN COALESCE(data_conclusao, CURRENT_DATE) ELSE NULL END, " +
            "TECNICO_idTECNICO=?, CLIENTE_idCliente=?, PRODUTO_idPRODUTO=? " +
            "WHERE idSERVICO=?";

        try (Connection con = new ConnectionFactory().getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlServico)) {
                validarClienteAtivo(con, os.getClienteIdCliente());

                ps.setDate(1, os.getDataAbertura());
                ps.setDate(2, os.getPrazoEntrega());
                ps.setString(3, os.getStatusOs());
                ps.setString(4, os.getStatusOs());
                ps.setInt(5, os.getTecnicoIdTecnico());
                ps.setInt(6, os.getClienteIdCliente());
                ps.setInt(7, os.getProdutoIdProduto());
                ps.setInt(8, os.getIdSERVICO());
                ps.executeUpdate();

                if (servicoEditandoManual) {
                    boolean atualizou = atualizarDetalhesServicoManual(con, os);
                    if (!atualizou) {
                        inserirDetalhesServicoManual(con, os);
                    }
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Cria a parte detalhada do serviço manual na tabela de relatório
    private void inserirDetalhesServicoManual(Connection con, Servico os) throws SQLException {
        String sql =
            "INSERT INTO relatorio_orcamento (defeito_relatado, diagnostico_tecnico, pecas_orcadas, " +
            "mao_obra_orcada, valor_total_orcamento, abertura, validade, status_orcamento, " +
            "CLIENTE_idCliente, SERVICO_idSERVICO, PRODUTO_idPRODUTO, TECNICO_idTECNICO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            preencherDetalhesServicoInsert(ps, os);
            ps.executeUpdate();
        }
    }

    // Atualiza a parte detalhada do serviço manual
    private boolean atualizarDetalhesServicoManual(Connection con, Servico os) throws SQLException {
        String sql =
            "UPDATE relatorio_orcamento SET defeito_relatado=?, diagnostico_tecnico=?, pecas_orcadas=?, " +
            "mao_obra_orcada=?, valor_total_orcamento=?, abertura=?, validade=?, status_orcamento=?, " +
            "CLIENTE_idCliente=?, PRODUTO_idPRODUTO=?, TECNICO_idTECNICO=? " +
            "WHERE SERVICO_idSERVICO=? AND status_orcamento='Servico Manual'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            preencherDetalhesServicoUpdate(ps, os);
            ps.setInt(12, os.getIdSERVICO());
            return ps.executeUpdate() > 0;
        }
    }

    // Preenche os dados do serviço detalhado quando estou inserindo
    private void preencherDetalhesServicoInsert(PreparedStatement ps, Servico os) throws SQLException {
        preencherDetalhesBase(ps, os);
        ps.setInt(10, os.getIdSERVICO());
        ps.setInt(11, os.getProdutoIdProduto());
        ps.setInt(12, os.getTecnicoIdTecnico());
    }

    // Preenche os dados do serviço detalhado quando estou atualizando
    private void preencherDetalhesServicoUpdate(PreparedStatement ps, Servico os) throws SQLException {
        preencherDetalhesBase(ps, os);
        ps.setInt(10, os.getProdutoIdProduto());
        ps.setInt(11, os.getTecnicoIdTecnico());
    }

    // Parte que é igual no insert e no update
    private void preencherDetalhesBase(PreparedStatement ps, Servico os) throws SQLException {
        garantirValorAntesDeSalvar();
        calcularTotal();

        ps.setString(1, taDescricao.getText().trim());
        ps.setString(2, "");

        BigDecimal maoObra = converterValor(tfMaoObra.getText());
        BigDecimal totalAtual = converterValor(tfTotal.getText());

        // Segurança extra: nunca salvo zero se a OS já tinha valor.
        // Isso resolve o caso de editar só o status e acabar zerando o serviço sem querer.
        if (maoObra.compareTo(BigDecimal.ZERO) == 0 && totalAtual.compareTo(BigDecimal.ZERO) > 0) {
            maoObra = totalAtual;
        }
        if (idOsEditando != -1
                && maoObra.compareTo(BigDecimal.ZERO) == 0
                && valorOriginalEditando != null
                && valorOriginalEditando.compareTo(BigDecimal.ZERO) > 0) {
            maoObra = valorOriginalEditando;
        }

        ps.setString(3, "0.00");
        ps.setBigDecimal(4, maoObra);
        ps.setBigDecimal(5, maoObra);
        ps.setDate(6, os.getDataAbertura());
        ps.setDate(7, os.getPrazoEntrega());
        ps.setString(8, "Servico Manual");
        ps.setInt(9, os.getClienteIdCliente());
    }

    private boolean ehStatusConcluido(String status) {
        return status != null && status.trim().toLowerCase(new Locale("pt", "BR")).startsWith("conclu");
    }

    // Se o status for concluído, salvo a data de hoje como data de conclusão.
    // Se não estiver concluído, fica vazio no banco.
    private java.sql.Date dataConclusaoParaStatus(String status) {
        if (ehStatusConcluido(status)) {
            return new java.sql.Date(System.currentTimeMillis());
        }
        return null;
    }

    // Cliente arquivado não pode receber serviço novo
    private void validarClienteAtivo(Connection con, int idCliente) throws SQLException {
        String sql = "SELECT ativo FROM cliente WHERE idCliente = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("ativo")) {
                    throw new SQLException("Cliente inativo não pode ser usado em serviços.");
                }
            }
        }
    }

    // PESQUISA DE CLIENTES COM AUTOCOMPLETE

    // Pesquisa clientes no banco conforme o usuário digita
    // O usuário pode pesquisar por nome, telefone ou CPF/CNPJ
    private void pesquisarClientesEnquantoDigita() {
        String termo = tfClientePesquisa.getText().trim();

        // Sempre que o usuário digita, limpo o ID selecionado
        // Assim evito salvar uma OS com um cliente antigo sem querer
        idClienteSelecionado = -1;
        limparComboEquipamentos();

        // Fecho e limpo o popup antes de montar os novos resultados
        popupClientes.setVisible(false);
        popupClientes.removeAll();

        // Se o campo estiver vazio, não busco nada
        if (termo.length() < 1) {
            return;
        }

        // Busco clientes cujo nome, telefone ou CPF/CNPJ contenha o texto digitado
        String sql = "SELECT idCliente, nome, telefone, cpf_cnpj FROM cliente " +
                     "WHERE ativo = 1 AND (nome LIKE ? OR telefone LIKE ? OR cpf_cnpj LIKE ?) " +
                     "ORDER BY nome LIMIT 10";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String busca = "%" + termo + "%";
            ps.setString(1, busca);
            ps.setString(2, busca);
            ps.setString(3, busca);

            try (ResultSet rs = ps.executeQuery()) {
                boolean encontrou = false;

                while (rs.next()) {
                    encontrou = true;

                    int id = rs.getInt("idCliente");
                    String nome = rs.getString("nome");
                    String telefone = rs.getString("telefone");
                    String cpfCnpj = rs.getString("cpf_cnpj");

                    // Texto que aparece na lista de resultados
                    String textoExibido = nome;

                    if (telefone != null && !telefone.trim().isEmpty()) {
                        textoExibido += " - " + telefone;
                    }

                    if (cpfCnpj != null && !cpfCnpj.trim().isEmpty()) {
                        textoExibido += " - " + cpfCnpj;
                    }

                    JMenuItem item = new JMenuItem(textoExibido);
                    item.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                    item.setPreferredSize(new Dimension(tfClientePesquisa.getWidth(), 32));

                    // Quando o usuário clica em um cliente, salvo o ID real dele
                    item.addActionListener(e -> {
                        tfClientePesquisa.setText(nome);
                        idClienteSelecionado = id;
                        carregarEquipamentosDoCliente(idClienteSelecionado);
                        popupClientes.setVisible(false);
                    });

                    popupClientes.add(item);
                }

                // Se encontrou clientes, mostro a lista embaixo do campo
                if (encontrou) {
                    popupClientes.show(tfClientePesquisa, 0, tfClientePesquisa.getHeight());
                    tfClientePesquisa.requestFocus();
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao pesquisar clientes: " + e.getMessage());
        }
    }

    // Limpa o ComboBox de equipamentos quando ainda não existe cliente selecionado
    private void limparComboEquipamentos() {
        if (comboEquipamento == null) {
            return;
        }

        comboEquipamento.removeAllItems();
        comboEquipamento.addItem(new ItemCombo(0, "Selecione um cliente"));
        comboEquipamento.setEnabled(false);
    }

    // Atualiza a lista de equipamentos do cliente que está selecionado na tela.
    // Fiz isso para não precisar reiniciar o sistema depois de cadastrar equipamento.
    private void atualizarComboEquipamentosDoCliente() {
        if (comboEquipamento == null) {
            return;
        }

        // Se ainda não escolheu cliente, não tem equipamento para carregar.
        if (idClienteSelecionado <= 0) {
            limparComboEquipamentos();
            return;
        }

        int idSelecionado = extrairId(comboEquipamento);
        carregarEquipamentosDoCliente(idClienteSelecionado);

        // Tento manter o equipamento que já estava marcado.
        // Se for equipamento novo ou se o antigo não existir, ele volta para "Selecione".
        if (idSelecionado > 0) {
            selecionarComboPorId(comboEquipamento, idSelecionado);
        }
    }

    // Carrega no ComboBox somente os equipamentos do cliente selecionado
    // Isso evita abrir OS no equipamento errado quando o cliente tiver mais de uma máquina
    private void carregarEquipamentosDoCliente(int idCliente) {
        if (comboEquipamento == null) {
            return;
        }

        comboEquipamento.removeAllItems();

        if (idCliente <= 0) {
            comboEquipamento.addItem(new ItemCombo(0, "Selecione um cliente"));
            comboEquipamento.setEnabled(false);
            return;
        }

        String sql = "SELECT idPRODUTO, tipo, marca, modelo FROM produto " +
                     "WHERE CLIENTE_idCliente = ? ORDER BY idPRODUTO ASC";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                comboEquipamento.addItem(new ItemCombo(0, "Selecione"));

                boolean encontrou = false;

                while (rs.next()) {
                    encontrou = true;

                    int idProduto = rs.getInt("idPRODUTO");
                    String tipo = rs.getString("tipo");
                    String marca = rs.getString("marca");
                    String modelo = rs.getString("modelo");

                    // Monto o texto do equipamento usando as informações que existirem
                    String texto = montarTextoEquipamento(tipo, marca, modelo);

                    comboEquipamento.addItem(new ItemCombo(idProduto, texto));
                }

                if (!encontrou) {
                    comboEquipamento.removeAllItems();
                    comboEquipamento.addItem(new ItemCombo(0, "Nenhum equipamento cadastrado"));
                    comboEquipamento.setEnabled(false);
                } else {
                    comboEquipamento.setEnabled(true);
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar equipamentos do cliente: " + e.getMessage());
            comboEquipamento.removeAllItems();
            comboEquipamento.addItem(new ItemCombo(0, "(erro ao carregar)"));
            comboEquipamento.setEnabled(false);
        }
    }

    // Monta o texto do equipamento para aparecer no ComboBox
    private String montarTextoEquipamento(String tipo, String marca, String modelo) {
        StringBuilder sb = new StringBuilder();

        if (tipo != null && !tipo.trim().isEmpty()) {
            sb.append(tipo.trim());
        }

        if (marca != null && !marca.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(marca.trim());
        }

        if (modelo != null && !modelo.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(modelo.trim());
        }

        if (sb.length() == 0) {
            return "Equipamento sem descrição";
        }

        return sb.toString();
    }

    // Atualiza a lista de técnicos no ComboBox
    // Uso isso para mostrar técnicos cadastrados depois que a tela de serviços já estava aberta
    private void atualizarComboTecnicos() {
        if (comboTecnico == null) {
            return;
        }

        int idSelecionado = extrairId(comboTecnico);

        carregarCombo(comboTecnico);

        // Tento manter o técnico que já estava selecionado
        // Se ele não existir mais, volta para "Selecione"
        if (idSelecionado > 0) {
            selecionarComboPorId(comboTecnico, idSelecionado);
        }
    }

    // Carrega os dados do banco dentro do ComboBox de técnico
    // Deixei igual ao orçamento: mostra só o nome do técnico e tira o Administrador.
    private void carregarCombo(JComboBox<ItemCombo> combo) {
        String sql = "SELECT idTECNICO, nome, status FROM tecnico " +
                     "WHERE LOWER(TRIM(nome)) <> 'administrador' " +
                     "AND (status IS NULL OR LOWER(TRIM(status)) = 'ativo') " +
                     "ORDER BY nome ASC";

        try (Connection con = new ConnectionFactory().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            combo.removeAllItems();
            combo.addItem(new ItemCombo(0, "Selecione"));

            while (rs.next()) {
                int idRealBanco = rs.getInt("idTECNICO");
                String nomeTecnico = rs.getString("nome");
                String nomeLimpo = nomeTecnico == null ? "" : nomeTecnico.trim();

                // Aqui aparece só o nome, igual na tela de orçamentos.
                combo.addItem(new ItemCombo(idRealBanco, nomeLimpo));
            }

        } catch (Exception e) {
            System.out.println("ERRO NA CONEXAO: " + e.getMessage());
            combo.removeAllItems();
            combo.addItem(new ItemCombo(0, "(banco indisponível)"));
        }
    }

    // BOTÃO ARREDONDADO DO PAINEL

    // Esse botão é usado no formulário, na tabela e na pesquisa
    // Ele tem borda arredondada, transparência e efeito ao passar o mouse
    class BotaoArredondado extends JButton {
        private Color corFundo;    // cor principal do botão
        private Color corTexto;    // cor do texto
        private boolean hover;     // indica se o mouse está em cima
        private int arco;          // arredondamento das bordas

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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int largura = getWidth();
            int altura = getHeight();

            // Clareio um pouco a cor quando o mouse passa em cima
            Color corAtual = hover ? clarearCor(corFundo, 25) : corFundo;

            // Fundo com transparência para combinar com o template
            g2.setColor(new Color(corAtual.getRed(), corAtual.getGreen(), corAtual.getBlue(), hover ? 235 : 210));
            g2.fillRoundRect(1, 1, largura - 3, altura - 3, arco, arco);

            // Borda do botão
            Color corBorda;
            if (corFundo.getRed() > 150 && corFundo.getGreen() < 90) {
                corBorda = new Color(255, 120, 120, 180); // borda para botão vermelho
            } else {
                corBorda = new Color(0, 200, 200, 170);   // borda ciano para os outros botões
            }

            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 1, largura - 3, altura - 3, arco, arco);

            // Texto centralizado
            g2.setFont(getFont());
            g2.setColor(corTexto);
            FontMetrics fm = g2.getFontMetrics();
            int x = (largura - fm.stringWidth(getText())) / 2;
            int y = (altura - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(getText(), x, y);
            g2.dispose();
        }

        // Clareia a cor para criar o efeito de hover
        private Color clarearCor(Color c, int valor) {
            int r = Math.min(255, c.getRed() + valor);
            int g = Math.min(255, c.getGreen() + valor);
            int b = Math.min(255, c.getBlue() + valor);
            return new Color(r, g, b);
        }
    }

    // CAMPO DE TEXTO ARREDONDADO

    // Esse campo substitui o JTextField padrão do Java
    // Fiz assim para combinar melhor com o visual arredondado do template
    class CampoArredondado extends JTextField {
        private Color corFundo = Color.WHITE;                    // fundo branco do campo
        private Color corBorda = new Color(0, 200, 200, 150);     // borda ciano suave
        private int arco = 14;                                   // arredondamento das bordas

        public CampoArredondado() {
            super();
            configurarCampo();
        }

        public CampoArredondado(int colunas) {
            super(colunas);
            configurarCampo();
        }

        private void configurarCampo() {
            setOpaque(false); // deixo falso porque eu mesmo desenho o fundo arredondado
            setBorder(new EmptyBorder(0, 12, 0, 12)); // espaço interno para o texto não colar na borda
            setBackground(corFundo);
            setSelectionColor(new Color(80, 180, 180));
            setSelectedTextColor(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Desenho o fundo arredondado antes do texto
            g2.setColor(corFundo);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Desenho uma borda bem leve para não ficar quadrado
            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);

            g2.dispose();
        }
    }


    // Área de texto arredondada para a descrição do serviço
    class AreaArredondada extends JTextArea {
        private Color corFundo = Color.WHITE;
        private Color corBorda = new Color(0, 200, 200, 150);
        private int arco = 14;

        public AreaArredondada(int rows, int cols) {
            super(rows, cols);
            setOpaque(false);
            setForeground(Color.BLACK);
            setCaretColor(Color.BLACK);
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setSelectionColor(new Color(80, 180, 180));
            setSelectedTextColor(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corFundo);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
            g2.dispose();
        }
    }


    // MÉTODOS AUXILIARES (usados em vários lugares)

    // Cria um painel com fundo arredondado semitransparente (efeito vidro)
    private JPanel criarCardVidro() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_VIDRO_CARDS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // bordas arredondadas
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    // Adiciona os botões de navegação do menu lateral
    // Cada botão é uma imagem PNG da pasta imagens
    private void adicionarBotoesMenu(JPanel p) {
        String[] botoes = {"clientes.png", "produtos.png", "tecnicos.png", "orcamentos.png", "servicos.png"};
        for (String arq : botoes) {
            JLabel btn = new JLabel();
            URL url = getClass().getResource("/imagens/" + arq);
            if (url != null) {
                // Redimensiono a imagem para 210x50px
                btn.setIcon(new ImageIcon(
                    new ImageIcon(url).getImage().getScaledInstance(210, 50, Image.SCALE_SMOOTH)));
            }
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // cursor de mãozinha ao passar o mouse
            p.add(btn);
            p.add(Box.createVerticalStrut(10)); // espaço entre os botões
        }
    }

    // Cria um JLabel estilizado com a fonte e cor padrão dos labels do sistema
    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 19));
        return l;
    }

    // Cria um JTextField estilizado com tamanho fixo para não encolher na tela
    private JTextField criarTextField(int col) {
        JTextField tf = new CampoArredondado(col);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        tf.setPreferredSize(new Dimension(180, 42)); // largura e altura fixas
        tf.setMinimumSize(new Dimension(180, 42));
        return tf;
    }

    // Cria um botão grande e arredondado para usar no formulário
    // Ele fica mais parecido com os botões laterais do template
    private JButton criarBotaoAcao(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 18);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setPreferredSize(new Dimension(120, 40));
        b.setMinimumSize(new Dimension(120, 40));
        b.setMaximumSize(new Dimension(120, 40));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return b;
    }

    // Coloca o logo da MTEC no menu lateral redimensionado para o tamanho certo
    private void configurarLogo(JPanel p, String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url != null) {
            p.add(new JLabel(new ImageIcon(
                new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH))));
        }
    }

    // Cria um campo de texto com máscara automática de data no formato DD/MM/AAAA
    // Só aceita números - as barras aparecem automaticamente
    private JTextField criarTextFieldData() {
        JTextField tf = new CampoArredondado();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        tf.setPreferredSize(new Dimension(180, 42));
        tf.setMinimumSize(new Dimension(180, 42));
        tf.setToolTipText("DD/MM/AAAA"); // dica que aparece ao passar o mouse

        // Aplico o filtro de máscara de data no documento do campo
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new FiltroData());
        return tf;
    }


    // Cria um painel com o campo de data e um botão de calendário ao lado
    // Assim o usuário pode escolher a data sem precisar digitar manualmente
    private JPanel criarPainelDataComBotao(JTextField campoData) {
        JPanel painel = new JPanel(new BorderLayout(6, 0));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(220, 42));
        painel.setMinimumSize(new Dimension(220, 42));

        // Deixo o campo um pouco menor para caber o botão do calendário do lado
        campoData.setPreferredSize(new Dimension(170, 42));
        campoData.setMinimumSize(new Dimension(170, 42));

        JButton btnCalendario = new BotaoArredondado("📅", new Color(80, 180, 180), Color.WHITE, 14);
        btnCalendario.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnCalendario.setPreferredSize(new Dimension(42, 42));
        btnCalendario.setToolTipText("Escolher data no calendário");

        // Quando clicar no botão, abro a janelinha para escolher a data
        btnCalendario.addActionListener(e -> abrirCalendario(campoData));

        painel.add(campoData, BorderLayout.CENTER);
        painel.add(btnCalendario, BorderLayout.EAST);

        return painel;
    }

    // Abre uma janelinha simples para escolher dia, mês e ano
    // Fiz sem biblioteca externa para facilitar na hora de entregar o projeto
    private void abrirCalendario(JTextField campoDestino) {
        CalendarioMtec.abrir(this, campoDestino);
    }

    private void abrirCalendarioAntigo(JTextField campoDestino) {
        if (campoDestino == null || !campoDestino.isEnabled() || !campoDestino.isEditable()) {
            return;
        }

        Window janelaPai = SwingUtilities.getWindowAncestor(this);

        JDialog dialog;
        if (janelaPai instanceof Frame) {
            dialog = new JDialog((Frame) janelaPai, "Selecionar data", true);
        } else {
            dialog = new JDialog((Frame) null, "Selecionar data", true);
        }

        dialog.setSize(430, 300);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        // Painel principal do calendário
        // Separei os campos dos botões para o botão Confirmar não sumir/cortar
        JPanel painelPrincipal = new JPanel(new BorderLayout(0, 14));
        painelPrincipal.setBorder(new EmptyBorder(22, 25, 22, 25));
        painelPrincipal.setBackground(new Color(20, 43, 66));

        JPanel painelCampos = new JPanel(new GridBagLayout());
        painelCampos.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        Calendar calendario = Calendar.getInstance();

        // Se o campo já tiver uma data válida, abro o calendário nela
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            java.util.Date dataAtual = sdf.parse(campoDestino.getText().trim());
            calendario.setTime(dataAtual);
        } catch (Exception e) {
            // Se o campo estiver vazio ou inválido, uso a data de hoje
        }

        JLabel lblDia = criarLabel("DIA:");
        JLabel lblMes = criarLabel("MÊS:");
        JLabel lblAno = criarLabel("ANO:");

        lblDia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAno.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JSpinner spDia = new JSpinner(new SpinnerNumberModel(
            calendario.get(Calendar.DAY_OF_MONTH), 1, 31, 1
        ));

        JSpinner spMes = new JSpinner(new SpinnerNumberModel(
            calendario.get(Calendar.MONTH) + 1, 1, 12, 1
        ));

        JSpinner spAno = new JSpinner(new SpinnerNumberModel(
            calendario.get(Calendar.YEAR), 2000, 2100, 1
        ));

        Dimension tamanhoSpinner = new Dimension(90, 32);
        spDia.setPreferredSize(tamanhoSpinner);
        spMes.setPreferredSize(tamanhoSpinner);
        spAno.setPreferredSize(tamanhoSpinner);

        // Tiro a separação de milhar do ano
        // Assim aparece 2026 em vez de 2.026
        spDia.setEditor(new JSpinner.NumberEditor(spDia, "00"));
        spMes.setEditor(new JSpinner.NumberEditor(spMes, "00"));
        spAno.setEditor(new JSpinner.NumberEditor(spAno, "0"));

        ((JSpinner.NumberEditor) spDia.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        ((JSpinner.NumberEditor) spMes.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        ((JSpinner.NumberEditor) spAno.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);

        // Atualiza o limite de dias quando mudar mês ou ano
        // Exemplo: fevereiro não deixa escolher dia 31
        Runnable atualizarLimiteDia = () -> {
            int mes = (Integer) spMes.getValue();
            int ano = (Integer) spAno.getValue();

            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, ano);
            c.set(Calendar.MONTH, mes - 1);
            c.set(Calendar.DAY_OF_MONTH, 1);

            int maxDia = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            SpinnerNumberModel modeloDia = (SpinnerNumberModel) spDia.getModel();
            modeloDia.setMaximum(maxDia);

            int diaAtual = (Integer) spDia.getValue();
            if (diaAtual > maxDia) {
                spDia.setValue(maxDia);
            }
        };

        spMes.addChangeListener(e -> atualizarLimiteDia.run());
        spAno.addChangeListener(e -> atualizarLimiteDia.run());
        atualizarLimiteDia.run();

        g.gridx = 0; g.gridy = 0;
        painelCampos.add(lblDia, g);
        g.gridx = 1;
        painelCampos.add(spDia, g);

        g.gridx = 0; g.gridy = 1;
        painelCampos.add(lblMes, g);
        g.gridx = 1;
        painelCampos.add(spMes, g);

        g.gridx = 0; g.gridy = 2;
        painelCampos.add(lblAno, g);
        g.gridx = 1;
        painelCampos.add(spAno, g);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        painelBotoes.setOpaque(false);

        JButton btnCancelar = new BotaoArredondado("Cancelar", VERMELHO_MTEC, Color.WHITE, 14);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setPreferredSize(new Dimension(105, 36));
        TooltipUtils.aplicarTooltipPadrao(btnCancelar);
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnConfirmar = new BotaoArredondado("Confirmar", new Color(80, 180, 180), Color.WHITE, 14);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirmar.setPreferredSize(new Dimension(115, 36));
        btnConfirmar.addActionListener(e -> {
            int dia = (Integer) spDia.getValue();
            int mes = (Integer) spMes.getValue();
            int ano = (Integer) spAno.getValue();

            String dataFormatada = String.format("%02d/%02d/%04d", dia, mes, ano);
            campoDestino.setText(dataFormatada);

            dialog.dispose();
        });

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnConfirmar);

        painelPrincipal.add(painelCampos, BorderLayout.CENTER);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        dialog.setContentPane(painelPrincipal);
        dialog.setVisible(true);
    }

    // FILTRO DE DATA - formata automaticamente enquanto digita

    // Essa classe intercepta o que o usuário digita e formata como DD/MM/AAAA
    static class FiltroData extends DocumentFilter {

        // Chamado quando o usuário digita um caractere novo
        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) return;
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            for (char c : text.toCharArray()) {
                if (Character.isDigit(c)) sb.insert(offset++, c); // só aceito números
            }
            aplicarMascara(fb, sb.toString());
        }

        // Chamado quando o usuário cola texto ou substitui uma seleção
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder sb = new StringBuilder(atual);
            sb.delete(offset, offset + length);
            if (text != null) {
                int pos = offset;
                for (char c : text.toCharArray()) {
                    if (Character.isDigit(c)) sb.insert(pos++, c); // só aceito números
                }
            }
            aplicarMascara(fb, sb.toString());
        }

        // Chamado quando o usuário apaga com Backspace ou Delete
        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder sb = new StringBuilder(atual);
            sb.delete(offset, offset + length);
            aplicarMascara(fb, sb.toString());
        }

        // Formata o texto como DD/MM/AAAA inserindo as barras automaticamente
        private void aplicarMascara(FilterBypass fb, String texto) throws BadLocationException {
            // Primeiro removo tudo que não for número
            String soDigitos = texto.replaceAll("[^0-9]", "");
            if (soDigitos.length() > 8) soDigitos = soDigitos.substring(0, 8); // máximo 8 dígitos

            // Monto a data formatada inserindo / nas posições certas
            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < soDigitos.length(); i++) {
                if (i == 2 || i == 4) formatado.append('/'); // barra depois do dia e do mês
                formatado.append(soDigitos.charAt(i));
            }

            // Substituo o conteúdo do campo pelo texto formatado
            fb.replace(0, fb.getDocument().getLength(), formatado.toString(), null);
        }
    }


    // Filtro simples para aceitar só texto de dinheiro
    static class FiltroMoeda extends DocumentFilter {
        private static final int MAX_DIGITOS_VALOR = 10;

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            if (!textoValido(text)) {
                return;
            }

            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String novoTexto = atual.substring(0, offset)
                + (text == null ? "" : text)
                + atual.substring(offset + length);

            // O banco guarda decimal(10,2), então deixo no máximo 10 números.
            // Pontos, vírgula, R$ e espaços não entram nessa contagem.
            if (contarDigitos(novoTexto) <= MAX_DIGITOS_VALOR) {
                super.replace(fb, offset, length, text == null ? "" : text, attr);
            }
        }

        private boolean textoValido(String texto) {
            // Aceita número, vírgula, ponto, R$, espaço comum e espaço invisível.
            return texto == null || texto.matches("[0-9.,R$\\s\\u00A0]*");
        }

        private int contarDigitos(String texto) {
            int total = 0;
            for (int i = 0; i < texto.length(); i++) {
                if (Character.isDigit(texto.charAt(i))) {
                    total++;
                }
            }
            return total;
        }
    }

    // Tenta carregar a fonte personalizada Exo2 da pasta imagens
    // Se não encontrar, usa uma fonte padrão do sistema
    private void carregarFonteExo() {
        try {
            InputStream is = getClass().getResourceAsStream("/imagens/Exo2-SemiBold.ttf");
            if (is != null) exo2SemiBold = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            exo2SemiBold = new Font("SansSerif", Font.BOLD, 14); // fonte de fallback
        }
    }

}
