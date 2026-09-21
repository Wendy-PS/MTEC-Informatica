/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package graficoLinha;

/**
 *
 * Este gráfico contempla a 
 */

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.block.BlockBorder;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class DashboardLinha extends JFrame {

    // Variáveis globais para as séries
    private XYSeries serieBrasil;
    private XYSeries serieArgentina;
    private XYSeries serieChile;

    public DashboardLinha() {
        // Criar dataset inicial
        XYDataset dataset = createDataset();

        // Criar gráfico
        JFreeChart chart = createChart(dataset);

        // ChartPanel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);

        add(chartPanel);

        pack();
        setTitle("Gráfico de linhas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel de filtros com checkboxes
        JPanel painelFiltros = new JPanel();

        JCheckBox chkBrasil = new JCheckBox("Brasil", true);
        JCheckBox chkArgentina = new JCheckBox("Argentina", true);
        JCheckBox chkChile = new JCheckBox("Chile", true);
        JCheckBox chkTodos = new JCheckBox("Todos", true);

        // Listener para atualizar o gráfico
        ActionListener atualizar = e -> {
            XYSeriesCollection novoDataset = new XYSeriesCollection();

            if (chkTodos.isSelected()) {
                // Se "Todos" estiver marcado, adiciona todas as séries
                novoDataset.addSeries(serieBrasil);
                novoDataset.addSeries(serieArgentina);
                novoDataset.addSeries(serieChile);
            } else {
                if (chkBrasil.isSelected()) novoDataset.addSeries(serieBrasil);
                if (chkArgentina.isSelected()) novoDataset.addSeries(serieArgentina);
                if (chkChile.isSelected()) novoDataset.addSeries(serieChile);
            }

            chart.getXYPlot().setDataset(novoDataset);
        };

        // Adiciona o listener em todos os checkboxes
        chkBrasil.addActionListener(atualizar);
        chkArgentina.addActionListener(atualizar);
        chkChile.addActionListener(atualizar);
        chkTodos.addActionListener(atualizar);

        // Adiciona os checkboxes ao painel
        painelFiltros.add(chkBrasil);
        painelFiltros.add(chkArgentina);
        painelFiltros.add(chkChile);
        painelFiltros.add(chkTodos);

        // Coloca o painel de filtros abaixo do gráfico
        add(painelFiltros, java.awt.BorderLayout.SOUTH);
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Pão com salame",
                "Idade",
                "Quantidade de pão",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Configura cores diferentes para cada série
        renderer.setSeriesPaint(0, Color.RED);    // Brasil
        renderer.setSeriesPaint(1, Color.BLUE);   // Argentina
        renderer.setSeriesPaint(2, Color.GREEN);  // Chile

        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));

        plot.setRenderer(renderer);

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(BlockBorder.NONE);
        }

        chart.setTitle(new TextTitle(
                "Consumo de pão com salame por idade",
                new Font("Arial", Font.BOLD, 18)
        ));

        return chart;
    }

    private XYDataset createDataset() {
        // Séries globais
        serieBrasil = new XYSeries("Brasil");
        serieBrasil.add(18, 10);
        serieBrasil.add(20, 80);
        serieBrasil.add(25, 3);

        serieArgentina = new XYSeries("Argentina");
        serieArgentina.add(18, 15);
        serieArgentina.add(20, 40);
        serieArgentina.add(25, 60);

        serieChile = new XYSeries("Chile");
        serieChile.add(18, 5);
        serieChile.add(20, 20);
        serieChile.add(25, 30);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(serieBrasil);
        dataset.addSeries(serieArgentina);
        dataset.addSeries(serieChile);

        return dataset;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            DashboardLinha dashboard = new DashboardLinha();
            dashboard.setVisible(true);
        });
    }
}
