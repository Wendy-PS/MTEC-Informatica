package DAO;

import Model.Cliente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.regex.Pattern;

public class PainelClientes extends JPanel {

    // ── Cores padronizadas do sistema MTEC ──────────────────────────
    private final Color COR_TITULO        = new Color(0x80CBCB);
    private final Color COR_VIDRO_CARDS   = new Color(20, 43, 66, 170);
    private final Color VERDE_INSERIR     = new Color(133, 201, 196);
    private final Color AZUL_ATUALIZAR    = new Color(60, 130, 180);
    private final Color VERMELHO_CANCEL   = new Color(200, 0, 0);
    private final Color ROXO_ARQUIVO      = new Color(120, 90, 180);
    private final Color COR_HEADER_TAB    = new Color(30, 90, 110);
    private final Color COR_LINHA_PAR     = new Color(20, 55, 80, 180);
    private final Color COR_LINHA_IMPAR   = new Color(12, 38, 58, 160);
    private final Color COR_SEL           = new Color(80, 180, 180, 120);

    private Font exo2SemiBold;

    // ── Componentes do Formulário ──────────────────────────────────
    private CampoArredondado txtNome;
    private CampoArredondado txtCpfCnpj;
    private CampoArredondado txtTelefone;
    private CampoArredondado txtEndereco;
    private CampoArredondado txtPesquisa;

    // ── Componentes da Tabela ──────────────────────────────────────
    private DefaultTableModel modeloTabela;
    private JTable tabela;
    private TableRowSorter<DefaultTableModel> filtroTabela;

    // ── Banco e controle ───────────────────────────────────────────
    private ClienteDAO clienteDAO = new ClienteDAO();
    private int idEditando = -1;
    private JButton btnInserir;
    private JButton btnAtualizar;
    private JButton btnCancelar;
    private JPanel painelBotaoPrincipal;
    private CardLayout cardBotaoPrincipal;

    public PainelClientes() {
        carregarFonteExo();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel containerCards = new JPanel(new BorderLayout(0, 20));
        containerCards.setOpaque(false);

        JPanel cardCrud = criarCardVidro();
        cardCrud.setLayout(new GridBagLayout());
        montarLayoutCrud(cardCrud);

        JPanel cardTabela = criarCardVidro();
        cardTabela.setLayout(new BorderLayout());
        cardTabela.setBorder(new EmptyBorder(10, 10, 10, 10));
        montarTabela(cardTabela);

        containerCards.add(cardCrud, BorderLayout.NORTH);
        containerCards.add(cardTabela, BorderLayout.CENTER);

        add(containerCards, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────
    // MONTAGEM DO FORMULÁRIO
    // ─────────────────────────────────────────────────────────────
private void montarLayoutCrud(JPanel p) {
    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(6, 10, 6, 10);

    JLabel lblTitulo = new JLabel("CLIENTES");
    if (exo2SemiBold != null) lblTitulo.setFont(exo2SemiBold.deriveFont(28f));
    else lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
    lblTitulo.setForeground(COR_TITULO);

    g.gridx = 0;
    g.gridy = 0;
    g.gridwidth = 4;
    g.fill = GridBagConstraints.NONE;
    g.anchor = GridBagConstraints.CENTER;
    g.weightx = 0;
    p.add(lblTitulo, g);

    g.gridy = 1;
    g.gridwidth = 1;

    g.gridx = 0;
    g.weightx = 0;
    g.anchor = GridBagConstraints.EAST;
    g.fill = GridBagConstraints.NONE;
    p.add(criarLabel("NOME:"), g);

    txtNome = criarTextField(20);
    LimiteCampos.aplicar(txtNome, 100);
    txtNome.setToolTipText(
        "<html><b>Nome do Cliente</b><br>Digite o nome completo do cliente.</html>"
    );

    g.gridx = 1;
    g.gridwidth = 2;
    g.weightx = 1.0;
    g.anchor = GridBagConstraints.WEST;
    g.fill = GridBagConstraints.HORIZONTAL;
    p.add(txtNome, g);
    g.gridwidth = 1;

    // Painel principal dos botões da direita.
    // Usei GridBagLayout para deixar a coluna toda centralizada.
    JPanel painelBotoes = new JPanel(new GridBagLayout());
    painelBotoes.setOpaque(false);
    painelBotoes.setPreferredSize(new Dimension(130, 175));

    btnInserir = criarBotaoAcao("INSERIR", VERDE_INSERIR, Color.WHITE);
    btnInserir.addActionListener(e -> onInserir());
    TooltipUtils.aplicarTooltipPadrao(btnInserir);
    btnInserir.setToolTipText("Cadastrar um novo cliente.");

    btnAtualizar = criarBotaoAcao("ATUALIZAR", AZUL_ATUALIZAR, Color.WHITE);
    btnAtualizar.addActionListener(e -> onAtualizar());
    TooltipUtils.aplicarTooltipPadrao(btnAtualizar);

    btnCancelar = criarBotaoAcao("APAGAR", VERMELHO_CANCEL, Color.WHITE);
    btnCancelar.addActionListener(e -> onCancelar());
    TooltipUtils.aplicarTooltipPadrao(btnCancelar);

    JButton btnArquivoMorto = criarBotaoAcao("INATIVOS", ROXO_ARQUIVO, Color.WHITE);
    btnArquivoMorto.addActionListener(e -> abrirArquivoMorto());
    TooltipUtils.aplicarTooltipPadrao(btnArquivoMorto);
    btnArquivoMorto.setToolTipText("Abrir a área de status/clientes inativos.");

    // Inserir e atualizar ficam no mesmo lugar.
    // Quando editar, o sistema troca um pelo outro sem mudar a posição.
    cardBotaoPrincipal = new CardLayout();
    painelBotaoPrincipal = new JPanel(cardBotaoPrincipal);
    painelBotaoPrincipal.setOpaque(false);
    painelBotaoPrincipal.setPreferredSize(new Dimension(120, 40));
    painelBotaoPrincipal.setMinimumSize(new Dimension(120, 40));
    painelBotaoPrincipal.setMaximumSize(new Dimension(120, 40));
    painelBotaoPrincipal.add(btnInserir, "INSERIR");
    painelBotaoPrincipal.add(btnAtualizar, "ATUALIZAR");

    // Caixa interna com espaçamento igual entre todos os botões.
    JPanel caixaBotoes = new JPanel(new GridLayout(3, 1, 0, 14));
    caixaBotoes.setOpaque(false);
    caixaBotoes.setPreferredSize(new Dimension(120, 148));
    caixaBotoes.add(painelBotaoPrincipal);
    caixaBotoes.add(btnCancelar);
    caixaBotoes.add(btnArquivoMorto);

    GridBagConstraints gb = new GridBagConstraints();
    gb.gridx = 0;
    gb.gridy = 0;
    gb.anchor = GridBagConstraints.CENTER;
    painelBotoes.add(caixaBotoes, gb);

    configurarModoEdicao(false);

    g.gridx = 3;
    g.gridy = 1;
    g.gridheight = 4;
    g.weightx = 0;
    g.anchor = GridBagConstraints.CENTER;
    g.fill = GridBagConstraints.NONE;
    g.insets = new Insets(6, 10, 6, 10);
    p.add(painelBotoes, g);

    g.gridheight = 1;
    g.insets = new Insets(6, 10, 6, 10);

    // CPF/CNPJ
    g.gridy = 2;
    g.gridx = 0;
    g.weightx = 0;
    g.anchor = GridBagConstraints.EAST;
    g.fill = GridBagConstraints.NONE;
    p.add(criarLabel("CPF/CNPJ:"), g);

    txtCpfCnpj = criarTextField(20);
    LimiteCampos.aplicar(txtCpfCnpj, 20);
    txtCpfCnpj.setToolTipText(
        "<html><b>CPF ou CNPJ</b><br>Digite um CPF ou CNPJ válido.<br>A formatação será aplicada automaticamente.</html>"
    );

    txtCpfCnpj.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            formatarCpfCnpj();
        }
    });

    g.gridx = 1;
    g.gridwidth = 2;
    g.weightx = 1.0;
    g.anchor = GridBagConstraints.WEST;
    g.fill = GridBagConstraints.HORIZONTAL;
    p.add(txtCpfCnpj, g);
    g.gridwidth = 1;

    // Telefone
    g.gridy = 3;
    g.gridx = 0;
    g.weightx = 0;
    g.anchor = GridBagConstraints.EAST;
    g.fill = GridBagConstraints.NONE;
    p.add(criarLabel("TELEFONE:"), g);

    txtTelefone = criarTextField(20);
    LimiteCampos.aplicar(txtTelefone, 15);
    txtTelefone.setToolTipText(
        "<html><b>Telefone</b><br>Digite o telefone com DDD.<br>Exemplo: (27) 99999-9999</html>"
    );

    txtTelefone.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            formatarTelefone();
        }
    });

    g.gridx = 1;
    g.gridwidth = 2;
    g.weightx = 1.0;
    g.anchor = GridBagConstraints.WEST;
    g.fill = GridBagConstraints.HORIZONTAL;
    p.add(txtTelefone, g);
    g.gridwidth = 1;

    // Endereço
    g.gridy = 4;
    g.gridx = 0;
    g.weightx = 0;
    g.anchor = GridBagConstraints.EAST;
    g.fill = GridBagConstraints.NONE;
    p.add(criarLabel("ENDEREÇO:"), g);

    txtEndereco = criarTextField(20);
    LimiteCampos.aplicar(txtEndereco, 255);
    txtEndereco.setToolTipText(
        "<html><b>Endereço</b><br>Digite o endereço completo do cliente.<br>Ex.: Rua, número, bairro e cidade.</html>"
    );

    g.gridx = 1;
    g.gridwidth = 2;
    g.weightx = 1.0;
    g.anchor = GridBagConstraints.WEST;
    g.fill = GridBagConstraints.HORIZONTAL;
    p.add(txtEndereco, g);
}

    private void configurarModoEdicao(boolean editando) {
    // Troca o botão principal sem mexer na posição dele.
    if (cardBotaoPrincipal != null && painelBotaoPrincipal != null) {
        cardBotaoPrincipal.show(painelBotaoPrincipal, editando ? "ATUALIZAR" : "INSERIR");
    }

    if (btnCancelar != null) {
        btnCancelar.setText(editando ? "CANCELAR" : "APAGAR");
    }

    if (txtCpfCnpj != null) {
        txtCpfCnpj.setEditable(true);
        txtCpfCnpj.setFocusable(true);
    }
}
    private void montarTabela(JPanel card) {
        JPanel painelPesquisa = new JPanel(new GridBagLayout());
        painelPesquisa.setOpaque(false);
        painelPesquisa.setBorder(new EmptyBorder(0, 0, 10, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.insets = new Insets(0, 0, 0, 8); g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblPesquisa = criarLabel("PESQUISAR:");
        lblPesquisa.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.EAST;
        painelPesquisa.add(lblPesquisa, g);

        txtPesquisa = new CampoArredondado();
        LimiteCampos.aplicar(txtPesquisa, 100);
        txtPesquisa.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPesquisa.setPreferredSize(new Dimension(280, 36));
        txtPesquisa.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filtrarTabela(); }
        });
        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST;
        painelPesquisa.add(txtPesquisa, g);

        JButton btnLimpar = new BotaoArredondado("Limpar", new Color(80, 180, 180), Color.WHITE, 12);
        btnLimpar.setToolTipText("Limpar o campo de pesquisa.");
        btnLimpar.setPreferredSize(new Dimension(90, 32));
        btnLimpar.addActionListener(e -> { txtPesquisa.setText(""); filtrarTabela(); });

        JPanel pnlBotaoLimpar = new JPanel(new GridBagLayout());
        pnlBotaoLimpar.setOpaque(false);
        pnlBotaoLimpar.setPreferredSize(new Dimension(170, 36));
        pnlBotaoLimpar.setBorder(new EmptyBorder(0, 0, 0, 42));
        pnlBotaoLimpar.add(btnLimpar);

        g.gridx = 2; g.weightx = 0; g.insets = new Insets(0, 0, 0, 0);
        painelPesquisa.add(pnlBotaoLimpar, g);

        card.add(painelPesquisa, BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome", "CPF/CNPJ", "Telefone", "Endereço", "Status", "Ações"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 6; }
        };

        tabela = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) c.setBackground(COR_SEL);
                else c.setBackground(row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR);
                if (col == 5) c.setForeground(new Color(133, 201, 196));
                else c.setForeground(Color.WHITE);
                return c;
            }
        };

        filtroTabela = new TableRowSorter<>(modeloTabela);
        tabela.setRowSorter(filtroTabela);
        for (int i = 0; i < modeloTabela.getColumnCount(); i++) filtroTabela.setSortable(i, false);

        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabela.setRowHeight(38);
        tabela.setShowGrid(false);
        tabela.setGridColor(new Color(0, 0, 0, 0));
        tabela.setIntercellSpacing(new Dimension(0, 2));
        tabela.setOpaque(false);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);

        personalizarCabecalhoTabela(tabela);

        DefaultTableCellRenderer cellCenter = new DefaultTableCellRenderer();
        cellCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            if (i != 6) tabela.getColumnModel().getColumn(i).setCellRenderer(cellCenter);
        }

        tabela.getColumnModel().getColumn(6).setCellRenderer(new AcoesCellRenderer());
        tabela.getColumnModel().getColumn(6).setCellEditor(new AcoesCellEditor(tabela));
        tabela.getColumnModel().getColumn(6).setPreferredWidth(145);
        tabela.getColumnModel().getColumn(6).setMinWidth(130);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);

        carregarDados();
    }

    private void personalizarCabecalhoTabela(JTable tab) {
        JTableHeader header = tab.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(COR_HEADER_TAB);
        header.setForeground(COR_TITULO);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setOpaque(false);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 15));
                label.setForeground(COR_TITULO);
                label.setBackground(COR_HEADER_TAB);
                label.setBorder(BorderFactory.createEmptyBorder());
                return label;
            }
        });
    }

    private void filtrarTabela() {
        if (filtroTabela == null || txtPesquisa == null) return;
        String texto = txtPesquisa.getText().trim();
        if (texto.isEmpty()) { filtroTabela.setRowFilter(null); return; }
        filtroTabela.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }

    private void abrirArquivoMorto() {
        try {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;
            DialogArquivoMorto dialog = new DialogArquivoMorto(parentFrame);
            dialog.setVisible(true);
            carregarDados(); // recarrega após possível restauração
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir painel de arquivos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onInserir() {
        if (!validarCampos()) return;
        try {
            Cliente cliente = montarClientePelosCampos();
            clienteDAO.adicionar(cliente);
            JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso!");
            onCancelar();
            carregarDados();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAtualizar() {
        if (idEditando == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para atualizar!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCampos()) return;
        try {
            Cliente cliente = montarClientePelosCampos();
            cliente.setIdCliente(idEditando);
            clienteDAO.alterar(cliente);
            JOptionPane.showMessageDialog(this, "Cliente atualizado com sucesso!");
            onCancelar();
            carregarDados();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desativarCliente(int idCliente) {
        int op = JOptionPane.showConfirmDialog(this,
            "Deseja enviar este cliente para o arquivo morto (Inativar)?",
            "Confirmar Desativação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (op != JOptionPane.YES_OPTION) return;
        try {
            clienteDAO.excluir(idCliente);
            JOptionPane.showMessageDialog(this, "Cliente arquivado com sucesso!");
            onCancelar();
            carregarDados();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Não foi possível desativar o cliente.\nErro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Cliente montarClientePelosCampos() {
        Cliente cliente = new Cliente();
        cliente.setNome(txtNome.getText().trim());
        cliente.setCpfCnpj(txtCpfCnpj.getText().trim());
        cliente.setTelefone(txtTelefone.getText().trim());
        cliente.setEndereco(txtEndereco.getText().trim());
        return cliente;
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Informe o nome do cliente."); txtNome.requestFocus(); return false; }
        String numeros = somenteNumeros(txtCpfCnpj.getText());
        if (numeros.isEmpty()) { JOptionPane.showMessageDialog(this, "Informe o CPF ou CNPJ."); txtCpfCnpj.requestFocus(); return false; }
        if (numeros.length() == 11 && !validarCpf(numeros)) { JOptionPane.showMessageDialog(this, "CPF inválido."); txtCpfCnpj.requestFocus(); return false; }
        if (numeros.length() == 14 && !validarCnpj(numeros)) { JOptionPane.showMessageDialog(this, "CNPJ inválido."); txtCpfCnpj.requestFocus(); return false; }
        if (numeros.length() != 11 && numeros.length() != 14) { JOptionPane.showMessageDialog(this, "CPF/CNPJ com tamanho inválido."); txtCpfCnpj.requestFocus(); return false; }
        String telefone = somenteNumeros(txtTelefone.getText());
        if (telefone.length() != 10 && telefone.length() != 11) { JOptionPane.showMessageDialog(this, "Telefone inválido."); txtTelefone.requestFocus(); return false; }
        if (txtEndereco.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Informe o endereço."); txtEndereco.requestFocus(); return false; }
        return true;
    }

    private void onCancelar() {
        txtNome.setText(""); txtCpfCnpj.setText(""); txtTelefone.setText(""); txtEndereco.setText("");
        idEditando = -1; configurarModoEdicao(false); txtNome.requestFocus();
    }

    private void carregarDados() {
        modeloTabela.setRowCount(0);
        try {
            List<Cliente> lista = clienteDAO.listarTudo();
            for (Cliente c : lista) {
                modeloTabela.addRow(new Object[]{ c.getIdCliente(), c.getNome(), c.getCpfCnpj(), c.getTelefone(), c.getEndereco(), "Ativo", "" });
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String somenteNumeros(String texto) { return texto == null ? "" : texto.replaceAll("\\D", ""); }

    private void formatarCpfCnpj() {
        String numeros = somenteNumeros(txtCpfCnpj.getText());
        if (numeros.length() > 14) numeros = numeros.substring(0, 14);
        String formatado = numeros;
        if (numeros.length() <= 11) {
            if (numeros.length() > 9) formatado = numeros.substring(0,3)+"."+numeros.substring(3,6)+"."+numeros.substring(6,9)+"-"+numeros.substring(9);
            else if (numeros.length() > 6) formatado = numeros.substring(0,3)+"."+numeros.substring(3,6)+"."+numeros.substring(6);
            else if (numeros.length() > 3) formatado = numeros.substring(0,3)+"."+numeros.substring(3);
        } else {
            formatado = numeros.substring(0,2)+"."+numeros.substring(2,5)+"."+numeros.substring(5,8)+"/"+numeros.substring(8,12)+"-"+numeros.substring(12);
        }
        aplicarTextoSemDisparar(txtCpfCnpj, formatado);
    }

    private void formatarTelefone() {
        String numeros = somenteNumeros(txtTelefone.getText());
        if (numeros.length() > 11) numeros = numeros.substring(0, 11);
        String formatado = numeros;
        if (numeros.length() == 11) formatado = "("+numeros.substring(0,2)+") "+numeros.substring(2,7)+"-"+numeros.substring(7);
        else if (numeros.length() == 10) formatado = "("+numeros.substring(0,2)+") "+numeros.substring(2,6)+"-"+numeros.substring(6);
        else if (numeros.length() > 2) formatado = "("+numeros.substring(0,2)+") "+numeros.substring(2);
        aplicarTextoSemDisparar(txtTelefone, formatado);
    }

    private void aplicarTextoSemDisparar(JTextField campo, String texto) {
        int pos = texto.length(); campo.setText(texto); campo.setCaretPosition(Math.min(pos, campo.getText().length()));
    }

    private boolean validarCpf(String cpf) {
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            int dig1 = 11 - (soma % 11); if (dig1 >= 10) dig1 = 0;
            if (dig1 != Character.getNumericValue(cpf.charAt(9))) return false;
            soma = 0;
            for (int i = 0; i < 10; i++) soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            int dig2 = 11 - (soma % 11); if (dig2 >= 10) dig2 = 0;
            return dig2 == Character.getNumericValue(cpf.charAt(10));
        } catch (Exception e) { return false; }
    }

    private boolean validarCnpj(String cnpj) {
        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) return false;
        try {
            int[] peso1 = {5,4,3,2,9,8,7,6,5,4,3,2}; int[] peso2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
            int soma = 0;
            for (int i = 0; i < 12; i++) soma += Character.getNumericValue(cnpj.charAt(i)) * peso1[i];
            int dig1 = soma % 11; dig1 = dig1 < 2 ? 0 : 11 - dig1;
            if (dig1 != Character.getNumericValue(cnpj.charAt(12))) return false;
            soma = 0;
            for (int i = 0; i < 13; i++) soma += Character.getNumericValue(cnpj.charAt(i)) * peso2[i];
            int dig2 = soma % 11; dig2 = dig2 < 2 ? 0 : 11 - dig2;
            return dig2 == Character.getNumericValue(cnpj.charAt(13));
        } catch (Exception e) { return false; }
    }

    // ─────────────────────────────────────────────────────────────
    // ELEMENTOS VISUAIS DA TABELA PRINCIPAL
    // ─────────────────────────────────────────────────────────────
    class AcoesCellRenderer implements TableCellRenderer {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        AcoesCellRenderer() {
            painel.setOpaque(true);
            painel.add(criarBtnPequeno("Editar", new Color(80, 180, 180), Color.WHITE));
            painel.add(criarBtnPequeno("Inativar", new Color(200, 60, 60), Color.WHITE));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            painel.setBackground(sel ? COR_SEL : (row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR)); return painel;
        }
    }

    class AcoesCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JButton btnEdit = criarBtnPequeno("Editar", new Color(80, 180, 180), Color.WHITE);
        private final JButton btnDel  = criarBtnPequeno("Inativar", new Color(200, 60, 60), Color.WHITE);
        private final JTable tab; private int linhaAtual;

        AcoesCellEditor(JTable t) {
            this.tab = t; painel.setOpaque(true); painel.add(btnEdit); painel.add(btnDel);
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                idEditando = Integer.parseInt(modeloTabela.getValueAt(linhaAtual, 0).toString());
                txtNome.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 1)));
                txtCpfCnpj.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 2)));
                txtTelefone.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 3)));
                txtEndereco.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 4)));
                configurarModoEdicao(true);
            });
            btnDel.addActionListener(e -> {
                fireEditingStopped();
                int idCliente = Integer.parseInt(modeloTabela.getValueAt(linhaAtual, 0).toString());
                desativarCliente(idCliente);
            });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            linhaAtual = tab.convertRowIndexToModel(row); painel.setBackground(COR_SEL); return painel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    private JButton criarBtnPequeno(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 12); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(75, 26)); TooltipUtils.aplicarTooltipPadrao(b); return b;
    }

    private void carregarFonteExo() {
        try {
            InputStream is = getClass().getResourceAsStream("/imagens/Exo2-SemiBold.ttf");
            if (is != null) exo2SemiBold = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) { exo2SemiBold = new Font("SansSerif", Font.BOLD, 14); }
    }

    private JPanel criarCardVidro() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_VIDRO_CARDS); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); g2.dispose();
            }
        };
        p.setOpaque(false); return p;
    }

    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt); l.setForeground(Color.WHITE); l.setFont(new Font("Segoe UI", Font.BOLD, 19)); return l;
    }

    private CampoArredondado criarTextField(int col) {
        CampoArredondado tf = new CampoArredondado(col); tf.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        tf.setPreferredSize(new Dimension(220, 42)); tf.setMinimumSize(new Dimension(120, 42)); return tf;
    }

    private JButton criarBotaoAcao(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 18); b.setPreferredSize(new Dimension(120, 40)); TooltipUtils.aplicarTooltipPadrao(b);
        b.setMinimumSize(new Dimension(120, 40)); b.setMaximumSize(new Dimension(120, 40)); b.setFont(new Font("Segoe UI", Font.BOLD, 14)); return b;
    }

    class BotaoArredondado extends JButton {
        private Color corFundo, corTexto; private boolean hover; private int arco;
        public BotaoArredondado(String texto, Color corFundo, Color corTexto, int arco) {
            super(texto); this.corFundo = corFundo; this.corTexto = corTexto; this.arco = arco;
            setForeground(corTexto); setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color corAtual = hover ? clarearCor(corFundo, 25) : corFundo;
            g2.setColor(new Color(corAtual.getRed(), corAtual.getGreen(), corAtual.getBlue(), hover ? 235 : 210));
            g2.fillRoundRect(1, 1, getWidth()-3, getHeight()-3, arco, arco);
            Color corBorda = (corFundo.getRed() > 150 && corFundo.getGreen() < 90) ? new Color(255,120,120,180) : new Color(0,200,200,170);
            g2.setColor(corBorda); g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arco, arco);
            g2.setFont(getFont()); g2.setColor(corTexto); FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2; int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y); g2.dispose();
        }
        private Color clarearCor(Color c, int valor) {
            return new Color(Math.min(255, c.getRed()+valor), Math.min(255, c.getGreen()+valor), Math.min(255, c.getBlue()+valor));
        }
    }

    class CampoArredondado extends JTextField {
        private Color corFundo = Color.WHITE, corBorda = new Color(0, 200, 200, 150); private int arco = 14;
        public CampoArredondado() { super(); configurar(); }
        public CampoArredondado(int cols) { super(cols); configurar(); }
        private void configurar() {
            setOpaque(false); setBorder(new EmptyBorder(0, 12, 0, 12)); setBackground(corFundo);
            setSelectionColor(new Color(80, 180, 180)); setSelectedTextColor(Color.WHITE);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corFundo); g2.fillRoundRect(1, 1, getWidth()-3, getHeight()-3, arco, arco); g2.dispose(); super.paintComponent(g);
        }
        @Override protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(corBorda); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arco, arco); g2.dispose();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DIALOG ARQUIVO MORTO — REDESENHADO (MANTÉM TEMA DO SISTEMA)
    // ─────────────────────────────────────────────────────────────
    class DialogArquivoMorto extends JDialog {

        // Cores exclusivas do dialog (variações do tema principal)
        private final Color COR_BG_GRAD_TOPO   = new Color(10, 20, 35);
        private final Color COR_BG_GRAD_BASE   = new Color(5, 12, 22);
        private final Color COR_CARD_FUNDO      = new Color(18, 40, 62, 210);
        private final Color COR_BORDA_CARD      = new Color(0, 180, 180, 100);
        private final Color COR_BORDA_DESTAQUE  = new Color(0, 220, 220, 180);
        private final Color COR_CONTADOR_BG     = new Color(30, 80, 110, 200);
        private final Color VERDE_RESTAURAR     = new Color(80, 200, 130);
        private final Color VERMELHO_EXCLUIR    = new Color(210, 70, 70);
        private final Color COR_BADGE_ATIVO     = new Color(30, 100, 60, 200);
        private final Color COR_BADGE_EXCLUIDO  = new Color(100, 30, 30, 200);

        private JTable tabelaClientesArq, tabelaAgendamentosArq;
        private DefaultTableModel modeloClientesArq, modeloAgendamentosArq;
        private TableRowSorter<DefaultTableModel> sorterClientes, sorterAgendamentos;

        // Labels de contador
        private JLabel lblContClientes, lblContAgendamentos;

        public DialogArquivoMorto(Frame pai) {
            super(pai, "MTEC — Central de Arquivos", true);
            setSize(1020, 680);
            setLocationRelativeTo(pai);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            // ── Fundo com gradiente ──────────────────────────────────────
            JPanel painelRaiz = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, COR_BG_GRAD_TOPO, 0, getHeight(), COR_BG_GRAD_BASE));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            setContentPane(painelRaiz);

            // ── Cabeçalho do dialog ──────────────────────────────────────
            painelRaiz.add(criarCabecalhoDialog(), BorderLayout.NORTH);

            // Corpo da janela.
            // Lista unica de clientes arquivados.
            modeloAgendamentosArq = new DefaultTableModel(
                new Object[]{"ID", "Cliente", "Defeito", "Valor Total", "Status", "Ação"}, 0);

            // Lista de clientes.
            JPanel painelAbaClientes = criarPainelAba();
            painelAbaClientes.add(criarBarraBuscaClientes(), BorderLayout.NORTH);
            painelAbaClientes.add(criarTabelaClientes(), BorderLayout.CENTER);
            painelAbaClientes.add(criarRodapeAba("clientes"), BorderLayout.SOUTH);



            JPanel painelCorpo = new JPanel(new BorderLayout());
            painelCorpo.setOpaque(false);
            painelCorpo.setBorder(new EmptyBorder(0, 14, 14, 14));
            painelCorpo.add(painelAbaClientes, BorderLayout.CENTER);
            painelRaiz.add(painelCorpo, BorderLayout.CENTER);

            carregarDadosArquivados();
        }

        // ── CABEÇALHO ──────────────────────────────────────────────────
        private JPanel criarCabecalhoDialog() {
            JPanel cab = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(new GradientPaint(0, 0, new Color(20, 50, 80), getWidth(), 0, new Color(8, 30, 55)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    // Linha decorativa inferior
                    g2.setColor(new Color(0, 200, 200, 120));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                    g2.dispose();
                }
            };
            cab.setPreferredSize(new Dimension(0, 76));
            cab.setOpaque(false);
            cab.setBorder(new EmptyBorder(0, 20, 0, 20));

            // Ícone + título
            JPanel ladoEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            ladoEsquerdo.setOpaque(false);

            // Ícone de pasta (simulado com label)
            JLabel icone = new JLabel("🗂") { // emoji como ícone
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Círculo de fundo do ícone
                    g2.setColor(new Color(0, 180, 180, 60));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(0, 220, 220, 120));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            icone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            icone.setPreferredSize(new Dimension(52, 52));
            icone.setHorizontalAlignment(SwingConstants.CENTER);
            icone.setVerticalAlignment(SwingConstants.CENTER);

            JPanel textos = new JPanel();
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.setOpaque(false);

            JLabel lblTitulo = new JLabel("CENTRAL DE ARQUIVOS");
            if (exo2SemiBold != null) lblTitulo.setFont(exo2SemiBold.deriveFont(22f));
            else lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
            lblTitulo.setForeground(COR_TITULO);

            JLabel lblSub = new JLabel("Registros inativados — restaure ou exclua permanentemente");
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSub.setForeground(new Color(160, 210, 210));

            textos.add(lblTitulo);
            textos.add(lblSub);

            ladoEsquerdo.add(icone);
            ladoEsquerdo.add(textos);

            // Botão fechar elegante
           cab.add(ladoEsquerdo, BorderLayout.WEST);
return cab;
        }

        // ── ABAS ESTILIZADAS ───────────────────────────────────────────
        private JTabbedPane criarAbaEstilizada() {
            JTabbedPane abas = new JTabbedPane() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(12, 30, 50, 200));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            abas.setFont(new Font("Segoe UI", Font.BOLD, 14));
            abas.setBackground(new Color(15, 40, 65));
            abas.setForeground(Color.WHITE);
            abas.setOpaque(false);

            abas.setUI(new BasicTabbedPaneUI() {
                @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                    // Remove borda padrão das abas
                }
                @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected) {
                        g2.setPaint(new GradientPaint(x, y, new Color(30, 90, 120), x, y+h, new Color(20, 60, 90)));
                        g2.fillRoundRect(x, y, w, h+6, 10, 10);
                        g2.setColor(new Color(0, 200, 200, 150));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawLine(x+2, y+h+5, x+w-2, y+h+5);
                    } else {
                        g2.setColor(new Color(18, 50, 75, 180));
                        g2.fillRoundRect(x, y, w, h, 10, 10);
                    }
                    g2.dispose();
                }
                @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                    // Sem borda extra
                }
            });
            return abas;
        }

        // ── PAINEL DE ABA (FUNDO VIDRO) ───────────────────────────────
        private JPanel criarPainelAba() {
            JPanel p = new JPanel(new BorderLayout(0, 10)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COR_CARD_FUNDO);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.setColor(COR_BORDA_CARD);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
                    g2.dispose();
                }
            };
            p.setOpaque(false);
            p.setBorder(new EmptyBorder(14, 14, 14, 14));
            return p;
        }

        // ── BARRA DE BUSCA — CLIENTES ─────────────────────────────────
        private JPanel criarBarraBuscaClientes() {
            JPanel barra = new JPanel(new BorderLayout(10, 0));
            barra.setOpaque(false);
            barra.setBorder(new EmptyBorder(0, 0, 10, 0));

            // Esquerda: busca
            JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            esq.setOpaque(false);

            JLabel lblBusca = new JLabel("Buscar:");
            lblBusca.setForeground(COR_TITULO);
            lblBusca.setFont(new Font("Segoe UI", Font.BOLD, 14));

            CampoArredondado txtBusca = new CampoArredondado(22);
            LimiteCampos.aplicar(txtBusca, 100);
            txtBusca.setPreferredSize(new Dimension(280, 34));
            txtBusca.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtBusca.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) {
                    String t = txtBusca.getText().trim();
                    sorterClientes.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(t), 1, 2));
                }
            });

            lblContClientes = new JLabel("0 registros");
            lblContClientes.setForeground(new Color(150, 220, 220));
            lblContClientes.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            esq.add(lblBusca);
            esq.add(txtBusca);
            esq.add(Box.createHorizontalStrut(8));
            esq.add(lblContClientes);

            // Direita: limpar todos
            JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            dir.setOpaque(false);

            JButton btnLimpar = criarBotaoDialog("Limpar Todos", VERMELHO_EXCLUIR);
            btnLimpar.setPreferredSize(new Dimension(155, 34));
            btnLimpar.setToolTipText(usuarioAdministrador()
                ? "Excluir permanentemente todos os clientes inativos."
                : "Apenas o ADM pode excluir permanentemente.");
            btnLimpar.addActionListener(e -> limparRegistrosClientes());
            dir.add(btnLimpar);

            barra.add(esq, BorderLayout.WEST);
            barra.add(dir, BorderLayout.EAST);
            return barra;
        }

        // ── TABELA — CLIENTES ─────────────────────────────────────────
        private JScrollPane criarTabelaClientes() {
            modeloClientesArq = new DefaultTableModel(
                new Object[]{"ID", "Nome", "CPF/CNPJ", "Telefone", "Ação"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return col == 4; }
            };

            tabelaClientesArq = criarTabelaEstilizada(modeloClientesArq);
            sorterClientes = new TableRowSorter<>(modeloClientesArq);
            tabelaClientesArq.setRowSorter(sorterClientes);
            for (int i = 0; i < modeloClientesArq.getColumnCount(); i++) sorterClientes.setSortable(i, false);

            tabelaClientesArq.getColumnModel().getColumn(0).setPreferredWidth(50);
            tabelaClientesArq.getColumnModel().getColumn(4).setPreferredWidth(200);
            tabelaClientesArq.getColumnModel().getColumn(4).setMinWidth(200);
            tabelaClientesArq.getColumnModel().getColumn(4).setCellRenderer(new AcoesArquivoCellRenderer());
            tabelaClientesArq.getColumnModel().getColumn(4).setCellEditor(new AcoesArquivoCellEditor(tabelaClientesArq, "Cliente"));
            tabelaClientesArq.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int row = tabelaClientesArq.rowAtPoint(e.getPoint());
                    int col = tabelaClientesArq.columnAtPoint(e.getPoint());
                    if (row < 0 || col == 4) return;
                    int modelRow = tabelaClientesArq.convertRowIndexToModel(row);
                    int idCliente = Integer.parseInt(modeloClientesArq.getValueAt(modelRow, 0).toString());
                    abrirDetalhesCliente(idCliente);
                }
            });
            tabelaClientesArq.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "abrirDetalhes");
            tabelaClientesArq.getActionMap().put("abrirDetalhes", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    int row = tabelaClientesArq.getSelectedRow();
                    if (row < 0) return;
                    int modelRow = tabelaClientesArq.convertRowIndexToModel(row);
                    int idCliente = Integer.parseInt(modeloClientesArq.getValueAt(modelRow, 0).toString());
                    abrirDetalhesCliente(idCliente);
                }
            });

            JScrollPane sc = new JScrollPane(tabelaClientesArq);
            sc.setOpaque(false); sc.getViewport().setOpaque(false);
            sc.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 150, 80)));
            sc.getVerticalScrollBar().setUnitIncrement(16);
            return sc;
        }

        // ── BARRA DE BUSCA — ORÇAMENTOS ───────────────────────────────
        private JPanel criarBarraBuscaAgendamentos() {
            JPanel barra = new JPanel(new BorderLayout(10, 0));
            barra.setOpaque(false);
            barra.setBorder(new EmptyBorder(0, 0, 10, 0));

            JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            esq.setOpaque(false);

            JLabel lblBusca = new JLabel("🔍  Buscar:");
            lblBusca.setForeground(COR_TITULO);
            lblBusca.setFont(new Font("Segoe UI", Font.BOLD, 14));

            CampoArredondado txtBusca = new CampoArredondado(22);
            LimiteCampos.aplicar(txtBusca, 100);
            txtBusca.setPreferredSize(new Dimension(280, 34));
            txtBusca.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtBusca.addKeyListener(new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) {
                    String t = txtBusca.getText().trim();
                    sorterAgendamentos.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(t)));
                }
            });

            lblContAgendamentos = new JLabel("0 registros");
            lblContAgendamentos.setForeground(new Color(150, 220, 220));
            lblContAgendamentos.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            esq.add(lblBusca);
            esq.add(txtBusca);
            esq.add(Box.createHorizontalStrut(8));
            esq.add(lblContAgendamentos);

            barra.add(esq, BorderLayout.WEST);
            return barra;
        }

        // ── TABELA — ORÇAMENTOS ───────────────────────────────────────
        private JScrollPane criarTabelaAgendamentos() {
            modeloAgendamentosArq = new DefaultTableModel(
                new Object[]{"ID", "Cliente", "Defeito", "Valor Total", "Status", "Ação"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return col == 5; }
            };

            tabelaAgendamentosArq = criarTabelaEstilizada(modeloAgendamentosArq);
            sorterAgendamentos = new TableRowSorter<>(modeloAgendamentosArq);
            tabelaAgendamentosArq.setRowSorter(sorterAgendamentos);
            for (int i = 0; i < modeloAgendamentosArq.getColumnCount(); i++) sorterAgendamentos.setSortable(i, false);

            tabelaAgendamentosArq.getColumnModel().getColumn(0).setPreferredWidth(50);
            tabelaAgendamentosArq.getColumnModel().getColumn(5).setPreferredWidth(200);
            tabelaAgendamentosArq.getColumnModel().getColumn(5).setMinWidth(200);
            tabelaAgendamentosArq.getColumnModel().getColumn(5).setCellRenderer(new AcoesArquivoCellRenderer());
            tabelaAgendamentosArq.getColumnModel().getColumn(5).setCellEditor(new AcoesArquivoCellEditor(tabelaAgendamentosArq, "Orçamento"));

            JScrollPane sc = new JScrollPane(tabelaAgendamentosArq);
            sc.setOpaque(false); sc.getViewport().setOpaque(false);
            sc.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 150, 80)));
            sc.getVerticalScrollBar().setUnitIncrement(16);
            return sc;
        }

        // ── RODAPÉ DA ABA ─────────────────────────────────────────────
        private JPanel criarRodapeAba(String tipo) {
            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            rodape.setOpaque(false);

            JLabel info = new JLabel("ℹ  " + (tipo.equals("clientes")
                ? "Restaurar um cliente também reativa seus orçamentos vinculados."
                : "Os orçamentos são restaurados automaticamente ao restaurar o cliente."));
            info.setForeground(new Color(120, 180, 180));
            info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            rodape.add(info);
            return rodape;
        }

        // ── TABELA ESTILIZADA (HELPER) ────────────────────────────────
        private JTable criarTabelaEstilizada(DefaultTableModel modelo) {
            JTable tab = new JTable(modelo) {
                @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component c = super.prepareRenderer(renderer, row, col);
                    if (isRowSelected(row)) c.setBackground(COR_SEL);
                    else c.setBackground(row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR);
                    c.setForeground(Color.WHITE);
                    return c;
                }
            };
            tab.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tab.setRowHeight(40);
            tab.setShowGrid(false);
            tab.setIntercellSpacing(new Dimension(0, 2));
            tab.setOpaque(false);
            tab.setFillsViewportHeight(true);
            tab.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JTableHeader header = tab.getTableHeader();
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
            header.setBackground(COR_HEADER_TAB);
            header.setForeground(COR_TITULO);
            header.setPreferredSize(new Dimension(0, 44));
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 180, 180, 100)));
            header.setOpaque(true);
            header.setReorderingAllowed(false);
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    JLabel l = new JLabel(v == null ? "" : v.toString());
                    l.setOpaque(true); l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    l.setForeground(COR_TITULO); l.setBackground(COR_HEADER_TAB);
                    l.setBorder(new EmptyBorder(0, 8, 0, 8));
                    return l;
                }
            });

            DefaultTableCellRenderer centerRen = new DefaultTableCellRenderer();
            centerRen.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < tab.getColumnCount() - 1; i++) {
                tab.getColumnModel().getColumn(i).setCellRenderer(centerRen);
            }
            return tab;
        }

        // ── CARREGAMENTO DE DADOS ─────────────────────────────────────
        private void carregarDadosArquivados() {
            modeloClientesArq.setRowCount(0);
            if (modeloAgendamentosArq != null) modeloAgendamentosArq.setRowCount(0);

            try (Connection con = new ConnectionFactory().getConnection()) {
                if (con == null) return;

                // Clientes inativos
                String sqlCli = "SELECT idCliente, nome, cpf_cnpj, telefone FROM cliente WHERE ativo = 0 ORDER BY nome";
                try (PreparedStatement pst = con.prepareStatement(sqlCli);
                     ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        modeloClientesArq.addRow(new Object[]{
                            rs.getInt("idCliente"),
                            rs.getString("nome"),
                            rs.getString("cpf_cnpj"),
                            rs.getString("telefone"),
                            ""
                        });
                    }
                } catch (Exception ex) { /* coluna pode não existir ainda */ }

                // Orçamentos vinculados a clientes inativos
                String sqlOrc = "SELECT r.idRELATORIO_ORCAMENTO, c.nome, " +
                    "r.defeito_relatado, r.valor_total_orcamento, r.status_orcamento " +
                    "FROM relatorio_orcamento r " +
                    "INNER JOIN cliente c ON c.idCliente = r.CLIENTE_idCliente " +
                    "WHERE c.ativo = 0 ORDER BY c.nome, r.idRELATORIO_ORCAMENTO DESC";
                try (PreparedStatement pst = con.prepareStatement(sqlOrc);
                     ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        modeloAgendamentosArq.addRow(new Object[]{
                            rs.getInt("idRELATORIO_ORCAMENTO"),
                            rs.getString("nome"),
                            rs.getString("defeito_relatado"),
                            String.format("R$ %.2f", rs.getDouble("valor_total_orcamento")),
                            rs.getString("status_orcamento"),
                            ""
                        });
                    }
                } catch (Exception ex) { /* tabela pode ter nome diferente */ }

            } catch (Exception e) { /* conexão indisponível */ }

            // Atualizar contadores
            int nCli = modeloClientesArq.getRowCount();
            int nOrc = modeloAgendamentosArq.getRowCount();
            if (lblContClientes    != null) lblContClientes.setText(nCli + (nCli == 1 ? " registro" : " registros"));
            if (lblContAgendamentos != null) lblContAgendamentos.setText(nOrc + (nOrc == 1 ? " orçamento" : " orçamentos"));
        }

        // ── AÇÕES: RESTAURAR E EXCLUIR ────────────────────────────────

        // Restaura o cliente arquivado.
        void realizarRestauracaoCliente(int idCliente) {
            int op = JOptionPane.showConfirmDialog(this,
                "<html><b>Restaurar este cliente?</b><br><br>" +
                "O cliente será reativado na tela de Clientes<br>" +
                "e todos os seus Orçamentos vinculados também<br>" +
                "voltarão a aparecer na tela de Orçamentos.</html>",
                "Confirmar Restauração", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (op != JOptionPane.YES_OPTION) return;

            try (Connection con = new ConnectionFactory().getConnection()) {
                // 1) Reativar o cliente
                try (PreparedStatement ps = con.prepareStatement("UPDATE cliente SET ativo = 1 WHERE idCliente = ?")) {
                    ps.setInt(1, idCliente);
                    ps.executeUpdate();
                }
                atualizarAtivoSeColunaExistir(con, "produto", "CLIENTE_idCliente", idCliente, 1);
                atualizarAtivoSeColunaExistir(con, "relatorio_orcamento", "CLIENTE_idCliente", idCliente, 1);
                atualizarAtivoSeColunaExistir(con, "servico", "CLIENTE_idCliente", idCliente, 1);
                JOptionPane.showMessageDialog(this,
                    "✅  Cliente e seus orçamentos foram restaurados com sucesso!\n" +
                    "Eles voltarão a aparecer nas telas de Clientes e Orçamentos.",
                    "Restaurado", JOptionPane.INFORMATION_MESSAGE);
                carregarDadosArquivados();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao restaurar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        void realizarExclusaoUnicaCliente(int idCliente) {
            if (!usuarioAdministrador()) {
                avisarPermissaoAdmin();
                return;
            }

            int op = JOptionPane.showConfirmDialog(this,
                "<html><b>⚠ Excluir PERMANENTEMENTE este cliente?</b><br><br>" +
                "Isso apagará o cliente e <b>todos os seus orçamentos,</b><br>" +
                "<b>ordens de serviço e equipamentos vinculados</b><br>" +
                "de forma irreversível. Esta ação não pode ser desfeita.</html>",
                "Exclusão Permanente", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (op != JOptionPane.YES_OPTION) return;

            try (Connection con = new ConnectionFactory().getConnection()) {
                con.setAutoCommit(false);
                try {
                    // 1) Orçamentos: dependem de cliente, serviço, produto e técnico (FK)
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM relatorio_orcamento WHERE CLIENTE_idCliente = ?")) {
                        ps.setInt(1, idCliente); ps.executeUpdate();
                    }
                    // 2) Ordens de serviço: dependem de cliente, técnico e produto (FK)
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM servico WHERE CLIENTE_idCliente = ?")) {
                        ps.setInt(1, idCliente); ps.executeUpdate();
                    }
                    // 3) Equipamentos/produtos: dependem de cliente (FK fk_produto_cliente)
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM produto WHERE CLIENTE_idCliente = ?")) {
                        ps.setInt(1, idCliente); ps.executeUpdate();
                    }
                    // 4) Por fim, o cliente
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM cliente WHERE idCliente = ?")) {
                        ps.setInt(1, idCliente);
                        if (ps.executeUpdate() > 0) {
                            con.commit();
                            JOptionPane.showMessageDialog(this, "Cliente excluído permanentemente.");
                            carregarDadosArquivados();
                        } else {
                            con.rollback();
                        }
                    }
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        void realizarRestauracaoOrcamento(int idOrcamento) {
            JOptionPane.showMessageDialog(this,
                "<html>Para restaurar um orçamento, restaure o <b>cliente</b> vinculado.<br>" +
                "O orçamento voltará automaticamente na tela de Orçamentos.</html>",
                "Dica", JOptionPane.INFORMATION_MESSAGE);
        }

        void realizarExclusaoUnicaOrcamento(int idOrcamento) {
            int op = JOptionPane.showConfirmDialog(this,
                "Excluir permanentemente o orçamento #" + idOrcamento + "?",
                "Exclusão Permanente", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (op != JOptionPane.YES_OPTION) return;

            try (Connection con = new ConnectionFactory().getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM relatorio_orcamento WHERE idRELATORIO_ORCAMENTO = ?")) {
                    ps.setInt(1, idOrcamento);
                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Orçamento excluído.");
                        carregarDadosArquivados();
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void limparRegistrosClientes() {
            if (!usuarioAdministrador()) {
                avisarPermissaoAdmin();
                return;
            }

            int op = JOptionPane.showConfirmDialog(this,
                "<html><b>⚠ ATENÇÃO: Limpar TODOS os registros arquivados?</b><br><br>" +
                "Isso excluirá <b>permanentemente</b> todos os clientes inativos<br>" +
                "e todos os orçamentos, ordens de serviço e equipamentos<br>" +
                "vinculados a eles.<br><br>" +
                "<span style='color:red'>Esta ação NÃO pode ser desfeita!</span></html>",
                "Limpeza Total", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (op != JOptionPane.YES_OPTION) return;

            try (Connection con = new ConnectionFactory().getConnection()) {
                con.setAutoCommit(false);
                try {
                    // 1) Orçamentos dos clientes inativos
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM relatorio_orcamento WHERE CLIENTE_idCliente IN (SELECT idCliente FROM cliente WHERE ativo = 0)")) {
                        ps.executeUpdate();
                    }
                    // 2) Ordens de serviço dos clientes inativos
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM servico WHERE CLIENTE_idCliente IN (SELECT idCliente FROM cliente WHERE ativo = 0)")) {
                        ps.executeUpdate();
                    }
                    // 3) Equipamentos/produtos dos clientes inativos (FK fk_produto_cliente)
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM produto WHERE CLIENTE_idCliente IN (SELECT idCliente FROM cliente WHERE ativo = 0)")) {
                        ps.executeUpdate();
                    }
                    // 4) Por fim, os clientes inativos
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM cliente WHERE ativo = 0")) {
                        int linhas = ps.executeUpdate();
                        con.commit();
                        JOptionPane.showMessageDialog(this,
                            linhas + " cliente(s) e todos os seus orçamentos, ordens de serviço e equipamentos foram removidos permanentemente.");
                        carregarDadosArquivados();
                    }
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        // ── RENDERERS E EDITORS DE AÇÕES ─────────────────────────────

        private void atualizarAtivoSeColunaExistir(Connection con, String tabela, String colunaCliente, int idCliente, int ativo)
                throws Exception {
            if (!colunaExiste(con, tabela, "ativo")) return;

            String sql = "UPDATE " + tabela + " SET ativo = ? WHERE " + colunaCliente + " = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, ativo);
                ps.setInt(2, idCliente);
                ps.executeUpdate();
            }
        }

        private boolean usuarioAdministrador() {
            return TelaPrincipal.Sessao.login != null
                && TelaPrincipal.Sessao.login.trim().equalsIgnoreCase("admin");
        }

        private void avisarPermissaoAdmin() {
            JOptionPane.showMessageDialog(this,
                "Apenas o ADM pode excluir clientes permanentemente.",
                "Acesso restrito", JOptionPane.WARNING_MESSAGE);
        }

        private boolean colunaExiste(Connection con, String tabela, String coluna) throws Exception {
            String catalog = con.getCatalog();
            try (ResultSet rs = con.getMetaData().getColumns(catalog, null, tabela, coluna)) {
                if (rs.next()) return true;
            }
            try (ResultSet rs = con.getMetaData().getColumns(catalog, null, tabela.toUpperCase(), coluna)) {
                if (rs.next()) return true;
            }
            try (ResultSet rs = con.getMetaData().getColumns(catalog, null, tabela.toLowerCase(), coluna)) {
                return rs.next();
            }
        }

        private void abrirDetalhesCliente(int idCliente) {
            DialogDetalhesCliente detalhes = new DialogDetalhesCliente(idCliente);
            detalhes.setVisible(true);
        }

        class DialogDetalhesCliente extends JDialog {
            private final int idCliente;
            private JPanel conteudo;

            DialogDetalhesCliente(int idCliente) {
                super(DialogArquivoMorto.this, "Detalhes do cliente arquivado", true);
                this.idCliente = idCliente;
                setSize(1040, 720);
                setLocationRelativeTo(DialogArquivoMorto.this);
                setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                montarTela();
                carregarDetalhes();
            }

            private void montarTela() {
                JPanel raiz = new JPanel(new BorderLayout(0, 12)) {
                    @Override protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setPaint(new GradientPaint(0, 0, new Color(10, 24, 39), 0, getHeight(), new Color(4, 13, 25)));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                };
                raiz.setBorder(new EmptyBorder(18, 18, 18, 18));
                setContentPane(raiz);

                JPanel topo = new JPanel(new BorderLayout());
                topo.setOpaque(false);

                JPanel textos = new JPanel();
                textos.setOpaque(false);
                textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

                JLabel titulo = new JLabel("DETALHES DO CLIENTE ARQUIVADO");
                titulo.setForeground(COR_TITULO);
                titulo.setFont(exo2SemiBold != null ? exo2SemiBold.deriveFont(22f) : new Font("Segoe UI", Font.BOLD, 22));

                JLabel sub = new JLabel("Dados cadastrais, equipamentos, orcamentos e ordens de servico vinculadas");
                sub.setForeground(new Color(160, 210, 210));
                sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                textos.add(titulo);
                textos.add(sub);
                topo.add(textos, BorderLayout.WEST);

                JButton fechar = criarBotaoDialog("Fechar", new Color(80, 130, 150));
                fechar.setPreferredSize(new Dimension(100, 34));
                fechar.addActionListener(e -> dispose());
                topo.add(fechar, BorderLayout.EAST);
                raiz.add(topo, BorderLayout.NORTH);

                conteudo = new JPanel();
                conteudo.setOpaque(false);
                conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));

                JScrollPane sc = new JScrollPane(conteudo);
                sc.setOpaque(false);
                sc.getViewport().setOpaque(false);
                sc.setBorder(BorderFactory.createLineBorder(new Color(0, 180, 180, 80)));
                sc.getVerticalScrollBar().setUnitIncrement(18);
                raiz.add(sc, BorderLayout.CENTER);
            }

            private void carregarDetalhes() {
                conteudo.removeAll();

                try (Connection con = new ConnectionFactory().getConnection()) {
                    adicionarDadosCliente(con);
                    adicionarProdutos(con);
                    adicionarOrcamentos(con);
                    adicionarServicos(con);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Erro ao carregar detalhes: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }

                conteudo.revalidate();
                conteudo.repaint();
            }

            private void adicionarDadosCliente(Connection con) throws Exception {
                JPanel secao = criarSecaoDetalhe("Dados do cliente");
                JPanel grid = new JPanel(new GridLayout(0, 2, 10, 8));
                grid.setOpaque(false);

                String sql = "SELECT idCliente, nome, cpf_cnpj, telefone, endereco, ativo FROM cliente WHERE idCliente = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idCliente);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            grid.add(criarInfoDetalhe("ID", rs.getString("idCliente")));
                            grid.add(criarInfoDetalhe("Status", rs.getInt("ativo") == 1 ? "Ativo" : "Inativo"));
                            grid.add(criarInfoDetalhe("Nome", rs.getString("nome")));
                            grid.add(criarInfoDetalhe("CPF/CNPJ", rs.getString("cpf_cnpj")));
                            grid.add(criarInfoDetalhe("Telefone", rs.getString("telefone")));
                            grid.add(criarInfoDetalhe("Endereco", rs.getString("endereco")));
                        }
                    }
                }

                secao.add(grid, BorderLayout.CENTER);
                adicionarSecao(secao, 178);
            }

            private void adicionarProdutos(Connection con) throws Exception {
                DefaultTableModel modelo = criarModeloDetalhe("ID", "Tipo", "Marca", "Modelo", "Especificações");
                String sql = "SELECT idPRODUTO, tipo, marca, modelo, especificacoes FROM produto " +
                    "WHERE CLIENTE_idCliente = ? ORDER BY idPRODUTO DESC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idCliente);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            modelo.addRow(new Object[]{
                                rs.getInt("idPRODUTO"),
                                texto(rs.getString("tipo")),
                                texto(rs.getString("marca")),
                                texto(rs.getString("modelo")),
                                textoCurto(rs.getString("especificacoes"), 38)
                            });
                        }
                    }
                }
                adicionarTabela("Produtos / equipamentos", modelo, "Nenhum produto vinculado.");
            }

            private void adicionarOrcamentos(Connection con) throws Exception {
                DefaultTableModel modelo = criarModeloDetalhe("ID", "Abertura", "Prazo", "Status", "Produto", "Técnico", "Defeito", "Valor");
                String sql =
                    "SELECT r.idRELATORIO_ORCAMENTO, r.abertura, r.validade, r.status_orcamento, " +
                    "r.defeito_relatado, r.valor_total_orcamento, " +
                    "COALESCE(NULLIF(TRIM(CONCAT_WS(' ', p.tipo, p.marca, p.modelo)), ''), 'Sem produto') AS produto, " +
                    "COALESCE(t.nome, 'Sem tecnico') AS tecnico " +
                    "FROM relatorio_orcamento r " +
                    "LEFT JOIN produto p ON p.idPRODUTO = r.PRODUTO_idPRODUTO " +
                    "LEFT JOIN tecnico t ON t.idTECNICO = r.TECNICO_idTECNICO " +
                    "WHERE r.CLIENTE_idCliente = ? ORDER BY r.idRELATORIO_ORCAMENTO DESC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idCliente);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            modelo.addRow(new Object[]{
                                rs.getInt("idRELATORIO_ORCAMENTO"),
                                texto(rs.getString("abertura")),
                                texto(rs.getString("validade")),
                                textoCurto(rs.getString("status_orcamento"), 18),
                                textoCurto(rs.getString("produto"), 28),
                                textoCurto(rs.getString("tecnico"), 24),
                                textoCurto(rs.getString("defeito_relatado"), 32),
                                moeda(rs.getDouble("valor_total_orcamento"))
                            });
                        }
                    }
                }
                adicionarTabela("Orçamentos", modelo, "Nenhum orçamento vinculado.");
            }

            private void adicionarServicos(Connection con) throws Exception {
                DefaultTableModel modelo = criarModeloDetalhe("ID OS", "Abertura", "Prazo", "Conclusão", "Status", "Produto", "Técnico", "Valor");
                String sql =
                    "SELECT s.idSERVICO, s.data_abertura, s.prazo_entrega, s.data_conclusao, s.status_os, " +
                    "COALESCE(NULLIF(TRIM(CONCAT_WS(' ', p.tipo, p.marca, p.modelo)), ''), 'Sem produto') AS produto, " +
                    "COALESCE(t.nome, 'Sem tecnico') AS tecnico, " +
                    "COALESCE(r.valor_total_orcamento, 0) AS valor_servico " +
                    "FROM servico s " +
                    "LEFT JOIN produto p ON p.idPRODUTO = s.PRODUTO_idPRODUTO " +
                    "LEFT JOIN tecnico t ON t.idTECNICO = s.TECNICO_idTECNICO " +
                    "LEFT JOIN relatorio_orcamento r ON r.SERVICO_idSERVICO = s.idSERVICO " +
                    "WHERE s.CLIENTE_idCliente = ? ORDER BY s.idSERVICO DESC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idCliente);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            modelo.addRow(new Object[]{
                                rs.getInt("idSERVICO"),
                                texto(rs.getString("data_abertura")),
                                texto(rs.getString("prazo_entrega")),
                                texto(rs.getString("data_conclusao")),
                                textoCurto(rs.getString("status_os"), 20),
                                textoCurto(rs.getString("produto"), 28),
                                textoCurto(rs.getString("tecnico"), 24),
                                moeda(rs.getDouble("valor_servico"))
                            });
                        }
                    }
                }
                adicionarTabela("Ordens de servico", modelo, "Nenhuma ordem de servico vinculada.");
            }

            private JPanel criarSecaoDetalhe(String titulo) {
                JPanel secao = new JPanel(new BorderLayout(0, 10)) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(COR_CARD_FUNDO);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                        g2.setColor(COR_BORDA_CARD);
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
                        g2.dispose();
                    }
                };
                secao.setOpaque(false);
                secao.setBorder(new EmptyBorder(14, 14, 14, 14));
                JLabel lbl = new JLabel(titulo);
                lbl.setForeground(COR_TITULO);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
                secao.add(lbl, BorderLayout.NORTH);
                return secao;
            }

            private JPanel criarInfoDetalhe(String titulo, String valor) {
                JPanel p = new JPanel(new BorderLayout(0, 2));
                p.setOpaque(false);
                JLabel t = new JLabel(titulo.toUpperCase());
                t.setForeground(new Color(150, 210, 210));
                t.setFont(new Font("Segoe UI", Font.BOLD, 11));
                JLabel v = new JLabel(texto(valor));
                v.setForeground(Color.WHITE);
                v.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                p.add(t, BorderLayout.NORTH);
                p.add(v, BorderLayout.CENTER);
                return p;
            }

            private DefaultTableModel criarModeloDetalhe(String... colunas) {
                return new DefaultTableModel(colunas, 0) {
                    @Override public boolean isCellEditable(int row, int col) { return false; }
                };
            }

            private void adicionarTabela(String titulo, DefaultTableModel modelo, String mensagemVazia) {
                if (modelo.getRowCount() == 0) adicionarLinhaVazia(modelo, mensagemVazia);
                JPanel secao = criarSecaoDetalhe(titulo);
                JTable tab = criarTabelaDetalhe(modelo);
                JScrollPane sc = new JScrollPane(tab);
                sc.setOpaque(false);
                sc.getViewport().setOpaque(false);
                sc.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 150, 70)));
                sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                secao.add(sc, BorderLayout.CENTER);
                int altura = Math.max(142, Math.min(230, 72 + modelo.getRowCount() * 32));
                adicionarSecao(secao, altura);
            }

            private JTable criarTabelaDetalhe(DefaultTableModel modelo) {
                JTable tab = new JTable(modelo) {
                    @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                        Component c = super.prepareRenderer(renderer, row, col);
                        c.setBackground(row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR);
                        c.setForeground(Color.WHITE);
                        if (c instanceof JLabel) {
                            ((JLabel) c).setToolTipText(getValueAt(row, col) == null ? "" : getValueAt(row, col).toString());
                        }
                        return c;
                    }
                };
                tab.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                tab.setRowHeight(30);
                tab.setShowGrid(false);
                tab.setIntercellSpacing(new Dimension(0, 2));
                tab.setOpaque(false);
                tab.setFillsViewportHeight(true);
                tab.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                JTableHeader header = tab.getTableHeader();
                header.setFont(new Font("Segoe UI", Font.BOLD, 12));
                header.setBackground(COR_HEADER_TAB);
                header.setForeground(COR_TITULO);
                header.setPreferredSize(new Dimension(0, 34));
                ajustarColunasDetalhe(tab);
                return tab;
            }

            private void ajustarColunasDetalhe(JTable tab) {
                for (int i = 0; i < tab.getColumnCount(); i++) {
                    String nome = tab.getColumnName(i);
                    int largura = 110;
                    if (nome.startsWith("ID")) largura = 58;
                    else if ("Abertura".equals(nome) || "Prazo".equals(nome) || "Conclusão".equals(nome)) largura = 92;
                    else if ("Status".equals(nome) || "Tipo".equals(nome) || "Marca".equals(nome) || "Modelo".equals(nome)) largura = 105;
                    else if ("Produto".equals(nome) || "Técnico".equals(nome)) largura = 130;
                    else if ("Defeito".equals(nome) || "Especificações".equals(nome)) largura = 180;
                    else if ("Valor".equals(nome)) largura = 78;
                    tab.getColumnModel().getColumn(i).setPreferredWidth(largura);
                }
            }

            private void adicionarSecao(JPanel secao, int altura) {
                secao.setAlignmentX(Component.LEFT_ALIGNMENT);
                secao.setPreferredSize(new Dimension(900, altura));
                secao.setMaximumSize(new Dimension(Integer.MAX_VALUE, altura));
                conteudo.add(secao);
                conteudo.add(Box.createVerticalStrut(12));
            }

            private void adicionarLinhaVazia(DefaultTableModel modelo, String texto) {
                Object[] linha = new Object[modelo.getColumnCount()];
                for (int i = 0; i < linha.length; i++) linha[i] = "";
                if (linha.length > 0) linha[0] = "-";
                if (linha.length > 1) linha[1] = texto;
                modelo.addRow(linha);
            }

            private String texto(String valor) {
                return valor == null || valor.trim().isEmpty() ? "-" : valor.trim();
            }

            private String textoCurto(String valor, int limite) {
                String texto = texto(valor);
                if (texto.length() <= limite) return texto;
                return texto.substring(0, Math.max(0, limite - 3)) + "...";
            }

            private String moeda(double valor) {
                return String.format("R$ %.2f", valor);
            }
        }

        class AcoesArquivoCellRenderer implements TableCellRenderer {
            private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 5));
            AcoesArquivoCellRenderer() {
                painel.setOpaque(true);
                painel.add(criarBtnDialogRenderer("Reativar", VERDE_RESTAURAR));
                painel.add(criarBtnDialogRenderer("Excluir",  VERMELHO_EXCLUIR));
            }
            private JButton criarBtnDialogRenderer(String texto, Color cor) {
                JButton b = new JButton(texto) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 40));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(cor);
                        g2.setStroke(new BasicStroke(1.3f));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        g2.setColor(Color.WHITE);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()-fm.getHeight())/2+fm.getAscent());
                        g2.dispose();
                    }
                };
                b.setPreferredSize(new Dimension(88, 28)); b.setContentAreaFilled(false);
                b.setBorderPainted(false); b.setFocusPainted(false); b.setForeground(Color.WHITE);
                return b;
            }
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                painel.setBackground(sel ? COR_SEL : (row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR));
                return painel;
            }
        }

        class AcoesArquivoCellEditor extends AbstractCellEditor implements TableCellEditor {
            private final JPanel   painel         = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 5));
            private final JButton  btnRestaurar;
            private final JButton  btnExcluir;
            private final JTable   tab;
            private final String   tipo;
            private int            linhaAtual;

            AcoesArquivoCellEditor(JTable t, String tipoAcao) {
                this.tab  = t;
                this.tipo = tipoAcao;
                painel.setOpaque(true);

                btnRestaurar = criarBotaoDialog("Restaurar", VERDE_RESTAURAR);
                btnExcluir   = criarBotaoDialog("Excluir",  VERMELHO_EXCLUIR);
                btnExcluir.setToolTipText(usuarioAdministrador()
                    ? "Excluir permanentemente."
                    : "Apenas o ADM pode excluir permanentemente.");

                btnRestaurar.addActionListener(e -> {
                    fireEditingStopped();
                    int id = Integer.parseInt(tab.getModel().getValueAt(linhaAtual, 0).toString());
                    if ("Cliente".equals(tipo))    DialogArquivoMorto.this.realizarRestauracaoCliente(id);
                    else                           DialogArquivoMorto.this.realizarRestauracaoOrcamento(id);
                });

                btnExcluir.addActionListener(e -> {
                    fireEditingStopped();
                    // Esta trava fica aqui e também dentro do método de excluir.
                    // Assim, mesmo que alguém tente chamar a ação por outro caminho, técnico não apaga cliente.
                    if ("Cliente".equals(tipo) && !usuarioAdministrador()) {
                        DialogArquivoMorto.this.avisarPermissaoAdmin();
                        return;
                    }
                    int id = Integer.parseInt(tab.getModel().getValueAt(linhaAtual, 0).toString());
                    if ("Cliente".equals(tipo))    DialogArquivoMorto.this.realizarExclusaoUnicaCliente(id);
                    else                           DialogArquivoMorto.this.realizarExclusaoUnicaOrcamento(id);
                });

                painel.add(btnRestaurar);
                painel.add(btnExcluir);
            }

            private JButton criarBotaoDialog(String texto, Color cor) {
                JButton b = new JButton(texto) {
                    private boolean h = false;
                    { setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                      setForeground(Color.WHITE); setCursor(new Cursor(Cursor.HAND_CURSOR));
                      addMouseListener(new MouseAdapter() {
                          public void mouseEntered(MouseEvent e) { h = true; repaint(); }
                          public void mouseExited (MouseEvent e) { h = false; repaint(); }
                      });
                    }
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int alpha = h ? 100 : 40;
                        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), alpha));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(h ? cor.brighter() : cor);
                        g2.setStroke(new BasicStroke(h ? 1.8f : 1.3f));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        g2.setColor(Color.WHITE);
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()-fm.getHeight())/2+fm.getAscent());
                        g2.dispose();
                    }
                };
                b.setPreferredSize(new Dimension(88, 28));
                b.setFont(new Font("Segoe UI", Font.BOLD, 12));
                return b;
            }

            @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
                linhaAtual = tab.convertRowIndexToModel(row);
                painel.setBackground(COR_SEL);
                return painel;
            }
            @Override public Object getCellEditorValue() { return ""; }
        }

        private JButton criarBotaoDialog(String texto, Color cor) {
            JButton b = new BotaoArredondado(texto, cor, Color.WHITE, 10);
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            return b;
        }
    }

    // ── PAINEL VIDRO (usado internamente) ─────────────────────────
    class PainelVidroSistema extends JPanel {
        public PainelVidroSistema(LayoutManager layout) { super(layout); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COR_VIDRO_CARDS);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(new Color(0, 200, 200, 150));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
