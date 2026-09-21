package DAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class frmSplash extends JFrame {

    private JProgressBar jProgressBar1;
    private JLabel lblCarregando;
    private Timer timer;

    public frmSplash() {
        // Configurações da janela
        setTitle("Carregando...");
        setSize(540, 233); //600,400
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);

        // Painel principal
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(102, 102, 102));

        // Logo
        JLabel lblLogo = new JLabel();
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            lblLogo.setIcon(new ImageIcon(getClass().getResource("/imagens/logo_1.png")));
        } catch (Exception e) {
            lblLogo.setText("[LOGO]");
        }

        // Label de carregamento
        lblCarregando = new JLabel("Inicializando ...");
        lblCarregando.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        lblCarregando.setForeground(Color.WHITE);
        lblCarregando.setHorizontalAlignment(SwingConstants.CENTER);

        // ProgressBar AZUL
        jProgressBar1 = new JProgressBar(0, 100);
        jProgressBar1.setForeground(new Color(0, 123, 255));  // 🔵 Cor azul
        jProgressBar1.setBackground(new Color(50, 50, 50));
        jProgressBar1.setStringPainted(true);
        jProgressBar1.setValue(0);

        // Montagem do layout
        painel.add(lblLogo, BorderLayout.CENTER);
        painel.add(lblCarregando, BorderLayout.SOUTH);
        painel.add(jProgressBar1, BorderLayout.PAGE_END);

        add(painel);

        // Iniciar animação
        iniciarAnimacao();
    }

    private void iniciarAnimacao() {
        timer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int valor = jProgressBar1.getValue();

                // Atualiza texto conforme progresso
                if (valor == 20) lblCarregando.setText("Carregando arquivos ...");
                else if (valor == 40) lblCarregando.setText("Carregando banco de dados ...");
                else if (valor == 60) lblCarregando.setText("Carregando cadastros ...");
                else if (valor == 80) lblCarregando.setText("Finalizando ...");

                if (valor < 100) {
                    jProgressBar1.setValue(valor + 2);
                } else {
                    timer.stop();
                    LoginFrame2();
                }
            }
        });
        timer.start();
    }

    private void LoginFrame2() {
    java.awt.EventQueue.invokeLater(() -> {
        try {
            System.out.println("🔐 [DEBUG] Abrindo tela de login...");
            LoginFrame2 telaLogin = new LoginFrame2();
            telaLogin.setVisible(true);
            dispose(); // Fecha o splash
            System.out.println(" Splash fechado, login aberto.");
        } catch (Exception ex) {
            System.err.println(" Ao abrir login: " + ex.getMessage());
            ex.printStackTrace();
        }
    });
}

    // Main para teste direto (opcional)
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new frmSplash().setVisible(true));
    }
}