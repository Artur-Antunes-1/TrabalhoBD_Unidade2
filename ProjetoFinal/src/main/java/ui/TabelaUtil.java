package ui;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Map;

/**
 * Funcoes auxiliares para popular JTables a partir de
 * List<Map<String,Object>> retornado pela RelatoriosDAO.
 */
public class TabelaUtil {

    private TabelaUtil() {}

    /** Cria um DefaultTableModel a partir de uma lista de Map. */
    public static DefaultTableModel construirModelo(List<Map<String,Object>> linhas) {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        if (linhas.isEmpty()) {
            modelo.addColumn("(sem resultados)");
            return modelo;
        }

        // Colunas a partir da primeira linha
        Map<String,Object> primeira = linhas.get(0);
        for (String coluna : primeira.keySet()) {
            modelo.addColumn(coluna);
        }

        // Linhas
        for (Map<String,Object> linha : linhas) {
            Object[] valores = new Object[primeira.size()];
            int i = 0;
            for (String coluna : primeira.keySet()) {
                valores[i++] = linha.get(coluna);
            }
            modelo.addRow(valores);
        }

        return modelo;
    }
}
