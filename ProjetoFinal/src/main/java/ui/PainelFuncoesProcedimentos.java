package ui;

import dao.RelatoriosDAO;
import modelo.ResumoFuncionario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Painel "Funcoes e Procedimentos" - executa as funcoes e procedimentos
 * da Etapa 05 e exibe os resultados.
 */
public class PainelFuncoesProcedimentos extends JPanel {

    private final RelatoriosDAO dao = new RelatoriosDAO();
    private final JTextArea areaSaida = new JTextArea();
    private final DefaultTableModel modeloResumo = new DefaultTableModel(
            new String[]{"Matricula","Nome","Total Vendas","Valor Total","Classificacao","Gerado em"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public PainelFuncoesProcedimentos() {
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // ============ FUNCOES ============================================
        JPanel painelFuncoes = new JPanel(new GridLayout(0, 1, 4, 4));
        painelFuncoes.setBorder(BorderFactory.createTitledBorder("Funcoes (Etapa 05)"));

        // ---- fn_total_venda(nfe)
        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campoNfe = new JTextField("NFE0000000001", 14);
        JButton btTotal = new JButton("fn_total_venda(nfe) ->");
        linha1.add(new JLabel("NF-e:"));
        linha1.add(campoNfe);
        linha1.add(btTotal);
        painelFuncoes.add(linha1);

        // ---- fn_categoria_funcionario(matricula)  [usa IF/ELSE]
        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campoMat = new JTextField("FUNC004", 10);
        JButton btCat = new JButton("fn_categoria_funcionario(matricula) ->");
        linha2.add(new JLabel("Matricula:"));
        linha2.add(campoMat);
        linha2.add(btCat);
        painelFuncoes.add(linha2);

        // ============ PROCEDIMENTOS ======================================
        JPanel painelProcs = new JPanel(new GridLayout(0, 1, 4, 4));
        painelProcs.setBorder(BorderFactory.createTitledBorder("Procedimentos (Etapa 05)"));

        // ---- pr_atualizar_preco_departamento  [UPDATE]
        JPanel linha3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campoDept = new JTextField("DEPT05", 8);
        JTextField campoPerc = new JTextField("10", 5);
        JButton    btUpdate  = new JButton("pr_atualizar_preco_departamento(dept, %)");
        linha3.add(new JLabel("Cod. Dept:"));
        linha3.add(campoDept);
        linha3.add(new JLabel("Percentual (%):"));
        linha3.add(campoPerc);
        linha3.add(btUpdate);
        painelProcs.add(linha3);

        // ---- pr_gerar_resumo_funcionarios()  [CURSOR]
        JPanel linha4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btResumo = new JButton("pr_gerar_resumo_funcionarios()  [CURSOR]");
        linha4.add(btResumo);
        painelProcs.add(linha4);

        // ---- topo
        JPanel topo = new JPanel(new BorderLayout(4,4));
        topo.add(painelFuncoes, BorderLayout.NORTH);
        topo.add(painelProcs,   BorderLayout.SOUTH);

        // ============ SAIDA ==============================================
        areaSaida.setEditable(false);
        areaSaida.setRows(4);
        areaSaida.setBorder(BorderFactory.createTitledBorder("Saida das funcoes"));

        JTable tabelaResumo = new JTable(modeloResumo);
        tabelaResumo.setBorder(BorderFactory.createTitledBorder("Resumo gerado pelo CURSOR"));

        JPanel centro = new JPanel(new BorderLayout(4,4));
        centro.add(new JScrollPane(areaSaida), BorderLayout.NORTH);
        centro.add(new JScrollPane(tabelaResumo), BorderLayout.CENTER);

        add(topo,   BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        // ============ ACOES ==============================================
        btTotal.addActionListener(e -> {
            try {
                BigDecimal total = dao.chamarFuncaoTotalVenda(campoNfe.getText().trim());
                anexarSaida("fn_total_venda(" + campoNfe.getText().trim() + ") = R$ " + total);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        btCat.addActionListener(e -> {
            try {
                String cat = dao.chamarFuncaoCategoriaFuncionario(campoMat.getText().trim());
                anexarSaida("fn_categoria_funcionario(" + campoMat.getText().trim() + ") = " + cat);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        btUpdate.addActionListener(e -> {
            try {
                BigDecimal perc = new BigDecimal(campoPerc.getText().trim());
                dao.chamarProcedimentoAtualizarPreco(campoDept.getText().trim(), perc);
                anexarSaida("Procedimento pr_atualizar_preco_departamento executado em "
                        + campoDept.getText().trim() + " com " + perc + "%.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        btResumo.addActionListener(e -> {
            try {
                dao.chamarProcedimentoGerarResumo();
                carregarResumo();
                anexarSaida("Procedimento pr_gerar_resumo_funcionarios executado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        // Carrega o resumo se ja existir
        carregarResumo();
    }

    private void anexarSaida(String texto) {
        areaSaida.append(texto + "\n");
        areaSaida.setCaretPosition(areaSaida.getDocument().getLength());
    }

    private void carregarResumo() {
        try {
            List<ResumoFuncionario> lista = dao.listarResumoFuncionarios();
            modeloResumo.setRowCount(0);
            for (ResumoFuncionario r : lista) {
                modeloResumo.addRow(new Object[]{
                        r.getMatricula(), r.getNome(), r.getTotalVendas(),
                        r.getValorTotal(), r.getClassificacao(), r.getDataGeracao()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
