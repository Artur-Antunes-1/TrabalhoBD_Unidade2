package ui;

import dao.RelatoriosDAO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Painel "Funcoes e Procedimentos" - executa as funcoes e procedimentos
 * da Etapa 05 e exibe os resultados.
 */
public class PainelFuncoesProcedimentos extends JPanel {

    private final RelatoriosDAO dao = new RelatoriosDAO();
    private final JTextArea areaSaida = new JTextArea();
    private final JTable tabelaProdutos = new JTable();

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

        // ---- fn_porte_venda(nfe)  [usa IF/ELSEIF/ELSE; chama fn_total_venda]
        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campoNfeP = new JTextField("NFE0000000003", 14);
        JButton btPorte = new JButton("fn_porte_venda(nfe) ->");
        linha2.add(new JLabel("NF-e:"));
        linha2.add(campoNfeP);
        linha2.add(btPorte);
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

        // ---- pr_promocao_produtos_parados  [CURSOR]
        JPanel linha4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campoPercSem   = new JTextField("20", 5);
        JTextField campoPercPouca = new JTextField("10", 5);
        JButton    btPromocao     = new JButton("pr_promocao_produtos_parados(%sem, %pouca)  [CURSOR]");
        linha4.add(new JLabel("% se 0 vendas:"));
        linha4.add(campoPercSem);
        linha4.add(new JLabel("% se 1-2 vendas:"));
        linha4.add(campoPercPouca);
        linha4.add(btPromocao);
        painelProcs.add(linha4);

        // ---- topo
        JPanel topo = new JPanel(new BorderLayout(4,4));
        topo.add(painelFuncoes, BorderLayout.NORTH);
        topo.add(painelProcs,   BorderLayout.SOUTH);

        // ============ SAIDA ==============================================
        areaSaida.setEditable(false);
        areaSaida.setRows(4);
        areaSaida.setBorder(BorderFactory.createTitledBorder("Saida das funcoes"));

        tabelaProdutos.setAutoCreateRowSorter(true);

        JScrollPane scrollTabela = new JScrollPane(tabelaProdutos);
        scrollTabela.setBorder(BorderFactory.createTitledBorder(
                "Produtos: total vendido e preco atual (clique no cabecalho pra ordenar)"));

        JPanel centro = new JPanel(new BorderLayout(4,4));
        centro.add(new JScrollPane(areaSaida), BorderLayout.NORTH);
        centro.add(scrollTabela, BorderLayout.CENTER);

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

        btPorte.addActionListener(e -> {
            try {
                String porte = dao.chamarFuncaoPorteVenda(campoNfeP.getText().trim());
                anexarSaida("fn_porte_venda(" + campoNfeP.getText().trim() + ") = " + porte);
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
                carregarProdutos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        btPromocao.addActionListener(e -> {
            try {
                BigDecimal percSem   = new BigDecimal(campoPercSem.getText().trim());
                BigDecimal percPouca = new BigDecimal(campoPercPouca.getText().trim());
                dao.chamarProcedimentoPromocao(percSem, percPouca);
                anexarSaida("Procedimento pr_promocao_produtos_parados executado "
                        + "(0 vendas -> -" + percSem + "%, 1-2 vendas -> -" + percPouca + "%).");
                carregarProdutos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        carregarProdutos();
    }

    private void anexarSaida(String texto) {
        areaSaida.append(texto + "\n");
        areaSaida.setCaretPosition(areaSaida.getDocument().getLength());
    }

    private void carregarProdutos() {
        try {
            tabelaProdutos.setModel(TabelaUtil.construirModelo(dao.listarProdutosComVendas()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
