package ui;

import dao.ProdutoDAO;
import modelo.Produto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Painel CRUD da tabela PRODUTO.
 *
 *  Detalhe importante: editar o preco aqui dispara o trigger
 *  tg_log_alteracao_preco (visivel na aba "Logs").
 */
public class PainelProdutos extends JPanel {

    private final ProdutoDAO dao = new ProdutoDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Codigo","Nome","Preco Base"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelProdutos() {
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JButton btInserir   = new JButton("Inserir");
        JButton btEditar    = new JButton("Editar");
        JButton btExcluir   = new JButton("Excluir");
        JButton btAtualizar = new JButton("Atualizar lista");

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBotoes.add(btInserir);
        painelBotoes.add(btEditar);
        painelBotoes.add(btExcluir);
        painelBotoes.add(btAtualizar);

        add(painelBotoes, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        btInserir.addActionListener(e -> abrirFormulario(null));
        btEditar.addActionListener(e -> {
            Produto p = produtoSelecionado();
            if (p != null) abrirFormulario(p);
        });
        btExcluir.addActionListener(e -> excluirSelecionado());
        btAtualizar.addActionListener(e -> recarregar());

        recarregar();
    }

    private Produto produtoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return null;
        }
        return new Produto(
                (String) modelo.getValueAt(linha, 0),
                (String) modelo.getValueAt(linha, 1),
                (BigDecimal) modelo.getValueAt(linha, 2));
    }

    public void recarregar() {
        try {
            List<Produto> lista = dao.listar();
            modelo.setRowCount(0);
            for (Produto p : lista) {
                modelo.addRow(new Object[]{
                        p.getCodigo(), p.getNome(), p.getPrecoBase()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar: " + ex.getMessage());
        }
    }

    private void excluirSelecionado() {
        Produto p = produtoSelecionado();
        if (p == null) return;

        int op = JOptionPane.showConfirmDialog(this,
                "Excluir produto " + p.getNome() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            dao.excluir(p.getCodigo());
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    private void abrirFormulario(Produto produtoParaEditar) {
        boolean edicao = produtoParaEditar != null;

        JTextField campoCodigo = new JTextField(edicao ? produtoParaEditar.getCodigo() : "");
        JTextField campoNome   = new JTextField(edicao ? produtoParaEditar.getNome() : "");
        JTextField campoPreco  = new JTextField(edicao ? produtoParaEditar.getPrecoBase().toString() : "");

        if (edicao) campoCodigo.setEditable(false);

        Object[] campos = {
                "Codigo:",     campoCodigo,
                "Nome:",       campoNome,
                "Preco Base:", campoPreco
        };

        int op = JOptionPane.showConfirmDialog(this, campos,
                edicao ? "Editar Produto" : "Inserir Produto",
                JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;

        try {
            Produto p = new Produto(
                    campoCodigo.getText().trim(),
                    campoNome.getText().trim(),
                    new BigDecimal(campoPreco.getText().trim()));

            if (edicao) dao.atualizar(p);
            else        dao.inserir(p);
            recarregar();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preco invalido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
