package DAO;

import Model.Cliente;
import Model.Orcamento;
import Model.Produto;
import Model.Tecnico;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PainelOrcamentos extends JPanel {

    private final Color COR_TITULO      = new Color(0x80CBCB);
    private final Color COR_VIDRO_CARDS = new Color(20, 43, 66, 170);
    private final Color VERDE_INSERIR   = new Color(133, 201, 196);
    private final Color AZUL_ATUALIZAR  = new Color(60, 130, 180);
    private final Color VERMELHO_CANCEL = new Color(200, 0, 0);
    private final Color ROXO_ARQUIVO    = new Color(120, 90, 180);
    private final Color COR_HEADER_TAB  = new Color(30, 90, 110);
    private final Color COR_LINHA_PAR   = new Color(20, 55, 80, 180);
    private final Color COR_LINHA_IMPAR = new Color(12, 38, 58, 160);
    private final Color COR_SEL         = new Color(80, 180, 180, 120);

    private Font exo2SemiBold;

    private CampoArredondado txtClientePesquisa;
    private JComboBox<ItemCombo> comboEquipamento;
    private CampoArredondado txtAbertura;
    private CampoArredondado txtValidade;
    private CampoArredondado txtPecas;
    private CampoArredondado txtMaoObra;
    private CampoArredondado txtTotal;
    private CampoArredondado txtPesquisa;
    private AreaArredondada taDescricao;
    private JComboBox<ItemCombo> comboTecnico;
    private JComboBox<String> cbStatus;
    private JButton btnInserir;
    private JButton btnAtualizar;
    private JButton btnCancelar;

    private JPopupMenu popupClientes;
    private JPopupMenu popupProdutos;

    private DefaultTableModel modeloTabela;
    private JTable tabela;
    private TableRowSorter<DefaultTableModel> filtroTabela;

    private OrcamentoDAO orcamentoDAO = new OrcamentoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private TecnicoDAO tecnicoDAO = new TecnicoDAO();

    private List<Cliente> clientes = new ArrayList<>();
    private List<Produto> produtos = new ArrayList<>();
    private List<Tecnico> tecnicos = new ArrayList<>();

    private int idOrcamentoEditando = -1;
    private int clienteIdSelecionado = 0;
    private int produtoIdSelecionado = 0;
    private int servicoIdSelecionado = 0;

    private boolean formatandoMoeda = false;

    private final SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
    private final DecimalFormat formatoMoeda;

    public PainelOrcamentos() {
        carregarFonteExo();

        formatoData.setLenient(false);

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        formatoMoeda = new DecimalFormat("R$ #,##0.00", simbolos);

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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                atualizarTela();
            }
        });

        // Quando volta para a tela de orçamentos pelo menu, atualizo os dados de novo.
        // Isso faz o equipamento cadastrado em Produtos aparecer sem reiniciar o sistema.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                atualizarTela();
            }
        });

        carregarListasDoBanco();
        configurarDatasIniciais();
        calcularTotal();
    }

    // Atualizo os dados principais da tela.
    // Fiz separado para usar tanto ao abrir a tela quanto ao voltar pelo menu.
    private void atualizarTela() {
        carregarListasDoBanco();

        // Se tiver cliente selecionado, atualizo também os equipamentos desse cliente.
        // Assim o equipamento novo aparece em tempo real no orçamento.
        if (idOrcamentoEditando == -1 && clienteIdSelecionado > 0) {
            atualizarComboEquipamentosDoCliente();
        }

        carregarDados();
    }

    private void montarLayoutCrud(JPanel p) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);

        JLabel lblTitulo = new JLabel("PAINEL DE ORÇAMENTOS");
        if (exo2SemiBold != null) lblTitulo.setFont(exo2SemiBold.deriveFont(28f));
        else lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(COR_TITULO);

        g.gridx = 0; g.gridy = 0; g.gridwidth = 6;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;
        p.add(lblTitulo, g);

        g.gridwidth = 1;
        g.gridy = 1;

        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("CLIENTE:"), g);

        txtClientePesquisa = criarTextField(14);
        LimiteCampos.aplicar(txtClientePesquisa, 100);
        txtClientePesquisa.setToolTipText("Digite para pesquisar o cliente");
        txtClientePesquisa.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { pesquisarClientes(); }
        });
        txtClientePesquisa.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { fecharPopupDepois(popupClientes); }
        });

        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(txtClientePesquisa, g);

        g.gridx = 2; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("EQUIPAMENTO:"), g);

        comboEquipamento = new JComboBox<>();
        comboEquipamento.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboEquipamento.setEditable(false);
        comboEquipamento.setBackground(Color.WHITE);
        comboEquipamento.setForeground(Color.BLACK);
        comboEquipamento.setPreferredSize(new Dimension(230, 42));
        comboEquipamento.addItem(new ItemCombo(0, "Selecione um cliente"));
        comboEquipamento.setEnabled(false);
        comboEquipamento.addActionListener(e -> {
            ItemCombo item = getItemSelecionado(comboEquipamento);
            produtoIdSelecionado = item == null ? 0 : item.getId();
        });

        // Quando abrir a lista de equipamentos, busco de novo no banco.
        // Assim, se cadastrou um equipamento em Produtos, ele aparece aqui na hora.
        comboEquipamento.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                if (idOrcamentoEditando == -1) {
                    atualizarComboEquipamentosDoCliente();
                }
            }

            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { }
        });

        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(comboEquipamento, g);

        g.gridx = 4; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("TÉCNICO:"), g);

        comboTecnico = new JComboBox<>();
        comboTecnico.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboTecnico.setEditable(false);
        comboTecnico.setBackground(Color.WHITE);
        comboTecnico.setForeground(Color.BLACK);
        comboTecnico.setPreferredSize(new Dimension(230, 42));

        g.gridx = 5; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(comboTecnico, g);

        g.gridy = 2;

        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("STATUS:"), g);

        cbStatus = new JComboBox<>(new String[]{"Aguardando", "Aprovado", "Recusado"});
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbStatus.setEditable(false);
        cbStatus.setBackground(Color.WHITE);
        cbStatus.setForeground(Color.BLACK);
        cbStatus.setPreferredSize(new Dimension(210, 42));
        cbStatus.setSelectedItem("Aguardando");
        cbStatus.setEnabled(false);

        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(cbStatus, g);

        g.gridx = 2; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("ABERTURA:"), g);

        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(criarCampoDataComBotao(true), g);

        g.gridx = 4; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("PRAZO:"), g);

        g.gridx = 5; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(criarCampoDataComBotao(false), g);

        g.gridy = 3;

        txtPecas = criarTextField(10);
        txtPecas.setText("R$ 0,00");

        g.gridx = 0; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("VALOR:"), g);

        txtMaoObra = criarTextField(10);
        txtMaoObra.setText("R$ 0,00");
        configurarCampoMoeda(txtMaoObra);

        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(txtMaoObra, g);

        g.gridx = 2; g.weightx = 0; g.anchor = GridBagConstraints.EAST; g.fill = GridBagConstraints.NONE;
        p.add(criarLabel("TOTAL:"), g);

        txtTotal = criarTextField(10);
        txtTotal.setEditable(false);
        txtTotal.setText("R$ 0,00");
        txtTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));

        g.gridx = 3; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(txtTotal, g);

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

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        painelBotoes.setOpaque(false);

        // Deixo uma margem interna para os botões ficarem "flutuando"
        // entre a descrição e a borda inferior do card
        painelBotoes.setBorder(new EmptyBorder(6, 0, 6, 0));

        btnInserir = criarBotaoAcao("INSERIR", VERDE_INSERIR, Color.WHITE);
        btnInserir.addActionListener(e -> onInserir());
        TooltipUtils.aplicarTooltipPadrao(btnInserir);

        btnAtualizar = criarBotaoAcao("ATUALIZAR", AZUL_ATUALIZAR, Color.WHITE);
        btnAtualizar.addActionListener(e -> onAtualizar());
        TooltipUtils.aplicarTooltipPadrao(btnAtualizar);

        JButton btnRelatorio = criarBotaoAcao("GERAR PDF", ROXO_ARQUIVO, Color.WHITE);
        btnRelatorio.addActionListener(e -> onGerarRelatorio());

        btnCancelar = criarBotaoAcao("APAGAR", VERMELHO_CANCEL, Color.WHITE);
        btnCancelar.addActionListener(e -> onCancelar());
        TooltipUtils.aplicarTooltipPadrao(btnCancelar);

        painelBotoes.add(btnInserir);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnRelatorio);
        painelBotoes.add(btnCancelar);

        configurarModoEdicao(false);

        g.gridy = 5; g.gridx = 0; g.gridwidth = 6; g.weightx = 1;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;

        // Espaço em cima e embaixo para não ficar colado nem na descrição nem na borda do card
        g.insets = new Insets(14, 10, 16, 10);
        p.add(painelBotoes, g);
    }

    private JPanel criarCampoDataComBotao(boolean abertura) {
        JPanel painel = new JPanel(new BorderLayout(6, 0));
        painel.setOpaque(false);
        painel.setPreferredSize(new Dimension(220, 42));
        painel.setMinimumSize(new Dimension(220, 42));

        CampoArredondado campo = criarTextField(10);
        campo.setPreferredSize(new Dimension(170, 42));
        campo.setMinimumSize(new Dimension(170, 42));
        campo.setToolTipText("DD/MM/AAAA");

        // Máscara igual a da tela de Serviços: só aceita números e coloca as barras sozinho
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroData());

        JButton btnCalendario = new BotaoArredondado("📅", new Color(80, 180, 180), Color.WHITE, 14);
        btnCalendario.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnCalendario.setPreferredSize(new Dimension(42, 42));
        btnCalendario.setToolTipText("Escolher data no calendário");

        if (abertura) {
            txtAbertura = campo;
            btnCalendario.addActionListener(e -> abrirCalendario(txtAbertura));
        } else {
            txtValidade = campo;
            btnCalendario.addActionListener(e -> abrirCalendario(txtValidade));
        }

        painel.add(campo, BorderLayout.CENTER);
        painel.add(btnCalendario, BorderLayout.EAST);

        return painel;
    }

    // Abre uma janelinha igual a da tela de Serviços, sem biblioteca externa
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

        JPanel painelPrincipal = new JPanel(new BorderLayout(0, 14));
        painelPrincipal.setBorder(new EmptyBorder(22, 25, 22, 25));
        painelPrincipal.setBackground(new Color(20, 43, 66));

        JPanel painelCampos = new JPanel(new GridBagLayout());
        painelCampos.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        Calendar calendario = Calendar.getInstance();

        // Se o campo já tiver data, abre nela. Se não tiver, usa a data de hoje.
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            java.util.Date dataAtual = sdf.parse(campoDestino.getText().trim());
            calendario.setTime(dataAtual);
        } catch (Exception e) {
            // Se estiver vazio ou inválido, mantém a data de hoje
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

        // Dia e mês com 2 dígitos, ano sem separador de milhar
        spDia.setEditor(new JSpinner.NumberEditor(spDia, "00"));
        spMes.setEditor(new JSpinner.NumberEditor(spMes, "00"));
        spAno.setEditor(new JSpinner.NumberEditor(spAno, "0"));

        ((JSpinner.NumberEditor) spDia.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        ((JSpinner.NumberEditor) spMes.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        ((JSpinner.NumberEditor) spAno.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);

        // Atualiza o limite de dias quando mudar mês ou ano
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

        JButton btnCancelar = new BotaoArredondado("Cancelar", VERMELHO_CANCEL, Color.WHITE, 14);
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

    private void carregarListasDoBanco() {
        try {
            clientes = clienteDAO.listarTudo();
            produtos = produtoDAO.listarTudo();
            tecnicos = tecnicoDAO.listarTudo();
            carregarComboTecnicos();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao carregar dados", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarComboTecnicos() {
        if (comboTecnico == null) return;

        comboTecnico.removeAllItems();
        comboTecnico.addItem(new ItemCombo(0, "Selecione"));

        for (Tecnico t : tecnicos) {
            String nomeTecnico = t.getNome() == null ? "" : t.getNome().trim();

            // Não mostro o administrador na lista de técnicos do orçamento
            if (nomeTecnico.equalsIgnoreCase("Administrador")) {
                continue;
            }

            if (t.getStatus() == null || t.getStatus().equalsIgnoreCase("Ativo")) {
                comboTecnico.addItem(new ItemCombo(t.getIdTECNICO(), t.getNome()));
            }
        }
    }

    private void pesquisarClientes() {
        atualizarClientesEProdutosParaPesquisa();

        String texto = txtClientePesquisa.getText().trim().toLowerCase();

        clienteIdSelecionado = 0;
        produtoIdSelecionado = 0;
        limparComboEquipamentos();

        if (texto.isEmpty()) {
            esconderPopup(popupClientes);
            return;
        }

        popupClientes = criarPopup();

        int count = 0;
        for (Cliente c : clientes) {
            String dados = (c.getNome() + " " + c.getCpfCnpj() + " " + c.getTelefone()).toLowerCase();

            if (dados.contains(texto)) {
                JMenuItem item = criarItemPopup(c.getNome() + " - " + c.getCpfCnpj());
                item.addActionListener(e -> {
                    clienteIdSelecionado = c.getIdCliente();
                    txtClientePesquisa.setText(c.getNome());
                    produtoIdSelecionado = 0;
                    carregarComboEquipamentosDoCliente(clienteIdSelecionado);
                    esconderPopup(popupClientes);
                });
                popupClientes.add(item);
                count++;
                if (count >= 8) break;
            }
        }

        mostrarPopupSeTiverItens(popupClientes, txtClientePesquisa);
    }

    private void atualizarClientesEProdutosParaPesquisa() {
        try {
            clientes = clienteDAO.listarTudo();
            produtos = produtoDAO.listarTudo();
        } catch (RuntimeException e) {
            System.out.println("Erro ao atualizar clientes do orçamento: " + e.getMessage());
        }
    }

    // Atualiza o combo de equipamentos do cliente que está selecionado.
    // Usei isso para o orçamento enxergar equipamento novo sem reiniciar o programa.
    private void atualizarComboEquipamentosDoCliente() {
        if (comboEquipamento == null) return;

        if (clienteIdSelecionado <= 0) {
            limparComboEquipamentos();
            return;
        }

        ItemCombo itemAtual = getItemSelecionado(comboEquipamento);
        int idAtual = itemAtual == null ? 0 : itemAtual.getId();

        try {
            produtos = produtoDAO.listarTudo();
        } catch (RuntimeException e) {
            System.out.println("Erro ao atualizar equipamentos do orçamento: " + e.getMessage());
        }

        carregarComboEquipamentosDoCliente(clienteIdSelecionado);

        // Tento manter o equipamento que já estava selecionado.
        if (idAtual > 0) {
            selecionarComboPorId(comboEquipamento, idAtual);
        }
    }

    private void limparComboEquipamentos() {
        if (comboEquipamento == null) return;

        comboEquipamento.removeAllItems();
        comboEquipamento.addItem(new ItemCombo(0, "Selecione um cliente"));
        comboEquipamento.setEnabled(false);
        produtoIdSelecionado = 0;
    }

    private void carregarComboEquipamentosDoCliente(int idCliente) {
        if (comboEquipamento == null) return;

        comboEquipamento.removeAllItems();
        comboEquipamento.addItem(new ItemCombo(0, "Selecione"));

        if (idCliente <= 0) {
            comboEquipamento.setEnabled(false);
            produtoIdSelecionado = 0;
            return;
        }

        for (Produto produto : produtos) {
            if (produto.getClienteIdCliente() == idCliente) {
                comboEquipamento.addItem(new ItemCombo(produto.getIdPRODUTO(), montarNomeEquipamento(produto)));
            }
        }

        comboEquipamento.setEnabled(comboEquipamento.getItemCount() > 1);
        comboEquipamento.setSelectedIndex(0);
        produtoIdSelecionado = 0;
    }

    private JPopupMenu criarPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false);
        return popup;
    }

    private JMenuItem criarItemPopup(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return item;
    }

    private void mostrarPopupSeTiverItens(JPopupMenu popup, JTextField campo) {
        if (popup == null) return;

        if (popup.getComponentCount() > 0) {
            popup.show(campo, 0, campo.getHeight());
        } else {
            esconderPopup(popup);
        }
    }

    private void esconderPopup(JPopupMenu popup) {
        if (popup != null) popup.setVisible(false);
    }

    private void fecharPopupDepois(JPopupMenu popup) {
        javax.swing.Timer t = new javax.swing.Timer(180, e -> esconderPopup(popup));
        t.setRepeats(false);
        t.start();
    }

    private String montarNomeEquipamento(Produto p) {
        String nome = (textoSeguro(p.getTipo()) + " " + textoSeguro(p.getMarca()) + " " + textoSeguro(p.getModelo())).trim();
        nome = nome.replaceAll("\\s+", " ");
        return nome.isEmpty() ? "Equipamento sem descrição" : nome;
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
        btnLimpar.addActionListener(e -> {
            txtPesquisa.setText("");
            filtrarTabela();
        });

        JPanel pnlBotaoLimpar = new JPanel(new GridBagLayout());
        pnlBotaoLimpar.setOpaque(false);
        pnlBotaoLimpar.setPreferredSize(new Dimension(170, 36));
        pnlBotaoLimpar.setBorder(new EmptyBorder(0, 0, 0, 42));
        pnlBotaoLimpar.add(btnLimpar);

        g.gridx = 2; g.weightx = 0; g.insets = new Insets(0, 0, 0, 0);
        painelPesquisa.add(pnlBotaoLimpar, g);

        card.add(painelPesquisa, BorderLayout.NORTH);

        String[] colunas = {"ID", "Cliente", "Equipamento", "Técnico", "Abertura", "Prazo", "Status", "Valor Total", "Ações",
                            "ClienteID", "ProdutoID", "TecnicoID", "ServicoID", "Pecas", "MaoObra", "Desc", "AberturaSql", "ValidadeSql"};

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 8; }
        };

        tabela = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) c.setBackground(COR_SEL);
                else c.setBackground(row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR);
                c.setForeground(Color.WHITE);
                return c;
            }
        };

        filtroTabela = new TableRowSorter<>(modeloTabela);
        tabela.setRowSorter(filtroTabela);

        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabela.setRowHeight(38);
        tabela.setShowGrid(false);
        tabela.setGridColor(new Color(0, 0, 0, 0));
        tabela.setIntercellSpacing(new Dimension(0, 2));
        tabela.setOpaque(false);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);

        personalizarCabecalhoTabela();

        DefaultTableCellRenderer cellCenter = new DefaultTableCellRenderer();
        cellCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            if (i != 8) tabela.getColumnModel().getColumn(i).setCellRenderer(cellCenter);
        }

        tabela.getColumnModel().getColumn(8).setCellRenderer(new AcoesCellRenderer());
        tabela.getColumnModel().getColumn(8).setCellEditor(new AcoesCellEditor(tabela));
        tabela.getColumnModel().getColumn(8).setPreferredWidth(165);
        tabela.getColumnModel().getColumn(8).setMinWidth(165);

        for (int i = 9; i <= 17; i++) ocultarColuna(tabela, i);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER);

        carregarDados();
    }

    private void personalizarCabecalhoTabela() {
        JTableHeader header = tabela.getTableHeader();
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setOpaque(false);
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
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

        if (filtroTabela != null) {
            for (int i = 0; i < tabela.getColumnCount(); i++) filtroTabela.setSortable(i, false);
        }
    }

    private void ocultarColuna(JTable tab, int index) {
        TableColumn col = tab.getColumnModel().getColumn(index);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setPreferredWidth(0);
        col.setResizable(false);
    }

    private void filtrarTabela() {
        if (filtroTabela == null || txtPesquisa == null) return;
        String texto = txtPesquisa.getText().trim();

        if (texto.isEmpty()) {
            filtroTabela.setRowFilter(null);
        } else {
            filtroTabela.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        }
    }

    private void configurarCampoMoeda(CampoArredondado campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroMoeda());

        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { calcularTotal(); }
            @Override public void removeUpdate(DocumentEvent e) { calcularTotal(); }
            @Override public void changedUpdate(DocumentEvent e) { calcularTotal(); }
        });

        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (campo.getText().trim().equals("R$ 0,00")) campo.setText("");
            }

            @Override public void focusLost(FocusEvent e) {
                formatarMoedaNoCampo(campo);
                calcularTotal();
            }
        });
    }

    private void calcularTotal() {
        if (txtPecas == null || txtMaoObra == null || txtTotal == null) return;

        BigDecimal pecas = converterValor(txtPecas.getText());
        BigDecimal maoObra = converterValor(txtMaoObra.getText());
        txtTotal.setText(formatarMoedaTexto(pecas.add(maoObra)));
    }

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

    private BigDecimal converterValor(String texto) {
        try {
            String limpo = texto == null ? "" : texto.trim();

            if (limpo.isEmpty()) return BigDecimal.ZERO;

            limpo = limpo.replace("R$", "").trim();

            if (limpo.contains(",")) {
                limpo = limpo.replace(".", "").replace(",", ".");
                return new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP);
            }

            limpo = limpo.replaceAll("[^0-9]", "");
            if (limpo.isEmpty()) return BigDecimal.ZERO;

            return new BigDecimal(limpo).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void configurarModoEdicao(boolean editando) {
        if (btnInserir != null) btnInserir.setVisible(!editando);
        if (btnAtualizar != null) btnAtualizar.setVisible(editando);
        if (btnCancelar != null) btnCancelar.setText(editando ? "CANCELAR" : "APAGAR");

        if (cbStatus != null) cbStatus.setEnabled(editando);

        if (txtClientePesquisa != null) {
            txtClientePesquisa.setEditable(!editando);
            txtClientePesquisa.setFocusable(!editando);
        }

        if (comboEquipamento != null) {
            comboEquipamento.setEnabled(!editando);
        }

        if (txtAbertura != null) {
            txtAbertura.setEditable(!editando);
            txtAbertura.setEnabled(!editando);
            txtAbertura.setFocusable(!editando);
        }
    }

    private void onInserir() {
        if (!validarCampos()) return;

        Object[] opcoes = {"Sim", "Não", "Cancelar"};
        int resposta = JOptionPane.showOptionDialog(
                this,
                "O cliente aprovou este orçamento?",
                "Confirmação do orçamento",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );

        if (resposta == JOptionPane.CANCEL_OPTION || resposta == JOptionPane.CLOSED_OPTION) {
            return;
        }

        if (resposta == JOptionPane.YES_OPTION) {
            cbStatus.setSelectedItem("Aprovado");
        } else {
            cbStatus.setSelectedItem("Aguardando");
        }

        try {
            Orcamento orcamento = montarOrcamentoPelosCampos();
            orcamentoDAO.adicionar(orcamento);

            JOptionPane.showMessageDialog(this, "Orçamento cadastrado com sucesso!");
            onCancelar();
            carregarListasDoBanco();
            carregarDados();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAtualizar() {
        if (idOrcamentoEditando == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um orçamento na tabela para editar!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validarCampos()) return;

        try {
            Orcamento orcamento = montarOrcamentoPelosCampos();
            orcamento.setIdRelatorioOrcamento(idOrcamentoEditando);
            orcamentoDAO.alterar(orcamento);

            JOptionPane.showMessageDialog(this, "Orçamento atualizado com sucesso!");
            onCancelar();
            carregarListasDoBanco();
            carregarDados();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirOrcamento(int idOrcamento) {
        int op = JOptionPane.showConfirmDialog(this, "Deseja excluir este orçamento?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            orcamentoDAO.excluir(idOrcamento);
            JOptionPane.showMessageDialog(this, "Orçamento excluído com sucesso!");
            onCancelar();
            carregarDados();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validarCampos() {
        if (clienteIdSelecionado <= 0) {
            JOptionPane.showMessageDialog(this, "Pesquise e selecione um cliente válido.", "Atenção", JOptionPane.WARNING_MESSAGE);
            txtClientePesquisa.requestFocus();
            return false;
        }

        ItemCombo equipamento = getItemSelecionado(comboEquipamento);

        // No cadastro, pego o equipamento escolhido no combo.
        // Na edição, mantenho o produtoIdSelecionado que veio da linha da tabela.
        if (idOrcamentoEditando == -1) {
            produtoIdSelecionado = equipamento == null ? 0 : equipamento.getId();
        } else if (produtoIdSelecionado <= 0 && equipamento != null && equipamento.getId() > 0) {
            produtoIdSelecionado = equipamento.getId();
        }

        if (produtoIdSelecionado <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um equipamento válido.", "Atenção", JOptionPane.WARNING_MESSAGE);
            comboEquipamento.requestFocus();
            return false;
        }

        ItemCombo tecnico = getItemSelecionado(comboTecnico);

        if (tecnico == null || tecnico.getId() <= 0) {
            JOptionPane.showMessageDialog(this, "Selecione um técnico válido.", "Atenção", JOptionPane.WARNING_MESSAGE);
            comboTecnico.requestFocus();
            return false;
        }

        Date abertura = pegarDataSql(txtAbertura);
        Date validade = pegarDataSql(txtValidade);

        if (abertura == null) {
            JOptionPane.showMessageDialog(this, "Informe uma data de abertura válida.", "Atenção", JOptionPane.WARNING_MESSAGE);
            txtAbertura.requestFocus();
            return false;
        }

        if (validade == null) {
            JOptionPane.showMessageDialog(this, "Informe uma data de prazo válida.", "Atenção", JOptionPane.WARNING_MESSAGE);
            txtValidade.requestFocus();
            return false;
        }

        if (validade.before(abertura)) {
            JOptionPane.showMessageDialog(this, "O prazo não pode ser antes da abertura.", "Atenção", JOptionPane.WARNING_MESSAGE);
            txtValidade.requestFocus();
            return false;
        }

        if (taDescricao.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a descrição/defeito relatado.", "Atenção", JOptionPane.WARNING_MESSAGE);
            taDescricao.requestFocus();
            return false;
        }

        return true;
    }

    private Orcamento montarOrcamentoPelosCampos() {
        calcularTotal();

        ItemCombo tecnico = getItemSelecionado(comboTecnico);

        Orcamento orcamento = new Orcamento();
        orcamento.setClienteIdCliente(clienteIdSelecionado);
        orcamento.setProdutoIdProduto(produtoIdSelecionado);
        orcamento.setTecnicoIdTecnico(tecnico.getId());
        orcamento.setServicoIdServico(servicoIdSelecionado);
        orcamento.setStatusOrcamento(String.valueOf(cbStatus.getSelectedItem()));
        orcamento.setAbertura(pegarDataSql(txtAbertura));
        orcamento.setValidade(pegarDataSql(txtValidade));
        orcamento.setDefeitoRelatado(taDescricao.getText().trim());
        orcamento.setDiagnosticoTecnico("");
        orcamento.setPecasOrcadas(converterValor(txtPecas.getText()));
        orcamento.setMaoObraOrcada(converterValor(txtMaoObra.getText()));
        orcamento.setValorTotalOrcamento(converterValor(txtTotal.getText()));

        return orcamento;
    }

    private void onCancelar() {
        txtClientePesquisa.setText("");
        limparComboEquipamentos();
        if (comboTecnico.getItemCount() > 0) comboTecnico.setSelectedIndex(0);
        txtPecas.setText("R$ 0,00");
        txtMaoObra.setText("R$ 0,00");
        taDescricao.setText("");
        txtTotal.setText("R$ 0,00");
        cbStatus.setSelectedItem("Aguardando");
        cbStatus.setEnabled(false);
        configurarDatasIniciais();

        idOrcamentoEditando = -1;
        clienteIdSelecionado = 0;
        produtoIdSelecionado = 0;
        servicoIdSelecionado = 0;
        configurarModoEdicao(false);
    }

    private void carregarDados() {
        modeloTabela.setRowCount(0);

        try {
            List<Orcamento> lista = orcamentoDAO.listarTudo();

            for (Orcamento o : lista) {
                modeloTabela.addRow(new Object[]{
                    o.getIdRelatorioOrcamento(),
                    o.getNomeCliente(),
                    o.getNomeEquipamento(),
                    o.getNomeTecnico(),
                    formatarDataTabela(o.getAbertura()),
                    formatarDataTabela(o.getValidade()),
                    o.getStatusOrcamento(),
                    formatarMoedaTexto(o.getValorTotalOrcamento()),
                    "",
                    o.getClienteIdCliente(),
                    o.getProdutoIdProduto(),
                    o.getTecnicoIdTecnico(),
                    o.getServicoIdServico(),
                    valorSeguro(o.getPecasOrcadas()),
                    valorSeguro(o.getMaoObraOrcada()),
                    textoSeguro(o.getDefeitoRelatado()),
                    o.getAbertura(),
                    o.getValidade()
                });
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarDatasIniciais() {
        txtAbertura.setText(formatarDataTabela(Date.valueOf(LocalDate.now())));
        txtValidade.setText("");
    }

    private java.util.Date converterCampoParaUtilDate(CampoArredondado campo) {
        try {
            if (campo.getText().trim().isEmpty()) return null;
            return formatoData.parse(campo.getText().trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private Date pegarDataSql(CampoArredondado campo) {
        java.util.Date data = converterCampoParaUtilDate(campo);
        if (data == null) return null;
        return new Date(data.getTime());
    }

    private String formatarDataTabela(Date data) {
        if (data == null) return "";
        return formatoData.format(data);
    }

    private String formatarMoedaTexto(BigDecimal valor) {
        if (valor == null) valor = BigDecimal.ZERO;
        return formatoMoeda.format(valor.setScale(2, RoundingMode.HALF_UP));
    }

    private String valorSeguro(BigDecimal valor) {
        return formatarMoedaTexto(valor);
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private void onGerarRelatorio() {
        try {
            boolean completo = tabela.print(JTable.PrintMode.FIT_WIDTH,
                new MessageFormat("Relatório de Orçamentos"),
                new MessageFormat("Página {0}"));

            if (completo) JOptionPane.showMessageDialog(this, "Relatório processado!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ItemCombo getItemSelecionado(JComboBox<ItemCombo> combo) {
        Object obj = combo.getSelectedItem();
        if (obj instanceof ItemCombo) return (ItemCombo) obj;
        return null;
    }

    private void selecionarComboPorId(JComboBox<ItemCombo> combo, int id) {
        if (combo == null) return;

        for (int i = 0; i < combo.getItemCount(); i++) {
            ItemCombo item = combo.getItemAt(i);

            if (item.getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }

        if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
    }

    class AcoesCellRenderer implements TableCellRenderer {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));

        AcoesCellRenderer() {
            painel.setOpaque(true);
            painel.add(criarBtnPequeno("Editar", new Color(80, 180, 180), Color.WHITE));
            painel.add(criarBtnPequeno("Excluir", new Color(200, 60, 60), Color.WHITE));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            painel.setBackground(sel ? COR_SEL : (row % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR));
            return painel;
        }
    }

    class AcoesCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JButton btnEdit = criarBtnPequeno("Editar", new Color(80, 180, 180), Color.WHITE);
        private final JButton btnDel = criarBtnPequeno("Excluir", new Color(200, 60, 60), Color.WHITE);
        private final JTable tab;
        private int linhaAtual;

        AcoesCellEditor(JTable t) {
            this.tab = t;
            painel.setOpaque(true);
            painel.add(btnEdit);
            painel.add(btnDel);

            btnEdit.addActionListener(e -> {
                fireEditingStopped();

                idOrcamentoEditando = Integer.parseInt(modeloTabela.getValueAt(linhaAtual, 0).toString());
                clienteIdSelecionado = Integer.parseInt(modeloTabela.getValueAt(linhaAtual, 9).toString());
                produtoIdSelecionado = Integer.parseInt(modeloTabela.getValueAt(linhaAtual, 10).toString());
                int tecnicoIdSelecionado = Integer.parseInt(modeloTabela.getValueAt(linhaAtual, 11).toString());

                Object servicoObj = modeloTabela.getValueAt(linhaAtual, 12);
                servicoIdSelecionado = servicoObj == null ? 0 : Integer.parseInt(servicoObj.toString());

                txtClientePesquisa.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 1)));

                // Guardo o ID antes de mexer no combo, porque o removeAllItems chama o ActionListener
                int produtoIdEditar = produtoIdSelecionado;

                // No modo edição, deixo o equipamento atual fixo para não resetar para "Selecione"
                comboEquipamento.removeAllItems();
                comboEquipamento.addItem(new ItemCombo(produtoIdEditar, String.valueOf(modeloTabela.getValueAt(linhaAtual, 2))));
                comboEquipamento.setSelectedIndex(0);
                produtoIdSelecionado = produtoIdEditar;

                selecionarComboPorId(comboTecnico, tecnicoIdSelecionado);

                txtAbertura.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 4)));
                txtValidade.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 5)));
                cbStatus.setSelectedItem(modeloTabela.getValueAt(linhaAtual, 6));
                txtPecas.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 13)));
                txtMaoObra.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 14)));
                taDescricao.setText(String.valueOf(modeloTabela.getValueAt(linhaAtual, 15)));

                configurarModoEdicao(true);
                calcularTotal();
            });

            btnDel.addActionListener(e -> {
                fireEditingStopped();
                int idOrcamento = Integer.parseInt(modeloTabela.getValueAt(linhaAtual, 0).toString());
                excluirOrcamento(idOrcamento);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            linhaAtual = tab.convertRowIndexToModel(row);
            painel.setBackground(COR_SEL);
            return painel;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }

    private JButton criarBtnPequeno(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 12);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(70, 26));
        return b;
    }

    class ItemCombo {
        private int id;
        private String texto;

        public ItemCombo(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        public int getId() { return id; }

        @Override public String toString() { return texto; }
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

    private JLabel criarLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return l;
    }

    private CampoArredondado criarTextField(int col) {
        CampoArredondado tf = new CampoArredondado(col);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        tf.setPreferredSize(new Dimension(180, 42));
        tf.setMinimumSize(new Dimension(160, 42));
        return tf;
    }

    private JButton criarBotaoAcao(String txt, Color bg, Color fg) {
        JButton b = new BotaoArredondado(txt, bg, fg, 18);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setPreferredSize(new Dimension(120, 40));
        b.setMinimumSize(new Dimension(120, 40));
        b.setMaximumSize(new Dimension(120, 40));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return b;
    }

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

            Color corBorda = (corFundo.getRed() > 150 && corFundo.getGreen() < 90)
                ? new Color(255, 120, 120, 180)
                : new Color(0, 200, 200, 170);

            g2.setColor(corBorda);
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

        private Color clarearCor(Color c, int valor) {
            return new Color(
                Math.min(255, c.getRed() + valor),
                Math.min(255, c.getGreen() + valor),
                Math.min(255, c.getBlue() + valor));
        }
    }

    class CampoArredondado extends JTextField {
        private Color corFundo = Color.WHITE;
        private Color corBorda = new Color(0, 200, 200, 150);
        private int arco = 14;

        public CampoArredondado() { super(); configurar(); }
        public CampoArredondado(int cols) { super(cols); configurar(); }

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

    // ─────────────────────────────────────────────────────────────
    // FILTRO DE DATA — formata automaticamente enquanto digita
    // ─────────────────────────────────────────────────────────────
    static class FiltroData extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) return;

            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            for (char c : text.toCharArray()) {
                if (Character.isDigit(c)) sb.insert(offset++, c);
            }
            aplicarMascara(fb, sb.toString());
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder sb = new StringBuilder(atual);
            sb.delete(offset, offset + length);

            if (text != null) {
                int pos = offset;
                for (char c : text.toCharArray()) {
                    if (Character.isDigit(c)) sb.insert(pos++, c);
                }
            }

            aplicarMascara(fb, sb.toString());
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder sb = new StringBuilder(atual);
            sb.delete(offset, offset + length);
            aplicarMascara(fb, sb.toString());
        }

        private void aplicarMascara(FilterBypass fb, String texto) throws BadLocationException {
            String soDigitos = texto.replaceAll("[^0-9]", "");
            if (soDigitos.length() > 8) soDigitos = soDigitos.substring(0, 8);

            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < soDigitos.length(); i++) {
                if (i == 2 || i == 4) formatado.append('/');
                formatado.append(soDigitos.charAt(i));
            }

            fb.replace(0, fb.getDocument().getLength(), formatado.toString(), null);
        }
    }

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

}
