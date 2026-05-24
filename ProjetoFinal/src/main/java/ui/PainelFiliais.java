package ui;

import dao.FilialDAO;
import modelo.Filial;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel CRUD da tabela FILIAL.
 */
public class PainelFiliais extends JPanel {

    private final FilialDAO dao = new FilialDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"CNPJ","Rua","Numero","CEP"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelFiliais() {
        setLayout(new BorderLayout(5,5));
        setBorder(Tema.bordaPainel());
        Tema.estilizarTabela(tabela);

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
            Filial f = filialSelecionada();
            if (f != null) abrirFormulario(f);
        });
        btExcluir.addActionListener(e -> excluirSelecionada());
        btAtualizar.addActionListener(e -> recarregar());

        recarregar();
    }

    private Filial filialSelecionada() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return null;
        }
        return new Filial(
                (String) modelo.getValueAt(linha, 0),
                (String) modelo.getValueAt(linha, 1),
                (String) modelo.getValueAt(linha, 2),
                (String) modelo.getValueAt(linha, 3));
    }

    private void recarregar() {
        try {
            List<Filial> lista = dao.listar();
            modelo.setRowCount(0);
            for (Filial f : lista) {
                modelo.addRow(new Object[]{
                        f.getCnpj(), f.getRua(), f.getNumero(), f.getCep()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar: " + ex.getMessage());
        }
    }

    private void excluirSelecionada() {
        Filial f = filialSelecionada();
        if (f == null) return;

        int op = JOptionPane.showConfirmDialog(this,
                "Excluir filial " + f.getCnpj() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            dao.excluir(f.getCnpj());
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    private void abrirFormulario(Filial filialParaEditar) {
        boolean edicao = filialParaEditar != null;

        JTextField campoCnpj   = new JTextField(edicao ? filialParaEditar.getCnpj() : "");
        JTextField campoRua    = new JTextField(edicao ? filialParaEditar.getRua() : "");
        JTextField campoNumero = new JTextField(edicao ? filialParaEditar.getNumero() : "");
        JTextField campoCep    = new JTextField(edicao ? filialParaEditar.getCep() : "");

        if (edicao) campoCnpj.setEditable(false);

        Object[] campos = {
                "CNPJ (14 digitos):", campoCnpj,
                "Rua:",               campoRua,
                "Numero:",            campoNumero,
                "CEP (8 digitos):",   campoCep
        };

        int op = JOptionPane.showConfirmDialog(this, campos,
                edicao ? "Editar Filial" : "Inserir Filial",
                JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;

        Filial f = new Filial(
                campoCnpj.getText().trim(),
                campoRua.getText().trim(),
                campoNumero.getText().trim(),
                campoCep.getText().trim());

        try {
            if (edicao) dao.atualizar(f);
            else        dao.inserir(f);
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
