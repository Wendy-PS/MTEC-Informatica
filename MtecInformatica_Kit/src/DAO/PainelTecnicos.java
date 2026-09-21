// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Importações necessárias para a tela funcionar
import Model.Tecnico;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import static javax.swing.SwingConstants.CENTER;

// Esse é o painel de Técnicos do sistema MTEC
// Ele fica dentro da TelaPrincipal e mostra o CRUD de técnicos
// Aqui ficam o formulário e a tabela com todos os técnicos cadastrados
public class PainelTecnicos extends JPanel {

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
    private DefaultTableModel modeloTabela;  // modelo de dados da tabela de técnicos
    private JTable tabelaTecnicos;           // tabela principal que mostra os técnicos
    private TableRowSorter<DefaultTableModel> filtroTabela; // filtro usado na pesquisa da tabela
    private JTextField tfPesquisaTecnico;    // campo de pesquisa dos técnicos cadastrados
    private JButton btnInserir;
    private JButton btnAtualizar;
    private JButton btnCancelar;
    
    // ── Campos do formulário — declaro aqui para acessar em qualquer método ──
    private JTextField tfIdTecnico;       // campo do ID do técnico (somente leitura)
    private JTextField tfNome;            // campo do nome do técnico
    private JTextField tfCpf;             // campo do CPF do técnico
    private JTextField tfTelefone;        // campo do telefone do técnico
    private JTextField tfEmail;           // campo do e-mail do técnico
    private JTextField tfLogin;           // campo do login do técnico
    private JPasswordField tfSenha;       // campo da senha do técnico
    private JPasswordField tfConfirmarSenha; // campo para confirmar a senha
    private JComboBox<String> comboEspecialidade;        // campo da especialidade do técnico
    private JComboBox<String> comboStatus;        // campo do status do técnico
    private int idTecnicoEditando = -1;            // guarda o ID do técnico quando estiver editando (-1 = nenhum)

    // ── Construtor: monta todo o painel quando a classe é criada ────
    public PainelTecnicos() {
        carregarFonteExo(); // carrega a fonte antes de qualquer coisa

        // Como esse painel vai entrar dentro da TelaPrincipal, ele não cria janela própria
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 0, 0, 0));

        // ── Container dos dois cards (formulário + tabela) ─────────
        JPanel containerCards = new JPanel(new BorderLayout(0, 20));
        containerCards.setOpaque(false);

        // Card de cima: formulário de cadastro de técnicos
        JPanel cardCrud = criarCardVidro();
        cardCrud.setLayout(new BorderLayout());

        // Uso um painel interno para centralizar melhor os campos do formulário
        // A margem da esquerda compensa o espaço visual da tela e deixa o formulário alinhado com as outras telas
        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setOpaque(false);
        painelFormulario.setBorder(new EmptyBorder(0, 0, 0, 0));
        montarLayoutCrud(painelFormulario);

        cardCrud.add(painelFormulario, BorderLayout.CENTER);

        // Card de baixo: tabela com todos os técnicos
        JPanel cardTabela = criarCardVidro();
        cardTabela.setLayout(new BorderLayout());
        cardTabela.setBorder(new EmptyBorder(10, 10, 10, 10));
        montarTabela(cardTabela);

        containerCards.add(cardCrud, BorderLayout.NORTH);
        containerCards.add(cardTabela, BorderLayout.CENTER);

        add(containerCards, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────
    // FORMULÁRIO DE CADASTRO DE TÉCNICOS
    // ─────────────────────────────────────────────────────────────

    // Monta todos os campos e botões do formulário usando GridBagLayout
    private void montarLayoutCrud(JPanel p) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.anchor = GridBagConstraints.WEST;

        // Título do painel
        JLabel titulo = new JLabel("PAINEL DE TÉCNICOS");
        if (exo2SemiBold != null) titulo.setFont(exo2SemiBold.deriveFont(28f));
        else titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(COR_TITULO);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 6;
        g.anchor = GridBagConstraints.CENTER;
        g.fill = GridBagConstraints.NONE;
        p.add(titulo, g);
        g.anchor = GridBagConstraints.WEST;

      // ── Linha 1: Nome (0,1) | CPF (2,3) | Status (4,5) ──
        g.gridwidth = 1;
        g.gridy = 1;

        // NOME na esquerda (em cima do TELEFONE)
        g.gridx = 0;
        p.add(criarLabel("NOME:"), g);

        tfNome = criarTextField(22);
        LimiteCampos.aplicar(tfNome, 100);
        tfNome.setPreferredSize(new Dimension(250, 42));
        tfNome.setMinimumSize(new Dimension(250, 42));
        tfNome.setToolTipText("Digite o nome completo do técnico");
        g.gridx = 1;
        p.add(tfNome, g);

        // CPF no meio (em cima do E-MAIL)
        g.gridx = 2;
        p.add(criarLabel("CPF:"), g);

        tfCpf = criarTextField(14);
        tfCpf.setPreferredSize(new Dimension(185, 42));
        tfCpf.setMinimumSize(new Dimension(185, 42));
        tfCpf.setToolTipText("Digite o CPF do técnico");
        ((AbstractDocument) tfCpf.getDocument()).setDocumentFilter(new FiltroCpf());
        g.gridx = 3;
        p.add(tfCpf, g);

        // STATUS na direita (em cima da ESPECIALIDADE)
        g.gridx = 4;
        p.add(criarLabel("STATUS:"), g);

        comboStatus = new JComboBox<>(new String[]{"Ativo", "Inativo"});
        comboStatus.setEditable(false);
        comboStatus.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboStatus.setPreferredSize(new Dimension(185, 42)); // Ajustado para alinhar perfeitamente com o CPF
        g.gridx = 5;
        p.add(comboStatus, g);

        // ── Linha 2: Telefone (0,1) | E-mail (2,3) | Especialidade (4,5) ──
        g.gridy = 2;

        // TELEFONE na esquerda
        g.gridx = 0;
        p.add(criarLabel("TELEFONE:"), g);

        tfTelefone = criarTextField(14);
        tfTelefone.setPreferredSize(new Dimension(190, 42));
        tfTelefone.setMinimumSize(new Dimension(190, 42));
        tfTelefone.setToolTipText("Digite o telefone ou celular do técnico");
        ((AbstractDocument) tfTelefone.getDocument()).setDocumentFilter(new FiltroTelefone());
        g.gridx = 1;
        p.add(tfTelefone, g);

        // E-MAIL no meio
        g.gridx = 2;
        p.add(criarLabel("E-MAIL:"), g);

        tfEmail = criarTextField(22);
        LimiteCampos.aplicar(tfEmail, 100);
        tfEmail.setPreferredSize(new Dimension(360, 42));
        tfEmail.setMinimumSize(new Dimension(360, 42));
        tfEmail.setToolTipText("Digite o e-mail do técnico");
        g.gridx = 3;
        p.add(tfEmail, g);

        // ESPECIALIDADE na direita
        g.gridx = 4;
        p.add(criarLabel("ESPECIALIDADE:"), g);

        comboEspecialidade = new JComboBox<>(new String[]{"Selecione", "Hardware", "Software", "Redes", "Sistemas", "Suporte Técnico", "Manutenção de Notebooks"});
        comboEspecialidade.setEditable(false);
        comboEspecialidade.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboEspecialidade.setPreferredSize(new Dimension(250, 42));
        g.gridx = 5;
        p.add(comboEspecialidade, g);

        // ── Linha 3: Login (0,1) | Senha (2,3) | Confirmar (4,5) ──
        g.gridy = 3;

        // LOGIN na esquerda (embaixo de TELEFONE)
        g.gridx = 0; 
        p.add(criarLabel("LOGIN:"), g);
        
        tfLogin = criarTextField(12);
        LimiteCampos.aplicar(tfLogin, 45);
        tfLogin.setPreferredSize(new Dimension(190, 42));
        g.gridx = 1; 
        p.add(tfLogin, g);

        // SENHA no meio (embaixo de E-MAIL)
        g.gridx = 2; 
        p.add(criarLabel("SENHA:"), g);
        
        tfSenha = criarPasswordField(12);
        LimiteCampos.aplicar(tfSenha, 100);
        tfSenha.setPreferredSize(new Dimension(160, 42));
        g.gridx = 3; 
        p.add(criarPainelSenhaComOlho(tfSenha), g);

        // CONFIRMAR na direita (embaixo de ESPECIALIDADE)
        g.gridx = 4; 
        p.add(criarLabel("CONFIRMAR:"), g);
        
        tfConfirmarSenha = criarPasswordField(12);
        LimiteCampos.aplicar(tfConfirmarSenha, 100);
        tfConfirmarSenha.setPreferredSize(new Dimension(160, 42));
        g.gridx = 5; 
        p.add(criarPainelSenhaComOlho(tfConfirmarSenha), g);
        // ── Linha 5: botões de ação ──
        // Deixo os botões com altura maior para não cortar o texto em monitores diferentes
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        painelBotoes.setOpaque(false);
        painelBotoes.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnInserir = criarBotaoAcao("INSERIR", VERDE_MTEC, Color.WHITE);
        btnInserir.addActionListener(e -> onInserir());
        TooltipUtils.aplicarTooltipPadrao(btnInserir);

        btnAtualizar = criarBotaoAcao("ATUALIZAR",
        new Color(60, 130, 180), Color.WHITE);
        btnAtualizar.addActionListener(e -> onAtualizar());
        TooltipUtils.aplicarTooltipPadrao(btnAtualizar);

        // inicia escondido
        btnAtualizar.setVisible(false);

        btnCancelar = criarBotaoAcao("APAGAR",
        VERMELHO_MTEC, Color.WHITE);
        btnCancelar.addActionListener(e -> onCancelar());
        TooltipUtils.aplicarTooltipPadrao(btnCancelar);
        
        painelBotoes.add(btnInserir);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnCancelar);

        g.gridy = 5;
        g.gridx = 0;
        g.gridwidth = 6;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(4, 12, 8, 12);
        p.add(painelBotoes, g);
    }

    // ─────────────────────────────────────────────────────────────
    // TABELA DE TÉCNICOS
    // ─────────────────────────────────────────────────────────────

    // Monta a tabela dentro do card e configura todo o visual dela
    private void montarTabela(JPanel card) {
        String[] colunas = {
            "ID",
            "Nome",
            "CPF",
            "Telefone",
            "E-mail",
            "Especialidade",
            "Status",
            "Ações",
            "Login",
        };
        // Crio o modelo da tabela — só a coluna Ações é editável
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 7; }
        };

        carregarDados();

        // Crio a tabela com cores alternadas nas linhas
        tabelaTecnicos = new JTable(modeloTabela) {
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

        // Crio o filtro da tabela para pesquisar técnico
        filtroTabela = new TableRowSorter<>(modeloTabela);
        tabelaTecnicos.setRowSorter(filtroTabela);
        for (int i = 0; i < modeloTabela.getColumnCount(); i++) filtroTabela.setSortable(i, false);

        // Configurações visuais gerais da tabela
        tabelaTecnicos.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabelaTecnicos.setRowHeight(38);
        tabelaTecnicos.setShowGrid(false);
        tabelaTecnicos.setIntercellSpacing(new Dimension(0, 2));
        tabelaTecnicos.setOpaque(false);
        tabelaTecnicos.setFillsViewportHeight(true);
        tabelaTecnicos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaTecnicos.getTableHeader().setReorderingAllowed(false);

        // Configuro o visual do cabeçalho da tabela
        JTableHeader header = tabelaTecnicos.getTableHeader();
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

        for (int i = 0; i < tabelaTecnicos.getColumnCount(); i++) {
            tabelaTecnicos.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Centralizo o texto de todas as células, menos a coluna de Ações
        DefaultTableCellRenderer cellCenter = new DefaultTableCellRenderer();
        cellCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabelaTecnicos.getColumnCount(); i++) {
            if (i != 7) {
                tabelaTecnicos.getColumnModel().getColumn(i).setCellRenderer(cellCenter);
            }
        }

        // Coluna de Ações com botões Editar/Excluir
        tabelaTecnicos.getColumnModel().getColumn(7).setCellRenderer(new AcoesCellRenderer());
        tabelaTecnicos.getColumnModel().getColumn(7).setCellEditor(new AcoesCellEditor(tabelaTecnicos));
        tabelaTecnicos.getColumnModel().getColumn(7).setPreferredWidth(155);
        tabelaTecnicos.getColumnModel().getColumn(7).setMinWidth(155);

        // Defino a largura preferida das colunas
        int[] larguras = {55, 170, 125, 145, 210, 170, 90, 155, 0};
        for (int i = 0; i < larguras.length && i < tabelaTecnicos.getColumnCount(); i++) {
            tabelaTecnicos.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Escondo a coluna de login
        // Ela fica guardada no modelo e uso quando clicar em Editar
        tabelaTecnicos.getColumnModel().getColumn(8).setMinWidth(0);
        tabelaTecnicos.getColumnModel().getColumn(8).setMaxWidth(0);
        tabelaTecnicos.getColumnModel().getColumn(8).setPreferredWidth(0);
        

        JPanel painelPesquisa = criarPainelPesquisaTabela();
        card.add(painelPesquisa, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tabelaTecnicos);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scroll, BorderLayout.CENTER);
    }

    // Cria o painel de pesquisa da tabela de técnicos
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

        tfPesquisaTecnico = new CampoArredondado();
        LimiteCampos.aplicar(tfPesquisaTecnico, 100);
        tfPesquisaTecnico.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfPesquisaTecnico.setPreferredSize(new Dimension(280, 36));
        tfPesquisaTecnico.setToolTipText("Pesquise por ID, nome, CPF, telefone, e-mail, especialidade ou status");

        // Toda vez que digita, filtro a tabela automaticamente
        tfPesquisaTecnico.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabelaTecnicos();
            }
        });

        JButton btnLimparPesquisa = criarBtnAcao("Limpar", new Color(80, 180, 180), Color.WHITE);
        btnLimparPesquisa.setToolTipText("Limpar o campo de pesquisa.");
        btnLimparPesquisa.setPreferredSize(new Dimension(90, 32));
        btnLimparPesquisa.addActionListener(e -> {
            tfPesquisaTecnico.setText("");
            filtrarTabelaTecnicos();
        });

        g.gridx = 0;
        g.weightx = 0;
        painelPesquisa.add(lblPesquisa, g);

        g.gridx = 1;
        g.weightx = 1;
        painelPesquisa.add(tfPesquisaTecnico, g);

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
    private void filtrarTabelaTecnicos() {
        if (filtroTabela == null || tfPesquisaTecnico == null) {
            return;
        }

        String texto = tfPesquisaTecnico.getText().trim();

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

    // Carrega os dados da tabela buscando do banco via TecnicoDAO
    private void carregarDados() {
        modeloTabela.setRowCount(0);

        try {
            List<Tecnico> lista = new TecnicoDAO().listarTudo();

            for (Tecnico tecnico : lista) {
                modeloTabela.addRow(new Object[]{
                    tecnico.getIdTECNICO(),
                    tecnico.getNome(),
                    tecnico.getCpf(),
                    tecnico.getTelefone(),
                    tecnico.getEmail(),
                    tecnico.getEspecialidade(),
                    tecnico.getStatus(),
                    "",
                    tecnico.getLogin()
                    
                });
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar técnicos: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar técnicos: " + e.getMessage(),
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
        idTecnicoEditando = (int) modeloTabela.getValueAt(linha, 0);
        //tfIdTecnico.setText(String.valueOf(idTecnicoEditando));
        tfNome.setText(valorTabela(linha, 1));
        tfCpf.setText(valorTabela(linha, 2));
        tfTelefone.setText(valorTabela(linha, 3));
        tfEmail.setText(valorTabela(linha, 4));

        comboEspecialidade.setSelectedItem(valorTabela(linha, 5));
        comboStatus.setSelectedItem(valorTabela(linha, 6));

        int id = Integer.parseInt(valorTabela(linha, 0));

        Tecnico tecnico = new TecnicoDAO().buscarPorId(id);

        tfLogin.setText(tecnico.getLogin());

        tfSenha.setText(tecnico.getSenha());
        tfConfirmarSenha.setText(tecnico.getSenha());
        
        btnInserir.setVisible(false);
        btnAtualizar.setVisible(true);
        if (btnCancelar != null) btnCancelar.setText("CANCELAR");
        
        JOptionPane.showMessageDialog(this,
            "Técnico nº " + idTecnicoEditando + " carregado para edição!\nAltere os campos e clique em ATUALIZAR.",
            "Editar Técnico", JOptionPane.INFORMATION_MESSAGE);
    }

    // Pega o valor de uma célula da tabela sem deixar aparecer "null" na tela
    private String valorTabela(int linha, int coluna) {
        Object valor = modeloTabela.getValueAt(linha, coluna);
        return valor == null ? "" : String.valueOf(valor);
    }

    // Ação do botão Excluir — pede confirmação e deleta o técnico do banco
    private void onExcluir(int linha) {
        int idTecnico = (int) modeloTabela.getValueAt(linha, 0);
        String nomeTecnico = String.valueOf(modeloTabela.getValueAt(linha, 1));

        int ok = JOptionPane.showConfirmDialog(this,
            "Deseja excluir o técnico " + nomeTecnico + "?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                new TecnicoDAO().excluir(idTecnico);
                modeloTabela.removeRow(linha);
                JOptionPane.showMessageDialog(this,
                    "Técnico excluído com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                onCancelar();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao excluir técnico: " + e.getMessage() + "\n\nDica: se esse técnico estiver vinculado a uma OS, remova ou altere essa OS primeiro.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // AÇÕES DOS BOTÕES DO FORMULÁRIO
    // ─────────────────────────────────────────────────────────────

    // Chamado quando clica em INSERIR — valida e salva novo técnico no banco
    private void onInserir() {
        try {
            if (!validarCampos()) {
                return;
            }

            Tecnico tecnico = montarTecnicoDoFormulario(false);
            new TecnicoDAO().adicionar(tecnico);

            JOptionPane.showMessageDialog(this,
                "Técnico inserido com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            onCancelar();
            carregarDados();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao inserir técnico: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Chamado quando clica em ATUALIZAR — salva alterações de um técnico existente
    private void onAtualizar() {
        if (idTecnicoEditando == -1) {
            JOptionPane.showMessageDialog(this,
                "Selecione um técnico na tabela para editar!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!validarCampos()) {
                return;
            }

            Tecnico tecnico = montarTecnicoDoFormulario(true);
            new TecnicoDAO().alterar(tecnico);

            JOptionPane.showMessageDialog(this,
                "Técnico atualizado com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            onCancelar();
            carregarDados();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao atualizar técnico: " + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Monta o objeto Tecnico com os dados que estão nos campos da tela
    private Tecnico montarTecnicoDoFormulario(boolean editando) {
        Tecnico tecnico = new Tecnico();

        if (editando) {
            tecnico.setIdTECNICO(idTecnicoEditando);
        }

        tecnico.setNome(tfNome.getText().trim());
        tecnico.setCpf(tfCpf.getText().trim());
        tecnico.setTelefone(tfTelefone.getText().trim());
        tecnico.setEmail(tfEmail.getText().trim());
        tecnico.setEspecialidade(pegarTextoCombo(comboEspecialidade));
        tecnico.setStatus(String.valueOf(comboStatus.getSelectedItem()));
        tecnico.setObservacoes(""); // campo observações foi removido da tela
        tecnico.setLogin(tfLogin.getText().trim());
        tecnico.setSenha(String.valueOf(tfSenha.getPassword()).trim());

        return tecnico;
    }

    // Chamado quando clica em APAGAR — limpa todos os campos do formulário
    private void onCancelar() {
        //tfIdTecnico.setText("");
        tfNome.setText("");
        tfCpf.setText("");
        tfTelefone.setText("");
        tfEmail.setText("");
        tfLogin.setText("");
        tfSenha.setText("");
        tfConfirmarSenha.setText("");
        comboEspecialidade.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        idTecnicoEditando = -1;
        
        btnInserir.setVisible(true);
        btnAtualizar.setVisible(false);
        if (btnCancelar != null) btnCancelar.setText("APAGAR");
    }

    // Valida os campos obrigatórios antes de salvar no banco
    private boolean validarCampos() {
        String nome = tfNome.getText().trim();
        String cpf = tfCpf.getText().trim();
        String telefone = tfTelefone.getText().trim();
        String email = tfEmail.getText().trim();
        String login = tfLogin.getText().trim();
        String senha = String.valueOf(tfSenha.getPassword()).trim();
        String confirmarSenha = String.valueOf(tfConfirmarSenha.getPassword()).trim();
        String especialidade = pegarTextoCombo(comboEspecialidade);

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe o nome do técnico!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfNome.requestFocus();
            return false;
        }

        if (cpf.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe o CPF do técnico!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfCpf.requestFocus();
            return false;
        }

        if (!validarCpf(cpf)) {
            JOptionPane.showMessageDialog(this,
                "CPF inválido! Verifique os números digitados.",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfCpf.requestFocus();
            return false;
        }

        if (!telefone.isEmpty() && !validarTelefone(telefone)) {
            JOptionPane.showMessageDialog(this,
                "Informe um telefone válido com DDD.\nExemplo: (27) 99999-9999",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfTelefone.requestFocus();
            return false;
        }

        if (!email.isEmpty() && !validarEmail(email)) {
            JOptionPane.showMessageDialog(this,
                "Informe um e-mail válido!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfEmail.requestFocus();
            return false;
        }
        if (especialidade.isEmpty() || especialidade.equalsIgnoreCase("Selecione")) {
            JOptionPane.showMessageDialog(this,
                "Informe a especialidade do técnico!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            comboEspecialidade.requestFocus();
            return false;
        }

        if (login.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe o login do técnico!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfLogin.requestFocus();
            return false;
        }

        if (login.contains(" ")) {
            JOptionPane.showMessageDialog(this,
                "O login não pode ter espaços!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfLogin.requestFocus();
            return false;
        }

        // Ao inserir, a senha e a confirmação são obrigatórias.
        // Ao atualizar, se deixar os dois campos vazios, mantém a senha antiga no banco.
        if (idTecnicoEditando == -1 && senha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe a senha do técnico!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfSenha.requestFocus();
            return false;
        }

        if (idTecnicoEditando == -1 && confirmarSenha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Confirme a senha do técnico!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfConfirmarSenha.requestFocus();
            return false;
        }

        // Se estiver atualizando e quiser trocar a senha, precisa preencher os dois campos
        if (idTecnicoEditando != -1 && !senha.isEmpty() && confirmarSenha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Confirme a nova senha do técnico!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfConfirmarSenha.requestFocus();
            return false;
        }

        if (idTecnicoEditando != -1 && senha.isEmpty() && !confirmarSenha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Informe a nova senha antes de confirmar!",
                "Atenção", JOptionPane.WARNING_MESSAGE);
            tfSenha.requestFocus();
            return false;
        }

        if (!senha.isEmpty() || !confirmarSenha.isEmpty()) {
            if (!senha.equals(confirmarSenha)) {
                JOptionPane.showMessageDialog(this,
                    "A senha e a confirmação de senha não são iguais!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                tfConfirmarSenha.requestFocus();
                return false;
            }
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

    // ─────────────────────────────────────────────────────────────
    // VALIDAÇÕES DE CPF, TELEFONE E E-MAIL
    // ─────────────────────────────────────────────────────────────

    // Valida CPF usando o cálculo dos dígitos verificadores
    private boolean validarCpf(String cpfTexto) {
        String cpf = cpfTexto.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return false;
        }

        // Evita CPF com todos os números iguais, tipo 111.111.111-11
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }

            int digito1 = 11 - (soma % 11);
            if (digito1 >= 10) {
                digito1 = 0;
            }

            if (digito1 != Character.getNumericValue(cpf.charAt(9))) {
                return false;
            }

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }

            int digito2 = 11 - (soma % 11);
            if (digito2 >= 10) {
                digito2 = 0;
            }

            return digito2 == Character.getNumericValue(cpf.charAt(10));

        } catch (Exception e) {
            return false;
        }
    }

    // Valida telefone ou celular contando os números digitados
    // Aceito 10 dígitos para fixo e 11 dígitos para celular
    private boolean validarTelefone(String telefoneTexto) {
        String numeros = telefoneTexto.replaceAll("\\D", "");
        return numeros.length() == 10 || numeros.length() == 11;
    }

    // Valida e-mail de forma simples, apenas para evitar formatos muito errados
    private boolean validarEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
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
    // CAMPO DE SENHA ARREDONDADO
    // ─────────────────────────────────────────────────────────────

    // Esse campo é usado para senha e confirmação de senha
    class CampoSenhaArredondado extends JPasswordField {
        private Color corFundo = Color.WHITE;
        private Color corBorda = new Color(0, 200, 200, 150);
        private int arco = 14;

        public CampoSenhaArredondado(int colunas) {
            super(colunas);
            configurarCampo();
        }

        private void configurarCampo() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setBackground(corFundo);
            setSelectionColor(new Color(80, 180, 180));
            setSelectedTextColor(Color.WHITE);
            setEchoChar('•');
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

    // Esse painel desenha o fundo arredondado da área de observações
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

    // Cria o campo de senha com um olhinho do lado
    private JPanel criarPainelSenhaComOlho(JPasswordField campoSenha) {
        JPanel painel = new JPanel(new BorderLayout(5, 0));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(190, 42));
        painel.setMinimumSize(new Dimension(190, 42));

        JLabel lblOlho = new JLabel("👁");
        lblOlho.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblOlho.setForeground(Color.WHITE);
        lblOlho.setHorizontalAlignment(SwingConstants.CENTER);
        lblOlho.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblOlho.setToolTipText("Mostrar senha");
        lblOlho.setPreferredSize(new Dimension(25, 42));

        final boolean[] senhaVisivel = {false};

        lblOlho.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                senhaVisivel[0] = !senhaVisivel[0];

                if (senhaVisivel[0]) {
                    campoSenha.setEchoChar((char) 0);
                    lblOlho.setText("🙈");
                    lblOlho.setToolTipText("Ocultar senha");
                } else {
                    campoSenha.setEchoChar('•');
                    lblOlho.setText("👁");
                    lblOlho.setToolTipText("Mostrar senha");
                }
            }
        });

        painel.add(campoSenha, BorderLayout.CENTER);
        painel.add(lblOlho, BorderLayout.EAST);

        return painel;
    }

    // Cria um JPasswordField estilizado para senha
    private JPasswordField criarPasswordField(int col) {
        JPasswordField pf = new CampoSenhaArredondado(col);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        pf.setPreferredSize(new Dimension(180, 42));
        pf.setMinimumSize(new Dimension(180, 42));
        return pf;
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

    // ─────────────────────────────────────────────────────────────
    // FILTROS DE DIGITAÇÃO
    // ─────────────────────────────────────────────────────────────

    // Filtro para CPF
    // Deixa digitar só números e monta o formato 000.000.000-00
    static class FiltroCpf extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) return;
            replace(fb, offset, 0, text, attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String novoTexto = atual.substring(0, offset) + (text == null ? "" : text) + atual.substring(offset + length);
            String numeros = novoTexto.replaceAll("\\D", "");

            if (numeros.length() > 11) {
                numeros = numeros.substring(0, 11);
            }

            String formatado = formatarCpf(numeros);
            fb.replace(0, fb.getDocument().getLength(), formatado, null);
        }

        private String formatarCpf(String numeros) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < numeros.length(); i++) {
                if (i == 3 || i == 6) sb.append(".");
                if (i == 9) sb.append("-");
                sb.append(numeros.charAt(i));
            }

            return sb.toString();
        }
    }

    // Filtro para telefone/celular
    // Deixa digitar só números e monta o formato (27) 99999-9999
    static class FiltroTelefone extends DocumentFilter {
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) return;
            replace(fb, offset, 0, text, attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String novoTexto = atual.substring(0, offset) + (text == null ? "" : text) + atual.substring(offset + length);
            String numeros = novoTexto.replaceAll("\\D", "");

            if (numeros.length() > 11) {
                numeros = numeros.substring(0, 11);
            }

            String formatado = formatarTelefone(numeros);
            fb.replace(0, fb.getDocument().getLength(), formatado, null);
        }

        private String formatarTelefone(String numeros) {
            StringBuilder sb = new StringBuilder();

            if (numeros.length() > 0) {
                sb.append("(");
            }

            for (int i = 0; i < numeros.length(); i++) {
                if (i == 2) sb.append(") ");
                if (i == 7 && numeros.length() == 11) sb.append("-");
                if (i == 6 && numeros.length() == 10) sb.append("-");
                sb.append(numeros.charAt(i));
            }

            return sb.toString();
        }
    }
}
