package ui;

import dao.RelatoriosDAO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Painel "Dashboard" - reune indicadores e 5 graficos
 * (todos consultando o banco de dados).
 *
 *  IMPORTANTE: a biblioteca JFreeChart e usada APENAS para
 *  desenhar os graficos - nao tem nada a ver com banco.
 *  Os dados de cada grafico vem da RelatoriosDAO -> JDBC -> SQL.
 */
public class PainelDashboard extends JPanel {

    private final RelatoriosDAO dao = new RelatoriosDAO();

    private final JLabel lblTotalClientes    = new JLabel("Clientes: ...");
    private final JLabel lblTotalProdutos    = new JLabel("Produtos: ...");
    private final JLabel lblTotalFuncs       = new JLabel("Funcionarios: ...");
    private final JLabel lblTotalVendas      = new JLabel("Vendas: ...");
    private final JLabel lblPrecoMedio       = new JLabel("Preco medio: ...");

    private final JPanel painelGraficos = new JPanel(new GridLayout(2, 3, 8, 8));

    public PainelDashboard() {
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // Cabecalho com indicadores numericos
        JPanel painelIndicadores = new JPanel(new GridLayout(1, 5, 8, 8));
        painelIndicadores.setBorder(BorderFactory.createTitledBorder("Indicadores resumidos"));
        for (JLabel l : new JLabel[]{ lblTotalClientes, lblTotalProdutos,
                                      lblTotalFuncs, lblTotalVendas, lblPrecoMedio }) {
            l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            painelIndicadores.add(l);
        }

        JButton btAtualizar = new JButton("Atualizar dashboard");
        btAtualizar.addActionListener(e -> atualizar());

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(painelIndicadores, BorderLayout.CENTER);
        topo.add(btAtualizar,        BorderLayout.EAST);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(painelGraficos), BorderLayout.CENTER);

        atualizar();
    }

    /** Recarrega dados e regera os graficos. */
    public void atualizar() {
        try {
            // ---- indicadores ---------------------------------------------
            lblTotalClientes.setText("Clientes: "    + dao.contarRegistros("cliente"));
            lblTotalProdutos.setText("Produtos: "    + dao.contarRegistros("produto"));
            lblTotalFuncs.setText("Funcionarios: "   + dao.contarRegistros("funcionario"));
            lblTotalVendas.setText("Vendas: "        + dao.contarRegistros("vende"));
            BigDecimal media = dao.mediaPrecoProdutos();
            lblPrecoMedio.setText("Preco medio: R$ " + media);

            // ---- graficos -----------------------------------------------
            painelGraficos.removeAll();
            painelGraficos.add(criarGraficoBarras(
                    "Vendas por funcionario", "Funcionario", "Qtd",
                    dao.dashboardVendasPorFuncionario()));
            painelGraficos.add(criarGraficoPizza(
                    "Vendas por forma de pagamento",
                    dao.dashboardVendasPorPagamento()));
            painelGraficos.add(criarGraficoLinha(
                    "Vendas por dia", "Data", "Qtd",
                    dao.dashboardVendasPorDia()));
            painelGraficos.add(criarGraficoBarras(
                    "Top 5 produtos mais vendidos", "Produto", "Qtd",
                    dao.dashboardTopProdutos()));
            painelGraficos.add(criarGraficoBarras(
                    "Vendas por filial", "CNPJ filial", "Qtd",
                    dao.dashboardVendasPorFilial()));

            painelGraficos.revalidate();
            painelGraficos.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    /* ============== fabricas de graficos JFreeChart =================== */

    private ChartPanel criarGraficoBarras(String titulo, String eixoX, String eixoY,
                                          Map<String,Integer> dados) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String,Integer> e : dados.entrySet()) {
            dataset.addValue(e.getValue(), eixoY, e.getKey());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                titulo, eixoX, eixoY, dataset,
                PlotOrientation.VERTICAL, false, true, false);
        return new ChartPanel(chart);
    }

    private ChartPanel criarGraficoLinha(String titulo, String eixoX, String eixoY,
                                         Map<String,Integer> dados) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String,Integer> e : dados.entrySet()) {
            dataset.addValue(e.getValue(), eixoY, e.getKey());
        }
        JFreeChart chart = ChartFactory.createLineChart(
                titulo, eixoX, eixoY, dataset,
                PlotOrientation.VERTICAL, false, true, false);
        return new ChartPanel(chart);
    }

    private ChartPanel criarGraficoPizza(String titulo, Map<String,Integer> dados) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String,Integer> e : dados.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }
        JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);
        return new ChartPanel(chart);
    }
}
