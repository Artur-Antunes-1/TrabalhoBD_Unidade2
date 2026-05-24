package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Configuracao visual centralizada da aplicacao.
 *
 *  - L&F Nimbus (built-in no JDK, sem dependencia extra)
 *  - Tipografia: Segoe UI 13pt como fonte padrao
 *  - Tabelas com zebra striping, linhas mais altas e header destacado
 *  - Bordas/paddings padronizados nos paineis
 */
public final class Tema {

    public static final Color  COR_PRIMARIA   = new Color( 25, 118, 210);   // azul
    public static final Color  COR_ACENTO     = new Color( 79, 144, 199);
    public static final Color  COR_ZEBRA      = new Color(244, 247, 251);
    public static final Color  COR_HEADER_BG  = new Color(220, 232, 246);
    public static final Color  COR_GRID       = new Color(215, 220, 226);
    public static final Color  COR_SELECAO    = new Color(186, 210, 240);

    public static final Font   FONTE_PADRAO   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font   FONTE_NEGRITO  = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font   FONTE_TITULO   = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font   FONTE_MONO     = new Font("Consolas", Font.PLAIN, 12);

    private Tema() {}

    /** Aplica o L&F Nimbus + fontes/cores padrao. Chamar uma vez no main(). */
    public static void aplicar() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // Se Nimbus nao estiver disponivel, segue com o padrao
        }

        UIManager.put("control",            new Color(245, 247, 250));
        UIManager.put("nimbusSelectionBackground", COR_SELECAO);
        UIManager.put("nimbusFocus",        COR_ACENTO);
        UIManager.put("nimbusOrange",       COR_PRIMARIA);

        UIManager.put("defaultFont",        FONTE_PADRAO);
        UIManager.put("Label.font",         FONTE_PADRAO);
        UIManager.put("Button.font",        FONTE_NEGRITO);
        UIManager.put("ComboBox.font",      FONTE_PADRAO);
        UIManager.put("TextField.font",     FONTE_PADRAO);
        UIManager.put("FormattedTextField.font", FONTE_PADRAO);
        UIManager.put("TextArea.font",      FONTE_PADRAO);
        UIManager.put("Table.font",         FONTE_PADRAO);
        UIManager.put("TableHeader.font",   FONTE_TITULO);
        UIManager.put("TabbedPane.font",    FONTE_NEGRITO);
        UIManager.put("TitledBorder.font",  FONTE_NEGRITO);
        UIManager.put("OptionPane.messageFont", FONTE_PADRAO);
        UIManager.put("OptionPane.buttonFont",  FONTE_NEGRITO);

        UIManager.put("Table.rowHeight",    26);
        UIManager.put("Table.gridColor",    COR_GRID);
    }

    /**
     * Estiliza uma JTable: zebra striping, linhas mais altas, header destacado.
     *
     *  Pode ser chamado mais de uma vez sem efeito colateral.
     */
    public static void estilizarTabela(JTable t) {
        if (t == null) return;

        t.setRowHeight(26);
        t.setShowGrid(true);
        t.setGridColor(COR_GRID);
        t.setIntercellSpacing(new Dimension(4, 2));
        t.setSelectionBackground(COR_SELECAO);
        t.setSelectionForeground(Color.BLACK);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);

        JTableHeader h = t.getTableHeader();
        h.setBackground(COR_HEADER_BG);
        h.setForeground(COR_PRIMARIA);
        h.setFont(FONTE_TITULO);
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 30));

        // Zebra striping atraves de renderer customizado
        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, col);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COR_ZEBRA);
                    c.setForeground(Color.DARK_GRAY);
                }
                return c;
            }
        };
        t.setDefaultRenderer(Object.class,  zebra);
        t.setDefaultRenderer(String.class,  zebra);
        t.setDefaultRenderer(Number.class,  zebra);
    }

    /** Borda interna padronizada para paineis principais. */
    public static Border bordaPainel() {
        return BorderFactory.createEmptyBorder(10, 10, 10, 10);
    }

    /** Titled border com fonte/cor padronizadas. */
    public static Border bordaTitulada(String titulo) {
        Border externa = BorderFactory.createMatteBorder(0, 0, 0, 0, COR_GRID);
        Border interna = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_GRID, 1, true),
                titulo, 0, 0, FONTE_NEGRITO, COR_PRIMARIA);
        return BorderFactory.createCompoundBorder(externa, interna);
    }
}
