package ui;

import dao.DepartamentoDAO;
import modelo.Departamento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel CRUD da tabela DEPARTAMENTO.
 * Demonstra o ciclo completo: listar -> inserir -> editar -> excluir.
 */
public class PainelDepartamentos extends JPanel {

    private final DepartamentoDAO dao = new DepartamentoDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Codigo","Categoria"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelDepartamentos() {
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // ---- Botoes -------------------------------------------------
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
        tabela.setAutoCreateRowSorter(true);

        // ---- Acoes --------------------------------------------------
        btInserir.addActionListener(e -> abrirFormulario(null));
        btEditar.addActionListener(e -> {
            Departamento d = departamentoSelecionado();
            if (d != null) abrirFormulario(d);
        });
        btExcluir.addActionListener(e -> excluirSelecionado());
        btAtualizar.addActionListener(e -> recarregar());

        recarregar();
    }

    /* ============================================================== */

    private Departamento departamentoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return null;
        }
        linha = tabela.convertRowIndexToModel(linha);
        return new Departamento(
                (String) modelo.getValueAt(linha, 0),
                (String) modelo.getValueAt(linha, 1));
    }

    private void recarregar() {
        try {
            List<Departamento> lista = dao.listar();
            modelo.setRowCount(0);
            for (Departamento d : lista) {
                modelo.addRow(new Object[]{d.getCodigo(), d.getCategoria()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar: " + ex.getMessage());
        }
    }

    private void excluirSelecionado() {
        Departamento d = departamentoSelecionado();
        if (d == null) return;

        int op = JOptionPane.showConfirmDialog(this,
                "Excluir departamento " + d.getCategoria() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            dao.excluir(d.getCodigo());
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    /**
     * Abre o formulario.  Se departamento == null, e insercao.
     * Senao, e edicao (codigo e travado).
     */
    private void abrirFormulario(Departamento depParaEditar) {
        boolean edicao = depParaEditar != null;

        JTextField campoCodigo    = new JTextField(edicao ? depParaEditar.getCodigo() : "");
        JTextField campoCategoria = new JTextField(edicao ? depParaEditar.getCategoria() : "");

        if (edicao) campoCodigo.setEditable(false);

        Object[] campos = {
                "Codigo (ex: DEPT05):", campoCodigo,
                "Categoria:",           campoCategoria
        };

        int op = JOptionPane.showConfirmDialog(this, campos,
                edicao ? "Editar Departamento" : "Inserir Departamento",
                JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;

        Departamento d = new Departamento(
                campoCodigo.getText().trim(),
                campoCategoria.getText().trim());

        try {
            if (edicao) dao.atualizar(d);
            else        dao.inserir(d);
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
