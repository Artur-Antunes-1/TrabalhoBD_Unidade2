package ui;

import dao.ClienteDAO;
import modelo.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel CRUD da tabela CLIENTE.
 * Demonstra o ciclo completo: listar -> inserir -> editar -> excluir.
 */
public class PainelClientes extends JPanel {

    private final ClienteDAO dao = new ClienteDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"CPF","Nome","Telefone"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelClientes() {
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

        // ---- Acoes --------------------------------------------------
        btInserir.addActionListener(e -> abrirFormulario(null));
        btEditar.addActionListener(e -> {
            Cliente c = clienteSelecionado();
            if (c != null) abrirFormulario(c);
        });
        btExcluir.addActionListener(e -> excluirSelecionado());
        btAtualizar.addActionListener(e -> recarregar());

        recarregar();
    }

    /* ============================================================== */

    private Cliente clienteSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return null;
        }
        return new Cliente(
                (String) modelo.getValueAt(linha, 0),
                (String) modelo.getValueAt(linha, 1),
                (String) modelo.getValueAt(linha, 2));
    }

    private void recarregar() {
        try {
            List<Cliente> lista = dao.listar();
            modelo.setRowCount(0);
            for (Cliente c : lista) {
                modelo.addRow(new Object[]{c.getCpf(), c.getNome(), c.getTelefone()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar: " + ex.getMessage());
        }
    }

    private void excluirSelecionado() {
        Cliente c = clienteSelecionado();
        if (c == null) return;

        int op = JOptionPane.showConfirmDialog(this,
                "Excluir cliente " + c.getNome() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            dao.excluir(c.getCpf());
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    /**
     * Abre o formulario.  Se cliente == null, e insercao.
     * Senao, e edicao (CPF e travado).
     */
    private void abrirFormulario(Cliente clienteParaEditar) {
        boolean edicao = clienteParaEditar != null;

        JTextField campoCpf      = new JTextField(edicao ? clienteParaEditar.getCpf() : "");
        JTextField campoNome     = new JTextField(edicao ? clienteParaEditar.getNome() : "");
        JTextField campoTelefone = new JTextField(edicao ? clienteParaEditar.getTelefone() : "");

        if (edicao) campoCpf.setEditable(false);

        Object[] campos = {
                "CPF (11 digitos):",   campoCpf,
                "Nome:",               campoNome,
                "Telefone:",           campoTelefone
        };

        int op = JOptionPane.showConfirmDialog(this, campos,
                edicao ? "Editar Cliente" : "Inserir Cliente",
                JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;

        Cliente c = new Cliente(
                campoCpf.getText().trim(),
                campoNome.getText().trim(),
                campoTelefone.getText().trim());

        try {
            if (edicao) dao.atualizar(c);
            else        dao.inserir(c);
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
