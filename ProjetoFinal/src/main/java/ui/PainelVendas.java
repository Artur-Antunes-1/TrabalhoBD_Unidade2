package ui;

import dao.VendaDAO;
import modelo.ItemVenda;
import modelo.Venda;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Painel CRUD da tabela VENDE.
 *
 *  Ao inserir uma venda dispara o trigger tg_log_nova_venda
 *  (ver aba Logs).
 *  Apos inserir a venda, o usuario pode adicionar itens (linhas
 *  em vende_produto) tambem pela tela.
 */
public class PainelVendas extends JPanel {

    private final VendaDAO dao = new VendaDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"NF-e","Data","Pagamento","Matricula Func","CPF Cliente"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelVendas() {
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JButton btInserir   = new JButton("Inserir Venda");
        JButton btEditar    = new JButton("Editar");
        JButton btExcluir   = new JButton("Excluir");
        JButton btItens     = new JButton("Ver/Adicionar Itens");
        JButton btAtualizar = new JButton("Atualizar lista");

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBotoes.add(btInserir);
        painelBotoes.add(btEditar);
        painelBotoes.add(btExcluir);
        painelBotoes.add(btItens);
        painelBotoes.add(btAtualizar);

        add(painelBotoes, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        btInserir.addActionListener(e -> abrirFormulario(null));
        btEditar.addActionListener(e -> {
            Venda v = vendaSelecionada();
            if (v != null) abrirFormulario(v);
        });
        btExcluir.addActionListener(e -> excluirSelecionada());
        btItens.addActionListener(e -> abrirItens());
        btAtualizar.addActionListener(e -> recarregar());

        recarregar();
    }

    private Venda vendaSelecionada() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return null;
        }
        Object dataObj = modelo.getValueAt(linha, 1);
        LocalDate data = (dataObj instanceof LocalDate)
                ? (LocalDate) dataObj
                : LocalDate.parse(dataObj.toString());

        return new Venda(
                (String) modelo.getValueAt(linha, 0),
                data,
                (String) modelo.getValueAt(linha, 2),
                (String) modelo.getValueAt(linha, 3),
                (String) modelo.getValueAt(linha, 4));
    }

    private void recarregar() {
        try {
            List<Venda> lista = dao.listar();
            modelo.setRowCount(0);
            for (Venda v : lista) {
                modelo.addRow(new Object[]{
                        v.getNfe(), v.getDataVenda(), v.getPagamento(),
                        v.getMatriculaFunc(), v.getCpfCliente()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar: " + ex.getMessage());
        }
    }

    private void excluirSelecionada() {
        Venda v = vendaSelecionada();
        if (v == null) return;

        int op = JOptionPane.showConfirmDialog(this,
                "Excluir venda " + v.getNfe() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op != JOptionPane.YES_OPTION) return;

        try {
            dao.excluir(v.getNfe());
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void abrirFormulario(Venda vendaParaEditar) {
        boolean edicao = vendaParaEditar != null;

        JTextField campoNfe    = new JTextField(edicao ? vendaParaEditar.getNfe() : "");
        JTextField campoData   = new JTextField(edicao
                ? vendaParaEditar.getDataVenda().toString()
                : LocalDate.now().toString());
        JComboBox<String> comboPagamento = new JComboBox<>(new String[]{"dinheiro","cartao","pix","boleto"});
        if (edicao) comboPagamento.setSelectedItem(vendaParaEditar.getPagamento());
        JTextField campoMatricula = new JTextField(edicao ? vendaParaEditar.getMatriculaFunc() : "");
        JTextField campoCpf       = new JTextField(edicao ? vendaParaEditar.getCpfCliente() : "");

        if (edicao) campoNfe.setEditable(false);

        Object[] campos = {
                "NF-e:",                  campoNfe,
                "Data (yyyy-MM-dd):",     campoData,
                "Pagamento:",             comboPagamento,
                "Matricula do funcionario:", campoMatricula,
                "CPF do cliente (opcional):", campoCpf
        };

        int op = JOptionPane.showConfirmDialog(this, campos,
                edicao ? "Editar Venda" : "Inserir Venda",
                JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;

        String cpf = campoCpf.getText().trim();
        if (cpf.isEmpty()) cpf = null;

        try {
            Venda v = new Venda(
                    campoNfe.getText().trim(),
                    LocalDate.parse(campoData.getText().trim()),
                    (String) comboPagamento.getSelectedItem(),
                    campoMatricula.getText().trim(),
                    cpf);

            if (edicao) dao.atualizar(v);
            else        dao.inserir(v);
            recarregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    /** Mostra os itens da venda selecionada e permite adicionar mais. */
    private void abrirItens() {
        Venda v = vendaSelecionada();
        if (v == null) return;

        try {
            List<ItemVenda> itens = dao.listarItensDaVenda(v.getNfe());

            DefaultTableModel mod = new DefaultTableModel(
                    new String[]{"NF-e","Cod. Produto","Quantidade"}, 0);
            for (ItemVenda it : itens) {
                mod.addRow(new Object[]{
                        it.getNfe(), it.getCodProduto(), it.getQuantidade()});
            }
            JTable tab = new JTable(mod);

            JTextField campoCodProd = new JTextField(8);
            JTextField campoQtd     = new JTextField(4);
            JButton    btAdd        = new JButton("Adicionar item");

            JPanel form = new JPanel(new FlowLayout());
            form.add(new JLabel("Cod. Produto:"));
            form.add(campoCodProd);
            form.add(new JLabel("Qtd:"));
            form.add(campoQtd);
            form.add(btAdd);

            JPanel painel = new JPanel(new BorderLayout());
            painel.add(new JScrollPane(tab), BorderLayout.CENTER);
            painel.add(form, BorderLayout.SOUTH);
            painel.setPreferredSize(new Dimension(450, 250));

            btAdd.addActionListener(e -> {
                try {
                    ItemVenda novo = new ItemVenda(
                            v.getNfe(),
                            campoCodProd.getText().trim(),
                            Integer.parseInt(campoQtd.getText().trim()));
                    dao.inserirItem(novo);
                    mod.addRow(new Object[]{novo.getNfe(), novo.getCodProduto(), novo.getQuantidade()});
                    campoCodProd.setText("");
                    campoQtd.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(painel, "Erro: " + ex.getMessage());
                }
            });

            JOptionPane.showMessageDialog(this, painel,
                    "Itens da venda " + v.getNfe(),
                    JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
