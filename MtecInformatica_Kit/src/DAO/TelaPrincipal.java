package DAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TelaPrincipal extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel painelTelas;
    private JPanel sidebar;
    private JPanel areaCentral;
    private TopMenuBar topMenuBar;
    private List<BotaoMenu> botoesMenu;
    private PainelOrcamentos painelOrcamentos;
    private PainelServicos painelServicos;
    private PainelRelatorioServicos painelRelatorio;
    private PainelDashboard painelDashboard;

    interface ElementoResponsivo {
        void setEscala(double scale);
    }
    
    public static class Sessao {
        public static int idUsuario = -1;
        public static String nomeUsuario = null;
        public static String login = null;
    }

    public TelaPrincipal() {
        botoesMenu = new ArrayList<>();
        configurarJanela();

        JPanel painelFundo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharImagem(g, "/imagensTemplate/Background.png", 0, 0, getWidth(), getHeight());
            }
        };
        painelFundo.setLayout(new BorderLayout());
        setContentPane(painelFundo);
        
        sidebar = criarSidebar();
        painelFundo.add(sidebar, BorderLayout.WEST);

        areaCentral = new JPanel(new BorderLayout());
        areaCentral.setOpaque(false);
        areaCentral.setBorder(new EmptyBorder(10, 14, 10, 14));

        topMenuBar = new TopMenuBar();
        areaCentral.add(topMenuBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        painelTelas = new JPanel(cardLayout);
        painelTelas.setOpaque(false);

        // -- Telas internas adicionadas aqui --
        painelTelas.add(criarTelaProvisoria("MTEC - Bem-vindo"), "vazio");
        painelOrcamentos = new PainelOrcamentos();
        painelServicos = new PainelServicos();

        painelDashboard = new PainelDashboard();
        painelTelas.add(painelDashboard, "Dashboard");
        painelTelas.add(new PainelClientes(), "clientes");
        painelTelas.add(new PainelProdutos(), "produtos");
        painelTelas.add(new PainelTecnicos(), "tecnicos");
        painelTelas.add(criarTelaTecnicosRestrita(), "tecnicos_restrito");
        painelTelas.add(painelOrcamentos, "orcamentos");
        painelTelas.add(painelServicos, "servicos");
        // Tela de relatório dos serviços concluídos
        painelRelatorio = new PainelRelatorioServicos();
        painelTelas.add(painelRelatorio, "relatorio"); 

        // Deixo as telas direto no centro, sem JScrollPane global.
        // Antes eu tinha colocado rolagem aqui para tentar ajudar em resoluções menores,
        // mas isso criava aquelas barras feias aparecendo em TODAS as telas.
        // O ajuste de resolução precisa ser feito pelos layouts internos, não por uma barra geral.
        areaCentral.add(painelTelas, BorderLayout.CENTER);
        painelFundo.add(areaCentral, BorderLayout.CENTER);

        ativarBotaoETela("Dashboard");

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarTamanhoElementos();
            }
        });
    }

    private void ajustarTamanhoElementos() {
        double scale = (double) getWidth() / 1280.0;
        if (scale < 0.72) scale = 0.72;
        if (scale > 1.20) scale = 1.20;

        double scaleSidebar = 1.0 + (scale - 1.0) * 0.4;
        int novaLarguraSidebar = Math.max(205, (int) (270 * scaleSidebar));
        sidebar.setPreferredSize(new Dimension(novaLarguraSidebar, 0));

        topMenuBar.setPreferredSize(new Dimension(0, (int) (75 * scaleSidebar)));
        topMenuBar.setEscala(scaleSidebar);

        // -- DIMINUIÇÃO DOS BOTÕES PARA CABER MAIS ITENS --
        // Em telas menores o menu encolhe um pouco e a área central ganha rolagem.
        Dimension dimBotao = new Dimension((int) (215 * scaleSidebar), (int) (45 * scaleSidebar));

        for (Component c : sidebar.getComponents()) {
            if (c instanceof BotaoMenu) {
                BotaoMenu btn = (BotaoMenu) c;
                btn.setPreferredSize(dimBotao);
                btn.setMinimumSize(dimBotao);
                btn.setMaximumSize(dimBotao);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setEscala(scaleSidebar);
            } else if (c instanceof ElementoResponsivo) {
                ((ElementoResponsivo) c).setEscala(scaleSidebar);
            }
        }

        sidebar.revalidate();
        sidebar.repaint();
        areaCentral.revalidate();
    }

    private void adicionarBotaoMenu(JPanel p, String texto, String iconePath, String nomeTela) {
        BotaoMenu btn = new BotaoMenu(texto, iconePath, nomeTela);
        botoesMenu.add(btn);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ativarBotaoETela(nomeTela);
            }
        });

        p.add(btn);
    }

    private void ativarBotaoETela(String nomeTela) {
        for (BotaoMenu btn : botoesMenu) {
            btn.setSelecionado(btn.getNomeTela().equals(nomeTela));
        }

        if ("tecnicos".equals(nomeTela) && !usuarioAdministrador()) {
            cardLayout.show(painelTelas, "tecnicos_restrito");
            return;
        }

        if ("Dashboard".equals(nomeTela) && painelDashboard != null) {
            painelDashboard.atualizarDashboard();
        }

        if ("servicos".equals(nomeTela) && painelServicos != null) {
            painelServicos.atualizarTela();
        }

        if ("relatorio".equals(nomeTela) && painelRelatorio != null) {
            painelRelatorio.atualizarTela();
        }

        cardLayout.show(painelTelas, nomeTela);
    }

    private boolean usuarioAdministrador() {
        return Sessao.login != null && Sessao.login.trim().equalsIgnoreCase("admin");
    }

    private JPanel criarTelaTecnicosRestrita() {
        JPanel painel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int largura = 560;
                int altura = 230;
                int x = (getWidth() - largura) / 2;
                int y = (getHeight() - altura) / 2;

                g2.setColor(new Color(20, 43, 66, 210));
                g2.fillRoundRect(x, y, largura, altura, 18, 18);

                g2.setColor(new Color(0, 200, 200, 180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, y, largura, altura, 18, 18);

                g2.dispose();
            }
        };

        painel.setOpaque(false);

        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.gridx = 0;
        g.anchor = GridBagConstraints.CENTER;

        JLabel icone = new JLabel("🔒");
        icone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 38));
        icone.setHorizontalAlignment(SwingConstants.CENTER);
        icone.setVerticalAlignment(SwingConstants.CENTER);
        icone.setPreferredSize(new Dimension(90, 58));
        icone.setMinimumSize(new Dimension(90, 58));
        g.gridy = 0;
        conteudo.add(icone, g);

        JLabel titulo = new JLabel("ACESSO RESTRITO");
        titulo.setForeground(new Color(128, 203, 203));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g.gridy = 1;
        conteudo.add(titulo, g);

        JLabel texto1 = new JLabel("A tela de técnicos só pode ser acessada pelo administrador.");
        texto1.setForeground(Color.WHITE);
        texto1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g.gridy = 2;
        conteudo.add(texto1, g);

        JLabel texto2 = new JLabel("Faça login como admin para cadastrar, editar ou visualizar técnicos.");
        texto2.setForeground(Color.WHITE);
        texto2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g.gridy = 3;
        conteudo.add(texto2, g);

        painel.add(conteudo);
        return painel;
    }

    private JPanel criarSidebar() {
        JPanel sb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(22, 38, 54));
                g.fillRect(0, 0, getWidth(), getHeight());
                desenharImagem(g, "/imagensTemplate/retangulo_lateral.png", 0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        sb.setOpaque(false);
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(new EmptyBorder(15, 0, 10, 0));

        sb.add(new PainelLogo("/imagensTemplate/logo_mtec.png", 220, 90));
        sb.add(new EspacadorMenu(30));

        // -- ESPAÇAMENTOS REDUZIDOS DE 18 PARA 10 --
        sb.add(new EspacadorMenu(10)); 
        
        adicionarBotaoMenu(sb, "DASHBOARD", "/imagensTemplate/ic_dashboard.png", "Dashboard");
        sb.add(new EspacadorMenu(10)); 
        
        adicionarBotaoMenu(sb, "CLIENTES", "/imagensTemplate/ic_clientes.png", "clientes");
        sb.add(new EspacadorMenu(10));

        adicionarBotaoMenu(sb, "PRODUTOS", "/imagensTemplate/ic_produtos.png", "produtos");
        sb.add(new EspacadorMenu(10));

        adicionarBotaoMenu(sb, "TÉCNICOS", "/imagensTemplate/ic_tecnicos.png", "tecnicos");
        sb.add(new EspacadorMenu(10));

        adicionarBotaoMenu(sb, "ORÇAMENTOS", "/imagensTemplate/ic_orcamentos.png", "orcamentos");
        sb.add(new EspacadorMenu(10));

        adicionarBotaoMenu(sb, "SERVIÇOS", "/imagensTemplate/ic_servicos.png", "servicos");
        sb.add(new EspacadorMenu(10));

        // Botão do relatório dos serviços concluídos
        adicionarBotaoMenu(sb, "RELATÓRIO", "/imagensTemplate/ic_financeiro.png", "relatorio");

        return sb;
    }

    class TopMenuBar extends JPanel implements ElementoResponsivo {
        // ... (Mantenha o código exato da sua TopMenuBar atual) ...
        private JLabel lblTitulo;
        private JLabel lblUsuario;
        private double scale = 1.0;
        private ImageIcon originalUserIcon;

        public TopMenuBar() {
            setLayout(new BorderLayout());
            setOpaque(false);
            lblTitulo = new JLabel("   GERENCIAMENTO DE CHAMADOS - MTEC");
            lblTitulo.setForeground(new Color(0, 200, 200));
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            add(lblTitulo, BorderLayout.WEST);

            JPanel painelPerfil = new JPanel(new GridBagLayout());
            painelPerfil.setOpaque(false);
            painelPerfil.setBorder(new EmptyBorder(0, 0, 0, 25));

            lblUsuario = new JLabel();
            lblUsuario.setCursor(new Cursor(Cursor.HAND_CURSOR));

            try {
                URL urlIconeUser = getClass().getResource("/imagensTemplate/ic_usuario.png");
                if (urlIconeUser != null) {
                    originalUserIcon = new ImageIcon(urlIconeUser);
                }
            } catch (Exception ignored) {}

            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.setBackground(new Color(22, 38, 54));
            popupMenu.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 200), 1));

            JMenuItem menuUser = new JMenuItem("Usuário: " + 
                (TelaPrincipal.Sessao.nomeUsuario != null ? TelaPrincipal.Sessao.nomeUsuario : "Convidado"));
            JMenuItem menuLogin = new JMenuItem("Login: " + 
                (TelaPrincipal.Sessao.login != null ? TelaPrincipal.Sessao.login : "N/A"));
            
            JMenuItem menuTrocar = new JMenuItem("Troca de Usuário");
            menuTrocar.setForeground(new Color(255, 80, 80));
            menuTrocar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            menuTrocar.addActionListener(e -> {
                popupMenu.setVisible(false);
                int confirm = JOptionPane.showConfirmDialog(
                    TelaPrincipal.this, "Deseja realmente trocar de usuário?",
                    "Troca de Usuário", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    TelaPrincipal.Sessao.idUsuario = -1;
                    TelaPrincipal.Sessao.nomeUsuario = null;
                    TelaPrincipal.Sessao.login = null;

                    new LoginFrame2().setVisible(true);
                    dispose();
                }
            });

            JMenuItem menuSair = new JMenuItem("Sair do Sistema");
            menuSair.setForeground(new Color(255, 80, 80));
            menuSair.setFont(new Font("Segoe UI", Font.BOLD, 14));
            menuSair.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(null, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) System.exit(0);
            });

            Font popupFont = new Font("Segoe UI", Font.PLAIN, 14);
            Color textColor = Color.WHITE;
            Color bgColor = new Color(22, 38, 54);

            for (JMenuItem item : new JMenuItem[]{menuUser, menuLogin, menuTrocar, menuSair}) {
                item.setBackground(bgColor);
                item.setForeground(textColor);
                item.setFont(popupFont);
            }
            
            menuTrocar.setForeground(new Color(255, 80, 80));
            menuTrocar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            menuSair.setForeground(new Color(255, 80, 80));
            menuSair.setFont(new Font("Segoe UI", Font.BOLD, 14));

            menuUser.setEnabled(false);
            menuLogin.setEnabled(false);

            popupMenu.add(menuUser);
            popupMenu.add(menuLogin);
            popupMenu.addSeparator();
            popupMenu.add(menuTrocar);
            popupMenu.add(menuSair);

            lblUsuario.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    popupMenu.show(lblUsuario, -50, lblUsuario.getHeight() + 5);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (originalUserIcon == null) lblUsuario.setForeground(new Color(0, 200, 200));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (originalUserIcon == null) lblUsuario.setForeground(Color.WHITE);
                }
            });

            painelPerfil.add(lblUsuario);
            add(painelPerfil, BorderLayout.EAST);
            atualizarIcone(1.0);
        }

        private void atualizarIcone(double currentScale) {
            if (originalUserIcon != null) {
                int size = (int) (40 * currentScale);
                Image img = originalUserIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                lblUsuario.setIcon(new ImageIcon(img));
                lblUsuario.setText("");
            } else {
                lblUsuario.setText("👤");
                lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, (int) (32 * currentScale)));
                lblUsuario.setForeground(Color.WHITE);
            }
        }

        @Override
        public void setEscala(double scale) {
            this.scale = scale;
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, (int) (22 * scale)));
            atualizarIcone(scale);
            repaint();
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = (int) (15 * scale);
            g2.setColor(new Color(22, 45, 65, 220));
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.setStroke(new BasicStroke(1.5f * (float) scale));
            g2.setColor(new Color(0, 200, 200));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
        }
    }

    class BotaoMenu extends JPanel {
        private String texto;
        private Image icone;
        private String nomeTela;
        private boolean selecionado = false;
        private boolean hover = false;
        private double scale = 1.0;

        public BotaoMenu(String texto, String caminhoIcone, String nomeTela) {
            this.texto = texto;
            this.nomeTela = nomeTela;
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            try {
                URL url = getClass().getResource(caminhoIcone);
                if (url != null) this.icone = new ImageIcon(url).getImage();
            } catch (Exception ignored) {}

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        public String getNomeTela() { return nomeTela; }
        public void setSelecionado(boolean selecionado) { this.selecionado = selecionado; repaint(); }
        public void setEscala(double scale) { this.scale = scale; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int w = getWidth(), h = getHeight();
            int arc = (int) (15 * scale);
            Color corCiano = new Color(0, 200, 200);

            if (selecionado) {
                g2.setColor(new Color(0, 200, 200, 110));
                g2.fillRoundRect(1, 1, w - 3, h - 3, arc, arc);
                g2.setStroke(new BasicStroke(2.5f * (float) scale));
            } else if (hover) {
                g2.setColor(new Color(0, 200, 200, 45));
                g2.fillRoundRect(1, 1, w - 3, h - 3, arc, arc);
                g2.setStroke(new BasicStroke(1.5f * (float) scale));
            } else {
                g2.setStroke(new BasicStroke(1.5f * (float) scale));
            }

            g2.setColor(corCiano);
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            int divX = (int) (65 * scale); // Área maior para os ícones novos do template
            g2.setColor(new Color(0, 200, 200, 150));
            g2.drawLine(divX, (int) (8 * scale), divX, h - (int) (9 * scale));

            int iconAreaSize = (int) Math.min(h - (14 * scale), 42 * scale); // Ícones maiores e mais visíveis
            if (icone != null) {
                int origW = icone.getWidth(null), origH = icone.getHeight(null);
                int drawW = iconAreaSize, drawH = iconAreaSize;
                if (origW > 0 && origH > 0) {
                    if (origW > origH) drawH = (iconAreaSize * origH) / origW;
                    else drawW = (iconAreaSize * origW) / origH;
                }
                int iconX = (divX - drawW) / 2;
                int iconY = (h - drawH) / 2;
                g2.drawImage(icone, iconX, iconY, drawW, drawH, null);
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (16 * scale)));
                g2.drawString(texto.substring(0, 1), (divX - 12) / 2, (h + 12) / 2 - 2);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (16 * scale)));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(texto, divX + (int) (12 * scale), textY);
            g2.dispose();
        }
    }

    class PainelLogo extends JPanel implements ElementoResponsivo {
        private Image imgLogo;
        private int baseWidth, baseHeight;
        private double scale = 1.0;

        public PainelLogo(String path, int baseWidth, int baseHeight) {
            this.baseWidth = baseWidth;
            this.baseHeight = baseHeight;
            setOpaque(false);
            setAlignmentX(Component.CENTER_ALIGNMENT);
            URL url = getClass().getResource(path);
            if (url != null) imgLogo = new ImageIcon(url).getImage();
        }

        @Override
        public void setEscala(double scale) { this.scale = scale; revalidate(); repaint(); }
        @Override
        public Dimension getPreferredSize() { return new Dimension((int) (baseWidth * scale), (int) (baseHeight * scale)); }
        @Override
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, (int) (baseHeight * scale)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imgLogo != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int origW = imgLogo.getWidth(null), origH = imgLogo.getHeight(null);
                if (origW > 0) {
                    int targetW = (int) (baseWidth * scale);
                    if (targetW > w) targetW = w;
                    int finalW = targetW, finalH = (finalW * origH) / origW;
                    if (finalH > h) { finalH = h; finalW = (finalH * origW) / origH; }
                    int ajusteHorizontal = (int) (8 * scale);
                    int ajusteVertical = (int) (8 * scale);
                    int x = (w - finalW) / 2 + ajusteHorizontal;
                    int y = (h - finalH) / 2 + ajusteVertical;
                    g2.drawImage(imgLogo, x, y, finalW, finalH, null);
                }
                g2.dispose();
            }
        }
    }

    class EspacadorMenu extends JPanel implements ElementoResponsivo {
        private int baseHeight;
        private double scale = 1.0;
        public EspacadorMenu(int baseHeight) { this.baseHeight = baseHeight; setOpaque(false); }
        @Override
        public void setEscala(double scale) { this.scale = scale; revalidate(); }
        @Override
        public Dimension getPreferredSize() { return new Dimension(0, (int) (baseHeight * scale)); }
        @Override
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, (int) (baseHeight * scale)); }
    }

    private JPanel criarTelaProvisoria(String texto) {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);
        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        painel.add(label);
        return painel;
    }

    private void desenharImagem(Graphics g, String path, int x, int y, int w, int h) {
        URL url = getClass().getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(img, x, y, w, h, null);
            g2.dispose();
        }
    }

    private void configurarJanela() {
        setTitle("MTEC - Gerenciamento de Chamados");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Tiro a barra branca padrão do Windows.
        // O sistema já tem topo próprio, então a janela fica com cara de aplicativo em tela cheia.
        setUndecorated(true);
        setSize(1280, 720);
        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaPrincipal().setVisible(true));
    }
}
