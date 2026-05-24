package ui;

import dao.RelatoriosDAO;
import modelo.LogAlteracao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel "Logs" - mostra a tabela log_alteracoes alimentada
 * pelos dois triggers (Etapa 05).
 */
public class PainelLogs extends JPanel {

    private final RelatoriosDAO dao = new RelatoriosDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID","Tabela","Evento","Descricao","Data/Hora"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public PainelLogs() {
        setLayout(new BorderLayout(5,5));
        setBorder(Tema.bordaPainel());

        JButton btAtualizar = new JButton("Atualizar logs");
        JPanel  topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Logs gerados pelos triggers tg_log_nova_venda e tg_log_alteracao_preco:"));
        topo.add(btAtualizar);

        JTable tabela = new JTable(modelo);
        Tema.estilizarTabela(tabela);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        btAtualizar.addActionListener(e -> recarregar());
        recarregar();
    }

    public void recarregar() {
        try {
            List<LogAlteracao> lista = dao.listarLogs();
            modelo.setRowCount(0);
            for (LogAlteracao l : lista) {
                modelo.addRow(new Object[]{
                        l.getId(), l.getTabela(), l.getTipoEvento(),
                        l.getDescricao(), l.getDataHora()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }
}
