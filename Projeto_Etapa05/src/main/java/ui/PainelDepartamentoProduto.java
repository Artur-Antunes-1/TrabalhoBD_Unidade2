package ui;

import dao.DepartamentoProdutoDAO;
import modelo.DepartamentoProduto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel da tabela DEPARTAMENTO_PRODUTO (associacao N:N entre
 * departamento e produto).
 *
 *  Como a PK e composta pelas duas colunas, oferecemos apenas
 *  Inserir / Excluir / Atualizar lista (UPDATE nao se aplica a
 *  uma linha cuja "informacao" e a propria chave).
 */
public class PainelDepartamentoProduto extends JPanel {

    private final DepartamentoProdutoDAO dao = new DepartamentoProdutoDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Cod. Departamento","Cod. Produto"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelDepartamentoProduto() {
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // ---- Botoes -------------------------------------------------
        JButton btInserir   = new JButton("Inserir");
        JButton btExcluir   = new JButton("Excluir");
        JButton btAtualizar = new JButton("Atualizar lista");

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBotoes.add(btInserir);
        painelBotoes.add(btExcluir);
        painelBotoes.add(btAtualizar);

        add(painelBotoes, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        tabela.setAutoCreateRowSorter(true);

        // ---- Acoes --------------------------------------------------
        btInserir.addActionListener(e -> abrirFormulario());
        btExcluir.addActionListener(e -> excluirSelecionado());
        btAtualizar.addActionListener(e -> recarregar());

        recarregar();
    }

    /* ============================================================== */

    private DepartamentoProduto associacaoSelecionada() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return null;
        }
        linha = tabela.convertRowIndexToModel(linha);
        return new DepartamentoProduto(
                (String) modelo.getValueAt(linha, 0),
                (String) modelo.getValueAt(linha, 1));
    }

    private void recarregar() {
        try {
            List<DepartamentoProduto> lista = dao.listar();
            modelo.setRowCount(0);
            for (DepartamentoProduto dp : lista) {
                modelo.addRow(new Object[]{dp.getCodDepartamento(), dp.getCodProduto()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar: " + ex.getMessage());
        }
    }

    private void excluirSelecionado() {
        DepartamentoProduto dp = associacaoSelecionada();
        if (dp == null) return;

        int op = JOptionPane.showConfirmDialog(this,
                "Excluir associacao " + dp.getCodDepartamento() + " - " + dp.getCodProduto() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            dao.excluir(dp.getCodDepartamento(), dp.getCodProduto());
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    private void abrirFormulario() {
        JTextField campoDept = new JTextField();
        JTextField campoProd = new JTextField();

        Object[] campos = {
                "Cod. Departamento (ex: DEPT05):", campoDept,
                "Cod. Produto (ex: PROD11):",      campoProd
        };

        int op = JOptionPane.showConfirmDialog(this, campos,
                "Inserir associacao Departamento-Produto",
                JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;

        DepartamentoProduto dp = new DepartamentoProduto(
                campoDept.getText().trim(),
                campoProd.getText().trim());

        try {
            dao.inserir(dp);
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
