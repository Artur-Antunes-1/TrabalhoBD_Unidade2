import conexao.ConexaoBanco;
import ui.JanelaPrincipal;

import javax.swing.*;
import java.sql.Connection;

/**
 * Ponto de entrada da aplicacao.
 *
 *  1. Tenta abrir uma conexao de teste para falhar cedo se o banco
 *     nao estiver acessivel (mostrando uma mensagem clara).
 *  2. Sobe a janela principal (ui.JanelaPrincipal).
 *
 *  Para rodar:    mvn -q exec:java -Dexec.mainClass=Principal
 *  ou pela IDE:   botao direito em Principal.java -> Run.
 */
public class Principal {

    public static void main(String[] args) {
        // Teste rapido de conexao com mensagem amigavel se falhar
        try (Connection c = ConexaoBanco.obterConexao()) {
            System.out.println("[OK] Conexao com o banco estabelecida.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Nao foi possivel conectar ao banco MySQL.\n\n" +
                    "Verifique se:\n" +
                    "  - O servico do MySQL esta rodando.\n" +
                    "  - O banco 'supermercado' foi criado " +
                          "(rode os scripts em sql/ na ordem 01..04).\n" +
                    "  - Usuario e senha estao corretos em ConexaoBanco.java.\n\n" +
                    "Detalhe: " + ex.getMessage(),
                    "Erro de conexao", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Inicia a UI Swing na thread correta
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }
}
