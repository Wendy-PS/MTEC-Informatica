// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Importações necessárias para a tela funcionar
import Model.Produto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.regex.Pattern;

// Esse é o painel de Produtos do sistema MTEC
// Na prática, essa tela cadastra equipamentos e peças dos clientes
// Exemplo: Desktop, Notebook, HD, SSD, Fonte, placa-mãe e outras peças avulsas
public class PainelProdutos extends JPanel {

    // ── Cores que uso em toda a tela ──────────────────────────────
    private final Color COR_TITULO        = new Color(0x80CBCB);          // azul claro dos títulos
    private final Color COR_VIDRO_CARDS   = new Color(20, 43, 66, 170);   // fundo dos cards
    private final Color VERDE_MTEC        = new Color(133, 201, 196);     // verde dos botões positivos
    private final Color VERMELHO_MTEC     = new Color(200, 0, 0);         // vermelho dos botões de cancelar/excluir
    private final Color COR_HEADER_TAB    = new Color(30, 90, 110);       // cor sólida do cabeçalho da tabela
    private final Color COR_LINHA_PAR     = new Color(20, 55, 80, 180);   // linhas pares da tabela
    private final Color COR_LINHA_IMPAR   = new Color(12, 38, 58, 160);   // linhas ímpares da tabela
    private final Color COR_SEL           = new Color(80, 180, 180, 120); // cor quando seleciona uma linha

    // ── Variáveis globais da tela ─────────────────────────────────
    private Font exo2SemiBold;               // fonte personalizada do sistema
    private DefaultTableModel modeloTabela;  // modelo de dados da tabela de produtos
    private JTable tabelaProdutos;           // tabela principal que mostra os equipamentos/peças
    private TableRowSorter<DefaultTableModel> filtroTabela; // filtro usado na pesquisa da tabela
    private JTextField tfPesquisaProduto;    // campo de pesquisa dos equipamentos cadastrados
    private JButton btnInserir;
    private JButton btnAtualizar;
    private JButton btnCancelar;
    
    // ── Campos do formulário ─────────────────────────────────────
    private JTextField tfIdProduto;          // campo do ID do produto (somente leitura)
    private JTextField tfClientePesquisa;    // campo para pesquisar cliente
    private JPopupMenu popupClientes;        // popup com resultados da busca de cliente
    private int idClienteSelecionado = -1;   // ID real do cliente selecionado
    private JComboBox<String> comboTipo;
    private JTextField tfOutroTipo; // NOVO
    private JTextField tfMarca;             // marca do equipamento/peça
    private JTextField tfModelo;             // modelo do equipamento/peça
    private JTextArea taEspecificacoes;      // especificações do equipamento/peça
    private int idProdutoEditando = -1;      // guarda o ID do produto quando estiver editando

    // ── Construtor: monta todo o painel quando a classe é criada ────
    public PainelProdutos() {
        carregarFonteExo(); // carrega a fonte antes de qualquer coisa

        // Como esse painel vai entrar dentro da TelaPrincipal, ele não cria janela própria
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 0, 0, 0));

        // ── Container dos dois cards (formulário + tabela) ─────────
        JPanel containerCards = new JPanel(new BorderLayout(0, 20));
        containerCards.setOpaque(false);

        // Card de cima: formulário de cadastro
        JPanel cardCrud = criarCardVidro();
        cardCrud.setLayout(new GridBagLayout());
        montarLayoutCrud(cardCrud);

        // Card de baixo: tabela com todos os produtos/equipamentos
        JPanel cardTabela = criarCardVidro();
        cardTabela.setLayout(new BorderLayout());
        cardTabela.setBorder(new EmptyBorder(10, 10, 10, 10));
        montarTabela(cardTabela);

        containerCards.add(cardCrud, BorderLayout.NORTH);
        containerCards.add(cardTabela, BorderLayout.CENTER);

        add(containerCards, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────
    // FORMULÁRIO DE CADASTRO DE PRODUTOS/EQUIPAMENTOS
    // ─────────────────────────────────────────────────────────────

    // Monta todos os campos e botões do formulário usando GridBagLayout
    private void montarLayoutCrud(JPanel p) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.anchor = GridBagConstraints.WEST;

       // Título do painel
        JLabel titulo = new JLabel("PAINEL DE PRODUTOS");
        if (exo2SemiBold != null) titulo.setFont(exo2SemiBold.deriveFont(28f));
        else titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(COR_TITULO);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 6;
        g.anchor = GridBagConstraints.CENTER;
        g.fill = GridBagConstraints.NONE;
        p.add(titulo, g);

        // ── Linha 1: Cliente | Tipo ──
        g.gridy = 1;

        // Label CLIENTE (Coluna 0) - ALINHADO À DIREITA
        g.gridx = 0;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.EAST; // 👈 Garante que o ":" alinhe à direita
        p.add(criarLabel("CLIENTE:"), g);
        g.anchor = GridBagConstraints.WEST; // Retorna para o padrão à esquerda

        tfClientePesquisa = criarTextField(22);
        LimiteCampos.aplicar(tfClientePesquisa, 100);
        tfClientePesquisa.setPreferredSize(new Dimension(360, 42));
        tfClientePesquisa.setMinimumSize(new Dimension(360, 42));
        tfClientePesquisa.setToolTipText("Digite nome, telefone ou CPF/CNPJ do cliente");

        popupClientes = new JPopupMenu();
        popupClientes.setFocusable(false);

        // Toda vez que digita, busco os clientes parecidos no banco
        tfClientePesquisa.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                pesquisarClientesEnquantoDigita();
            }
        });

        // Caixa de texto do CLIENTE movida para a Coluna 1
        g.gridx = 1;
        g.gridwidth = 1; 
        p.add(tfClientePesquisa, g);

        // Label TIPO reposicionado para a Coluna 2
        g.gridx = 2;
        g.gridwidth = 1;
        p.add(criarLabel("TIPO:"), g);

        comboTipo = new JComboBox<>(new String[]{
            "Selecione", "Desktop", "Notebook", "HD", "SSD", 
            "Fonte", "Placa-mãe", "Placa de vídeo", "Memória RAM", "Outro"
        });
        comboTipo.setEditable(false);
        comboTipo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboTipo.setPreferredSize(new Dimension(210, 42));

        tfOutroTipo = criarTextField(12);
        LimiteCampos.aplicar(tfOutroTipo, 45);
        tfOutroTipo.setPreferredSize(new Dimension(180, 42));
        tfOutroTipo.setVisible(false);

        comboTipo.addActionListener(e -> {
            String tipo = comboTipo.getSelectedItem().toString();
            if (tipo.equals("Outro")) {
                tfOutroTipo.setVisible(true);
                tfOutroTipo.requestFocus();
            } else {
                tfOutroTipo.setVisible(false);
                tfOutroTipo.setText("");
            }
            revalidate();
            repaint();
        });

        JPanel painelTipo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        painelTipo.setOpaque(false);
        painelTipo.add(comboTipo);
        painelTipo.add(tfOutroTipo);

        // Painel do Tipo reposicionado para a Coluna 3
        g.gridx = 3;
        g.gridwidth = 3; 
        p.add(painelTipo, g);


        // ── Linha 2: Marca | Modelo ──
        g.gridy = 2;

        // Label MARCA (Coluna 0) - ALINHADO À DIREITA
        g.gridx = 0;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.EAST; // 👈 Garante que o ":" alinhe à direita
        p.add(criarLabel("MARCA:"), g);
        g.anchor = GridBagConstraints.WEST; // Retorna para o padrão à esquerda

        tfMarca = criarTextField(15);
        LimiteCampos.aplicar(tfMarca, 45);
        tfMarca.setPreferredSize(new Dimension(230, 42));
        tfMarca.setMinimumSize(new Dimension(230, 42));
        tfMarca.setToolTipText("Exemplo: Dell, Lenovo, Kingston, Seagate");
        
        // Caixa de texto da MARCA (Coluna 1)
        g.gridx = 1;
        g.gridwidth = 1;
        p.add(tfMarca, g);

        // Label MODELO (Coluna 2)
        g.gridx = 2;
        g.gridwidth = 1;
        p.add(criarLabel("MODELO:"), g);

        tfModelo = criarTextField(22);
        LimiteCampos.aplicar(tfModelo, 45);
        tfModelo.setPreferredSize(new Dimension(360, 42));
        tfModelo.setMinimumSize(new Dimension(360, 42));
        tfModelo.setToolTipText("Exemplo: XPS 15, A400 480GB, Barracuda 1TB");
        
        // Caixa de texto do MODELO (Coluna 3)
        g.gridx = 3;
        g.gridwidth = 3;
        p.add(tfModelo, g);


        // ── Linha 3: Especificações ──
        g.gridy = 3;

        // Label ESPECIFICAÇÕES (Coluna 0) - ALINHADO À DIREITA
        g.gridx = 0;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.EAST; // 👈 Garante que o ":" alinhe à direita
        p.add(criarLabel("ESPECIFICAÇÕES:"), g);
        g.anchor = GridBagConstraints.WEST; // Retorna para o padrão à esquerda

        taEspecificacoes = new JTextArea(3, 40);
        LimiteCampos.aplicar(taEspecificacoes, 255);
        taEspecificacoes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        taEspecificacoes.setLineWrap(true);
        taEspecificacoes.setWrapStyleWord(true);
        taEspecificacoes.setOpaque(false);
        taEspecificacoes.setBorder(new EmptyBorder(8, 12, 8, 12));
        taEspecificacoes.setSelectionColor(new Color(80, 180, 180));
        taEspecificacoes.setSelectedTextColor(Color.WHITE);

        JScrollPane scrollEspec = new JScrollPane(new AreaTextoArredondada(taEspecificacoes));
        scrollEspec.setPreferredSize(new Dimension(650, 82));
        scrollEspec.setMinimumSize(new Dimension(650, 82));
        scrollEspec.setOpaque(false);
        scrollEspec.getViewport().setOpaque(false);
        scrollEspec.setBorder(BorderFactory.createEmptyBorder());

        // Caixa das especificações começando na Coluna 1
        g.gridx = 1;
        g.gridwidth = 5; 
        p.add(scrollEspec, g);

        // ── Linha 4: botões de ação ──
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        painelBotoes.setOpaque(false);

        btnInserir = criarBotaoAcao("INSERIR", VERDE_MTEC, Color.WHITE);
        btnInserir.addActionListener(e -> onInserir());
        TooltipUtils.aplicarTooltipPadrao(btnInserir);

        btnAtualizar = criarBotaoAcao("ATUALIZAR", new Color(60, 130, 180), Color.WHITE);
        btnAtualizar.addActionListener(e -> onAtualizar());
        TooltipUtils.aplicarTooltipPadrao(btnAtualizar);

        // Esconde o botão atualizar inicialmente
        btnAtualizar.setVisible(false);

        // Botão CANCELAR: limpa todos os campos do formulário
        btnCancelar = criarBotaoAcao("APAGAR", VERMELHO_MTEC, Color.WHITE);
        btnCancelar.addActionListener(e -> onCancelar());
        TooltipUtils.aplicarTooltipPadrao(btnCancelar);

        painelBotoes.add(btnInserir);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnCancelar);

        g.gridy = 4;
        g.gridx = 0;
        g.gridwidth = 6;
        g.anchor = GridBagConstraints.CENTER;
        p.add(painelBotoes, g);
    }

    // ─────────────────────────────────────────────────────────────
    // TABELA DE PRODUTOS/EQUIPAMENTOS
    // ─────────────────────────────────────────────────────────────

    // Monta a tabela dentro do card e configura todo o visual dela
    private void montarTabela(JPanel card) {
        String[] colunas = {"ID", "Cliente", "Tipo", "Marca", "Modelo", "Especificações", "Ações", "ID Cliente"};

        // Crio o modelo da tabela — só a coluna Ações é editável
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 6; }
        };

        carregarDados();

        // Crio a tabela com cores alternadas nas linhas
        tabelaProdutos = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(COR_SEL);
                } else {
                    c.setBackground(row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR);
                }
                c.setForeground(Color.WHITE);
                return c;
            }
        };

        // Crio o filtro da tabela para pesquisar equipamentos.
        // Deixo a ordenação por clique desligada porque a tela já tem barra de pesquisa.
        filtroTabela = new TableRowSorter<>(modeloTabela);
        for (int i = 0; i < modeloTabela.getColumnCount(); i++) {
            filtroTabela.setSortable(i, false);
        }
        tabelaProdutos.setRowSorter(filtroTabela);

        // Configurações visuais gerais da tabela
        tabelaProdutos.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabelaProdutos.setRowHeight(38);
        tabelaProdutos.setShowGrid(false);
        tabelaProdutos.setIntercellSpacing(new Dimension(0, 2));
        tabelaProdutos.setOpaque(false);
        tabelaProdutos.setFillsViewportHeight(true);
        tabelaProdutos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaProdutos.getTableHeader().setReorderingAllowed(false);

        // Configuro o visual do cabeçalho da tabela
        JTableHeader header = tabelaProdutos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(COR_HEADER_TAB);
        header.setForeground(COR_TITULO);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setOpaque(true);

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

        for (int i = 0; i < tabelaProdutos.getColumnCount(); i++) {
            tabelaProdutos.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Centralizo o texto de todas as células, menos especificações e ações
        DefaultTableCellRenderer cellCenter = new DefaultTableCellRenderer();
        cellCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaProdutos.getColumnCount(); i++) {
            if (i != 5 && i != 6) {
                tabelaProdutos.getColumnModel().getColumn(i).setCellRenderer(cellCenter);
            }
        }

        // Coluna de Ações com botões Editar/Excluir
        tabelaProdutos.getColumnModel().getColumn(6).setCellRenderer(new AcoesCellRenderer());
        tabelaProdutos.getColumnModel().getColumn(6).setCellEditor(new AcoesCellEditor(tabelaProdutos));
        tabelaProdutos.getColumnModel().getColumn(6).setPreferredWidth(155);
        tabelaProdutos.getColumnModel().getColumn(6).setMinWidth(155);

        // Defino a largura preferida das colunas
        int[] larguras = {55, 170, 130, 130, 160, 310, 155, 0};
        for (int i = 0; i < larguras.length; i++) {
            tabelaProdutos.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Escondo a coluna ID Cliente, mas ela fica guardada para editar corretamente
        ocultarColuna(tabelaProdutos, 7);

        JPanel painelPesquisa = criarPainelPesquisaTabela();
        card.add(painelPesquisa, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tabelaProdutos);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scroll, BorderLayout.CENTER);
    }

    // Cria o painel de pesquisa da tabela de produtos/equipamentos
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

        tfPesquisaProduto = new CampoArredondado();
        LimiteCampos.aplicar(tfPesquisaProduto, 100);
        tfPesquisaProduto.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfPesquisaProduto.setPreferredSize(new Dimension(280, 36));
        tfPesquisaProduto.setToolTipText("Pesquise por ID, cliente, tipo, marca, modelo ou especificações");

        // Toda vez que digita, filtro a tabela automaticamente
        tfPesquisaProduto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabelaProdutos();
            }
        });

        JButton btnLimparPesquisa = criarBtnAcao("Limpar", new Color(80, 180, 180), Color.WHITE);
        btnLimparPesquisa.setToolTipText("Limpar o campo de pesquisa.");
        btnLimparPesquisa.setPreferredSize(new Dimension(90, 32));
        btnLimparPesquisa.addActionListener(e -> {
            tfPesquisaProduto.setText("");
            filtrarTabelaProdutos();
        });

        g.gridx = 0;
        g.weightx = 0;
        painelPesquisa.add(lblPesquisa, g);

        g.gridx = 1;
        g.weightx = 1;
        painelPesquisa.add(tfPesquisaProduto, g);

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

    // Filtra a tabela usando o texto digitado no campo de pesquisa
    private void filtrarTabelaProdutos() {
        if (filtroTabela == null || tfPesquisaProduto == null) {
            return;
        }

        String texto = tfPesquisaProduto.getText().trim();

        if (texto.isEmpty()) {
            filtroTabela.setRowFilter(null);
            return;
        }

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

    // Carrega os dados da tabela buscando do banco via ProdutoDAO
    private void carregarDados() {
        modeloTabela.setRowCount(0);

        try {
            List<Produto> lista = new ProdutoDAO().listarTudo();

            for (Produto produto : lista) {
                modeloTabela.addRow(new Object[]{
                    produto.getIdPRODUTO(),
                    produto.getNomeCliente(),
                    produto.getTipo(),
                    produto.getMarca(),
                    produto.getModelo(),
                    produto.getEspecificacoes(),
                    "",
                    produto.getClienteIdCliente()
                });
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar produtos/equipamentos: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar produtos/equipamentos: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BOTÕES EDITAR E EXCLUIR DA TABELA
    // ─────────────────────────────────────────────────────────────

    // Esse renderer mostra os botões Editar/Excluir em cada linha
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
            painel.setBackground(sel ? COR_SEL : (row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR));
            return painel;
        }
    }

    // Esse editor ativa os botões quando o usuário clica na célula de Ações
    class AcoesCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JButton btnEdit = criarBtnAcao("Editar",  new Color(80, 180, 180), Color.WHITE);
        private final JButton btnDel  = criarBtnAcao("Excluir", new Color(200, 60, 60),  Color.WHITE);
        private final JTable tabela;
        private int linhaAtual;

        AcoesCellEditor(JTable tabela) {
            this.tabela = tabela;
            painel.setOpaque(true);
            painel.add(btnEdit);
            painel.add(btnDel);

            btnEdit.addActionListener(e -> { fireEditingStopped(); onEditar(linhaAtual); });
            btnDel.addActionListener(e  -> { fireEditingStopped(); onExcluir(linhaAtual); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            linhaAtual = tabela.convertRowIndexToModel(row);
            painel.setBackground(COR_SEL);
            return painel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    // Ação do botão Editar — preenche o formulário com os dados da linha selecionada
    private void onEditar(int linha) {
        idProdutoEditando = (int) modeloTabela.getValueAt(linha, 0);
      
        btnInserir.setVisible(false);
        btnAtualizar.setVisible(true);
        if (btnCancelar != null) btnCancelar.setText("CANCELAR");
        
        //tfIdProduto.setText(String.valueOf(idProdutoEditando));

        tfClientePesquisa.setText(valorTabela(linha, 1));
        idClienteSelecionado = (int) modeloTabela.getValueAt(linha, 7);

        String tipoBanco = valorTabela(linha, 2);

boolean encontrou = false;

for (int i = 0; i < comboTipo.getItemCount(); i++) {

    if (comboTipo.getItemAt(i).equalsIgnoreCase(tipoBanco)) {
        comboTipo.setSelectedIndex(i);
        encontrou = true;
        break;
    }
}

if (!encontrou) {

    comboTipo.setSelectedItem("Outro");

    tfOutroTipo.setVisible(true);
    tfOutroTipo.setText(tipoBanco);
}
        tfMarca.setText(valorTabela(linha, 3));
        tfModelo.setText(valorTabela(linha, 4));
        taEspecificacoes.setText(valorTabela(linha, 5));

        JOptionPane.showMessageDialog(this,
            "Produto nº " + idProdutoEditando + " carregado para edição!\nAltere os campos e clique em ATUALIZAR.",
            "Editar Produto", JOptionPane.INFORMATION_MESSAGE);
    }

    // Pega o valor de uma célula da tabela sem deixar aparecer "null" na tela
    private String valorTabela(int linha, int coluna) {
        Object valor = modeloTabela.getValueAt(linha, coluna);
        return valor == null ? "" : String.valueOf(valor);
    }

    // Ação do botão Excluir — pede confirmação e deleta o produto/equipamento do banco
    private void onExcluir(int linha) {
        int idProduto = (int) modeloTabela.getValueAt(linha, 0);
        String nomeProduto = valorTabela(linha, 2) + " " + valorTabela(linha, 3) + " " + valorTabela(linha, 4);

        int ok = JOptionPane.showConfirmDialog(this,
            "Deseja excluir o produto/equipamento " + nomeProduto.trim() + "?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                new ProdutoDAO().excluir(idProduto);
                onCancelar();
                carregarDados();
                JOptionPane.showMessageDialog(this,
                    "Produto/equipamento excluído com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                onCancelar();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao excluir produto/equipamento: " + e.getMessage() +
                    "\n\nDica: se esse equipamento estiver vinculado a uma OS, remova ou altere essa OS primeiro.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // AÇÕES DOS BOTÕES DO FORMULÁRIO
    // ─────────────────────────────────────────────────────────────

    // Chamado quando clica em INSERIR — valida e salva novo produto/equipamento no banco
    private void onInserir() {
        try {
            if (!validarCampos()) {
                return;
            }

            Produto produto = montarProdutoDoFormulario(false);
            new ProdutoDAO().adicionar(produto);

            JOptionPane.showMessageDialog(this,
                "Produto/equipamento inserido com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            onCancelar();
            carregarDados();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao inserir produto/equipamento: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Chamado quando clica em ATUALIZAR — salva alterações de um produto/equipamento existente
    private void onAtualizar() {
        if (idProdutoEditando == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um produto/equipamento na tabela para editar!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!validarCampos()) {
                return;
            }

            Produto produto = montarProdutoDoFormulario(true);
            new ProdutoDAO().alterar(produto);

            JOptionPane.showMessageDialog(this,
                "Produto/equipamento atualizado com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            onCancelar();
            carregarDados();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao atualizar produto/equipamento: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Monta o objeto Produto com os dados que estão nos campos da tela
    private Produto montarProdutoDoFormulario(boolean editando) {
        Produto produto = new Produto();

        if (editando) {
            produto.setIdPRODUTO(idProdutoEditando);
        }

        produto.setClienteIdCliente(idClienteSelecionado);
        produto.setTipo(pegarTextoTipo());
        produto.setMarca(tfMarca.getText().trim());
        produto.setModelo(tfModelo.getText().trim());
        produto.setEspecificacoes(taEspecificacoes.getText().trim());

        return produto;
    }

    // Chamado quando clica em CANCELAR — limpa todos os campos do formulário
    private void onCancelar() {

    tfClientePesquisa.setText("");
    idClienteSelecionado = -1;
    comboTipo.setSelectedIndex(0);
    tfOutroTipo.setText("");
    tfOutroTipo.setVisible(false);
    tfMarca.setText("");
    tfModelo.setText("");
    taEspecificacoes.setText("");
    idProdutoEditando = -1;

    btnInserir.setVisible(true);
    btnAtualizar.setVisible(false);
    if (btnCancelar != null) btnCancelar.setText("APAGAR");

    if (popupClientes != null) {
        popupClientes.setVisible(false);
    }
}

    // Valida os campos obrigatórios antes de salvar no banco
    private boolean validarCampos() {
        String tipo = pegarTextoTipo();
        String marca = tfMarca.getText().trim();
        String modelo = tfModelo.getText().trim();

        if (comboTipo.getSelectedItem().equals("Outro")
        && tfOutroTipo.getText().trim().isEmpty()) {

        JOptionPane.showMessageDialog(this,
        "Digite o tipo do produto!",
        "Atenção",
        JOptionPane.WARNING_MESSAGE);

        tfOutroTipo.requestFocus();
        return false;
}
        
        if (idClienteSelecionado <= 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um cliente válido na lista de resultados!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfClientePesquisa.requestFocus();
            return false;
        }

        if (tipo.isEmpty() || tipo.equalsIgnoreCase("Selecione")) {
            JOptionPane.showMessageDialog(this,
                "Informe o tipo do equipamento ou peça!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            comboTipo.requestFocus();
            return false;
        }

        if (marca.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe a marca do equipamento ou peça!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfMarca.requestFocus();
            return false;
        }

        if (modelo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe o modelo do equipamento ou peça!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfModelo.requestFocus();
            return false;
            
            
            
        }

        return true;
    }

    // Pega o texto de um ComboBox editável sem deixar retornar null
        private String pegarTextoCombo(JComboBox<String> combo) {
         Object item;

         if (combo.isEditable()) {
             item = combo.getEditor().getItem();
         } else {
             item = combo.getSelectedItem();
         }

         if (item == null) {
             return "";
         }

         return item.toString().trim();
     }

     // NOVO MÉTODO
     private String pegarTextoTipo() {

         String tipo = comboTipo.getSelectedItem().toString();

         if (tipo.equals("Outro")) {
             return tfOutroTipo.getText().trim();
         }

         return tipo;
     }

    // ─────────────────────────────────────────────────────────────
    // PESQUISA DE CLIENTES COM AUTOCOMPLETE
    // ─────────────────────────────────────────────────────────────

    // Pesquisa clientes no banco conforme o usuário digita
    // O usuário pode pesquisar por nome, telefone ou CPF/CNPJ
    private void pesquisarClientesEnquantoDigita() {
        String termo = tfClientePesquisa.getText().trim();

        // Sempre que o usuário digita, limpo o ID selecionado
        // Assim evito salvar produto para cliente errado sem querer
        idClienteSelecionado = -1;

        popupClientes.setVisible(false);
        popupClientes.removeAll();

        if (termo.length() < 1) {
            return;
        }

        String sql = "SELECT idCliente, nome, telefone, cpf_cnpj FROM cliente " +
                     "WHERE nome LIKE ? OR telefone LIKE ? OR cpf_cnpj LIKE ? " +
                     "ORDER BY nome LIMIT 10";

        try (Connection con = ConnectionFactory.getConnection();
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
                        popupClientes.setVisible(false);
                    });

                    popupClientes.add(item);
                }

                if (encontrou) {
                    popupClientes.show(tfClientePesquisa, 0, tfClientePesquisa.getHeight());
                    tfClientePesquisa.requestFocus();
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao pesquisar clientes: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BOTÃO ARREDONDADO DO PAINEL
    // ─────────────────────────────────────────────────────────────

    // Esse botão é usado no formulário, na tabela e na pesquisa
    // Ele tem borda arredondada, transparência e efeito ao passar o mouse
    class BotaoArredondado extends JButton {
        private Color corFundo;
        private Color corTexto;
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
            Color corAtual = hover ? clarearCor(corFundo, 25) : corFundo;

            g2.setColor(new Color(corAtual.getRed(), corAtual.getGreen(), corAtual.getBlue(), hover ? 235 : 210));
            g2.fillRoundRect(1, 1, largura - 3, altura - 3, arco, arco);

            Color corBorda;
            if (corFundo.getRed() > 150 && corFundo.getGreen() < 90) {
                corBorda = new Color(255, 120, 120, 180);
            } else {
                corBorda = new Color(0, 200, 200, 170);
            }

            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 1, largura - 3, altura - 3, arco, arco);

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

    // ─────────────────────────────────────────────────────────────
    // CAMPO DE TEXTO ARREDONDADO
    // ─────────────────────────────────────────────────────────────

    // Esse campo substitui o JTextField padrão do Java
    // Fiz assim para combinar melhor com o visual arredondado do template
    class CampoArredondado extends JTextField {
        private Color corFundo = Color.WHITE;
        private Color corBorda = new Color(0, 200, 200, 150);
        private int arco = 14;

        public CampoArredondado() {
            super();
            configurarCampo();
        }

        public CampoArredondado(int colunas) {
            super(colunas);
            configurarCampo();
        }

        private void configurarCampo() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setBackground(corFundo);
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

    // ─────────────────────────────────────────────────────────────
    // ÁREA DE TEXTO ARREDONDADA
    // ─────────────────────────────────────────────────────────────

    // Esse painel desenha o fundo arredondado da área de especificações
    class AreaTextoArredondada extends JPanel {
        private JTextArea area;

        public AreaTextoArredondada(JTextArea area) {
            super(new BorderLayout());
            this.area = area;
            setOpaque(false);
            setBorder(new EmptyBorder(1, 1, 1, 1));
            add(area, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

            g2.setColor(new Color(0, 200, 200, 150));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

            g2.dispose();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODOS AUXILIARES
    // ─────────────────────────────────────────────────────────────

    // Cria um painel com fundo arredondado semitransparente (efeito vidro)
    private JPanel criarCardVidro() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
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

    // Cria um JLabel estilizado com a fonte e cor padrão dos labels do sistema
    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return l;
    }

    // Cria um JTextField estilizado com tamanho fixo para não encolher na tela
    private JTextField criarTextField(int col) {
        JTextField tf = new CampoArredondado(col);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        tf.setPreferredSize(new Dimension(180, 42));
        tf.setMinimumSize(new Dimension(180, 42));
        return tf;
    }

    // Cria um botão grande e arredondado para usar no formulário
    private JButton criarBotaoAcao(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 18);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setPreferredSize(new Dimension(120, 40));
        b.setMinimumSize(new Dimension(120, 40));
        b.setMaximumSize(new Dimension(120, 40));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return b;
    }

    // Cria um botão pequeno e arredondado para usar dentro da tabela e na pesquisa
    private JButton criarBtnAcao(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 12);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(70, 26));
        return b;
    }

    // Tenta carregar a fonte personalizada Exo2 da pasta imagens
    // Se não encontrar, usa uma fonte padrão do sistema
    private void carregarFonteExo() {
        try {
            InputStream is = getClass().getResourceAsStream("/imagens/Exo2-SemiBold.ttf");
            if (is != null) exo2SemiBold = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            exo2SemiBold = new Font("SansSerif", Font.BOLD, 14);
        }
    }
}
