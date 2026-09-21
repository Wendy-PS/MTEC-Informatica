package graficoBarraVertical;


/**
 * 
 *Este gráfico contemplará o gráfico de venda por produto/equipamento
 */

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.SwingWorker;
import java.util.Map;
import DAO.TecnicoDAO;

public class DashboardBarraVertical extends JPanel { // ← Estende JPanel

    private ChartPanel chartPanel; // Referência ao painel do gráfico para atualizações dinâmicas

    public DashboardBarraVertical() {
        setBackground(Color.WHITE);
        setLayout(new java.awt.BorderLayout());
        
        // Mostra um painel de "carregando" inicialmente enquanto busca dados do banco
        setBorder(BorderFactory.createTitledBorder("Carregando dados dos técnicos..."));
        
        // Carrega os dados do banco de dados em segundo plano (não trava a interface)
        carregarDadosDoBanco();
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTODO PRINCIPAL: Carrega dados do banco em thread separada
    // ═══════════════════════════════════════════════════════════
    // Usa SwingWorker para não bloquear a interface gráfica durante a consulta
    private void carregarDadosDoBanco() {
        SwingWorker<CategoryDataset, Void> worker = new SwingWorker<>() {
            @Override
            protected CategoryDataset doInBackground() throws Exception {
                // Cria o dataset buscando os dados reais do banco de dados
                return createDataset();
            }

            @Override
            protected void done() {
                try {
                    // Quando os dados estiverem prontos, atualiza o gráfico na tela
                    CategoryDataset dataset = get();
                    atualizarGrafico(dataset);
                } catch (Exception e) {
                    // Em caso de erro, mostra mensagem amigável no border do painel
                    setBorder(BorderFactory.createTitledBorder("❌ Erro ao carregar dados"));
                    System.err.println("Erro ao carregar dados do gráfico: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute(); // Inicia a tarefa em background
    }
    
    // ═══════════════════════════════════════════════════════════
    // Atualiza o gráfico na interface com os novos dados carregados
    // ═══════════════════════════════════════════════════════════
    private void atualizarGrafico(CategoryDataset dataset) {
        // Verifica se já existe um chartPanel para remover antes de adicionar o novo
        if (chartPanel != null) {
            remove(chartPanel);
        }
        
        // Cria o novo gráfico com os dados atualizados
        JFreeChart chart = createChart(dataset);

        // Configura o painel do gráfico com as mesmas propriedades visuais
        chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 30, 30));
        chartPanel.setBackground(Color.white);
        chartPanel.setOpaque(true);

        // Adiciona o novo painel do gráfico ao centro deste JPanel
        add(chartPanel, java.awt.BorderLayout.CENTER);
        
        // Atualiza o border para indicar que os dados foram carregados com sucesso
        setBorder(BorderFactory.createTitledBorder("Técnicos por Especialidade"));
        
        // Força o painel a se redesenhar com o novo conteúdo
        revalidate();
        repaint();
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                "Técnicos por Especialidade",   // título atualizado para refletir os dados reais
                "Especialidade",                        // eixo X (agora são as especialidades)
                "Quantidade de Técnicos",             // eixo Y atualizado
                dataset
        );
    
        // Personalização visual (fundo branco)
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setPaint(new Color(44, 62, 80));
    
        var plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);
    
        // Cor das barras
        var renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(52, 152, 219)); // Azul profissional
    
        return chart;
    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    
        // Série única: "Técnicos" (aparecerá na legenda)
        String serie = "Técnicos";
        
        // ═══════════════════════════════════════════════════════════
        // BUSCA OS DADOS REAIS DO BANCO DE DADOS via TecnicoDAO
        // ═══════════════════════════════════════════════════════════
        TecnicoDAO dao = new TecnicoDAO();
        Map<String, Integer> dadosEspecialidades = dao.buscarEstatisticasPorEspecialidade();
        
        // Percorre o mapa e adiciona cada especialidade com sua quantidade no dataset
        for (Map.Entry<String, Integer> entrada : dadosEspecialidades.entrySet()) {
            String especialidade = entrada.getKey();
            Integer quantidade = entrada.getValue();
            dataset.setValue(quantidade, serie, especialidade);
        }
        
        // Se não houver dados no banco, adiciona um valor de exemplo para não ficar vazio
        if (dataset.getColumnCount() == 0) {
            dataset.setValue(0, serie, "Sem dados");
        }
    
        return dataset;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTODO PÚBLICO: Permite atualizar o gráfico manualmente
    // Pode ser chamado quando um técnico for inserido/alterado/excluído
    // ═══════════════════════════════════════════════════════════
    public void atualizarDados() {
        carregarDadosDoBanco();
    }

// No final da classe DashboardBarraVertical.java

// ← Método main APENAS para testes unitários do gráfico
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("TÉCNICOS POR ESPECIALIDADE");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        //Torna o frame sem decorações e com fundo transparente
        frame.setUndecorated(true);  // Remove barra de título e bordas
        frame.setBackground(new Color(0, 0, 0, 0)); // Fundo transparente
        frame.getContentPane().setBackground(new Color(0, 0, 0, 0));
        
        DashboardBarraVertical dashboard = new DashboardBarraVertical();
        dashboard.setOpaque(false); // Torna o painel transparente
        frame.add(dashboard);
        
        frame.setVisible(true);
    });
}
}