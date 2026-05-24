package ui;

import dao.RelatoriosDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Painel "Consultas e Views" - executa as 4 consultas e 2 views da
 * Etapa 04 e exibe o resultado em tabelas.
 *
 *  A consulta 1.2 ("vendas no periodo") tem dois campos de filtro
 *  (data inicial e data final) demonstrando uso de parametros.
 */
public class PainelConsultas extends JPanel {

    private final RelatoriosDAO dao = new RelatoriosDAO();
    private final JTable tabelaResultado = new JTable();

    private final JTextField campoIni = new JTextField("2025-10-01", 10);
    private final JTextField campoFim = new JTextField("2025-10-31", 10);

    public PainelConsultas() {
        setLayout(new BorderLayout(5,5));
        setBorder(Tema.bordaPainel());
        Tema.estilizarTabela(tabelaResultado);

        // GridLayout 2x3: altura previsivel; FlowLayout dentro de BorderLayout.NORTH
        // cortava a segunda linha de botoes.
        JPanel painelBotoes = new JPanel(new GridLayout(2, 3, 4, 4));

        JButton bt1 = new JButton("1.1  Func. com >1 venda (JOIN+GROUP+HAVING)");
        JButton bt2 = new JButton("1.2  Vendas do periodo (2 JOINs+WHERE)");
        JButton bt3 = new JButton("1.3  Clientes sem compra (ANTI JOIN)");
        JButton bt4 = new JButton("1.4  Produtos > preco medio (SUBQUERY)");
        JButton btV1 = new JButton("View vw_vendas_detalhadas");
        JButton btV2 = new JButton("View vw_funcionarios_destaque");

        painelBotoes.add(bt1);
        painelBotoes.add(bt2);
        painelBotoes.add(bt3);
        painelBotoes.add(bt4);
        painelBotoes.add(btV1);
        painelBotoes.add(btV2);

        // Filtro de periodo (so usado pela consulta 1.2)
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Periodo (yyyy-MM-dd):"));
        filtros.add(campoIni);
        filtros.add(new JLabel("ate"));
        filtros.add(campoFim);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(painelBotoes, BorderLayout.NORTH);
        topo.add(filtros,      BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabelaResultado), BorderLayout.CENTER);

        // ---- Acoes (cada botao chama um metodo do RelatoriosDAO) ----
        bt1.addActionListener(e -> executar(() -> dao.consultaFuncionariosComMaisDeUmaVenda()));
        bt2.addActionListener(e -> executar(() -> dao.consultaVendasNoPeriodo(
                campoIni.getText().trim(), campoFim.getText().trim())));
        bt3.addActionListener(e -> executar(() -> dao.consultaClientesSemCompra()));
        bt4.addActionListener(e -> executar(() -> dao.consultaProdutosAcimaDaMedia()));
        btV1.addActionListener(e -> executar(() -> dao.viewVendasDetalhadas()));
        btV2.addActionListener(e -> executar(() -> dao.viewFuncionariosDestaque()));
    }

    /** Interface funcional usada para passar metodos do DAO ao painel. */
    @FunctionalInterface
    private interface ConsultaSql {
        List<Map<String,Object>> executar() throws Exception;
    }

    private void executar(ConsultaSql consulta) {
        try {
            tabelaResultado.setModel(TabelaUtil.construirModelo(consulta.executar()));
            Tema.estilizarTabela(tabelaResultado);  // setModel reseta o header
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
