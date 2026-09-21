package DAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame2 extends JFrame {

    class FieldPanel extends JPanel {
        public Color borderColor = new Color(255, 255, 255, 60);

        public FieldPanel() {
            super(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

            g2.dispose();
        }
    }

    class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setFont(new Font("SansSerif", Font.PLAIN, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 180, 180));
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2.drawString(placeholder, 0, y);
                g2.dispose();
            }
        }
    }

    class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;

        public PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(null);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setFont(new Font("SansSerif", Font.PLAIN, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (String.valueOf(getPassword()).isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 180, 180));
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2.drawString(placeholder, 0, y);
                g2.dispose();
            }
        }
    }

    private int buscarIdUsuario(String usuario, String senha) {
        String sql = "SELECT idTECNICO FROM tecnico WHERE login = ? AND senha = ? AND status = 'Ativo' LIMIT 1";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, senha);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idTECNICO");
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar ID do técnico: " + e.getMessage());
        }

        return -1;
    }

    private String buscarNomeTecnico(String usuario, String senha) {
        String sql = "SELECT nome FROM tecnico WHERE login = ? AND senha = ? AND status = 'Ativo' LIMIT 1";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, senha);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome");
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar nome do técnico: " + e.getMessage());
        }

        return usuario;
    }

    private boolean validarLoginNoBanco(String usuario, String senha) {
        String sql = "SELECT idTECNICO FROM tecnico WHERE login = ? AND senha = ? AND status = 'Ativo' LIMIT 1";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, senha);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao validar login no banco: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void shakeComponent(Component comp) {
        final int originalX = comp.getX();

        Timer timer = new Timer(30, null);
        timer.addActionListener(new ActionListener() {
            int count = 0;
            int[] offsets = {8, -8, 6, -6, 4, -4, 2, -2, 0};

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < offsets.length) {
                    comp.setLocation(originalX + offsets[count], comp.getY());
                    count++;
                } else {
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    public LoginFrame2() {
        setTitle("MTEC Informática - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(null);

        URL imgUrl = getClass().getResource("/imagens/background.jpg");
        if (imgUrl != null) {
            JLabel background = new JLabel(new ImageIcon(imgUrl));
            background.setBounds(0, 0, 1000, 600);
            setContentPane(background);
            background.setLayout(null);
        }

        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint glassGrad = new GradientPaint(
                        0, 0, new Color(10, 35, 65, 230),
                        0, getHeight(), new Color(0, 140, 140, 200)
                );

                g2.setPaint(glassGrad);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40));

                g2.setColor(new Color(0, 200, 200, 100));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 40, 40));

                g2.dispose();
            }
        };

        glassPanel.setOpaque(false);
        glassPanel.setBounds(300, 40, 400, 500);
        glassPanel.setLayout(null);
        add(glassPanel);

        URL logoUrl = getClass().getResource("/imagens/logo_mtec_1.png");
        if (logoUrl != null) {
            ImageIcon logoIcon = new ImageIcon(logoUrl);

            int larguraOriginal = logoIcon.getIconWidth();
            int alturaOriginal = logoIcon.getIconHeight();

            int larguraMaxima = 315;
            int alturaMaxima = 115;

            double escalaLargura = (double) larguraMaxima / larguraOriginal;
            double escalaAltura = (double) alturaMaxima / alturaOriginal;
            double escalaFinal = Math.min(escalaLargura, escalaAltura);

            int larguraFinal = (int) (larguraOriginal * escalaFinal);
            int alturaFinal = (int) (alturaOriginal * escalaFinal);

            int posXLogo = ((400 - larguraFinal) / 2) + 2;
            int posYLogo = 32;

            Image scaledLogo = logoIcon.getImage().getScaledInstance(larguraFinal, alturaFinal, Image.SCALE_SMOOTH);
            JLabel lblLogoImg = new JLabel(new ImageIcon(scaledLogo));
            lblLogoImg.setBounds(posXLogo, posYLogo, larguraFinal, alturaFinal);

            glassPanel.add(lblLogoImg);
        }

        JLabel lblUserTitle = new JLabel("USUÁRIO");
        lblUserTitle.setForeground(new Color(200, 200, 200));
        lblUserTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUserTitle.setBounds(50, 140, 100, 20);
        glassPanel.add(lblUserTitle);

        FieldPanel userFieldPanel = new FieldPanel();
        userFieldPanel.setBounds(50, 165, 300, 45);
        glassPanel.add(userFieldPanel);

        URL userIconUrl = getClass().getResource("/imagens/user_icon.png");
        if (userIconUrl != null) {
            ImageIcon userIcon = new ImageIcon(userIconUrl);
            JLabel lblUserIcon = new JLabel(new ImageIcon(
                    userIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
            ));
            userFieldPanel.add(lblUserIcon, BorderLayout.WEST);
        }

        PlaceholderTextField txtUser = new PlaceholderTextField("Digite seu usuário");
        LimiteCampos.aplicar(txtUser, 45);
        userFieldPanel.add(txtUser, BorderLayout.CENTER);

        JLabel lblPassTitle = new JLabel("SENHA");
        lblPassTitle.setForeground(new Color(200, 200, 200));
        lblPassTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPassTitle.setBounds(50, 230, 100, 20);
        glassPanel.add(lblPassTitle);

        FieldPanel passFieldPanel = new FieldPanel();
        passFieldPanel.setBounds(50, 255, 300, 45);
        glassPanel.add(passFieldPanel);

        URL lockIconUrl = getClass().getResource("/imagens/lock_icon.png");
        if (lockIconUrl != null) {
            ImageIcon lockIcon = new ImageIcon(lockIconUrl);
            JLabel lblLockIcon = new JLabel(new ImageIcon(
                    lockIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
            ));
            passFieldPanel.add(lblLockIcon, BorderLayout.WEST);
        }

        PlaceholderPasswordField txtPass = new PlaceholderPasswordField("Digite sua senha");
        LimiteCampos.aplicar(txtPass, 100);
        txtPass.setEchoChar('•');
        passFieldPanel.add(txtPass, BorderLayout.CENTER);

        JLabel lblOlhoSenha = new JLabel("👁");
        lblOlhoSenha.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblOlhoSenha.setForeground(Color.WHITE);
        lblOlhoSenha.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblOlhoSenha.setToolTipText("Mostrar senha");
        passFieldPanel.add(lblOlhoSenha, BorderLayout.EAST);

        final boolean[] senhaVisivel = {false};

        lblOlhoSenha.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                senhaVisivel[0] = !senhaVisivel[0];

                if (senhaVisivel[0]) {
                    txtPass.setEchoChar((char) 0);
                    lblOlhoSenha.setText("🙈");
                    lblOlhoSenha.setToolTipText("Ocultar senha");
                } else {
                    txtPass.setEchoChar('•');
                    lblOlhoSenha.setText("👁");
                    lblOlhoSenha.setToolTipText("Mostrar senha");
                }
            }
        });

        JLabel lblForgot = new JLabel("Esqueci minha senha?", SwingConstants.RIGHT);
        lblForgot.setForeground(Color.WHITE);
        lblForgot.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblForgot.setBounds(50, 305, 300, 20);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        glassPanel.add(lblForgot);

        lblForgot.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame2.this,
                        "<html>"
                        + "<div style='width: 330px; font-family: SansSerif; font-size: 12px;'>"
                        + "<b>Recuperação de senha</b><br><br>"
                        + "Por segurança, solicite ao administrador a redefinição da sua senha.<br><br>"
                        + "📞(27) 99589-3897<br>"                   
                        + "</div>"
                        + "</html>",
                        "Esqueci minha senha",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JLabel lblError = new JLabel("É necessário preencher todos os campos!", SwingConstants.CENTER);
        lblError.setForeground(new Color(255, 100, 100));
        lblError.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblError.setBounds(50, 350, 300, 20);
        lblError.setVisible(false);
        glassPanel.add(lblError);

        JButton btnLogin = new JButton("ENTRAR  →") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint btnGrad = new GradientPaint(
                        0, 0, new Color(0, 180, 180),
                        getWidth(), 0, new Color(0, 90, 110)
                );

                g2.setPaint(btnGrad);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 4;

                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        btnLogin.setBounds(50, 390, 300, 55);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setToolTipText("Entrar no sistema.");
        glassPanel.add(btnLogin);

        btnLogin.addActionListener(e -> {
            String usuario = txtUser.getText().trim();
            String senha = String.valueOf(txtPass.getPassword()).trim();

            boolean userEmpty = usuario.isEmpty();
            boolean passEmpty = senha.isEmpty();

            if (userEmpty || passEmpty) {
                lblError.setText("É necessário preencher todos os campos!");
                lblError.setVisible(true);

                if (userEmpty) {
                    userFieldPanel.borderColor = Color.RED;
                    shakeComponent(userFieldPanel);
                } else {
                    userFieldPanel.borderColor = new Color(255, 255, 255, 60);
                }

                if (passEmpty) {
                    passFieldPanel.borderColor = Color.RED;
                    shakeComponent(passFieldPanel);
                } else {
                    passFieldPanel.borderColor = new Color(255, 255, 255, 60);
                }

                userFieldPanel.repaint();
                passFieldPanel.repaint();
                return;
            }

            if (validarLoginNoBanco(usuario, senha)) {
                lblError.setVisible(false);
                userFieldPanel.borderColor = new Color(255, 255, 255, 60);
                passFieldPanel.borderColor = new Color(255, 255, 255, 60);
                userFieldPanel.repaint();
                passFieldPanel.repaint();

                // Login correto: entra direto no sistema, sem mostrar mensagem de sucesso.

                TelaPrincipal.Sessao.idUsuario = buscarIdUsuario(usuario, senha);
                TelaPrincipal.Sessao.nomeUsuario = buscarNomeTecnico(usuario, senha);
                TelaPrincipal.Sessao.login = usuario;

                new TelaPrincipal().setVisible(true);
                this.dispose();

            } else {
                lblError.setText("Usuário ou senha incorretos!");
                lblError.setVisible(true);

                userFieldPanel.borderColor = Color.RED;
                passFieldPanel.borderColor = Color.RED;
                userFieldPanel.repaint();
                passFieldPanel.repaint();

                shakeComponent(userFieldPanel);
                shakeComponent(passFieldPanel);
            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> new LoginFrame2().setVisible(true));
    }
}
