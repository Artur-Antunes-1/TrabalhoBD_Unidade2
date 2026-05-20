package ui;

import dao.FuncionarioDAO;
import modelo.Funcionario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel CRUD da tabela FUNCIONARIO.
 */
public class PainelFuncionarios extends JPanel {

    private final FuncionarioDAO dao = new FuncionarioDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Matricula","CPF","Nome","Tel1","Tel2","Tipo","CNPJ Filial","Supervisor"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelFuncionarios() {
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
        tabela.setAutoCreateRowSorter(true);

        btInserir.addActionListener(e -> abrirFormulario(null));
        btEditar.addActionListener(e -> {
            Funcionario f = funcionarioSelecionado();
            if (f != null) abrirFormulario(f);
        });
        btExcluir.addActionListener(e -> excluirSelecionado());
        btAtualizar.addActionListener(e -> recarregar());

        recarregar();
    }

    private Funcionario funcionarioSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return null;
        }
        linha = tabela.convertRowIndexToModel(linha);
        return new Funcionario(
                (String) modelo.getValueAt(linha, 0),
                (String) modelo.getValueAt(linha, 1),
                (String) modelo.getValueAt(linha, 2),
                (String) modelo.getValueAt(linha, 3),
                (String) modelo.getValueAt(linha, 4),
                (String) modelo.getValueAt(linha, 5),
                (String) modelo.getValueAt(linha, 6),
                (String) modelo.getValueAt(linha, 7));
    }

    private void recarregar() {
        try {
            List<Funcionario> lista = dao.listar();
            modelo.setRowCount(0);
            for (Funcionario f : lista) {
                modelo.addRow(new Object[]{
                        f.getMatricula(), f.getCpf(), f.getNome(),
                        f.getTelefone1(), f.getTelefone2(),
                        f.getTipo(), f.getCnpjFilial(), f.getSupervisor()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar: " + ex.getMessage());
        }
    }

    private void excluirSelecionado() {
        Funcionario f = funcionarioSelecionado();
        if (f == null) return;

        int op = JOptionPane.showConfirmDialog(this,
                "Excluir funcionario " + f.getNome() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            dao.excluir(f.getMatricula());
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage());
        }
    }

    private void abrirFormulario(Funcionario funcionarioParaEditar) {
        boolean edicao = funcionarioParaEditar != null;

        JTextField campoMatricula  = new JTextField(edicao ? funcionarioParaEditar.getMatricula() : "");
        JTextField campoCpf        = new JTextField(edicao ? funcionarioParaEditar.getCpf() : "");
        JTextField campoNome       = new JTextField(edicao ? funcionarioParaEditar.getNome() : "");
        JTextField campoTel1       = new JTextField(edicao ? funcionarioParaEditar.getTelefone1() : "");
        JTextField campoTel2       = new JTextField(edicao ? funcionarioParaEditar.getTelefone2() : "");
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"operacional","administrativo"});
        if (edicao) comboTipo.setSelectedItem(funcionarioParaEditar.getTipo());
        JTextField campoCnpjFilial = new JTextField(edicao ? funcionarioParaEditar.getCnpjFilial() : "");
        JTextField campoSupervisor = new JTextField(edicao ? funcionarioParaEditar.getSupervisor() : "");

        if (edicao) campoMatricula.setEditable(false);

        Object[] campos = {
                "Matricula:",     campoMatricula,
                "CPF:",           campoCpf,
                "Nome:",          campoNome,
                "Telefone 1:",    campoTel1,
                "Telefone 2:",    campoTel2,
                "Tipo:",          comboTipo,
                "CNPJ Filial:",   campoCnpjFilial,
                "Supervisor:",    campoSupervisor
        };

        int op = JOptionPane.showConfirmDialog(this, campos,
                edicao ? "Editar Funcionario" : "Inserir Funcionario",
                JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;

        String supervisor = campoSupervisor.getText().trim();
        if (supervisor.isEmpty()) supervisor = null;

        String telefone2 = campoTel2.getText().trim();
        if (telefone2.isEmpty()) telefone2 = null;

        Funcionario f = new Funcionario(
                campoMatricula.getText().trim(),
                campoCpf.getText().trim(),
                campoNome.getText().trim(),
                campoTel1.getText().trim(),
                telefone2,
                (String) comboTipo.getSelectedItem(),
                campoCnpjFilial.getText().trim(),
                supervisor);

        try {
            if (edicao) dao.atualizar(f);
            else        dao.inserir(f);
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
