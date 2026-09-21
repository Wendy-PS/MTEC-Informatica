// Declaro que essa classe faz parte do pacote DAO do projeto
package DAO;

// Importações necessárias para a tela funcionar
import Model.Servico;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;

// Essa é a tela principal de Serviços do sistema MTEC
// Ela mostra o formulário para cadastrar ordens de serviço
// e a tabela com todas as OS já cadastradas
public class TelaServicos extends JFrame {

    // ── Cores que uso em toda a tela ──────────────────────────────
    private final Color COR_TITULO        = new Color(0x80CBCB);        // azul claro dos títulos
    private final Color COR_VIDRO_LATERAL = new Color(15, 35, 55, 190); // fundo do menu lateral
    private final Color COR_VIDRO_CARDS   = new Color(20, 43, 66, 170); // fundo dos cards
    private final Color VERDE_MTEC        = new Color(133, 201, 196);   // verde dos botões positivos
    private final Color VERMELHO_MTEC     = new Color(200, 0, 0);       // vermelho dos botões de cancelar/excluir
    private final Color COR_HEADER_TAB    = new Color(30, 90, 110, 220); // cor do cabeçalho da tabela
    private final Color COR_LINHA_PAR     = new Color(20, 55, 80, 180);  // linhas pares da tabela
    private final Color COR_LINHA_IMPAR   = new Color(12, 38, 58, 160);  // linhas ímpares da tabela
    private final Color COR_SEL           = new Color(80, 180, 180, 120); // cor quando seleciona uma linha

    // ── Variáveis globais da tela ─────────────────────────────────
    private Font exo2SemiBold;               // fonte personalizada do sistema
    private JLayeredPane layeredPane;        // camadas da tela (fundo + interface em cima)
    private JPanel painelFundo;             // painel que desenha o background
    private JPanel painelInterface;         // painel que fica em cima do fundo com os componentes
    private DefaultTableModel modeloTabela; // modelo de dados da tabela de OS

    // ── Campos do formulário — declaro aqui para acessar em qualquer método ──
    private JTextField tfOs;                   // campo do número da OS (somente leitura)
    private JTextField tfClientePesquisa;      // campo para pesquisar cliente pelo nome, telefone ou CPF/CNPJ
    private JPopupMenu popupClientes;          // menu suspenso com resultados da busca de clientes
    private int idClienteSelecionado = -1;     // guarda o ID real do cliente selecionado
    private JComboBox<ItemCombo> comboTecnico; // lista de técnicos do banco com ID escondido
    private JComboBox<String> comboStatus;     // lista de status da OS
    private JButton btnCancelar;
    private JTextField tfPrazo;                // campo do prazo com máscara de data
    private JTextField tfAbertura;             // campo da data de abertura com máscara
    private int idOsEditando = -1;             // guarda o ID da OS quando estiver editando (-1 = nenhuma)

    // ── Classe usada no ComboBox de técnico ───────────────────────
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

    // ── Construtor: monta toda a tela quando a classe é criada ────
    public TelaServicos() {
        carregarFonteExo(); // carrega a fonte antes de qualquer coisa

        // Configurações básicas da janela
        setTitle("MTEC - Gerenciamento de Chamados");
        setMinimumSize(new Dimension(1050, 700)); // tamanho mínimo para não quebrar o layout
        setSize(1200, 750);                       // tamanho inicial
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // fecha o programa ao fechar a janela
        setLocationRelativeTo(null); // centraliza na tela

        // Crio o layeredPane para poder colocar o fundo e a interface em camadas separadas
        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);

        // ── Camada 1: Painel do fundo (imagem de background) ──────
        painelFundo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Tenta carregar o background da pasta imagens
                URL res = getClass().getResource("/imagens/Background.png");
                if (res != null) {
                    // Se encontrou a imagem, desenha ela ocupando toda a tela
                    g.drawImage(new ImageIcon(res).getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Se não encontrou, pinta um azul escuro no lugar
                    g.setColor(new Color(10, 25, 40));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        layeredPane.add(painelFundo, JLayeredPane.DEFAULT_LAYER); // camada de baixo

        // ── Camada 2: Interface (fica por cima do fundo) ──────────
        painelInterface = new JPanel(new BorderLayout());
        painelInterface.setOpaque(false); // transparente para o fundo aparecer
        layeredPane.add(painelInterface, JLayeredPane.PALETTE_LAYER); // camada de cima

        // ── Menu Lateral esquerdo ──────────────────────────────────
        JPanel menuLateral = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Pinto o fundo do menu com a cor azul escura semitransparente
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COR_VIDRO_LATERAL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        menuLateral.setOpaque(false);
        menuLateral.setPreferredSize(new Dimension(250, 0)); // largura fixa de 250px
        menuLateral.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
        menuLateral.setBorder(new EmptyBorder(30, 0, 10, 0)); // espaço no topo

        // Adiciono o logo e os botões no menu
        configurarLogo(menuLateral, "/imagens/logo_mtec.png", 180, 70);
        menuLateral.add(Box.createVerticalStrut(25)); // espaço entre logo e botões
        adicionarBotoesMenu(menuLateral);

        // ── Área direita (onde fica o conteúdo principal) ─────────
        JPanel areaDireita = new JPanel(new BorderLayout(0, 20));
        areaDireita.setOpaque(false);
        areaDireita.setBorder(new EmptyBorder(20, 20, 20, 20)); // margem em volta

        // ── Barra superior com o título do sistema ─────────────────
        JPanel painelHeader = new JPanel(new BorderLayout());
        painelHeader.setOpaque(true);
        painelHeader.setBackground(new Color(15, 35, 55, 220));
        painelHeader.setPreferredSize(new Dimension(0, 60)); // altura da barra
        painelHeader.setBorder(new EmptyBorder(0, 15, 0, 15));

        // Título que aparece na barra do topo
        JLabel lblTituloHeader = new JLabel("GERENCIAMENTO DE CHAMADOS - MTEC");
        if (exo2SemiBold != null) lblTituloHeader.setFont(exo2SemiBold.deriveFont(32f));
        else lblTituloHeader.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblTituloHeader.setForeground(COR_TITULO);

        // Ícone de usuário no canto direito da barra
        JLabel lblUserIcon = new JLabel();
        lblUserIcon.setBorder(new EmptyBorder(0, 0, 0, 10));
        URL userRes = getClass().getResource("/imagens/user.png");
        if (userRes != null) {
            lblUserIcon.setIcon(new ImageIcon(
                new ImageIcon(userRes).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        }
        lblUserIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        painelHeader.add(lblTituloHeader, BorderLayout.WEST);
        painelHeader.add(lblUserIcon, BorderLayout.EAST);
        areaDireita.add(painelHeader, BorderLayout.NORTH);

        // ── Container dos dois cards (formulário + tabela) ─────────
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

        areaDireita.add(containerCards, BorderLayout.CENTER);

        // Junto o menu lateral e a área direita na interface
        painelInterface.add(menuLateral, BorderLayout.WEST);
        painelInterface.add(areaDireita, BorderLayout.CENTER);

        // Quando a janela for redimensionada, atualizo o tamanho dos painéis de fundo
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                painelFundo.setBounds(0, 0, getWidth(), getHeight());
                painelInterface.setBounds(0, 0, getWidth(), getHeight());
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // TABELA DE ORDENS DE SERVIÇO
    // ─────────────────────────────────────────────────────────────

    // Monta a tabela dentro do card e configura todo o visual dela
    private void montarTabela(JPanel card) {
        // Defino os nomes das colunas da tabela
        // As colunas ID Cliente e ID Técnico ficam ocultas, mas ajudam na hora de editar sem depender só do nome
        String[] colunas = {"ID OS", "Cliente", "Equipamento", "Abertura", "Prazo", "Status", "Ações", "ID Cliente", "ID Técnico"};

        // Crio o modelo da tabela — ele guarda os dados das linhas
        // Só a coluna 6 (Ações) é editável, pois tem os botões Editar/Excluir
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 6; }
        };

        carregarDados(); // preencho a tabela com os dados do banco

        // Crio a tabela com cores alternadas nas linhas
        JTable tabela = new JTable(modeloTabela) {
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

        // Configurações visuais gerais da tabela
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabela.setRowHeight(38);          // altura de cada linha
        tabela.setShowGrid(false);        // sem linhas de grade
        tabela.setIntercellSpacing(new Dimension(0, 2)); // espaço entre linhas
        tabela.setOpaque(false);          // transparente para o card aparecer
        tabela.setFillsViewportHeight(true); // ocupa todo o espaço disponível
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // só uma linha selecionada por vez
        tabela.getTableHeader().setReorderingAllowed(false); // impede arrastar colunas

        // Configuro o visual do cabeçalho da tabela
        JTableHeader header = tabela.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(COR_HEADER_TAB);
        header.setForeground(COR_TITULO);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());

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
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Centralizo o texto de todas as células, menos a coluna de Ações
        DefaultTableCellRenderer cellCenter = new DefaultTableCellRenderer();
        cellCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            if (i != 6) {
                tabela.getColumnModel().getColumn(i).setCellRenderer(cellCenter);
            }
        }

        // Coluna de Ações: uso renderer e editor personalizados com botões Editar/Excluir
        tabela.getColumnModel().getColumn(6).setCellRenderer(new AcoesCellRenderer());
        tabela.getColumnModel().getColumn(6).setCellEditor(new AcoesCellEditor(tabela));
        tabela.getColumnModel().getColumn(6).setPreferredWidth(165);
        tabela.getColumnModel().getColumn(6).setMinWidth(165);

        // Defino a largura preferida das colunas visíveis
        int[] larguras = {65, 140, 170, 100, 100, 110};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Escondo as colunas ID Cliente e ID Técnico
        // Elas continuam no modelo da tabela, mas não aparecem para o usuário
        ocultarColuna(tabela, 7);
        ocultarColuna(tabela, 8);

        // Coloco a tabela dentro de um scroll para funcionar quando tiver muitas linhas
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16); // velocidade do scroll

        card.add(scroll, BorderLayout.CENTER);
    }

    // Oculta uma coluna da JTable sem remover os dados dela do modelo
    private void ocultarColuna(JTable tabela, int indice) {
        TableColumn coluna = tabela.getColumnModel().getColumn(indice);
        coluna.setMinWidth(0);
        coluna.setMaxWidth(0);
        coluna.setPreferredWidth(0);
        coluna.setResizable(false);
    }

    // Carrega os dados da tabela buscando do banco via ServicoDAO
    // Se o banco falhar, mostra os dados de exemplo para não travar a tela
    private void carregarDados() {
        modeloTabela.setRowCount(0); // limpo a tabela antes de recarregar
        try {
            // Busco todas as OS do banco usando o ServicoDAO
            List<Servico> lista = new ServicoDAO().listarTudo();
            for (Servico os : lista) {
                // Formato as datas de Date para DD/MM/AAAA para exibir na tabela
                String abertura = os.getDataAbertura() != null
                    ? new SimpleDateFormat("dd/MM/yyyy").format(os.getDataAbertura()) : "";
                String prazo = os.getPrazoEntrega() != null
                    ? new SimpleDateFormat("dd/MM/yyyy").format(os.getPrazoEntrega()) : "";

                // Adiciono cada OS como uma linha na tabela
                // Os dois últimos campos são IDs ocultos para facilitar a edição depois
                modeloTabela.addRow(new Object[]{
                    os.getIdSERVICO(),
                    os.getNomeCliente(),
                    os.getNomeEquipamento(),
                    abertura,
                    prazo,
                    os.getStatusOs(),
                    "",
                    os.getClienteIdCliente(),
                    os.getTecnicoIdTecnico()
                });
            }
        } catch (Exception e) {
            // Se o banco falhar, mostro os dados de exemplo
            System.out.println("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace(); // mostro o erro completo no console para descobrir o problema
            Object[][] exemplo = {
                {101, "Joao Silva",      "Notebook Dell XPS 15",  "01/05/2026", "10/05/2026", "Aguardando Aprovação", "", 1, 1},
                {102, "Maria Souza",     "iPhone 14 Pro",          "02/05/2026", "08/05/2026", "Em Andamento",          "", 2, 1},
                {103, "Pedro Costa",     "PC Gamer Ryzen 9",       "03/05/2026", "12/05/2026", "Aguardando Peça",      "", 3, 1},
                {104, "Ana Martins",     "Impressora Epson L3250", "04/05/2026", "09/05/2026", "Concluído",            "", 4, 1},
                {105, "Roberto Almeida", "Tablet Samsung S8",      "05/05/2026", "11/05/2026", "Cancelado",            "", 5, 1},
            };
            for (Object[] linha : exemplo) modeloTabela.addRow(linha);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BOTÕES EDITAR E EXCLUIR DA TABELA
    // ─────────────────────────────────────────────────────────────

    // Esse renderer mostra os botões Editar/Excluir em cada linha (visual apenas)
    class AcoesCellRenderer implements TableCellRenderer {
        private final JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        AcoesCellRenderer() {
            painel.setOpaque(true);
            painel.add(criarBtnAcao("Editar",  new Color(80, 180, 180), Color.BLACK));
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
        private final JButton btnEdit = criarBtnAcao("Editar",  new Color(80, 180, 180), Color.BLACK);
        private final JButton btnDel  = criarBtnAcao("Excluir", new Color(200, 60, 60),  Color.WHITE);
        private int linhaAtual; // guarda qual linha foi clicada

        AcoesCellEditor(JTable tabela) {
            painel.setOpaque(true);
            painel.add(btnEdit);
            painel.add(btnDel);

            // Quando clica em Editar, chama o método onEditar com o número da linha
            btnEdit.addActionListener(e -> { fireEditingStopped(); onEditar(linhaAtual);  });
            // Quando clica em Excluir, chama o método onExcluir com o número da linha
            btnDel.addActionListener(e  -> { fireEditingStopped(); onExcluir(linhaAtual); });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            linhaAtual = row; // salvo qual linha está sendo editada
            painel.setBackground(COR_SEL);
            return painel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    // Cria um botão pequeno para usar dentro da tabela
    private JButton criarBtnAcao(String txt, Color bg, Color fg) {
        JButton b = new JButton(txt);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(70, 26));
        return b;
    }

    // Ação do botão Editar — preenche o formulário com os dados da linha selecionada
    private void onEditar(int linha) {
        // Pego os dados da linha clicada na tabela
        idOsEditando = (int) modeloTabela.getValueAt(linha, 0); // salvo o ID da OS
        tfOs.setText(String.valueOf(idOsEditando));

        // Preencho o cliente usando o nome visível e guardo o ID real usando a coluna oculta
        tfClientePesquisa.setText((String) modeloTabela.getValueAt(linha, 1));
        idClienteSelecionado = (int) modeloTabela.getValueAt(linha, 7);

        // Seleciono o técnico correto usando o ID salvo na coluna oculta
        int idTecnico = (int) modeloTabela.getValueAt(linha, 8);
        selecionarComboPorId(comboTecnico, idTecnico);

        // Preencho os campos com os dados da linha para o usuário editar
        tfAbertura.setText((String) modeloTabela.getValueAt(linha, 3));
        tfPrazo.setText((String) modeloTabela.getValueAt(linha, 4));

        // Seleciono o status correto no ComboBox
        String status = (String) modeloTabela.getValueAt(linha, 5);
        for (int i = 0; i < comboStatus.getItemCount(); i++) {
            if (comboStatus.getItemAt(i).equals(status)) {
                comboStatus.setSelectedIndex(i);
                break;
            }
        }

        JOptionPane.showMessageDialog(this,
            "OS nº " + idOsEditando + " carregada para edição!\nAltere os campos e clique em ATUALIZAR.",
            "Editar OS", JOptionPane.INFORMATION_MESSAGE);
        if (btnCancelar != null) btnCancelar.setText("CANCELAR");
    }

    // Ação do botão Excluir — pede confirmação e deleta a OS do banco
    private void onExcluir(int linha) {
        int idOs = (int) modeloTabela.getValueAt(linha, 0); // pego o ID da OS
        int ok = JOptionPane.showConfirmDialog(this,
            "Deseja excluir a OS nº " + idOs + "?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
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

    // ─────────────────────────────────────────────────────────────
    // FORMULÁRIO DE CADASTRO DE OS (painel superior)
    // ─────────────────────────────────────────────────────────────

    // Monta todos os campos e botões do formulário usando GridBagLayout
    private void montarLayoutCrud(JPanel p) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 15, 10, 15); // espaçamento entre os componentes
        g.anchor = GridBagConstraints.WEST;

        // Título do painel
        JLabel titulo = new JLabel("PAINEL DE SERVIÇOS");
        if (exo2SemiBold != null) titulo.setFont(exo2SemiBold.deriveFont(28f));
        else titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(COR_TITULO);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 7;
        p.add(titulo, g);

        // ── Linha 1: Número da OS | Cliente | Técnico | Botão Inserir ──
        g.gridwidth = 1; g.gridy = 1; g.gridx = 0;
        p.add(criarLabel("OS Nº:"), g);

        // Campo OS: somente leitura, preenchido automaticamente pelo banco
        tfOs = criarTextField(5);
        tfOs.setEditable(false);
        tfOs.setToolTipText("Gerado automaticamente pelo sistema");
        g.gridx = 1; p.add(tfOs, g);

        g.gridx = 2; p.add(criarLabel("CLIENTE:"), g);

        // Campo de pesquisa de cliente
        // Aqui o usuário digita o nome, telefone ou CPF/CNPJ e o sistema mostra os resultados
        tfClientePesquisa = criarTextField(18);
        tfClientePesquisa.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tfClientePesquisa.setPreferredSize(new Dimension(220, 42));
        tfClientePesquisa.setMinimumSize(new Dimension(220, 42));
        tfClientePesquisa.setToolTipText("Digite o nome, telefone ou CPF/CNPJ do cliente");

        // Popup que aparece embaixo do campo com os clientes encontrados
        popupClientes = new JPopupMenu();
        popupClientes.setFocusable(false);

        // Toda vez que o usuário digita, busco clientes parecidos no banco
        tfClientePesquisa.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                pesquisarClientesEnquantoDigita();
            }
        });

        g.gridx = 3; p.add(tfClientePesquisa, g);

        g.gridx = 4; p.add(criarLabel("TÉCNICO:"), g);

        // ComboBox de técnicos: busco pelo ID real do banco, mas mostro uma ordem visual sem pular número
        comboTecnico = new JComboBox<>();
        comboTecnico.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboTecnico.setPreferredSize(new Dimension(180, 42));
        carregarCombo(comboTecnico, "SELECT idTECNICO, nome FROM tecnico ORDER BY idTECNICO ASC");
        g.gridx = 5; p.add(comboTecnico, g);

        // Botão INSERIR: salva uma nova OS no banco
        JButton btnInserir = criarBotaoAcao("INSERIR", VERDE_MTEC, Color.BLACK);
        TooltipUtils.aplicarTooltipPadrao(btnInserir);
        btnInserir.addActionListener(e -> onInserir());
        g.gridx = 6; p.add(btnInserir, g);

        // ── Linha 2: Status | Data Abertura | Prazo | Atualizar | Cancelar ──
        g.gridy = 2; g.gridx = 0;
        p.add(criarLabel("STATUS:"), g);

        // ComboBox de status da OS com as opções do fluxo de atendimento
        comboStatus = new JComboBox<>(new String[]{
            "Aguardando Aprovação", // cliente ainda não aprovou
            "Em Andamento",         // técnico está executando
            "Aguardando Peça",      // parado esperando peça chegar
            "Concluído",            // serviço finalizado
            "Cancelado"             // OS cancelada
        });
        comboStatus.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboStatus.setPreferredSize(new Dimension(210, 42));
        g.gridx = 1; p.add(comboStatus, g);

        g.gridx = 2; p.add(criarLabel("ABERTURA:"), g);
        tfAbertura = criarTextFieldData(); // campo com máscara DD/MM/AAAA
        g.gridx = 3; p.add(tfAbertura, g);

        g.gridx = 4; p.add(criarLabel("PRAZO:"), g);
        tfPrazo = criarTextFieldData(); // campo com máscara DD/MM/AAAA
        g.gridx = 5; p.add(tfPrazo, g);

        // Botão ATUALIZAR: salva as alterações de uma OS existente no banco
        JButton btnAtualizar = criarBotaoAcao("ATUALIZAR", new Color(60, 130, 180), Color.WHITE);
        TooltipUtils.aplicarTooltipPadrao(btnAtualizar);
        btnAtualizar.addActionListener(e -> onAtualizar());
        g.gridx = 6; p.add(btnAtualizar, g);

        // Botão CANCELAR: limpa todos os campos do formulário
        btnCancelar = criarBotaoAcao("APAGAR", VERMELHO_MTEC, Color.WHITE);
        TooltipUtils.aplicarTooltipPadrao(btnCancelar);
        btnCancelar.addActionListener(e -> onCancelar());
        g.gridx = 7; p.add(btnCancelar, g);
    }

    // ─────────────────────────────────────────────────────────────
    // AÇÕES DOS BOTÕES DO FORMULÁRIO
    // ─────────────────────────────────────────────────────────────

    // Chamado quando clica em INSERIR — valida e salva nova OS no banco
    private void onInserir() {
        try {
            // Verifico se os campos obrigatórios foram preenchidos
            // O cliente precisa ter sido escolhido na lista de resultados, não apenas digitado
            if (idClienteSelecionado <= 0 || comboTecnico.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Selecione um Cliente válido e um Técnico!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Monto o objeto Servico com os dados do formulário
            Servico os = new Servico();
            os.setClienteIdCliente(idClienteSelecionado); // pego o ID do cliente selecionado na pesquisa
            os.setTecnicoIdTecnico(extrairId(comboTecnico));
            os.setStatusOs((String) comboStatus.getSelectedItem());
            os.setDataAbertura(converterData(tfAbertura.getText())); // converto DD/MM/AAAA para Date
            os.setPrazoEntrega(converterData(tfPrazo.getText()));

            // Salvo no banco usando o ServicoDAO
            new ServicoDAO().adicionar(os);

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

    // Chamado quando clica em ATUALIZAR — salva alterações de uma OS existente
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
            if (idClienteSelecionado <= 0 || comboTecnico.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Selecione um Cliente válido e um Técnico!",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Monto o objeto com os novos dados do formulário
            Servico os = new Servico();
            os.setIdSERVICO(idOsEditando);
            os.setClienteIdCliente(idClienteSelecionado);
            os.setTecnicoIdTecnico(extrairId(comboTecnico));
            os.setStatusOs((String) comboStatus.getSelectedItem());
            os.setDataAbertura(converterData(tfAbertura.getText()));
            os.setPrazoEntrega(converterData(tfPrazo.getText()));

            // Atualizo no banco usando o ServicoDAO
            new ServicoDAO().alterar(os);

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

    // Chamado quando clica em CANCELAR — limpa todos os campos do formulário
    private void onCancelar() {
        tfOs.setText("");                 // limpo o campo de número da OS
        tfPrazo.setText("");              // limpo o prazo
        tfAbertura.setText("");           // limpo a data de abertura
        tfClientePesquisa.setText("");    // limpo o campo de pesquisa de cliente
        idClienteSelecionado = -1;        // removo o cliente selecionado

        // Escondo o popup de clientes, caso ele esteja aberto
        if (popupClientes != null) {
            popupClientes.setVisible(false);
        }

        comboTecnico.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        if (btnCancelar != null) btnCancelar.setText("APAGAR");
        idOsEditando = -1; // indico que não está mais editando nenhuma OS
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date d = sdf.parse(texto);
            return new java.sql.Date(d.getTime());
        } catch (Exception e) {
            return null; // se a data estiver vazia ou inválida, retorno null
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PESQUISA DE CLIENTES COM AUTOCOMPLETE
    // ─────────────────────────────────────────────────────────────

    // Pesquisa clientes no banco conforme o usuário digita
    // O usuário pode pesquisar por nome, telefone ou CPF/CNPJ
    private void pesquisarClientesEnquantoDigita() {
        String termo = tfClientePesquisa.getText().trim();

        // Sempre que o usuário digita, limpo o ID selecionado
        // Assim evito salvar uma OS com um cliente antigo sem querer
        idClienteSelecionado = -1;

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

    // Carrega os dados do banco dentro do ComboBox de técnico
    // O ID real fica guardado por trás, mas na tela eu mostro uma ordem visual sem pular número
    private void carregarCombo(JComboBox<ItemCombo> combo, String sql) {
        try {
            // Conecto ao banco usando o ConnectionFactory do projeto
            Connection con = new ConnectionFactory().getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            combo.removeAllItems(); // limpo o combo antes de adicionar
            combo.addItem(new ItemCombo(0, "Selecione")); // opção padrão vazia

            int numeroVisual = 1; // número que aparece na tela, sem pular

            // Adiciono cada técnico mantendo o ID real do banco escondido no objeto
            while (rs.next()) {
                int idRealBanco = rs.getInt(1);
                String nomeTecnico = rs.getString(2);

                combo.addItem(new ItemCombo(idRealBanco, numeroVisual + " - " + nomeTecnico));
                numeroVisual++;
            }
            rs.close(); ps.close(); con.close(); // fecho os recursos do banco
        } catch (Exception e) {
            // Imprimo o erro no console para descobrir o que está falhando
            System.out.println("ERRO NA CONEXAO: " + e.getMessage());
            // Se der erro na conexão, mostro aviso no combo sem travar a tela
            combo.removeAllItems();
            combo.addItem(new ItemCombo(0, "(banco indisponível)"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTODOS AUXILIARES (usados em vários lugares)
    // ─────────────────────────────────────────────────────────────

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
        JTextField tf = new JTextField(col);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        tf.setPreferredSize(new Dimension(180, 42)); // largura e altura fixas
        tf.setMinimumSize(new Dimension(180, 42));
        return tf;
    }

    // Cria um JButton estilizado com as cores e tamanho padrão do sistema
    private JButton criarBotaoAcao(String txt, Color bg, Color fg) {
        JButton b = new JButton(txt);
        TooltipUtils.aplicarTooltipPadrao(b);
        b.setPreferredSize(new Dimension(185, 50));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);  // remove a borda de foco
        b.setBorderPainted(false); // remove a borda padrão
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
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
    // Só aceita números — as barras aparecem automaticamente
    private JTextField criarTextFieldData() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        tf.setPreferredSize(new Dimension(180, 42));
        tf.setMinimumSize(new Dimension(180, 42));
        tf.setToolTipText("DD/MM/AAAA"); // dica que aparece ao passar o mouse

        // Aplico o filtro de máscara de data no documento do campo
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new FiltroData());
        return tf;
    }

    // ─────────────────────────────────────────────────────────────
    // FILTRO DE DATA — formata automaticamente enquanto digita
    // ─────────────────────────────────────────────────────────────

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

    // Ponto de entrada do programa — abre a tela na thread correta do Swing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaServicos().setVisible(true));
    }
}
