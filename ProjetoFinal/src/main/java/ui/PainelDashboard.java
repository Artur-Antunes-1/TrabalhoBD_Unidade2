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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Painel "Dashboard" - reune indicadores e 5 graficos.
 *
 *  Inclui filtros interativos (periodo, filial, forma de pagamento) que
 *  sao aplicados em todos os graficos e no contador de vendas.
 *  Vale +0,5 EXTRA na Etapa 06.
 *
 *  IMPORTANTE: a biblioteca JFreeChart e usada APENAS para
 *  desenhar os graficos - nao tem nada a ver com banco.
 *  Os dados de cada grafico vem da RelatoriosDAO -> JDBC -> SQL.
 */
public class PainelDashboard extends JPanel {

    private static final String PERIODO_TUDO   = "Tudo";
    private static final String PERIODO_HOJE   = "Hoje";
    private static final String PERIODO_7D     = "Ultimos 7 dias";
    private static final String PERIODO_30D    = "Ultimos 30 dias";
    private static final String PERIODO_MES    = "Mes atual";
    private static final String FILIAL_TODAS   = "Todas";
    private static final String PAGTO_TODAS    = "Todas";

    private final RelatoriosDAO dao = new RelatoriosDAO();
    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JLabel lblTotalClientes = new JLabel("Clientes: ...");
    private final JLabel lblTotalProdutos = new JLabel("Produtos: ...");
    private final JLabel lblTotalFuncs    = new JLabel("Funcionarios: ...");
    private final JLabel lblTotalVendas   = new JLabel("Vendas: ...");
    private final JLabel lblPrecoMedio    = new JLabel("Preco medio: ...");

    private final JComboBox<String> cmbPeriodo   = new JComboBox<>(new String[]{
            PERIODO_TUDO, PERIODO_HOJE, PERIODO_7D, PERIODO_30D, PERIODO_MES });
    private final JComboBox<String> cmbFilial    = new JComboBox<>();
    private final JComboBox<String> cmbPagamento = new JComboBox<>(new String[]{
            PAGTO_TODAS, "dinheiro", "cartao", "pix", "boleto" });

    private final JPanel painelGraficos = new JPanel(new GridLayout(2, 3, 8, 8));

    public PainelDashboard() {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // ---- Cabecalho com indicadores (compacto) -----------------------
        JPanel painelIndicadores = new JPanel(new GridLayout(1, 5, 4, 0));
        painelIndicadores.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COR_GRID, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        Font fonteIndicador = Tema.FONTE_NEGRITO.deriveFont(12f);
        for (JLabel l : new JLabel[]{ lblTotalClientes, lblTotalProdutos,
                                      lblTotalFuncs, lblTotalVendas, lblPrecoMedio }) {
            l.setFont(fonteIndicador);
            l.setForeground(Tema.COR_PRIMARIA);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            painelIndicadores.add(l);
        }

        // ---- Barra de filtros (uma linha so) ----------------------------
        JButton btLimpar    = new JButton("Limpar");
        JButton btAtualizar = new JButton("Atualizar");
        btLimpar.setMargin(new Insets(2, 8, 2, 8));
        btAtualizar.setMargin(new Insets(2, 8, 2, 8));

        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        painelFiltros.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        painelFiltros.add(new JLabel("Periodo:"));
        painelFiltros.add(cmbPeriodo);
        painelFiltros.add(new JLabel("Filial:"));
        painelFiltros.add(cmbFilial);
        painelFiltros.add(new JLabel("Pagamento:"));
        painelFiltros.add(cmbPagamento);
        painelFiltros.add(btLimpar);
        painelFiltros.add(btAtualizar);

        JPanel topo = new JPanel(new BorderLayout(0, 2));
        topo.add(painelIndicadores, BorderLayout.NORTH);
        topo.add(painelFiltros,     BorderLayout.SOUTH);

        // Painel central: gaps menores entre graficos
        painelGraficos.setLayout(new GridLayout(2, 3, 4, 4));

        add(topo, BorderLayout.NORTH);
        add(painelGraficos, BorderLayout.CENTER);

        // ---- Acoes ------------------------------------------------------
        cmbPeriodo.addActionListener(e -> atualizar());
        cmbFilial.addActionListener(e -> atualizar());
        cmbPagamento.addActionListener(e -> atualizar());
        btAtualizar.addActionListener(e -> atualizar());
        btLimpar.addActionListener(e -> limparFiltros());

        carregarFiliais();
        atualizar();
    }

    /** Popula o combo de filial com os CNPJs vindos do banco. */
    private void carregarFiliais() {
        cmbFilial.removeAllItems();
        cmbFilial.addItem(FILIAL_TODAS);
        try {
            for (String cnpj : dao.listarCnpjsFiliais()) {
                cmbFilial.addItem(cnpj);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar filiais: " + ex.getMessage());
        }
    }

    /** Reseta os combos para o estado "sem filtro" e atualiza. */
    private void limparFiltros() {
        cmbPeriodo.setSelectedItem(PERIODO_TUDO);
        cmbFilial.setSelectedItem(FILIAL_TODAS);
        cmbPagamento.setSelectedItem(PAGTO_TODAS);
        atualizar();
    }

    /** Recarrega dados e regera os graficos respeitando os filtros. */
    public void atualizar() {
        try {
            String[] datas      = periodoSelecionado();
            String   dataIni    = datas[0];
            String   dataFim    = datas[1];
            String   cnpjFilial = filtroFilial();
            String   pagamento  = filtroPagamento();

            // ---- indicadores ---------------------------------------------
            lblTotalClientes.setText("Clientes: "    + dao.contarRegistros("cliente"));
            lblTotalProdutos.setText("Produtos: "    + dao.contarRegistros("produto"));
            lblTotalFuncs.setText("Funcionarios: "   + dao.contarRegistros("funcionario"));
            lblTotalVendas.setText("Vendas: "        + dao.contarVendasFiltradas(dataIni, dataFim, cnpjFilial, pagamento));
            BigDecimal media = dao.mediaPrecoProdutos();
            lblPrecoMedio.setText("Preco medio: R$ " + media);

            // ---- graficos -----------------------------------------------
            painelGraficos.removeAll();
            painelGraficos.add(criarGraficoBarras(
                    "Vendas por funcionario", "Funcionario", "Qtd",
                    dao.dashboardVendasPorFuncionario(dataIni, dataFim, cnpjFilial, pagamento)));
            painelGraficos.add(criarGraficoPizza(
                    "Vendas por forma de pagamento",
                    dao.dashboardVendasPorPagamento(dataIni, dataFim, cnpjFilial)));
            painelGraficos.add(criarGraficoLinha(
                    "Vendas por dia", "Data", "Qtd",
                    dao.dashboardVendasPorDia(dataIni, dataFim, cnpjFilial, pagamento)));
            painelGraficos.add(criarGraficoBarras(
                    "Top 5 produtos mais vendidos", "Produto", "Qtd",
                    dao.dashboardTopProdutos(dataIni, dataFim, cnpjFilial, pagamento)));
            painelGraficos.add(criarGraficoBarras(
                    "Vendas por filial", "CNPJ filial", "Qtd",
                    dao.dashboardVendasPorFilial(dataIni, dataFim, pagamento)));

            painelGraficos.revalidate();
            painelGraficos.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    /* =================== conversao dos filtros ============================ */

    /** Converte a opcao do combo de periodo em (dataIni, dataFim). Null = sem filtro. */
    private String[] periodoSelecionado() {
        Object sel = cmbPeriodo.getSelectedItem();
        if (sel == null || PERIODO_TUDO.equals(sel)) return new String[]{ null, null };

        LocalDate hoje = LocalDate.now();
        LocalDate ini;
        LocalDate fim = hoje;

        switch (sel.toString()) {
            case PERIODO_HOJE: ini = hoje;                           break;
            case PERIODO_7D:   ini = hoje.minusDays(7);              break;
            case PERIODO_30D:  ini = hoje.minusDays(30);             break;
            case PERIODO_MES:  ini = hoje.withDayOfMonth(1);         break;
            default:           return new String[]{ null, null };
        }
        return new String[]{ ini.format(fmt), fim.format(fmt) };
    }

    private String filtroFilial() {
        Object sel = cmbFilial.getSelectedItem();
        return (sel == null || FILIAL_TODAS.equals(sel)) ? null : sel.toString();
    }

    private String filtroPagamento() {
        Object sel = cmbPagamento.getSelectedItem();
        return (sel == null || PAGTO_TODAS.equals(sel)) ? null : sel.toString();
    }

    /* ============== fabricas de graficos JFreeChart =================== */

    private ChartPanel criarGraficoBarras(String titulo, String eixoX, String eixoY,
                                          Map<String,Integer> dados) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (dados.isEmpty()) {
            dataset.addValue(0, eixoY, "(sem dados)");
        } else {
            for (Map.Entry<String,Integer> e : dados.entrySet()) {
                dataset.addValue(e.getValue(), eixoY, e.getKey());
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(
                titulo, eixoX, eixoY, dataset,
                PlotOrientation.VERTICAL, false, true, false);
        chart.getTitle().setFont(Tema.FONTE_NEGRITO);
        return compactar(new ChartPanel(chart));
    }

    private ChartPanel criarGraficoLinha(String titulo, String eixoX, String eixoY,
                                         Map<String,Integer> dados) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (dados.isEmpty()) {
            dataset.addValue(0, eixoY, "(sem dados)");
        } else {
            for (Map.Entry<String,Integer> e : dados.entrySet()) {
                dataset.addValue(e.getValue(), eixoY, e.getKey());
            }
        }
        JFreeChart chart = ChartFactory.createLineChart(
                titulo, eixoX, eixoY, dataset,
                PlotOrientation.VERTICAL, false, true, false);
        chart.getTitle().setFont(Tema.FONTE_NEGRITO);
        return compactar(new ChartPanel(chart));
    }

    private ChartPanel criarGraficoPizza(String titulo, Map<String,Integer> dados) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (dados.isEmpty()) {
            dataset.setValue("(sem dados)", 1);
        } else {
            for (Map.Entry<String,Integer> e : dados.entrySet()) {
                dataset.setValue(e.getKey(), e.getValue());
            }
        }
        JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);
        chart.getTitle().setFont(Tema.FONTE_NEGRITO);
        return compactar(new ChartPanel(chart));
    }

    /**
     * Reduz o ChartPanel: setPreferredSize pequeno para encolher dentro do GridLayout,
     * setMinimumSize forca o painel a aceitar tamanhos pequenos, e disabilita os botoes
     * de zoom/popup que JFreeChart agrega por padrao (poluem o visual em telas pequenas).
     */
    private ChartPanel compactar(ChartPanel cp) {
        cp.setPreferredSize(new Dimension(320, 200));
        cp.setMinimumSize(new Dimension(220, 160));
        cp.setMaximumDrawWidth(2000);
        cp.setMaximumDrawHeight(2000);
        cp.setMouseWheelEnabled(true);
        cp.setPopupMenu(null);
        return cp;
    }
}
